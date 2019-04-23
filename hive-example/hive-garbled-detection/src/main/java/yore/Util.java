package yore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yore on 2019-04-24 10:29
 */
public class Util {
    private static Pattern p = Pattern.compile("\\s*|t*|r*|n*");

//    public static char stringToChar(String s){
//        s.toCharArray()
//    }


    /**
     * 判断字符串是否乱码
     *
     * @param str 字符串值
     * @return boolean 是否乱码
     */
    public static boolean isMessyCode(String str) {
        Matcher m = p.matcher(str);
        String after = m.replaceAll("");
        String temp = after.replaceAll("\\p{P}", "");
        char[] ch = temp.trim().toCharArray();
        float chLength = ch.length;
        float count = 0;
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (!Character.isLetterOrDigit(c)) {
                if (!isChinese(c)) {
                    count = count + 1;
                }
            }
        }
        float result = count / chLength;
        if (result > 0.4) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断字符是否为中文
     *
     * @author: yore
     * @param  c 字符
     * @return boolean true-是中文，反之
     * @date: 2019/4/24 10:32
     */
    public static boolean isChinese(char c){
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }


}
