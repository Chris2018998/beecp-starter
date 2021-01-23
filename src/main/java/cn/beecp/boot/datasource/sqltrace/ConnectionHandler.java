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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;

/**
 * @author Chris.Liao
 */
public class ConnectionHandler implements InvocationHandler {
    private static final String Type_Statement = "Statement";
    private static final String Type_PreparedStatement = "PreparedStatement";
    private static final String Type_CallableStatement = "CallableStatement";
    private static final String Method_Statement = "createStatement";
    private static final String Method_PreparedStatement = "prepareStatement";
    private static final String Method_CallableStatement = "prepareCall";

    private String dsId;
    private Connection connection;

    public ConnectionHandler(Connection connection, String dsId) {
        this.dsId = dsId;
        this.connection = connection;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        Object re = method.invoke(connection, args);
        if (Method_Statement.equals(name)) {
            return ProxyFactory.createStatementProxy((Statement) re, Type_Statement, dsId, null);
        } else if (Method_PreparedStatement.equals(name)) {
            return ProxyFactory.createStatementProxy((Statement) re, Type_PreparedStatement, dsId, (String) args[0]);
        } else if (Method_CallableStatement.equals(name)) {
            return ProxyFactory.createStatementProxy((Statement) re, Type_CallableStatement, dsId, (String) args[0]);
        } else {
            return re;
        }
    }
}
