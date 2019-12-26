package edu.gatech.cse6242;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.io.IOException;
// First step map
public class Q4 {
	
	public static class Map extends Mapper<Object, Text, IntWritable, IntWritable> {
		private IntWritable source = new IntWritable();
		private final IntWritable one = new IntWritable(1);
		private final IntWritable minusone = new IntWritable(-1);
		
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
			StringTokenizer Tokenizer = new StringTokenizer(value.toString(), "\t");
			
			source.set(Integer.valueOf(Tokenizer.nextToken()));
			context.write(source, one);
			source.set(Integer.valueOf(Tokenizer.nextToken()));
			context.write(source, minusone);
		}
	}
	
	public static class Reduce extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable>{
		private IntWritable diff = new IntWritable();
		public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException{
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();	
			}
			diff.set(sum);
			context.write(key, diff);
		}
	}
	
	public static class diffMapper extends Mapper<Object, Text, IntWritable, IntWritable>{
		private final IntWritable one = new IntWritable(1);
		private IntWritable diff = new IntWritable();
		
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer tokenizer = new StringTokenizer(value.toString(), "\t");
			
			tokenizer.nextToken();
			diff.set(Integer.valueOf(tokenizer.nextToken()));
			context.write(diff, one);
		}
	}
	
	public static class diffReducer extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable>{
		private IntWritable result = new IntWritable();
		
		public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}

    public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job1 = Job.getInstance(conf, "job1");

	job1.setJarByClass(Q4.class);
    job1.setMapperClass(Map.class);
    job1.setReducerClass(Reduce.class);
    job1.setOutputKeyClass(IntWritable.class);
    job1.setOutputValueClass(IntWritable.class);
	
    FileInputFormat.addInputPath(job1, new Path(args[0]));
    FileOutputFormat.setOutputPath(job1, new Path("first output"));
	job1.waitForCompletion(true);
	
	Job job2 = Job.getInstance(conf, "job2");

	job2.setJarByClass(Q4.class);
    job2.setMapperClass(diffMapper.class);
    job2.setReducerClass(diffReducer.class);
    job2.setOutputKeyClass(IntWritable.class);
    job2.setOutputValueClass(IntWritable.class);
	
    FileInputFormat.addInputPath(job2, new Path("first output"));
    FileOutputFormat.setOutputPath(job2, new Path(args[1]));
    System.exit(job2.waitForCompletion(true) ? 0 : 1);
  }
}
