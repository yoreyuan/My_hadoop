package yore.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * AES（Advanced Encryption Standard）高级加密标准，是一种常见的对称加密算法。
 * <a href="http://www.moserware.com/2009/09/stick-figure-guide-to-advanced.html">A Stick Figure Guide to the Advanced Encryption Standard (AES)</a>
 *
 * Created by yore on 2020/5/6 09:57
 */
public class AesUtil {
    private static Logger LOG = LoggerFactory.getLogger(AesUtil.class);;
    private static final String CHARSET_NAME = "UTF-8";
    private static final String C_Key = "Mercury";
    private static SecretKeySpec skeySpec;
    private static Cipher cipher;

    static {
        init(C_Key);
    }


    /**
     * 初始化加密工具类
     *
     * @param key 指定的密钥
     */
    public static void init(String key){
        if(StringUtils.isEmpty(key)){
            key = C_Key;
        }else if(key.length()<16){
            key = String.format("%-16s", key);
        }else if(key.length()>16){
            key = key.substring(0, 16);
        }
        try {
            byte[] raw = key.getBytes("ASCII");
            skeySpec = new SecretKeySpec(raw, "AES");
            cipher = Cipher.getInstance("AES");
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage());
        } catch (NoSuchPaddingException e) {
            LOG.error(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e.getMessage());
        }
    }


    /**
     * 返回解密后的字符串（默认使用 cKey）
     *
     * @param content 待解密的字符串
     * @return 解密后的字符串。null 值返回 null，空字符串返回空字符串
     */
    public static String decrypt(String content) throws Exception {
        if(null == content) return null;
        if("".equals(content)) return "";
        try {
            cipher.init(2, skeySpec);
            return new String(cipher.doFinal(toByte(new String(content.getBytes(), CHARSET_NAME))), CHARSET_NAME);
        } catch (Exception e) {
            LOG.error("AES解密失败，失败原因:" + e.getMessage());
            return null;
        }
    }


    /**
     * 返回加密后的字符串（默认使用 cKey）
     *
     * @param content 待加密的字符串
     * @return 加密后的字符串
     */
    public static String encrypt(String content) throws Exception {
        if(null == content) return null;
        if("".equals(content)) return "";
        try {
            cipher.init(1, skeySpec);
            return toHexStr(cipher.doFinal(content.getBytes("utf-8")));
        }catch (Exception e){
            LOG.error("AES加密失败，失败原因:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    public static byte[] toByte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        } else {
            byte[] result = new byte[hexStr.length() / 2];

            for(int i = 0; i < hexStr.length() / 2; ++i) {
                int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
                int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
                result[i] = (byte)(high * 16 + low);
            }
            return result;
        }
    }
    public static String toHexStr(byte[] buf) {
        StringBuffer sb = new StringBuffer();

        for(int i = 0; i < buf.length; ++i) {
            String hex = Integer.toHexString(buf[i] & 255);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }

            sb.append(hex.toUpperCase());
        }

        return sb.toString();
    }
}
