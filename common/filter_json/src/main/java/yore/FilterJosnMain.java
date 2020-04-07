package yore;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * --input /Users/yoreyuan/Downloads/index_20200403.json --output /Users/yoreyuan/Downloads/del_index_20200403.json --where _source.begin_date>='2019-10-01' --where _source.begin_date<'2020-04-01'
 *
 * 15637
 * 5806
 *
 * 21443
 *
 * <pre>
 *   total = 21687
 *   1. statistical_year=2019， statistical_granularity=10month；（-32条），10986+10669=21655
 *     --input /Users/yoreyuan/Downloads/index_20200404.json --output /Users/yoreyuan/Downloads/del_index_20200404_p1.json --where _source.statistical_year=2019 --where _source.statistical_granularity!=10month
 *     --input /Users/yoreyuan/Downloads/index_20200404.json --output /Users/yoreyuan/Downloads/del_index_20200404_p2.json --where _source.statistical_year=2020 --where _source.statistical_granularity!=10month
 *
 *   2. statistical_year=2019， statistical_granularity=11month；（-63条），10923+10669=21592
 *     --input /Users/yoreyuan/Downloads/del_index_20200404_p1.json --output /Users/yoreyuan/Downloads/del_index_20200404_p3.json --where _source.statistical_granularity!=11month
 *
 *   3. statistical_year=2019， statistical_granularity=12month；（-53条），10870+10669=21539
 *   --input /Users/yoreyuan/Downloads/del_index_20200404_p3.json --output /Users/yoreyuan/Downloads/del_index_20200404_p4.json --where _source.statistical_granularity!=12month
 *
 *   4. statistical_year=2020， statistical_granularity=1month；（-96条），10870+10573=21443
 *   --input /Users/yoreyuan/Downloads/del_index_20200404_p2.json --output /Users/yoreyuan/Downloads/del_index_20200404_p5.json --where _source.statistical_granularity!=1month
 *
 *   5. statistical_year=2020， statistical_granularity=2month；（-116条），10870+10457=21327
 *   --input /Users/yoreyuan/Downloads/del_index_20200404_p5.json --output /Users/yoreyuan/Downloads/del_index_20200404_p6.json --where _source.statistical_granularity!=2month
 *
 *   汇中，将 del_index_20200404_p4.json 与 del_index_20200404_p6.json 合并
 *
 * </pre>
 *
 * Created by yore on 2020/4/4 02:59
 */
public class FilterJosnMain {


    public static void main(String[] args) throws Exception{
        String tips = new StringBuffer()
                .append("------------------------------").append("\n")
                .append("\t --input \tjson文件的输入路径。").append("\n")
                .append("\t --output \t过滤后的结果数据输出的路径").append("\n")
                .append("\t --where \t条件，可以跟多个，支持的运算符有：=、!=、>、>=、<、<=").append("\n")
                .append("\t --help \t输出帮助信息。").append("\n")
                .append("------------------------------")
                .toString();
        System.out.println(tips);

        JSONObject paramJsom = Utils.analyseInputArgs(args);
//        if(null!=paramJsom.getString("--help") && ""!=paramJsom.getString("--help")) System.out.println(tips);
        System.out.println(paramJsom);
        System.out.println(paramJsom.getString("--help"));
        List<String> whereList = (List<String>)paramJsom.get("where");


        BufferedReader in = Utils.getFileReader(paramJsom.getString("--input"));
        OutputStreamWriter out = Utils.getOutPutStreamWriter(paramJsom.getString("--output"));

        String line = "";
        Long count = 0L;

        int i= 0;
        while ( (line=in.readLine()) != null ){
            JSONObject.parse(line);

            // "where":["_source.end_date>='2019-10-01'","_source.end_date<'2020-04-01'"]

            boolean isSkip = true;
            for(String w : whereList){
                w = w.replaceAll("'|\"|\\s", "");
//
                String[] kv = new String[2];
//                System.out.println("2019-10-02".compareTo("2019-10-01"));// 1
//                System.out.println("2019-10-01".compareTo("2019-10-01"));// 0
//                System.out.println("2019-06-01".compareTo("2019-10-01"));// -1
                if(w.contains(">=")){
                    kv = w.split(">=");
                    if(String.valueOf(JSONPath.extract(line, "$." + kv[0])).compareTo(kv[1]) <0){
                        isSkip = false;
                    }
                }else if(w.contains("!=")){
                    kv = w.split("!=");
                    if(String.valueOf(JSONPath.extract(line, "$." + kv[0])).compareTo(kv[1]) == 0){
                        isSkip = false;
                    }
                }else if(w.contains("<=")){
                    kv = w.split("<=");
                    if(String.valueOf(JSONPath.extract(line, "$." + kv[0])).compareTo(kv[1]) >0){
                        isSkip = false;
                    }
                }else if(w.contains(">")){
                    kv = w.split(">");
                    if(String.valueOf(JSONPath.extract(line, "$." + kv[0])).compareTo(kv[1]) <= 0){
                        isSkip = false;
                    }
                }else if(w.contains("=")){
                    kv = w.split("=");
                    if(String.valueOf(JSONPath.extract(line, "$." + kv[0])).compareTo(kv[1]) != 0){
                        isSkip = false;
                    }
                }else if(w.contains("<")){
                    kv = w.split("<");
                    if(String.valueOf(JSONPath.extract(line, "$." + kv[0])).compareTo(kv[1]) >=0){
                        isSkip = false;
                    }
                }else {
                    throw new RuntimeException("不支持的操作符");
                }
            }

            if(isSkip){
                count++;
                out.write(line + "\n");
                out.flush();
            }
//            if(i>3)break;
            i++;
        }

        System.out.println("------------------------------");
        System.out.println("满足条件共：" + count);
        out.close();
        in.close();

    }




    static class Utils {

        public static JSONObject analyseInputArgs(String[] args) throws Exception{
            JSONObject json = new JSONObject();
            List<String> whereList = new ArrayList<String>();

            for(int i=0; i<args.length; i++){
                try {
                    if("--where".equalsIgnoreCase(args[i].trim())){
                        whereList.add(args[i+1]);
                    }else{
                        json.put(args[i], args[i+1]);
                    }
                }catch (Exception e){
                    throw new RuntimeException("请检查你输入的参数 " + args[i]);
                }
                i++;
            }
            json.put("where", whereList);
            return json;
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

    }


    /**
     * Created by yore on 2020/4/4 03:28
     */
    public enum Operate {
        eq("等于", "="),
        lt("小于", "<"),
        gt("大于", ">"),
        lte("小于等于", "<="),
        gte("大于等于", ">="),
        neq("不等于", "!=");

        private String name;
        private String sign;
        Operate(String name, String sign) {
            this.name = name;
            this.sign = sign;
        }

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getSign() {
            return sign;
        }
        public void setSign(String sign) {
            this.sign = sign;
        }
    }


}

