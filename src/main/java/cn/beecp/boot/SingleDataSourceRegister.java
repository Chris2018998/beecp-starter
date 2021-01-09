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
import cn.beecp.boot.datasource.BeeDataSourceSetFactory;
import cn.beecp.boot.monitor.BeeDataSourceCollector;
import cn.beecp.boot.monitor.BeeDataSourceWrapper;
import cn.beecp.boot.monitor.sqltrace.SqlTraceAlert;
import cn.beecp.boot.monitor.sqltrace.SqlTraceConfig;
import cn.beecp.boot.monitor.sqltrace.SqlTracePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static cn.beecp.boot.DataSourceUtil.Spring_DS_Prefix;
import static cn.beecp.boot.DataSourceUtil.getConfigValue;

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
@ConditionalOnClass(BeeDataSourceWrapper.class)
@ConditionalOnProperty(name = "spring.datasource.type", havingValue = "cn.beecp.BeeDataSource")
public class SingleDataSourceRegister {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Bean
    public DataSource beeDataSource(Environment environment) throws Exception {
        configSqlTracePool(environment);//set config properties to sql trace pool

        String dsName = "beeDataSource";
        BeeDataSource ds = new BeeDataSource();
        BeeDataSourceSetFactory dsAttrSetFactory = new BeeDataSourceSetFactory();
        dsAttrSetFactory.setFields(ds, dsName, Spring_DS_Prefix, environment);//set properties to dataSource
        BeeDataSourceWrapper dsWrapper = new BeeDataSourceWrapper(ds, dsName, SqlTracePool.getInstance().isSqlTrace());

        Map<String, BeeDataSourceWrapper> dataSourceMap = new HashMap<>(1);
        dataSourceMap.put(dsName, dsWrapper);
        BeeDataSourceCollector.getInstance().setDataSourceMap(dataSourceMap);
        return dsWrapper;
    }

    //read sql trace configuration and set then to trace pool
    protected void configSqlTracePool(Environment environment) {
        try {
            SqlTraceConfig config = new SqlTraceConfig();
            Field[] configFields = config.getClass().getDeclaredFields();
            for (Field field : configFields) {
                String configVal = getConfigValue(environment, Spring_DS_Prefix, field.getName());
                if (!DataSourceUtil.isBlank(configVal))
                    setSqlTraceConfig(field, configVal, config);
            }
            SqlTracePool.getInstance().init(config);
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
