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

import java.util.LinkedList;
import java.util.List;

/**
 * Collect Bee dataSource
 *
 * @author Chris.Liao
 */
public class DataSourceCollector {
    private final static DataSourceCollector single = new DataSourceCollector();
    private List<DataSourceWrapper> dsList = new LinkedList<DataSourceWrapper>();

    public final static DataSourceCollector getInstance() {
        return single;
    }

    public void addDataSource(DataSourceWrapper ds) {
        dsList.add(ds);
    }

    public void removeDataSource(DataSourceWrapper ds) {
        dsList.remove(ds);
    }

    public DataSourceWrapper[] getAllDataSource() {
        return dsList.toArray(new DataSourceWrapper[dsList.size()]);
    }
}
