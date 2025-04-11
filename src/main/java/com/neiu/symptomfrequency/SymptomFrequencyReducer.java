package com.neiu.symptomfrequency;

import org.apache.hadoop.io.IntWritable;//Hadoop specific wrappers for Java types
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import java.io.IOException;

public class SymptomFrequencyReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
    //<input key type, input value type, output key type, output value type>
    //the input val in our case is a list, but we have IntWritable ?! --> the reducer always gets a list of val for every input key
    //eg. input key - text= 18-25:EyeStrain, input value- IntWritable=1
    //      output value - text= same as input key, output value - IntWritable = sum of occurrences
    private IntWritable result = new IntWritable(); //store the sum of counts/number of occurrences

    public void reduce(Text key, Iterable<IntWritable> values, Context context) //iterable list of 1's
            throws IOException, InterruptedException { //once the <key, val> is processed, we write the output into a context obj

        int sum = 0;
        for (IntWritable val : values) { //iterate through the list of values that was the function input
            sum += val.get(); //compute the sum of values (extract the int values from IntWritable obj & adds it to the sum)
        }
        result.set(sum); //updates the result with the final sum from the for loop
        context.write(key, result); //writes the output <key, val>
    }
}

