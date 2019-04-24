package yore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yore on 2019-04-24 10:29
 */
public class Util {
    private static Pattern p = Pattern.compile("\\s*|t*|r*|n*");

    /**
     * 判断字符串是否乱码
     *
     * @param s 多参数的字符串数组
     * @return boolean 是否乱码
     */
    public static boolean isMessyCode(String ... s){
        for(String s1: s){
            boolean flag = isMessyCode(s1);
            if(flag) return true;
        }
        return false;
    }

    /**
     * 判断字符串是否乱码
     *
     * @param str 字符串值
     * @return boolean 是否乱码
     */
    public static boolean isMessyCode(String str) {
        try {
            Matcher m = p.matcher(str);
            String after = m.replaceAll("");
            String temp = after.replaceAll("\\p{P}", "");
            char[] ch = temp.trim().toCharArray();
            for(char c: ch){
                if (!Character.isLetterOrDigit(c)) {
                    String strCh = "" + c;
                    if (!strCh.matches("[\u4e00-\u9fa5]+")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断是否为乱码
     *
     * @param str 待判断的字符串
     * @return boolean 是否为乱码
     */
    public static boolean isMessyCode2(String str) {
        for(char c : str.toCharArray()){
            // 当从Unicode编码向某个字符集转换时，如果在该字符集中没有对应的编码，则得到0x3f（即问号字符?）
            //从其他字符集向Unicode编码转换时，如果这个二进制数在该字符集中没有标识任何的字符，则得到的结果是0xfffd
            //System.out.println("--- " + (int) c);
            System.out.println((int)c);
            // 65533
            if ((int) c == 0xfffd) {
                // 存在乱码
                System.out.println("存在乱码 " + (int) c);
                return true;
            }
        }
        return false;
    }


    /**
     * 判断字符串是否乱码
     *
     * @param str 字符串值
     * @return boolean 是否乱码
     */
    public static boolean isMessyCode3(String str) {
        Matcher m = p.matcher(str);
        String after = m.replaceAll("");
        String temp = after.replaceAll("\\p{P}", "");
        char[] ch = temp.trim().toCharArray();
        float chLength = ch.length;
        float count = 0;
        for(char c : ch){
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

    /**
     *
     * @param s 字符串
     * @param charsetName 编码名字
     * <blockquote><table width="80%" summary="Description of standard charsets">
     * <tr><th align="left">Charset</th><th align="left">Description</th></tr>
     * <tr><td valign=top><tt>US-ASCII</tt></td>
     *     <td>Seven-bit ASCII, a.k.a. <tt>ISO646-US</tt>,
     *         a.k.a. the Basic Latin block of the Unicode character set</td></tr>
     * <tr><td valign=top><tt>ISO-8859-1&nbsp;&nbsp;</tt></td>
     *     <td>ISO Latin Alphabet No. 1, a.k.a. <tt>ISO-LATIN-1</tt></td></tr>
     * <tr><td valign=top><tt>UTF-8</tt></td>
     *     <td>Eight-bit UCS Transformation Format</td></tr>
     * <tr><td valign=top><tt>UTF-16BE</tt></td>
     *     <td>Sixteen-bit UCS Transformation Format,
     *         big-endian byte&nbsp;order</td></tr>
     * <tr><td valign=top><tt>UTF-16LE</tt></td>
     *     <td>Sixteen-bit UCS Transformation Format,
     *         little-endian byte&nbsp;order</td></tr>
     * <tr><td valign=top><tt>UTF-16</tt></td>
     *     <td>Sixteen-bit UCS Transformation Format,
     *         byte&nbsp;order identified by an optional byte-order mark</td></tr>
     * </table></blockquote>
     *
     * @return
     */
    public static boolean isT(String s, String charsetName){
        return java.nio.charset.Charset.forName(charsetName).newEncoder().canEncode(s);
    }


    /**
     * 校验某个字符是否是a-z、A-Z、_、0-9
     *
     * @param c 被校验的字符
     * @return true代表符合条件
     */
    public static boolean isWord(char c) {
        String regEx = "[\\w]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher("" + c);
        return m.matches();
    }


    /**
     * 验证是否是汉字或者0-9、a-z、A-Z
     *
     * @param c
     *  被验证的char
     * @return true代表符合条件
     */
    public static boolean isRightChar(char c) {
        return isChinese(c) || isWord(c);
    }

    /**
     * 判断字符串中是否包含中文
     * @param str 待校验字符串
     * @return 是否为中文
     */
    public static boolean isContainChinese(String str) {
        //TODO 校验是否为中文标点符号
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }


    /**
     * 校验String是否全是中文
     *
     * @param name 被校验的字符串
     * @return true代表全是汉字
     */
    public static boolean checkNameChese(String name) {
        char[] cTemp = name.toCharArray();
        for (int i = 0; i < name.length(); i++) {
            if (!isChinese(cTemp[i])) {
                return false;
            }
        }
        return true;
    }

}
