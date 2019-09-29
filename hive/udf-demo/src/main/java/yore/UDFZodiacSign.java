package yore;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 实现一个UDF，
 * 将输入的日期，转换为对应的星座(Zodiac Sign)。
 * 通过继承UDF类并实现evaluate()方法：
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
 *   类上的注解 Description 是可选的。注明了这个函数的文档说明，
 *   用户需要通过这个注解来阐明自定义的UDF的使用方法和例子。
 *   这样当用户通过  DESCRIBE FUNCTION ... 查看该函数时，注解中的 _FUNC_ 字符串将会被替换为用户为这个函数定义的临时函数名称，
 *   当创建完函数之后，输入如下命令可以看到表述信息：
 *      hive> DESCRIBE FUNCTION zodiac; 或者
 *      hive> DESCRIBE FUNCTION EXTENDED zodiac;
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
public class UDFZodiacSign extends UDF {

    private SimpleDateFormat sf;
    private Calendar calendar = Calendar.getInstance();

    public UDFZodiacSign() {
        this.sf = new SimpleDateFormat("MM-dd-yyyy");
    }


    /**
     * 手动实现 evaluate() 方法
     */
    public String evaluate(String bday){
        Date date = null;
        try {
            // 生日字段对应的值，例如： 2-12-1981 字符串，格式化为Date对象
            date = sf.parse(bday);
        } catch (Exception ex) {
            return null;
        }
        return this.evaluate(date);
    }


    public String evaluate(Date bday ){
        calendar.setTime(bday);
        return this.evaluate(calendar.get(Calendar.MONDAY)+1, calendar.get(Calendar.DATE) );
    }


    /**
     * 重载 evaluate 方法
     *   Aquarius       水瓶座     1.20-2.18
     *   Pisces         双鱼座     2.19-3.20
     *   Aries          白羊座     3.21-4.19
     *   Taurus         金牛座     4.20-5.20
     *   Gemini         双子座     5.21-6.21
     *   Cacer          巨蟹座     6.22-7.22
     *   Leo            狮子座     7.23-8.22
     *   Virgo          处女座     8.23-9.22
     *   Libra          天秤座     9.23-10.23
     *   Scorpio        天蝎座     10.24-11.22
     *   Sagittarius    射手座     11.23-12.21
     *   Capricorn      魔羯座     12.22-1.19
     */
    public String evaluate(Integer month, Integer day ){
        String errorHit = "数字非法，请验证%s: %d";
        switch (month){
            case 1:{
                if(day<20)return "Capricorn";
                else if(day<32)  return "Aquarius";
                else return String.format(errorHit, "day", day);
            }
            case 2:{
                if(day<19)return "Aquarius";
                else if(day<30)  return "Pisces";
                else return String.format(errorHit, "day", day);
            }
            case 3:{
                if(day<21)return "Pisces";
                else if(day<32)  return "Aries";
                else return String.format(errorHit, "day", day);
            }
            case 4:{
                if(day<20)return "Aries";
                else if(day<31)  return "Taurus";
                else return String.format(errorHit, "day", day);
            }
            case 5:{
                if(day<21)return "Taurus";
                else if(day<32)  return "Gemini";
                else return String.format(errorHit, "day", day);
            }
            case 6:{
                if(day<22)return "Gemini";
                else if(day<31)  return "Cacer";
                else return String.format(errorHit, "day", day);
            }
            case 7:{
                if(day<23)return "Cacer";
                else if(day<32)  return "Leo";
                else return String.format(errorHit, "day", day);
            }
            case 8:{
                if(day<23)return "Leo";
                else if(day<32)  return "Virgo";
                else return String.format(errorHit, "day", day);
            }
            case 9:{
                if(day<23)return "Virgo";
                else if(day<31)  return "Libra";
                else return String.format(errorHit, "day", day);
            }
            case 10:{
                if(day<24)return "Libra";
                else if(day<32)  return "Scorpio";
                else return String.format(errorHit, "day", day);
            }
            case 11:{
                if(day<23)return "Scorpio";
                else if(day<31) return "Sagittarius";
                else return String.format(errorHit, "day", day);
            }
            case 12:{
                if(day<22)return "Sagittarius";
                else if(day<32) return "Capricorn";
                else return String.format(errorHit, "day", day);
            }
            default:
                return String.format(errorHit, "month", month);
        }
    }


}

//class test {
//    public static void main(String[] args) throws Exception {
//        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date d = sf.parse("2019-09-30 09:34:08");
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(d);
//        System.out.println(calendar.get(Calendar.MONDAY));
//        System.out.println(calendar.get(Calendar.DATE));
//        System.out.println(calendar.get(Calendar.DAY_OF_MONTH));
//
//    }
//}
