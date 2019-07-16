package yore;

import org.junit.Test;

import java.sql.*;
import java.util.Arrays;

/**
 * 建议：
 *   建表时，指定SCHM，例如创建person表，
 *
 * 注意：
 *   Phoenix是区分大小写的，默认是大写，如果是小写请用引号引起来
 *
 * Created by yore on 2019/7/07 09:12
 */
public class PhoenixJdbcClient {

    static String url = "jdbc:phoenix:cdh1,cdh2,cdh3:2181";
    static String driver = "org.apache.phoenix.jdbc.PhoenixDriver";

    static {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }



    /**
     * 创建表（带SCHEM的）
     *
     * @auther: yore
     * @throws SQLException
     */
    @Test
    public void createTable() throws SQLException{
        Connection conn = DriverManager.getConnection(url);
        String sql = "create table IF NOT EXISTS test.Person (\"idcard_num\" INTEGER not null primary key, \"name\" varchar(20),\"age\" INTEGER)";
        PreparedStatement ps = conn.prepareStatement(sql);

        Integer num = ps.executeUpdate();
        System.out.println("num = " + num);

        ps.close();
        conn.close();
    }


    /**
     * 插入数据（批量）
     *
     * @auther: yore
     * @throws SQLException
     */
    @Test
    public void insertRow() throws SQLException{
        Connection conn = DriverManager.getConnection(url);
        String sql = "upsert into test.Person(\"idcard_num\" , \"name\" ,\"age\") values(?, ?, ?)";
        conn.setAutoCommit(false);
        PreparedStatement ps = conn.prepareStatement(sql);

        int[] idCardArr = new int[]{100, 101, 103};
        String[] name = new String[]{"小明", "小红", "小王"};
        int[] age = new int[]{11, 22, 33};

        for(int i=0; i<idCardArr.length; i++){
            System.out.println("insert idCardArr=" + idCardArr[i]);
            ps.setInt(1, idCardArr[i]);
            ps.setString(2, name[i]);
            ps.setInt(3, age[i]);
            ps.addBatch();
        }

        int[] numArr = ps.executeBatch();
        System.out.println("num = " + Arrays.toString(numArr));

        conn.commit();
        ps.close();
        conn.close();
    }


    /**
     * 查询数据
     *
     * @auther: yore
     * @throws SQLException
     */
    @Test
    public void queryData() throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        String  sql = "select * from test.person";

        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()){
            Integer idcard_num = rs.getInt("idcard_num");
            String name = rs.getString("name");
            Integer age = rs.getInt("age");

            System.out.println("idcard_num="+idcard_num + "\tname=" + name + "\tage=" + age);
        }

        rs.close();
        ps.close();
        conn.close();

    }

    /**
     * 更新数据，同插入数据相同，但查的数据 如果是字符串，一定用 单引号
     *
     * @auther: yore
     * @throws SQLException
     */
    @Test
    public void updateRow () throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        String sql = "upsert into test.person(\"idcard_num\",\"name\", \"age\") VALUES(102, '小兰' ,20)";

        PreparedStatement ps = conn.prepareStatement(sql);
        Integer num = ps.executeUpdate();

        System.out.println("num = " + num);

        conn.commit();
        ps.close();
        conn.close();

    }


    /**
     * 删除数据，
     *
     * @auther: yore
     * @throws SQLException
     */
    @Test
    public void deleteRow() throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        String sql = "DELETE FROM  test.person WHERE \"idcard_num\"=102 ";

        PreparedStatement ps = conn.prepareStatement(sql);
        Integer num = ps.executeUpdate();

        System.out.println("num = " + num);

        conn.commit();
        ps.close();
        conn.close();
    }


}
