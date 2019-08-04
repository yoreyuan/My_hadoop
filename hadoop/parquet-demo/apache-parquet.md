[Apache Parquet](http://parquet.apache.org/)
=========
Apache Parquet是一种柱状存储格式（columnar storage format），可用于Hadoop生态系统中的任何项目，无论选择何种数据处理框架，数据模型或编程语言。
它是一种有效存储嵌套数据的列式存储格式。

Parquet脱胎于Google发表的一篇关于[Dremel的论文](https://ai.google/research/pubs/pub36632)，它通过一种新颖的技术，以扁平的列式存储格式和很小的额外开销来存储嵌套的结构。

# 关于Parquet
## 数据模型
Parquet定义了少数几个院子数据类型

类型 | 描述
:---- | :----
boolean              | 二进制值 
int32                | 32-bit有符号整数 
int64                | 64-bit有符号整数 
int96                | 96-bit有符号整数 
float                | (32-bit) IEEE 754单精度浮点数 
double               | (64-bit) IEEE 754双精度浮点数 
binary               | 8位无符号字节序列 
fixed_len_byte_array | 固定数量的8位无符号字节

## 逻辑数据类型
1. UTF8
    * 由UTF-8字符组成的字符串，可用于注解binary
    ```
    message m {
       required binary a (UTF8); 
    }
    ```
2. ENUM
    * 命名值的集合，客用户注解binary
    ```
    message m {
       required binary a (ENUM); 
    }
    ```

3. DECIMAL
    * 任意精度的有符号小数，可用于注解int32、int64、binary、fixed_len_byte_array
    ```
    message m {
       required int32 a (DECIMAL(5,2)); 
    }
    ```

4. DATE
    * 不带时间的日期值，客用户注解int32。用Unix元年（1970年1月1日）以来的天数标识
    ```
    message m {
       required int32 a (DATE); 
    }
    ```
    
5. LIST
    * 一组有序的值，可用于注解group
    ```
    message m {
       required group a (LIST){
          repeated group list {
             required int32 element;
          }
       }
    }
    ```
    
6. MAP
    * 一组无序的key-value对，可用于注解group 
    ```
    message m {
       required group a (MAP) {
          repeated group key_value {
             required binary key (UTF8);
             optional int32 value;     
          }
       }
    }
    ```

## Parquet文件格式
Partuet文件由一个文件头（Header）、一个或多个紧随其后的文件块（block），以及一个用于结尾的文件尾（footer）构成。文件头中就包含一个称谓PAR1的4字节数字（Magic Number），
它用来识别整个Parquet文件格式。文件的所有元数据都被保存在文件尾中。未见尾中的元数据包括文件格式的版本信息、模式信息、额外的键值对以及所有块的元数据信息。
文件尾的最后两个字节分别是一个4字节字段（其中包含了文件尾中元数据长度的编码）和一个PAR1（与文件头中的相同）。

Parquet文件中的每个文件块负责存储一个行组（row group），行组有列块（column chunk）构成，且一个列块负责存储一列数据。每个列块中的数据以页（Page）为单位存储。

每个page中的数据都来自同一个列，那么极有可能这些值之间的差异并不大，那么使用page作为压缩单位是非常合适的。最简单的编码方式是无格式编码，然而，这种编码方式没有提供任何程度的压缩。
Parquet会使用一些带有压缩效果的编码方式，包括差分编码（保存值与值之间的差）、游程长度编码（将一连串相同的值编码为一个值以及重复次数）、字典编码（创建一个字典、对字典本身进行编码，然后使用代表字典索引的一个整数来标识值）。
大多数情况下，Parquet还会使用其他一些技术，比如位紧缩法（bit packing），它将多个较小的值保存在一个字节中以节省空间。

## Parquet的配置
配置项 | 类型 | 默认值 | 描述
:---- | :---- | :---- | :----
parquet.block.size           | int     | 134217728 (128 MB) | 文件块（行组）的大小，以字节为单位
parquet.page.size            | int     | 1048576 (1 MB)     | 页的大小，以字节为单位
parquet.dictionary.page.size | int     | 1048576 (1 MB)     | 一个页允许的最大的字典，以字节为单位，若字典超过这个尺寸，将退回到无格式编码
parquet.enable.dictionary    | boolean | true               | 是否使用字典编码
parquet.compression          | String  | UNCOMPRESSED       | Parquet文件使用的压缩类型：UNCOMPRESSED、SNAPPY、GZIP 或者 LZO。用于替代`mapreduce.output.fileoutputformat.compress`

## Parquet文件的读/写
在大多数情况下，会使用高级工具来处理Parquet文件的读/写操作，比如Pig、Hive或者Impala。不过，有些时候也需要对其进行低级的顺序访问，

[ParquetDemo.java](src/main/java/yore/ParquetDemo.java)

## Avro、Protocol Buffers 和 Thrift
大多数应用程序倾向于使用Avro、Protocol Buffers或Thrift 这样的框架来定义数据类型，Parq则迎合了这些需求。
因此我们使用的不再是ParquetWriter和ParquetReader，而是AvroParquetWriter、ProtoParquetWriter 或 ThriftParquetWriter及其分别对应的reader类。
这些类负责完成Avro、Protocol Buffers或Thrift模式与Parquet模式之间的转换。

## Parquet 工具
工具下载 [parquet-tools](https://search.maven.org/search?q=a:parquet-tools)
如果环境没有配置相关jar包可能会报 NoClassDefFoundError ，可以按照如下方式将缺失临时加载到本次执行中。
```bash
java -Xbootclasspath/a:\
/Users/yoreyuan/maven/repository/org/apache/hadoop/hadoop-common/2.7.7/hadoop-common-2.7.7.jar:\
/Users/yoreyuan/maven/repository/commons-logging/commons-logging/1.1.3/commons-logging-1.1.3.jar:\
/Users/yoreyuan/maven/repository/com/google/guava/guava/11.0.2/guava-11.0.2.jar:\
/Users/yoreyuan/maven/repository/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar:\
/Users/yoreyuan/maven/repository/commons-lang/commons-lang/2.6/commons-lang-2.6.jar:\
/Users/yoreyuan/maven/repository/org/apache/hadoop/hadoop-auth/2.7.7/hadoop-auth-2.7.7.jar:\
/Users/yoreyuan/maven/repository/org/slf4j/slf4j-api/1.7.5/slf4j-api-1.7.5.jar:\
/Users/yoreyuan/maven/repository/org/apache/hadoop/hadoop-mapreduce-client-core/2.7.7/hadoop-mapreduce-client-core-2.7.7.jar:\
/Users/yoreyuan/maven/repository/log4j/log4j/1.2.17/log4j-1.2.17.jar \
-jar parquet-tools-1.10.1.jar \
meta data.parquet
#schema data.parquet

```



## 资料
* [Protocol Buffers](https://developers.google.com/protocol-buffers/)



