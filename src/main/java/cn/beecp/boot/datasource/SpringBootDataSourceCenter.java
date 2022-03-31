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

import cn.beecp.pool.ConnectionPoolMonitorVo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collect registered dataSource
 *
 * @author Chris.Liao
 */
class SpringBootDataSourceCenter {
    private final static SpringBootDataSourceCenter instance = new SpringBootDataSourceCenter();
    private final ThreadLocal<String> dsIdLocal = new ThreadLocal<String>();
    private final Map<String, SpringBootDataSource> dsMap = new ConcurrentHashMap<>(2);

    static SpringBootDataSourceCenter getInstance() {
        return instance;
    }

    String getCurDsId() {
        return dsIdLocal.get();
    }

    void setCurDsId(String dsId) {
        dsIdLocal.set(dsId);
    }

    void removeCurDsId() {
        dsIdLocal.remove();
    }

    void addDataSource(SpringBootDataSource ds) {
        dsMap.put(ds.getId(), ds);
    }

    SpringBootDataSource getDataSource(String dsId) {
        return dsMap.get(dsId);
    }

    List<Map<String, Object>> getPoolMonitorVoList() {
        List<Map<String, Object>> poolMonitorVoList = new ArrayList<>(dsMap.size());
        Iterator<SpringBootDataSource> iterator = dsMap.values().iterator();
        while (iterator.hasNext()) {
            SpringBootDataSource ds = iterator.next();
            ConnectionPoolMonitorVo vo = ds.getPoolMonitorVo();
            if (vo == null) continue;
            if (vo.getPoolState() == 2) {//POOL_CLOSED
                iterator.remove();
            } else {
                Map<String, Object> monitorMap = new HashMap<>(9);
                monitorMap.put("dsId", ds.getId());
                monitorMap.put("poolName", vo.getPoolName());
                monitorMap.put("poolMode", vo.getPoolMode());
                monitorMap.put("poolState", vo.getPoolState());
                monitorMap.put("poolMaxSize", vo.getPoolMaxSize());
                monitorMap.put("idleSize", vo.getIdleSize());
                monitorMap.put("usingSize", vo.getUsingSize());
                monitorMap.put("semaphoreWaitingSize", vo.getSemaphoreWaitingSize());
                monitorMap.put("transferWaitingSize", vo.getTransferWaitingSize());
                poolMonitorVoList.add(monitorMap);
            }
        }
        return poolMonitorVoList;
    }
}
