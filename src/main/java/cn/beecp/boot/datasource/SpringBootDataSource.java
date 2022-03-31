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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import static cn.beecp.boot.datasource.SpringBootDataSourceUtil.tryToCloseDataSource;

/**
 * trace datasource
 *
 * @author Chris.Liao
 */
public class SpringBootDataSource implements DataSource {
    private final String id;
    private final DataSource ds;
    private final boolean jndiDs;
    private final boolean isBeeDs;
    private final Logger Log = LoggerFactory.getLogger(SpringBootDataSource.class);

    private boolean primary;
    private boolean traceSQL;
    private Method poolClearMethod;
    private Method poolMonitorVoMethod;

    SpringBootDataSource(String id, DataSource ds, boolean jndiDs) {
        this.id = id;
        this.ds = ds;

        this.jndiDs = jndiDs;
        this.isBeeDs = ds instanceof BeeDataSource || ds instanceof BeeJtaDataSource;
        if (isBeeDs) readBeeDsMethods();
    }

    String getId() {
        return id;
    }

    boolean isPrimary() {
        return primary;
    }

    void setPrimary(boolean primary) {
        this.primary = primary;
    }

    boolean isTraceSQL() {
        return traceSQL;
    }

    void setTraceSQL(boolean traceSQL) {
        this.traceSQL = traceSQL;
    }

    public Connection getConnection() throws SQLException {
        Connection con = ds.getConnection();
        return traceSQL ? ProxyFactory.createConnection(con, id) : con;
    }

    public Connection getConnection(String username, String password) throws SQLException {
        Connection con = ds.getConnection(username, password);
        return traceSQL ? ProxyFactory.createConnection(con, id) : con;
    }

    public PrintWriter getLogWriter() throws SQLException {
        return ds.getLogWriter();
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        ds.setLogWriter(out);
    }

    public int getLoginTimeout() throws SQLException {
        return ds.getLoginTimeout();
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        ds.setLoginTimeout(seconds);
    }

    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return ds.getParentLogger();
    }

    public boolean isWrapperFor(Class<?> clazz) {
        return clazz != null && clazz.isInstance(this);
    }

    public <T> T unwrap(Class<T> clazz) throws SQLException {
        if (clazz != null && clazz.isInstance(this))
            return clazz.cast(this);
        else
            throw new SQLException("Wrapped object was not an instance of " + clazz);
    }

    void close() {
        if (!jndiDs) tryToCloseDataSource(ds);
    }

    void clear() {
        if (poolClearMethod != null) {
            try {
                poolClearMethod.invoke(ds, false);
            } catch (Throwable e) {
                Log.warn("Failed to execute dataSource 'clear' method", e);
            }
        }
    }

    ConnectionPoolMonitorVo getPoolMonitorVo() {
        if (poolMonitorVoMethod != null) {
            try {
                return (ConnectionPoolMonitorVo) poolMonitorVoMethod.invoke(ds);
            } catch (Throwable e) {
                Log.warn("Failed to execute dataSource 'getPoolMonitorVo' method", e);
            }
        }
        return null;
    }

    private void readBeeDsMethods() {
        if (isBeeDs) {
            Class dsClass = ds.getClass();
            try {
                poolMonitorVoMethod = dsClass.getMethod("getPoolMonitorVo");
            } catch (NoSuchMethodException e) {
                Log.warn("DataSource method(getPoolMonitorVo) not found", e);
            }
            try {
                poolClearMethod = dsClass.getMethod("clear", Boolean.TYPE);
            } catch (NoSuchMethodException e) {
                Log.warn("DataSource method(clear) not found", e);
            }
        }
    }
}
