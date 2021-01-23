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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;

/**
 * XA Connection handler
 *
 * @author Chris.Liao
 */
public class XAConnectionHandler implements InvocationHandler {
    private static final String Method_GetConnection = "getConnection";
    private String dsId;
    private XAConnection xaConnection;

    public XAConnectionHandler(XAConnection xaConnection, String dsId) {
        this.dsId = dsId;
        this.xaConnection = xaConnection;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        Object re = method.invoke(xaConnection, args);
        if (Method_GetConnection.equals(name)) {
            return ProxyFactory.createConnection((Connection) re, dsId);
        } else {
            return re;
        }
    }
}
