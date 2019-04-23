package yore;

import org.apache.hadoop.hive.ql.exec.UDAF;
import org.apache.hadoop.hive.ql.exec.UDAFEvaluator;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;

/**
 * 自定义聚合函数。
 * 用于计算表中乱码的数据条数占总数据条数的比值
 *
 * Created by yore on 2019-04-23 23:44
 */
@SuppressWarnings("deprecation")
public class RatioGarbled extends UDAF {

    public static class RatioGarbledEvaluator implements UDAFEvaluator {

        private DoubleWritable result;
        private IntWritable countNum;
        private IntWritable garbledNum;

        @Override
        public void init() {
            result = null;
        }


        /**
         * 聚合的多行中每行的被聚合的值都会被调用interate方法，所以这个方法里面我们来定义聚合规则
         *
         */
        public boolean iterate(String ... s){
            if(s==null || s.length< 1 ){
                return false;
            }

            return true;
        }



//        //iterate接收传入的参数，并进行内部的轮转。其返回类型为boolean。
//        public boolean iterate(String a,String b){
//            //每条数据的处理逻辑
//            if(a!=null||b!=null){
//                line += a + b;
//                return true;
//            }
//            line += "";
//            return true;
//        }
//
//        /*
//        * terminate返回最终的聚集函数结果
//        */
//        public String terminate(){
//            //最终的输出结果，这里返回的就是一个字符串
//            return line;
//        }
//
//        /*
//        * terminatePartial无参数，其为iterate函数轮转结束后，返回轮转数据，
//        * terminatePartial类似于hadoop的Combiner。
//        */
//        public String terminatePartial(){
//            //map阶段的输出结果
//            return line;
//        }
//
//        /*
//        * merge接收terminatePartial的返回结果，进行数据merge(合并)操作，其返回类型为boolean。
//        */
//        public boolean merge(String other){
//            return iterate(line,other); //a,b,c,d,e,f,g,h,
//        }

    }
}
