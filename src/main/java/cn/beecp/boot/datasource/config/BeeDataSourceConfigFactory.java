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
package cn.beecp.boot.datasource.config;

import cn.beecp.BeeDataSource;
import cn.beecp.BeeDataSourceConfig;
import cn.beecp.boot.datasource.DataSourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

import static cn.beecp.boot.datasource.DataSourceUtil.getConfigValue;
import static cn.beecp.boot.datasource.DataSourceUtil.setMethodAccessible;
import static cn.beecp.pool.PoolStaticCenter.*;

/*
 *  Bee Data Source Attribute Set Factory
 *
 *  spring.datasource.d1.poolName=BeeCP1
 *  spring.datasource.d1.username=root
 *  spring.datasource.d1.password=root
 *  spring.datasource.d1.jdbcUrl=jdbc:mysql://localhost:3306/test
 *  spring.datasource.d1.driverClassName=com.mysql.cj.jdbc.Driver
 *
 *  @author Chris.Liao
 */

public class BeeDataSourceConfigFactory extends DataSourceBaseConfigFactory {
    //logger
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * after Set Attributes
     *
     * @param ds           dataSource
     * @param dsId         dataSource name
     * @param configPrefix configured prefix name
     * @param environment  SpringBoot environment
     * @throws Exception when fail to set
     */
    protected void afterConfig(Object ds, String dsId, String configPrefix, Environment environment) throws Exception {

        BeeDataSource bds = (BeeDataSource) ds;
        String connectPropName = "connectProperties";
        String configVal = getConfigValue(environment, configPrefix, connectPropName);
        if (DataSourceUtil.isBlank(configVal))
            configVal = getConfigValue(environment, configPrefix, propertyNameToFieldId(connectPropName, DS_Config_Prop_Separator_MiddleLine));
        if (DataSourceUtil.isBlank(configVal))
            configVal = getConfigValue(environment, configPrefix, propertyNameToFieldId(connectPropName, DS_Config_Prop_Separator_UnderLine));
        if (!isBlank(configVal)) {
            configVal = configVal.trim();
            String[] attributeArray = configVal.split(";");
            for (String attribute : attributeArray) {
                String[] pairs = attribute.split("=");
                if (pairs.length == 2)
                    bds.addConnectProperty(pairs[0].trim(), pairs[1].trim());
            }
        }

        Method checkMethod = null;
        boolean accessibleChanged = false;
        try {//test config check
            checkMethod = BeeDataSourceConfig.class.getDeclaredMethod("check", new Class[0]);

            if (!checkMethod.isAccessible()) {
                setMethodAccessible(checkMethod, true);
                accessibleChanged = true;
            }
            checkMethod.invoke(ds, new Object[0]);
        } catch (NoSuchMethodException e) {
            log.error("Failed to check dataSource configuration", e);
            throw e;
        } catch (SecurityException e) {
            log.error("Failed to check dataSource configuration", e);
            throw e;
        } catch (IllegalAccessException e) {
            log.error("Failed to check dataSource configuration", e);
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("Failed to check dataSource configuration", e);
            throw e;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause != null) {
                log.error("Failed to check dataSource configuration", cause);
                throw new SQLException(cause);
            } else {
                throw new SQLException("Failed to check dataSource configuration:" + e.getMessage());
            }
        } finally {
            if (checkMethod != null && accessibleChanged) {
                setMethodAccessible(checkMethod, false);
            }
        }
    }
}
