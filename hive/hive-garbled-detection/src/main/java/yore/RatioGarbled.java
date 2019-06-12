package yore;

import org.apache.hadoop.hive.ql.exec.UDAF;
import org.apache.hadoop.hive.ql.exec.UDAFEvaluator;

/**
 * 自定义聚合函数。
 * 用于计算表中乱码的数据条数占总数据条数的比值
 *
 * Created by yore on 2019-04-24 14:25
 */
@SuppressWarnings("deprecation")
public class RatioGarbled extends UDAF {

    public static class ResultState{
        private Double countNum;
        private Double garbledNum;
    }

    public static class RatioGarbledEvaluator implements UDAFEvaluator {
        ResultState resultState;

        public RatioGarbledEvaluator(){
            super();
            resultState = new ResultState();
            init();
        }

        /**
         * 用于 UDAF 函数初始化
         */
        @Override
        public void init() {
            resultState.countNum = .0;
            resultState.garbledNum = .0;
        }


        /**
         * 聚合的多行中每行的被聚合的值都会被调用interate方法，所以这个方法里面我们来定义聚合规则
         *
         * @auther: yore
         * @param s 传入的字符串值集合
         * @return boolean
         */
        public boolean iterate(String ... s){
            if(s==null || s.length< 1 ){
                return false;
            }
            resultState.countNum ++;
            if(Util.isMessyCode(s))
                resultState.garbledNum++;
            return true;
        }


        /**
         * terminatePartial无参数，其为iterate函数轮转结束后，返回轮转数据，
         * terminatePartial类似于hadoop的Combiner。
         *
         * @auther: yore
         * @return ResultState 结果状态对象
         */
        public ResultState terminatePartial(){
            return resultState.countNum==0? null:resultState;
        }


        /**
         * merge接收terminatePartial的返回结果，进行数据merge(合并)操作，其返回类型为boolean。
         *
         * @auther: yore
         * @param other terminatePartial的返回结果
         * @return boolean
         */
        public boolean merge(ResultState other){
            if(other != null){
                resultState.countNum += other.countNum;
                resultState.garbledNum += other.garbledNum;
            }
            return true;
        }


        /**
         * terminate返回最终的聚集函数结果
         *
         * @auther: yore
         * @return Double 结果比值
         */
        public Double terminate(){
            return resultState.countNum==0? null: resultState.garbledNum / resultState.countNum;
        }

    }
}
