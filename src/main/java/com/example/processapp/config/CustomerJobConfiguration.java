package com.example.processapp.config;

import com.example.processapp.dto.CustomerDTO;
import com.example.processapp.entity.CustomerInfo;
import com.example.processapp.repository.CustomerInfoRepository;
import com.example.processapp.repository.InvalidCustomerInfoRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class CustomerJobConfiguration {
    public JobBuilderFactory jobBuilderFactory;
    public StepBuilderFactory stepBuilderFactory;
    public CustomerInfoRepository customerInfoRepository;
    public InvalidCustomerInfoRepository invalidCustomerInfoRepository;
    private CustomerItemWriter customerItemWriter;
    private DataSource dataSource;
    private CustomerProcessor customerProcessor;

    /**
     * The default value for this field is {@value}.
     */
    public static final String INPUT_RESOURCE = "input/1M-customers.txt";
    public static final String VALID_CUSTOMER_RESOURCE = "output/customer_output.csv";
    public static final String INVALID_CUSTOMER_RESOURCE = "output/invalid_customer_output.csv";

    /**
     * This is the reader method which read the text file using line mapper.
     * its take file from the specified location of project
     *
     * @return FlatFileItemReader<CustomerInfo>.
     *
     */
    @Bean
    public FlatFileItemReader<CustomerInfo> reader() {
        FlatFileItemReader<CustomerInfo> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource(INPUT_RESOURCE));
        itemReader.setName("textDataReader");
        //itemReader.setLinesToSkip(0);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }


    /**
     * This method helps to map the line. its split the data using comma
     * and map the data with the object
     *
     * @return LineMapper<CustomerInfo>.
     *
     */
    private LineMapper<CustomerInfo> lineMapper() {
        DefaultLineMapper<CustomerInfo> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("firstName", "lastName", "city", "country", "zipCode", "phoneNumber", "email", "ipAddress");

        BeanWrapperFieldSetMapper<CustomerInfo> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CustomerInfo.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;

    }

    /**
     * This method helps to process the valid and invalid data using custom processor
     *
     * @return CustomerItemProcessor.
     *
     */
    @Bean
    public CustomerItemProcessor processor() {
        return new CustomerItemProcessor();
    }

    @Bean
    public ColumnRangePartitioner partitioner() {
        return new ColumnRangePartitioner();
    }

    @Bean
    public PartitionHandler partitionHandler() {
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setGridSize(4);
        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
        taskExecutorPartitionHandler.setStep(dataProcessStep());
        return taskExecutorPartitionHandler;
    }

    /**
     * This Step method allows the read, process and write the data.
     * It's skip the Data integrity violation exception upto 1000.
     * because if there are any duplicate email or phone number it will hamper the total process.
     *
     * @return Step.
     */
    @Bean
    public Step dataProcessStep() {
        return stepBuilderFactory.get("dataProcessStep").<CustomerInfo, CustomerInfo>chunk(1000)
                .reader(reader())
                .processor(processor())
                .writer(customerItemWriter)
                .faultTolerant()
                .skipLimit(1000)
                .skip(DataIntegrityViolationException.class)
                .build();
    }

    /**
     * This Step called data process step with partitioning.
     * Task will be divided in parallel processing
     *
     * @return Step
     */
    @Bean
    public Step topStep() {
        return stepBuilderFactory.get("topStep").
                partitioner(dataProcessStep().getName(), partitioner())
                .partitionHandler(partitionHandler())
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(4);
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setQueueCapacity(4);
        return taskExecutor;
    }


    /**
     * This method helps to read data from DB(valid customer table) .     *
     *
     * @return this return a CustomerInformation
     */
    @Bean
    public ItemStreamReader<CustomerInfo> customerInfoDBReader() {
        JdbcCursorItemReader<CustomerInfo> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("select * from customer_infos order by id");
        reader.setRowMapper(new CustomerInfoDBRowMapper());
        return reader;
    }

    /**
     * This Step calls the reader, processor and writer for write data into a csv file*
     *
     * @return a Step that call by job
     * @exception Exception On writing file.
     */
    @Bean
    public Step customerFileStep() throws Exception {
        return this.stepBuilderFactory.get("customerFileStep")
                .<CustomerInfo, CustomerDTO>chunk(100)
                .reader(customerInfoDBReader())
                .processor(customerProcessor)
                .writer(customerInfoFileWriter())
                .build();
    }

    @Bean
    public ItemWriter<CustomerDTO> customerInfoFileWriter() throws Exception {
        FlatFileItemWriter<CustomerDTO> writer = new FlatFileItemWriter<>();

        writer.setResource(new FileSystemResource(VALID_CUSTOMER_RESOURCE));
        writer.setLineAggregator(new DelimitedLineAggregator<CustomerDTO>() {
            {
                setFieldExtractor(new BeanWrapperFieldExtractor<CustomerDTO>() {
                    {
                        setNames(new String[]{"id", "firstName", "lastName", "email", "phoneNumber", "city", "country", "zipCode", "ipAddress"});
                    }
                });
            }
        });
        writer.setHeaderCallback(new FlatFileHeaderCallback() {
            @Override
            public void writeHeader(Writer writer) throws IOException {
                writer.write("id,firstName, lastName, email,phoneNumber,city, country, zipCode, ipAddress");
            }
        });
        writer.setShouldDeleteIfExists(true);
        return writer;
    }

    //
    @Bean
    public ItemStreamReader<CustomerInfo> invalidCustomerInfoDBReader() {
        JdbcCursorItemReader<CustomerInfo> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("select * from invalid_customer_infos order by id");
        reader.setRowMapper(new CustomerInfoDBRowMapper());
        return reader;
    }

    @Bean
    public Step inValidCustomerFileStep() throws Exception {
        return this.stepBuilderFactory.get("inValidCustomerFileStep")
                .<CustomerInfo, CustomerDTO>chunk(100)
                .reader(invalidCustomerInfoDBReader())
                .processor(customerProcessor)
                .writer(invalidCustomerInfoFileWriter())
                .build();
    }

    @Bean
    public ItemWriter<CustomerDTO> invalidCustomerInfoFileWriter() throws Exception {
        FlatFileItemWriter<CustomerDTO> writer = new FlatFileItemWriter<>();

        writer.setResource(new FileSystemResource(INVALID_CUSTOMER_RESOURCE));
        writer.setLineAggregator(new DelimitedLineAggregator<CustomerDTO>() {
            {
                setFieldExtractor(new BeanWrapperFieldExtractor<CustomerDTO>() {
                    {
                        setNames(new String[]{"id", "firstName", "lastName", "email", "phoneNumber", "city", "country", "zipCode", "ipAddress"});
                    }
                });
            }
        });
        writer.setHeaderCallback(new FlatFileHeaderCallback() {
            @Override
            public void writeHeader(Writer writer) throws IOException {
                writer.write("id,firstName, lastName, email,phoneNumber,city, country, zipCode, ipAddress");
            }
        });
        writer.setShouldDeleteIfExists(true);
        return writer;
    }

    /**
     * This Job first read the customers data from a text file and save all in the DB.
     * In the second step it read the valid customers and write in a csv file
     * In the third step it read the invalid customer and write in another csv file.
     *
     */

    @Bean
    public Job runJob() throws Exception {

        return jobBuilderFactory.get("processCustomersInfoJob")
                .flow(topStep())
                .next(customerFileStep())
                .next(inValidCustomerFileStep())
                .end().build();
    }

}
