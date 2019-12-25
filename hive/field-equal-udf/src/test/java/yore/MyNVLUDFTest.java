package yore;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by yore on 2019/12/25 10:03
 */
public class MyNVLUDFTest {

    @Test
    public void udfTest() {
        MyNVLUDF myNVLUDF = new MyNVLUDF();

        String str_f = null;
        Assert.assertEquals(myNVLUDF.evaluate(str_f), "0");
        Assert.assertEquals(myNVLUDF.evaluate(str_f, "0"), "0");

        str_f = "";
        Assert.assertEquals(myNVLUDF.evaluate(str_f), "0");
        Assert.assertEquals(myNVLUDF.evaluate(str_f, "0"), "0");

        str_f = "                ";
        Assert.assertEquals(myNVLUDF.evaluate(str_f), "0");
        Assert.assertEquals(myNVLUDF.evaluate(str_f, "0"), "0");

        str_f = "     a           ";
        Assert.assertEquals(myNVLUDF.evaluate(str_f), "a");
        Assert.assertEquals(myNVLUDF.evaluate(str_f, "0"), "a");

    }
}
