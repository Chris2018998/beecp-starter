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
package cn.beecp.boot.datasource.factory;

import cn.beecp.BeeDataSource;
import cn.beecp.BeeDataSourceConfig;
import org.springframework.core.env.Environment;

import static cn.beecp.boot.datasource.SpringBootDataSourceUtil.configDataSource;
import static cn.beecp.boot.datasource.SpringBootDataSourceUtil.getConfigValue;
import static cn.beecp.pool.PoolStaticCenter.isBlank;

/*
 *  BeeDataSource Springboot Factory
 *
 *  spring.datasource.d1.poolName=BeeCP1
 *  spring.datasource.d1.username=root
 *  spring.datasource.d1.password=root
 *  spring.datasource.d1.jdbcUrl=jdbc:mysql://localhost:3306/test
 *  spring.datasource.d1.driverClassName=com.mysql.cj.jdbc.Driver
 *
 *  @author Chris.Liao
 */
public class BeeDataSourceFactory implements SpringBootDataSourceFactory {

    public Object getObjectInstance(Environment environment, String dsId, String dsConfigPrefix) throws Exception {
        BeeDataSourceConfig config = new BeeDataSourceConfig();
        configDataSource(config, environment, dsId, dsConfigPrefix);
        setConnectPropertiesConfig(config, environment, dsConfigPrefix);

        return new BeeDataSource(config);
    }

    private void setConnectPropertiesConfig(BeeDataSourceConfig config, Environment environment, String dsConfigPrefix) {
        config.addConnectProperty(getConfigValue(environment, dsConfigPrefix, "connectProperties"));
        String connectPropertiesCount = getConfigValue(environment, dsConfigPrefix, "connectProperties.count");
        if (!isBlank(connectPropertiesCount)) {
            int count = 0;
            try {
                count = Integer.parseInt(connectPropertiesCount.trim());
            } catch (Throwable e) {
            }
            for (int i = 1; i <= count; i++)
                config.addConnectProperty(getConfigValue(environment, dsConfigPrefix, "connectProperties." + i));
        }
    }
}