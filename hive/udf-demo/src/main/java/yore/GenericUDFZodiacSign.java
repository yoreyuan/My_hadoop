package yore;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorConverters;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

/**
 * 实现一个UDF， 通过 GenericUDF 方式。
 * 将输入的日期，转换为对应的星座(Zodiac Sign)。
 * 通过继承UDF类并实现evaluate()方法：
 *
 * 类上的注解 Description 是可选的。注明了这个函数的文档说明，
 * 用户需要通过这个注解来阐明自定义的UDF的使用方法和例子。
 * 这样当用户通过  DESCRIBE FUNCTION ... 查看该函数时，注解中的 _FUNC_ 字符串将会被替换为用户为这个函数定义的临时函数名称，
 *
 * <pre>
 *     继承 UDF 类的方式已经过时，现在推荐使用 GenericUDF，优点有：
 *       1. 它可以接受复杂类型的参数，并返回复杂类型
 *       2. 它可以接受变长参数
 *       3. 它可以接受无限数量的函数签名（function signature），
 *           例如，编写GenericUDF时可以很容易的接受 array<int>, array<array<int>> 和任意嵌套级别
 *       4. 它可以使用DeferedObject进行短路评估
 *
 *
 *   注意：
 *      String类型输出时记得转为 org.apache.hadoop.io.Text ，否则会报 ClassCastException 异常。
 *
 * </pre>
 *
 * Created by yore on 2019/5/19 15:44
 */
@Description(name = "zodiac",
        value = "_FUNC_(date) - from the input date string "+
                "or separate month and day arguments, returns the sign of the Zodiac.",
        extended = "Example:\n"
                + " > SELECT _FUNC_(date_string) FROM src;\n"
                + " > SELECT _FUNC_(month, day) FROM src;")
public class GenericUDFZodiacSign extends GenericUDF {

    private SimpleDateFormat sf;
    private UDFZodiacSign zodiac;

    private ObjectInspectorConverters.Converter[] converters;

    private Text text ;

    /**
     * 这个方法只调用一次，并且在evaluate()方法之前调用。
     * 该方法接受的参数是一个ObjectInspectors数组。
     * 该方法检查接受正确的参数类型和参数个数。
     * 比如，在该方法中可以初始化对象实例，初始化数据库链接，初始化读取文件等；
     *
     */
    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        this.zodiac = new UDFZodiacSign();
        this.sf = new SimpleDateFormat("MM-dd-yyyy");
        converters = new ObjectInspectorConverters.Converter[arguments.length];
        text = new Text();
        if(arguments.length==0) return null;

        //参数 <--->参数转换器
        for(int i=0; i<arguments.length; i++){
            converters[i] = ObjectInspectorConverters.getConverter(arguments[i], PrimitiveObjectInspectorFactory.javaStringObjectInspector);
        }

        // 定义返回的数据类型为 String
        return PrimitiveObjectInspectorFactory.writableStringObjectInspector;
    }


    /**
     * 这个方法类似UDF的evaluate()方法。
     * 它处理真实的参数，并返回最终结果。
     */
    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        StringBuffer result = new StringBuffer();
        int i = 0;
        for(DeferredObject dateStrsObj : arguments){
            String arg = (String)converters[i].convert(dateStrsObj.get());
            try {
                result.append(
                        zodiac.evaluate(sf.parse(
                                //((Text)dateStrsObj).toString()
                                arg
                        ))
                ).append(",");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            i++;
        }
        String s = result.toString();
        // 注意：这里需要传过来的对象时 Text，需要用Text来转成String
        text.set(s.endsWith(",")? s.substring(0, s.lastIndexOf(",")) : s);
        return text;
    }


    /**
     * 这个方法用于当实现的GenericUDF出错的时候，打印出提示信息。
     * 而提示信息就是你实现该方法最后返回的字符串。
     */
    @Override
    public String getDisplayString(String[] children) {
        StringBuilder sb = new StringBuilder();
        sb.append("The input parameter is：" + Arrays.toString(children));
        sb.append("Usage：zodiac(data_string ...) \n");
        sb.append("Support multiple parameters. \n");
        sb.append("parames format is MM-dd-yyyy .");
        return sb.toString();
    }
}
