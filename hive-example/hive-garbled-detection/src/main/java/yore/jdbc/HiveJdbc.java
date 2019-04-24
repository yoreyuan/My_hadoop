package yore.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yore on 2019-04-25 10:36
 */
public class HiveJdbc {
    // Log
    private static Logger LOG = LoggerFactory.getLogger(HiveJdbc.class);


    public static void main(String[] args) {
        // TODO

        executeTableGarbled();

    }

    private static void executeTableGarbled(){
        String sql = "show tables";
        Connection conn = HiveJdbcUtil.getCon();


        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet result = ps.executeQuery();
            while (result.next()){
                String tableName = result.getString(1);
                System.out.println(tableName);

                //TODO 获取该表元数据信息
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
        Connection conn = HiveJdbcUtil.getCon();

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
        Connection conn = HiveJdbcUtil.getCon();
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
        private static final String dirverName = "org.apache.hive.jdbc.HiveDriver";
        private static final String url = "jdbc:hive2://node1:10000/default";

        static ThreadLocal<Connection> tl = new ThreadLocal<>();

        private static Connection conn;

        static {
            try {
                Class.forName(dirverName);
                conn = DriverManager.getConnection(url);
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                LOG.error("An exception occurred while getting Hive Connection !! ");
            }
        }

        public static Connection getCon(){
            try {
                if(tl.get() == null){
                    conn = DriverManager.getConnection(url);
                    tl.set(conn);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return tl.get();
        }
    }

}
