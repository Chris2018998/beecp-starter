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

import cn.beecp.boot.datasource.sqltrace.ProxyFactory;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import static cn.beecp.boot.datasource.DataSourceUtil.tryToCloseDataSource;

/**
 * XADataSource wrapper.
 *
 * @author Chris.Liao
 */
public class DataSourceXaWrapper implements XADataSource {
    private String dsId;
    private boolean traceSQL;
    private boolean jndiDs;
    private XADataSource delegate;

    public DataSourceXaWrapper(String dsId, XADataSource delegate, boolean traceSQL, boolean jndiDs) {
        this.dsId = dsId;
        this.delegate = delegate;
        this.traceSQL = traceSQL;
        this.jndiDs = jndiDs;
    }

    public XAConnection getXAConnection() throws SQLException {
        XAConnection con = delegate.getXAConnection();
        return traceSQL ? ProxyFactory.createXAConnection(con, dsId) : con;
    }

    public XAConnection getXAConnection(String username, String password) throws SQLException {
        XAConnection con = delegate.getXAConnection(username, password);
        return traceSQL ? ProxyFactory.createXAConnection(con, dsId) : con;
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

    public void close() {
        if (!jndiDs)
            tryToCloseDataSource(delegate);
    }
}
