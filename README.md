<img height="20px" width="20px" align="bottom" src="https://github.com/Chris2018998/BeeCP/blob/master/doc/individual/bee.png"></img>
<p align="left">
 <a><img src="https://img.shields.io/badge/JDK-1.8+-green.svg"></a>
 <a><img src="https://img.shields.io/badge/Springboot-2.0.9+-blue.svg"></a>
 <a><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg"></a>
 <a><img src="https://maven-badges.herokuapp.com/maven-central/com.github.chris2018998/beecp-spring-boot-starter/badge.svg"></a>
</p>

## :coffee: 简介

BeeCP-Starter是<a href="https://github.com/Chris2018998/BeeCP">BeeCP(小蜜蜂连接池)</a>在Springboot的数据源管理工具

[*如果您在寻找一款更专业性数据源管理工具，推荐使用Baomidou(https://github.com/baomidou/dynamic-datasource-spring-boot-starter)*]

## :arrow_down: 下载 

Maven坐标(Java8)
```xml
<dependency>
   <groupId>com.github.chris2018998</groupId>
   <artifactId>beecp-spring-boot-starter</artifactId>
   <version>1.6.0</version>
</dependency>
```

## :computer: 监控画面

监控标签启用后，通过访问地址:http://IP:port/xxxx/beecp  可打开监控界面（其中xxxx为项目部署名）
   
 <img height="50%" width="50%" src="https://github.com/Chris2018998/BeeCP-Starter/blob/master/doc/login.png"></img>
 
<img height="100%" width="100%" src="https://github.com/Chris2018998/BeeCP-Starter/blob/master/doc/monitor1.png"></img>

<img height="100%" width="100%" src="https://github.com/Chris2018998/BeeCP-Starter/blob/master/doc/monitor2.png"></img>


## :book: 标签介绍

| 标签                     | 备注                                                                 |
| ----------------------- | ------------------------------------------------------------------   |
| @EnableMultiDataSource  | 多数据源启用标签，一定要配置在@SpringBootApplication<strong>之前</strong> |
| @EnableDataSourceMonitor| 连接池监控启用标签，可通过界面实时查看连接情况和SQL执行情况                  |
| @DataSourceId           | 数据源动态切换标签                                                      |





## :capital_abcd: 配置项  
 
| 配置项                        |      说明                             | 备注                                  |
|------------------------------|-------------------------------------- |---------------------------------------|    
|dsId                          | 数据源配置名单表,名字作为数据源的Ioc注册名 | 必须提供                                |      
|type                          | 数据源类名,必须含有无参构造函数           | 其他数据源必须提供，则会默认为小蜜蜂池的配置 |
|primary                       | 是否为首要数据源,不配置为false           |                                        |
|jndiName                      | 数据源Jndi名，数据源来自部署容器本身      | 此项配置与type配置互斥                   |

## :capital_abcd: SQL监控配置

```yml
spring.datasource.sql-trace=true                      #开启动SQL监控(默认为True)
spring.datasource.sql-show=true                       #是否打印SQL
spring.datasource.sql-trace-max-size=100              #SQL执行跟踪的个数
spring.datasource.sql-trace-timeout=60000             #SQL执行跟踪最大时间 （毫秒） 
spring.datasource.sql-exec-slow-time=5000             #SQL执行时间警告值（毫秒） 
spring.datasource.sql-trace-timeout-scan-period=18000 #SQL执行跟踪扫描时间 （毫秒）
spring.datasource.sql-exec-alert-action=xxxxx         #SQL执行时间预警值类名（需要扩展类：cn.beecp.boot.datasource.sqltrace.SqlTraceAlert)

```

## 单源例子

若不启用@EnableMultiDataSource标签，启动器则自动尝试装载单源，前提系统ClassPath中存在小蜜蜂数据源类，适用于单一数据源的情况,参考配置如下

```yml
spring.datasource.type=cn.beecp.BeeDataSource
spring.datasource.poolName=BeeCP1
spring.datasource.username=root
spring.datasource.password=
spring.datasource.jdbcUrl=jdbc:mysql://localhost:3306/test
spring.datasource.driverClassName=com.mysql.jdbc.Driver
spring.datasource.fairMode=true
spring.datasource.initialSize=10
spring.datasource.maxActive=10
spring.datasource.xxx=value
```
	
<i>xxx代指数据源的配置属性名,更多属性请参照小蜜蜂连接属性列表</i>
 
完整参考代码: https://github.com/Chris2018998/BeeCP-Starter/blob/master/doc/SingleDsDemo_JPA.rar


## 多源例子

若启用@EnableMultiDataSource标签，则表示工具按多源配置的方式装载数据源，配置个数不限制，但最少一个。

application.properties文件配置

```yml
#按单加载的列表，为数据源的名字清单
spring.datasource.dsId=ds1,ds2,ds3 
    
#第1数据源
spring.datasource.ds1.primary=true  
spring.datasource.ds1.poolName=BeeCP1
spring.datasource.ds1.username=root
spring.datasource.ds1.password=root
spring.datasource.ds1.jdbcUrl=jdbc:mysql://localhost:3306/test
spring.datasource.ds1.driverClassName=com.mysql.cj.jdbc.Driver
     
#第2数据源（容器jndi数据源,不加入监控）
spring.datasource.ds2.jndiName=testDB 
      
#第3数据源(其他类型数据源,不加入监控）
spring.datasource.ds3.poolName=Hikari
#其他数据源类名，类型必须配置
spring.datasource.ds3.type=com.zaxxer.hikari.HikariDataSource 
spring.datasource.ds3.username=root
spring.datasource.ds3.password=root
spring.datasource.ds3.jdbcUrl=jdbc:mysql://localhost:3306/test
spring.datasource.ds3.driverClassName=com.mysql.cj.jdbc.Driver
```
完整参考代码：https://github.com/Chris2018998/BeeCP-Starter/blob/master/doc/MutilDsDemo_JPA.rar


## :sparkling_heart:捐助

如果您觉得此作品不错，可以捐赠请我们喝杯咖啡吧，在此表示感谢^_^。

<img height="50%" width="50%" src="https://github.com/Chris2018998/BeeCP/blob/master/doc/individual/donate.png"> 

