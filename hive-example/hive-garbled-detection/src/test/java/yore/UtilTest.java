package yore;

import org.junit.Test;
import static yore.Util.isMessyCode;

/**
 * Created by yore on 2019-04-24 2:38
 */
public class UtilTest {

    @Test
    public void isMessyCodeTest(){
        String str = "Ã©Å¸Â©Ã©Â¡ÂºÃ¥Â¹Â³";
        str = "hello 你好";
        str = "紶閫掔粰Matcher绫荤殑appendReplacement 鏂规硶涓€涓\uE044瓧闈㈠瓧绗︿覆涓€鏍峰伐浣溿€�";
        str = " 鏇挎崲妯″紡涓庣粰瀹氭浛鎹㈠瓧绗︿覆鐩稿尮閰嶇";
        str = "ℼ潄瑣灹\u2065瑨汭ਾ格浴㹬㰊敨摡ਾ㰉敭慴栠瑴";
        str = "Java ć\u00ADŁĺ\u0088\u0099čĄ¨čžžĺź\u008F";

        boolean result = isMessyCode(str);

        System.out.print(str + "\t\t");
        System.out.println(result);
    }

}
