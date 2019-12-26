package edu.gatech.cse6242;

import java.io.IOException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Q1 {

	public static class Map extends Mapper<LongWritable, Text, Text, Text>{
		
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String line = value.toString();
			String[] lines = line.split("\t");
			String sourceId = lines[0];
			String targetId = lines[1];
			String weight = lines[2];
			context.write(new Text(sourceId), new Text(targetId+","+weight));
		}
	}

    public static class Reduce extends Reducer<Text, Text, Text, Text> {
		
		public void reduce(Text key, Iterable<Text> value, Context context) throws IOException, InterruptedException {
	int maxWeight = Integer.MIN_VALUE;
	int maxTarget = Integer.MIN_VALUE;
	for (Text x: value){
		String y = x.toString();
		String[] target_weight = y.split(",");
		String target = target_weight[0];
		String weight = target_weight[1];
		if(maxWeight < Integer.parseInt(weight)){
			maxWeight = Integer.parseInt(weight);
			maxTarget = Integer.parseInt(target);
		}
		else if(maxWeight == Integer.parseInt(weight)){
			maxTarget = Math.min(maxTarget,Integer.parseInt(target));
		}	
				
	}
	context.write(key,new Text( maxTarget+","+maxWeight));
}
}
  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Q1");
	job.setJarByClass(Q1.class);
	job.setMapperClass(Map.class);
	job.setReducerClass(Reduce.class);
	job.setOutputKeyClass(Text.class);
	job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}


