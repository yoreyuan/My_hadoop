package yore;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.avro.AvroReadSupport;
import org.apache.parquet.column.ParquetProperties;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.GroupFactory;
import org.apache.parquet.example.data.simple.SimpleGroupFactory;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.example.GroupReadSupport;
import org.apache.parquet.hadoop.example.GroupWriteSupport;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.MessageTypeParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by yore on 2019/8/1 19:53
 */
public class ParquetDemo {

    /**
     * Parquet写文件
     *
     * @throws Exception
     */
    @Test
    public void parquetWriter() throws Exception{
        // 定义Parquet模式
        MessageType schema = MessageTypeParser.parseMessageType(
                "message Pair {\n" +
                        "  required binary left (UTF8);\n" +
                        "  required binary right (UTF8);\n" +
                        "}"
        );

        GroupFactory groupFactory = new SimpleGroupFactory(schema);
        //一个message表示为一个Group实例
        Group group = groupFactory.newGroup()
                .append("left", "L")
                .append("right", "R");

        Configuration conf = new Configuration();
        //message类型转换为Parquet类型
        GroupWriteSupport writeSupport = new GroupWriteSupport();
        //调用GroupWriteSupport静态方法setSchema可以将Parquet模式设置到Configuration对象上。后面再把conf传递给ParquetWriter
        GroupWriteSupport.setSchema(schema, conf);
        Path path = new Path("hadoop/parquet-demo/data.parquet");
        ParquetWriter<Group> writer = new ParquetWriter<Group>(
                path,
                writeSupport,
                ParquetWriter.DEFAULT_COMPRESSION_CODEC_NAME,
                ParquetWriter.DEFAULT_BLOCK_SIZE,
                ParquetWriter.DEFAULT_PAGE_SIZE,
                ParquetWriter.DEFAULT_PAGE_SIZE, /* dictionary page size */
                ParquetWriter.DEFAULT_IS_DICTIONARY_ENABLED,
                ParquetWriter.DEFAULT_IS_VALIDATING_ENABLED,
                ParquetProperties.WriterVersion.PARQUET_1_0,
                conf);
        writer.write(group);
        writer.close();
    }


    /**
     * Parquet读文件。
     *  在读操操作时不需要保存Parquet文件模式，因此也就不需要定义模式。
     */
    @Test
    public void parquetReader() throws IOException {
        Path path = new Path("data.parquet");

        GroupReadSupport readSupport = new GroupReadSupport();
        ParquetReader<Group> reader = new ParquetReader<Group>(path, readSupport);

        // read()方法可以读取下一个message，当读取到文件结尾，会返回NULL值
        Group result = reader.read();
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getString("left", 0), "L");
        Assert.assertEquals(result.getString("right", 0), "R");
        Assert.assertNull(reader.read());
    }


    /**
     * 从Avro读取schema并将数据写入Parquet
     *
     * @throws IOException
     */
    @Test
    public void avroParquetWriter() throws IOException {
        Schema.Parser parser = new Schema.Parser();
//        Schema schema = parser.parse(getClass().getResourceAsStream("StringPair.avsc"));
        Schema schema = parser.parse(new File("../avro-demo/src/main/avro/StringPair.avsc"));
        GenericRecord datum = new GenericData.Record(schema);
        datum.put("left", "L");
        datum.put("right", "R");

        //写一个Parquet文件
        Path path = new Path("data.parquet2");
        AvroParquetWriter<GenericRecord> writer = new AvroParquetWriter<GenericRecord>(path, schema);
        writer.write(datum);
        writer.close();
    }


    /**
     * 使用AvroParquetReader读取Parquet文件
     *
     * @throws IOException
     */
    @Test
    public void avroParquetReader() throws IOException {
        Path path = new Path("data.parquet");
        AvroParquetReader<GenericRecord> reader = new AvroParquetReader<GenericRecord>(path);
        GenericRecord result = reader.read();

        Assert.assertNotNull(result);
        Assert.assertEquals(result.get("left").toString(), "L");
        Assert.assertEquals(result.get("right").toString(), "R");
        Assert.assertNull(reader.read());
    }


    /**
     * 利用Parquet将Avro模式重新映射，读取旧的parquet格式数据。
     * 用来过滤想要从Parquet中获取的列，可以减少数据的扫描。
     *
     * @throws IOException
     */
    @Test
    public void avroProjectionSchemaParquet() throws IOException {
        Schema.Parser parser = new Schema.Parser();
        Schema projectionSchema = parser.parse(getClass().getResourceAsStream("ProjectedStringPair.avsc"));
        Configuration conf = new Configuration();
        //利用AvroReadSupport静态方法setRequestedProjection将该投影模式配置到conf中
        AvroReadSupport.setRequestedProjection(conf, projectionSchema);

        //将该配置传递给 AvroParquetReader构造方法
        Path path = new Path("data.parquet");
        AvroParquetReader<GenericRecord> reader = new AvroParquetReader<GenericRecord>(conf, path);
        GenericRecord result = reader.read();
        Assert.assertNull(result.get("left"));
        Assert.assertEquals(result.get("right").toString(), "R");
    }



}
