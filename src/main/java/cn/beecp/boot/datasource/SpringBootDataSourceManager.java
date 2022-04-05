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

import cn.beecp.boot.datasource.statement.StatementTrace;
import cn.beecp.boot.datasource.statement.StatementTraceAlert;
import cn.beecp.boot.datasource.statement.StatementTraceConfig;
import cn.beecp.pool.ConnectionPoolMonitorVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;

import static cn.beecp.boot.datasource.SpringBootDataSourceUtil.*;
import static cn.beecp.pool.PoolStaticCenter.POOL_CLOSED;

/*
 * @author Chris.Liao
 *
 *  Sql statement pool
 *  spring.datasource.monitor-login=true
 *  spring.datasource.monitor-user=admin
 *  spring.datasource.monitor-password=admin
 *
 *  spring.datasource.sql-statement=true
 *  spring.datasource.sql-show=true
 *  spring.datasource.sql-statement-max-size=100
 *  spring.datasource.sql-exec-slow-time=5000
 *  spring.datasource.sql-statement-timeout=60000
 *  spring.datasource.sql-exec-alert-action=xxxxx
 *  spring.datasource.sql-statement-timeout-scan-period=18000
 */
public class SpringBootDataSourceManager {
    private final static SpringBootDataSourceManager instance = new SpringBootDataSourceManager();
    private final ThreadLocal<String> dsIdLocal;
    private final Map<String, SpringBootDataSource> dsMap;
    private final Logger Log = LoggerFactory.getLogger(SpringBootDataSourceManager.class);

    private boolean sqlShow;
    private boolean sqlTrace;
    private long sqlExecSlowTime;
    private long sqlTraceTimeout;
    private int sqlTraceMaxSize;
    private StatementTraceAlert sqlTraceAlert;
    private LinkedList<StatementTrace> sqlAlertTempList;
    private LinkedBlockingQueue<StatementTrace> sqlTraceQueue;

    private SpringBootDataSourceManager() {
        this.dsIdLocal = new ThreadLocal<>();
        this.dsMap = new ConcurrentHashMap<>(1);
    }

    public static SpringBootDataSourceManager getInstance() {
        return instance;
    }

    void removeCurrentDsId() {
        dsIdLocal.remove();
    }

    String getCurrentDsId() {
        return dsIdLocal.get();
    }

    void setCurrentDsId(String dsId) {
        dsIdLocal.set(dsId);
    }

    SpringBootDataSource getSpringBootDataSource(String dsId) {
        return dsMap.get(dsId);
    }

    void addSpringBootDataSource(SpringBootDataSource ds) {
        dsMap.put(ds.getId(), ds);
        ds.setTraceSql(sqlTrace);
    }

    //create sql statement pool
    void setupSqlTraceConfig(Environment environment) {
        //1:create sql statement config instance
        StatementTraceConfig config = new StatementTraceConfig();
        //2:set Properties
        setConfigPropertiesValue(config, Config_DS_Prefix, null, environment);
        //3:set sql trace properties
        if (sqlTrace = config.isSqlTrace()) {
            this.sqlShow = config.isSqlShow();
            this.sqlExecSlowTime = config.getSqlExecSlowTime();
            this.sqlTraceMaxSize = config.getSqlTraceMaxSize();
            this.sqlTraceTimeout = config.getSqlTraceTimeout();
            this.sqlAlertTempList = new LinkedList<>();
            this.sqlTraceAlert = config.getSqlExecAlertAction();
            this.sqlTraceQueue = new LinkedBlockingQueue<StatementTrace>(sqlTraceMaxSize);

            ScheduledThreadPoolExecutor timeoutScanExecutor = new ScheduledThreadPoolExecutor(1, new TimeoutScanThreadThreadFactory());
            timeoutScanExecutor.setKeepAliveTime(15, TimeUnit.SECONDS);
            timeoutScanExecutor.allowCoreThreadTimeOut(true);
            timeoutScanExecutor.scheduleAtFixedRate(new Runnable() {
                public void run() {// check idle connection
                    removeTimeoutTrace();
                }
            }, 0, config.getSqlTraceTimeoutScanPeriod(), TimeUnit.MILLISECONDS);
        }
    }

    //clear pool
    public void clearDsConnections(String dsId) {
        SpringBootDataSource ds = dsMap.get(dsId);
        if (ds != null) ds.clearAllConnections();
    }

    //get sql statement list
    public Collection<StatementTrace> getSqlExecutionList() {
        return sqlTraceQueue;
    }

    //get pool connection monitor
    public List<Map<String, Object>> getPoolMonitorVoList() {
        List<Map<String, Object>> poolMonitorVoList = new ArrayList<>(dsMap.size());
        Iterator<SpringBootDataSource> iterator = dsMap.values().iterator();
        while (iterator.hasNext()) {
            SpringBootDataSource ds = iterator.next();
            ConnectionPoolMonitorVo vo = ds.getPoolMonitorVo();
            if (vo == null) continue;
            if (vo.getPoolState() == POOL_CLOSED) {//POOL_CLOSED
                iterator.remove();
            } else {
                Map<String, Object> monitorMap = new HashMap<>(9);
                monitorMap.put("dsId", ds.getId());
                monitorMap.put("dsUUID", ds.getDsUUID());
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

    //add statement sql
    public Object traceSqlExecution(StatementTrace vo, Statement statement, Method method, Object[] args) throws Throwable {
        if (vo == null) return null;
        vo.setMethodName(method.getName());
        vo.setTraceStartTime(System.currentTimeMillis());
        if (sqlTrace) {
            while (sqlTraceQueue.size() >= sqlTraceMaxSize) sqlTraceQueue.poll();
            sqlTraceQueue.offer(vo);
        }

        try {
            if (sqlShow) Log.info("Executing sql:{}", vo.getSql());
            Object re = method.invoke(statement, args);
            vo.setExecSuccessInd(true);
            return re;
        } catch (Throwable e) {
            vo.setExecSuccessInd(false);
            if (e instanceof InvocationTargetException) {
                InvocationTargetException ee = (InvocationTargetException) e;
                if (ee.getCause() != null) {
                    e = ee.getCause();
                }
            }
            vo.setFailCause(e);
            throw e;
        } finally {
            vo.setExecInd(true);
            Date endDate = new Date();
            vo.setExecEndTime(formatDate(endDate));
            vo.setExecTookTimeMs(endDate.getTime() - vo.getExecStartTimeMs());
            if (vo.getExecTookTimeMs() >= sqlExecSlowTime)//alert
                vo.setExecSlowInd(true);
        }
    }

    private void removeTimeoutTrace() {
        sqlAlertTempList.clear();
        Iterator<StatementTrace> iterator = sqlTraceQueue.iterator();
        while (iterator.hasNext()) {
            StatementTrace vo = iterator.next();
            if (vo.isExecInd() && (!vo.isExecSuccessInd() || vo.isExecSlowInd()) && !vo.isAlertInd()) {//failed
                vo.setAlertInd(true);
                sqlAlertTempList.add(vo);
            }

            if (System.currentTimeMillis() - vo.getTraceStartTime() > sqlTraceTimeout)
                iterator.remove();
        }

        if (!sqlAlertTempList.isEmpty()) //should be in short time
            sqlTraceAlert.alert(sqlAlertTempList);
    }

    private static final class TimeoutScanThreadThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            Thread th = new Thread(r, "SqlTrace-TimeoutScan");
            th.setDaemon(true);
            return th;
        }
    }
}
