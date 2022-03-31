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

import javax.sql.XAConnection;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Statement;

/**
 * @author Chris.Liao
 */
public class ProxyFactory {
    private static final ClassLoader classLoader = ProxyFactory.class.getClassLoader();
    private static final Class[] INTF_Connection = new Class[]{Connection.class};
    private static final Class[] INTF_XAConnection = new Class[]{XAConnection.class};
    private static final Class[] INTF_CallableStatement = new Class[]{CallableStatement.class};

    public static Connection createConnection(Connection delegate, String dsId) {
        return (Connection) Proxy.newProxyInstance(
                classLoader,
                INTF_Connection,
                new ConnectionHandler(delegate, dsId)
        );
    }

    public static Statement createStatementProxy(Statement delegate, String statementType, String dsId) {
        return createStatementProxy(delegate, statementType, dsId, null);
    }

    static Statement createStatementProxy(Statement delegate, String statementType, String dsId, String SQL) {
        return (Statement) Proxy.newProxyInstance(
                classLoader,
                INTF_CallableStatement,
                new StatementHandler(delegate, statementType, dsId, SQL)
        );
    }
}
