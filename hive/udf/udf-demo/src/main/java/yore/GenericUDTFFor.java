package yore;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.WritableConstantIntObjectInspector;
import org.apache.hadoop.io.IntWritable;

import java.util.ArrayList;

/**
 * Created by yore on 2019/5/17 22:46
 */
public class GenericUDTFFor extends GenericUDTF {
    IntWritable start;
    IntWritable end;
    IntWritable inc;
    Object[] forwardObj = null;

    /**
     * 初始化方法
     *  因为函数输入的参数是两个常量，所以可以在initialize方法中将 start 和 end 进行初始化，
     *  根据传入的参数的个数，也可以将循环的步长 inc 初始化，默认为1
     *
     * @throws UDFArgumentException
     */
    @Override
    public StructObjectInspector initialize(ObjectInspector[] args) throws UDFArgumentException {
        start=((WritableConstantIntObjectInspector) args[0]).getWritableConstantValue();
        end=((WritableConstantIntObjectInspector) args[1]).getWritableConstantValue();
        if (args.length == 3) {
            inc =((WritableConstantIntObjectInspector) args[2]).getWritableConstantValue();
        } else {
            inc = new IntWritable(1);
        }
        this.forwardObj = new Object[1];
        ArrayList<String> fieldNames = new ArrayList<String>();
        ArrayList<ObjectInspector> fieldOIs = new ArrayList<ObjectInspector>();
        // 新的列名
        fieldNames.add("col0");
        // 一行数据，而且这行数据的类型为整形
        fieldOIs.add(
                PrimitiveObjectInspectorFactory.getPrimitiveJavaObjectInspector(
                        PrimitiveObjectInspector.PrimitiveCategory.INT));
        return ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, fieldOIs);
    }


    /**
     * 实际进行处理的过程。
     *
     * 需要注意的是其返回值类型为void。这是应为UDTF可以向前获取零行或者多行，而不像UDF，其只有唯一返回值。
     * 此时会在for循环中调用父类继承下的 forward 方法，将输出的每一行传递给collector，进行最终结果的输出。
     */
    @Override
    public void process(Object[] args) throws HiveException {
        for (int i = start.get(); i < end.get(); i = i + inc.get()) {
            this.forwardObj[0] = new Integer(i);
            forward(forwardObj);
        }
    }

    @Override
    public void close() throws HiveException {
        //noop
    }
}
