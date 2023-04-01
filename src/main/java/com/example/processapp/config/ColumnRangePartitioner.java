package com.example.processapp.config;
/**
 * The class helps to partition the process
 *
 * @version 1.0
 * @author Bappi Mazumder
 * @since 2023-03-31
 */
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class ColumnRangePartitioner implements Partitioner {


    /**
     * This method partition the process for parallel processing
     *
     * @param gridSize This is parameter to partition method
     *
     *
     */

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int min = 1;
        int max = 1231081;
        int targetSize = (max - min) / gridSize + 1;//500
       // System.out.println("targetSize : " + targetSize);
        Map<String, ExecutionContext> result = new HashMap<>();

        int number = 0;
        int start = min;
        int end = start + targetSize - 1;
        //1 to 500
        // 501 to 1000
        while (start <= max) {
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + number, value);

            if (end >= max) {
                end = max;
            }
            value.putInt("minValue", start);
            value.putInt("maxValue", end);
            start += targetSize;
            end += targetSize;
            number++;
        }
       // System.out.println("partition result:" + result.toString());
        return result;
    }
}
