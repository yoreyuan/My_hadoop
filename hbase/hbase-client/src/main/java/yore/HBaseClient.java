package yore;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yore on 2018/9/12 16:23
 */
public class HBaseClient {

    public static final String TABLE_NAME = "demo";
    static List<String> rowkeyList = new ArrayList<String>(){{
        add("20");
        add("100");
    }};
    static Configuration conf = new Configuration();
    Connection conn = null;
    Table table = null;


    static {
        conf.set("hbase.zookeeper.quorum","cdh6");
        conf.set("hbase.zookeeper.property.clientPort","2181");
        conf.set("zookeeper.znode.parent","/hbase");
        conf = HBaseConfiguration.create(conf);
    }

    @Before
    public void init() throws IOException {
        //连接hbase
        conn = ConnectionFactory.createConnection(conf);
        Admin admin = conn.getAdmin();
        //获取HBase表
        table = conn.getTable(TableName.valueOf(TABLE_NAME));
    }

    @After
    public void close() throws IOException {
        conn.close();
    }



    /**
     * 根据给定rowkey查询一行的值
     */
    @Test
    public void getByRowkey(){
        String rowkey = rowkeyList.get(0);
        try {
            Get get = new Get(Bytes.toBytes(rowkey));
            Result result = table.get(get);
            for (Cell kv : result.rawCells()) {
                String value = Bytes.toString(CellUtil.cloneValue(kv));
                String qualifier = Bytes.toString(CellUtil.cloneQualifier(kv));
                System.out.println(qualifier +" = " + value );
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 根据给定rowkey集合查询每一行数据
     */
    @Test
    public void getByRowkeys(){
        try {
            List<Get> getList = new ArrayList();
            for(String rk : rowkeyList){
                Get get = new Get(Bytes.toBytes(rk));
                getList.add(get);
            }
            Result[] results = table.get(getList);
            for (Result result : results){//对返回的结果集进行操作
                for (Cell kv : result.rawCells()) {
                    String value = Bytes.toString(CellUtil.cloneValue(kv));
                    String qualifier = Bytes.toString(CellUtil.cloneQualifier(kv));
                    System.out.println(qualifier +" = " + value );
                }
                System.out.println("----------");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    /**
     * 扫描全表数据
     *
     * @throws IOException
     */
    @Test
    public void scanData() throws IOException {
        Scan scan = new Scan();
        ResultScanner result = table.getScanner(scan);
        for(Result row : result){
            String rowkey = Bytes.toString(row.getRow());
            System.out.println(rowkey + "\t" + row);
        }
    }

    /**
     * 通过RowKey后缀查询数据
     * <pre>
     *     CompareFilter.CompareOp.EQUAL将在3.x后被废弃
     *
     *     插入测试数据：
     *       put 'demo','a1553523060000','dept:dname','NULL5'
     *       put 'demo','b1553523102000','dept:dname','NULL6'
     *       put 'demo','c1553523119000','dept:dname','NULL7'
     *
     *
     *     ① scan 'demo',{STARTROW=>'a1553523060000',ENDROW=>'c1553523119000'}
     *     ② scan 'demo',{FILTER=>"RowFilter(=,'regexstring:a.*')"}
     *        scan 'demo', {FILTER => org.apache.hadoop.hbase.filter.PrefixFilter.new(org.apache.hadoop.hbase.util.Bytes.toBytes('a'))}
     *     ③ scan 'demo',{FILTER=>"RowFilter(=,'regexstring:.*1553523102000')"}
     *
     * </pre>
     *
     */
    @Test
    public void getBySufflixRowKey() throws IOException {
        //RowFilter filter = new RowFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator(".*1553523102000"));


        Scan scan = new Scan();

        //一、 获取时间戳在 [1553523060000, 1553523119000) 区间内的数据
//        scan.withStartRow(Bytes.toBytes("a1553523060000"), true);
//        scan.withStopRow(Bytes.toBytes("c1553523119000"), false);

        //二、 获取以a为前缀的所有数据
//        PrefixFilter filter = new PrefixFilter(Bytes.toBytes("a"));
//        scan.setFilter(filter);

        //三、 获取时间戳为1553523102000的数据
        RowFilter filter = new RowFilter(CompareOperator.EQUAL, new RegexStringComparator(".*1553523102000"));
        scan.setFilter(filter);


        ResultScanner result = table.getScanner(scan);
        for(Result row : result){
            System.out.println(Bytes.toString(row.getRow()) + "\t" + row);
        }

    }

}
