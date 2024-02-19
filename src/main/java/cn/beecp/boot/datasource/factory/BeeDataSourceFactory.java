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
import cn.beecp.boot.datasource.SpringBootDataSourceUtil;
import cn.beecp.jta.BeeJtaDataSource;
import org.springframework.core.env.Environment;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import static cn.beecp.boot.datasource.SpringBootDataSourceUtil.getConfigValue;
import static cn.beecp.pool.ConnectionPoolStatics.*;

/*
 *  BeeDataSource Springboot Factory
 *
 *  spring.datasource.d1.poolName=BeeCP1
 *  spring.datasource.d1.username=root
 *  spring.datasource.d1.password=root
 *  spring.datasource.d1.jdbcUrl=jdbc:mysql://localhost:3306/test
 *  spring.datasource.d1.driverClassName=com.mysql.cj.jdbc.Driver
 *
 *  @author Chris liao
 */
public class BeeDataSourceFactory implements SpringBootDataSourceFactory {

    private static void setConnectPropertiesConfig(BeeDataSourceConfig config, String dsPrefix, Environment environment) {
        config.addConnectProperty(getConfigValue(dsPrefix, CONFIG_CONNECT_PROP, environment));
        String connectPropertiesCount = getConfigValue(dsPrefix, CONFIG_CONNECT_PROP_SIZE, environment);
        if (!isBlank(connectPropertiesCount)) {
            int count = Integer.parseInt(connectPropertiesCount.trim());
            for (int i = 1; i <= count; i++)
                config.addConnectProperty(getConfigValue(dsPrefix, CONFIG_CONNECT_PROP_KEY_PREFIX + i, environment));
        }
    }

    private static void setSqlExceptionFatalConfig(BeeDataSourceConfig config, String dsPrefix, Environment environment) {
        String sqlExceptionCode = getConfigValue(dsPrefix, CONFIG_SQL_EXCEPTION_CODE, environment);
        String sqlExceptionState = getConfigValue(dsPrefix, CONFIG_SQL_EXCEPTION_STATE, environment);

        if (!isBlank(sqlExceptionCode)) {
            for (String code : sqlExceptionCode.trim().split(",")) {
                config.addSqlExceptionCode(Integer.parseInt(code));
            }
        }

        if (!isBlank(sqlExceptionState)) {
            for (String state : sqlExceptionState.trim().split(",")) {
                config.addSqlExceptionState(state);
            }
        }
    }

    public DataSource createDataSource(String dsPrefix, String dsId, Environment environment) throws Exception {
        //1:read spring configuration and inject to datasource's config object
        BeeDataSourceConfig config = new BeeDataSourceConfig();
        SpringBootDataSourceUtil.setConfigPropertiesValue(config, dsPrefix, dsId, environment);
        setConnectPropertiesConfig(config, dsPrefix, environment);
        setSqlExceptionFatalConfig(config, dsPrefix, environment);

        //2:try to lookup TransactionManager by jndi
        TransactionManager tm = null;
        String tmJndiName = getConfigValue(dsPrefix, CONFIG_TM_JNDI, environment);
        if (!isBlank(tmJndiName)) {
            Context nameCtx = new InitialContext();
            tm = (TransactionManager) nameCtx.lookup(tmJndiName);
        }

        //3:create dataSource instance
        BeeDataSource ds = new BeeDataSource(config);
        return (tm != null) ? new BeeJtaDataSource(ds, tm) : ds;
    }
}