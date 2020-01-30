测试
---

[Slowly_changing_dimension](https://en.wikipedia.org/wiki/Slowly_changing_dimension)

[SCDType2Benchmark.scala](https://github.com/apache/carbondata/blob/master/examples/spark2/src/main/scala/org/apache/carbondata/benchmark/SCDType2Benchmark.scala)


<br/><br/>

Benchmark for **SCD** (Slowly Change Dimension) Type 2 performance.

这里显示了两种更新方法的 基准测试
1. **overwrite_solution**：使用 `INSERT OVERWRITE`。 这是 hive warehouse 常用的方法。
2. **update_solution**： 使用 `CarbonData's update` 语句直接更新历史记录表 。


在 8-cores 笔记本电脑运行基准测试显示：
1. test one:  
历史记录表 1M 条记录（约为百万），每天更新 10k 条记录，每天插入 10k 条记录。模拟三天。
    - **hive_solution**： 总处理时间为 13s
    - **carbon_solution**: 总处理时间为 6s

2. test two:  
历史记录表 10M 条记录（约为千万），每天更新 10K 条记录，每天插入 10K 条记录，模拟3天。
    - **hive_solution**： 总处理时间为 115s
    - **carbon_solution**: 总处理时间为 15s








