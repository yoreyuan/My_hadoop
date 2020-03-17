package yore;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 这种方式需要将pom 依赖都打入包中
 *
 * Created by yore on 2020/3/16 16:46
 */
public class GetTablesInfoBySQL {

    // Hive 连接信息
    private static final String dirverName = "org.apache.hive.jdbc.HiveDriver";

    /**
     * java -jar hive-get-tables-info-1.0-SNAPSHOT.jar  jdbc:hive2://cdh3:10000/hive_test hive hive
     *
     * @auther: yore
     * @param
     * @return
     * @date: 2020/3/16 5:03 PM
     */
    public static void main(String[] args) {
        String url = args[0];
        String user = args.length>1? args[1]:"";
        String password = args.length>2? args[2]:"";

        String dbName = url.substring(url.lastIndexOf("/"));
        //BufferedReader in = getFileReader("." + dbName + "_tableInfo-"+System.currentTimeMillis()+".csv");
        OutputStreamWriter out = Utils.getOutPutStreamWriter("." + dbName + "_tableInfo-"+System.currentTimeMillis()+".csv");


        try {
            boolean hashPassword = password!=null && password!="";
            Class.forName(dirverName);
            Connection conn;
            if(hashPassword){ // 带密码的
                conn = DriverManager.getConnection(url, user, password);
            }else{ // 不带密码的
                conn = DriverManager.getConnection(url);
            }

            PreparedStatement ps = conn.prepareStatement("SHOW TABLES");
            ResultSet result = ps.executeQuery();

            List<String> tablesName = new ArrayList<>();
            while (result.next()){
                tablesName.add(result.getString(1));
            }
            ps.close();

            // 写出表头信息
            out.write("表名（table_name）,大小（total_size）\n");
            out.flush();

            for(String tName : tablesName){
                ps = conn.prepareStatement("DESC formatted " + tName);
                result = ps.executeQuery();

                /**
                 * +-------------------------------+----------------------------------------------------+-----------------------+
                 * |           col_name            |                     data_type                      |        comment        |
                 * +-------------------------------+----------------------------------------------------+-----------------------+
                 * | # col_name                    | data_type                                          | comment               |
                 * |                               | NULL                                               | NULL                  |
                 * | id                            | int                                                |                       |
                 * | name                          | string                                             |                       |
                 * |                               | NULL                                               | NULL                  |
                 * | # Detailed Table Information  | NULL                                               | NULL                  |
                 * | Database:                     | hive_test                                          | NULL                  |
                 * | OwnerType:                    | USER                                               | NULL                  |
                 * | Owner:                        | root                                               | NULL                  |
                 * | CreateTime:                   | Fri Aug 30 12:46:46 CST 2019                       | NULL                  |
                 * | LastAccessTime:               | UNKNOWN                                            | NULL                  |
                 * | Retention:                    | 0                                                  | NULL                  |
                 * | Location:                     | hdfs://cdh1:8020/user/hive/warehouse/hive_test.db/tmp_test | NULL          |
                 * | Table Type:                   | MANAGED_TABLE                                      | NULL                  |
                 * | Table Parameters:             | NULL                                               | NULL                  |
                 * |                               | numFiles                                           | 2                     |
                 * |                               | numRows                                            | 3                     |
                 * |                               | rawDataSize                                        | 200                   |
                 * |                               | totalSize                                          | 578                   |
                 * |                               | transient_lastDdlTime                              | 1584344072            |
                 * |                               | NULL                                               | NULL                  |
                 * | # Storage Information         | NULL                                               | NULL                  |
                 * | SerDe Library:                | org.apache.hadoop.hive.ql.io.orc.OrcSerde          | NULL                  |
                 * | InputFormat:                  | org.apache.hadoop.hive.ql.io.orc.OrcInputFormat    | NULL                  |
                 * | OutputFormat:                 | org.apache.hadoop.hive.ql.io.orc.OrcOutputFormat   | NULL                  |
                 * | Compressed:                   | No                                                 | NULL                  |
                 * | Num Buckets:                  | -1                                                 | NULL                  |
                 * | Bucket Columns:               | []                                                 | NULL                  |
                 * | Sort Columns:                 | []                                                 | NULL                  |
                 * | Storage Desc Params:          | NULL                                               | NULL                  |
                 * |                               | field.delim                                        | ?                     |
                 * |                               | serialization.format                               | ?                     |
                 * +-------------------------------+----------------------------------------------------+-----------------------+
                 */
                while (result.next()){
                    // System.out.println(result.getString(1) + "\t" + result.getString(2) + "," + result.getString(3));
                    // 如果是表的 totalSize
                    String data_type = result.getString(2);
                    ;
                    if((data_type!=null && data_type!="") && "totalSize".equals(data_type.trim())){
                        Long totalSize = Long.parseLong(result.getString(3).trim());
                        out.write(tName + "," + Utils.formetFileSize(totalSize) + "\n");
                        out.flush();

                    }

                }

            }
            // 关闭文件输出流
            out.close();
        } catch (ClassNotFoundException | SQLException | IOException /*| InterruptedException*/ e) {
            e.printStackTrace();
        }
    }


}
