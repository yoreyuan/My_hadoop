package yore;

import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * 判断字段对应的值是否为中文，如果含有中文返回true
 *
 * '[^\\u4e00-\\u9fa5\\w\\s]+'
 *
 * Created by yore on 2019/4/26 13:18
 */
public class IsChinese extends UDF {

    /**
     * 重写 evaluate
     *      methodName="evaluate"
     *
     * @return Boolean 是否有乱码
     */
    public Boolean evaluate(String ... s){
        String celVal = "";

        // 迭代每个字段的值
        for(String cel : s){
            // 去除标点符号
            celVal = cel.replaceAll("\\p{P}", "");
            for(char c : celVal.toCharArray()){
                if(Util.isWord(c) )continue;
                // 如果有中文就返回 true
                if(Util.isChinese(c))return true;
            }
        }
        return false;
    }

}