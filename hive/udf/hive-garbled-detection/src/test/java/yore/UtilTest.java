package yore;

import org.junit.Test;


import static yore.Util.isMessyCode;

/**
 * Created by yore on 2019-04-24 2:38
 */
public class UtilTest {

    @Test
    public void isMessyCodeTest(){
        String str = "";
        str = " 鏇挎崲妯″紡涓庣粰瀹氭浛鎹㈠瓧绗︿覆鐩稿尮閰嶇";
        str = "Ã©Å¸Â©Ã©Â¡ÂºÃ¥Â¹Â³";
        str = "ℼ潄瑣灹\u2065瑨汭ਾ格浴㹬㰊敨摡ਾ㰉敭慴栠瑴";
        str = "Java ć\u00ADŁĺ\u0088\u0099čĄ¨čžžĺź\u008F";
        str = "紶閫掔粰Matcher绫荤殑appendReplacement 鏂规硶涓€涓\uE044瓧闈㈠瓧绗︿覆涓€鏍峰伐浣溿€�";
        str = "hello ，你好";
        str = "ss201888asdf;#????鏇挎崲妯";

        str = "1	客户1	6.11	鎾戠潃娌圭焊浼�";
        str = "2	客户2	9.42	ç‹¬č‡Ş";
        str = "3	客户3	18.31	彷徨在悠长、";
        str = "4	客户4	3.33	悠长";
        str = "5	客户5	91	又寂寥的雨巷";
        str = "6	客户6	33	我希望逢着";
        str = "7	客户7	92	一个丁香一样地";
        str = "8	åŪĒæ·8	79	瀯볡��곫�①쉪冶묈쮼";
//        str = "9	若€댎9	25	她是有";
//        str = "10	ĺ®˘ć·10	65	丁香一样的颜色";
//        str = "11	客户11	28	丁香一样的芬芳";
//        str = "12	客户13	17	丁香一样的忧愁";

        boolean result = isMessyCode(str);

//        System.out.print(str + "\t\t");
//        System.out.println(result);

//        System.out.println(isT(str, "UTF-8"));
//        System.out.println(str.split("\t").length + "\t" + isMessyCode(str));

        char[] lc = str.toCharArray();

        for(char c : lc){
            if(Util.isWord(c))continue;
            System.out.println(c + "\t" + Util.isChinese(c));
        }



    }


    @Test
    public void isChinese(){
        String line = "a1A2;；客鏇Ã©Å5冶묈";
        line = "鏇挎崲妯″紡涓庣粰瀹氭浛鎹㈠瓧绗︿覆鐩稿尮閰嶇";

        boolean r = line.matches("[^\u4e00-\u9fa5\\w]+");


        System.out.println(r);


    }







}
