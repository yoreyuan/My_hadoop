package yore;

import io.prestosql.jdbc.PrestoConnection;
import io.prestosql.jdbc.PrestoStatement;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Properties;
import java.util.TimeZone;

/**
 * JDBC访问 Presto
 * @see <a href="https://prestodb.github.io/docs/current/installation/jdbc.html">JDBC Driver<a/><br/>
 *
 * Created by yore on 2019/5/8 13:10
 */
public class PrestoJDBCClient {

    public static void printrow(ResultSet rs, int[] types) throws SQLException {
        for(int i=0; i<types.length; i++){
            System.out.print(" ");
            System.out.print(rs.getObject(i+1));
        }
        System.out.println("");
    }

    public static void connect() throws Exception {
        // @see <a href="https://en.wikipedia.org/wiki/List_of_tz_database_time_zones">List of tz database time zones</a>
        // 设置时区，这里必须要设置  map.put("CTT", "Asia/Shanghai");
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.SHORT_IDS.get("CTT")));

        PrestoConnection conn = null;
        PrestoStatement statement = null;
        try {
            // 加载 Presto JDBC 驱动类
            Class.forName("io.prestosql.jdbc.PrestoDriver");

            /*
             * presto连接串：
             *      在 url 指定默认的 catalog 为：system, 默认为 Schema 为： runtime ,
             *      使用的用户名为： presto-user，这个用户名根据实际自己设定，用来标示执行 SQL 的用户，虽然不会通过该用户名进行身份认证，大师必须要写
             *      密码直接为 null, 或者可以随意指定一个任意密码， Presto 是不会对密码进行认证的。
             */
            /*conn = (PrestoConnection)DriverManager.getConnection(
                    // 端口查看config.properties文件中配置项http-server.http.port的值
                    "jdbc:presto://cdh6:8080/hive/default",
                    "presto-user",
                    null
            );*/


            // URL parameters
            String url = "jdbc:presto://cdh6:8080/hive/default";
            Properties properties = new Properties();
            properties.setProperty("user", "presto-user");
//            properties.setProperty("password", null);
            properties.setProperty("SSL", "false");
            conn = (PrestoConnection)DriverManager.getConnection(url, properties);

            statement = (PrestoStatement)conn.createStatement();

            String sql = "select * from nodes";
            sql = "show tables";
            sql = " select count(*) from dw_orders_his";
            long start = System.currentTimeMillis();
            ResultSet rs = statement.executeQuery(sql);


            int cn = rs.getMetaData().getColumnCount();
            int[] types = new int[cn];
            for(int i=0; i<cn; i++){
                types[i] = rs.getMetaData().getColumnType(i+1);
            }

            while (rs.next()){
                printrow(rs, types);
            }
            System.out.println("-------");
            System.out.println("用时：" + (System.currentTimeMillis()-start) + "(ms)");

        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        connect();
    }

}
