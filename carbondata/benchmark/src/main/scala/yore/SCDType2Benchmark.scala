/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package yore

import java.io.File
import java.sql.Date

import org.apache.commons.lang3.time.DateUtils
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

import org.apache.spark.sql.catalyst.catalog.SessionCatalog



/**
  * 详见 <a href="https://github.com/apache/carbondata/blob/master/examples/spark2/src/main/scala/org/apache/carbondata/benchmark/SCDType2Benchmark.scala">org.apache.carbondata.benchmark</a>
  *
  * <pre>
  *
  *   这里显示了两种更新方法的 基准测试: ① overwrite、② update
  *
  *   在 8-cores 笔记本电脑运行基准测试显示：
  *
  *   1. test one:   历史记录表 1M 条记录（约为百万），每天更新 10k 条记录，每天插入 10k 条记录。模拟三天。
  *     - **hive_solution**： 总处理时间为 13s
  *     - **carbon_solution**: 总处理时间为 6s
  *
  * 2. test two:     历史记录表 10M 条记录（约为千万），每天更新 10K 条记录，每天插入 10K 条记录，模拟3天。
  *     - **hive_solution**： 总处理时间为 115s
  *     - **carbon_solution**: 总处理时间为 15s
  *
  * </pre>
  *
  * Created by yore on 2019/12/30 09:34
  */
object SCDType2Benchmark {

  /**
    * 历史记录表 结构
    *
    * dw_order_solution1 - 使用 overwrite
    * dw_order_solution2 - 使用 update
    *
    *  +-------------+-----------+-------------+
    *  | Column name | Data type | Cardinality |
    *  +-------------+-----------+-------------+
    *  | order_id    | string    | 10,000,000  |
    *  +-------------+-----------+-------------+
    *  | customer_id | string    | 10,000,000  |
    *  +-------------+-----------+-------------+
    *  | start_date  | date      | NA          |
    *  +-------------+-----------+-------------+
    *  | end_date    | date      | NA          |
    *  +-------------+-----------+-------------+
    *  | state       | int       | 4           |
    *  +-------------+-----------+-------------+
    *
    */
  case class Order (order_id: String, customer_id: String, start_date: Date, end_date: Date, state: Int)


  /**
    * Change 表结构，用来每天更新历史表
    * 表名 ods_order
    *
    *  +-------------+-----------+-------------+
    *  | Column name | Data type | Cardinality |
    *  +-------------+-----------+-------------+
    *  | order_id    | string    | 10,000,000  |
    *  +-------------+-----------+-------------+
    *  | customer_id | string    | 10,000,000  |
    *  +-------------+-----------+-------------+
    *  | update_date | date      | NA          |
    *  +-------------+-----------+-------------+
    *  | state       | int       | 4           |
    *  +-------------+-----------+-------------+
    *
    */
  case class Change (order_id: String, customer_id: String, update_date: Date, state: Int)


  // 第一天的记录数 (10M 条)
  val numOrders = 10000000
  //每天更新的记录数 （10k 条）
  val numUpdateOrdersDaily = 10000
  //每天要插入的新记录数 （10k 条）
  val newNewOrdersDaily = 10000
  //要模拟的天数
  val numDays = 3
  //打印每天结果是否输出到控制台
  val printDetail = true


  /**
    * Main
    *
    * @param args Array[String]
    */
  def main(args: Array[String]): Unit = {
    import org.apache.spark.sql.CarbonSession._
    // 项目的根目录下
    val rootPath = new File(this.getClass.getResource("/").getPath + "../../../..").getCanonicalPath
    println(rootPath)

    val spark = SparkSession
      .builder()
      .master("local[8]")
      .enableHiveSupport()
//      .config("spark.sql.warehouse.dir", s"$rootPath/carbondata/benchmark/target/warehouse")
      .config("spark.sql.warehouse.dir", s"$rootPath/carbondata/benchmark/src/main/resources")
      .getOrCreateCarbonSession()
    spark.sparkContext.setLogLevel("error")

    // prepare base data for first day
    spark.sql("drop table if exists dw_order_solution1")
    spark.sql("drop table if exists dw_order_solution2")
    spark.sql("drop table if exists change")


    val baseData = generateDataForDay0(
      sparkSession = spark,
      numOrders = numOrders,
      startDate = Date.valueOf("2018-05-01"))
    baseData.write
      .format("carbondata")
      .option("tableName", "dw_order_solution1")
      .mode(SaveMode.Overwrite)
      .save()

    baseData.write
      .format("carbondata")
      .option("tableName", "dw_order_solution2")
      .option("sort_columns", "order_id")
      .mode(SaveMode.Overwrite)
      .save()

    //    spark.sql("show tables").show()

    var startDate = Date.valueOf("2018-05-01")
    var state = 2
    var solution1UpdateTime = 0L
    var solution2UpdateTime = 0L

    if (printDetail) {
      println("## day0")
      spark.sql("select * from dw_order").show(100, false)
    }

    def timeIt(func: (SparkSession) => Unit): Long = {
      val start = System.nanoTime()
      func(spark)
      System.nanoTime() - start
    }


    for (i <- 1 to numDays) {
      // prepare for incremental update data for day-i
      val newDate = new Date(DateUtils.addDays(startDate, 1).getTime)
      val changeData = generateDailyChange(
        sparkSession = spark,
        numUpdatedOrders = numUpdateOrdersDaily,
        startDate = startDate,
        updateDate = newDate,
        newState = state,
        numNewOrders = newNewOrdersDaily)
      changeData.write
        .format("carbondata")
        .option("tableName", "change")
        .mode(SaveMode.Overwrite)
        .save()

      if (printDetail) {
        println(s"day$i Change")
        spark.sql("select * from change").show(100, false)
      }

      // apply Change to history table by using INSERT OVERWRITE
      solution1UpdateTime += timeIt(solution1)

      // apply Change to history table by using UPDATE
      solution2UpdateTime += timeIt(solution2)

      if (printDetail) {
        println(s"day$i result")
        spark.sql("select * from dw_order_solution1").show(false)
        spark.sql("select * from dw_order_solution2").show(false)
      }

      startDate = newDate
      state = state + 1
    }


    // do a query after apply SCD Type2 update
    val solution1QueryTime = timeIt(
      spark => spark.sql(
        s"""
           | select sum(state) as sum, customer_id
           | from dw_order_solution1
           | group by customer_id
           | order by sum
           | limit 10
           |""".stripMargin).collect()
    )

    val solution2QueryTime = timeIt(
      spark => spark.sql(
        s"""
           | select sum(state) as sum, customer_id
           | from dw_order_solution2
           | group by customer_id
           | order by sum
           | limit 10
           |""".stripMargin).collect()
    )

    // print update time
    println(s"overwrite solution update takes ${solution1UpdateTime / 1000 / 1000 / 1000} s")
    println(s"update solution update takes ${solution2UpdateTime / 1000 / 1000 / 1000} s")

    // print query time
    println(s"overwrite solution query takes ${solution1QueryTime / 1000 / 1000 / 1000} s")
    println(s"update solution query takes ${solution2QueryTime / 1000 / 1000 / 1000} s")

    spark.close()

  }



  def generateDataForDay0(
                           sparkSession: SparkSession,
                           numOrders: Int = 1000000,
                           startDate: Date = Date.valueOf("2018-05-01")
                         ) : DataFrame = {
    import sparkSession.implicits._
    sparkSession
      .sparkContext
      .parallelize(1 to numOrders, 4)
      .map(x => Order(s"order$x", s"customer$x", startDate, Date.valueOf("9999-01-01"), 1))
      .toDS()
      .toDF()
  }


  def generateDailyChange(
                           sparkSession: SparkSession,
                           numUpdatedOrders: Int,
                           startDate: Date,
                           updateDate: Date,
                           newState: Int,
                           numNewOrders: Int
                         ): DataFrame = {
    import sparkSession.implicits._
    // 更新历史记录表数据
    val ds1 = sparkSession
      .sparkContext
      .parallelize(1 to numUpdatedOrders, 4)
      .map(x => Change(s"order$x", s"customer$x", updateDate, newState))
      .toDS()
      .toDF()

    // 插入历史记录表日期
    val ds2 = sparkSession
      .sparkContext
      .parallelize(1 to numNewOrders, 4)
      .map (x => Change(s"newOrder${System.currentTimeMillis()}", s"customer$x", updateDate, 1))
      .toDS()
      .toDF()

    // 合并它们，以便 Change 表包含用于更新和插入的数据
    ds1.union(ds2)

  }

  /**
    * Typical solution when using hive
    * This solution uses INSERT OVERWRITE to rewrite the whole table every day
    */
  def solution1(spark: SparkSession) = {
    spark.sql(
      """
        | insert overwrite table dw_order_solution1
        | select * from
        | (
        |   select A.order_id, A.customer_id, A.start_date,
        |     case when A.end_date > B.update_date then B.update_date
        |     else A.end_date
        |     end as end_date,
        |   A.state
        |   from dw_order_solution1 A
        |   left join change B
        |   on A.order_id = B.order_id
        |   union all
        |     select B.order_id, B.customer_id, B.update_date, date("9999-01-01"), B.state
        |     from change B
        | ) T
      """.stripMargin)
  }

  /**
    * Solution leveraging carbon's update syntax
    */
  def solution2(spark: SparkSession) = {
    spark.sql(
      """
        | update dw_order_solution2 A
        | set (A.end_date) =
        |   (select B.update_date
        |   from change B
        |   where A.order_id = B.order_id and A.end_date > B.update_date)
      """.stripMargin).show
    spark.sql(
      """
        | insert into dw_order_solution2
        | select B.order_id, B.customer_id, B.update_date, date('9999-12-30'), B.state
        | from change B
      """.stripMargin)
  }

}

