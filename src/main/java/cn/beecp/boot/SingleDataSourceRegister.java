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
package cn.beecp.boot;

import cn.beecp.BeeDataSource;
import cn.beecp.boot.monitor.DataSourceCollector;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/*
 *  SpringBoot dataSource config demo
 *  spring.datasource.type=cn.beecp.BeeDataSource
 *  spring.datasource.*=xx
 *
 *   @author Chris.Liao
 */

@Configuration
@ConditionalOnClass(cn.beecp.BeeDataSource.class)
@ConditionalOnProperty(name="spring.datasource.type",havingValue="cn.beecp.BeeDataSource")
public class SingleDataSourceRegister {
    @Bean
    @ConfigurationProperties(prefix="spring.datasource")
    public DataSource beeDataSource() throws BeansException {
        BeeDataSource ds= new BeeDataSource();
        DataSourceCollector.getInstance().addDataSource(ds);
        return ds;
    }
}
