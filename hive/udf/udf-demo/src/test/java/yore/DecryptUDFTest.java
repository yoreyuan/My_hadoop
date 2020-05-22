package yore;

import junit.framework.Assert;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredJavaObject;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF.DeferredObject;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.Text;
import org.junit.Test;
import yore.utils.AesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yore on 2020/4/30 10:12
 */
public class DecryptUDFTest {

    @Test
    public void decryptUDFTest() throws HiveException {
        DecryptUDF udf = new DecryptUDF();
        ObjectInspector valueOI1 = PrimitiveObjectInspectorFactory.writableVoidObjectInspector;
        ObjectInspector[] arguments = { valueOI1};
        udf.initialize(arguments);

        DeferredObject[] args = new DeferredObject[1];
        String columnVal = "F9953708E0E479101887BF30ECF4606A";
        args[0] =  new DeferredJavaObject(columnVal==null? null: new Text(columnVal));
        Object result = udf.evaluate(args);
        System.out.println(result);
        Assert.assertEquals(result, "Yore");


        columnVal = null;
        args[0] =  new DeferredJavaObject(columnVal==null? null: new Text(columnVal));
        result = udf.evaluate(args);
        Assert.assertEquals(result, null);

        columnVal = "DD64FE4645DFD4A8A5A788E94B67428F";
        args[0] =  new DeferredJavaObject(columnVal==null? null: new Text(columnVal));
        result = udf.evaluate(args);
        System.out.println(result);
        Assert.assertEquals(result, "呵护");

    }

    @Test
    public void genDataTest() throws Exception{
        List<String> utteranceList = new ArrayList<String>(){{
            add("纯白的爱");    add("神秘");    add("执著");
            add("游戏人生");    add("耐心");    add("洁白无瑕的爱");
            add("单恋");        add("爱情");    add("纯朴");
            add("不屈不挠");    add("善良");    add("优雅");
            add("自我");        add("含羞");    add("严格");
            add("竞争");        add("挚爱");    add("猜测");
            add("健康、长寿");   add("童真");    add("沉静、安详");
            add("母爱");        add("服从");    add("进退得宜");
            add("纯真");        add("敏感");    add("坚固");
            add("勇气");        add("呵护");    add("盼望的幸福");
            add("青春喜悦");
        }};

        utteranceList.forEach(s -> {
            try {
                System.out.println( AesUtil.encrypt(s));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    @Test
    public void test01() throws Exception{
        System.out.println(AesUtil.encrypt("Mercury"));
        System.out.println(AesUtil.encrypt("Yore"));
        System.out.println(AesUtil.encrypt("呵护"));

    }


}


















