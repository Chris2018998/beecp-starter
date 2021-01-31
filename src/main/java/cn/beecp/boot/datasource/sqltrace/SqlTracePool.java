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
package cn.beecp.boot.datasource.sqltrace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.beecp.boot.datasource.SpringBootDataSourceUtil.formatDate;

/*
 *  Sql trace pool
 *
 *  @author Chris.Liao
 *
 *  spring.datasource.monitor-login=true
 *  spring.datasource.monitor-user=admin
 *  spring.datasource.monitor-password=admin
 *
 *  spring.datasource.sql-trace=true
 *  spring.datasource.sql-show=true
 *  spring.datasource.sql-trace-max-size=100
 *  spring.datasource.sql-exec-slow-time=5000
 *  spring.datasource.sql-trace-timeout=60000
 *  spring.datasource.sql-exec-alert-action=xxxxx
 *  spring.datasource.sql-trace-timeout-scan-period=18000
 */
public class SqlTracePool {
    private static final SqlTracePool instance = new SqlTracePool();
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final LinkedList<SqlTraceEntry> alertEntryList = new LinkedList();
    private final AtomicInteger tracedQueueSize = new AtomicInteger(0);
    private final ConcurrentLinkedDeque<SqlTraceEntry> traceQueue = new ConcurrentLinkedDeque<SqlTraceEntry>();
    private final ScheduledThreadPoolExecutor timeoutSchExecutor = new ScheduledThreadPoolExecutor(1, new TimeoutScanThreadThreadFactory());

    private boolean inited;
    private boolean sqlTrace = true;
    private boolean sqlShow = false;
    private int sqlTraceMaxSize = 100;
    private long sqlExecSlowTime = TimeUnit.SECONDS.toMillis(6);
    private long sqlTraceTimeout = TimeUnit.MINUTES.toMillis(3);
    private SqlTraceAlert sqlTraceAlert = new SqlTraceAlert();
    private LinkedList<SqlTraceEntry> tempList = new LinkedList();

    public static final SqlTracePool getInstance() {
        return instance;
    }

    public void init(SqlTraceConfig config) {
        if (!inited) {
            int sqlTraceMaxSize = config.getSqlTraceMaxSize();
            long sqlTraceTimeout = config.getSqlTraceTimeout();
            long sqlExecSlowTime = config.getSqlExecSlowTime();
            SqlTraceAlert sqlTraceAlert = config.getSqlExecAlertAction();

            this.sqlTrace = config.isSqlTrace();
            this.sqlShow = config.isSqlShow();
            if (sqlTraceMaxSize > 0 && sqlTraceMaxSize <= 1000)
                this.sqlTraceMaxSize = sqlTraceMaxSize;
            if (sqlTraceTimeout > 0)
                this.sqlTraceTimeout = sqlTraceTimeout;
            if (sqlExecSlowTime > 0)
                this.sqlExecSlowTime = sqlExecSlowTime;
            if (sqlTraceAlert != null)
                this.sqlTraceAlert = sqlTraceAlert;
            this.inited = true;

            if (sqlTrace) {
                long traceTimeoutScanPeriod = TimeUnit.MINUTES.toMillis(3);
                if (config.getSqlTraceTimeoutScanPeriod() > 0)
                    traceTimeoutScanPeriod = config.getSqlTraceTimeoutScanPeriod();

                timeoutSchExecutor.setKeepAliveTime(15, TimeUnit.SECONDS);
                timeoutSchExecutor.allowCoreThreadTimeOut(true);

                timeoutSchExecutor.scheduleAtFixedRate(new Runnable() {
                    public void run() {// check idle connection
                        removeTimeoutTrace();
                    }
                }, 0, traceTimeoutScanPeriod, TimeUnit.MILLISECONDS);
            }
        }
    }

    public boolean isSqlTrace() {
        return this.sqlTrace;
    }

    public final Collection<SqlTraceEntry> getTraceQueue() {
        tempList.clear();
        tempList.addAll(traceQueue);
        return tempList;
    }

    Object trace(SqlTraceEntry vo, Statement statement, Method method, Object[] args, String dsId) throws Throwable {
        vo.setMethodName(method.getName());
        vo.setTraceStartTime(System.currentTimeMillis());
        traceQueue.offerFirst(vo);
        if (tracedQueueSize.incrementAndGet() > sqlTraceMaxSize) {
            traceQueue.pollLast();
            tracedQueueSize.decrementAndGet();
        }

        try {
            if (sqlShow) log.info("Executing sql:{}", vo.getSql());
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
        alertEntryList.clear();
        Iterator<SqlTraceEntry> iterator = traceQueue.descendingIterator();
        while (iterator.hasNext()) {
            SqlTraceEntry vo = iterator.next();
            if (vo.isExecInd() && (!vo.isExecSuccessInd() || vo.isExecSlowInd()) && !vo.isAlertInd()) {//failed
                vo.setAlertInd(true);
                alertEntryList.add(vo);
            }

            if (System.currentTimeMillis() - vo.getTraceStartTime() > sqlTraceTimeout) {
                tracedQueueSize.decrementAndGet();
                traceQueue.remove(vo);
            }
        }

        if (!alertEntryList.isEmpty()) {//should be in short time
            sqlTraceAlert.alert(alertEntryList);
        }
    }

    private static final class TimeoutScanThreadThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            Thread th = new Thread(r, "SqlTrace-TimeoutScan");
            th.setDaemon(true);
            return th;
        }
    }
}
