package yore;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 实现一个UDF，
 *  对比传入的两个字段的值是否相等
 *
 * Created by yore on 2019/5/19 15:44
 */
@Description(name = "fiedl_equal",
        value = "_FUNC_(field1, field2) - Compare whether the two input field values are equal.  "+
                "If they are equal, return true. If they are not equal, return false. If they are null, return NULL.",
        extended = "Example:\n"
                + " > SELECT _FUNC_(field1, field1') FROM src;"
)
public class FieldEqualUDF extends UDF {

    private SimpleDateFormat sf;

    public FieldEqualUDF() {
        // H(0-23)
        // h(0-12)
        this.sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public Boolean evaluate(){
        return null;
    }

    public Boolean evaluate(Object f1){
        return evaluate();
    }

    /**
     * 传入两个字段时
     *
     * <pre>
     *     当两个字段都是 null 相等
     *     当两个字段的值相等，则相等，如果不相等进一步判断
     *     当字段类型为 Integer、Long、Float、Double、Boolean、String、Date时，比较其值，如果相等返回true，否则返回false
     *     如果是其它类型、或者类型不一致，直接返回false
     * </pre>
     *
     * @auther: yore
     * @param f1 字段1
     * @param f2 字段2
     * @return boolean 两字段是否相等
     */
    public Boolean evaluate(Object f1, Object f2){
        if((f1==null && f2==null) || f1 == f2) return true;

        if((f1 instanceof Integer ) && (f2 instanceof Integer )) {
            return f1.equals(f2);
        }else if((f1 instanceof Long ) && (f2 instanceof Long )){
            return f1.equals(f2);
        }else if((f1 instanceof Float ) && (f2 instanceof Float )){
            return f1.equals(f2);
        }else if((f1 instanceof Double ) && (f2 instanceof Double )){
            return f1.equals(f2);
        }else if((f1 instanceof Boolean ) && (f2 instanceof Boolean )){
            return f1.equals(f2);
        }else if((f1 instanceof String ) && (f2 instanceof String )){
            return f1.equals(f2);
        }else if((f1 instanceof Date) && (f2 instanceof Date )){
            String f1Str = sf.format((Date) f1);
            String f2Str = sf.format((Date) f2);
            return f1.equals(f2);
        }else {
            return false;
        }
    }

    public Boolean evaluate(String f1, String f2){
        if((f1==null && f2==null) || f1 == f2) return true;
        return f1.equals(f2);
    }

    public Boolean evaluate(Integer f1, Integer f2){
        if((f1==null && f2==null) || f1 == f2) return true;
        return f1.equals(f2);
    }

    public Boolean evaluate(Long f1, Long f2){
        if((f1==null && f2==null) || f1 == f2) return true;
        return f1.equals(f2);
    }

    public Boolean evaluate(Float f1, Float f2){
        if((f1==null && f2==null) || f1 == f2) return true;
        return f1.equals(f2);
    }

    public Boolean evaluate(Double f1, Double f2){
        if((f1==null && f2==null) || f1 == f2) return true;
        return f1.equals(f2);
    }

    public Boolean evaluate(Boolean f1, Boolean f2){
        if((f1==null && f2==null) || f1 == f2) return true;
        return f1.equals(f2);
    }

    public Boolean evaluate(Date f1, Date f2){
        if((f1==null && f2==null) || f1 == f2) return true;

        String f1Str = sf.format((Date) f1);
        String f2Str = sf.format((Date) f2);
        return f1.equals(f2);
    }

}
