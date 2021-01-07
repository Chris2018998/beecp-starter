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

import cn.beecp.boot.ConfigException;
import cn.beecp.boot.DataSourceFieldSetFactory;
import cn.beecp.boot.DataSourceUtil;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;

import static cn.beecp.boot.DataSourceUtil.getConfigValue;

/*
 *  Data Source Base Set Factory
 *
 *  @author Chris.Liao
 */
public abstract class BaseDataSourceSetFactory implements DataSourceFieldSetFactory {

    /**
     * @return config fields
     */
    public abstract Field[] getConfigFields();

    /**
     * get Properties values from environment and set to dataSource
     *
     * @param ds           dataSource
     * @param dsName       dataSource name
     * @param configPrefix configured prefix name
     * @param environment  SpringBoot environment
     * @throws Exception when fail to set
     */
    public void setFields(Object ds, String dsName, String configPrefix, Environment environment) throws Exception {
        Field[] fields = getConfigFields();
        for (Field field : fields) {
            String configVal = getConfigValue(environment, configPrefix, field.getName());
            if (DataSourceUtil.isBlank(configVal)) continue;

            configVal = configVal.trim();
            Class fieldType = field.getType();
            boolean ChangedAccessible = false;
            try {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                    ChangedAccessible = true;
                }

                if (fieldType.equals(String.class)) {
                    field.set(ds, configVal);
                } else if (fieldType.equals(Boolean.class) || fieldType.equals(Boolean.TYPE)) {
                    field.set(ds, Boolean.valueOf(configVal));
                } else if (fieldType.equals(Integer.class) || fieldType.equals(Integer.TYPE)) {
                    field.set(ds, Integer.valueOf(configVal));
                } else if (fieldType.equals(Long.class) || fieldType.equals(Long.TYPE)) {
                    field.set(ds, Long.valueOf(configVal));
                } else {
                    setField(ds, dsName, field, configVal, environment);
                }
            } catch (Exception e) {
                throw new ConfigException("Failed to inject field(" + field.getName() + ") on dataSource(" + dsName + ")", e);
            } finally {
                if (ChangedAccessible) field.setAccessible(false);//reset
            }
        }

        afterSetFields(ds, dsName, configPrefix, environment);
    }

    /**
     * set complex properties values from environment and set to dataSource
     *
     * @param ds             dataSource
     * @param dsName         dataSource name
     * @param field          attributeFiled
     * @param attributeValue attributeFiled value
     * @param environment    SpringBoot environment
     * @throws Exception when fail to set
     */
    protected void setField(Object ds, String dsName, Field field, String attributeValue, Environment environment) throws Exception {
    }

    /**
     * after Set Attributes
     *
     * @param ds           dataSource
     * @param dsName       dataSource name
     * @param configPrefix configured prefix name
     * @param environment  SpringBoot environment
     * @throws Exception when fail to set
     */
    protected void afterSetFields(Object ds, String dsName, String configPrefix, Environment environment) throws Exception {
    }
}
