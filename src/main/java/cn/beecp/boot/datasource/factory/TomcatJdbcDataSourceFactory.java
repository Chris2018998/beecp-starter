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

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.core.env.Environment;

import static cn.beecp.boot.datasource.SpringBootDataSourceUtil.configDataSource;

/*
 *  Tomcat-JDBC DataSource Configuration Set Factory
 *
 *  @author Chris.Liao
 */
public class TomcatJdbcDataSourceFactory implements SpringBootDataSourceFactory {
    public Object getObjectInstance(Environment environment, String dsId, String dsConfigPrefix) throws Exception {
        PoolProperties p = new PoolProperties();
        configDataSource(p, environment, dsId, dsConfigPrefix);

        org.apache.tomcat.jdbc.pool.DataSource datasource = new org.apache.tomcat.jdbc.pool.DataSource();
        datasource.setPoolProperties(p);
        return datasource;
    }
}
