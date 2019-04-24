package yore;

import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * 自定义函数。
 * 用于判断每一行数据中是否存在乱码hive
 *
 * hive中注册支持的函数有：
 *     org.apache.hadoop.hive.ql.exec.FunctionRegistry
 *
 * Created by yore on 2019-04-23 23:33
 */
public class IsGarbled extends UDF{

    /**
     * 重写 evaluate
     *      methodName="evaluate"
     *
     * @return Boolean 是否有乱码
     */
    public Boolean evaluate(String ... s){
        return Util.isMessyCode(s);
    }

}
