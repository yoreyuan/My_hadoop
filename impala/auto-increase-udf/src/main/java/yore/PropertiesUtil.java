package yore;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by yore on 2018-03-21 16:26
 */
public class PropertiesUtil {
    public static Properties myProp;

    static {
        myProp = new Properties();
        try {
            myProp.load(PropertiesUtil.class.getResourceAsStream("/my.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回my.properties的配置对象
     * @author yore
     * @return Properties my.properties的配置对象
     * @Date 2017/12/26 14:22
     */
    public static Properties getMyProperties(){
        return myProp;
    }

    /**
     *
     * 获取配置文件中key对应的字符串值
     *
     * @author yore
     * @return java.util.Properties
     * <br/><br/>date 2018/6/29 14:24
     */
    public static String getPropString(String key){
        return getMyProperties().getProperty(key);
    }

    /**
     *
     * 获取配置文件中key对应的整数值
     *
     * @author yore
     * @return java.util.Properties
     * <br/><br/>date 2018/6/29 14:24
     */
    public static Integer getPropInt(String key){
        return Integer.parseInt(getMyProperties().getProperty(key));
    }

    /**
     *
     * 获取配置文件中key对应的布尔值
     *
     * @author yore
     * @return java.util.Properties
     * <br/><br/>date 2018/6/29 14:24
     */
    public static Boolean getPropBoolean(String key) {
        return Boolean.parseBoolean(getMyProperties().getProperty(key));
    }
}
