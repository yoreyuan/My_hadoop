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
#getNowTime


## Hive 的 url。默认连接
#hive_url="jdbc:hive2://cdh3:10000/$database_name"
## 库的路径
#db_base_path="/user/hive/warehouse/impala_demo.db"


# 库名。默认为 default
database_name="default"
# Hive 的 url。
hive_url="jdbc:hive2://localhost:10000/$database_name"
hive_username=""
hive_password=""
# 库的路径
db_base_path="/user/hive/warehouse"


### 读取输入参数。可以直接回车
echo ">> 直接回车将采用默认值！"
read -p "请输入需要导出的库名（默认为 default）:" DB_NAME
read -p "请输入hive 的 jdbc url（默认为 jdbc:hive2://localhost:10000/$DB_NAME）:" JDBC_URL
read -p "请输入hive 的用户名（如果没有可以直接回车跳过）:" HIVE_USER
read -p "请输入hive 的密码（如果没有可以直接回车跳过）:" HIVE_PASSD
#read -p "请输入库的位置（默认为 /user/hive/warehouse）:" DB_PATH

# 判断输入的参数是否为 null。
#  -z "$str" 字符串为 empty。  -n "$str" 字符串不为 empty。
if [ -n "$DB_NAME" ]; then
    database_name="$DB_NAME"
    hive_url="jdbc:hive2://localhost:10000/$database_name"
fi
if [ -n "$JDBC_URL" ]; then
    hive_url="$JDBC_URL"
fi
if [ -n "$HIVE_USER" ]; then
    hive_username="$HIVE_USER"
fi
if [ -n "$HIVE_PASSD" ]; then
    hive_password="$HIVE_PASSD"
fi
#if [ -n "$DB_PATH" ]; then
#    db_base_path="$DB_PATH"
#fi

#echo -e "database_name=$database_name\nhive_url=$hive_url\ndb_base_path=$db_base_path"


## 保存表的临时文件路径
#tables_path="/tmp/${database_name}_ables-${currentTimeStamp}.txt"
#
#
## 获取库下的所有表
#echo -e "==================== \t 开始导出表信息"
#
#
## desc formatted 
#beeline -n hive -p hive -d "org.apache.hive.jdbc.HiveDriver" -u $hive_url --showHeader=false --outputformat=csv2 -e "show tables" > $tables_path
#
#
## 获取表文件中所有表及表大小 
#echo "表名,大小" >>./${database_name}_result.csv
#for i in `cat $tables_path`
#do
#   echo -e "$i,\c" >>./${database_name}_result.csv
#   hadoop fs -du "${db_base_path}/$i" | awk '{ sum=$1 ;dir2=$2 ; hum[1024**3]="Gb";hum[1024**2]="Mb";hum[1024]="Kb"; for (x=1024**3; x>=1024; x/=1024){ if (sum>=x) { printf "%.2f %s \t %s\n",sum/x,hum[x],dir2;break } }}' | awk '{print $1 " " $2}' >>./${database_name}_result.csv
#   echo ">> $i 表信息生成完毕"
#done
#
## 删除本地的临时文件
#rm -rf ${tables_path}
#
#echo -e "====================\t SUCCESS!"


#hadoop fs -du /user/hive/warehouse/hive_test.db/tmp_test/ | awk '{ sum=$1 ;dir2=$2 ; hum[1024**3]="Gb";hum[1024**2]="Mb";hum[1024]="Kb"; for (x=1024**3; x>=1024; x/=1024){ if (sum>=x) { printf "%.2f %s \t %s\n",sum/x,hum[x],dir2;break } }}'
#hadoop fs -du "/user/hive/warehouse/hive_test.db/tmp_test/" | awk '{ sum=$1 ;dir2=$2 ; hum[1024**3]="Gb";hum[1024**2]="Mb";hum[1024]="Kb"; for (x=1024**3; x>=1024; x/=1024){ if (sum>=x) { printf "%.2f %s \t %s\n",sum/x,hum[x],dir2;break } }}' | awk '{print $1 " " $2}' >> aaaaa.csv
#jdbc:hive2://cdh3:10000/hive_test


###########################
###########################

java -jar hive-get-tables-info-1.0.jar $database_name $hive_url $hive_username $hive_password

echo -e "====================\t SUCCESS!"



