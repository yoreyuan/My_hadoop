Single Server Deployments
======
Druid包含一组用于单机部署的参考配置和启动脚本：
* `micro-quickstart`
* `small`
* `medium`
* `large`
* `xlarge`

`micro-quickstart`适用于笔记本电脑等小型机器，用于快速评估用例。

其他配置旨在用于通用单机部署。 它们的大小基本上基于亚马逊的i3系列EC2实例。

这些示例配置的启动脚本与Druid服务一起运行单个ZK实例。 您也可以选择单独部署ZK。

示例配置使用可选配置`druid.coordinator.asOverlord.enabled=true`在单个进程中一起运行Druid Coordinator和Overlord，如[Coordinator配置文档](https://druid.apache.org/docs/latest/configuration/index.html#coordinator-operation)中所述。

虽然为非常大的单机提供了示例配置，但在更高的规模上，我们建议在[集群部署](https://druid.apache.org/docs/latest/tutorials/cluster.md)中运行Druid，以实现容错和减少资源争用。

## Single Server Reference Configurations
**Micro-Quickstart**: 4 CPU, 16GB RAM  
启动命令: `bin/start-micro-quickstart` 配置目录: `conf/druid/single-server/micro-quickstart`

**Small**: 8 CPU, 64GB RAM (~i3.2xlarge)  
启动命令: `bin/start-small` 配置目录: `conf/druid/single-server/small`

**Medium**: 16 CPU, 128GB RAM (~i3.4xlarge)
启动命令: `bin/start-medium` 配置目录: `conf/druid/single-server/medium`

**Large**: 32 CPU, 256GB RAM (~i3.8xlarge)
启动命令: `bin/start-large` 配置目录: `conf/druid/single-server/large`

**X-Large**: 64 CPU, 512GB RAM (~i3.16xlarge)
启动命令: `bin/start-xlarge` 配置目录: `conf/druid/single-server/xlarge`

