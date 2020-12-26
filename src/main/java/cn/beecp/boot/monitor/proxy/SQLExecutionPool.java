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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * SQL Execute Monitor center
 *
 *  @author Chris.Liao
 */
public class SQLExecutionPool {
    private static final SQLExecutionPool instance = new SQLExecutionPool();
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private long tracedTimeoutMs = TimeUnit.MINUTES.toMillis(3);
    private AtomicInteger tracedSize = new AtomicInteger(0);
    private ConcurrentLinkedQueue<SQLExecutionVo> sqlTraceQueue = new ConcurrentLinkedQueue<SQLExecutionVo>();
    private ScheduledThreadPoolExecutor timeoutSchExecutor = new ScheduledThreadPoolExecutor(1, new TimeoutScanThreadThreadFactory());

    private SQLExecutionPool() {
        timeoutSchExecutor.setKeepAliveTime(15, TimeUnit.SECONDS);
        timeoutSchExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {// check idle connection
                removeTimeoutTrace();
            }
        }, 1000, 3, TimeUnit.MINUTES);
    }

    public static final SQLExecutionPool getInstance() {
        return instance;
    }

    public final ConcurrentLinkedQueue getTraceQueue() {
        return sqlTraceQueue;
    }

    public void setTracedTimeoutMs(long tracedTimeoutMs) {
        this.tracedTimeoutMs = tracedTimeoutMs;
    }

    Object executeSQL(SQLExecutionVo vo, Statement statement, Method method, Object[] args, String poolName) throws Throwable {
        vo.setMethodName(method.getName());
        tracedSize.incrementAndGet();
        sqlTraceQueue.offer(vo);

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
        Iterator<SQLExecutionVo> itor = sqlTraceQueue.iterator();
        while (itor.hasNext()) {
            SQLExecutionVo vo = itor.next();
            if (System.currentTimeMillis() - vo.getStartTimeMs() > tracedTimeoutMs) {
                tracedSize.decrementAndGet();
                sqlTraceQueue.remove(vo);
            }
        }
    }

    private static final class TimeoutScanThreadThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            Thread th = new Thread(r, "SQLTrace-TimeoutScan");
            th.setDaemon(true);
            return th;
        }
    }
}
