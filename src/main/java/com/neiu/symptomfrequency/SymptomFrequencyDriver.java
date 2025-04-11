package com.neiu.symptomfrequency;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class SymptomFrequencyDriver {
    public static void main(String[] args) throws Exception { // Accepts command-line arguments and may throw exceptions.
        if (args.length != 2) { //require the input text file path & output file path
            System.err.println("Invalid Command");
            System.err.println("Usage: SymptomFrequencyDriver <input path> <output path>");
            System.exit(0);
        } //basically, the job cannot run unless specified the input file and the output path

        Configuration conf = new Configuration();
        //initialize a configuration, and by default picks up the config of the Hadoop environment

        Job job = Job.getInstance(conf, "Symptom Frequency by Age Group");
        //create a new job object called “Symptom Frequency by Age Group”.
//❗This will hold all the specifications that we are going to use to run the job: which is the mapper, which is the reducer,
//      where is the input file path, and the output file path

        job.setJarByClass(SymptomFrequencyDriver.class);
        //this is the jar that needs to be deployed across the node and run. Has to be specified the file that contains the main()

        //set the input and output files, FileInputFormat is a hadoop class that defines the input format
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        //the input path can be either a file, it can be a directory, or you can also specify a pattern which the file will match.
        // The output path, must be a directory that does not exist, is not created yet. Hadoop will create it and place the output in that directory

        job.setMapperClass(SymptomFrequencyMapper.class);
        job.setReducerClass(SymptomFrequencyReducer.class);
        // say that the Mapper & Reducer class points at the SymptomFMapper andSymptomFReducer

        job.setCombinerClass(SymptomFrequencyReducer.class);
        // Set the COMBINER class to  be the same as the Reducer (because the Reducers just sums counts)
        // A combiner will pre-aggregate data locally before sending it to the reducer.

        job.setPartitionerClass(HashPartitioner.class);
        //set the custom partitioner class to control key distribution across reducers.

        job.setNumReduceTasks(2);
        // Set the number of reduce tasks to 2.
        // This will run 2 reducers in parallel to process the intermediate key-value pairs.

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        //explicitly set the type of output keys and values of the Reducer class.
        // This has to match the types we specified in our SymptomFrequencyReducer class



        System.exit(job.waitForCompletion(true) ? 0 : 1); //submits the job to Hadoop and wait for it’s
    }                                  //completion. True if ran successfully
}
// ? 0 : 1- ternary operator ( concise way to write an if-else statement.). If the condition (job.waitForCompletion(true)) is true (the job succeeds), the expression returns 0.
// true = job progress will be printed on screen

/*  * args[0] is the first command-line argument passed when running the Hadoop job (in terminal) -input file
    * new Path(args[0]) converts this argument into a Hadoop Path object
     (which represents a file or directory path in HDFS)

 */