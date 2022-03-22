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
class SpringDataSourceRegMap {
    private final static SpringDataSourceRegMap instance = new SpringDataSourceRegMap();
    private final ThreadLocal<String> dsIdLocal = new ThreadLocal<String>();
    private final Map<String, SpringRegDataSource> dsMap = new ConcurrentHashMap<>(2);

    public final static SpringDataSourceRegMap getInstance() {
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

    void addDataSource(SpringRegDataSource ds) {
        dsMap.put(ds.getId(), ds);
    }

    SpringRegDataSource getDataSource(String dsId) {
        return dsMap.get(dsId);
    }

    List<Map<String, Object>> getPoolMonitorVoList() {
        List<Map<String, Object>> poolMonitorVoList = new ArrayList<>(dsMap.size());
        Iterator<SpringRegDataSource> iterator = dsMap.values().iterator();
        while (iterator.hasNext()) {
            SpringRegDataSource ds = iterator.next();
            ConnectionPoolMonitorVo vo = ds.getPoolMonitorVo();
            if (vo == null) continue;
            if (vo.getPoolState() == 2) {//POOL_CLOSED
                iterator.remove();
            } else {
                Map<String, Object> poolMap = new LinkedHashMap<>(9);
                poolMap.put("dsId", ds.getId());
                poolMap.put("poolName", vo.getPoolName());
                poolMap.put("poolMode", vo.getPoolMode());
                poolMap.put("poolState", vo.getPoolState());
                poolMap.put("poolMaxSize", vo.getPoolMaxSize());
                poolMap.put("idleSize", vo.getIdleSize());
                poolMap.put("usingSize", vo.getUsingSize());
                poolMap.put("semaphoreWaitingSize", vo.getSemaphoreWaitingSize());
                poolMap.put("transferWaitingSize", vo.getTransferWaitingSize());
                poolMonitorVoList.add(poolMap);
            }
        }
        return poolMonitorVoList;
    }
}
