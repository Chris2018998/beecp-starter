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
package org.stone.beecp.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.stone.beecp.BeeConnectionPoolMonitorVo;
import org.stone.beecp.BeeMethodExecutionLog;
import org.stone.beecp.springboot.monitor.redis.RedisPushTask;
import org.stone.beecp.springboot.statement.StatementTrace;
import org.stone.beecp.springboot.statement.StatementTraceAlert;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.stone.tools.CommonUtil.isNotBlank;

/*
 * DataSource Manager
 *
 * @author Chris Liao
 */
public class SpringBootDataSourceManager {
    private static final int POOL_CLOSED = 3;
    private final static SpringBootDataSourceManager instance = new SpringBootDataSourceManager();
    private final Map<String, SpringBootDataSource> dsMap;
    private final ScheduledThreadPoolExecutor timerExecutor;
    private final Logger Log = LoggerFactory.getLogger(SpringBootDataSourceManager.class);
    @Autowired
    public ObjectMapper objectMapper;
    private boolean sqlShow;
    private boolean sqlTrace;
    private long sqlExecSlowTime;
    private long sqlTraceTimeout;
    private int sqlTraceMaxSize;
    private StatementTraceAlert sqlTraceAlert;
    private AtomicInteger sqlTracedSize;
    private ConcurrentLinkedDeque<StatementTrace> sqlTraceQueue;
    private boolean ignoreSet;

    private SpringBootDataSourceManager() {
        this.dsMap = new ConcurrentHashMap<>(1);
        timerExecutor = new ScheduledThreadPoolExecutor(2, new SpringBootDsThreadFactory());
        timerExecutor.setKeepAliveTime(15, TimeUnit.SECONDS);
        timerExecutor.allowCoreThreadTimeOut(true);
    }

    public static SpringBootDataSourceManager getInstance() {
        return instance;
    }

    SpringBootDataSource getSpringBootDataSource(String dsId) {
        return dsMap.get(dsId);
    }

    void addSpringBootDataSource(SpringBootDataSource ds) {
        dsMap.put(ds.getDsId(), ds);
        ds.setTraceSql(sqlTrace);
    }

    //create sql statement pool
    void setupMonitorConfig(DataSourceMonitorConfig config) {
        if (sqlTrace = config.isSqlTrace()) {
            this.sqlShow = config.isSqlShow();
            this.sqlExecSlowTime = config.getSqlExecSlowTime();
            this.sqlTraceMaxSize = config.getSqlTraceMaxSize();
            this.sqlTraceTimeout = config.getSqlTraceTimeout();
            this.sqlTraceAlert = config.getSqlExecAlertAction();
            this.sqlTracedSize = new AtomicInteger(0);
            this.sqlTraceQueue = new ConcurrentLinkedDeque<>();
            //sql trace timeout scan
            timerExecutor.scheduleAtFixedRate(new SqlTraceTimeoutTask(), 0, config.getSqlTraceTimeoutScanPeriod(), MILLISECONDS);

            String redisHost = config.getRedisHost();
            if (isNotBlank(redisHost)) {//send datasource info to redis
                JedisPoolConfig redisConfig = new JedisPoolConfig();
                redisConfig.setMinIdle(0);
                redisConfig.setMaxTotal(1);
                JedisPool pool = new JedisPool(redisConfig, redisHost, config.getRedisPort(), config.getRedisTimeoutMs(), config.getRedisUserId(), config.getRedisPassword());

                int expireSeconds = (int) MILLISECONDS.toSeconds(config.getRedisSendPeriod());
                timerExecutor.scheduleAtFixedRate(new RedisPushTask(pool, expireSeconds), 0, config.getRedisSendPeriod(), MILLISECONDS);
            }
        }
    }

    //clear pool
    public void restartPool(String dsId) {
        SpringBootDataSource ds = dsMap.get(dsId);
        if (ds != null) ds.clearPool();
    }

    //clear pool
    public void interruptPool(String dsId) {
        SpringBootDataSource ds = dsMap.get(dsId);
        if (ds != null) ds.interruptPool();
    }

    //get sql statement list
    public Collection<BeeMethodExecutionLog> getSqlExecutionList() throws SQLException {
        LinkedList<BeeMethodExecutionLog> sqlExecutionList = new LinkedList<>();
        for (SpringBootDataSource ds : dsMap.values()) {
            List<BeeMethodExecutionLog> sqlList = ds.getSqlExecutionList();
            if (sqlList != null && !sqlList.isEmpty()) {
//                for(BeeMethodExecutionLog log:sqlList){
//                    System.out.println("("+log.getPoolName() +"):"+log.getSql());
//                }
                sqlExecutionList.addAll(sqlList);
            }
        }
        return sqlExecutionList;
    }

    //get pool connection monitor
    public List<BeeConnectionPoolMonitorVo> getPoolMonitorVoList() {
        List<BeeConnectionPoolMonitorVo> poolMonitorVoList = new ArrayList<>(dsMap.size());
        Iterator<SpringBootDataSource> iterator = dsMap.values().iterator();
        while (iterator.hasNext()) {
            SpringBootDataSource ds = iterator.next();
            BeeConnectionPoolMonitorVo vo = ds.getPoolMonitorVo();

            if (vo == null) continue;
            if (vo.isClosed()) {//POOL_CLOSED
                iterator.remove();
            } else {
                poolMonitorVoList.add(vo);
            }
        }
        return poolMonitorVoList;
    }

    //add statement sql
    public Object traceSqlExecution(StatementTrace vo, Statement statement, Method method, Object[] args) throws Throwable {
        vo.setMethodName(method.getName());
        sqlTraceQueue.offerFirst(vo);
        if (sqlTracedSize.incrementAndGet() > sqlTraceMaxSize) {
            sqlTraceQueue.pollLast();
            sqlTracedSize.decrementAndGet();
        }

        try {
            if (sqlShow) Log.info("Executing sql:{}", vo.getSql());
            Object re = method.invoke(statement, args);
            vo.setSuccessInd(true);
            return re;
        } catch (InvocationTargetException e) {
            vo.setSuccessInd(false);
            Throwable failedCause = e.getCause();
            if (failedCause == null) failedCause = e;
            vo.setFailCause(failedCause);
            throw failedCause;
        } catch (Throwable e) {
            vo.setSuccessInd(false);
            vo.setFailCause(e);
            throw e;
        } finally {
            Date endDate = new Date();
            vo.setEndTimeMs(endDate.getTime());
            vo.setEndTime(SpringBootDataSourceUtil.formatDate(endDate));
            vo.setTookTimeMs(vo.getEndTimeMs() - vo.getStartTimeMs());
            if (vo.isSuccessInd() && vo.getTookTimeMs() >= sqlExecSlowTime)//alert
                vo.setSlowInd(true);
        }
    }

    private void removeTimeoutTrace(LinkedList<StatementTrace> sqlAlertTempList) {
        Iterator<StatementTrace> iterator = sqlTraceQueue.descendingIterator();
        while (iterator.hasNext()) {
            StatementTrace vo = iterator.next();
            if (vo.getEndTimeMs() > 0 && (!vo.isSuccessInd() || vo.isSlowInd()) && !vo.isAlertedInd()) {//failed or slow
                vo.setAlertedInd(true);
                sqlAlertTempList.add(vo);
                if (sqlShow) Log.info("{} sql:{}", vo.isSlowInd() ? "Slow" : "Error", vo.getSql());
            }

            if (System.currentTimeMillis() - vo.getStartTimeMs() >= sqlTraceTimeout) {
                iterator.remove();
                sqlTracedSize.decrementAndGet();
            }
        }

        if (!sqlAlertTempList.isEmpty()) { //should be in short time
            try {
                sqlTraceAlert.alert(sqlAlertTempList);
            } finally {
                sqlAlertTempList.clear();
            }
        }
    }

    private static final class SpringBootDsThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            Thread th = new Thread(r, "SpringBootDsThreadFactory");
            th.setDaemon(true);
            return th;
        }
    }

    private static final class SqlTraceTimeoutTask implements Runnable {
        private final LinkedList<StatementTrace> sqlAlertTempList = new LinkedList<>();

        public void run() {// check idle connection
            instance.removeTimeoutTrace(sqlAlertTempList);
        }
    }
}
