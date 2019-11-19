package yore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * Created by yore on 2019/11/19 17:16
 */
public class FieldEqualUDFTest {

    private FieldEqualUDF fieldEqualUDF;
    private SimpleDateFormat sf;

    @Before
    public void init(){
        this.fieldEqualUDF = new FieldEqualUDF();
        this.sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Test
    public void udfTest() throws ParseException {
        Boolean result;

        Integer a1=127, a2=127, a3=128;
        result = fieldEqualUDF.evaluate(a1, a2);
        Assert.assertTrue(result);
        result = fieldEqualUDF.evaluate(a1, a3);
        Assert.assertFalse(result);

        Long b1=127L, b2=127L, b3=128L;
        result = fieldEqualUDF.evaluate(b1, b2);
        Assert.assertTrue(result);
        result = fieldEqualUDF.evaluate(b1, b3);
        Assert.assertFalse(result);

        Float c1=127.0f, c2=127.0f, c3=128.0f;
        result = fieldEqualUDF.evaluate(c1, c2);
        Assert.assertTrue(result);
        result = fieldEqualUDF.evaluate(c1, c3);
        Assert.assertFalse(result);

        Double d1=127.0, d2=127.0, d3=128.0;
        result = fieldEqualUDF.evaluate(d1, d2);
        Assert.assertTrue(result);
        result = fieldEqualUDF.evaluate(d1, d3);
        Assert.assertFalse(result);

        Boolean e1=true, e2=true, e3=false;
        result = fieldEqualUDF.evaluate(e1, e2);
        Assert.assertTrue(result);
        result = fieldEqualUDF.evaluate(e1, e3);
        Assert.assertFalse(result);

        String f1="str1", f2="str1", f3="";
        result = fieldEqualUDF.evaluate(f1, f2);
        Assert.assertTrue(result);
        result = fieldEqualUDF.evaluate(f1, f3);
        Assert.assertFalse(result);

        Date g1=sf.parse("2019-03-25 00:00:01"),
                g2=sf.parse("2019-03-25 00:00:01"),
                g3=sf.parse("2019-03-25 00:00:03");
        result = fieldEqualUDF.evaluate(g1, g2);
        Assert.assertTrue(result);
        result = fieldEqualUDF.evaluate(g1, g3);
        Assert.assertFalse(result);

        Integer h1 = 127;
        Long h2 = 127L;
        Date h3=sf.parse("2019-03-25 00:00:03");
        result = fieldEqualUDF.evaluate(h1, h2);
        Assert.assertFalse(result);
        result = fieldEqualUDF.evaluate(h2, h3);
        Assert.assertFalse(result);
        result = fieldEqualUDF.evaluate(h1, h3);
        Assert.assertFalse(result);
        result = fieldEqualUDF.evaluate(h1, null);
        Assert.assertFalse(result);

    }
}
