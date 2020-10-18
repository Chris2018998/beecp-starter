BeeCP-Starter是小蜜蜂连接池在Springboot上的启动器


**相关功能**
---
1：文件方式配置数据源信息

2：支持一个或多数据源配置

3：支持配置Jndi数据源

4：扩展支持其他数据源


**运行依赖**
---
1：Java版本：JDK1.8

2：Springboot版本：2.0.9.RELEASE


**版本下载**
---
    <dependency>
    	<groupId>com.github.chris2018998</groupId>
	<artifactId>beecp-spring-boot-starter</artifactId>
	<version>1.3.7</version>
    </dependency>


**配置介绍**
---

| 配置项                        |      说明                            | 必填   |         参考数据                                           |  
|------------------------------|--------------------------------------|---------------|-----------------------------------------------------------|         
|nameList                      | 数据源配置名单表,名字作为数据源的Ioc注册名 | 是           |spring.datasource.nameList=d1,d2,d3                        |     
|datasourceType                | 数据源类名,必须含有无参构造函数           | 否(不填默认采用小蜜蜂数据源)|spring.datasource.d1.datasourceType=cn.beecp.BeeDataSoruce |         
|datasourceAttributeSetFactory | 数据源属性注入工厂类                     | 否(其他数据源必填)        |spring.datasource.d1.datasourceAttributeSetFactory=xxxx    |
|primary                       | 是否为首要数据源,不配置为false           | 否                   |spring.datasource.d1.primary=true                          |
|jndiName                      | 中间件数据源Jndi名,若配置则作为首要配置    | 否                   |spring.datasource.d2.jndiName=testDB                      |
|poolName                      | 数据源地连接池名                         | 否                   |spring.datasource.d1.poolName=BeeCP1                     |
|username                      | JDBC连接用户名                          | 是                   |spring.datasource.d1.username=root                       |
|password                      | JDBC连接用密码                          | 是                   |spring.datasource.d1.password=root                       |
|jdbcUrl                       | JDBC连接URL                            | 是                   |spring.datasource.d1.jdbcUrl=jdbc:mysql://localhost:3306/test|
|driverClassName               | JDBC连接用驱动                          | 是                   |spring.datasource.d1.driverClassName=com.mysql.cj.jdbc.Driver|

  
**单数据源范例**
---
application.properties
   
    #单数据源配置点
    spring.datasource.type=cn.beecp.BeeDataSource
    spring.datasource.poolName=BeeCP1
    spring.datasource.username=root
    spring.datasource.password=
    spring.datasource.jdbcUrl=jdbc:mysql://localhost:3306/test
    spring.datasource.driverClassName=com.mysql.jdbc.Driver
  
   下载参考代码: https://github.com/Chris2018998/BeeCP-Starter/blob/master/doc/SingleDsStarterDemo.zip

**多数据源范例**
---
application.properties

    #多数据源配置起点
    spring.datasource.nameList=ds1,ds2,ds3 
    
    #第1数据源
    spring.datasource.ds1.primary=true  
    spring.datasource.ds1.poolName=BeeCP1
    spring.datasource.ds1.username=root
    spring.datasource.ds1.password=root
    spring.datasource.ds1.jdbcUrl=jdbc:mysql://localhost:3306/test
    spring.datasource.ds1.driverClassName=com.mysql.cj.jdbc.Driver
     
    #第2数据源
    spring.datasource.ds2.jndiName=testDB 
      
    
    #第3数据源
    spring.datasource.ds3.poolName=Hikari
    spring.datasource.ds3.datasourceType=com.zaxxer.hikari.HikariDataSource 
    spring.datasource.ds3.datasourceAttributeSetFactory=cn.beecp.boot.setFactory.HikariDataSourceSetFactory
    spring.datasource.ds3.username=root
    spring.datasource.ds3.password=root
    spring.datasource.ds3.jdbcUrl=jdbc:mysql://localhost:3306/test
    spring.datasource.ds3.driverClassName=com.mysql.cj.jdbc.Driver
  
    #xxxx为对应连接池的属性注入工厂类的实现,请参照*扩展接口*

  DemoApplication.java   
     
   
    @EnableMultiDataSource   //多数据源标签
    @EnableDataSourceMonitor //开启数据源监控
    @SpringBootApplication
    public class DemoApplication {
      public static void main(String[] args) {
         SpringApplication.run(DemoApplication.class, args);
       }
    }
      

  下载参考代码 https://github.com/Chris2018998/BeeCP-Starter/blob/master/doc/MutilDsStarterDemo.zip

**扩展接口**
---

```java
  public interface DataSourceAttributeSetFactory {
    
     //get Properties value from environment and set to dataSource
     public void setAttributes(Object ds,String configPrefix,Environment environment)throws Exception;
  }
 ```
    
**其他数据源属性工厂实现**
---

| 数据源类名                              |      属性注入工厂                                        | 
|----------------------------------------|--------------------------------------------------------|
|com.zaxxer.hikari.HikariDataSource      |  cn.beecp.boot.setFactory.HikariDataSourceSetFactory   | 
|com.alibaba.druid.pool.DruidDataSource  |  cn.beecp.boot.setFactory.DruidDataSourceSetFactory     | 
|org.apache.tomcat.jdbc.pool.DataSource  |  cn.beecp.boot.setFactory.TomcatJdbcDataSourceSetFactory | 


**数据源池监控**
---

在打开监控标签后，访问页面的地址为:http://IP:port/xxxx/dsMonitorView_zh.html  （其中xxxx为项目名）效果页面如下
   
<img height="100%" width="100%" src="https://github.com/Chris2018998/BeeCP-Starter/blob/master/doc/monitorView.png"></img>

    










  
  
