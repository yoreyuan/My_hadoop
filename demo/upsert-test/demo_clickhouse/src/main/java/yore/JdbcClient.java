package yore;

import java.sql.*;

/**
 * Created by yore on 2019/11/25 15:55
 */
public class JdbcClient {

    final static String JDBC_DRIVER = "ru.yandex.clickhouse.ClickHouseDriver";
    final static String URL = "jdbc:clickhouse://cdh3:8123/upsert_test";

    public static void main(String[] args) throws SQLException {
        System.out.println("-------------------");
        System.out.println(
                "\t 0 \t建表\n" +
                        "\t 1 \t测试支持的最大列\n" +
                        "\t 2 \t根据主键将 600W 指标数据更新插入到 指标表中\n"
        );

        long start = System.currentTimeMillis();
        if("0".equals(args[0]) ){
            System.out.println("创建 ClickHouse 表");
            createClickHouseTable();
        }else if("1".equals(args[0])){
            System.out.println("开始测试Impala 支持的最大列数");
            supportColumnMax();
        }else if("2".equals(args[0])){
//            update600w();
        }else if("3".equals(args[0])){
//            update3000w();
        }

        long end = System.currentTimeMillis();

        System.out.println("-------------------");
        System.out.println(" 本次执行共花费：" + (end-start) +"ms");
    }

    /**
     * 创建一个 ClickHouse 表
     *
     * @auther: yore
     * @date: 2019/11/25 12:48 PM
     */
    public static void createClickHouseTable() throws SQLException {
        Connection conn = getConn();

        // 建表语句
        String sql = "CREATE TABLE target(\n" +
                " id           UInt64  COMMENT '标签主键',\n" +
                " name   FixedString(64)  COMMENT '用户名',\n" +
                " indicator1    Float64  COMMENT '指标1'\n" +
                ")ENGINE = MergeTree ORDER BY id;";
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
     * 测试 ClickHouse 表 支持的最大列
     *
     * @auther: yore
     * @date: 2019/11/25 12:48 PM
     */
    public static void supportColumnMax() throws SQLException {
        Connection conn = getConn();
        PreparedStatement ps = null;

        for(int i=3; i<10000; i++){
            try {
                // 添加列
                String sql = "ALTER TABLE upsert_test.target ADD COLUMN indicator"+(i-1)+" Float64 COMMENT '指标"+(i-1)+"';";
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



    private static Connection getConn() {
        try {
            Class.forName(JDBC_DRIVER);
            // 添加用户和密码的认证
            return DriverManager.getConnection(URL, "default", "KavrqeN1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void printResultSet(ResultSet rs) throws SQLException {
        while (rs.next()){
            for(int pos=1; pos<=rs.getMetaData().getColumnCount(); pos++){
                System.out.print(rs.getObject(pos)+ "\t");
            }
            System.out.println();
        }
    }

}
