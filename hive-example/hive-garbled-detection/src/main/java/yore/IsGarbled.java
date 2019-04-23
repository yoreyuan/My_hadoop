package yore;

import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * 自定义函数。
 * 用于判断每一行数据中是否存在乱码
 *
 * Created by yore on 2019-04-23 23:33
 */
public class IsGarbled extends UDF{

    /**
     * 重写 evaluate
     *      methodName="evaluate"
     *
     * @return Boolean
     */
    public Boolean evaluate(String ... s){

        return false;
    }

}
