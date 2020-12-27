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
package cn.beecp.boot.monitor.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;

/*
 * SQL Execute Monitor center
 *
 *  @author Chris.Liao
 */
public class SQLExecutionPool {
    private static final SQLExecutionPool instance = new SQLExecutionPool();
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int traceMaxSize = 1000;
    private SqlExecutionAlert sqlexecutionAlert = new SqlExecutionAlert();
    private long sqlExecutionAlertTime = TimeUnit.MINUTES.toMillis(1);
    private AtomicInteger tracedSize = new AtomicInteger(0);
    private long traceTimeoutMs = TimeUnit.MINUTES.toMillis(3);

    private ConcurrentLinkedQueue<SQLExecutionVo> sqlTraceQueue = new ConcurrentLinkedQueue<SQLExecutionVo>();
    private ScheduledThreadPoolExecutor timeoutSchExecutor = new ScheduledThreadPoolExecutor(1, new TimeoutScanThreadThreadFactory());
    private LinkedList<SQLExecutionVo> alertList = new LinkedList();

    private SQLExecutionPool() {
        timeoutSchExecutor.setKeepAliveTime(15, SECONDS);
        timeoutSchExecutor.allowCoreThreadTimeOut(true);
        timeoutSchExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {// check idle connection
                removeTimeoutTrace();
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    public static final SQLExecutionPool getInstance() {
        return instance;
    }

    public void setTraceMaxSize(int traceMaxSize) {
        if (traceMaxSize > 0)
            this.traceMaxSize = traceMaxSize;
    }

    public void setTraceTimeoutMs(long traceTimeoutMs) {
        if (traceTimeoutMs > 0)
            this.traceTimeoutMs = traceTimeoutMs;
    }

    public void setSqlExecutionAlertTime(long sqlExecutionAlertTime) {
        if (sqlExecutionAlertTime > 0)
            this.sqlExecutionAlertTime = sqlExecutionAlertTime;
    }

    public void setSqlExecutionAlert(SqlExecutionAlert arlert) {
        if (arlert != null)
            this.sqlexecutionAlert = arlert;
    }

    public final ConcurrentLinkedQueue getTraceQueue() {
        return sqlTraceQueue;
    }

    Object executeStatement(SQLExecutionVo vo, Statement statement, Method method, Object[] args, String poolName) throws Throwable {
        vo.setMethodName(method.getName());
        int size = tracedSize.incrementAndGet();
        sqlTraceQueue.offer(vo);
        if (size > traceMaxSize) sqlTraceQueue.poll();

        try {
            Date startDate = new Date();
            vo.setStartTime(formatter.format(startDate));
            vo.setStartTimeMs(startDate.getTime());
            Object re = method.invoke(statement, args);
            vo.setSuccess(true);
            return re;
        } catch (Throwable e) {
            vo.setSuccess(false);
            if (e instanceof InvocationTargetException) {
                InvocationTargetException ee = (InvocationTargetException) e;
                if (ee.getCause() != null) {
                    e = ee.getCause();
                }
            }
            vo.setFailCause(e);
            throw e;
        } finally {
            Date endDate = new Date();
            vo.setEndTime(formatter.format(endDate));
            vo.setTookTimeMs(endDate.getTime() - vo.getStartTimeMs());
        }
    }

    private void removeTimeoutTrace() {
        alertList.clear();
        Iterator<SQLExecutionVo> itor = sqlTraceQueue.iterator();
        while (itor.hasNext()) {
            SQLExecutionVo vo = itor.next();
            if (vo.getTookTimeMs() >= traceTimeoutMs)
                alertList.add(vo);
            if (System.currentTimeMillis() - vo.getStartTimeMs() > traceTimeoutMs) {
                tracedSize.decrementAndGet();
                sqlTraceQueue.remove(vo);
            }
        }

        if (!alertList.isEmpty()) {
            sqlexecutionAlert.alert(alertList);
        }
    }

    private static final class TimeoutScanThreadThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            Thread th = new Thread(r, "SQLExecTrace-TimeoutScan");
            th.setDaemon(true);
            return th;
        }
    }
}
