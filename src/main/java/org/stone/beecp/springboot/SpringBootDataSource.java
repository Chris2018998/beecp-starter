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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stone.beecp.BeeConnectionPoolMonitorVo;
import org.stone.beecp.BeeDataSource;
import org.stone.beecp.jta.BeeJtaDataSource;
import org.stone.beecp.springboot.statement.StatementTraceUtil;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.UUID;

/**
 * statement datasource
 *
 * @author Chris Liao
 */
public class SpringBootDataSource implements DataSource {
    private final static Logger Log = LoggerFactory.getLogger(SpringBootDataSource.class);
    private final String dsId;
    private final String dsUUID;
    private final DataSource ds;
    private final boolean jndiDs;
    private final boolean isBeeDs;

    private boolean primary;
    private boolean traceSql;
    private Method poolMonitorVoMethod;
    private Method poolRestartPoolMethod;
    private Method poolInterruptPoolMethod;

    SpringBootDataSource(String dsId, DataSource ds, boolean jndiDs) {
        this.dsId = dsId;
        this.ds = ds;
        this.jndiDs = jndiDs;

        this.isBeeDs = ds instanceof BeeDataSource || ds instanceof BeeJtaDataSource;
        if (isBeeDs) readBeeDsMethods();
        this.dsUUID = "SpringDs_" + UUID.randomUUID();
    }

    String getDsId() {
        return dsId;
    }

    boolean isPrimary() {
        return primary;
    }

    void setPrimary(boolean primary) {
        this.primary = primary;
    }

    void setTraceSql(boolean traceSql) {
        this.traceSql = traceSql;
    }

    public Connection getConnection() throws SQLException {
        Connection con = ds.getConnection();
        return traceSql ? StatementTraceUtil.createConnection(con, dsId, dsUUID) : con;
    }

    public Connection getConnection(String username, String password) throws SQLException {
        Connection con = ds.getConnection(username, password);
        return traceSql ? StatementTraceUtil.createConnection(con, dsId, dsUUID) : con;
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
        if (!jndiDs) SpringBootDataSourceUtil.tryToCloseDataSource(ds);
    }

    void clearPool() {
        if (poolRestartPoolMethod != null) {
            try {
                poolRestartPoolMethod.invoke(ds, false);
            } catch (Throwable e) {
                Log.warn("Failed to execute dataSource 'restartPool' method", e);
            }
        }
    }

    void interruptPool() {
        if (poolInterruptPoolMethod != null) {
            try {
                poolInterruptPoolMethod.invoke(ds, false);
            } catch (Throwable e) {
                Log.warn("Failed to execute dataSource 'restartPool' method", e);
            }
        }
    }

    BeeConnectionPoolMonitorVo getPoolMonitorVo() {
        if (poolMonitorVoMethod != null) {
            try {
                BeeConnectionPoolMonitorVo vo = (BeeConnectionPoolMonitorVo) poolMonitorVoMethod.invoke(ds);
                SpringConnectionPoolMonitorVo newVo = new SpringConnectionPoolMonitorVo(vo);
                newVo.setDsId(dsId);
                newVo.setDsUUID(dsUUID);
                return newVo;
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
                poolRestartPoolMethod = dsClass.getMethod("clear", Boolean.TYPE);
            } catch (NoSuchMethodException e) {
                Log.warn("DataSource method(clear) not found", e);
            }

            try {
                poolInterruptPoolMethod = dsClass.getMethod("interruptConnectionCreating", Boolean.TYPE);
            } catch (NoSuchMethodException e) {
                Log.warn("DataSource method(clear) not found", e);
            }
        }
    }
}
