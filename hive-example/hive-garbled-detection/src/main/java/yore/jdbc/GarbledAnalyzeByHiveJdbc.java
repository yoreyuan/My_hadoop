package yore.jdbc;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yore.PropertiesUtil;
import yore.Util;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 通过 Hive JDBC 方式分析数据中的乱码信息
 *
 *
 *
 * Created by yore on 2019-04-25 10:36
 */
public class GarbledAnalyzeByHiveJdbc {
    // Log
    private static Logger LOG = LoggerFactory.getLogger(GarbledAnalyzeByHiveJdbc.class);
    // Hive 连接信息
    private static String url = PropertiesUtil.getMyProperties().getProperty("hive.url", "jdbc:hive2://localhost:10000/default");
    private static String user = PropertiesUtil.getPropString("hive.thrift.user");
    private static String password = PropertiesUtil.getPropString("hive.thrift.password");
    private static int limitNum = PropertiesUtil.getPropInt("result.limit.num");


    /**
     *  -u root -p 123456 -url jdbc:hive2://node1:10000/default -tables dw_orders_his,garbled_t -fields status;name,desc
     */
    public static void main(String[] args) throws IOException {
        System.out.println(
                "--------------------------------\n" +
                " 运行jar包前请配置好项目中 my.properties中的参数！\n" +
                " 如果未指定任何参数，程序将以配置文件中的配置参数为准进行分析，分析当前库下的所有表中未字符串类型(string、varchar)的字段值是否存在乱码。\n" +
                " 如果指定参数，本程序支持如下参数：\n" +
                "\t -u \t\t 连接 Hive 的用户名。可选，如果未指定，以配置文件为准。\n" +
                "\t -p \t\t 连接 Hive 的密码。如果未指定，以配置文件为准。\n" +
                "\t -url \t\t 连接 Hive 的ip。例如：jdbc:hive2://hive_ip:10000/库名 。如果未指定，以配置文件为准，如果未配置默认连接 jdbc:hive2://localhost:10000/default\n\n" +

                "\t -tables \t\t 可选。明确指定某一表名，多表时请用英文逗号分隔，如：id,name。不指定将分析url指定库的所有表。\n" +
                "\t -limit \t\t 限制最终分析结果的输出的条数，如：类似于 SQL 中的 limit n。不指定则以配置文件我准，如果没有默认为10 \n" +
                "\t -fields \t\t 可选。指定对应表需要分析的字段值，不同表之间用英文分号分割，如：id,name;id,desc,name。如果不指定将分析指定表的字符串类型的字段。\n" +
                "--------------------------------\n"
        );

        List<String> hintTableName = new ArrayList<>();
        Map<String, String> hintField = new HashMap<>();

        initArgsByInput(args, hintTableName, hintField);

        System.out.println(url);
        System.out.println(user);
        System.out.println(password);
        System.out.println(hintTableName);
        System.out.println(hintField);

        String outputPath = "/Users/yoreyuan/Downloads/" + "hive_" + user + "_garbled_analyze_result.md";
        System.out.println(outputPath);
        OutputStreamWriter out = Util.getOutPutStreamWriter(outputPath);

        // TODO
//        executeTableGarbled();
        // 1.

        /**
         * 一、分析表名是否存在乱码
         *      1.1 指定的表分析
         *      1.2 未指定时的分析
         */





        out.close();
    }


    /**
     * 根据输入的参数，进行初始化
     *
     * @auther: yore
     * @param args 程序输入的参数数组
     * @param hintTableName 指明分析的表名集合
     * @param hintField 指明分析的各表的字段
     * @date: 2019/4/25 5:31 PM
     */
    public static void initArgsByInput(String[] args,
                                       List<String> hintTableName, Map<String, String> hintField){
        try {
            for(int i=0; i< args.length; i++){
                switch (args[i]){
                    case "-u" :{
//                        System.out.println("user " + args[i+1]);
                        if(StringUtils.isNotBlank(args[i+1])){
                            user = args[i+1];
                        }
                        i++;
                        break;
                    }
                    case "-p" : {
//                        System.out.println("password " + args[i+1]);
                        if(StringUtils.isNotBlank(args[i+1])){
                            password = args[i+1];
                        }
                        i++;
                        break;
                    }
                    case "-url" : {
//                        System.out.println("url " + args[i+1]);
                        if(StringUtils.isNotBlank(args[i+1])){
                            url = args[i+1];
                        }
                        i++;
                        break;
                    }
                    case "-tables" : {
//                        System.out.println("tables " + args[i+1]);
                        if(StringUtils.isNotBlank(args[i+1])){
                            for(String ts : args[i+1].split(","))
                                hintTableName.add(ts);
                        }
                        i++;
                        break;
                    }
                    case "-fields" : {
//                        System.out.println("fields " + args[i+1]);
                        if(StringUtils.isNotBlank(args[i+1])){
                            String[] fsArr = args[i+1].split(";");
                            for(int j=0;j<fsArr.length; j++){
                                hintField.put(hintTableName.get(j), fsArr[j]);
                            }
                        }
                        i++;
                        break;
                    }
                }
            }
        }catch (Exception e){
            LOG.error("输入的参数有误，请确认输入的参数。");
            e.printStackTrace();
        }
    }

    private static void executeTableGarbled(){
        String sql = "show tables";
        Connection conn = HiveJdbcUtil.getConnection();


        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet result = ps.executeQuery();
            while (result.next()){
                String tableName = result.getString(1);
                System.out.println(tableName);

                // 获取该表元数据信息 -- 字段中是 string 类型的列名
                List<String> stringFieldNameList = getTableMateDate(tableName);

                //TODO 对字符串类型的数据分析
                executeTableLineGarbled(tableName, stringFieldNameList);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static void executeTableLineGarbled(String tableName, List<String> stringFieldNameList){
        String sql = "SELECT * FROM " + tableName;
        System.out.println(sql);
        Connection conn = HiveJdbcUtil.getConnection();

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
//            ps.setString(1, tableName);

            ResultSet result = ps.executeQuery();
            while (result.next()){
                for(String fileName:stringFieldNameList){
                    System.out.print(result.getString(fileName) + "\t");
                }
                System.out.println("\n ---------------");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static List<String> getTableMateDate(String tableName){
        List<String> fieldNameList = new ArrayList<>();
        String sql = "desc " + tableName;
        Connection conn = HiveJdbcUtil.getConnection();
        try {
            ResultSet r = conn.prepareStatement(sql).executeQuery();
            while (r.next()){
                String fieldName = r.getString(1);
                String fieldType = r.getString(2);
                boolean isString = "string".equalsIgnoreCase(fieldType.trim());
                System.out.println("meta date: \t\t" + fieldName + "\t" + fieldType + "\t" + isString);
                if(isString){
                    fieldNameList.add(fieldName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fieldNameList;
    }



    static class HiveJdbcUtil{
        private static final String dirverName = PropertiesUtil.getPropString("hive.driver");
        // 连接池的大小
        private static final Integer POOL_SIZE = PropertiesUtil.getPropInt("hive.connection.pool.size");
        private static LinkedBlockingQueue<Connection> dbConnectionPool = new LinkedBlockingQueue<>(POOL_SIZE);

        static {
            boolean hashPassword = StringUtils.isNotBlank(password);
            try {
                Class.forName(dirverName);
                // 初始化给定大小的连接池
                for(int i=0; i< POOL_SIZE; i++){
                    Connection conn;
                    if(hashPassword){
                        conn = DriverManager.getConnection(url, user, password);
                    }else{
                        conn = DriverManager.getConnection(url);
                    }
                    dbConnectionPool.put(conn);
                }
            }catch (ClassNotFoundException | SQLException | InterruptedException e) {
                e.printStackTrace();
                LOG.error("An exception occurred while getting Hive Connection !! ");
            }
        }

        /**
         * 获取连接对象
         *
         * @auther: yore
         * @date: 2017/12/27 11:45
         */
        public static Connection getConnection(){
            while (0 == dbConnectionPool.size()){
                try {
                    Thread.sleep(2);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            try {
                return dbConnectionPool.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return dbConnectionPool.poll();
            }
        }


        /**
         * 回收连接对象
         *
         * @auther: yore
         * @date: 2017/12/27 11:45
         */
        public static void closeConnection(Connection conn){
            if(conn != null){
                try {
                    dbConnectionPool.put(conn);
                } catch (InterruptedException e) {
                    LOG.error("An exception occurred while Connection was recycled");
                    e.printStackTrace();
                }
            }
        }



        public static void main(String[] args) throws Exception{
            Connection conn2 = getConnection();
            ResultSet r = conn2.prepareStatement("show tables").executeQuery();

            while (r.next()){
                System.out.println(r.getString(1));
            }

        }
    }



}
