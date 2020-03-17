package yore;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * 通过读取 HDFS 上的信息获取表大小
 *
 * Created by yore on 2020/3/16 16:46
 */
public class GetTablesInfoHDFF {

    /**
     * 可以直接执行脚本（会提示输入的参数） ./get_tables_info.sh
     *
     * <pre>
     *     原理：
     *       通过 shell 提示用户需要输入的参数
     *       然后执行： java -jar hive-get-tables-info-1.1.jar  $hive_url $hive_username $hive_password $db_base_path
     *
     *
     *     代码本地测试，可以传入
     *       jdbc:hive2://cdh3:10000/hive_test "" "" hdfs://cdh1:8020/user/hive/warehouse/hive_test.db
     *       jdbc:hive2://cdh3:10000/impala_demo hive hive hdfs://cdh1:8020/user/hive/warehouse/impala_demo.db
     *       jdbc:hive2://cdh3:10000/default "" "" hdfs://cdh1:8020/user/hive/warehouse
     * </pre>
     *
     *
     * @auther: yore
     * @param args 传入主函数的参数列表
     * @date: 2020/3/16 1:35 PM
     */
    public static void main(String[] args) throws IOException {
        //System.out.println(Arrays.toString(args));
        //String database_name = args[0];
        String hive_url = args[0];
        String user = args.length>1? args[1]:"";;
        String password = args.length>2? args[2].trim():"";
        String db_base_path = args.length>3? args[3].trim():"/user/hive/warehouse";


        boolean hashPassword = password!=null && password!="";
        String dbName = hive_url.substring(hive_url.lastIndexOf("/"));
        // 参数校验。校验 url 中库名 和 数据库在 HDFS 的路径是否已同一个
        if(!("/default".equals(dbName))){
            String pathDb = db_base_path.substring(db_base_path.lastIndexOf("/"));
            if( (pathDb.indexOf(".db")>0 && !pathDb.split("\\.")[0].equals(dbName))||
                    (pathDb.indexOf(".db")<0 && !pathDb.equals(dbName))){
                throw new RuntimeException("url指定的库和 hdfs 上库的路径不一致，请检查输入的参数！");
            }
        }
        db_base_path = db_base_path.endsWith("/")? db_base_path:db_base_path+"/";

        // 获取所有表的 Beeline 命令
        String getTablesBeelineShell = hashPassword?
                "beeline -n $user -p $password -d org.apache.hive.jdbc.HiveDriver -u $hive_url --showHeader=false --outputformat=csv2 -e 'SHOW TABLES;'"
                        .replace("$user", user)
                        .replace("$password", password)
                        .replace("$hive_url", hive_url):
                "beeline -d org.apache.hive.jdbc.HiveDriver -u $hive_url --showHeader=false --outputformat=csv2 -e 'SHOW  TABLES;'"
                        .replace("$hive_url", hive_url);


        OutputStreamWriter out = Utils.getOutPutStreamWriter("." + dbName + "_tableInfo-"+System.currentTimeMillis()+".csv");
        out.write("表名（table_name）,大小（total_size）\n");

        List<String> tables = Utils.runShellScript2(getTablesBeelineShell);
        boolean startShow = false;
        for(String table : tables){
            // 表可能会存在单个或多个
            if((table.indexOf("row selected")>0 || table.indexOf("rows selected")>0) && table.indexOf(")")>0){
                startShow = false;
            }

            if(startShow){
                System.out.print(table + " 表处理中：");
                out.write(table+",");
                getTableInfo(db_base_path + table, out);
            }

            if("INFO  : OK".equals(table)){
                startShow = true;
            }
        }

        out.flush();
        out.close();

    }


    /**
     * 根据 hdfs 命令获取表的信息（主要是获取表大小）
     * <pre>
     *     -rwxrwxrwx   3 root supergroup        279 2019-03-25 14:19 /user/hive/warehouse/hive_test.db/tmp_test/000000_0
     *     -rwxrwxrwx   3 root supergroup        299 2019-03-25 15:34 /user/hive/warehouse/hive_test.db/tmp_test/000000_0_copy_1
     * </pre>
     *
     * @auther: yore
     * @param hdfsPathStr HDFS 命令字符串：`hdfs dfs -count path`
     * @param  out OutputStreamWriter
     * @date: 2020/3/16 5:03 PM
     */
    private static void getTableInfo(String hdfsPathStr, OutputStreamWriter out) throws IOException {
        String hdfsCommand = "hdfs dfs -count " + hdfsPathStr;
        boolean hashTotalSize = false;
        try {
            List<String> results = Utils.runShellScript2(hdfsCommand);
            for(String info : results){
                if(!(info.indexOf("ERROR")>0 || info.indexOf("WARN")>0 ||
                        info.indexOf("INFO")>0 || info.indexOf("DEBUG")>0)){
                    String totalSize = Utils.formetFileSize(Long.parseLong(info.trim().split("\\s+")[2]));
                    System.out.println(totalSize);
                    out.write(totalSize + "\n");
                    hashTotalSize = true;
                }
            }
        }catch (Exception e){
        }

        // 如果没有查询到表大小 默认赋为 0
        if(!hashTotalSize){
            System.out.println("0");
            out.write(Utils.formetFileSize(0L) + "\n");
        }
        out.flush();

    }


}
