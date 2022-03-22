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
import cn.beecp.jta.BeeJtaDataSource;
import cn.beecp.pool.ConnectionPoolMonitorVo;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import static cn.beecp.boot.datasource.SpringBootDataSourceUtil.tryToCloseDataSource;

/**
 * trace datasource
 *
 * @author Chris.Liao
 */
public class SpringRegDataSource implements DataSource {
    protected String dsId;
    private boolean jndiDs;
    private DataSource ds;
    private boolean isBeeDs;
    private boolean traceSQL;

    private Method resetPoolMethod;
    private Method getPoolMonitorVoMethod;

    SpringRegDataSource(String dsId, DataSource ds, boolean traceSQL, boolean jndiDs) {
        this.dsId = dsId;
        this.ds = ds;

        this.jndiDs = jndiDs;
        this.traceSQL = traceSQL;
        this.isBeeDs = ds instanceof BeeDataSource || ds instanceof BeeJtaDataSource;
    }

    public String getId() {
        return dsId;
    }

    public boolean isJndiDs() {
        return jndiDs;
    }

    boolean isTraceSQL() {
        return traceSQL;
    }

    public boolean isBeeDs() {
        return isBeeDs;
    }

    public Connection getConnection() throws SQLException {
        Connection con = ds.getConnection();
        return traceSQL ? ProxyFactory.createConnection(con, dsId) : con;
    }

    public Connection getConnection(String username, String password) throws SQLException {
        Connection con = ds.getConnection(username, password);
        return traceSQL ? ProxyFactory.createConnection(con, dsId) : con;
    }

    public java.io.PrintWriter getLogWriter() throws SQLException {
        return ds.getLogWriter();
    }

    public void setLogWriter(java.io.PrintWriter out) throws SQLException {
        ds.setLogWriter(out);
    }

    public int getLoginTimeout() throws SQLException {
        return ds.getLoginTimeout();
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        ds.setLoginTimeout(seconds);
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return ds.getParentLogger();
    }

    public <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException {
        return ds.unwrap(iface);
    }

    public boolean isWrapperFor(java.lang.Class<?> iface) throws java.sql.SQLException {
        return ds.isWrapperFor(iface);
    }

    public void close() {
        if (!jndiDs) tryToCloseDataSource(ds);
    }

    void clear() throws SQLException {
        if (isBeeDs) {
            if (resetPoolMethod == null) {
                try {
                    resetPoolMethod = ds.getClass().getMethod("clear", Boolean.TYPE);
                } catch (Throwable e) {
                }
            }

            if (resetPoolMethod != null) {
                try {
                    resetPoolMethod.invoke(ds, false);
                } catch (Throwable e) {
                }
            }
        }
    }

    ConnectionPoolMonitorVo getPoolMonitorVo() {
        if (isBeeDs) {
            if (getPoolMonitorVoMethod == null) {
                try {
                    getPoolMonitorVoMethod = ds.getClass().getMethod("getPoolMonitorVo");
                } catch (Throwable e) {
                }
            }

            if (getPoolMonitorVoMethod != null) {
                try {
                    return (ConnectionPoolMonitorVo) getPoolMonitorVoMethod.invoke(ds, new Object[0]);
                } catch (Throwable e) {
                }
            }
        }
        return null;
    }
}
