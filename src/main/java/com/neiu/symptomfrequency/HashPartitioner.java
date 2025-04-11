package com.neiu.symptomfrequency;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class HashPartitioner extends Partitioner<Text, IntWritable> {
    @Override
    public int getPartition(Text key, IntWritable value, int numPartitions) {
        //takes in 3 parameters: key and value from the mapper & number of reducers from the Driver class

        return (key.hashCode() & Integer.MAX_VALUE) % numPartitions;
        // Compute the hash code of the key, ensure it's non-negative with Integer.MAX_VALUE
        // then takes the reminder when divided by the number of partitions
    }
}
