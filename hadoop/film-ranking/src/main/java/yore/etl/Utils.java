package yore.etl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utils
 *
 * Created by yore on 2019/7/24 20:15
 */
public class Utils {
    private static Logger LOG = LoggerFactory.getLogger(Utils.class);


    /**
     * 根据传入的豆瓣连接，截取电影的的标识符作为id返回。
     * 例如：https://movie.douban.com/subject/1300267/，返回 1300267
     *
     *
     * @auther: yore
     * @param url 电影连接
     * @return 标识符作为id
     * @date: 2019/7/24 20:33
     */
    public static String getIdByDoubanURL(String url){
        List<String> uriSplitList = Arrays.asList(url.split("/"));
        Collections.reverse(uriSplitList);
        return uriSplitList.get(0);
    }


    /**
     *
     * 返回文件读取的输入流对象
     * <pre>
     *   默认使用UTF-8编码
     * 使用方法：
     * BufferedReader in = getFileReader("文件路径");
     * while ( (line=in.readLine()) != null ){
     *      System.out.println(line);
     * }
     * <pre/>
     * @author: yore
     * @param filePath 输入文件的路径
     * @return java.io.BufferedReader
     * @date: 2018/7/4 14:26
     */
    public static BufferedReader getFileReader( String filePath){
        // 读取文件的输入流对象
        BufferedReader in = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(new File(filePath)),"UTF-8"
                    )
            );
        } catch (Exception e){
            LOG.error("读取文件{}时发生了错误", filePath);
        }
        return in;
    }


    /**
     *
     * 返回文件读取的输出流对象OutputStreamWriter
     * 默认是追加写
     * @author: yore
     * @param writerFilePath 输出的文件的路径
     * @return java.io.BufferedReader 文件读取的输出流对象
     * @date: 2018/7/4 14:26
     */
    public static OutputStreamWriter getOutPutStreamWriter(String writerFilePath/*, String charsetName*/){
        // 读取文件的输入流对象
        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(new FileOutputStream(writerFilePath, true), "UTF-8");
        } catch (Exception e){
            LOG.error("读取文件{}时发生了错误", writerFilePath);
        }
        return out;
    }

}
