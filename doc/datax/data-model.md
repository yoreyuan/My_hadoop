# HDFS -> HDFS
```json
{
  "job": {
    "content": [
      {
        "reader": {
          "name": "hdfsreader",
          "parameter": {
            "column": [
              "*"
            ],
            "defaultFS": "hdfs://cdh1:8020",
            "encoding": "UTF-8",
            "fieldDelimiter": ",",
            "fileType": "TEXT",
            "path": "/user/hive/warehouse/impala_test.db/*"
          }
        },
        "writer": {
          "name": "hdfswriter",
          "parameter": {
            "defaultFS": "hdfs://cdh6:8020",
            "column": [
              {
                "name": "col1",
                "type": "TINYINT"
              },
              {
                "name": "col2",
                "type": "SMALLINT"
              },
              {
                "name": "col3",
                "type": "INT"
              },
              {
                "name": "col4",
                "type": "BIGINT"
              },
              {
                "name": "col5",
                "type": "FLOAT"
              },
              {
                "name": "col6",
                "type": "DOUBLE"
              },
              {
                "name": "col7",
                "type": "STRING"
              },
              {
                "name": "col8",
                "type": "VARCHAR"
              },
              {
                "name": "col9",
                "type": "CHAR"
              },
              {
                "name": "col10",
                "type": "BOOLEAN"
              },
              {
                "name": "col11",
                "type": "date"
              },
              {
                "name": "col12",
                "type": "TIMESTAMP"
              }
            ],
            "compress": "NONE",
            "fieldDelimiter": "$",
            "fileName": "htgy_2017",
            "fileType": "ORC",
            "path": "/odps/sdata_chq/s010_acct_loan",
            "writeMode": "append"
          }
        }
      }
    ],
    "setting": {
      "speed": {
        "channel": "3"
      }
    }
  }
}

```

JOSN2。text不带压缩的
```json
{
  "job": {
    "setting": {
      "speed": {
        "channel": 1
      }
    },
    "content": [
      {
        "reader": {
          "name": "hdfsreader",
          "parameter": {
            "path": "/user/hive/warehouse/hive_test.db/dataxtype_test/*",
            "defaultFS": "hdfs://cdh1:8020",
            "fileType": "text",
            "encoding": "UTF-8",
            "fieldDelimiter": "|",
            "column": [
              {
                "type": "Long",
                "index": "0"
              },
              {
                "type": "String",
                "index": "1"
              },
              {
                "type": "Boolean",
                "index": "2"
              },
              {
                "type": "Double",
                "index": "3"
              },
              {
                "type": "Double",
                "index": "4"
              },
              {
                "type": "Date",
                "index": "5"
              }
            ]
          }
        },
        "writer": {
          "name": "hdfswriter",
          "parameter": {
            "defaultFS": "hdfs://cdh1:8020",
            "fileType": "TEXT",
            "path": "/user/hive/warehouse/kudu_test.db/dataxtype_test",
            "fileName": "xxxx",
            "writeMode": "append",
            "fieldDelimiter": "?",
            "column": [
              {
                "name": "f1",
                "type": "BIGINT"
              },
              {
                "name": "f2",
                "type": "STRING"
              },
              {
                "name": "f3",
                "type": "BOOLEAN"
              },
              {
                "name": "f4",
                "type": "DOUBLE"
              },
			  {
                "name": "f5",
                "type": "FLOAT"
              },
              {
                "name": " f6",
                "type": "TIMESTAMP"
              }
            ]
          }
        }
      }
    ]
  }
}
```


# 
datax.py -r mongodbreader -w hdfswriter
```json
{
    "job": {
        "content": [
            {
                "reader": {
                    "name": "mongodbreader",
                    "parameter": {
                        "address": ["127.0.0.1:27017"],
                        "userName": "",
                        "userPassword": "",
                        "dbName": "",
                        "collectionName": "",
                         "column": []
                    }
                },
                "writer": {
                    "name": "hdfswriter",
                    "parameter": {
                        "column": [],
                        "compress": "",
                        "defaultFS": "",
                        "fieldDelimiter": "",
                        "fileName": "",
                        "fileType": "",
                        "path": "",
                        "writeMode": ""
                    }
                }
            }
        ],
        "setting": {
            "speed": {
                "channel": ""
            }
        }
    }
}


```



{
  "job": {
    "content": [
      {
        "reader": {
          "name": "mongodbreader",
          "parameter": {
            "address": [
              "192.168.100.170:27017"
            ],
            "userName": "",
            "userPassword": "",
            "dbName": "yg_mongo",
            "collectionName": "m_cpws_info_w3",
            "column": [
              {
                "name": "_id",
                "type": "string"
              },
              {
                "name": "relater_cert_no",
                "type": "string"
              },
              {
                "name": "relater_name",
                "type": "string"
              },
              {
                "name": "borrower_cert_no",
                "type": "string"
              },
              {
                "name": "borrower_name",
                "type": "string"
              },
              {
                "name": "relater_flag",
                "type": "string"
              },
              {
                "name": "cpws_json",
                "type": "string"
              }
            ]
          }
        },
        "writer": {
          "name": "hdfswriter",
          "parameter": {
            "defaultFS": "file:///",
            "compress": "NONE",
            "fieldDelimiter": "╬",
            "fileName": "m_cpws_info_w3",
            "fileType": "ORC",
            "path": "/root/mongo_data",
            "writeMode": "append",
            "column": [
              {
                "name": "_id",
                "type": "string"
              },
              {
                "name": "relater_cert_no",
                "type": "string"
              },
              {
                "name": "relater_name",
                "type": "string"
              },
              {
                "name": "borrower_cert_no",
                "type": "string"
              },
              {
                "name": "borrower_name",
                "type": "string"
              },
              {
                "name": "relater_flag",
                "type": "string"
              },
              {
                "name": "cpws_json",
                "type": "string"
              }
            ]
          }
        }
      }
    ],
    "setting": {
      "speed": {
        "channel": ""
      }
    }
  }
}

create table m_cpws_info_w3(
`_id` string,
relater_cert_no string,
relater_name string,
borrower_cert_no string,
borrower_name string,
relater_flag string,
cpws_json string
)ROW FORMAT DELIMITED FIELDS TERMINATED BY '╬'
STORED AS orc;

LOAD DATA LOCAL INPATH '/root/mongo_data/*'  OVERWRITE INTO TABLE m_cpws_info_w3;