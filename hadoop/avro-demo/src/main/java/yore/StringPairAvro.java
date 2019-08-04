package yore;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 *
 * <pre>
 *
 *     GenericDatumWriter对象，它可以将GenericRecord字段的值传递给Encoder对象
 *
 *     DatumWriter对象将数据对象翻译成Encoder对象可以理解的类型，然后由后者写入输出类。
 *
 *     定义的Schema如下：
 *
 *      {
 *          "type": "record",
 *          "name": "StringPair",
 *          "doc": "A pair of strings.",
 *          "fields": [
 *              {
 *                   "name": "left",
 *                   "type": "string"
 *              },
 *              {
 *                  "name": "right",
 *                  "type": "string"
 *              }
 *          ]
 *      }
 *
 * </pre>
 * Created by yore on 2019/8/1 20:25
 */
public class StringPairAvro {

    public static void main(String[] args) throws IOException {
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(new File("hadoop/avro-demo/src/main/avro/StringPair.avsc"));

        // 新建一个Avro Record实例
        GenericRecord datum = new GenericData.Record(schema);
        datum.put("left", "L");
        datum.put("right", "R");

        // 将记录序列化到输出流中
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // 它将Java对象转换为内存中的序列化格式
        DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(schema);
        // 由于没有重用先前构建的Encoder，此处我们将null传递给Encoder工厂。
        Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        writer.write(datum, encoder);
        encoder.flush();
        out.close();

        DatumReader<GenericRecord> reader = new GenericDatumReader<GenericRecord>(schema);
        Decoder decoder = DecoderFactory.get().binaryDecoder(out.toByteArray(),null);
        GenericRecord result = reader.read(datum, decoder);
        Assert.assertEquals(result.get("left").toString(), "L");
        Assert.assertEquals(result.get("right").toString(), "R");

    }



}
