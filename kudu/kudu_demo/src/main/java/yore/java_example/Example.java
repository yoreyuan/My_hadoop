package yore.java_example;

import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.Type;
import org.apache.kudu.client.*;
import org.apache.kudu.client.KuduPredicate.ComparisonOp;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用同步 Kudu 客户端的简单实例
 *  - 创建一个表
 *  - 插入行
 *  - 修改一个表
 *  - 扫描行
 *  - 删除表格
 *
 *
 *  <pre>
 *    常用sql
 *
 *      (1)、重命名 Impala 映射表
 *          ALTER TABLE 表名1 RENAME TO 新表名1;
 *
 *      (2)、重新命名内部表的基础 Kudu 表
 *          表名2：通过impala创建的kudu表
 *          ALTER TABLE 表名2 SET TBLPROPERTIES('kudu.table_name' = '新表名2');
 *
 *      (3)、将外部表重新映射到不同的 Kudu 表（Kudu表更改了，Impala需要重新映射）
 *          表名3：impala已经存在的表
 *          表名4：Kudu有alter后的表名
 *          ALTER TABLE 表名3
 *          SET TBLPROPERTIES('kudu.table_name' = '表名4')
 *
 *      (4)、更改 Kudu Master 地址
 *          表名5：impala已经存在的需要修改kudu master地址的表
 *          ALTER TABLE 表名5
 *          SET TBLPROPERTIES('kudu.master_addresses' = 'new_kudu_master_ip:7051');
 *
 *      (5)、将内部管理的表更改为外部
 *          ALTER TABLE 表名6 SET TBLPROPERTIES('EXTERNAL' = 'TRUE');
 *
 *  </pre>
 *
 * Created by yore on 2019/4/29 18:08
 */
public class Example {
    private static final Double DEFAULT_DOUBLE = 12.345;
    private static final String KUDU_MASTERS = System.getProperty("kuduMasters", "cdh3:7051");


    /**
     * 创建示例表
     *
     * @auther: yore
     * @param client Kudu Client
     * @param tableName 表名
     * @throws KuduException
     * @date: 2019/4/30 10:18 AM
     */
    public static void createExampleTable(KuduClient client, String tableName) throws KuduException {
        // 设置一个简单的 schema
        List<ColumnSchema> columns = new ArrayList<>();
        columns.add(
                new ColumnSchema.ColumnSchemaBuilder("key", Type.INT32).key(true).build()
        );
        columns.add(
                new ColumnSchema.ColumnSchemaBuilder("value", Type.STRING).nullable(true).build()
        );
        Schema schema = new Schema(columns);

        /**
         *
         * 设置分区模式，通过哈希将行分配到不同的 tablets
         * Kudu还支持按 key 范围进行分区。Hash 和 范围分区可以组合。
         * 更多信息请参阅：http://kudu.apache.org/docs/schema_design.html.
         *
         */
        CreateTableOptions cto = new CreateTableOptions();
        List<String> hashKeys = new ArrayList<>(1);
        hashKeys.add("key");
        // 需要 hash 的列，好 Hash 到的桶数
        cto.addHashPartitions(hashKeys, 8);

        // 创建表
        client.createTable(tableName, schema, cto);
        System.out.println("$ Created table " + tableName);
    }


    /**
     * 插入数据到指定的 Kudu 表
     *
     * @auther: yore
     * @param client  Kudu Client
     * @param tableName 表名
     * @param numRows 行数
     * @date: 2019/4/30 11:38 AM
     */
    public static void insertRows(KuduClient client, String tableName, int numRows) throws KuduException{
        // 打开新创建的表并创建 KuduSession。
        KuduTable table = client.openTable(tableName);
        KuduSession session = client.newSession();

        for(int i=0; i<numRows; i++){
            Insert insert = table.newInsert();
            PartialRow row = insert.getRow();
            row.addInt("key", i);

            // 使 偶数键 的 value 字段 具有 null 值
            if(i % 2 == 0){
                row.setNull("value");
            }else {
                row.addString("value", "value " + i);
            }
            session.apply(insert);
        }

        /**
         * 调用 session.close() 以结束会话并确保刷新行且返回错误信息。
         * 你也可以调用 session.flush() 来执行相同操作而不结束会话。
         * 当刷新在 AUTO_FLUSH_BACKGROUND 模式下（建议用于大多数工作负载的默认模式），如下所示你必须检查挂起的错误，
         * 因为写入操作在后台线程中刷新到 Kudu
         */
        session.close();
        if(session.countPendingErrors() != 0){
            System.out.println("$ errors follow:");
            org.apache.kudu.client.RowErrorsAndOverflowStatus roStatus = session.getPendingErrors();
            org.apache.kudu.client.RowError[] errs = roStatus.getRowErrors();
            int numErrs = Math.min(errs.length, 5);
            System.out.println("there were errors inserting rows to Kudu");
            System.out.println("the first few errors follow:");
            for (int i = 0; i < numErrs; i++) {
                System.out.println(errs[i]);
            }
            if (roStatus.isOverflowed()) {
                System.out.println("error buffer overflowed: some errors were discarded");
            }
            throw new RuntimeException("error inserting rows to Kudu");
        }
        System.out.println("Inserted " + numRows + " rows");
    }


    /**
     * 扫描打印数据并检测结果数据
     *
     * @auther: yore
     * @param client  Kudu Client
     * @param tableName 表名
     * @param numRows 行数
     * @date: 2019/4/30 1:46 PM
     */
    public static void scanTableAndCheckResults(KuduClient client, String tableName, int numRows) throws KuduException{
        KuduTable table = client.openTable(tableName);
        Schema schema = table.getSchema();

        // 使用基于'key' 列的扫描，返回 'value' 和 'added'列
        List<String> projectColumns = new ArrayList<>(2);
        projectColumns.add("key");
        projectColumns.add("value");
        projectColumns.add("added");

        int lowerBound = 0;
        // 列模式，比较操作，要比较的值
        KuduPredicate lowerPred = KuduPredicate.newComparisonPredicate(
                schema.getColumn("key"),
                ComparisonOp.GREATER_EQUAL,
                lowerBound);
        int upperBound = numRows / 2;
        KuduPredicate upperPred = KuduPredicate.newComparisonPredicate(
                schema.getColumn("key"),
                ComparisonOp.LESS,
                upperBound);
        KuduScanner scanner = client.newScannerBuilder(table)
                .setProjectedColumnNames(projectColumns)
                .addPredicate(lowerPred)
                .addPredicate(upperPred)
                .build();

        /**
         * 检查正确的值数并返回 null 值，同时为每行的新列设置默认值。
         * 注意：扫描 hash 分区表不会返回主键顺序的结果。
         */
        int resultCount = 0;
        int nullCount = 0;
        while (scanner.hasMoreRows()) {
            RowResultIterator results = scanner.nextRows();
            while (results.hasNext()) {
                RowResult result = results.next();
//                System.out.println(result.getInt(1) + "\t" + result.getString(2) + "\t" + result.getDouble(3));
                System.out.println(result.getInt("key") + "\t" /*+ result.getString("value") */+ "\t" + result.getDouble("added"));
                if (result.isNull("value")) {
                    nullCount++;
                }
                double added = result.getDouble("added");
                if (added != DEFAULT_DOUBLE) {
                    throw new RuntimeException("expected added=" + DEFAULT_DOUBLE +
                            " but got added= " + added);
                }
                resultCount++;
            }
        }

        int expectedResultCount = upperBound - lowerBound;
        if (resultCount != expectedResultCount) {
            throw new RuntimeException("scan error: expected " + expectedResultCount +
                    " results but got " + resultCount + " results");
        }
        int expectedNullCount = expectedResultCount / 2 + (numRows % 2 == 0 ? 1 : 0);
        if (nullCount != expectedNullCount) {
            throw new RuntimeException("scan error: expected " + expectedNullCount +
                    " rows with value=null but found " + nullCount);
        }
        System.out.println("Scanned some rows and checked the results");

    }


    /**
     * 删除表
     *
     * @auther: yore
     * @param client  Kudu Client
     * @param tableName 表名
     * @date: 2019/4/30 3:42 PM
     */
    public static void deleteTable(KuduClient client, String tableName ){
        try {
            client.deleteTable(tableName);
            System.out.println("$ Deleted the table");
        } catch (KuduException e) {
            e.printStackTrace();
        }finally {
            try {
                client.shutdown();
            } catch (KuduException e) {
                e.printStackTrace();
            }
        }
    }

    public static void shutdownClient(KuduClient client ){
        try {
            client.shutdown();
        } catch (KuduException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        System.out.println("-----------------------------------------------");
        System.out.println("Will try to connect to Kudu master(s) at " + KUDU_MASTERS);
        System.out.println("Run with -DkuduMasters=master-0:port,master-1:port,... to override.");
        System.out.println("-----------------------------------------------");

        KuduClient client = new KuduClient.KuduClientBuilder(KUDU_MASTERS).build();

        try {
            // create example table
            // 注意： 在kudu 1.7.0(CDH)版本，如果和 impala 集成查询数据，表名最好不要出向 横杠 -，否则 impala 映射表时会报 AnalysisException
//            String tableName = "java_example_" + System.currentTimeMillis();
//            createExampleTable(client, tableName);


            // insert table
//            insertRows(client, "java_example_1556593492110", 33);


            /**
             *
             * Alter 表，添加带有默认值的一列。
             * 注意：在修改表之前，表需要重新打开。
             */
//            AlterTableOptions ato = new AlterTableOptions();
//            ato.addColumn("added", Type.DOUBLE, DEFAULT_DOUBLE);
//            client.alterTable("java_example_1556593492110", ato);
//            System.out.println("Altered the table");


            // scan table
            scanTableAndCheckResults(client,"java_example_1556593492110", 33);

            //
//            deleteTable(client, "java_example_1556593492110");

        } catch (KuduException e) {
            e.printStackTrace();
        } finally {
            shutdownClient(client);
        }

    }


}
