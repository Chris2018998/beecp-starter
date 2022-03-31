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
import cn.beecp.boot.datasource.factory.BeeDataSourceFactory;
import cn.beecp.boot.datasource.factory.SpringBootDataSourceException;
import cn.beecp.boot.datasource.sqltrace.SqlTraceConfig;
import cn.beecp.boot.datasource.sqltrace.SqlTracePool;
import cn.beecp.pool.PoolStaticCenter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

import static cn.beecp.boot.datasource.SpringBootDataSourceUtil.*;

/*
 *  config example
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
    public DataSource beeDataSource(Environment environment) throws Exception {
        String dsId = getConfigValue(SP_DS_Prefix, SP_DS_Id, environment);
        if (PoolStaticCenter.isBlank(dsId)) dsId = "beeDs";//default ds Id

        DataSource beesDs = new BeeDataSourceFactory().createDataSource(SP_DS_Prefix, dsId, environment);
        SpringBootDataSource springDs = new SpringBootDataSource(dsId, beesDs, false);
        springDs.setTraceSQL(setupSqlTracePool(dsId, environment));
        SpringBootDataSourceCenter.getInstance().addDataSource(springDs);
        return springDs;
    }

    /**
     * config sql trace pool
     *
     * @param dsId        dataSource Id
     * @param environment Springboot environment
     * @return sql trace indicator
     */
    boolean setupSqlTracePool(String dsId, Environment environment) {
        try {
            //1:create sql trace config instance
            SqlTraceConfig config = new SqlTraceConfig();

            //2:set Properties
            setPropertiesValue(config, SP_DS_Prefix, dsId, environment);

            //3:create sql-trace pool
            SqlTracePool tracePool = SqlTracePool.getInstance();
            tracePool.init(config);
            return tracePool.isSqlTrace();
        } catch (Exception e) {
            throw new SpringBootDataSourceException("Failed to set config value to sql-trace pool", e);
        }
    }
}
