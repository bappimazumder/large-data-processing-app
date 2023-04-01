/**
 * The class allows to process the customer data by reading the text file and
 * save it to DB and show expected output
 *
 * @version 1.0
 * @author Bappi Mazumder
 * @since 2023-03-31
 */


package com.example.processapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/customers")
public class CustomerInfoController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;


    /**
     * This method call a job, and then it read the text file and save to DB and then
     * read the customer information from DB and write into a file.
     * valid customer and invalid customer are identified
     *
     * @return  a modelandview  file with execution time
     */
    @GetMapping("/importCustomers")
    public ModelAndView importCustomerTextToDB() {
        ModelAndView modelAndView = new ModelAndView();
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("JobID", System.currentTimeMillis()).toJobParameters();
        try {
            Instant start = Instant.now();
            jobLauncher.run(job, jobParameters);
            Instant end = Instant.now();
            Duration timeElapsed = Duration.between(start, end);
            modelAndView.addObject("executionTime",timeElapsed.getSeconds());
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            e.printStackTrace();
        }
        modelAndView.setViewName("home");
        return modelAndView;
    }
}
