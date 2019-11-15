package yore

import java.sql.{Connection, DriverManager, ResultSet, ResultSetMetaData, Statement}

/**
  * 数据集可以通过执行这个脚本获取：
  * https://github.com/Percona-Lab/ontime-airline-performance/blob/master/download.sh
  *
  *
  *
  * Created by yore on 2019/11/4 17:25
  */
object JdbcClient {
  private val address =  "jdbc:clickhouse://cdh3:8123/default"


  def main(args: Array[String]): Unit = {
    Class.forName("ru.yandex.clickhouse.ClickHouseDriver")

    var connection: Connection = null
    var statement: Statement = null
    var results: ResultSet = null

    var sql = "SELECT COUNT(*) FROM part "
    sql = "SELECT * FROM lineorder ORDER BY LO_COMMITDATE DESC LIMIT 10"
    sql =
      """
        |SELECT
        |   min(Year), max(Year), Carrier, count(*) AS cnt,
        |   sum(ArrDelayMinutes>30) AS flights_delayed,
        |   round(sum(ArrDelayMinutes>30)/count(*),2) AS rate
        |FROM ontime
        |WHERE
        |   DayOfWeek NOT IN (6,7) AND OriginState NOT IN ('AK', 'HI', 'PR', 'VI')
        |   AND DestState NOT IN ('AK', 'HI', 'PR', 'VI')
        |   AND FlightDate < '2010-01-01'
        |GROUP by Carrier
        |HAVING cnt>100000 and max(Year)>1990
        |ORDER by rate DESC
        |LIMIT 1000;
      """.stripMargin


    try{
      // 用户名和密码就是前面在/etc/clickhouse-server/users.xml 中配置的
      connection = DriverManager.getConnection(address, "ck", "123456")
      statement = connection.createStatement

      val begin = System.currentTimeMillis
      results = statement.executeQuery(sql)
      val end = System.currentTimeMillis

      val rsmd: ResultSetMetaData = results.getMetaData()
      for(i <- 1 to rsmd.getColumnCount){
        print(s"${rsmd.getColumnName(i)}\t")
      }
      println()

      while(results.next()){
        for(i <- 1 to rsmd.getColumnCount){
          print(s"${results.getString(rsmd.getColumnName(i))}\t")
        }
        println()
      }
      println(s"${"-"*30}\n执行${sql} 耗时${end-begin} ms")

    }catch {
      case e: Exception => e.printStackTrace()
    }


  }

}
