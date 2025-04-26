# **Overview**
This project implements a Hadoop MapReduce job to compute frequency of symptoms group by age range, using a Mac.
It is designed for beginners who are new to Hadoop and distributing computing
It consists of three Java classes written in IntelliJ:
1. SymptomFrequencyMapper - Extracts the age and the symptoms, and Maps the input csv into key-value pairs (age_range:symptom, 1>).
2. SymptomFrequencyReducer - Grouping the occurrences of the same key, and sums up the counts for each key.
3. SymptomFrequencyDriver - Configures and runs the Hadoop job.
4. HashPartition - Controls how the output from the Mapper is divided among the Reducer tasks. The partitioner decides which reducer a particular key-value pair goes to.

# **Steps to run the Project**:
# 1. Download the input file that we will work with from Kaggle website
- Dataset URL: [Dry Eye Disease DataSet](https://www.kaggle.com/datasets/dakshnagra/dry-eye-disease)

## 2. Set up Hadoop:  Make sure Hadoop is installed and running on your Mac.

## 3. Start Hadoop(HDFS and YARN):
* open terminal(on Mac: Cmd + space bar -->Terminal)
* start Hadoop by running on the terminal: "start-dfs.sh" and "start-yarn.sh"
* open the Hadoop web interface/browser to check if HDFS started: http://localhost:9870 -->
* you should see a green header with the message: "Overview: localhost:9000 active"
* If the page doesn't open up, HDFS wasn't configure properly

## 4. Upload the csv file to HDFS(Hadoop Distributed File System):
* create a directory called "test" in HDFS by running in the terminal: "hadoop fs -mkdir /test"
* go to the Hadoop web interface to check your work: . -->Utilities(top right hand side) -->Browse the file system --> you should see your newly created directory called test
* copy the csv dataset from your local machine to HDFS: in the terminal run "hadoop fs -copyFromLocal <localFilePath>  <hdfsPath>"
  * replace <LocalFilePath> with your csv file path. Go in the folder where csv is, right click on it -->get info-->the path is displayed there
  * eg. /Users/anedelcu/Lavinia_Nedelcu/School/Dry_Eye_Dataset.csv
  * replace <hdfsPath> with /test (= you want to copy the csv to the directory /test)
  * should look like: "hadoop fs -copyFromLocal /Users/anedelcu/Lavinia_Nedelcu/School/Dry_Eye_Dataset.csv /test"
- a. Verify if the transfer from local to HDFS was successful: 2 ways
    * check the web interface again, http://localhost:9870: in the /test should be the uploaded Dry_Eye_Dataset.csv
    * in the terminal, run "hadoop fs -ls /test" = this will display the content of the test directory
- b. Extra: display the content of the /test directory on the terminal: in the terminal run:
    * "hadoop fs -cat<fileName>" eg: "Hadoop fs -cat<Dry_Eye_Dataset.csv>"



## 5.Run the MapReduce job:
- a.in the terminal run the job using "hadoop jar" command:  `hadoop jar <JARpath> <mainclass> <inputfile> <outputdirectory>`
  * replace <JARpath> with full path to the Jar
  * `<mainclass>`: must include its full path. If a package was created, ensure the package name is included.
    eg: main class is SymptomFrequencyDriver within the package com.neiu.symptomfrequency, it should be specified as com.neiu.symptomfrequency.SymptomFrequencyDriver
  * `<outputdirectory>`: doesn't have to exist beforehand, Hadoop will create it automatically.
    eg. "test/output" = put the output file in the "/output" directory located in the test directory we created
  * full command in my case:"hadoop jar /User/anedelcu/hadoop-install/hadoop-3.4.1/Java-work/symptomfrequency/target/symptomfrequency.jar 
    com.neiu.symptomfrequency.SymptomFrequencyDriver /test/Dry_Eye.Dataset.csv /test/output"
  * !!   If the job fails during execution, you must change the output directory name before re-running the command; otherwise, it will not work
- b.check the Hadoop web browser:  http://localhost:9870: in the /test next to Dry_Eye_Dataset.csv you should have the "output" directory.
      Open it up and if the job was successful you should see 2 or more file: _SUCCESS, part-r-0000 and part-r-0001(if multiple reducers are used)

## 6. View the output on the terminal  
* in the terminal run `hadoop fs -cat /test/output/part-r-00000`

## 7.Copy the output to your local system(if you want)
* The Reducer writes the final aggregated results to HDFS, so you can copy to local machine
* `hdfs dfs -copyToLocal /test/output /local/path`

## 8. If re-running the job
* HDFS does not allow overwriting directories.
* before re-running the job, delete the previous output directory: 'hdfs dfs -rm -r /test/output`

#MAPREDUCE JOB IS RUN SUCCESSFULLY!

# MAPPERS, REDUCERS AND THE SIZE OF DATA BLOCKS
## 1. HDFS block size
* as we know, the input is split into block, and these blocks are located on DataNodes. Each HDFS block by default is 128 MB
* the size can be changed, but is not always recommended as it might drag down the process
* 2 ways to change:
*  ** globally, edit the hdfs-site.xml file: <pre > <property>
                                                              <name>dfs.blocksize</name>
                                                              <value>268435456</value> <!-- 256 MB -->
                                                          </property> </pre>
  ** set it for a specific file during upload: in the terminal run`hadoop fs -D dfs.blocksize=268435456 -put localfile /hdfs/path`

## 2. Configuring the Number of Mappers
* the number of mappers is not directly set by the user. Instead, it is determined by the number of block the input was split in
* the number of mappers is determined by YARN
* eg. input data was split into 7 block = 7 mappers will run when we run the MapReduce job
* You can change the input split size to control the number of mappers: in the driver class add the following code snippet
 `Configuration conf = new Configuration();
conf.set("mapreduce.input.fileinputformat.split.maxsize", "268435456"); // 256 MB
Job job = Job.getInstance(conf, "My Job");`


## 3. Configuring the Number of Reducers

The number of reducers is fully configurable by the user.

It can be set in three ways:

### 3.1 Globally (edit `mapred-site.xml`)

Edit the `mapred-site.xml` file and set the property:
`<property>
  <name>mapreduce.job.reduces</name>
  <value>10</value> <!-- Set to 10 reducers -->
</property> `

### 3.2 In the Driver Class (programatically)
Use the setNumReduceTasks() method:  
` job.setNumReduceTasks(10);` // Set to 10 reducers for example  

### 3.3 In the terminal when you submit the hadoop jar command  

`hadoop jar symptomfrequency.jar com.neiu.symptomfrequency.SymptomFrequencyDriver -D mapreduce.job.reduces=10 /input /output`  
here ` -D mapreduce.job.reduces= 10` will set the job to run with 10 reducers



 # COMBINER AND The PARTITIONER
## 1. Combiner:
* the combiner runs at each DataNode where the mapper executes
* takes the output of the mapper,processes and reduces it before sending it over the network to the reducers
* in our case: using the same logic as the reducer to pre-sum counts
* eg: you need to specify the "job.setCombinerClass(SymptomFrequencyReducer.class);" in the Driver class to set up your combiner
* however, not all reducers can be directly used as combiners
* Hadoop itself decides whether to use the Combiner, even if it has been specified in the code.

## 2. Partitioner:
* The partitioner runs after the combiner (if used) on the mapper node.
* takes the output of each combiner(if used)/ or of each map() and it splits in partitions
* It determines which reducer will receive each key-value pair.
* number of reducers = number of partitions
* In our case: HashPartitioner assigns keys to reducers based on their hash value.
* to set up partitions: You need to specify the "job.setPartitionerClass(HashPartitioner.class);" in the Driver class.
* Ensures even data distribution across reducers to prevent workload imbalance.
* By default, Hadoop uses HashPartitioner, but a custom partitioner can be implemented if needed.


## STEP-BY-STEP DATA FLOW WITH INPUT/OUTPUT
1. Mapper Execution (on DataNodes):
      * input:A line from the CSV file, example "Male,22,Y,N,Y"
      * processing: Extracts age and symptoms, assigns an age group, and emits key-value pairs
      * output: <key(Text),val(IntWritable)>, eg. <18-25:EyeStrain, 1>, <18-25:Itchiness, 1>
2. Combiner Execution (Optional, on DataNodes, after Mapper):
      * input: Key-value pairs from the mapper, e.g. multiple occurrences of <18-25:EyeStrain, 1>
      * processing: Aggregates (sums) the values locally for each key
      * output: <key (Text), val (IntWritable)>, e.g. <18-25:EyeStrain, 2>, <18-25:Itchiness, 2>
3. Partitioner (on the Mapper Node, before the Shuffle Phase):
      * input: Key-value pairs from the combiner (or directly from the mapper if no combiner is used)
      * processing: Uses a hash function to assign each key to a partition based on the total number of reducers
      * output: Same key-value pairs tagged with a reducer assignment, e.g. <18-25:EyeStrain, 2> is assigned to Reducer 0
4. Shuffle & Sort (Data Transfer Over Network to a different DataNode where everything is collected)
      * input: Partitioned key-value pairs from different mapper nodes.
      * processing: Groups identical keys from different mappers together. Sorts the values before sending them to the reducers.
      * output: Sorted and grouped key-value pairs assigned to reducers. "18-25:EyeStrain" → [2,3,4] (Collected from multiple nodes)
5. Reducer Execution(on different nodes)
      * input: the <key(Text), val(IntWritable)> from shuffle and sort. eg <18-25:EyeStrain, [2,3,4]>
      * processing: Sums up the occurrences of each symptom for the age group
      * output: final <key(Text), val(IntWritable)> eg. <18-25:EyeStrain, 9>
