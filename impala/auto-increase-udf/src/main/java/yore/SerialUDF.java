package yore;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;

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
    private static final String DATA_CENTER_ID = PropertiesUtil.getMyProperties().getProperty("datacenterId", "0");
    private static final String MACHINE_ID = PropertiesUtil.getMyProperties().getProperty("machineId", "1");

    private SnowFlakeGenerator.Factory factory;
    private SnowFlakeGenerator snowFlakeGenerator;


    public SerialUDF(){
        this.factory = new SnowFlakeGenerator.Factory();
        this.snowFlakeGenerator = factory.create(2, 3);
    }


    /**
     * 手动实现 evaluate() 方法
     */
    public long evaluate(long datacenterId){
//        SnowFlakeGenerator.Factory factory = new SnowFlakeGenerator.Factory();
//        SnowFlakeGenerator snowFlakeGenerator = factory.create(datacenterId, 1);
        return snowFlakeGenerator.nextId();
    }

    public long evaluate(String datacenterId){
        return snowFlakeGenerator.nextId();
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

    public static void main(String[] args) {
        SerialUDF s = new SerialUDF();
        for(int i=0;i<100;i++){
            System.out.println(s.evaluate(i));
        }
    }
}
