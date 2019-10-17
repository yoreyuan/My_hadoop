Data Ingestion （数据摄取）
======

# 1 [Ingestion overview](https://druid.apache.org/docs/latest/ingestion/index.html) （摄取概述）

## 1.1 Datasources and segments
Apache Druid(孵化中)数据存储在“datasources”中，类似于传统RDBMS中的表。每个datasource按时间划分，并可选择进一步按其他属性划分。每个时间范围称为“chunk”（例如，如果您的datasource按天分区，则为一天）。
在chunk内，数据被划分为一个或多个"[segments](https://druid.apache.org/docs/latest/design/segments.html)"。 每个segment都是单个文件，通常包含多达几百万行数据。
由于segments被组织成时间chunks，因此将segments视为如下所示的靠时间轴存活的有时会有所帮助：

![druid-timeline.png](https://druid.apache.org/docs/img/druid-timeline.png)

 datasources可能只有几个segments，最多可达数十万甚至数百万个Segments。每个segment都开始在MiddleManager上创建生命，并且在那时，它是可变的和未提交的。
 segment构建过程包括以下步骤，旨在生成紧凑的数据文件并支持快速查询：
 * 转换为柱状格式
 * 使用位图索引进行索引
 * 使用各种算法进行压缩
     - 具有String列的id存储最小化的字典编码
     - 位图索引的位图压缩
     - 所有列的类型感知压缩

定期提交和发布segments。此时，它们被写入[深存储](https://druid.apache.org/docs/latest/design/index.html#deep-storage)，变为不可变，
并从MiddleManagers转移到Historical进程（有关详细信息，请参阅上面的[体系架构](https://druid.apache.org/docs/latest/design/index.html#architecture)）。
关于segment的条目也被写入[元数据存储](https://druid.apache.org/docs/latest/design/index.html#metadata-storage)。 此条目是关于segment的自描述元数据，
包括segment的架构，其大小以及它在深存储上的位置。 这些条目是Coordinator用于了解群集上应该有哪些数据可用。

有关segment文件格式的详细信息，请参阅[segment files](https://druid.apache.org/docs/latest/design/segments.html)。

有关使用Druid建模数据的详细信息，请参阅[schema design](https://druid.apache.org/docs/latest/ingestion/schema-design.html)。

### 1.1.1 Segment identifiers （Segment标识符）
segment都具有包含以下组件的四部分标识符：
* 数据源名称。
* 时间间隔（包含segment的时间块;这对应于在摄取时指定的`segmentGranularity`）。
* 版本号（通常是与首次启动段集时对应的ISO8601时间戳）。
* 分区号（一个整数，唯一的 datasource+interval+version；可能不一定是连续的）。

例如，这是数据源中的segment的标识符`clarity-cloud0`，时间块 `2018-05-21T16:00:00.000Z/2018-05-21T17:00:00.000Z`，版本` 2018-05-21T15:56:09.909Z`和分区号1：
```
clarity-cloud0_2018-05-21T16:00:00.000Z_2018-05-21T17:00:00.000Z_2018-05-21T15:56:09.909Z_1
```

分区号为0的segment（分块中的第一个分区）省略分区号，如下例所示，它是与前一个segment在同一时间块中的segment，但分区号为0而不是1：
```
clarity-cloud0_2018-05-21T16:00:00.000Z_2018-05-21T17:00:00.000Z_2018-05-21T15:56:09.909Z
```

### 1.1.2 Segment versioning （Segment版本）
您可能想知道上一部分中描述的“版本号”是什么。 或者您可能不会，在这种情况下对您有好处，您可以跳过本节！

它支持批量模式重写。在Druid如果您所做的只是追加数据，那么每个时间块只会有一个版本。 但是当您重写数据时，幕后发生的事情是使用相同的数据源，相同的时间间隔创建一组新的segment，
但版本号更高。 这是Druid系统其余部分的一个信号，即应该从群集中删除旧版本，新版本应该替换它。

切换似乎是即时发生给用户的，因为Druid首先通过加载新数据（但不允许查询）来处理这个问题，然后一旦新数据全部加载，切换所有新查询以使用这些新segment。 然后它在几分钟后删掉旧segment。

### 1.1.3 Segment states （Segment状态）
segment可以是可用的也可以是不可用的，这指的是它们当前是否由某些Druid服务器进程提供服务。它们也可以发布或未发布，这是指它们是否已写入深存储和元数据存储。
已发布的segment可以使用也可以不使用，这是指Druid是否认为活跃的segment应该被服务。

将这些放在一起，一个segment可以有五个基本状态：
* **已发布,可用和已使用**：这些segment在深存储和元数据存储中发布，它们由Historical进程提供。它们是Druid集群中的大多数活动数据（它们包括除了in-flight实时数据之外的所有数据）。
* **已发布,可用和未使用**：这些segment由Historical提供，但不会持续很长时间。它们可能是最近被重写的segment（请参阅[Segment versioning](https://druid.apache.org/docs/latest/ingestion/index.html#segment-versioning)）
或由于其他原因（例如删除规则或手动删除）而丢弃的segment。
* **已发布,不可用和已使用**：这些segment在深存储和元数据存储中发布，应该提供，但实际上并未提供服务。如果segment保持这种状态超过几分钟，通常是因为出了问题。一些更常见的原因包括：
大量历史记录失败，历史记录超出下载更多segment的能力，以及一些协调问题阻止Coordinator告知Historical加载新segment。
* **已发布,不可用和未使用**：这些segment在深存储和元数据存储中发布，但处于非活动状态（因为它们已被重写或删除）。它们处于休眠状态，如果需要，可以通过手动操作复活（特别是：将“used”标志设置为true）。
* **未发布和可用**：这是segment由Druid摄取任务构建时所处的状态。这包括尚未传递给Historical的所有“实时”数据。处于此状态的segment可能会也可能不会被复制。如果所有副本都丢失，
则必须从头开始重建该segment。这可能是也可能是不可能的。 （有可能使用Kafka，并且可以自动发生;通过重新启动作业可以使用S3/HDFS;而且Tranquility不可能，因此在这种情况下，数据将会丢失。）

### 1.1.4 Indexing and handoff （索引和切换）
索引是创建新segment的机制，切换是发布它们并开始由Historical进程提供服务的机制。该机制在索引方面的工作原理如下：
1. 索引任务开始运行并构建新segment。它必须在开始构建之前确定该segment的标识符。对于附加的任务（如Kafka任务或附加模式中的索引任务），这将通过调用Overlord上的**allocate API**来完成，
以便可能将新分区添加到现有的一组segment中。对于重写的任务（如Hadoop任务或索引任务不在追加模式中），可通过锁定间隔并创建新版本号和新的segment集来完成。
2. 如果索引任务是实时任务（如Kafka任务），则此时可立即查询该segment。它可用，但未发表。
3. 当索引任务完成读取segment的数据时，它会将其推送到深存储，然后通过将记录写入元数据存储来发布它。
4. 如果索引任务是实时任务，则此时它等待Historical进程加载该段。如果索引任务不是实时任务，则会立即退出。

在Coordinator/Historical 方面这样：
1. Coordinator定期轮询元数据存储（默认情况下，每1分钟一次），用于新发布的segment。
2. 当Coordinator找到已发布和使用但不可用的segment时，它会选择Historical进程来加载该segment并指示历史记录执行此操作。
3. 历史记录加载segment并开始提供它。
4. 此时，如果索引任务正在等待切换，则它将退出。

## 1.2 Ingestion methods （摄取方法）
在大多数摄取方法中，这项工作由Druid MiddleManager进程完成。一个例外是基于Hadoop的摄取，其中这项工作是使用YARN上的Hadoop MapReduce作业完成的（尽管MiddleManager进程仍然参与启动和监视Hadoop作业）。

一旦生成了segment并将其存储在[深存储](https://druid.apache.org/docs/latest/dependencies/deep-storage.html)中，它们将由Druid Historical进程加载。一些Druid摄取方法还支持实时查询，
这意味着您可以在完成转换并写入深存储之前查询MiddleManager进程中的正在运行的数据。通常，相对于从Historical进程提供的大量历史数据，将在MiddleManager进程中传输少量数据。

有关Druid如何存储和管理数据的更多详细信息，请参见[设计](https://druid.apache.org/docs/latest/design/index.html)页面。

下表列出了Druid最常见的数据提取方法，以及帮助您根据自己的情况选择最佳数据的比较。

方法 | 工作原理 | 可以追加和重写吗？ | 可以处理延迟数据吗？ | 恰好一次摄入 | 实时查询？
:---- | :---- | :---- | :---- | :---- |:---- 
[Native batch](https://druid.apache.org/docs/latest/ingestion/native_tasks.html) | Druid直接从S3, HTTP, NFS或者其他网络存储加载数据 | Append 或 overwrite | Yes | Yes | No
[Hadoop](https://druid.apache.org/docs/latest/ingestion/hadoop.html) | Druid启动Hadoop Map/Reduce job加载数据文件 | overwrite | Yes | Yes | No
[Kafka索引服务](https://druid.apache.org/docs/latest/development/extensions-core/kafka-ingestion.html) | Druid直接从Kafka读取 | 仅Append | Yes | Yes | Yes
[Tranquility](https://druid.apache.org/docs/latest/ingestion/stream-push.html) | 你可以使用Tranquility，一个客户端库，将个人记录推送到到Druid | 仅Append | No - 延迟数据被删除 | No - 可能丢弃或者数据重复 | Yes

## 1.3 Partitioning （分区）
Druid是一个分布式数据存储，它会对您的数据进行分区，以便并行处理它。Druid [datasources](https://druid.apache.org/docs/latest/design/index.html)总是首先根据摄取规范的
[segmentGranularity](https://druid.apache.org/docs/latest/ingestion/index.html#granularityspec)参数按时间划分。这些时间分区中的每一个都称为时间chunk，
每个时间chunk都包含一个或多个[segments](https://druid.apache.org/docs/latest/design/segments.html)。特定时间chunk内的segments可以使用根据您选择的摄取方法而变化的选项进一步分区。
* 使用[Hadoop](https://druid.apache.org/docs/latest/ingestion/hadoop.html)，您可以在一列或多列上执行基于hash或范围的分区。
* 使用[Native batch](https://druid.apache.org/docs/latest/ingestion/native_tasks.html)，您可以对维度列进行hash分区。启用rollup时，这非常有用，因为它可以最大程度地节省空间。
* 使用[Kafka indexing](https://druid.apache.org/docs/latest/development/extensions-core/kafka-ingestion.html)，分区基于Kafka分区，并且无法通过Druid进行配置。您可以使用Kafka生产者的分区功能在Kafka端配置它。
* 使用[Tranquility](https://druid.apache.org/docs/latest/ingestion/stream-push.html)，默认情况下，在所有维度列上进行hash分区，以便最大化rollup。您还可以提供自定义的Partitioner类;有关详细信息，请参阅[Tranquility documentation](https://github.com/druid-io/tranquility/blob/master/docs/overview.md#partitioning-and-replication)。

所有Druid datasources都按时间划分。每个数据摄取方法在加载数据时必须在特定时间范围内获取写锁定，因此两个方法不可能同时在同一数据源的同一时间范围内操作。 但是两种数据摄取方法可以同时在同一datasource的不同时间范围内运行。 
例如，您可以从Hadoop执行批量回填，同时还可以从Kafka执行实时加载，只要回填数据和实时数据不需要写入同一时间分区即可。 （如果他们这样做，实时负载将优先。）

有关分区如何影响性能和存储空间的提示，请参阅[schema design](https://druid.apache.org/docs/latest/ingestion/schema-design.html#partitioning)页面。

## 1.4 Rollup （汇总）
Druid能够使用我们称之为“roll-up”的过程在摄取时间汇总原始数据(summarize raw data)。 roll-up是对选定的一组“维度”的第一级聚合操作，其中聚合了一组“metrics”。

假设我们有以下原始数据，表示源和目标之间的流量的特定秒的总 packet/byte 计数。 `srcIP`和`dstIP`字段是维度，而`packets`和`bytes`是度量。

```
timestamp                 srcIP         dstIP          packets     bytes
2018-01-01T01:01:35Z      1.1.1.1       2.2.2.2            100      1000
2018-01-01T01:01:51Z      1.1.1.1       2.2.2.2            200      2000
2018-01-01T01:01:59Z      1.1.1.1       2.2.2.2            300      3000
2018-01-01T01:02:14Z      1.1.1.1       2.2.2.2            400      4000
2018-01-01T01:02:29Z      1.1.1.1       2.2.2.2            500      5000
2018-01-01T01:03:29Z      1.1.1.1       2.2.2.2            600      6000
2018-01-02T21:33:14Z      7.7.7.7       8.8.8.8            100      1000
2018-01-02T21:33:45Z      7.7.7.7       8.8.8.8            200      2000
2018-01-02T21:35:45Z      7.7.7.7       8.8.8.8            300      3000
```

如果我们使用`queryCranularity` 的 `minute`（将时间戳记为几分钟）将此数据摄取到Druid中，则roll-up操作等效于以下伪代码（pseudocode）：
```
GROUP BY TRUNCATE(timestamp, MINUTE), srcIP, dstIP :: SUM(packets), SUM(bytes)
```

在roll-up期间汇总上述数据后，将接收以下行：
```
timestamp                 srcIP         dstIP          packets     bytes
2018-01-01T01:01:00Z      1.1.1.1       2.2.2.2            600      6000
2018-01-01T01:02:00Z      1.1.1.1       2.2.2.2            900      9000
2018-01-01T01:03:00Z      1.1.1.1       2.2.2.2            600      6000
2018-01-02T21:33:00Z      7.7.7.7       8.8.8.8            300      3000
2018-01-02T21:35:00Z      7.7.7.7       8.8.8.8            300      3000
```

汇总粒度是您将能够探查数据的最小粒度，并且事件将被覆盖到此粒度。 因此，Druid摄取规格将此粒度定义为数据的查询粒度。支持的最低`queryGranularity`是毫秒。

以下链接可能有助于进一步了解维度和指标：
* [https://en.wikipedia.org/wiki/Dimension_(data_warehouse)](https://en.wikipedia.org/wiki/Dimension_(data_warehouse))
* [https://en.wikipedia.org/wiki/Measure_(data_warehouse)](https://en.wikipedia.org/wiki/Measure_(data_warehouse))

有关如何在Druid架构设计中使用汇总的提示，请参阅[schema design](https://druid.apache.org/docs/latest/ingestion/schema-design.html#rollup)页面。

### 1.4.1 Roll-up modes （汇总模式）
Druid支持两种汇总模式，即完美汇总（perfect roll-up）和尽力而汇总（best-effort roll-up）。在完美汇总模式中，Druid保证输入数据在摄取时间内完美聚合。同时，在尽力汇总中，
输入数据可能不会被完美地聚合，因此可以有多个segment保持的行应该属于同一segment并具有完美汇总，因为它们具有相同的维度值并且时间戳落入相同的时间间隔。

完美汇总模式包含一个额外的预处理步骤，如果未在ingestionSpec中指定，则在实际数据摄取之前确定间隔和shardSpec。此预处理步骤通常会扫描整个输入数据，
这可能会增加摄取时间。 [Hadoop indexing task](https://druid.apache.org/docs/latest/ingestion/hadoop.html)总是以这种完美的汇总模式运行。

相反，尽力汇总模式不需要任何预处理步骤，但摄取数据的大小可能大于完美汇总的大小。所有类型的[streaming indexing (例如 kafka indexing service)](https://druid.apache.org/docs/latest/ingestion/stream-ingestion.html)都以此模式运行。

最后，[本机索引任务](https://druid.apache.org/docs/latest/ingestion/native_tasks.html)支持两种模式，您可以选择适合您的应用程序的模式。

## 1.5 Data maintenance （数据维护）
### 1.5.1 Inserts and overwrites （摄入与重写）
Druid可以通过将新segment添加到现有segment set来将新数据插入现有数据源。 它还可以通过将现有的一组segment与新数据合并并重写原始集来添加新数据。

Druid不支持主键的单记录更新。

Update会进一步在[更新现有数据](https://druid.apache.org/docs/latest/ingestion/update-existing-data.html)时描述。

### 1.5.2 Compaction （压缩）
压缩是一种重写操作，它读取现有的一组segment，将它们组合成一个具有较大但较少segment的新set，并用新压缩集重写原始集，而不更改存储的数据。

出于性能原因，将一组segment压缩为一组较大但较少的segment有时是有益的，因为在摄取和查询路径中存在一些每个segment进程和内存开销。

有关压缩的文档，请参阅[tasks](https://druid.apache.org/docs/latest/ingestion/tasks.html)。

### 1.5.3 Retention and Tiering （保留与分层）
Druid支持retention规则，用于定义应保留数据的时间间隔，以及应丢弃数据的间隔。

Druid还支持将Historical进程分成层，并且可以配置retention规则以将特定时间间隔的数据分配给特定层。

这些功能对 性能/成本 管理很有用; 一个常见的用例是将Historical进程分为“热”层和“冷”层。

有关更多信息，请参阅[Load rules](https://druid.apache.org/docs/latest/operations/rule-configuration.html)。

### 1.5.4 Deletes
Druid支持永久删除处于“未使用”状态的segment（请参阅上面的[segment状态](https://druid.apache.org/docs/latest/ingestion/index.html#segment-states)部分）。

Kill Task从元数据存储和深存储中删除指定时间间隔内未使用的segment。

有关更多信息，请参阅[Kill Task](https://druid.apache.org/docs/latest/ingestion/tasks.html#kill-task)。

<br/>

**********

<br/>




