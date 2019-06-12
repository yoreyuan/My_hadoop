package yore.insert_loadgen;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.kudu.Schema;
import org.apache.kudu.Type;
import org.apache.kudu.client.Insert;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduSession;
import org.apache.kudu.client.KuduTable;
import org.apache.kudu.client.PartialRow;
import org.apache.kudu.client.SessionConfiguration;

/**
 * 随机插入负载生成器。这将使用 `AUTO_BACKGROUND_FLUSH`模式尽可能快地插入到预先存在的表中。
 * 所有字段都是随机的。负载生成器将继续插入，直到它停止或遇到错误。此负载生成器是单线程的。
 *
 * Created by yore on 2019/4/29 10:57
 */
public class InsertLoadgen {

    private static class RandomDataGenerator{
        private final Random rng;
        private final int index;
        private final Type type;

        /**
         * 为具体的字段实例化随机数据生成器。
         * @param index 行模式中的列索引数字
         * @param type type 索引 {@code index} 的数据类型
         */
        public RandomDataGenerator(int index, Type type) {
            this.rng = new Random();
            this.index = index;
            this.type = type;
        }

        /**
         * 对于给定的行类型{@code type}在列索引{@code index} 处添加随机数
         * @param row 要添加字段的行
         */
        void generateColumnData(PartialRow row){
            switch (type) {
                case INT8:
                    row.addByte(index, (byte) rng.nextInt(Byte.MAX_VALUE));
                    return;
                case INT16:
                    row.addShort(index, (short)rng.nextInt(Short.MAX_VALUE));
                    return;
                case INT32:
                    row.addInt(index, rng.nextInt(Integer.MAX_VALUE));
                    return;
                case INT64:
                case UNIXTIME_MICROS:
                    row.addLong(index, rng.nextLong());
                    return;
                case BINARY:
                    byte bytes[] = new byte[16];
                    rng.nextBytes(bytes);
                    row.addBinary(index, bytes);
                    return;
                case STRING:
                    row.addString(index, UUID.randomUUID().toString());
                    return;
                case BOOL:
                    row.addBoolean(index, rng.nextBoolean());
                    return;
                case FLOAT:
                    row.addFloat(index, rng.nextFloat());
                    return;
                case DOUBLE:
                    row.addDouble(index, rng.nextDouble());
                    return;
                case DECIMAL:
                    row.addDecimal(index, new BigDecimal(rng.nextDouble()));
                    return;
                default:
                    throw new UnsupportedOperationException("Unknown type " + type);
            }
        }
    }


    /**
     * master_addresses Kudu 的master地址，
     * table_name是预先存在的表名
     *
     * 例如：
     *      cdh3:7051 impala::kudu_test.loadgen_test
     *
     * @param args master_addresses table_name
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: InsertLoadgen master_addresses table_name");
            System.exit(1);
        }

        String masterAddrs = args[0];
        String tableName = args[1];

        try (KuduClient client = new KuduClient.KuduClientBuilder(masterAddrs).build()) {
            KuduTable table = client.openTable(tableName);
            Schema schema = table.getSchema();
            // list中放置随机数据生成器
            List<RandomDataGenerator> generators = new ArrayList<>(schema.getColumnCount());
            for (int i = 0; i < schema.getColumnCount(); i++) {
                generators.add(new RandomDataGenerator(i, schema.getColumnByIndex(i).getType()));
            }

            KuduSession session = client.newSession();
            session.setFlushMode(SessionConfiguration.FlushMode.AUTO_FLUSH_BACKGROUND);
            for (int insertCount = 0; ; insertCount++) {
                Insert insert = table.newInsert();
                PartialRow row = insert.getRow();
                for (int i = 0; i < schema.getColumnCount(); i++) {
                    generators.get(i).generateColumnData(row);
                }
                session.apply(insert);

                // Check for errors. This is done periodically since inserts are batched.
                if (insertCount % 1000 == 0 && session.countPendingErrors() > 0) {
                    throw new RuntimeException(session.getPendingErrors().getRowErrors()[0].toString());
                }
            }
        }

    }
}
