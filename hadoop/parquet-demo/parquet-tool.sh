#!/usr/bin/env bash
java -Xbootclasspath/a:\
/Users/yoreyuan/soft/maven/repository/org/apache/hadoop/hadoop-common/2.7.7/hadoop-common-2.7.7.jar:\
/Users/yoreyuan/soft/maven/repository/commons-logging/commons-logging/1.1.3/commons-logging-1.1.3.jar:\
/Users/yoreyuan/soft/maven/repository/com/google/guava/guava/11.0.2/guava-11.0.2.jar:\
/Users/yoreyuan/soft/maven/repository/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar:\
/Users/yoreyuan/soft/maven/repository/commons-lang/commons-lang/2.6/commons-lang-2.6.jar:\
/Users/yoreyuan/soft/maven/repository/org/apache/hadoop/hadoop-auth/2.7.7/hadoop-auth-2.7.7.jar:\
/Users/yoreyuan/soft/maven/repository/org/slf4j/slf4j-api/1.7.5/slf4j-api-1.7.5.jar:\
/Users/yoreyuan/soft/maven/repository/org/apache/hadoop/hadoop-mapreduce-client-core/2.7.7/hadoop-mapreduce-client-core-2.7.7.jar:\
/Users/yoreyuan/soft/maven/repository/log4j/log4j/1.2.17/log4j-1.2.17.jar \
-jar parquet-tools-1.10.1.jar $1 $2 $3
