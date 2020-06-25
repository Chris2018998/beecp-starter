
BeeCP-Starter是小蜜蜂连接池在Springboot上的启动器。


**相关功能**

1：文件方式配置数据源信息

2：支持多数据源配置

3：可通过自定义的方式支持其他池

4：支持配置Jndi数据源引入配置

**运行依赖：**

1：Java版本：JDK1.8

2：Springboot版本：2.0.9.RELEASE

3：BeeCP版本:2.4.7


**版本下载**

<dependency>
	<groupId>com.github.chris2018998</groupId>
	<artifactId>spring-boot-starter-beecp</artifactId>
	<version>1.0.RELEASE</version>
</dependency>


**配置介绍**





**配置范例**
	
  数据源名清单(必须配置,名字是对应数据源的Ioc注册名)
  
  spring.datasource.nameList=d1,d2,d3
  
   #第1数据源
  spring.datasource.d1.primary=true  
  spring.datasource.d1.datasourceType=cn.beecp.BeeDataSoruce
  spring.datasource.d1.poolName=BeeCP1
  spring.datasource.d1.username=root
  spring.datasource.d1.password=root
  spring.datasource.d1.jdbcUrl=jdbc:mysql://localhost:3306/test
  spring.datasource.d1.driverClassName=com.mysql.cj.jdbc.Driver
  

  #第2数据源
  spring.datasource.d2.jndiName=testDB 
  
  #第3数据源（支持其他连接池）
  spring.datasource.d3.jndiName=testDB
  #实现接口:javax.sql.DataSource
  spring.datasource.d3.datasourceType=xxx.xxx.xxx
  #实现接口:cn.beecp.boot.DataSourceAttributeSetFactory
  spring.datasource.d3.datasourceAttributeSetFactory=xxxx
  spring.datasource.d3.username=root
  spring.datasource.d3.password=root
  spring.datasource.d3.jdbcUrl=jdbc:mysql://localhost:3306/test
  spring.datasource.d3.driverClassName=com.mysql.cj.jdbc.Driver
  
 
**扩展接口**
	
 //从environment属性值读取，并注入数据源对象中
 public interface DataSourceAttributeSetFactory {

    //get Properties value from environment and set to dataSource
    public void set(DataSource ds,String configPrefix,Environment environment)throws Exception;
}




  
  
