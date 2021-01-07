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
package cn.beecp.boot.monitor;

import java.util.HashMap;
import java.util.Map;

/**
 * Collect Bee dataSource
 *
 * @author Chris.Liao
 */
public class BeeDataSourceCollector {
    private final static BeeDataSourceCollector single = new BeeDataSourceCollector();
    private boolean setted;
    private Map<String, BeeDataSourceWrapper> dataSourceMap = new HashMap<>();

    public final static BeeDataSourceCollector getInstance() {
        return single;
    }

    public void setDataSourceMap(Map<String, BeeDataSourceWrapper> dataSourceMap) {
        if (!setted) {
            setted = true;
            this.dataSourceMap = dataSourceMap;
        }
    }

    void removeDataSource(String dsName) {
        dataSourceMap.remove(dsName);
    }

    BeeDataSourceWrapper getDataSource(String dsName) {
        return dataSourceMap.get(dsName);
    }

    BeeDataSourceWrapper[] getAllDataSource() {
        return dataSourceMap.values().toArray(new BeeDataSourceWrapper[dataSourceMap.size()]);
    }
}
