package yore

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}

/**
  * Impala JDBC
  *
  * Created by yore on 2019/5/20 06:46
  */
object ImpalaJDBCClient {

  private[yore] val JDBC_DRIVER: String = "com.cloudera.impala.jdbc41.Driver"
  // 端口可以查看：Impala 配置项 hs2_port
  private[yore] val URL = "jdbc:impala://cdh3:21050/kudu_test"

  def main(args: Array[String]): Unit = {
    var conn: Connection = null
    var ps: PreparedStatement = null

    try{
      Class.forName(JDBC_DRIVER)
      conn = DriverManager.getConnection(URL)

      var sql = "show tables"
      sql = "select * from tag_5 limit 10"

      ps = conn.prepareStatement(sql)
      val result: ResultSet = ps.executeQuery()

      val start = System.currentTimeMillis()
      while (result.next()){
        for(pos <- 1 to result.getMetaData.getColumnCount){
          print(result.getObject(pos) + "\t")
        }
        println()
      }
      val end = System.currentTimeMillis()
      println("-"*26 + s" Impala\n\t共花费：${(end - start).toDouble/1000} 秒")
    }catch {
      case e: Exception => e.printStackTrace()
    }finally {
      if(ps != null) ps.close()
      if(conn != null) conn.close()
      println("-"*26)
    }


  }

}
