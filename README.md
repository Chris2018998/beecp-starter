![图片](https://user-images.githubusercontent.com/32663325/154847136-10e241ae-af4c-478a-a608-aaa685e0464b.png)
<p align="left">
 <a><img src="https://img.shields.io/badge/JDK-1.8+-green.svg"></a>
 <a><img src="https://img.shields.io/badge/Springboot-2.0.9+-blue.svg"></a>
 <a><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg"></a>
 <a><img src="https://maven-badges.herokuapp.com/maven-central/com.github.chris2018998/beecp-spring-boot-starter/badge.svg"></a>
</p>

## :coffee: 简介
BeeCP-Starter是<a href="https://github.com/Chris2018998/BeeCP">BeeCP(小蜜蜂连接池)</a>在Springboot的数据源管理工具

[*如果您在寻找一款更专业性数据源管理工具，推荐使用Baomidou动态数据源启动器(https://github.com/baomidou/dynamic-datasource-spring-boot-starter)*]

## :arrow_down: 下载 
Maven坐标(Java8)
```xml
<dependency>
   <groupId>com.github.chris2018998</groupId>
   <artifactId>beecp-spring-boot-starter</artifactId>
   <version>1.6.6</version>
</dependency>
```

## :computer: 监控画面
监控地址:http://IP:port/xxxx/beecp 可打开监控界面（其中xxxx为项目部署名）
   
![图片](https://user-images.githubusercontent.com/32663325/153717085-00c35733-604a-4287-be1a-9b8cd42df9d5.png)

![图片](https://user-images.githubusercontent.com/32663325/153717101-3f82894a-62ba-4686-a78b-98656aedf619.png)

![图片](https://user-images.githubusercontent.com/32663325/153717113-d47d85bf-b1db-4e80-9844-d4d4fe9adf32.png)

## :book: 应用标签
| 标签                     | 备注                                                                 |
| ----------------------- | ------------------------------------------------------------------   |
|@EnableMultiDs           |多数据源启用标签，一定要配置在@SpringBootApplication<strong>之前</strong> |
|@EnableDsMonitor         |连接池监控启用标签，否则监控界面无法打开                                   |
|@DsId                    |组合数据源应用时，可指定数据源id                                          |

## :book: 数据源配置项   
### :capital_abcd: dsId
数据源Id,作为BeanId注册进Spring容器，在多源（@EnableMultiDataSource）时可以配置多个，用逗号隔开例如：ds1,ds2
### :1234: type
数据源类名，如果不填写则默认为：cn.beecp.BeeDataSource,此项配置可用于支持其他数据源
#### :capital_abcd: primary
是否注册为默认数据标记
### :1234: jndiName
数据源Jndi名，数据源来自部署容器本身，此项配置与type配置互斥
:sunny: *更多属性项，请参照<a href="https://github.com/Chris2018998/BeeCP/blob/master/README_ZH.md">BeeCP</a>属性清单*
## :book: 监控项配置 
### :capital_abcd: spring.datasource.monitorUserId
监控登陆用户Id，此项不配置则表示无需登陆
### :1234: spring.datasource.monitorPassword
监控登陆用户口令
### :capital_abcd: spring.datasource.sql-trace
sql执行监控开关，true则表示打开

### :1234: spring.datasource.sql-show
后端是否打印sql的开关
### :capital_abcd: spring.datasource.sql-trace-max-size
sql监控池的大小（1000以内）
### :1234: spring.datasource.sql-trace-timeout
sql处于监控池的最大时间，单位：毫秒
### :capital_abcd: spring.datasource.sql-exec-slow-time
低效SqL执行的时间阀值，单位：毫秒
### :capital_abcd: spring.datasource.sql-trace-timeout-scan-period
sql监控池定时扫描间隔时间，在池中时间大于sql-trace-timeout则被清理，单位：毫秒
### :1234: spring.datasource.sql-exec-alert-action
sql执行预警触发类名（需要扩展类：cn.beecp.boot.datasource.statement.StatementTraceAlert），低效与错误sql触发

## :point_right: 参考例子
```yml
spring.datasource.sql-trace=true                      #开启动SQL监控(默认为True)
spring.datasource.sql-show=true                       #是否打印SQL
spring.datasource.sql-trace-max-size=100              #SQL执行跟踪的个数
spring.datasource.sql-trace-timeout=60000             #SQL执行跟踪最大时间 （毫秒） 
spring.datasource.sql-exec-slow-time=5000             #SQL执行时间警告值（毫秒） 
spring.datasource.sql-trace-timeout-scan-period=18000 #SQL执行跟踪扫描时间 （毫秒）
spring.datasource.sql-exec-alert-action=xxxxx         #SQL执行时间预警值类名（需要扩展类：cn.beecp.boot.datasource.statement.StatementTraceAlert)

```

## :tractor: 单源例子
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


## :tractor: 多源例子
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
