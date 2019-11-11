package yore;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by yore on 2019/11/8 17:38
 */
@Description(name = "SerialUDF",
        value = "_FUNC_() - from the input date string "+
                "根据Twitter的雪花算法，生成自增不重复的id，返回long类型值",
        extended = "Example:\n"
                + " > SELECT _FUNC_() FROM src;\n"
                + " > SELECT _FUNC_(datacenterId, machineId) FROM src;")
public class SerialUDF extends UDF {
//    private static final String DATA_CENTER_ID = PropertiesUtil.getMyProperties().getProperty("datacenterId", "0");
    private static final String MACHINE_COUNT = PropertiesUtil.getMyProperties().getProperty("machine.count", "1");




    private SnowFlakeGenerator.Factory factory;
    private SnowFlakeGenerator snowFlakeGenerator ;


    public SerialUDF(){
        this.factory = new SnowFlakeGenerator.Factory();
        /**
         * 这里注意分布式环境下这个程序会在多个机器上运行，
         * 为了在分布式环境下生成唯一序列值，create()的第二个参数一定要能唯一标识出运行的那台机器的一个值，这里使用hostName的Hash值的绝对值与集群总数的余值作为标识。
         */
        this.snowFlakeGenerator = factory.create(0, Math.abs(getHostName().hashCode())%Integer.parseInt(MACHINE_COUNT));
    }


    /**
     * 手动实现 evaluate() 方法
     */
    public long evaluate(long datacenterId){
        return snowFlakeGenerator.nextId();
    }

    public long evaluate(String datacenterId){
        return evaluate(datacenterId.hashCode());
    }



//    public long evaluate(long datacenterId){
//        this.snowFlakeGenerator = new SnowFlakeGenerator.Factory().create(2, 3);
//        return snowFlakeGenerator.nextId();
//    }
//
//    public long evaluate(long datacenterId, long machineId){
//        this.snowFlakeGenerator = new SnowFlakeGenerator.Factory().create(datacenterId, machineId);
//        return snowFlakeGenerator.nextId();
//    }

    /**
     * 返回运行程序的该机器的 hostname 字符串值
     *
     * @return String
     */
    public static String getHostName(){
        try {
            InetAddress addr = addr = InetAddress.getLocalHost();
            //String ip=addr.getHostAddress().toString(); //获取本机ip
            String hostName=addr.getHostName().toString(); //获取本机计算机名称
            //System.out.println(ip);
            return hostName;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) throws Exception {
        SerialUDF s = new SerialUDF();
        Set<Long> set = new HashSet<>();
        for(int i=0;i<10000;i++){
            long s2 = s.evaluate(i);
            set.add(s2);
            System.out.println(s2);
        }
        System.out.println(set.size());
    }
}
