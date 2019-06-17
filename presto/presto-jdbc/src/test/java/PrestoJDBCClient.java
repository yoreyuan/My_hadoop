//package yore;

import io.prestosql.jdbc.PrestoConnection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
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

    static final String JDBC_DRIVER  = "io.prestosql.jdbc.PrestoDriver";
    static final String URL = "jdbc:presto://cdh6:8080/kudu/default";

    public static void main(String[] args) {
        // @see <a href="https://en.wikipedia.org/wiki/List_of_tz_database_time_zones">List of tz database time zones</a>
        // 设置时区，这里必须要设置  map.put("CTT", "Asia/Shanghai");
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.SHORT_IDS.get("CTT")));

        Properties properties = new Properties();
        properties.setProperty("user", "presto-user");
        properties.setProperty("SSL", "false");

        PrestoConnection conn = null;
        PreparedStatement ps = null;

        try {
            Class.forName(JDBC_DRIVER);
            conn = (PrestoConnection)DriverManager.getConnection(URL, properties);

            String sql = "show tables";
            sql = "select * from tag_5 limit 10";

            ps = conn.prepareStatement(sql);
            ResultSet result = ps.executeQuery();

            long start = System.currentTimeMillis();
            while (result.next()){
                for(int pos=1; pos< result.getMetaData().getColumnCount(); pos++){
                    System.out.print(result.getObject(pos) + "\t");
                }
                System.out.println();
            }

            long end = System.currentTimeMillis();
            System.out.println("--------------------------Presto" );
            System.out.println("\\t共花费：" + (double) (end - start) /1000 + "秒");

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("--------------------------" );
        }
    }
}
