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

import cn.beecp.BeeDataSource;
import cn.beecp.boot.datasource.sqltrace.ProxyFactory;
import cn.beecp.pool.ConnectionPoolMonitorVo;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * trace datasource
 *
 * @author Chris.Liao
 */
public class TraceDataSource implements DataSource {
    private String dsId;
    private boolean jndiDs;
    private boolean traceSQL;
    private boolean beeType;
    private DataSource delegate;

    private Method resetMethod;
    private Object[] resetParamValues = new Object[]{false};
    private Method getPoolMonitorVoMethod;
    private Object[] emptyParamValues = new Object[0];

    public TraceDataSource(String dsId, DataSource delegate, boolean traceSQL, boolean jndiDs) {
        this.dsId = dsId;
        this.delegate = delegate;

        this.jndiDs = jndiDs;
        this.traceSQL = traceSQL;
        this.beeType = delegate instanceof BeeDataSource;
    }

    public String getId() {
        return dsId;
    }

    public boolean isJndiDs() {
        return jndiDs;
    }

    public boolean isTraceSQL() {
        return traceSQL;
    }

    public boolean isBeeType() {
        return beeType;
    }

    public Connection getConnection() throws SQLException {
        Connection con = delegate.getConnection();
        return traceSQL ? ProxyFactory.createConnection(con, dsId) : con;
    }

    public Connection getConnection(String username, String password) throws SQLException {
        Connection con = delegate.getConnection(username, password);
        return traceSQL ? ProxyFactory.createConnection(con, dsId) : con;
    }

    public java.io.PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    public void setLogWriter(java.io.PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    public <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException {
        return delegate.unwrap(iface);
    }

    public boolean isWrapperFor(java.lang.Class<?> iface) throws java.sql.SQLException {
        return delegate.isWrapperFor(iface);
    }

    public void close() {
        if (!jndiDs) {
            Class[] paramTypes = new Class[0];
            Class dsClass = delegate.getClass();
            String[] methodNames = new String[]{"close", "shutdown", "terminate"};
            for (String name : methodNames) {
                try {
                    Method method = dsClass.getMethod(name, paramTypes);
                    method.invoke(delegate, emptyParamValues);
                    break;
                } catch (Throwable e) {
                }
            }
        }
    }

    public void resetPool() throws SQLException {
        if (beeType) {
            if (resetMethod == null) {
                try {
                    Class dsClass = delegate.getClass();
                    resetMethod = dsClass.getMethod("resetPool", new Class[]{Boolean.TYPE});
                } catch (Throwable e) {
                }
            }

            if (resetMethod != null) {
                try {
                    resetMethod.invoke(delegate, resetParamValues);
                } catch (Throwable e) {
                }
            }
        }
    }

    public ConnectionPoolMonitorVo getPoolMonitorVo() throws Exception {
        if (beeType) {
            if (getPoolMonitorVoMethod == null) {
                try {
                    Class dsClass = delegate.getClass();
                    getPoolMonitorVoMethod = dsClass.getMethod("getPoolMonitorVo", new Class[0]);
                } catch (Throwable e) {
                }
            }

            if (getPoolMonitorVoMethod != null) {
                try {
                    return (ConnectionPoolMonitorVo) getPoolMonitorVoMethod.invoke(delegate, emptyParamValues);
                } catch (IllegalAccessException e) {
                    throw e;
                } catch (IllegalArgumentException e) {
                    throw e;
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getTargetException();
                    if (cause != null) {
                        if (cause instanceof Exception) {
                            throw (Exception) cause;
                        } else if (cause instanceof Error) {
                            throw (Error) cause;
                        }
                    } else {
                        throw new Exception(e.getMessage());
                    }
                }
            }
        }
        return null;
    }
}
