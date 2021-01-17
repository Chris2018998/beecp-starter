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
import cn.beecp.boot.datasource.config.BeeDataSourceSetFactory;
import cn.beecp.boot.datasource.config.ConfigException;
import cn.beecp.boot.datasource.sqltrace.SqlTraceAlert;
import cn.beecp.boot.datasource.sqltrace.SqlTraceConfig;
import cn.beecp.boot.datasource.sqltrace.SqlTracePool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.lang.reflect.Field;

import static cn.beecp.boot.datasource.DataSourceUtil.Spring_DS_Prefix;
import static cn.beecp.boot.datasource.DataSourceUtil.getConfigValue;

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
        configSqlTracePool(environment);//set config properties to sql trace pool

        String dsId = "beeDs";
        BeeDataSource ds = new BeeDataSource();
        BeeDataSourceSetFactory dsAttrSetFactory = new BeeDataSourceSetFactory();
        dsAttrSetFactory.setFields(ds, dsId, Spring_DS_Prefix, environment);//set properties to dataSource
        boolean traceSQL = SqlTracePool.getInstance().isSqlTrace();
        TraceDataSource dsWrapper = new TraceDataSource(dsId, ds, traceSQL, false);
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
            SqlTraceConfig config = new SqlTraceConfig();
            Field[] configFields = config.getClass().getDeclaredFields();
            for (Field field : configFields) {
                String configVal = getConfigValue(environment, Spring_DS_Prefix, field.getName());
                if (!DataSourceUtil.isBlank(configVal))
                    setSqlTraceConfig(field, configVal, config);
            }

            SqlTracePool tracePool = SqlTracePool.getInstance();
            tracePool.init(config);
            return tracePool.isSqlTrace();
        } catch (Exception e) {
            throw new ConfigException("Failed to set config value to sql-trace pool", e);
        }
    }

    //set one config value to sql trace config
    private void setSqlTraceConfig(Field field, String configVal, SqlTraceConfig config) throws Exception {
        Class fieldType = field.getType();
        boolean ChangedAccessible = false;
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
                ChangedAccessible = true;
            }
            if (fieldType.equals(String.class)) {
                field.set(config, configVal);
            } else if (fieldType.equals(Boolean.class) || fieldType.equals(Boolean.TYPE)) {
                field.set(config, Boolean.valueOf(configVal));
            } else if (fieldType.equals(Integer.class) || fieldType.equals(Integer.TYPE)) {
                field.set(config, Integer.valueOf(configVal));
            } else if (fieldType.equals(Long.class) || fieldType.equals(Long.TYPE)) {
                field.set(config, Long.valueOf(configVal));
            } else if (fieldType.equals(SqlTraceAlert.class)) {
                Class actionClass = Class.forName(configVal);
                SqlTraceAlert alert = (SqlTraceAlert) actionClass.newInstance();
                field.set(config, alert);
            }
        } finally {
            if (ChangedAccessible) field.setAccessible(false);//reset field Accessible
        }
    }
}
