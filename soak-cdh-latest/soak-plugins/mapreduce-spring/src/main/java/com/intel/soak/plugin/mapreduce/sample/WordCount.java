package com.intel.soak.plugin.mapreduce.sample;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.Tool;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 12/4/13
 * Time: 1:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class WordCount extends Configured implements Tool {

    public static class TokenizerMapper extends MapReduceBase
            implements org.apache.hadoop.mapred.Mapper<LongWritable, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(LongWritable key, Text value,
                        OutputCollector<Text, IntWritable> output,
                        Reporter reporter) throws IOException {
            String line = value.toString();
            StringTokenizer itr = new StringTokenizer(line);
            while (itr.hasMoreTokens()) {
                word.set(itr.nextToken());
                output.collect(word, one);
            }
        }
    }

    public static class IntSumReducer extends MapReduceBase
            implements org.apache.hadoop.mapred.Reducer<Text, IntWritable, Text, IntWritable> {

        public void reduce(Text key, Iterator<IntWritable> values,
                           OutputCollector<Text, IntWritable> output,
                           Reporter reporter) throws IOException {
            int sum = 0;
            while (values.hasNext()) {
                sum += values.next().get();
            }
            output.collect(key, new IntWritable(sum));
        }
    }

    @Override
    public int run(String[] strings) throws Exception {
        if (strings.length < 2) {
            return 1;
        }
        JobConf job = new JobConf(getConf(), WordCount.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(strings[0]));
        FileOutputFormat.setOutputPath(job, new Path(strings[1]));
        RunningJob rj = JobClient.runJob(job);
        return rj.isSuccessful() ? 0 : 1;
    }
}
