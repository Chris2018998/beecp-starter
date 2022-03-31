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

import cn.beecp.pool.PoolStaticCenter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Statement;

/**
 * @author Chris.Liao
 */
public class StatementHandler implements InvocationHandler {
    private static final String Execute = "execute";
    private String dsId;
    private Statement statement;
    private String statementType;
    private SqlTraceEntry traceEntry;

    public StatementHandler(Statement statement, String statementType, String dsId) {
        this(statement, statementType, dsId, null);
    }

    StatementHandler(Statement statement, String statementType, String dsId, String sql) {
        this.dsId = dsId;
        this.statement = statement;
        this.statementType = statementType;
        if (!PoolStaticCenter.isBlank(sql)) {
            traceEntry = new SqlTraceEntry(dsId, sql, statementType);
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().startsWith(Execute)) {//execute method
            if (args == null || args.length == 0) {//PreparedStatement.executeXXX();
                if (traceEntry != null) {
                    return SqlTracePool.getInstance().trace(traceEntry, statement, method, args, dsId);
                } else
                    return method.invoke(statement, args);
            } else {//Statement.executeXXXX(sql)
                SqlTraceEntry sqlVo = new SqlTraceEntry(dsId, (String) args[0], statementType);
                return SqlTracePool.getInstance().trace(sqlVo, statement, method, args, dsId);
            }
        } else {
            return method.invoke(statement, args);
        }
    }
}
