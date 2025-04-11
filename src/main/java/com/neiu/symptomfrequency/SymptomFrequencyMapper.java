package com.neiu.symptomfrequency;

import org.apache.hadoop.io.IntWritable;   //Hadoop specific wrappers for Java types
import org.apache.hadoop.io.Text;       //IntWritable=int, text=String, longWritable=long
import org.apache.hadoop.mapreduce.Mapper;  //base class for creating mapper clas
import java.io.IOException;

public class SymptomFrequencyMapper extends Mapper<Object, Text, Text, IntWritable> {
    //<input key type, input value type, output key type, output value type>
    //eg: inputs we have <Male,22,Y,N,Y>, and the outputs <age_symptom,1>
    private Text ageGroupKey = new Text();
    private final static IntWritable one = new IntWritable(1);

    public void map(Object key, Text value, Context context) //called for each input <key, value>
            throws IOException, InterruptedException { //once the <key, val> is processed, we write the output into a context obj

        //String[] fields = value.toString().split(",");
        String line = value.toString(); // Converts the input of map(value) to a String
        String[] fields = line.split(","); // Splits the line of text into an array of fields using comma as the delimiter

        // Skip header row(if 1st row is gender it means is the header row)
        if (fields[0].equals("Gender")) return;

        try {
            //.trim() method - removes spaces, tabs, or newline characters from a string(cleans)
            //ensures that the extracted values from fields[] do not contain unwanted spaces
            int age = Integer.parseInt(fields[1].trim()); // Age column -column numbers are taken from csv file
            //Integer.parseInt - it converts the clean string from field 1 to an integer
            String eyeStrain = fields[22].trim(); // Eye-strain column
            String redness = fields[23].trim(); // Redness column
            String itchiness = fields[24].trim(); // Itchiness column


            // Define age group
            String ageGroup;
            if (age >= 18 && age <= 25) ageGroup = "18-25";
            else if (age >= 26 && age <= 35) ageGroup = "26-35";
            else ageGroup = "36-45";

            // Emit key-value pairs if symptom is present (assuming 'Y' = yes)
            if (eyeStrain.equalsIgnoreCase("Y")) { //equalsIgnoreCase("Y") method ensures that both "Y" and "y" are considered a match.
                ageGroupKey.set(ageGroup + ":EyeStrain"); //if symptom is present, we are setting the key by combining the age + symptom
                context.write(ageGroupKey, one); //writes the output <key, val>
            }
            if (redness.equalsIgnoreCase("Y")) {
                ageGroupKey.set(ageGroup + ":Redness");
                context.write(ageGroupKey, one); //writes the output <key, val>
            }
            if (itchiness.equalsIgnoreCase("Y")) {
                ageGroupKey.set(ageGroup + ":Itchiness");
                context.write(ageGroupKey, one); //writes the output <key, val>
            }
        } catch (NumberFormatException e) {
            // Ignore invalid age values
        }
    }
}
/*Context Context: part of Hadoop framework.
 * it stores the output
 * It's used to communicate across the MapReduce system.
 * the way we communicate between the mapper, the reducer, the merge, the sort and any other part of the MapReduce infrastructure.
 */

/*try-catch
*  prevents the mapper from crashing on invalid age fields.
* Integer.parseInt may throw NumberFormatException for non-numeric values.
* This ensures the job continues despite encountering bad data.
*/