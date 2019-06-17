package yore;

import org.apache.kudu.Schema;
import org.apache.kudu.client.*;
import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by yore on 2019/5/7 17:27
 */
public class ExampleTest {

    private static final String KUDU_MASTERS = System.getProperty("kuduMasters", "cdh3:7051");
    private static String tableName = "b060_prplclaim";

    private KuduClient kuduClient;
    private KuduSession session;

    @Before
    public void init(){
        // 设置时区，这里必须要设置  map.put("CTT", "Asia/Shanghai");
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.SHORT_IDS.get("CTT")));

        kuduClient = new KuduClient.KuduClientBuilder(KUDU_MASTERS).build();
        session = kuduClient.newSession();

        /**
         * ② AUTO_FLUSH_BACKGROUND
         * ① AUTO_FLUSH_SYNC（默认）
         * ③ MANUAL_FLUSH
         */
//        session.setFlushMode(SessionConfiguration.FlushMode.MANUAL_FLUSH);
//        session.setTimeoutMillis(60000);
//        session.setMutationBufferSpace(10000);

       /* Field field = Option.Some.class.getDeclaredField("numbers");
        Class<?> type = field.getType();
        Class<?> componentType = type.getComponentType();
        assertEquals(componentType,Integer.class);*/
    }


    @Test
    public void insertTest() throws Exception{
        KuduTable table = kuduClient.openTable(tableName);
        Upsert upsert = table.newUpsert();
        PartialRow upsertRow = upsert.getRow();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        upsertRow.addString("claimno", "b");
        Long endcasedate = System.currentTimeMillis()*1000;
        System.out.println(endcasedate);    // 1557222019848000  1557294368306000
        upsertRow.addLong("endcasedate", endcasedate);

        session.apply(upsert);
    }


    @Test
    public void scanTest()  throws KuduException{
        KuduTable table = kuduClient.openTable("b060_prplclaim");
        Schema schema = table.getSchema();

        // 使用基于'key' 列的扫描，返回 'value' 和 'added'列
        List<String> projectColumns = new ArrayList<>(2);
        projectColumns.add("claimno");
        projectColumns.add("policyno");
        projectColumns.add("sumclaim");
        projectColumns.add("endcasedate");

        KuduScanner scanner = kuduClient.newScannerBuilder(table)
                .setProjectedColumnNames(projectColumns)
//                .addPredicate(lowerPred)
//                .addPredicate(upperPred)
                .build();
        while (scanner.hasMoreRows()) {
            RowResultIterator results = scanner.nextRows();
            while (results.hasNext()) {
                RowResult result = results.next();
                if(!"b".equals(result.getString(0)))continue;
                try {
                    System.out.print(result.getString(0) + "\t");
//                    System.out.print(result.getString(1) + "\t");
//                    System.out.print(result.getDouble(2) + "\t");
                    System.out.println(result.toStringLongFormat());
                    System.out.println(new Date(result.getLong(3)/1000) + "\t" + result.getLong(3));
                }catch (Exception e){
                    System.out.println(result.getString(0) + " 发生异常");
                }

            }
        }

    }

}
