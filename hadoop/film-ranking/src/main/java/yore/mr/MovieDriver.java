package yore.mr;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yore.BeanUtils;
import yore.etl.MovieEntity;
import yore.etl.RankQuoteEntity;

import java.io.IOException;
import java.util.Arrays;

/**
 * MapReduce驱动类，
 * 提交命名：
 *      集群：hadoop jar film-ranking-1.0-SNAPSHOT.jar hdfs://cdh6:8020/home/douban/doubanMovie.csv hdfs://cdh6:8020/home/douban/rankQuote.csv hdfs://cdh6:8020/home/douban/out
 *           hadoop jar film-ranking-1.0-SNAPSHOT.jar hdfs://cdh6:8020/home/douban/doubanMovie.csv hdfs://cdh6:8020/home/douban/rankQuote.csv file:///home/out
 *
 * Created by yore on 2019/7/25 21:18
 */
public class MovieDriver extends Configured implements Tool {
    private static Logger LOG = LoggerFactory.getLogger(MovieDriver.class);
    private static StringBuilder params = new StringBuilder();
    static {
        params.append("\t 三个参数：\n")
                .append("\t\t 参数1 :\t csv文件1路径\n")
                .append("\t\t 参数2 :\t csv文件2路径\n")
                .append("\t\t 参数3 :\t 结果输出路径\n")
                .append("\t 或四个参数：\n")
                .append("\t\t 参数1 :\t csv文件1路径\n")
                .append("\t\t 参数2 :\t csv文件2路径\n")
                .append("\t\t 参数3 :\t 中间结果输出路径\n")
                .append("\t\t 参数4 :\t 最终结果路径\n");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("---------params-----------\n");
        System.out.println(Arrays.toString(args));
        if(args == null){
            System.err.println(params.toString());
            System.exit(1);
        }
        if(args.length == 3){
            System.out.println(params.toString().substring(0, params.indexOf("\t 或四个参数：\n")));
        }else if(args.length == 4){
            System.out.println(params.toString().substring(params.indexOf("\t 或四个参数：\n")));
        }else{
            System.err.println(params.toString());
            System.exit(1);
        }
        long start = System.currentTimeMillis();

        int exitCode = ToolRunner.run(new MovieDriver(), args);

        /*int exitCode = ToolRunner.run(new yore.mr.MovieDriver(), new String[]{
                "hdfs://cdh6:8020/home/douban/doubanMovie.csv",
                "hdfs://cdh6:8020/home/douban/rankQuote.csv",
                "hdfs://cdh6:8020/home/douban/out",
                "file:///Users/yoreyuan/Downloads/out"
        });*/

        long end = System.currentTimeMillis();
        System.out.println("本次共花费：" + (double)(end -start)/1000 + " 秒");
        System.exit(exitCode);
    }


    @Override
    public int run(String[] args) throws Exception {
        LOG.info("MR STARTED .... ");

        Configuration conf =  this.getConf();
         // reduce的key和value的分隔符，默认是制表符。 org.apache.hadoop.mapreduce.lib.output.TextOutputFormat
//        conf.set("mapred.textoutputformat.separator", "");
        conf.set("mapreduce.output.textoutputformat.separator", " ");

        // --------------------- job 1  -----------------------------
        Job job = Job.getInstance(conf, "movie join quote job");
        job.setNumReduceTasks(1);
        job.setPartitionerClass(MovieJoinPartitioner.class);

        job.setJarByClass(getClass());
        job.setMapperClass(MovieJoinMapper.class);
        job.setReducerClass(MovieJoinReducer.class);

        job.setMapOutputKeyClass(Text.class);       //Mapper的key输出类型
        job.setMapOutputValueClass(Text.class);     //Mapper的value输出类型
        job.setOutputKeyClass(Text.class);          //key输出类型
        job.setOutputValueClass(Text.class);        //value的输出类型

//        Path out = new Path(args[2]);
        Path out = args.length==3? new Path(args[2] + "-tmp"):new Path(args[2]);
        // 删除存在的文件，方便任务重跑
        out.getFileSystem(conf).delete(out,true);
        // 多文件作为输入
        MultipleInputs.addInputPath(job, new Path(args[0]), TextInputFormat.class, MovieJoinMapper.class);
        MultipleInputs.addInputPath(job, new Path(args[1]), TextInputFormat.class, MovieJoinMapper.class);
        // 输出格式化
        FileOutputFormat.setOutputPath(job, out);



        // --------------------- job 2  -----------------------------
          Job job2 = Job.getInstance(conf, "movie sort job");
        job2.setNumReduceTasks(1);
        job2.setPartitionerClass(RankPartitioner.class);
        job2.setGroupingComparatorClass(SecondSortGroupComparator.class);

        job2.setJarByClass(getClass());
        job2.setMapperClass(RankMapper.class);
        job2.setReducerClass(RankReducer.class);

        job2.setMapOutputKeyClass(MovieWritable.class);
        job2.setMapOutputValueClass(LongWritable.class);
        job2.setOutputKeyClass(MovieWritable.class);
        job2.setOutputValueClass(NullWritable.class);

        Path out2 = args.length==3? new Path(args[2]):new Path(args[3]);

        out2.getFileSystem(conf).delete(out2,true);
        FileInputFormat.addInputPath(job2, out);
        FileOutputFormat.setOutputPath(job2, out2);
        job2.setInputFormatClass(TextInputFormat.class);
        job2.setOutputFormatClass(TextOutputFormat.class);


        // VERBOSE OUTPUT
        /*if (job.waitForCompletion(true)) {
            LOG.debug("Job SUCCESSFULLY...");
            return 0;
        } else {
            LOG.debug("Job FAILED...");
            return 1;
        }*/


        //--------------------- 多job关联  -----------------------------
        ControlledJob cjob = new ControlledJob(conf);
        cjob.setJob(job);
        ControlledJob cjob2 = new ControlledJob(getConf());
        cjob2.setJob(job2);
        //设置依赖，也就说job2依赖job
        cjob2.addDependingJob(cjob);
        //设置总控制器，将第一个ControlledJob和第二个ControlledJob
        JobControl jc = new JobControl("douban-movie-group");
        //放入总控制器中
        jc.addJob(cjob);
        jc.addJob(cjob2);
        // 新建一个线程来运行已加入JobControl中的作业，开始进程并等待结束
        Thread jobControlThread = new Thread(jc);
        jobControlThread.start();
        while (!jc.allFinished()) {
            Thread.sleep(500);
        }
        jc.stop();
        if(args.length==3) out.getFileSystem(conf).delete(out,true);
        return 0;

    }

    // ------------------------ join -----------------------
    /**
     * Mapper类，多文件输入，根据主键，将组件相同的分到同一个分区
     *
     * @auther: yore
     */
    static class MovieJoinMapper extends Mapper<LongWritable, Text, Text, Text> {
        Text id = new Text();
        Text retain = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            if(StringUtils.isBlank(value.toString()))return;
            String[] lineArr = value.toString().split(",");
            try {
                if(3==lineArr.length){
                    RankQuoteEntity rankQuoteEntity = BeanUtils.copyToBean(value.toString(), RankQuoteEntity.class);
                    id.set(rankQuoteEntity.getId());
                    retain.set(rankQuoteEntity.toString());
                    context.write(id, retain);
                }else if(15==lineArr.length){
                    MovieEntity movieEntity = BeanUtils.copyToBean(value.toString(), MovieEntity.class);
                    id.set(movieEntity.getId());

                    StringBuilder sb = new StringBuilder();
                    sb.append(movieEntity.getId())
                            .append(",")
                            .append(movieEntity.getMovie_name())
                            .append(",")
                            .append(movieEntity.getRating_num())
                            .append(",")
                            .append(movieEntity.getRating_people())
                            ;
                    retain.set(sb.toString());

                    context.write(id, retain);
                }
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 自定义分区，相同id的分到同一份分区
     *
     * @auther: yore
     */
    static class MovieJoinPartitioner extends Partitioner<Text, Text>{
        @Override
        public int getPartition(Text key, Text value, int numPartitions) {
            // 根据主键哈希值分区，并且值不能超过Integer的最大值。
            return (key.hashCode() & Integer.MAX_VALUE) % numPartitions;
        }
    }

    /**
     * Reducer类，根据key合并
     *
     * @auther: yore
     */
    static class MovieJoinReducer extends Reducer<Text, Text, Text, Text> {
        Text t1 = new Text();
        Text t2 = new Text();

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String pre = "";
            String next = "";
            for(Text val : values){
                String valStr = val.toString();
                if(3==valStr.split(",").length){
                    next = valStr.substring(valStr.indexOf(","));
                }else{
                    pre = valStr;
                }
            }
            t1.set(pre);
            t2.set(next);
            context.write(t1, t2);
        }
    }



    // ------------------------ order -----------------------
    /**
     * Mapper类，用来对数据进行排序，获取评分最高且评论人数最多
     *
     * @auther: yore
     */
    public static class RankMapper extends Mapper<LongWritable, Text, MovieWritable, LongWritable> {

        private MovieWritable movieWritable = new MovieWritable();
        private LongWritable num = new LongWritable();

        @Override
        protected void map(LongWritable key, Text value, Context context) {
            if(StringUtils.isBlank(value.toString()))return;
            try {
//                System.out.println(value.toString());
                String[] valueArr = value.toString().split(",");
                //id,m.movie_name,m.rating_num,m.rating_people,q.rank,q.quote
                movieWritable.setId(valueArr[0]);
                movieWritable.setMovie_name(valueArr[1]);
                movieWritable.setRating_num(Float.parseFloat(valueArr[2].trim()));
                movieWritable.setRating_people(Long.parseLong(valueArr[3].trim()));
                movieWritable.setRank(valueArr[4]);
                movieWritable.setQuote(valueArr[5]);

                num.set(Long.parseLong(valueArr[3].trim()));

                context.write(movieWritable, num);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(value.toString());
            }
        }
    }

    /**
     * 自定义分区，尽量让相同评分的值分到同一个分区
     *
     * @auther: yore
     */
    public static class RankPartitioner extends Partitioner<MovieWritable, LongWritable>{

        @Override
        public int getPartition(MovieWritable movieWritable, LongWritable longWritable, int numPartitions) {
            return (movieWritable.getRating_num().hashCode() & Integer.MAX_VALUE) % numPartitions;
        }
    }

    static class SecondSortGroupComparator extends WritableComparator {
        // 对象MovieWritable.class注册，让比较器知道该对象并能够初始化
        protected SecondSortGroupComparator() {
            super(MovieWritable.class, true);
        }

        @Override
        public int compare(WritableComparable a, WritableComparable b) {
            if(a==null || b==null){
                return 0;
            }

            MovieWritable first = (MovieWritable)a;
            MovieWritable second = (MovieWritable)b;

            // 自定义按原始数据中第一个key分组
            return second.getRating_num().compareTo(first.getRating_num());
        }
    }

    /**
     * Reducer类
     *
     * @auther: yore
     */
    static class RankReducer extends Reducer<MovieWritable, LongWritable, MovieWritable, NullWritable> {

        @Override
        protected void reduce(MovieWritable key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
            //这里一定需要注意，经过前一步，已经根据评分分组排序了，并且同一分组内的也已经按评论人数降序排列。
            //这里需要将同一分组内的也循环输出
            for(LongWritable val : values){
                context.write(key, NullWritable.get());
            }
        }
    }

}
