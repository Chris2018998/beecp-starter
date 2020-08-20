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
import cn.beecp.pool.ConnectionPool;
import cn.beecp.pool.ConnectionPoolMonitorVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

@RestController
public class DataSourceMonitor {
    private static Field poolField = null;
    static {
        try {
            poolField = BeeDataSource.class.getDeclaredField("pool");
            poolField.setAccessible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
    }

    //logger
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private List<ConnectionPoolMonitorVo> poolInfoList = new LinkedList<ConnectionPoolMonitorVo>();
    @RequestMapping("/dataSourceMonitor/getDataSourceInfo")
    public List<ConnectionPoolMonitorVo> getDataSourceInfo() {
        if(poolField==null)throw new java.lang.RuntimeException("Missed 'pool' field in BeeDataSource class");

        poolInfoList.clear();
        DataSourceCollector collector=DataSourceCollector.getInstance();

        BeeDataSource[] dsArray = collector.getAllDataSource();
        for (BeeDataSource ds : dsArray) {
            try {
                  ConnectionPool pool=(ConnectionPool)poolField.get(ds);
                  ConnectionPoolMonitorVo vo=pool.getMonitorVo();
                  if(vo.getPoolState()==3){//POOL_CLOSED
                      collector.removeDataSource(ds);
                  }else{
                      poolInfoList.add(vo);
                  }
            } catch (Exception e) {
                log.info("Failed to get dataSource monitor info",e);
            }
        }

        return poolInfoList;
    }
}
