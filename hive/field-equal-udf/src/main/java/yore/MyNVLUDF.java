package yore;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * 实现一个UDF，
 *
 *  <p>
 *      一个string 参数 str1，
 *      处理:str1=null or '' 还回'0'
 *      否则输出str1
 *      输出string 值
 *  </p>
 *
 *  <pre>
 *    实现的功能如下：
 *      SELECT id,CASE WHEN nullvalue(name) THEN "0" WHEN name="" THEN "0" ELSE name END AS name FROM tmp_test;
 *
 *    创建：
 *        CREATE FUNCTION IF NOT EXISTS yore_nvl(STRING) RETURNS STRING LOCATION 'hdfs:/app/udf-lib/field-equal-udf.jar' symbol='yore.MyNVLUDF';
 *        CREATE FUNCTION IF NOT EXISTS yore_nvl(STRING, STRING) RETURNS STRING LOCATION 'hdfs:/app/udf-lib/field-equal-udf.jar' symbol='yore.MyNVLUDF';
 *
 *    使用：
 *        SELECT id,yore_nvl(name) AS name FROM tmp_test;
 *        SELECT id,yore_nvl(name, "-") AS name FROM tmp_test;
 *
 *  </pre>
 *
 * Created by yore on 2019/5/19 15:44
 */
@Description(name = "fiedl_equal",
        value = "_FUNC_(STRING str_f, STRING replacement) - null or \"\" string converted to \"${replacement}\".  ",
        extended = "Example:\n"
                + " > SELECT _FUNC_(STRING str_f, STRING replacement) FROM table_name;"
)
public class MyNVLUDF extends UDF {


    /**
     * 如果传入一个 str ，为 null 或者 "" ，默认返回 "0"，否则返回该字段值
     *
     * @auther: yore
     * @param str_f 字符串字段值
     * @return str_f 或者 "0"
     * @date: 2019/12/25 9:20 AM
     */
    public String evaluate(String str_f){
        return evaluate(str_f, "0");
    }


    /**
     * 传入两个参数，如果第一个 str ，为 null 或者 "" ，默认返回 "0"，否则返回该字段值
     *
     *
     * @auther: yore
     * @param str_f 字符串字段值
     * @param replacement 如果为  null 或者 ""  是替换的 值
     * @return str_f 或者 $replacement
     * @date: 2019/12/25 9:20 AM
     */
    public String evaluate(String str_f, String replacement){
        if(str_f==null || "".equals(str_f) || "".equals(str_f.replaceAll("\\s", ""))){
            return replacement;
        }
        return str_f.trim();
    }

}
