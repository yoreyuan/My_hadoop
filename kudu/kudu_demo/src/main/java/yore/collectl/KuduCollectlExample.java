package yore.collectl;

import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.Type;
import org.apache.kudu.client.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 程序在 TCP 套接字上侦听与 collectl 有线歇息相对的时间序列数据。
 *
 * <pre>
 *   启动示例服务
 *   java -jar target / kudu-collectl-example-1.0-SNAPSHOT.jar
 *
 *   如果没有 collectl 工具的可以下载安装
 *    wget https://sourceforge.net/projects/collectl/files/collectl/collectl-4.3.1/collectl-4.3.1.src.tar.gz
 *    collectl-4.3.1/INSTALL
 *      collectl还可用于
 *          ① 监测cpu使用率
 *              collectl -sc
 *              collectl -sC
 *           ② 内存监测
 *              collectl -sm
 *              collectl -sM
 *           ③ 查看磁盘使用情况
 *              collectl -sd
 *              collectl -sD
 *              collectl -sd --verbose
 *           ④ 同时报告多系统情况
 *              collectl -scmd
 *           ⑤ 显示统计时间
 *              collectl -scmd -oT
 *
 *   开始收集数据。(ip为 KuduCollectlExample代码运行所在的服务器 ip )
 *   collectl --export=graphite,127.0.0.1,p=/
 * </pre>
 *
 * Created by yore on 2019/4/28 16:19
 */
public class KuduCollectlExample {

    private static final int GRAPHITE_PORT = 2003;
    private static final String TABLE_NAME = "collectl_metrics";
    private static final String ID_TABLE_NAME = "collectl_metric_ids";

    private static final String KUDU_MASTER = "cdh3:7051";

    private KuduClient client;
    private KuduTable table;
    private KuduTable idTable;
    private Set<String> existingMetrics = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    public KuduCollectlExample() {
        this.client = new KuduClient.KuduClientBuilder(KUDU_MASTER).build();
    }

    public void run() throws Exception{
        // 如果表不存在则创建
        createTableIfNecessary();
        createIdTableIfNecessary();
        this.table = client.openTable(TABLE_NAME);
        this.idTable = client.openTable(ID_TABLE_NAME);

        try( ServerSocket listener = new ServerSocket(GRAPHITE_PORT) ){
            while (true){
                Socket s = listener.accept();
                new HandlerThread(s).start();
            }
        }
    }

    private void createTableIfNecessary() throws Exception{
        if(client.tableExists(TABLE_NAME)){
            return;
        }

        List<ColumnSchema> cols = new ArrayList<>();
        cols.add(
                new ColumnSchema.ColumnSchemaBuilder("host", Type.STRING).key(true).encoding(
                        ColumnSchema.Encoding.DICT_ENCODING
                ).build()
        );
        cols.add(
                new ColumnSchema.ColumnSchemaBuilder("metric", Type.STRING).key(true).encoding(
                        ColumnSchema.Encoding.DICT_ENCODING
                ).build()
        );
        cols.add(
                new ColumnSchema.ColumnSchemaBuilder("timestamp", Type.INT32).key(true).encoding(
                        ColumnSchema.Encoding.BIT_SHUFFLE
                ).build()
        );
        cols.add(
                new ColumnSchema.ColumnSchemaBuilder("value", Type.DOUBLE).encoding(
                        ColumnSchema.Encoding.BIT_SHUFFLE
                ).build()
        );

        // Need to set this up since we're not pre-partitioning.
        List<String> rangeKeys = new ArrayList<>();
        rangeKeys.add("host");
        rangeKeys.add("metric");
        rangeKeys.add("timestamp");

        client.createTable(TABLE_NAME, new Schema(cols),
                new CreateTableOptions().setRangePartitionColumns(rangeKeys));

    }

    private void createIdTableIfNecessary() throws Exception{
        if(client.tableExists(ID_TABLE_NAME)){
            return;
        }

        List<ColumnSchema> cols = new ArrayList<>();
        cols.add(
                new ColumnSchema.ColumnSchemaBuilder("host", Type.STRING).key(true).build()
        );
        cols.add(
                new ColumnSchema.ColumnSchemaBuilder("metric", Type.STRING).key(true).build()
        );

        // Need to set this up since we're not pre-partitioning.
        List<String> rangeKeys = new ArrayList<>();
        rangeKeys.add("host");
        rangeKeys.add("metric");

        client.createTable(ID_TABLE_NAME, new Schema(cols),
                new CreateTableOptions().setRangePartitionColumns(rangeKeys));

    }


    private class HandlerThread extends Thread {
        private Socket socket;
        private KuduSession session;

        public HandlerThread(Socket s) {
            this.socket = s;
            this.session = client.newSession();
            // TODO: AUTO_FLUSH_BACKGROUND 对于这种用例会更好, 但是看起来它缓冲的数据太长, 只能根据大小进行刷新。也许我们应该支持基于时间的缓冲?
            session.setFlushMode(SessionConfiguration.FlushMode.MANUAL_FLUSH);

            // 增加我们可以缓冲的变动数量。
            session.setMutationBufferSpace(10000);
        }

        @Override
        public void run() {
            try {
                doRun();
            } catch (Exception e) {
                System.err.println("exception handling connection from " + socket);
                e.printStackTrace();
            }
        }

        private void doRun() throws Exception {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            socket = null;

            // 从 collectd 读取行. 每行数据如下:（ip/.host value timestamp）
            // hostname.example.com/.cpuload.avg1 2.27 1435788059
            String input;
            while ((input = br.readLine()) != null) {
                System.out.println("# \t" + input);
                String[] fields = input.split(" ");
                if (fields.length != 3) {
                    throw new Exception("Invalid input: " + input);
                }
                String[] hostAndMetric = fields[0].split("/.");
                if (hostAndMetric.length != 2) {
                    System.err.println("bad line: " + input);
                    throw new Exception("expected /. delimiter between host and metric name. " +
                            "Did you run collectl with --export=collectl,<hostname>,p=/ ?");
                }
                String host = hostAndMetric[0];
                String metric = hostAndMetric[1];
                insertIdIfNecessary(host, metric);
                double val = Double.parseDouble(fields[1]);
                int ts = Integer.parseInt(fields[2]);

                Insert insert = table.newInsert();
                insert.getRow().addString("host", hostAndMetric[0]);
                insert.getRow().addString("metric", hostAndMetric[1]);
                insert.getRow().addInt("timestamp", ts);
                insert.getRow().addDouble("value", val);
                session.apply(insert);

                // 如果要读取更多数据，不要flush  -- 更好的累计更大的批次。
                if (!br.ready()) {
                    List<OperationResponse> responses = session.flush();
                    for (OperationResponse r : responses) {
                        if (r.hasRowError()) {
                            RowError e = r.getRowError();
                            // TODO: 客户端应该为不同的行错误提供枚举，而不是字符串比较！
                            // getStatus()已过期，此处使用getErrorStatus()替代
                            if ("ALREADY_PRESENT".equals(e.getErrorStatus())) {
                                continue;
                            }
                            System.err.println("Error inserting " + e.getOperation().toString()
                                    + ": " + e.toString());
                        }
                    }
                }
            }

        }

        private void insertIdIfNecessary(String host, String metric) throws Exception {
            String id = host + "/" + metric;
            if (existingMetrics.contains(id)) {
                return;
            }
            Insert ins = idTable.newInsert();
            ins.getRow().addString("host", host);
            ins.getRow().addString("metric", metric);
            session.apply(ins);
            session.flush();
            // TODO: error 处理 !
            //System.err.println("registered new metric " + id);
            existingMetrics.add(id);
        }
    }


    public static void main(String[] args) throws Exception {
        new KuduCollectlExample().run();
    }


}
