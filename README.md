BeeCP-Starter是<a href="https://github.com/Chris2018998/BeeCP">小蜜蜂连接池</a>在Springboot的装载器，通过<strong>标签+配置文件</strong>的方式启动一个或多个数据源，同时提供监控界面。

Maven坐标(Java8)
```xml
<dependency>
   <groupId>com.github.chris2018998</groupId>
   <artifactId>beecp-spring-boot-starter</artifactId>
   <version>1.5.0</version>
</dependency>
```
---

##### 标签介绍

| 标签                     | 备注                                                                 |
| ----------------------- | ------------------------------------------------------------------   |
| @EnableMultiDataSource  | 多数据源启用标签，一定要配置在@SpringBootApplication<strong>之前</strong> |
| @EnableDataSourceMonitor| 连接池监控启用标签，可通过界面实时查看连接情况和SQL执行情况                  |
| @DataSourceId           | 数据源动态切换标签                                                      |

---

##### 单源例子

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
 
完整参考代码: https://github.com/Chris2018998/BeeCP-Starter/blob/master/doc/SingleDsStarterDemo.zip

---

#####  多源例子

若启用@EnableMultiDataSource标签，则表示工具按多源配置的方式装载数据源，配置个数不限制，但最少一个。

application.properties文件配置

```yml
#按单加载的列表，为数据源的名字清单
spring.datasource.ds-ids=ds1,ds2,ds3 
    
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
完整参考代码：https://github.com/Chris2018998/BeeCP-Starter/blob/master/doc/MutilDsStarterDemo.zip

---
 
#####  多源配置
 
| 配置项                        |      说明                             | 备注                                  |
|------------------------------|-------------------------------------- |---------------------------------------|    
|dsIds                         | 数据源配置名单表,名字作为数据源的Ioc注册名 | 必须提供                                |      
|type                          | 数据源类名,必须含有无参构造函数           | 其他数据源必须提供，则会默认为小蜜蜂池的配置 |
|fieldSetFactory               | 数据源配置属性注入工厂类                 | 其他数据源必须提供                        |
|primary                       | 是否为首要数据源,不配置为false           |                                        |
|jndiName                      | 数据源Jndi名，数据源来自部署容器本身      | 此项配置与type配置互斥                   |

数据源配置工厂如下

```java
public interface SpringBootDataSourceFactory {

    Object getObjectInstance(Environment environment, String dsId, String dsConfigPrefix) throws Exception;
}
```
---

#####  监控界面


监控标签启用后，访问页面的地址为:http://IP:port/xxxx/BeeCPMonitor.html（其中xxxx为项目名）效果页面如下
   
   
<img height="100%" width="100%" src="https://github.com/Chris2018998/BeeCP-Starter/blob/master/doc/monitor1.png"></img>


<img height="100%" width="100%" src="https://github.com/Chris2018998/BeeCP-Starter/blob/master/doc/monitor2.png"></img>

<i>黄色部分为执行时间比较长的SQL,红色部分为执行出错的SQL</i>

---

#####  SQL监控配置

```yml
spring.datasource.sql-trace=true                      #开启动SQL监控(默认为True)
spring.datasource.sql-show=true                       #是否打印SQL
spring.datasource.sql-trace-max-size=100              #SQL执行跟踪的个数
spring.datasource.sql-trace-timeout=60000             #SQL执行跟踪最大时间 （毫秒） 
spring.datasource.sql-exec-alert-time=5000            #SQL执行时间警告值（毫秒） 
spring.datasource.sql-trace-timeout-scan-period=18000 #SQL执行跟踪扫描时间 （毫秒）
spring.datasource.sql-exec-alert-action=xxxxx         #SQL执行时间预警值类名（需要扩展类：cn.beecp.boot.monitor.sqltrace.SqlTraceAlert)

```





  
  
