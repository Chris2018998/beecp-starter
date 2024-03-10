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
package org.stone.beecp.springboot;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.stone.beecp.BeeDataSource;
import org.stone.beecp.springboot.factory.BeeDataSourceFactory;

import javax.sql.DataSource;

import static org.stone.tools.CommonUtil.isBlank;

/*
 * config example
 *
 * spring.datasource.dsId=beeDs
 * spring.datasource.type=org.stone.beecp.BeeDataSource
 * spring.datasource.username=root
 * spring.datasource.password=
 * spring.datasource.jdbcUrl=jdbc:mysql://localhost:3306/test
 * spring.datasource.driverClassName=com.mysql.jdbc.Driver
 * spring.datasource.fairMode=true
 * spring.datasource.initialSize=10
 * spring.datasource.maxActive =10
 *
 * @author Chris Liao
 */
@ConditionalOnClass(BeeDataSource.class)
@ConditionalOnProperty(name = "spring.datasource.type", havingValue = "org.stone.beecp.BeeDataSource")
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
public class SingleDataSourceRegister {
    @Bean
    public DataSource beeDataSource(Environment environment) throws Exception {
        //1:read ds Id
        String dsId = SpringBootDataSourceUtil.getConfigValue(SpringBootDataSourceUtil.Config_DS_Prefix, SpringBootDataSourceUtil.Config_DS_Id, environment);
        if (isBlank(dsId)) dsId = "beeDataSource";//default ds Id

        //2:read datasource monitor config
        DataSourceMonitorConfig dataSourceMonitorConfig = SpringBootDataSourceUtil.readMonitorConfig(environment);

        //3:setup monitor config
        SpringBootDataSourceManager.getInstance().setupMonitorConfig(dataSourceMonitorConfig);

        //4:create BeeDataSource
        DataSource ds = new BeeDataSourceFactory().createDataSource(SpringBootDataSourceUtil.Config_DS_Prefix, dsId, environment);
        SpringBootDataSource springDs = new SpringBootDataSource(dsId, ds, false);
        SpringBootDataSourceManager.getInstance().addSpringBootDataSource(springDs);

        return springDs;
    }
}
