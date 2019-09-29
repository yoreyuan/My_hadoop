package yore;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDFUtils;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;

/**
 * 如果传入的值为 null，那么返回一个默认值。
 * <pre>
 *     nvl要求有两个参数:
 *       如果第一个参数为null，那么返回第二个参数值；
 *       如果第一个参数不为null，那么就正常返回该值
 *
 *     hive> ADD JAR hdfs:/app/lib/zodiac.jar;
 *     hive> CREATE FUNCTION nvl AS 'yore.GenericUDFNvl' USING jar 'hdfs:/app/lib/zodiac.jar';
 *     hive> SELECT nvl(1,2) AS COL1, nvl(NULL, 3) AS COL2, nvl(NULL, "STUFF") AS COL3 FROM src LIMIT 1;
 *
 *     将jar集成到Hive，
 *       修改（hive-exec_*.jar）：ql/src/java/org/apache/hadoop/hive/ql/exec/FunctionRegistry.java
 *              registerUDF("parse_url", UDFParseUrl.class, false);
 *              registerGenericUDF("nvl", GenericUDFNvl.class);
 *              registerGenericUDF("split", GenericUDFSplit.class);
 *
 * </pre>
 *
 *
 * Created by yore on 2019/5/19 19:46
 */
@Description(name = "nvl",
        value = "_FUNC_(value,default_value) - Returns default value if value"
                +" is null else returns value",
        extended = "Example:\n"
                + " > SELECT _FUNC_(null,'bla') FROM src LIMIT 1;\n")
public class GenericUDFNvl extends GenericUDF{
    private GenericUDFUtils.ReturnObjectInspectorResolver returnOIResolver;
    private ObjectInspector[] argumentOIs;

    /**
     * 会被输入的每个参数调用，并最终传入到一个ObjectInspector对象中。
     * 这个方法的目的是确定参数的返回类型。
     * 如果传入方法的参数不合法，这是用户同样可以向控制台抛出一个Exception异常信息。
     *
     * returnOIResolver是一个内置的类，其通过获取非null值的变量的类型并使用这个数据类型来确定返回值类型
     *
     */
    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        argumentOIs = arguments;
        if (arguments.length != 2) {
            throw new UDFArgumentLengthException(
                    "The operator 'NVL' accepts 2 arguments.");
        }
        returnOIResolver = new GenericUDFUtils.ReturnObjectInspectorResolver(true);
        if (!(returnOIResolver.update(arguments[0]) && returnOIResolver
                .update(arguments[1]))) {
            throw new UDFArgumentTypeException(2,
                    "The 1st and 2nd args of function NLV should have the same type, "
                            + "but they are different: \"" + arguments[0].getTypeName()
                            + "\" and \"" + arguments[1].getTypeName() + "\"");
        }
        return returnOIResolver.get();
    }


    /**
     * 输入是一个DeferredObject数组对象，
     * initialize方法中创建的returnOIResolver作用于DeferredObject对象中获取到值。
     *
     * @param arguments
     * @return
     * @throws HiveException
     */
    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        Object retVal = returnOIResolver.convertIfNecessary(arguments[0].get(),
                argumentOIs[0]);
        // 如果第一个值为null，返回第二个值
        if (retVal == null ){
            retVal = returnOIResolver.convertIfNecessary(arguments[1].get(),
                    argumentOIs[1]);
        }
        return retVal;
    }


    /**
     * 用于Hadoop Task内部，在使用到这个函数时来展示调试信息
     * @param children
     * @return
     */
    @Override
    public String getDisplayString(String[] children) {
        StringBuilder sb = new StringBuilder();
        sb.append("if ");
        sb.append(children[0]);
        sb.append(" is null ");
        sb.append("returns");
        sb.append(children[1]);
        return sb.toString() ;
    }
}
