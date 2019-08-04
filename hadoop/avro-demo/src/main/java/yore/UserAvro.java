package yore;

import example.avro.User;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 *
 * Created by yore on 2019/8/1 21:01
 */
public class UserAvro {

    /**
     * Avro的序列化
     * 这里使用User对象为通过Maven生成的example.avro.User
     *
     * @throws IOException
     */
    @Test
    public void testAvroSerializing() throws IOException{
        User user1 = new User();
        user1.setName("Mecury");
        user1.setFavoriteNumber(33);

        User user2 = new User("Yore", 7, "red");

        User user3 = User.newBuilder()
                .setName("Charlie")
                .setFavoriteNumber(null)
                .setFavoriteColor("bule")
                .build();

        DatumWriter<User> userDatumWriter = new SpecificDatumWriter<>(User.class);
        DataFileWriter<User> dataFileWriter = new DataFileWriter<User>(userDatumWriter);
        dataFileWriter.create(user1.getSchema(), new File("users.avro"));
        dataFileWriter.append(user1);
        dataFileWriter.append(user2);
        dataFileWriter.append(user3);
        dataFileWriter.close();

    }

    /**
     * 反序列化。
     * 将上一步序列化的users.avro，进行反序列化。
     * <pre>
     *  输出的结果为：
     *      {"name": "Mecury", "favorite_number": 33, "favorite_color": null}
     *      {"name": "Yore", "favorite_number": 7, "favorite_color": "red"}
     *      {"name": "Charlie", "favorite_number": null, "favorite_color": "bule"}
     *
     * </pre>
     *
     * @throws IOException
     */
    @Test
    public void testAvroDeserializing() throws IOException{
        // Deserialize Users from disk
        DatumReader<User> userDatumReader = new SpecificDatumReader<User>(User.class);
        DataFileReader<User> dataFileReader = new DataFileReader<User>(new File("users.avro"), userDatumReader);
        User user = null;
        while (dataFileReader.hasNext()) {
            // Reuse user object by passing it to next(). This saves us from
            // allocating and garbage collecting many objects for files with
            // many items.
            user = dataFileReader.next(user);
            System.out.println(user);
        }
    }


    /**
     * 无需代码生成-序列化
     *
     * @throws IOException
     */
    @Test
    public void testAvroWithoutCodeGenerationSerializing() throws IOException{
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(new File("src/main/avro/User.avsc"));

        //Using this schema, let's create some users.
        GenericRecord user1 = new GenericData.Record(schema);
        user1.put("name", "Alyssa");
        user1.put("favorite_number", 256);
        // Leave favorite color null

        GenericRecord user2 = new GenericData.Record(schema);
        user2.put("name", "Ben");
        user2.put("favorite_number", 7);
        user2.put("favorite_color", "red");

        boolean isInMemeory = true;
        if(isInMemeory){
            // 序列化到内存中
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(schema);
            Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            writer.write(user1, encoder);
            writer.write(user2, encoder);
            encoder.flush();
            out.close();

            DatumReader<GenericRecord> reader = new SpecificDatumReader<>(schema);
            Decoder decoder = DecoderFactory.get().binaryDecoder(out.toByteArray(),null);
            GenericRecord result = reader.read(user1, decoder);
            System.out.println(result.get("name"));
            result = reader.read(user2, decoder);
            System.out.println(result.get("name"));

        }else{
            // 序列化user1和user2到磁盘
            File file = new File("users2.avro");
            //我们创建一个DatumWriter，它将Java对象转换为内存中的序列化格式。
            DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(schema);
            DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<GenericRecord>(datumWriter);
            dataFileWriter.create(schema, file);
            dataFileWriter.append(user1);
            dataFileWriter.append(user2);
            dataFileWriter.close();
        }





    }


    /**
     * 无需代码生成-反序列化。
     * 将上一步序列化生成的 "users2.avro" 反序列化
     * <pre>
     *     结果如下：
     *      {"name": "Alyssa", "favorite_number": 256, "favorite_color": null}
     *      {"name": "Ben", "favorite_number": 7, "favorite_color": "red"}
     *
     * </pre>
     *
     * @throws IOException
     */
    @Test
    public void testAvroWithoutCodeGenerationDeserializing() throws IOException{
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(new File("src/main/avro/User.avsc"));
        // Deserialize users from disk
        DatumReader<GenericRecord> datumReader = new GenericDatumReader<GenericRecord>(schema);
        DataFileReader<GenericRecord> dataFileReader = new DataFileReader<GenericRecord>(new File("users2.avro"), datumReader);
        GenericRecord user = null;
        while (dataFileReader.hasNext()) {
            // Reuse user object by passing it to next(). This saves us from
            // allocating and garbage collecting many objects for files with
            // many items.
            user = dataFileReader.next(user);
            System.out.println(user);
        }
    }



}
