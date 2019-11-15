ClickHouse
======
[Home](https://clickhouse.yandex/) &nbsp; &nbsp; | &nbsp; &nbsp;
[GitHub](https://github.com/ClickHouse/ClickHouse)


# 1 概述
ClickHouse是一个开源的用于联机分析（OLAP）的面向列的数据库管理系统（DBMS），它能够使用SQL查询实时生成分析数据报告。

ClickHouse，只是让你思考更快
* 在相同的时间内运行更多查询
* 测试更多的预想
* 以更多新方式对数据进行切片和切块
* 从新角度看待你的数据
* 发现新维度

## 1.1 何时使用ClickHouse
用于分析或清洗结构化的且不可变的事件或日志类数据。建议将每个此类流放入具有预先连接（pre-joined）维度的单个宽事实表中。

一些可行的应用程序示例：
* 网络和应用分析
* 广告网络和实时出价
* 电信
* 电子商务和金融
* 信息安全
* 监控和遥测
* 时间序列
* 商业情报
* 线上游戏
* 物联网

## 1.2 何时不使用ClickHouse
* 事务性工作负载（OLTP）
* 高请求率的键值访问
* Blob或文档存储
* 标准化数据


## 1.3 特点
### 1.3.1 快速燃烧（Blazing Fast）
ClickHouse的性能[超过了](https://clickhouse.yandex/benchmark.html)目前市场上可对比的面向列的DBMS。
每秒钟每台服务器每秒处理数亿至十亿多行和数十千兆字节的数据。

ClickHouse会充分利用所有可用的硬件，以尽可能快地处理每个查询。单个查询（解压缩后、仅使用的列）的峰值处理性能超过每秒2 TB。

**ClickHouse的工作速度比传统方法快100-1,000倍**：
与常见的数据管理方法不同，在常规方法中，大量原始格式的原始数据可作为任何给定查询的“数据湖”（data lake），而ClickHouse在大多数情况下可提供即时结果：
处理数据的速度比创建数据的速度更快。单击[下面的链接](https://clickhouse.yandex/benchmark.html)，以查看ClickHouse的Yandex与其他数据库管理系统进行比较的详细基准。
下一节还将提供有关第三方基准的一些链接。


### 1.3.2 线性可扩展（Linearly Scalable）
ClickHouse允许公司在必要时将服务器添加到群集中，而无需花费时间或金钱进行任何其他DBMS修改。该系统已成功为[Yandex.Metrica](https://metrica.yandex.com/)提供服务，
而其主要生产集群中的服务器数量在两年内已从60台增加到394台，这些服务器位于六个地理分布的数据中心内。

ClickHouse可以在垂直和水平方向上很好地缩放。ClickHouse易于调整以在具有数百个节点的群集上，在单个服务器上甚至在小型虚拟机上执行。
当前，每个节点的安装量超过2万亿行，每个节点的存储量为100Tb。

### 1.3.3 硬件效率高(Hardware Efficient)
与具有相同可用 I/O 吞吐量的传统的面向行的系统相比，ClickHouse处理**典型的分析查询要快两到三个数量级**。系统的列式存储格式允许将更多热数据放入RAM中，从而缩短了响应时间。

ClickHouse可以最大程度地减少范围查询的次数，从而提高了使用旋转磁盘驱动器的效率，因为它可以保持连续存储数据的参考位置。

由于ClickHouse的矢量化查询执行涉及相关的处理器指令和运行时代码生成，因此它具有CPU效率。

通过最大限度地减少大多数查询类型的数据传输，ClickHouse使公司无需使用专门针对高性能计算的专用网络即可管理其数据并创建报告。


### 1.3.4 容错(Fault Tolerant)
ClickHouse支持多主机异步复制，并且可以跨多个数据中心进行部署。单个节点或整个数据中心的停机时间不会影响系统的读写可用性。
分布式读取将自动与活动副本保持平衡，以避免增加延迟。服务器停机后，复制的数据将自动或半自动同步。

### 1.3.5 功能丰富(Feature Rich)
ClickHouse具有用户友好的SQL查询方言，具有许多内置分析功能。例如它包括概率数据结构，用于快速和有效存储基数和分位数的计算。有用于工作日期、时间和时区的功能，
以及一些专门的功能，例如寻址URL和IP（IPv4和IPv6）以及更多功能。

ClickHouse中可用的数据组织选项，例如数组、数组联接、元组和嵌套数据结构，对于管理非规范化数据非常有效。

由于系统支持本地联接和分布式联接，因此使用ClickHouse可以联接分布式数据和位于同一地点的数据。它还提供了使用外部词典（从外部源加载的维度表）进行简单语法无缝连接的机会。

ClickHouse支持近似查询处理–您可以根据需要快速获得结果，这在处理TB级和PB级数据时必不可少。

系统的条件汇总函数，总计和极值的计算，使您可以通过一次查询获得结果，而不必运行多个查询。


### 1.3.6 高度可靠(Highly Reliable)
ClickHouse一直在管理PB级数据，这些数据为俄罗斯领先的搜索提供商，欧洲最大的IT公司之一[Yandex](https://www.yandex.com/company/)的大量高负载大众受众服务提供服务。
自2012年以来，ClickHouse一直为公司的网络分析服务，比较电子商务平台，公共电子邮件服务，在线广告平台，商业智能工具和基础架构监视提供强大的数据库管理。

ClickHouse可以配置为位于独立节点上的纯分布式系统，而没有任何单点故障。

软件和硬件故障或配置错误不会导致数据丢失。ClickHouse不会删除“损坏的”数据，而是将其保存或询问您在启动前该怎么做。每次对磁盘或网络进行读取或写入之前，
所有数据均经过校验和。几乎不可能偶然删除数据，因为即使存在人为错误，也有保护措施。

ClickHouse提供了对查询复杂性和资源使用情况的灵活限制，可以通过设置对其进行微调。可以同时为多个高优先级低延迟请求和一些具有后台优先级的长时间运行的查询提供服务。


### 1.3.7 简单方便(Simple and Handy)
ClickHouse简化了所有数据处理。它易于使用：将所有结构化数据提取到系统中，并可立即用于报告。可以随时轻松将用于新属性或维度的新列添加到系统中，而不会降低系统运行速度。

ClickHouse简单易用，开箱即用。除了在数百个节点群集上执行之外，该系统还可以轻松地安装在单个服务器甚至虚拟机上。安装ClickHouse不需要任何开发经验或代码编写技能。


<br/>


# 2 ClickHouse 架构概述
ClickHouse 是一个真正的列式数据库管理系统（DBMS)。在 ClickHouse 中，数据始终是按列存储的，包括矢量（向量或列块）执行的过程。
只要有可能，操作都是基于矢量进行分派的，而不是单个的值，这被称为“矢量化查询执行”，它有利于降低实际的数据处理开销。

通常有两种不同的加速查询处理的方法：**矢量化查询执行**和**运行时代码生成**。在后者中，动态地为每一类查询生成代码，消除了间接分派和动态分派。
这两种方法中，并没有哪一种严格地比另一种好。运行时代码生成可以更好地将多个操作融合在一起，从而充分利用 CPU 执行单元和流水线。
矢量化查询执行不是特别实用，因为它涉及必须写到缓存并读回的临时向量。如果 L2 缓存容纳不下临时数据，那么这将成为一个问题。
但矢量化查询执行更容易利用 CPU 的 SIMD 功能(Single Instruction Multiple Data，单指令多数据流)。关于将两种方法结合起来的更好选择的研究可以查看论文 
[Vectorization vs. Compilation in Query Execution](https://15721.courses.cs.cmu.edu/spring2016/papers/p5-sompolski.pdf)。
ClickHouse 使用了矢量化查询执行，同时初步提供了有限的运行时动态代码生成。

## 2.1 列（Columns）
要表示内存中的列（实际上是列块），需使用`IColumn`接口，该接口提供了用于实现各种关系操作符的辅助方法。几乎所有的操作都是不可变的：这些操作不会更改原始列，但是会创建一个新的修改后的列，
比如`IColumn::filter`方法接受过滤字节掩码，用于`WHERE` 和 `HAVING` 关系操作符中。另外的例子：`IColumn::permute` 方法支持 `ORDER BY` 实现，`IColumn::cut` 方法支持 `LIMIT` 实现等等。
       
不同的 `IColumn` 实现（`ColumnUInt8`、`ColumnString` 等）负责不同的列内存布局(the memory layout of columns)，内存布局通常是一个连续的数组。对于数据类型为整型的列，只是一个连续的数组，
比如 `std::vector`；对于 `String` 列和 `Array` 列则由两个向量组成：其中一个向量连续存储所有的 `String` 或数组元素，另一个存储每一个 `String` 或 `Array` 的起始元素在第一个向量中的偏移。
而 `ColumnConst` 则仅在内存中存储一个值，但是看起来像一个列。

## 2.2 字段（Field）
不过有时候也可能需要处理单个值，表示单个值可以使用 `Field`，`Field` 是 `UInt64`、`Int64`、`Float64`、`String` 和 `Array` 组成的联合。`IColumn` 拥有 `operator[]` 方法来获取
第 n 个值成为一个 `Field`，同时也拥有 `insert` 方法将一个 `Field` 追加到一个列的末尾。这些方法并不高效，因为它们需要处理表示单一值的临时 `Field` 对象，
但是有更高效的方法比如 `insertFrom` 和 `insertRangeFrom` 等。

Field 中并没有足够的关于一个表（table）的特定数据类型的信息。比如，`UInt8`、`UInt16`、`UInt32` 和 `UInt64` 在 `Field` 中均表示为 `UInt64`。

## 2.3 Leaky Abstractions
`IColumn` 具有用于数据的常见关系转换的方法，但这些方法并不能够满足所有需求。比如`ColumnUInt64` 没有用于计算两列和的方法；`ColumnString` 没有用于进行子串搜索的方法。
这些无法计算的例程在 `Icolumn` 之外实现。

列（Columns)上的各种函数可以通过使用 `Icolumn` 的方法来提取 Field 值，或根据特定的 `Icolumn` 实现的数据内存布局的知识，以一种通用但不高效的方式实现。
为此，函数将会转换为特定的 `IColumn` 类型并直接处理内部表示。比如`ColumnUInt64` 具有 `getData` 方法，该方法返回一个指向列的内部数组的引用，
然后一个单独的例程可以直接读写或填充该数组。实际上，“抽象漏洞（leaky abstractions）”允许我们以更高效的方式来实现各种特定的例程。

## 2.4 数据类型（Data Types）
`IDataType` 负责序列化和反序列化：读写二进制或文本形式的列或单个值构成的块，`IDataType` 直接与表的数据类型相对应，比如有 `DataTypeUInt32`、`DataTypeDateTime`、`DataTypeString` 等数据类型。

`IDataType` 与 `IColumn` 之间的关联并不大，不同的数据类型在内存中能够用相同的 `IColumn` 实现来表示，比如`DataTypeUInt32` 和 `DataTypeDateTime` 都是用 `ColumnUInt32` 或 `ColumnConstUInt32` 来表示的。
另外，相同的数据类型也可以用不同的 `IColumn` 实现来表示，比如`DataTypeUInt8` 既可以使用 `ColumnUInt8` 来表示，也可以使用`ColumnConstUInt8` 来表示。

`IDataType` 仅存储元数据，比如`DataTypeUInt8` 不存储任何东西（除了 vptr）。`DataTypeFixedString` 仅存储 N（固定长度字符串的串长度）。

`IDataType` 具有针对各种数据格式的辅助函数，比如如下一些辅助函数：序列化一个值并加上可能的引号、序列化一个值用于 JSON 格式、序列化一个值作为 XML 格式的一部分。
辅助函数与数据格式并没有直接的对应，比如两种不同的数据格式`Pretty` 和 `TabSeparated` 均可以使用 `IDataType` 接口提供的 `serializeTextEscaped` 这一辅助函数。

## 2.5 块（Block）
Block 是表示内存中表的子集（chunk）的容器，是由三元组：`(IColumn, IDataType, column name)` 构成的集合。在查询执行期间，数据是按 Block 进行处理的。如果我们有一个 Block，
那么就有了数据（在 IColumn 对象中），有了数据的类型信息告诉我们如何处理该列，同时也有了列名（来自表的原始列名，或人为指定的用于临时计算结果的名字）。

当我们遍历一个块中的列进行某些函数计算时，会把结果列加入到块中，但不会更改函数参数中的列，因为操作是不可变的。之后，不需要的列可以从块中删除，但不是修改。这对于消除公共子表达式非常方便。

Block 用于处理数据块。注意，对于相同类型的计算，列名和类型对不同的块保持相同，仅列数据不同。最好把块数据（block data）和块头（block header）分离开来，因为小块大小会因复制共享指针和列名而带来很高的临时字符串开销。

## 2.6 块流（Block Streams）
块流用于处理数据。我们可以使用块流从某个地方读取数据、执行数据转换或将数据写到某个地方。`IBlockInputStream` 具有 `read` 方法，其能够在数据可用时获取下一个块。
`IBlockOutputStream` 具有 `write` 方法，其能够将块写到某处。

块流负责：
* 读或写一个表。表仅返回一个流用于读写块。
* 完成数据格式化。比如如果你打算将数据以 `Pretty` 格式输出到终端，你可以创建一个块输出流，将块写入该流中，然后进行格式化。
* 执行数据转换。假设你现在有 `IBlockInputStream` 并且打算创建一个过滤流，那么你可以创建一个 `FilterBlockInputStream` 并用 `IBlockInputStream` 进行初始化。
之后当你从 `FilterBlockInputStream` 中拉取块时，会从你的流中提取一个块对其进行过滤，然后将过滤后的块返回给你。查询执行流水线就是以这种方式表示的。

还有一些更复杂的转换。比如当你从 `AggregatingBlockInputStream` 拉取数据时，会从数据源读取全部数据进行聚合，然后将聚合后的数据流返回给你。
另一个例子：`UnionBlockInputStream` 的构造函数接受多个输入源和多个线程，其能够启动多线程从多个输入源并行读取数据。

块流使用“pull”方法来控制流：当你从第一个流中拉取块时，它会接着从嵌套的流中拉取所需的块，然后整个执行流水线开始工作。”pull“和“push”都不是最好的方案，
因为控制流不是明确的，这限制了各种功能的实现，比如多个查询同步执行（多个流水线合并到一起）。这个限制可以通过并发或直接运行互相等待的线程来解决。如果控制流明确，
那么我们会有更多的可能性：如果我们定位了数据从一个计算单元传递到那些外部的计算单元中其中一个计算单元的逻辑。
阅读[这篇文章](http://journal.stuffwithstuff.com/2013/01/13/iteration-inside-and-out/)来获取更多的想法。

我们需要注意，查询执行管线在每一步都会创建临时数据。我们要尽量使块的大小足够小，从而 CPU 缓存能够容纳下临时数据。在这个假设下与其他计算相比，读写临时数据几乎是没有任何开销的。
我们也可以考虑一种替代方案：将管线中的多个操作融合在一起使管线尽可能短，并删除大量临时数据。这可能是一个优点，但同时也有缺点，
比如拆分管线使得中间数据缓存、获取同时运行的类似查询的中间数据以及相似查询的流水线合并等功能很容易实现。

## 2.7 格式（Formats）
数据格式同块流一起实现，既有仅用于向客户端输出数据的”展示“格式，如 `IBlockOutputStream` 提供的 `Pretty` 格式，也有其它输入输出格式，比如 `TabSeparated` 或 `JSONEachRow`。

此外还有行流（row streams）：`IRowInputStream` 和 `IRowOutputStream`。它们允许你按行 pull/push 数据而不是按块。行流只需要简单地面向行格式实现。
包装器 `BlockInputStreamFromRowInputStream` 和 `BlockOutputStreamFromRowOutputStream` 允许你将面向行的流转换为正常的面向块的流。

## 2.8 I/O
对于面向字节的输入输出，有 `ReadBuffer` 和 `WriteBuffer `这两个抽象类。它们用来替代 C++ 的 `iostream`。不用担心：每个成熟的 C++ 项目都会有充分的理由使用某些东西来代替 `iostream`。

`ReadBuffer` 和 `WriteBuffer` 由一个连续的缓冲区和指向缓冲区中某个位置的一个指针组成。实现中缓冲区可能拥有内存，也可能不拥有内存。
有一个虚方法会使用随后的数据来填充缓冲区（针对 ReadBuffer）或刷新缓冲区（针对 WriteBuffer），该虚方法很少被调用。

`ReadBuffer` 和 `WriteBuffer` 的实现用于处理文件、文件描述符和网络套接字（socket），也用于实现压缩（CompressedWriteBuffer 在写入数据前需要先用一个 WriteBuffer 进行初始化并进行压缩）
和其它用途，`ConcatReadBuffer`、`LimitReadBuffer` 和 `HashingWriteBuffer` 的用途正如其名字所描述的一样。

ReadBuffer 和 WriteBuffer 仅处理字节，为了实现格式化输入和输出（比如以十进制格式写一个数字），`ReadHelpers` 和 `WriteHelpers` 头文件中有一些辅助函数可用。

让我们来看一下，当你把一个结果集以 JSON 格式写到标准输出（stdout）时会发生什么。你已经准备好从 `IBlockInputStream` 获取结果集，
然后创建` WriteBufferFromFileDescriptor(STDOUT_FILENO)` 用于写字节到标准输出，创建 `JSONRowOutputStream` 并用 `WriteBuffer` 初始化，
用于将行以 JSON 格式写到标准输出，你还可以在其上创建 `BlockOutputStreamFromRowOutputStream`将其表示为 `IBlockOutputStream`，
然后调用 `copyData` 将数据从 `IBlockInputStream` 传输到 `IBlockOutputStream`和一切工作。在内部`JSONRowOutputStream` 会写入 JSON 分隔符，
并以指向 `IColumn` 的引用和行数作为参数调用 `IDataType::serializeTextJSON` 函数。随后`IDataType::serializeTextJSON` 将会调用 `WriteHelpers.h `中的一个方法：
比如`writeText` 用于数值类型，`writeJSONString` 用于 `DataTypeString` 。

## 2.9 表（Tables）
表由 `IStorage` 接口表示，该接口的不同实现对应不同的表引擎，比如 `StorageMergeTree`、`StorageMemory` 等。这些类的实例就是表。

`IStorage` 中最重要的方法是 `read` 和 `write`，除此之外还有 `alter`、`rename` 和 `drop` 等方法。`read` 方法接受如下参数：需要从表中读取的列集合和需要执行的 AST 查询，以及所需返回的流的数量。
`read` 方法的返回值是一个或多个 `IBlockInputStream` 对象，以及在查询执行期间在一个表引擎内完成的关于数据处理阶段的信息。

在大多数情况下，read 方法仅负责从表中读取指定的列，而不会进行进一步的数据处理。进一步的数据处理均由查询解释器完成，不由 IStorage 负责。但是也有值得注意的例外：
* AST 查询被传递给 read 方法，表引擎可以使用它来判断是否能够使用索引，从而从表中读取更少的数据。
* 有时候表引擎能够将数据处理到一个特定阶段，比如`StorageDistributed` 可以向远程服务器发送查询，要求它们将来自不同的远程服务器能够合并的数据处理到某个阶段，并返回预处理后的数据，然后查询解释器完成后续的数据处理。

表的 `read` 方法能够返回多个 `IBlockInputStream` 对象以允许并行处理数据。多个块输入流能够从一个表中并行读取。然后你可以通过不同的转换对这些流进行装饰（比如表达式求值或过滤），
转换过程能够独立计算，并在其上创建一个 `UnionBlockInputStream`以并行读取多个流。

另外也有 `TableFunction`，它能够在查询的 `FROM` 字句中返回一个临时的 `IStorage` 以供使用。

要快速了解如何实现自己的表引擎，可以查看一些简单的表引擎，比如 `StorageMemory `或 `StorageTinyLog`。

作为 `read` 方法的结果，`IStorage` 返回 `QueryProcessingStage` （关于 storage 里哪部分查询已经被计算的信息），当前我们仅有非常粗粒度的信息。
Storage 无法告诉我们“对于这个范围的数据，我已经处理完了 `WHERE` 字句里的这部分表达式”。我们需要在这个地方继续努力。

## 2.10 解析器（Parsers）
查询由一个手写的递归下层解析器解析。比如`ParserSelectQuery` 只是针对查询的不同部分递归地调用下层解析器。解析器创建 AST，AST 由节点表示，节点是 IAST 的实例。

由于历史原因，未使用解析器生成器。


## 2.11 解释器（Interpreters）
解释器负责从 AST 创建查询执行管线，既有一些简单的解释器如 `InterpreterExistsQuery` 和 `InterpreterDropQuery`，也有更复杂的解释器如 `InterpreterSelectQuery`。
查询执行管线由块输入或输出流组成。比如`SELECT` 查询的解释结果是从 `FROM` 字句的结果集中读取数据的 `IBlockInputStream`；`INSERT` 查询的结果是写入需要插入的数据的 `IBlockOutputStream`；
`SELECT INSERT` 查询的解释结果是 `IBlockInputStream`，它在第一次读取时返回一个空结果集，同时将数据从 `SELECT` 复制到 `INSERT`。

`InterpreterSelectQuery` 使用 `ExpressionAnalyzer` 和 `ExpressionActions` 机制来进行查询分析和转换。这是大多数基于规则的查询优化完成的地方。
`ExpressionAnalyzer` 非常混乱，应该进行重写：不同的查询转换和优化应该被提取出来并划分成不同的类，从而允许模块化转换或查询。

## 2.12 函数（Functions）
函数既有普通函数，也有聚合函数。对于聚合函数，请看下一节。

普通函数不会改变行数 - 它们的执行看起来就像是独立地处理每一行数据。实际上函数不会作用于一个单独的行上，而是作用在以 `Block` 为单位的数据上，以实现向量查询执行。

还有一些杂项函数，比如 `blockSize`、`rowNumberInBlock`以及 `runningAccumulate`，它们对块进行处理，并且不遵从行的独立性。

ClickHouse 具有强类型，因此隐式类型转换不会发生。如果函数不支持某个特定的类型组合则会抛出异常，但函数可以通过重载以支持许多不同的类型组合，
比如plus 函数（to implement the + operator）支持任意数字类型的组合：`UInt8 + Float32`，`UInt16 + Int8` 等。同时，一些可变参数的函数能够级接收任意数目的参数，比如 concat 函数。

实现函数可能有些不方便，因为函数的实现需要包含所有支持该操作的数据类型和 `IColumn` 类型。比如`plus` 函数能够利用 C++ 模板针对不同的数字类型组合、常量以及非常量的左值和右值进行代码生成。

这是一个实现动态代码生成的好地方，从而能够避免模板代码膨胀。同样运行时代码生成也使得实现融合函数成为可能，比如融合“乘-加”，或者在单层循环迭代中进行多重比较。

由于向量查询执行函数不会“短路”，比如如果你写 `WHERE f(x) AND g(y)`，两边都会进行计算，即使是对于 `f(x)` 为 0 的行（除非 f(x) 是零常量表达式）。
但是如果 `f(x)` 的选择条件很高，并且计算 f(x) 比计算 g(y) 要划算得多，那么最好进行多遍计算：首先计算 f(x)，根据计算结果对列数据进行过滤，然后计算 g(y)，之后只需对较小数量的数据进行过滤。

## 2.13 聚合函数（Aggregate Functions）
聚合函数是状态函数，它们将传入的值激活到某个状态，并允许你从该状态获取结果。聚合函数使用 `IAggregateFunction` 接口进行管理。状态可以非常简单（`AggregateFunctionCount` 的状态只是一个单一的UInt64 值），
也可以非常复杂（`AggregateFunctionUniqCombined` 的状态是由一个线性数组、一个散列表和一个 `HyperLogLog` 概率数据结构组合而成的）。

为了能够在执行一个基数很大的 `GROUP BY` 查询时处理多个聚合状态，需要在 `Arena`（一个内存池）或任何合适的内存块中分配状态。状态可以有一个非普通构造器和析构器：比如复杂的聚合状态能够自己分配额外的内存。
这需要注意状态的创建和销毁并恰当地传递状态的所有权，以跟踪谁将何时销毁状态。

聚合状态可以被序列化和反序列化，以在分布式查询执行期间通过网络传递或者在内存不够的时候将其写到硬盘。聚合状态甚至可以通过 `DataTypeAggregateFunction` 存储到一个表中，以允许数据的增量聚合。

聚合函数状态的序列化数据格式目前尚未版本化。如果只是临时存储聚合状态，这样是可以的。但是我们有 `AggregatingMergeTree` 表引擎用于增量聚合，并且人们已经在生产中使用它。
这就是为什么在未来当我们更改任何聚合函数的序列化格式时需要增加向后兼容的支持。

## 2.14 服务（Server）
服务端实现了多个不同的接口：
* 一个用于任何外部客户端的 HTTP 接口。
* 一个用于本机 ClickHouse 客户端以及在分布式查询执行中跨服务器通信的 TCP 接口。
* 一个用于传输数据以进行拷贝的接口。

在内部，它只是一个没有coroutines、fibers等的基础多线程服务器。服务端不是为处理高速率的简单查询设计的，而是为处理相对低速率的复杂查询设计的，每一个复杂查询能够对大量的数据进行处理分析。

服务端使用必要的查询执行需要的环境初始化 `Context` 类：可用数据库列表、用户和访问权限、设置、集群、进程列表和查询日志等。这些环境被解释器使用。

我们维护了服务器 TCP 协议的完全向后向前兼容性：旧客户端可以和新服务器通信，新客户端也可以和旧服务器通信。但是我们并不想永久维护它，我们将在大约一年后删除对旧版本的支持。

对于所有的外部应用，我们推荐使用 HTTP 接口，因为该接口很简单，容易使用。TCP 接口与内部数据结构的联系更加紧密：它使用内部格式传递数据块，并使用自定义帧来压缩数据。
我们没有发布该协议的 C 库，因为它需要链接大部分的 ClickHouse 代码库，这是不切实际的。

## 2.15 分布式查询执行（Distributed Query Execution）
集群设置中的服务器大多是独立的。你可以在一个集群中的一个或多个服务器上创建一个 `Distributed` 表。`Distributed` 表本身并不存储数据，它只为集群的多个节点上的所有本地表提供一个“视图（view）”。
当从 `Distributed` 表中进行 SELECT 时，它会重写该查询，根据负载均衡设置来选择远程节点，并将查询发送给节点。`Distributed` 表请求远程服务器处理查询，直到可以合并来自不同服务器的中间结果的阶段。
然后它接收中间结果并进行合并。分布式表会尝试将尽可能多的工作分配给远程服务器，并且不会通过网络发送太多的中间数据。

当 IN 或 JOIN 子句中包含子查询并且每个子查询都使用分布式表时，事情会变得更加复杂。我们有不同的策略来执行这些查询。

分布式查询执行没有全局查询计划，每个节点都有针对自己的工作部分的本地查询计划。我们仅有简单的一次性分布式查询执行：将查询发送给远程节点，然后合并结果。
但是对于具有高基数的 GROUP BY 或具有大量临时数据的 JOIN 这样困难的查询的来说，这是不可行的：在这种情况下，我们需要在服务器之间“改组”数据，这需要额外的协调。
ClickHouse 不支持这类查询执行，我们需要在这方面进行努力。

## 2.16 合并树（Merge Tree）
`MergeTree` 是一系列支持按主键索引的存储引擎，主键可以是一个任意的列或表达式的元组。`MergeTree` 表中的数据存储于“parts”（分块）中。
每一个分块以主键序存储数据（数据按主键元组的字典序排序），表的所有列都存储在这些“分块”中分离的 `column.bin` 文件中，`column.bin` 文件由压缩块组成，
每一个块通常是 64 KB 到 1 MB 大小的未压缩数据，具体取决于平均值大小。这些块由一个接一个连续放置的列值组成。每一列的列值顺序相同（顺序由主键定义），因此当你按多列进行迭代时，你能够得到相应列的值。

主键本身是“稀疏”的，它并不是索引单一的行，而是索引某个范围内的数据。一个单独的`primary.idx` 文件具有每个第 N 行的主键值，其中 N 称为 `index_granularity`（通常N = 8192）。
同时对于每一列，都有带有标记的 `column.mrk` 文件，该文件记录的是每个第 N 行在数据文件中的偏移量。每个标记是一个 pair：文件中的偏移量到压缩块的起始、以及解压缩块中的偏移量到数据的起始。
通常压缩块根据标记对齐，并且解压缩块中的偏移量为 0。`primary.idx` 的数据始终驻留在内存，同时 `column.mrk` 的数据被缓存。

当我们要从 `MergeTree` 的一个分块中读取部分内容时，我们会查看 `primary.idx` 数据并查找可能包含所请求数据的范围，然后查看 `column.mrk` 并计算偏移量从而得知从哪里开始读取些范围的数据。
由于稀疏性，可能会读取额外的数据。ClickHouse 不适用于高负载的简单点查询，因为对于每一个键，整个`index_granularity` 范围的行的数据都需要读取，并且对于每一列需要解压缩整个压缩块。
我们使索引稀疏，是因为每一个单一的服务器需要在索引没有明显内存消耗的情况下，维护数万亿行的数据。另外由于主键是稀疏的，
导致其不是唯一的：无法在 INSERT 时检查一个键在表中是否存在。你可以在一个表中使用同一个键创建多个行。

当你向 MergeTree 中插入一堆数据时，数据按主键排序并形成一个新的分块。**为了保证分块的数量相对较少，有后台线程定期选择一些分块并将它们合并成一个有序的分块，这就是 MergeTree 的名称来源**。
当然，合并会导致“写入放大”。所有的分块都是不可变的：它们仅会被创建和删除，不会被修改。当运行 SELECT 查询时，MergeTree 会保存一个表的快照（分块集合）。合并之后，还会保留旧的分块一段时间，
以便发生故障后更容易恢复，因此如果我们发现某些合并后的分块可能已损坏，我们可以将其替换为原分块。

MergeTree 不是 LSM 树，因为它不包含”memtable“和”log“：插入的数据直接写入文件系统。**这使得它仅适用于批量插入数据**，而不适用于非常频繁地一行一行插入 - 大约每秒一次是没问题的，
但是每秒一千次就会有问题。我们这样做是为了简单起见，因为我们已经在我们的应用中批量插入数据。

MergeTree 表只能有一个（主）索引：没有任何辅助索引。在一个逻辑表下，允许有多个物理表示，比如可以以多个物理顺序存储数据，或者同时表示预聚合数据和原始数据。

有些 MergeTree 引擎会在后台合并期间做一些额外工作，比如 CollapsingMergeTree 和 AggregatingMergeTree。这可以视为对更新的特殊支持。
请记住这些不是真正的更新，因为用户通常无法控制后台合并将会执行的时间，并且 MergeTree 中的数据几乎总是存储在多个分块中，而不是完全合并的形式。

## 2.17 复制（Replication）
ClickHouse 中的复制是基于表实现的，你可以在同一个服务器上有一些可复制的表和不可复制的表。你也可以以不同的方式进行表的复制，比如一个表进行双因子复制，另一个进行三因子复制。

复制是在 `ReplicatedMergeTree` 存储引擎中实现的。ZooKeeper 中的路径被指定为存储引擎的参数。ZooKeeper 中所有具有相同路径的表互为副本：它们同步数据并保持一致性。只需创建或删除表，就可以实现动态添加或删除副本。

复制使用异步多主机方案。你可以将数据插入到与 ZooKeeper 进行会话的任意副本中，并将数据复制到所有其它副本中。由于 **ClickHouse 不支持 UPDATEs**，因此复制是无冲突的。由于没有对插入的仲裁确认，如果一个节点发生故障，刚刚插入的数据可能会丢失。

用于复制的元数据存储在 ZooKeeper 中。其中一个复制日志列出了要执行的操作。操作包括：获取分块、合并分块和删除分区等。每一个副本将复制日志复制到其队列中，
然后执行队列中的操作，比如，在插入时，在复制日志中创建“获取分块”这一操作，然后每一个副本都会去下载该分块。所有副本之间会协调进行合并以获得相同字节的结果。
所有的分块在所有的副本上以相同的方式合并。为实现该目的，其中一个副本被选为领导者，该副本首先进行合并，并把“合并分块”操作写到日志中。

复制是物理的：只有压缩的分块会在节点之间传输，查询则不会。为了降低网络成本（避免网络放大），大多数情况下，会在每一个副本上独立地处理合并。
只有在存在显著的合并延迟的情况下，才会通过网络发送大块的合并分块。

另外，每一个副本将其状态作为分块和校验组成的集合存储在 ZooKeeper 中。当本地文件系统中的状态与 ZooKeeper 中引用的状态不同时，
该副本会通过从其它副本下载缺失和损坏的分块来恢复其一致性。当本地文件系统中出现一些意外或损坏的数据时，ClickHouse 不会将其删除，而是将其移动到一个单独的目录下并忘记它。

ClickHouse 集群由独立的分片组成，每一个分片由多个副本组成。集群不是弹性的，因此在添加新的分片后，数据不会自动在分片之间重新平衡。相反，集群负载将变得不均衡。
该实现为你提供了更多控制，对于相对较小的集群，例如只有数十个节点的集群来说是很好的。但是对于我们在生产中使用的具有数百个节点的集群来说，这种方法成为一个重大缺陷。
我们应该实现一个表引擎，使得该引擎能够跨集群扩展数据，同时具有动态复制的区域，这些区域能够在集群之间自动拆分和平衡。


******

Document
----

# 1 介绍
ClickHouse是一个用于联机分析(OLAP)的列式数据库管理系统(DBMS)。

## OLAP场景的关键特征  
* 大多数是**读请求**；
* 数据总是以相当大的批(> 1000 rows)进行写入
* 不修改已添加的数据；
* 每次查询都从数据库中读取大量的行，但是同时又**仅需要少量的列**；
* 宽表，即每个表包含着大量的列
* 较少的查询(通常每台服务器每秒数百个查询或更少)
* 对于简单查询，允许延迟大约50毫秒
* 列中的数据相对较小： 数字和短字符串(例如，每个URL 60个字节)
* 处理单个查询时需要高吞吐量（每个服务器每秒高达数十亿行）
* 事务不是必须的
* 对数据一致性要求低
* 每一个查询除了一个大表外都很小
* 查询结果明显小于源数据，换句话说，数据被过滤或聚合后能够被盛放在单台服务器的内存中

## lickHouse可以考虑缺点的功能
没有完整的事务支持。
**缺少高频率、低延迟的修改或删除已存在数据的能力**。仅能用于批量删除或修改数据，但这符合 GDPR。
稀疏索引使得ClickHouse不适合通过其键检索单行的点查询。

## 性能
[Performance comparison of analytical DBMS](https://clickhouse.yandex/benchmark.html)

数据的写入性能¶    
我们建议每次写入不少于1000行的批量写入，或每秒不超过一个写入请求。当使用tab-separated格式将一份数据写入到MergeTree表中时，写入速度大约为50到200MB/s。
如果您写入的数据每行为1Kb，那么写入的速度为50，000到200，000行每秒。如果您的行更小，那么写入速度将更高。
**为了提高写入性能，您可以使用多个INSERT进行并行写入，这将带来线性的性能提升**。


******

# SQL语法
## 函数
函数 | 描述 | 返回值 
---- | ---- | ---- 
generateUUIDv4  | 生成一个UUID（版本4）。 | UUID类型的值。
toUUID (x)      | 将String类型的值转换为UUID类型的值。   | UUID类型的值
UUIDStringToNum | 接收一个String类型的UUID格式的字符串，返回为UUID的数值 | FixedString(16)
UUIDNumToString | 接受一个FixedString(16)类型的值，返回其对应的String表现形式。 | String


******



# 快速开始
以CentOS为例（`cat /etc/redhat-release`）。

ClickHouse安装需要Linux系统支持`SSE 4.2`
```
# “SSE 4.2 supported”
grep -q sse4_2 /proc/cpuinfo && echo “SSE 4.2 supported” || echo “SSE 4.2 not supported.

```

## 安装部署
可以查看我的blog [ClickHouse的安装(含集群方式)和使用](https://blog.csdn.net/github_39577257/article/details/103066747)，
也可以查看 [using.md](../clickhouse/ch-jdbc-client/using.md)


## 通过源码
```bash
# 1 下载源码
#  1.1 最新版
wget https://github.com/ClickHouse/ClickHouse/archive/v19.17.1.1603-testing.tar.gz
#  1.2 稳定版
wget https://github.com/ClickHouse/ClickHouse/archive/v19.11.13.74-stable.tar.gz

# 2 解压
tar -zxf v19.17.1.1603-testing.tar.gz

# 编译
mkdir build
cd build
cmake ..
ninja
cd ..

```

**问题1** GCC version must be at least 8
```
CMake Error at cmake/tools.cmake:11 (message):
  GCC version must be at least 8.  For example, if GCC 8 is available under
  gcc-8, g++-8 names, do the following: export CC=gcc-8 CXX=g++-8; rm -rf
  CMakeCache.txt CMakeFiles; and re run cmake or ./release.
Call Stack (most recent call first):
  CMakeLists.txt:19 (include)
```
我们通过`gcc --version`可以查看gcc版本。接下来升级我们系统的gcc。
```bash
# 1 下载gcc 8
 wget http://ftp.tsukuba.wide.ad.jp/software/gcc/releases/gcc-8.2.0/gcc-8.2.0.tar.gz

# 2 解压
tar -zxf gcc-8.2.0.tar.gz
cd gcc-8.2.0

# 3 安装依赖。All prerequisites downloaded successfully.
./contrib/download_prerequisites

# 4 配置
#./configure  --prefix=/usr --enable-multilib
./configure --prefix=/usr --enable-multilib --with-system-zlib

# 5 编译。如果有报错：make all-gcc
make
sudo make install 

# 6 查看是否安装成功
gcc --version


```

**问题2** if GCC 8 is available under gcc-8, g++-8 names, do the following: 
```bash
export CC=gcc-8
export CXX=g++-8
```

## 示例数据集
### On Time航班飞行数据
详细可查看 []

### Star Schema基准测试
```bash
git clone https://github.com/vadimtk/ssb-dbgen.git
cd ssb-dbgen
make

# 生成数据
#  生成 customer.tbl表数据，30000000 行，大概有  3.2G
#./dbgen -s 1000 -T c 
#  生成 customer.tbl表数据，30000 行，大概有  3.2M
#1,"Customer#000000001","j5JsirBM9P","MOROCCO  0","MOROCCO","AFRICA","25-989-741-2988","BUILDING",
#2,"Customer#000000002","487LW1dovn6Q4dMVym","JORDAN   1","JORDAN","MIDDLE EAST","23-768-687-3665","AUTOMOBILE",
./dbgen -s 1 -T c

#  生成 lineorder.tbl 大概有 > 50GB
#./dbgen -s 1000 -T l
#  生成 lineorder.tbl，6001215 行数据，大概有 641M
#1,1,7381,155190,828,"1996-01-02","5-LOW",0,17,2116823,17366547,4,2032150,74711,2,"1996-02-12","TRUCK",
#1,2,7381,67310,163,"1996-01-02","5-LOW",0,36,4598316,17366547,9,4184467,76638,6,"1996-02-28","MAIL",
./dbgen -s 1 -T l

#  生成 part.tbl 大概有  
#./dbgen -s 1000 -T p
#  200000 行，大概有  20M
#1,"lace spring","MFGR#1","MFGR#11","MFGR#1121","goldenrod","PROMO BURNISHED COPPER",7,"JUMBO PKG",
#2,"rosy metallic","MFGR#4","MFGR#43","MFGR#4318","blush","LARGE BRUSHED BRASS",1,"LG CASE",
./dbgen -s 1 -T p

#  生成 supplier.tbl 大概有  
#./dbgen -s 1000 -T s
#  2000 行，大概有  188K
#1,"Supplier#000000001","sdrGnXCDRcfriBvY0KL,i","PERU     0","PERU","AMERICA","27-989-741-2988",
#2,"Supplier#000000002","TRMhVHz3XiFu","ETHIOPIA 1","ETHIOPIA","AFRICA","15-768-687-3665",
./dbgen -s 1 -T s

#  生成 date.tbl) 大概有  
#./dbgen -s 1000 -T d
#  2556 行，大概有  272K
#19920101,"January 1, 1992","Thursday","January",1992,199201,"Jan1992",5,1,1,1,1,"Winter","0","1","1","1",
#19920102,"January 2, 1992","Friday","January",1992,199201,"Jan1992",6,2,2,1,1,"Winter","0","1","0","1",
./dbgen -s 1 -T d

```

在ClickHouse中创建表结构：
```sql

CREATE TABLE customer( \
 C_CUSTKEY       UInt32,\
 C_NAME          String,\
 C_ADDRESS       String,\
 C_CITY          LowCardinality(String),\
 C_NATION        LowCardinality(String),\
 C_REGION        LowCardinality(String),\
 C_PHONE         String,\
 C_MKTSEGMENT    LowCardinality(String)\
)ENGINE = MergeTree ORDER BY (C_CUSTKEY);

CREATE TABLE lineorder(\
 LO_ORDERKEY             UInt32,\
 LO_LINENUMBER           UInt8,\
 LO_CUSTKEY              UInt32,\
 LO_PARTKEY              UInt32,\
 LO_SUPPKEY              UInt32,\
 LO_ORDERDATE            Date,\
 LO_ORDERPRIORITY        LowCardinality(String),\
 LO_SHIPPRIORITY         UInt8,\
 LO_QUANTITY             UInt8,\
 LO_EXTENDEDPRICE        UInt32,\
 LO_ORDTOTALPRICE        UInt32,\
 LO_DISCOUNT             UInt8,\
 LO_REVENUE              UInt32,\
 LO_SUPPLYCOST           UInt32,\
 LO_TAX                  UInt8,\
 LO_COMMITDATE           Date,\
 LO_SHIPMODE             LowCardinality(String)\
)ENGINE = MergeTree PARTITION BY toYear(LO_ORDERDATE) ORDER BY (LO_ORDERDATE, LO_ORDERKEY);

CREATE TABLE part(\
 P_PARTKEY       UInt32,\
 P_NAME          String,\
 P_MFGR          LowCardinality(String),\
 P_CATEGORY      LowCardinality(String),\
 P_BRAND         LowCardinality(String),\
 P_COLOR         LowCardinality(String),\
 P_TYPE          LowCardinality(String),\
 P_SIZE          UInt8,\
 P_CONTAINER     LowCardinality(String)\
)ENGINE = MergeTree ORDER BY P_PARTKEY;

CREATE TABLE supplier(
 S_SUPPKEY       UInt32,
 S_NAME          String,
 S_ADDRESS       String,
 S_CITY          LowCardinality(String),
 S_NATION        LowCardinality(String),
 S_REGION        LowCardinality(String),
 S_PHONE         String
)ENGINE = MergeTree ORDER BY S_SUPPKEY;

```

写入数据：
```bash
clickhouse-client --query "INSERT INTO customer FORMAT CSV" < customer.tbl
clickhouse-client --query "INSERT INTO part FORMAT CSV" < part.tbl
clickhouse-client --query "INSERT INTO supplier FORMAT CSV" < supplier.tbl
clickhouse-client --query "INSERT INTO lineorder FORMAT CSV" < lineorder.tbl

```

将“星型模型”转换为非规范化的“平面模型”：
```sql
SET max_memory_usage = 2000000000, allow_experimental_multiple_joins_emulation = 1;

-- 这一步数据集如果太小可能关联不到数据
--  可以修改部分数据： ALTER TABLE part UPDATE P_PARTKEY = 200000 where LO_ORDERKEY=3840416
--  in (select user_id from tt);
CREATE TABLE lineorder_flat\
ENGINE = MergeTree\
PARTITION BY toYear(LO_ORDERDATE)\
ORDER BY (LO_ORDERDATE, LO_ORDERKEY) AS\
SELECT l.*, c.*, s.*, p.*\
FROM lineorder l\
 ANY INNER JOIN customer c ON (c.C_CUSTKEY = l.LO_CUSTKEY)\
 ANY INNER JOIN supplier s ON (s.S_SUPPKEY = l.LO_SUPPKEY)\
 ANY INNER JOIN part p ON  (p.P_PARTKEY = l.LO_PARTKEY);

ALTER TABLE lineorder_flat DROP COLUMN C_CUSTKEY, DROP COLUMN S_SUPPKEY, DROP COLUMN P_PARTKEY;

30000
-- ALTER TABLE part UPDATE P_PARTKEY = 200000 where LO_ORDERKEY=3840416
SELECT l.*, c.*, s.*, p.*\
FROM lineorder l\
 ANY INNER JOIN customer c ON (1=1)\
 ANY INNER JOIN supplier s ON (2=2)\
 ANY INNER JOIN part p ON  (3=3);

SELECT COUNT() FROM part WHERE 3=3


SELECT * FROM lineorder UNION ALL customer,supplier,part LIMIT 10;

```

执行如下的 SQL 查询
```sql
--Q1.1

```






