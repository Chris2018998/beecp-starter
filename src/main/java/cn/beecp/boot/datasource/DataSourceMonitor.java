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

import cn.beecp.boot.datasource.sqltrace.SqlTracePool;
import cn.beecp.pool.ConnectionPoolMonitorVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Controller
 *
 * @author Chris.Liao
 */

@Controller
public class DataSourceMonitor {
    private final String chinese_page = "/beecp/chinese.html";
    private final String english_page = "/beecp/english.html";
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private List<Map<String, Object>> poolInfoList = new LinkedList<Map<String, Object>>();
    private TraceDataSourceMap collector = TraceDataSourceMap.getInstance();

    @RequestMapping("/beecp")
    public String welcome1() {
        return "redirect:" + chinese_page;
    }

    @RequestMapping("/beecp/")
    public String welcome2() {
        return chinese_page;
    }

    @RequestMapping("/beecp/cn")
    public String openChinesePage1() {
        return chinese_page;
    }

    @RequestMapping("/beecp/chinese")
    public String openChinesePage2() {
        return chinese_page;
    }

    @RequestMapping("/beecp/index")
    public String openChinesePage3() {
        return chinese_page;
    }

    @RequestMapping("/beecp/en")
    public String openEnglishPage1() {
        return english_page;
    }

    @RequestMapping("/beecp/english")
    public String openEnglishPage2() {
        return english_page;
    }


    /*******************************************************************************
     *                             Below are Rest methods
     *******************************************************************************/

    @ResponseBody
    @RequestMapping("/beecp/getPoolList")
    public List<Map<String, Object>> getPoolList() {
        return getPoolInfoList();
    }

    @ResponseBody
    @RequestMapping("/beecp/getSqlTraceList")
    public Object getSqTraceList() {
        return SqlTracePool.getInstance().getTraceQueue();
    }

    @ResponseBody
    @RequestMapping("/beecp/clearAllConnections")
    public void clearAllConnections(@RequestBody Map<String, String> parameterMap) {
        if (parameterMap != null) {
            TraceDataSource ds = collector.getDataSource(parameterMap.get("dsId"));
            if (ds != null) {
                try {
                    ds.clearAllConnections();
                } catch (SQLException e) {
                    log.error("Failed to reset datasource({}) connection pool", ds.getId());
                }
            }
        }
    }

    private List<Map<String, Object>> getPoolInfoList() {
        poolInfoList.clear();
        for (TraceDataSource ds : collector.getAllDataSource()) {
            ConnectionPoolMonitorVo vo = ds.getPoolMonitorVo();
            if (vo == null) continue;
            if (vo.getPoolState() == 3) {//POOL_CLOSED
                collector.removeDataSource(ds.getId());
            } else {
                Map<String, Object> poolMap = new LinkedHashMap<>(9);
                poolMap.put("dsId", ds.getId());
                poolMap.put("poolName", vo.getPoolName());
                poolMap.put("poolMode", vo.getPoolMode());
                poolMap.put("poolState", vo.getPoolState());
                poolMap.put("maxActive", vo.getMaxActive());
                poolMap.put("idleSize", vo.getIdleSize());
                poolMap.put("usingSize", vo.getUsingSize());
                poolMap.put("semaphoreWaiterSize", vo.getSemaphoreWaiterSize());
                poolMap.put("transferWaiterSize", vo.getTransferWaiterSize());
                poolInfoList.add(poolMap);
            }
        }
        return poolInfoList;
    }
}
