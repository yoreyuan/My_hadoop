package yore;

import junit.framework.TestCase;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredJavaObject;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredObject;
import org.apache.hadoop.hive.serde2.io.ByteWritable;
import org.apache.hadoop.hive.serde2.io.DateWritable;
import org.apache.hadoop.hive.serde2.io.DoubleWritable;
import org.apache.hadoop.hive.serde2.io.ShortWritable;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import java.sql.Date;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Created by yore on 2020/4/30 10:12
 */
public class GenericUDFGreatestTest extends TestCase {


    public void testVoids() throws HiveException {
        GenericUDFGreatest udf = new GenericUDFGreatest();
        ObjectInspector valueOI1 = PrimitiveObjectInspectorFactory.writableVoidObjectInspector;
        ObjectInspector valueOI2 = PrimitiveObjectInspectorFactory.writableIntObjectInspector;
        ObjectInspector valueOI3 = PrimitiveObjectInspectorFactory.writableStringObjectInspector;
        ObjectInspector[] arguments = { valueOI1, valueOI2, valueOI3 };
        udf.initialize(arguments);
        System.out.println(udf.getFuncName() + "\t"  + udf.getOrder());
//        runAndVerify(new Object[] { null, 1, "test"}, null, udf);
        runAndVerify(new Object[] { null, null, 22, 1, 333, 4}, null, udf);

    }


    private void runAndVerify(Object[] v, Object expResult, GenericUDF udf) throws HiveException {
        GenericUDF.DeferredObject[] args = new GenericUDF.DeferredObject[v.length];
        for (int i = 0; i < v.length; i++) {
            args[i] = new GenericUDF.DeferredJavaObject(getWritable(v[i]));
        }
        Stream.of(args).forEach(a -> {
            try {
                System.out.print(a.get() + "\t");
            } catch (HiveException e) {
                e.printStackTrace();
            }
        });
        System.out.println();
        Object output = udf.evaluate(args);
        output = parseOutput(output);
        System.out.println(output);


        assertEquals("greatest() test ", expResult, output != null ? output : null);
    }

    private Object getWritable(Object o) {
        if (o instanceof String) {
            return o != null ? new Text((String) o) : null;
        } else if (o instanceof Integer) {
            return o != null ? new IntWritable((Integer) o) : null;
        } else if (o instanceof Double) {
            return o != null ? new DoubleWritable((Double) o) : null;
        } else if (o instanceof Date) {
            return o != null ? new DateWritable((Date) o) : null;
        } else if (o instanceof Byte) {
            return o != null ? new ByteWritable((Byte) o): null;
        } else if (o instanceof Short) {
            return o != null ? new ShortWritable((Short) o) : null;
        } else if (o instanceof Long) {
            return o != null ? new LongWritable((Long) o) : null;
        }
        return null;
    }

    private Object parseOutput(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Text) {
            return o.toString();
        } else if (o instanceof IntWritable) {
            return ((IntWritable) o).get();
        } else if (o instanceof DoubleWritable) {
            return ((DoubleWritable) o).get();
        } else if (o instanceof DateWritable) {
            return ((DateWritable) o).get();
        } else if (o instanceof ByteWritable) {
            return ((ByteWritable) o).get();
        } else if (o instanceof ShortWritable) {
            return ((ShortWritable) o).get();
        } else if (o instanceof LongWritable) {
            return ((LongWritable) o).get();
        }
        return null;
    }

}