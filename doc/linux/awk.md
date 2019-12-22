
```bash
# shell给一个文件中的每一行开头插入字符的方法：
awk '{print "xxx"$0}' fileName

# shell给一个文件中的每一行结尾插入字符的方法：
awk '{print $0"xxx"}' fileName

# shell给一个文件中的每一行的指定列插入字符的方法：
awk '$O=$O" xxx"' fileName


# 切分文件
sed -n '1,5000'p row10000_col10000_column.csv >> row10000_col10000_column_0-5000.csv
sed -n '5001,10000'p row10000_col10000_column.csv >> row10000_col10000_column_5001-1w.csv



```