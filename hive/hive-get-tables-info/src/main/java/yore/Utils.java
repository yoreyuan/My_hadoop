package yore;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yore on 2020/3/16 20:09
 */
public class Utils {


    /**
     * 转换文件大小
     *
     * @param fileS
     * @return 合适单位的文件大小值（保留 2 位）
     */
    public static String formetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#0.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + " B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + " KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + " MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + " GB";
        }
        return fileSizeString;
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
    public static BufferedReader getFileReader(String filePath){
        // 读取文件的输入流对象
        BufferedReader in = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(new File(filePath)),"UTF-8"
                    )
            );
        } catch (Exception e){
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return out;
    }

    /**
     * java执行系统本地命令
     *
     * @Author: yore
     * @param command shell命令的字符串
     * @return void
     * @Date: 2018/2/8 8:58
     */
    public static void runCommandScript(String command){
        try {
            System.out.println("$ " + command);
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            //阻塞，直到上述命令执行完
            process.waitFor();

            if(process.exitValue()!=0){
                System.out.println("命令执行失败");
            }


            String backStr;
            /*
             * 利用Java提供的Process类提供的getInputStream,getErrorStream方法让Java虚拟机截获被调用程序的
             * 标准输出、错误输出，在waitfor()命令之前读掉输出缓冲区中的内容
             */
//            System.out.println("-----------running shell---------------");
            while ( (backStr=bufferedReader.readLine()) != null) {
                System.out.println(backStr);
            }

            bufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * java执行shell命令
     * @Author: yore
     * @param shellStr shell命令的字符串
     * @return List<String> 执行的结果
     * @Date: 2018/2/8 8:58
     */
    public static List<String> runShellScript2(String shellStr){
        shellStr = shellStr.replace("'", "\"");
        String pattStr = "(?<=\").*?(?=\")";
        Pattern pattern= Pattern.compile(pattStr);
        Matcher matcher=pattern.matcher(shellStr);
        while(matcher.find()) {
            shellStr = shellStr.replace(matcher.group(), matcher.group().replace(" ", "&nbsp;"));
        }

        String[] commandSplit = shellStr.split(" ");
        List<String> lcommand = new ArrayList<String>();
        for (int i = 0; i < commandSplit.length; i++) {
            lcommand.add(commandSplit[i].replace("&nbsp;", " "));
        }

//        String resultLog = "";
        List<String> resultList = new ArrayList<>();
        try {

            ProcessBuilder processBuilder = new ProcessBuilder(lcommand);
            processBuilder.redirectErrorStream(true);
            Process p = processBuilder.start();
            InputStream is = p.getInputStream();
            BufferedReader bs = new BufferedReader(new InputStreamReader(is));
            p.waitFor();

            if (p.exitValue() != 0) {
                //说明命令执行失败
                //可以进入到错误处理步骤中
                //System.out.println("命令执行失败");
            }

            String line = null;
            while ((line = bs.readLine()) != null) {
//                resultLog += line + "\n";
                resultList.add(line);
            }
            p.destroy();

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return resultList;
    }


}
