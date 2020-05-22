package yore;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.log4j.Logger;
import yore.utils.AesUtil;

/**
 *
 * <pre>
 *    hadoop fs -put zodiac.jar /app/udf-lib/
 *
 *    CREATE FUNCTION default.udf_decrypt AS 'yore.DecryptUDF' USING jar 'hdfs:/app/udf-lib/zodiac.jar';
 *    CREATE TEMPORARY FUNCTION default.udf_decrypt2 AS 'yore.DecryptUDF' USING jar 'hdfs:/app/udf-lib/zodiac.jar';
 *
 *     SELECT udf_decrypt("F9953708E0E479101887BF30ECF4606A");
 *
 *     SELECT udf_decrypt("F9953708E0E479101887BF30ECF4606A") FROM movie LIMIT 20;
 *
 *     DROP FUNCTION udf_decrypt;
 * </pre>
 *
 * Created by yore on 2020/5/6 09:57
 */
@Description(name="udf_decrypt",
        value="_FUNC_(value) - Returns the decrypted value of the encrypted string",
        extended = "Example:\n"
                + " > SELECT _FUNC_('4A6DE4A853E3C69B5FA63D70C7B1F350') AS a1 FROM pdata_chq.t_test_jm limit 1; \n")
public class DecryptUDF extends GenericUDF {
    private static Logger logger = Logger.getLogger(DecryptUDF.class);

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        if(arguments.length != 1){
            throw new UDFArgumentException("The operator 'udf_decrypt' accepts 1 arguments.");
        }
//        GenericUDFUtils.ReturnObjectInspectorResolver returnOIResolver = new GenericUDFUtils.ReturnObjectInspectorResolver(true);
//        return returnOIResolver.get();
        ObjectInspector returnType = PrimitiveObjectInspectorFactory.getPrimitiveJavaObjectInspector(PrimitiveObjectInspector.PrimitiveCategory.STRING);
        return returnType;
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        if(arguments[0].get()==null)return null;
        try {
            return AesUtil.decrypt(String.valueOf(arguments[0].get()));
        }catch (Exception e){
            logger.info(e.getMessage());
            return null;
        }
    }

    @Override
    public String getDisplayString(String[] children) {
        StringBuilder sb = new StringBuilder();
        sb.append("返回 " + children[0] + " 的解密后的值")
                .append("\n")
                .append("Usage: udf_decrypt(cloumn_name)")
                .append("\n")
                .append("当解密过程中报异常，返回 null");
        return sb.toString();
    }
}
