#!/bin/bash
# @auther: Yore
# @date: 2020-03-12

# 查看 Hive 表大小可以通过: DESC formatted 表名; 这个SQL 会列出表的详细信息，包括字段信息、位置、大小、创建日期等
# 但如果汇总某库下所有表的大小信息，当表比较多时，使用 SQL 操作会比较麻烦，这里可以使用这个脚本。

function getNowTime {
	current=`date "+%Y-%m-%d %H:%M:%S"`
	timeStamp=`date -d "$current" +%s` 
	currentTimeStamp=$((timeStamp*1000+`date "+%N"`/1000000))  #将current转换为时间戳，精确到毫秒
}
getNowTime


# 库名
database_name="impala_demo"

# 库的路径
db_base_path="/user/hive/warehouse/impala_demo.db"

# 保存表的临时文件路径
tables_path="/tmp/${database_name}_ables-${currentTimeStamp}.txt"


# 获取库下的所有表
echo -e "==================== \t 开始导出表信息"

beeline -n hive -p hive -d "org.apache.hive.jdbc.HiveDriver" -u "jdbc:hive2://cdh3:10000/impala_demo" --showHeader=false --outputformat=csv2 -e "show tables" > $tables_path


# 获取表文件中所有表及表大小 
echo "表名,大小" >>./${database_name}_result.csv
for i in `cat $tables_path`
do
   echo -e "$i,\c" >>./${database_name}_result.csv
   hadoop fs -du "${db_base_path}/$i" | awk '{ sum=$1 ;dir2=$2 ; hum[1024**3]="Gb";hum[1024**2]="Mb";hum[1024]="Kb"; for (x=1024**3; x>=1024; x/=1024){ if (sum>=x) { printf "%.2f %s \t %s\n",sum/x,hum[x],dir2;break } }}' | awk '{print $1 " " $2}' >>./${database_name}_result.csv
	echo ">> $i 表信息生成完毕"
done

# 删除本地的临时文件
rm -rf ${tables_path}

echo -e "====================\t SUCCESS!"

