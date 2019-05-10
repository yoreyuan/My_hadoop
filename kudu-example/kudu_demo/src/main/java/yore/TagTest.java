package yore;

import org.apache.kudu.ColumnSchema;
import org.apache.kudu.Schema;
import org.apache.kudu.Type;
import org.apache.kudu.client.*;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

/**
 *
 * <pre>
 *    1. 测试批量插入
 *    2. 测试批量读取
 *    3. 测试增加列字段
 *
 *    相关文章：
 *      <a href="https://kudu.apache.org/kudu.pdf">Kudu: Storage for Fast Analytics on Fast Data</a>
 *      <a href="https://www.oreilly.com/ideas/kudu-resolving-transactional-and-analytic-trade-offs-in-hadoop">Kudu: Resolving transactional and analytic trade-offs in Hadoop</a>
 *
 * </pre>
 *
 * Created by yore on 2019/5/9 23:31
 */
public class TagTest {
    private static final String KUDU_MASTERS = System.getProperty("kuduMasters", "cdh3:7051");

    private static KuduClient kuduClient;
    private static KuduSession session;

    public static void main0(String[] args) throws Exception {
        // 设置时区，  map.put("CTT", "Asia/Shanghai");
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.SHORT_IDS.get("CTT")));
        kuduClient = new KuduClient.KuduClientBuilder(KUDU_MASTERS).build();


        /**
         * 创建表
         *   tag_5
         *
         *   tag_50
         *   tag_500
         */
        // 创建 tag_5 表， 5列
//        createTable("tag_5", 5);
//        alterTable("tag_5", 45);
        // 插入数据， 1000条， 每100条插入一次
//        insertTest("tag_5", 10000, 100);
        // 查询数据
//        scanTest("tag_5");

        if(args.length<0){
            System.out.println("--------------------");
            System.out.println("\t 可输入参数如下：");
            System.out.println("\t\t create 表名 列数");
            System.out.println("\t\t alter 表名 增加的列数");
            System.out.println("\t\t insert 表名 插入数据条数 batch大小");
            System.out.println("\t\t scan 表名 ");
            System.exit(1);
        }

        /**
         * java -cp user-tag-test-1.0-SNAPSHOT-jar-with-dependencies.jar yore.TagTest
         * ALTER TABLE tag_5 SET TBLPROPERTIES('kudu.table_name' = 'tag_5');
         *
         * create tag_5 5
         * insert tag_5 10000 100
         * scan tag_5
         *
         * # alter tag_5 45
         * insert tag_5 100000 100
         * scan tag_5
         *
         * # alter tag_5 450
         * insert tag_5 1000000 100
         * scan tag_5
         *
         *
         *
         * create tag_50 50
         * insert tag_50 10000 100
         * scan tag_50
         *
         * insert tag_50 100000 100
         * scan tag_50
         *
         * insert tag_50 1000000 100
         * scan tag_50
         *
         *
         *
         *
         *
         * create tag_500 500
         * insert tag_500 10000 100
         * scan tag_500
         *
         * insert tag_500 100000 100
         * scan tag_500
         *
         * insert tag_500 1000000 100
         * scan tag_500
         *
         *
         */
        try {
            switch (args[0]){
                case "create" : createTable(args[1], Integer.parseInt(args[2])); break;
                case "alter" : alterTable(args[1], Integer.parseInt(args[2])); break;
                case "insert" : insertTest(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3])); break;
                case "scan" : scanTest(args[1]); break;
                default:
                    System.err.println("参数不对");
            }
        }catch (Exception e){
            System.out.println("\t 可输入参数如下：");
            System.out.println("\t\t create 表名 列数");
            System.out.println("\t\t alter 表名 增加的列数");
            System.out.println("\t\t insert 表名 插入数据条数 batch大小");
            System.out.println("\t\t scan 表名 ");
            System.exit(1);
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {
        // 设置时区，  map.put("CTT", "Asia/Shanghai");
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.SHORT_IDS.get("CTT")));
        kuduClient = new KuduClient.KuduClientBuilder(KUDU_MASTERS).build();

//        alterTable("tag_5", 200);
        createTable("tag", 301);
    }


    /**
     * 创建 kudu 表。
     * 分区方式以 主键的 主键的hash为主，
     * 查询优化部分可以根据具体业务 hash 和 range 分区结合的方式。
     *
     * @auther: yore
     * @param tableName 表名
     * @param columnsNum 以5位基。 5、50、500
     * @date: 2019/5/9 11:56 PM
     */
    public static void createTable(String tableName, int columnsNum) throws Exception{
        List<ColumnSchema> columns = new ArrayList<>();
        CreateTableOptions cto = new CreateTableOptions();
        /**
         * 创建表时，必须为表提供分区模式
         * 可以使用 Impala 的 PARTITION BY 关键字对表进行分区，该关键字支持 RANGE 或 HASH分发。
         * Kudu 分区有两种：
         *  hash
         *  range
         */
        List<String> hashKeys = new ArrayList<>(1);

        for(int i=0; i<columnsNum; i+=5){
            if(i==0){
                // 主键，默认为 _id, 自增
                columns.add(new ColumnSchema.ColumnSchemaBuilder("_id", Type.INT32).key(true).build());
            }else{
                columns.add(new ColumnSchema.ColumnSchemaBuilder("id_"+i/5, Type.INT32).build());
            }
            columns.add(new ColumnSchema.ColumnSchemaBuilder("tag_name_"+i/5, Type.STRING).nullable(true).build());
            columns.add(new ColumnSchema.ColumnSchemaBuilder("tagid_"+i/5, Type.STRING).nullable(true).build());
            columns.add(new ColumnSchema.ColumnSchemaBuilder("uid_"+i/5, Type.DOUBLE).nullable(true).build());
            columns.add(new ColumnSchema.ColumnSchemaBuilder("weight_"+i/5, Type.INT8).defaultValue((byte)0).build());
        }
        Schema schema = new Schema(columns);
        hashKeys.add("_id");
        // 需要 hash 的列，好 Hash 到的桶数
        cto.addHashPartitions(hashKeys, 8);

        // 创建表
        kuduClient.createTable(tableName, schema, cto);
        System.out.println("$ Created table " + tableName);

    }


    /**
     * 修改表：增加列
     *
     * @auther: yore
     * @param tableName 表名
     * @param increColsNum 增加的列
     * @date: 2019/5/10 9:34 AM
     */
    public static void alterTable(String tableName, int increColsNum) throws Exception{
        System.out.println(String.format("----------------- alter: %s ----------------------", tableName));
        System.out.println("\t start: " + System.currentTimeMillis());
        KuduTable table = kuduClient.openTable(tableName);
        Schema schema = table.getSchema();
        int columnCount = schema.getColumnCount();
        System.out.println(columnCount);

        AlterTableOptions ato = new AlterTableOptions();

        Long start = System.currentTimeMillis();
        for(int i=columnCount; i< columnCount + increColsNum; i+=5){
            ato.addColumn("id_"+i/5, Type.INT32, 0);
            ato.addColumn("tag_name_"+i/5, Type.STRING, "-");
            ato.addColumn("tagid_"+i/5, Type.STRING, "-");
            ato.addColumn("uid_"+i/5, Type.DOUBLE, 0.0);
            ato.addColumn("weight_"+i/5, Type.INT8, (byte)0);
        }
        kuduClient.alterTable(tableName, ato);

        Long end = System.currentTimeMillis();
        System.out.println("\t increase column("+increColsNum+") spend (ms): " + (end-start) );
    }


    /**
     * 批量插入
     *
     * @auther: yore
     * @param tableName 表名
     * @param batchSize 批量大小
     * @date: 2019/5/9 11:55 PM
     */
    public static void insertTest(String tableName, int rowNum, int batchSize) throws Exception{
        System.out.println(String.format("----------------- insert: %s ----------------------", tableName));
        System.out.println("\t start: " + System.currentTimeMillis());
        KuduTable table = kuduClient.openTable(tableName);
        Schema schema = table.getSchema();

//        Upsert upsert = table.newUpsert();
//        PartialRow upsertRow = upsert.getRow();
//        upsertRow.addString("claimno", "b");

        session = kuduClient.newSession();
        /**
         * ② AUTO_FLUSH_BACKGROUND
         * ① AUTO_FLUSH_SYNC（默认）
         * ③ MANUAL_FLUSH
         */
        session.setFlushMode(SessionConfiguration.FlushMode.MANUAL_FLUSH);
//        session.setTimeoutMillis(60000);
//        session.setMutationBufferSpace(10000);


//        List<PartialRow>

        Long start = System.currentTimeMillis();
        for(int i=0; i<rowNum; i++){
            Insert insert = table.newInsert();
            PartialRow row = insert.getRow();

            // 插入列
            // _id,tag_name_0,tagid_0,uid_0,weight_0,    id_1,tag_name_1,tagid_1,uid_1,weight_1,
            // int,string,string,double,byte
            for (int j = 0; j < schema.getColumnCount(); j+=5) {
//                schema.getColumnByIndex(j).getName();
//            generators.get(i).generateColumnData(row);
                DM_tag dm_tag = getDM_tag();
//                System.out.println(dm_tag);
                row.addInt(j, i);
                row.addString(j+1, dm_tag.getTag_name());
                row.addString(j+2, dm_tag.getTagid());
                row.addDouble(j+3, dm_tag.getUid());
                row.addByte(j+4, dm_tag.getWeight());
            }

            session.apply(insert);
            if(i%batchSize == 0){
                session.flush();
            }
        }
        session.close();
        Long end = System.currentTimeMillis();
        System.out.println("\t insert spend (ms): " + (end-start) );

    }


    /**
     * 扫描数据
     *
     * @auther: yore
     * @param tableName 表名
     * @date: 2019/5/10 9:10 AM
     */
    public static void scanTest(String tableName)  throws KuduException{
        System.out.println(String.format("----------------- scan: %s ----------------------", tableName));
        System.out.println("\t start: " + System.currentTimeMillis());

        KuduTable table = kuduClient.openTable(tableName);
        Schema schema = table.getSchema();

        int columnCount = schema.getColumnCount();
        List<String> projectColumns = new ArrayList<>(columnCount);
        for (int i = 0; i < columnCount; i++) {
//            System.out.println(schema.getColumnByIndex(i).getName() + "\t" + schema.getColumnByIndex(i).getType());
            projectColumns.add(schema.getColumnByIndex(i).getName());
        }

        KuduScanner scanner = kuduClient.newScannerBuilder(table)
                .setProjectedColumnNames(projectColumns)
//                .addPredicate(lowerPred)
//                .addPredicate(upperPred)
                .build();

        Long start = System.currentTimeMillis();
        int count = 0;
        while (scanner.hasMoreRows()) {
            // 迭代每行
            RowResultIterator results = scanner.nextRows();
//            System.out.println(count + " | ");
            while (results.hasNext()) {
                // 获取每列
                RowResult result = results.next();
                StringBuffer line = new StringBuffer();
                for(int i=0; i<columnCount; i++){
//                        System.out.println(result.getColumnType(i));
                    try {
                        switch (result.getColumnType(i)){
                            case INT8:
                                line.append(result.getByte(i));
                                break;
                            case INT16:
                                line.append(result.getShort(i));
                                break;
                            case INT32:
                                line.append(result.getInt(i));
                                break;
                            case INT64:
                            case UNIXTIME_MICROS:
                                line.append(result.getLong(i));
                                break;
                            case STRING:
                            line.append(result.getString(i));
                                break;
                            case BOOL:
                                line.append(result.getBoolean(i));
                                break;
                            case FLOAT:
                                line.append(result.getFloat(i));
                                break;
                            case DOUBLE:
                            line.append(result.getDouble(i));
                                break;
                            case DECIMAL:
                                line.append(result.getDecimal(i));
                                break;
                            default:
                                line.append("Unknown type: row=" + i);
                        }


                    }catch (Exception e1){
                        line.append("null");
                    }finally {
                        line.append("\t");
                    }
                }
                if(count%10000 == 0){
                    System.out.println(line.toString());
                }
                count++;
            }
        }
        Long end = System.currentTimeMillis();
        System.out.println(String.format("----------------- scan end: %s ----------------------", tableName));
        System.out.println("- count: " + count);
        System.out.println("- scan spend (ms): " + (end-start) );
        System.out.println("- average (row/ms)： " + ((double)count/(end-start)) );

    }





    /**
     * 测试数据
     *
     * @auther: yore
     * @date: 2019/5/09 11:03 PM
     */
    static Random random = new Random();
    private static DM_tag getDM_tag(){
        return RandomDataGenerator.get(random.nextInt(RandomDataGenerator.size()));
    }
    private static final List<DM_tag> RandomDataGenerator = new ArrayList<DM_tag>(){
        {
            add(new DM_tag("发展期", "_122_346", Math.random(), (byte)0));
            add(new DM_tag("超高盈利", "_207_408", Math.random(), (byte)0));
            add(new DM_tag("空仓偏好", "_37_398", Math.random(), (byte)0));
            add(new DM_tag("钻石客户", "_37_398", Math.random(), (byte)0));
            add(new DM_tag("周转率低", "_60_321", Math.random(), (byte)0));
            add(new DM_tag("个人投资者", "_21_181", Math.random(), (byte)0));
            add(new DM_tag("未进行操作", "_206_413", Math.random(), (byte)0));
            add(new DM_tag("持股5~10股", "_47_1076", Math.random(), (byte)0));
            add(new DM_tag("购买过金算盘", "_240_435", Math.random(), (byte)0));
            add(new DM_tag("选股能力很差", "_208_367", Math.random(), (byte)0));
            add(new DM_tag("择时能力很差", "_209_360", Math.random(), (byte)0));
            add(new DM_tag("中等资产客户", "_160_249", Math.random(), (byte)0));
            add(new DM_tag("可购买浙商汇银", "_241_437", Math.random(), (byte)0));
            add(new DM_tag("可开通B股交易", "_241_1101", Math.random(), (byte)0));
            add(new DM_tag("已开通沪港通", "_240_431", Math.random(), (byte)0));
            add(new DM_tag("已开通期权交易", "_240_429", Math.random(), (byte)0));
            add(new DM_tag("已开通融资融券", "_37_398", Math.random(), (byte)0));
            add(new DM_tag("偏好权利方投资", "_182_318", Math.random(), (byte)0));
            add(new DM_tag("已开通基金定投", "_240_427", Math.random(), (byte)0));
            add(new DM_tag("单一持仓集中度较高", "_179_308", Math.random(), (byte)0));
        }
    } ;

    // _id,tag_name_0,tagid_0,uid_0,weight_0,    id_1,tag_name_1,tagid_1,uid_1,weight_1,
    // int,string,string,double,byte
    static class DM_tag implements java.io.Serializable{
        private String tag_name;
        private String tagid;
        private Double uid;
        private Byte weight;

        public DM_tag(String tag_name, String tagid, Double uid, Byte weight) {
            this.tag_name = tag_name;
            this.tagid = tagid;
            this.uid = uid;
            this.weight = weight;
        }

        public DM_tag() {
        }


        public String getTag_name() {
            return tag_name;
        }

        public void setTag_name(String tag_name) {
            this.tag_name = tag_name;
        }

        public String getTagid() {
            return tagid;
        }

        public void setTagid(String tagid) {
            this.tagid = tagid;
        }

        public Double getUid() {
            return uid;
        }

        public void setUid(Double uid) {
            this.uid = uid;
        }

        public Byte getWeight() {
            return weight;
        }

        public void setWeight(Byte weight) {
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "DM_tag{" +
                    ", tag_name='" + tag_name + '\'' +
                    ", tagid='" + tagid + '\'' +
                    ", uid=" + uid +
                    ", weight=" + weight +
                    '}';
        }
    }

}
