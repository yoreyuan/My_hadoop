[Apache Avro](https://avro.apache.org/)
========
Apache Avro（名字来源于20世纪英国的一家飞机制造商）是一种数据序列化系统。项目有Doug Cutting（the creator of Hadoop）创建，旨在解决Hadoop中Writable类型的不足：缺乏可移植性。
拥有一个可被多语言处理的数据格式（C、C++、C#、Java、PHP、Python、Ruby），

# Introduction
Avro提供：
* 丰富的数据结构
* 紧凑、款速的二进制数据格式
* 用于存储持久数据的container文件
* RPC
* 与动态语言的简单集成。不需要代码生成来读取或写入数据文件，也不需要使用或实现RPC协议。代码生成作为可选优化，仅值得为静态类型语言实现。

与其他类似系统相比（例如Thrift、Protocol Buffers等）
* 动态类型：Avro不需要生成代码。数据始终伴随着一种模式，该模式允许在不生成代码、静态数据类型等的情况下完全处理该数据。这有助于构建通用数据处理系统和语言。
* 未标记的数据：当数据在读取时Schema的存在，因此需要数据编码的类型信息要少的多，从而导致序列化大小变小。
* 无手动分配的字段ID：当模式更改时，在处理数据时，旧模式和新模式都始终存在，因此可以使用字段名称以符号方式解析差异

# Getting Started (Java)
一如依赖： [pom.xml](pom.xml)

## 引入Manve依赖
```
<dependencies>
        <dependency>
            <groupId>org.apache.avro</groupId>
            <artifactId>avro</artifactId>
            <version>1.9.0</version>
        </dependency>

    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.avro</groupId>
                <artifactId>avro-maven-plugin</artifactId>
                <version>1.9.0</version>
                <executions>
                    <execution>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>schema</goal>
                        </goals>
                        <configuration>
                            <sourceDirectory>${project.basedir}/src/main/avro/</sourceDirectory>
                            <outputDirectory>${project.basedir}/src/main/java/</outputDirectory>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
```

## 定义Avro Schema
将其放到`${project.basedir}/src/main/avro/`下，文件名为`User.avsc`
```json
{"namespace": "example.avro",
 "type": "record",
 "name": "User",
 "fields": [
     {"name": "name", "type": "string"},
     {"name": "favorite_number",  "type": ["int", "null"]},
     {"name": "favorite_color", "type": ["string", "null"]}
 ]
}
```

## 生成
直接运行Manve的编译命令，就会在`${project.basedir}/src/main/java/`生成以包路径为`example.avro`的`User`类

## 代码
[UserAvro.java](src/main/java/yore/UserAvro.java)

**当读模式和写入时的模式不同时**，需要调用GenericDatumReader的构造函数
```
DatumReader<GenericRecord> reader = new GenericDatumReader<GenericRecord>(schema, newSchema);
```

## Avro工具类
下载 [avro-tools-1.9.0.jar](http://archive.apache.org/dist/avro/avro-1.9.0/java/avro-tools-1.9.0.jar)，
可以通过命令 
```bash
# 将Avro文件中的数据内容转储为JSON并打印到控制台
 java -jar $AVRO_HOME/avro-tools-1.9.0.jar tojson users.avro
```






# [Specification](http://avro.apache.org/docs/current/spec.html) （规范）

## 基本类型
类型 | 描述 | 模式示例
:---- | :---- | :----
null    | 空值                  | "null"
boolean | 二进制值               | "boolean"
int     | 32位带符号整数          | "int"
long    | 64位带符号整数          | "long"
float   | 单精度32位IEEE 754浮点数 |"float"
double  | 双精度64位IEEE 754浮点数 |"double"
bytes   | 8位无符号字节序列        | "bytes"
string  | Unicode字符序列         | "string"


## 复杂类型
Avro支持六种复杂类型：records, enums, arrays, maps, unions 和 fixed.

### Records
一个任意类型的命名字段集合。
* name: 提供record名称的JSON字符串（必填）
* namespace： 一个限定名称的JSON字符串。
* doc: 一个 JSON 字符串，为该模式的用户提供doc（可选）
* aliases: JSON字符串数组，为此Record提供备用名称（可选）
* fields: 一个JSON数组，字符串列表（必填）。每一个字段都是具有以下属性的JSON对象。
    * name: 提供字段名称（必需）的JSON字符串
    * doc: 为用户描述此字段的JSON字符串（可选）
    * type: 一个模式，如上文定义
    * default: 此字段的默认值，用于读取缺少此字段的实例（可选）。
    * order: 自定此字段如何影响此record的排序顺序（可选）。有效值为**ascending**（默认值）、"descending" 或 "ignore".
    * aliases: 字符串的JSON数组，为此字段提供备用名称（可选）。

模式示例：
```json
{
  "type": "record",
  "name": "WeatherRecord",
  "doc": "A weather reading.",
  "fields": [
    {
      "name": "year",
      "type": "int"
    },
    {
      "name": "temperature",
      "type": "int"
    },
    {
      "name": "stationId",
      "type": "string"
    }
  ]
}
```

### Enums
一个命名的值的集合。
* name: 提供枚举名称的JSON字符串（必须）。
* namespace, 一个限定名称的JSON字符串；
* aliases: 自妇产的JSON数组，为此枚举提供备用名称（可选）
* doc: 一个JSON字符串，为该模式的用户提供文档（可选）
* symbols:一个JSON数组，symbols的列表。作为JSON字符串（必需）。枚举中的所有符号必须是唯一的; 禁止重复。每个符号必须与正则表达式`[A-Za-z_][A-Za-z0-9_]*`匹配。
* default: 此枚举的默认值，在解析过程中，当reader遇到writer中未在reader模式中定义符号时使用（可选）。此处提供的值必须是JSON字符串，该字符串是symbols数组的成员。

示例：
```json
{
  "type": "enum",
  "name": "Cutlery",
  "doc": "An eating utensil.",
  "symbols": [
    "KNIFE",
    "FORK",
    "SPOON"
  ]
}
```


### Arrays
一个排过序的对象集合。特定数组中的所有对象必须模式相同

* items: 数组项的模式

示例：
```json
{
  "type": "array",
  "items": "long"
}
```


### Maps
未排过序的键值对。键必须是字符串，值可以是hi任何一种类型，但一个特定map中所有制必须模式相同

* values：map值的模式。

示例：
```json
{
  "type": "map",
  "values": "string"
}
```

### Unions
模式的并集。并集可用JSON数组标识，其中每个元素为一个模式。并集表示的数据必须与其内的某个模式相匹配。

示例：
```json
[
  "null",
  "string",
  {
    "type": "map",
    "values": "string"
  }
]
```

### Fixed
一组固定数量的8位无符号字节

* name: 命名此fixed的字符串（必须）
* namespace, 一个限定名称的字符串；
* aliases: 自妇产的JSON数组，为此提供备用名称
* size: 一个整数，指定每个值的字节数（必需）

示例：
```json
{
  "type": "fixed",
  "name": "Md5Hash",
  "size": 16
}
```

