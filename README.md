BeeCP-Starter是小蜜蜂连接池在Springboot上的启动器


**相关功能**
---
1：文件方式配置数据源信息

2：支持多数据源配置

3：可通过自定义的方式支持其他数据源

4：支持配置Jndi数据源引


**运行依赖**
---
1：Java版本：JDK1.8

2：Springboot版本：2.0.9.RELEASE

3：BeeCP版本:2.4.7


**版本下载**
---
    <dependency>
    	<groupId>com.github.chris2018998</groupId>
    	<artifactId>spring-boot-starter-beecp</artifactId>
    	<version>1.3.2.RELEASE</version>
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
  

  参考源码工程: https://github.com/Chris2018998/BeeCP-Starter/tree/master/doc/SingleDataSourceTest.zip

**多数据源范例**
---
application.properties

    #多数据源配置起点
    spring.datasource.nameList=d1,d2,d3 
    
    #第1数据源
    spring.datasource.d1.primary=true  
    spring.datasource.d1.poolName=BeeCP1
    spring.datasource.d1.username=root
    spring.datasource.d1.password=root
    spring.datasource.d1.jdbcUrl=jdbc:mysql://localhost:3306/test
    spring.datasource.d1.driverClassName=com.mysql.cj.jdbc.Driver
     
    #第2数据源
    spring.datasource.d2.jndiName=testDB 
      
    
    #第3数据源
    spring.datasource.d3.poolName=testDB
    spring.datasource.d3.datasourceType=com.xxx.xxxDataSource
    spring.datasource.d3.datasourceAttributeSetFactory=xxxx
    spring.datasource.d3.username=root
    spring.datasource.d3.password=root
    spring.datasource.d3.jdbcUrl=jdbc:mysql://localhost:3306/test
    spring.datasource.d3.driverClassName=com.mysql.cj.jdbc.Driver
  
    #xxxx为对应连接池的属性注入工厂类的实现,请参照*扩展接口*
  
  
  
  DemoApplication.java   
     
    //引入多数据源标签
    @EnableMultiDataSource
    @SpringBootApplication
    public class DemoApplication {
      public static void main(String[] args) {
         SpringApplication.run(DemoApplication.class, args);
       }
    }
      

  参考源码工程 https://github.com/Chris2018998/BeeCP-Starter/blob/master/doc/MutilDataSourceTest.zip

**扩展接口**
---

     public interface DataSourceAttributeSetFactory {
    
       //get Properties value from environment and set to dataSource
       public void set(Object ds,String configPrefix,Environment environment)throws Exception;
    }
    



  
  
