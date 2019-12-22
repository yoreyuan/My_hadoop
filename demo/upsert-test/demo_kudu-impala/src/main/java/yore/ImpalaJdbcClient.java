package yore;

import java.sql.*;

/**
 * Created by yore on 2019/11/24 15:50
 */
public class ImpalaJdbcClient {

    final static String JDBC_DRIVER = "com.cloudera.impala.jdbc41.Driver";
    final static String URL = "jdbc:impala://cdh3:21050/upsert_test";

    public static void main(String[] args) throws SQLException {
        System.out.println("-------------------");
        System.out.println(
                "\t 0 \t创建 Impala 表\n" +
                "\t 1 \t创建 Kudu 表\n" +
                "\t 2 \t测试支持的最大列\n" +
                "\t 3 \t根据主键将 600W 指标数据更新插入到 指标表中\n"
        );

        long start = System.currentTimeMillis();

        if("0".equals(args[0]) ){
            // 创建 Impala 表
            createImpalaTable();
        }else if("1".equals(args[0])){
            // 创建 Kudu 表
            createKuduTable();
        }else if("2".equals(args[0])){
            // 测试 Impala 支持的最大列数
//            supportColumnMax(300);
            supportColumnMax(10000);
        }else if("3".equals(args[0])){
            update600w();
        }else if("4".equals(args[0])){
            insert600w();
        }

        long end = System.currentTimeMillis();

        System.out.println("-------------------");
        System.out.println(" 本次执行共花费：" + (end-start) +"ms");

    }


    /**
     * 创建一个 Kudu 表
     *
     * @auther: yore
     * @date: 2019/11/25 12:48 PM
     */
    public static void createKuduTable() throws SQLException {
        Connection conn = getConn();

        // 建表语句
        String sql = "CREATE TABLE target(\n" +
                "id BIGINT,\n" +
                "name STRING,\n" +
                "indicator1 DOUBLE,\n" +
                "PRIMARY KEY (id)\n" +
                ") PARTITION BY HASH (id) PARTITIONS 3\n" +
                "STORED AS KUDU;";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.executeUpdate();

        sql = "SHOW TABLES";
        ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        printResultSet(rs);

        ps.close();
        conn.close();
    }


    /**
     * 创建一个 Impala 表
     *
     * @auther: yore
     * @date: 2019/11/25 12:48 PM
     */
    public static void createImpalaTable() throws SQLException {
        Connection conn = getConn();

        // 建表语句
        String sql = "CREATE TABLE target_tmp(\n" +
                "id BIGINT,\n" +
                "name STRING,\n" +
                "indicator1 DOUBLE\n" +
                ")ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' \n" +
                "STORED AS TEXTFILE;";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.executeUpdate();

        ps.close();
        conn.close();
    }

    /**
     * 测试 Kudu表 支持的最大列
     *
     * @auther: yore
     * @date: 2019/11/25 12:48 PM
     */
    public static void supportColumnMax(Integer limit) throws SQLException {
        Connection conn = getConn();
        PreparedStatement ps = null;

        for(int i=3; i<limit; i++){
            try {
                // 添加列
                String sql = "ALTER TABLE upsert_test.target_i_tmp ADD COLUMNS(indicator"+(i-1)+" DOUBLE);";
                ps = conn.prepareStatement(sql);
                ps.executeUpdate();
                System.out.println("添加第" + (i-1) + " 字段");

            }catch (Exception e){
                e.printStackTrace();
                System.out.println(" 添加第" + (i-1) + " 字段时报错！！");
                System.out.println("---------------");
                System.out.println("最大允许创建： " + (i+1) + "列");
                break;
            }
        }

        if(ps != null){
            ps.close();
        }

        ps.close();
        conn.close();
    }


    /**
     * 根据主键将 600W 指标数据更新插入到 指标表中
     *
     * @auther: yore
     * @date: 2019/11/26 3:48 PM
     */
    public static void update600w(){
        Connection conn = getConn();
        String sql = "UPDATE t0 SET indicator2=t1.indicator2 \n" +
                "FROM target t0 LEFT JOIN target_p1 t1 \n" +
                "ON t1.id=t2.id;";


    }

    /**
     * 根据主键将 3000W 指标数据更新插入到 指标表中
     *
     * @auther: yore
     * @date: 2019/11/26 3:48 PM
     */
    public static void insert600w(){

    }




    public static Connection getConn() {
        try {
            Class.forName(JDBC_DRIVER);
            return DriverManager.getConnection(URL);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void printResultSet(ResultSet rs) throws SQLException {
        while (rs.next()){
            for(int pos=1; pos<=rs.getMetaData().getColumnCount(); pos++){
                System.out.print(rs.getObject(pos)+ "\t");
            }
            System.out.println();
        }
    }

}
