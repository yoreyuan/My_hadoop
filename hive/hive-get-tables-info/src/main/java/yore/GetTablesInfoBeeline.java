package yore;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 *
 * Created by yore on 2020/3/16 16:46
 */
public class GetTablesInfoBeeline {

    /**
     * 可以直接执行脚本（会提示输入的参数） ./get_tables_info.sh
     *
     * <pre>
     *     原理：
     *       通过 shell 提示用户需要输入的参数
     *       然后执行： java -jar hive-get-tables-info-1.0-SNAPSHOT.jar $database_name $hive_url $hive_username $hive_password
     *
     *
     *     代码本地测试，可以传入
     *       hive_test jdbc:hive2://cdh3:10000/hive_test "" ""
     *       impala_demo jdbc:hive2://cdh3:10000/impala_demo hive hive
     * </pre>
     *
     *
     *
     * @auther: yore
     * @param args 传入主函数的参数列表
     * @date: 2020/3/16 1:35 PM
     */
    public static void main(String[] args) throws IOException {
        String database_name = args[0];
        String hive_url = args[1];
        String user = args.length>2? args[2]:"";;
        String password = args.length>3? args[3].trim():"";
        boolean hashPassword = password!=null && password!="";

        // 获取所有表的 Beeline 命令
        String getTablesBeelineShell = hashPassword?
                "beeline -n $user -p $password -d org.apache.hive.jdbc.HiveDriver -u $hive_url --showHeader=false --outputformat=csv2 -e 'SHOW TABLES;'"
                        .replace("$user", user)
                        .replace("$password", password)
                        .replace("$hive_url", hive_url):
                "beeline -d org.apache.hive.jdbc.HiveDriver -u $hive_url --showHeader=false --outputformat=csv2 -e 'SHOW  TABLES;'"
                        .replace("$hive_url", hive_url);
        //System.out.println("$ " + getTablesBeelineShell);

        String getTableInfoBeelineShell = hashPassword?
                "beeline -n $user -p $password -d org.apache.hive.jdbc.HiveDriver -u $hive_url --showHeader=false --outputformat=csv2 -e 'DESC formatted ?1;'"
                        .replace("$user", user)
                        .replace("$password", password)
                        .replace("$hive_url", hive_url):
                "beeline -d org.apache.hive.jdbc.HiveDriver -u $hive_url --showHeader=false --outputformat=csv2 -e 'DESC formatted ?1;'"
                        .replace("$hive_url", hive_url);




        OutputStreamWriter out = Utils.getOutPutStreamWriter("./" + database_name + "_tableInfo-"+System.currentTimeMillis()+".csv");
        out.write("表名（table_name）,大小（total_size）\n");

        List<String> tables = Utils.runShellScript2(getTablesBeelineShell);
        boolean startShow = false;
        for(String table : tables){
            // 表可能会存在单个或多个
            if((table.indexOf("rows selected")>0 || table.indexOf("row selected")>0) && table.indexOf(")")>0){
                startShow = false;
            }

            if(startShow){
                System.out.print(table + " 表处理中：");
                out.write(table+",");
                getTableInfo(getTableInfoBeelineShell.replace("?1", table), out);
            }

            if("INFO  : OK".equals(table)){
                startShow = true;
            }
        }

        out.flush();
        out.close();

    }


    /**
     * 根据 Beeline 命令获取表的信息（主要是解析表大小 totalSize）
     *
     * @auther: yore
     * @param shellStr Beeline 获取表格式详情的命令字符串
     * @param  out OutputStreamWriter
     * @date: 2020/3/16 5:03 PM
     */
    private static void getTableInfo(String shellStr, OutputStreamWriter out) throws IOException {
        //System.out.println("$ " + shellStr);

        List<String> results = Utils.runShellScript2(shellStr);
        boolean startShow = false, hashtotalSize = false;
        for(String info : results){
            if(info.indexOf("rows selected")>0 && info.indexOf(")")>0){
                startShow = false;
            }

            if(startShow){
                // totalSize
                if(info.indexOf("totalSize")>0){
                    String totalSize = Utils.formetFileSize(Long.parseLong(info.split(",")[2].trim()));
                    System.out.println(totalSize);
                    out.write(totalSize + "\n");
                    hashtotalSize = true;
                }
            }

            if(info.startsWith("# col_name")){
                startShow = true;
            }
        }

        // 如果没有查询到表大小 默认赋为 0
        if(!hashtotalSize){
            System.out.println("0");
            out.write(Utils.formetFileSize(0L) + "\n");
        }
        out.flush();

    }


}
