package yore;

import com.google.common.primitives.Ints;
import org.apache.commons.lang.SerializationException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Kafka生产者，
 *
 * <pre>
 *    创建一个 Topic
 *      kafka-topics.sh --create --zookeeper cdh1:2181,cdh2:2181,cdh3:2181 --partitions 1 --replication-factor 1 --topic test.weblog
 *
 * </pre>
 *
 *
 * Created by yore on 2019/5/20 10:13
 */
public class TestProducer {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "cdh1:9092,cdh2:9092,cdh3:9092");
        props.put("acks", "1");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
//        props.put("partitioner.class", LongPartitioner.class.getName());
//        props.put("key.serializer", LongEncoder.class.getName());
        props.put("key.serializer", "org.apache.kafka.common.serialization.LongSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<Long, String> producer = new KafkaProducer<>(props);

        String[] names = {"Alice", "Bob", "Cara", "David", "Edward"};
        String[] genders = {"female", "male", "female", "male", "male"};
        String[] websites = {"www.jd.com", "www.google.com", "www.facebook.com", "www.linkin.com", "www.twitter.com"};

        Random random = new Random();
        long count = 0;

        while (true){
            StringBuffer sb = new StringBuffer("{\"user\":{\"name\":\"");
            int num = random.nextInt(5);
            sb.append(names[num]);
            sb.append("\", \"gender\":\"");
            sb.append(genders[num]);
            sb.append("\"}, \"time\":");
            long runtime = new Date().getTime();
            sb.append(runtime);
            sb.append(", \"website\":\"");
            num = random.nextInt(5);
            sb.append(websites[num]);
            sb.append("\"}");
//            System.out.println(sb);

            producer.send(new ProducerRecord<Long, String>("test.weblog", count++,sb.toString()));

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}

class LongPartitioner implements Partitioner{

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        //key不能空   在源码中 key为空的会通过轮询的方式 选择分区
        if(keyBytes == null){
            throw new RuntimeException("key is null");
        }

        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int numPartitions = partitions.size();


        if(key instanceof Long){
            return Ints.checkedCast((Long)value) % numPartitions;
        }
        return 0;
    }

    @Override
    public void close() {
        // noop
    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}

class LongEncoder implements Serializer<Long> {
    private String encoding = "UTF-8";

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, Long data) {
        try{
            if(data == null) {
                return null;
            }else{
                // 否则将String序列化，即转为byte[]即可
                //return data.getBytes(encoding);
                ByteBuffer buf = ByteBuffer.allocate(8);
                buf.putLong(data);
                return buf.array();
            }
        }catch (Exception e){
            throw new SerializationException("Error when serializing string to byte[] due to unsupported encoding " + encoding);
        }
    }

    @Override
    public void close() {
        // noop
    }
}




