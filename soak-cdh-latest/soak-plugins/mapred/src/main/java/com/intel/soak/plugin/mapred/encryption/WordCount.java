package com.intel.soak.plugin.mapred.encryption;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
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
 * Date: 11/20/13
 * Time: 11:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class WordCount extends Configured implements Tool {

    public static class TokenizerMapper extends MapReduceBase
            implements Mapper<LongWritable, Text, Text, IntWritable> {

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
            implements Reducer<Text, IntWritable, Text, IntWritable> {

        private JobConf conf;

        public void configure(JobConf conf) {
            this.conf = conf;
        }

        public void reduce(Text key, Iterator<IntWritable> values,
                           OutputCollector<Text, IntWritable> output,
                           Reporter reporter) throws IOException {
            int sum = 0;
            while (values.hasNext()) {
                sum += values.next().get();
            }
            output.collect(key, new IntWritable(sum));
        }

        /*
        public void close() {
            try {
                FileSystem fs = FileSystem.get(conf);
                fs.deleteOnExit(new Path(conf.get("custom.output")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        */
    }


    protected JobConf jobConf;

    @Override
    public int run(String[] strings) throws Exception {
        try {
            this.jobConf = new JobConf(getConf(), this.getClass());
            jobConf.setMapperClass(TokenizerMapper.class);
            jobConf.setCombinerClass(IntSumReducer.class);
            jobConf.setReducerClass(IntSumReducer.class);
            jobConf.setOutputKeyClass(Text.class);
            jobConf.setOutputValueClass(IntWritable.class);
            FileInputFormat.addInputPath(jobConf, new Path(strings[0]));
            FileOutputFormat.setOutputPath(jobConf, new Path(strings[1]));
            //jobConf.set("custom.output", strings[1]);

            JobClient jc = new JobClient(jobConf);
            RunningJob rj = jc.submitJob(jobConf);
            try {
                if (!jc.monitorAndPrintJob(jobConf, rj)) {
                    System.out.println("Job Failed: " + rj.getFailureInfo());
                    throw new IOException("Job failed!");
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            return rj.isSuccessful() ? 0 : -1;
        } catch (Throwable e) {
            e.printStackTrace();
            return -1;
        }
    }

}
