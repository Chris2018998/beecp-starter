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

import cn.beecp.boot.datasource.DataSourceUtil;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.beecp.boot.datasource.DataSourceUtil.SP_DS_Prefix;
import static cn.beecp.boot.datasource.DataSourceUtil.getConfigValue;
import static cn.beecp.pool.PoolStaticCenter.*;
import static cn.beecp.pool.PoolStaticCenter.DS_Config_Prop_Separator_UnderLine;

/*
 *  Data Source Base Set Factory
 *
 *  @author Chris.Liao
 */
public abstract class DataSourceBaseConfigFactory implements DataSourceConfigFactory {

    /**
     * get Properties values from environment and set to dataSource
     *
     * @param ds           dataSource
     * @param dsId         dataSource id
     * @param configPrefix configured prefix name
     * @param environment  SpringBoot environment
     * @throws Exception when fail to set
     */
    public void config(Object ds, String dsId, String configPrefix, Environment environment) throws Exception {
        Map<String, Object> setValueMap = new LinkedHashMap<String, Object>();
        Map<String, Method> setMethodMap = getSetMethodMap(ds.getClass());
        Iterator<String> iterator = setMethodMap.keySet().iterator();
        while (iterator.hasNext()) {
            String propertyName = iterator.next();
            String configVal = getConfigValue(environment, SP_DS_Prefix, propertyName);
            if (DataSourceUtil.isBlank(configVal))
                configVal = getConfigValue(environment, configPrefix, propertyNameToFieldId(propertyName, DS_Config_Prop_Separator_MiddleLine));
            if (DataSourceUtil.isBlank(configVal))
                configVal = getConfigValue(environment, configPrefix, propertyNameToFieldId(propertyName, DS_Config_Prop_Separator_UnderLine));

            if (DataSourceUtil.isBlank(configVal)) continue;
            setValueMap.put(propertyName, configVal.trim());
        }
        setPropertiesValue(ds, setMethodMap, setValueMap);

        afterConfig(ds, dsId, configPrefix, environment);
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
    protected void afterConfig(Object ds, String dsId, String configPrefix, Environment environment) throws Exception {
    }
}
