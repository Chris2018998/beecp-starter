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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

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

public class BeeDataSourceSetFactory extends BaseDataSourceSetFactory {
    //logger
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * return config field
     */
    public Field[] getConfigFields() {
        List<Field> attributeList = new LinkedList<Field>();
        Class configClass = BeeDataSourceConfig.class;
        Field[] fields = configClass.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            if ("checked".equals(fieldName)
                    || "connectionFactory".equals(fieldName))
                continue;
            attributeList.add(field);
        }

        return attributeList.toArray(new Field[attributeList.size()]);
    }

    /**
     * get Properties values from environment and set to dataSource
     *
     * @param ds             dataSource
     * @param dsId           dataSource name
     * @param field          attributeFiled
     * @param attributeValue SpringBoot environment
     * @throws Exception when fail to set
     */
    protected void setField(Object ds, String dsId, Field field, String attributeValue, Environment environment) throws Exception {
        if ("connectProperties".equals(field.getName())) {
            Properties connectProperties = new Properties();
            attributeValue = attributeValue.trim();
            String[] attributeArray = attributeValue.split(";");
            for (String attribute : attributeArray) {
                String[] pairs = attribute.split("=");
                if (pairs.length == 2)
                    connectProperties.put(pairs[0].trim(), pairs[1].trim());
            }
            field.set(ds, new Object[]{connectProperties});
        }
    }

    /**
     * after Set Attributes
     *
     * @param ds           dataSource
     * @param dsId         dataSource name
     * @param configPrefix configured prefix name
     * @param environment  SpringBoot environment
     * @throws Exception when fail to set
     */
    protected void afterSetFields(Object ds, String dsId, String configPrefix, Environment environment) throws Exception {
        if (ds instanceof BeeDataSource) {//current dataSource type is BeeDataSource
            Method checkMethod = null;
            boolean accessibleChanged = false;

            try {//test config check
                checkMethod = BeeDataSourceConfig.class.getDeclaredMethod("check", new Class[0]);
                if (!checkMethod.isAccessible()) {
                    DataSourceUtil.setMethodAccessible(checkMethod, true);
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
                    DataSourceUtil.setMethodAccessible(checkMethod, false);
                }
            }
        }
    }
}
