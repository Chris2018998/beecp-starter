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

import cn.beecp.boot.monitor.sqltrace.SqlTracePool;
import cn.beecp.pool.ConnectionPoolMonitorVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Restful Controller
 *
 * @author Chris.Liao
 */

@RestController
@RequestMapping("/dsMonitor")
public class BeeDataSourceMonitor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private List<Map<String,Object>> poolInfoList = new LinkedList<Map<String,Object>>();

    @RequestMapping("/getPoolList")
    public List<Map<String,Object>> getPoolList() {
        return getPoolInfoList();
    }

    @RequestMapping("/getSqlTraceList")
    public Object getSqTraceList() {
        return SqlTracePool.getInstance().getTraceQueue();
    }

    private List<Map<String,Object>> getPoolInfoList() {
        poolInfoList.clear();
        BeeDataSourceCollector collector = BeeDataSourceCollector.getInstance();
        for (BeeDataSourceWrapper ds : collector.getAllDataSource()) {
            try {
                ConnectionPoolMonitorVo vo = ds.getPoolMonitorVo();
                if (vo.getPoolState() == 3) {//POOL_CLOSED
                    collector.removeDataSource(ds.getDsName());
                } else {
                    Map<String,Object>poolMap=new LinkedHashMap<>(9);
                    poolMap.put("dsName",ds.getDsName());
                    poolMap.put("poolName",vo.getPoolName());
                    poolMap.put("poolMode",vo.getPoolMode());
                    poolMap.put("poolState",vo.getPoolState());
                    poolMap.put("maxActive",vo.getMaxActive());
                    poolMap.put("idleSize",vo.getIdleSize());
                    poolMap.put("usingSize",vo.getUsingSize());
                    poolMap.put("semaphoreWaiterSize",vo.getSemaphoreWaiterSize());
                    poolMap.put("transferWaiterSize",vo.getTransferWaiterSize());
                    poolInfoList.add(poolMap);
                }
            } catch (Exception e) {
                log.info("Failed to get dataSource monitor info", e);
            }
        }
        return poolInfoList;
    }
}
