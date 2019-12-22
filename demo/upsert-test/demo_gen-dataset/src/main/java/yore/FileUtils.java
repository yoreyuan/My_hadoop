package yore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by yore on 2019/11/23 16:37
 */
public class FileUtils {
    private static Logger LOG = LoggerFactory.getLogger(FileUtils.class);

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
            LOG.error("读取JSON数据文件时发生了错误");
        }
        return out;
    }
}
