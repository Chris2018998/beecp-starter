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
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static cn.beecp.boot.datasource.SpringBootDataSourceUtil.*;
import static cn.beecp.pool.PoolStaticCenter.getSetMethodMap;
import static cn.beecp.pool.PoolStaticCenter.setPropertiesValue;

/*
 *  config example
 *
 * spring.datasource.type=cn.beecp.BeeDataSource
 * spring.datasource.username=root
 * spring.datasource.password=
 * spring.datasource.jdbcUrl=jdbc:mysql://localhost:3306/test
 * spring.datasource.driverClassName=com.mysql.jdbc.Driver
 * spring.datasource.fairMode=true
 * spring.datasource.initialSize=10
 * spring.datasource.maxActive = 10
 *
 * @author Chris.Liao
 */
@ConditionalOnClass(BeeDataSource.class)
@ConditionalOnProperty(name = "spring.datasource.type", havingValue = "cn.beecp.BeeDataSource")
public class SingleDataSourceRegister {
    @Bean
    public DataSource beeDataSource(Environment environment) throws Exception {
        String dsId = "beeDs";
        boolean traceSQL = configSqlTracePool(environment);
        BeeDataSourceFactory dsFactory = new BeeDataSourceFactory();
        XADataSource ds = (XADataSource) dsFactory.getObjectInstance(environment, dsId, SP_DS_Prefix);

        TraceDataSource dsWrapper = new TraceXDataSource(dsId, ds, traceSQL, false);
        TraceDataSourceMap.getInstance().addDataSource(dsWrapper);
        return dsWrapper;
    }

    /**
     * config sql trace pool
     *
     * @param environment Springboot environment
     * @return sql trace indicator
     */
    protected boolean configSqlTracePool(Environment environment) {
        try {
            //1:create sql trace config instance
            SqlTraceConfig config = new SqlTraceConfig();
            //2:get all properties set methods
            Map<String, Method> setMethodMap = getSetMethodMap(config.getClass());
            //3:create properties to collect config value
            Map<String, Object> setValueMap = new HashMap<String, Object>(setMethodMap.size());
            //4:loop to find config value by properties map
            Iterator<String> iterator = setMethodMap.keySet().iterator();
            while (iterator.hasNext()) {
                String propertyName = iterator.next();
                String configVal = getConfigValue(environment, SP_DS_Prefix, propertyName);
                if (SpringBootDataSourceUtil.isBlank(configVal)) continue;
                setValueMap.put(propertyName, configVal.trim());
            }
            if (!setValueMap.isEmpty()) {
                //5:inject found config value to ds config object
                setPropertiesValue(config, setMethodMap, setValueMap);
            }

            //6:read admin account and password
            setAdminInfo(environment);

            //7:create sql-trace pool
            SqlTracePool tracePool = SqlTracePool.getInstance();
            tracePool.init(config);
            return tracePool.isSqlTrace();
        } catch (Exception e) {
            throw new SpringBootDataSourceException("Failed to set config value to sql-trace pool", e);
        }
    }

    /**
     * read admin info
     *
     * @param environment Springboot environment
     */
    protected void setAdminInfo(Environment environment) {
        String adminName = getConfigValue(environment, SP_DS_Prefix, SP_DS_Monitor_UserId);
        String adminPassword = getConfigValue(environment, SP_DS_Prefix, SP_DS_Monitor_Password);
        DataSourceMonitorAdmin admin = DataSourceMonitorAdmin.singleInstance;
        admin.setUserId(adminName);
        admin.setPassword(adminPassword);
    }
}
