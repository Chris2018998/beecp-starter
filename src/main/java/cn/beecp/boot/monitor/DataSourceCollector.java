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

import cn.beecp.BeeDataSource;

import java.util.LinkedList;
import java.util.List;

/**
 * Collect Bee dataSource
 */
public class DataSourceCollector {
    private static DataSourceCollector single=new DataSourceCollector();

    public static DataSourceCollector getInstance(){
        return single;
    }

    private List<BeeDataSource> dsList=new LinkedList<BeeDataSource>();

    public void addDataSource(BeeDataSource ds){
        dsList.add(ds);
    }

    public void removeDataSource(BeeDataSource ds){
        dsList.remove(ds);
    }

    public BeeDataSource[] getAllDataSource(){
       return dsList.toArray(new BeeDataSource[dsList.size()]);
    }
}
