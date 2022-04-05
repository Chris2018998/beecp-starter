/*
 * Copyright Chris2018998
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.beecp.boot.datasource;

import cn.beecp.BeeDataSource;
import cn.beecp.boot.datasource.factory.SpringBootDataSourceException;
import cn.beecp.pool.PoolStaticCenter;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

import static cn.beecp.boot.datasource.SpringBootDataSourceUtil.*;

/*
 * config example
 *
 * spring.datasource.dsId=beeDs
 * spring.datasource.type=cn.beecp.BeeDataSource
 * spring.datasource.username=root
 * spring.datasource.password=
 * spring.datasource.jdbcUrl=jdbc:mysql://localhost:3306/test
 * spring.datasource.driverClassName=com.mysql.jdbc.Driver
 * spring.datasource.fairMode=true
 * spring.datasource.initialSize=10
 * spring.datasource.maxActive =10
 *
 * @author Chris.Liao
 */
@ConditionalOnClass(BeeDataSource.class)
@ConditionalOnProperty(name = "spring.datasource.type", havingValue = "cn.beecp.BeeDataSource")
public class SingleDataSourceRegister {
    @Bean
    public DataSource beeDataSource(Environment environment, BeanDefinitionRegistry registry) throws Exception {
        //1:read ds Id
        String dsId = getConfigValue(Config_DS_Prefix, Config_DS_Id, environment);
        if (PoolStaticCenter.isBlank(dsId)) dsId = BeeCP_DS_ID;//default ds Id
        if (existsBeanDefinition(dsId, registry))
            throw new SpringBootDataSourceException("Duplicate spring bean id:" + dsId);

        //2:create BeeDataSource
        DataSource beesDs = BeeDataSourceFactory.createDataSource(Config_DS_Prefix, dsId, environment);
        SpringBootDataSource springDs = new SpringBootDataSource(dsId, beesDs, false);

        //3:add dataSource to manager
        SpringBootDataSourceManager.getInstance().setupSqlTraceConfig(environment);
        //4:add to manager
        SpringBootDataSourceManager.getInstance().addSpringBootDataSource(springDs);
        return springDs;
    }
}
