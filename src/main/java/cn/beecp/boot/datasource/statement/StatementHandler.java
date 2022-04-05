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
package cn.beecp.boot.datasource.statement;

import cn.beecp.boot.datasource.SpringBootDataSourceManager;
import cn.beecp.pool.PoolStaticCenter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Statement;

/**
 * @author Chris.Liao
 */
class StatementHandler implements InvocationHandler {
    private static final String Execute = "execute";
    private final String dsId;
    private final String dsUUID;
    private final Statement statement;
    private final String statementType;
    private StatementTrace traceVo;

    StatementHandler(Statement statement, String statementType, String sql, String dsId, String dsUUID) {
        this.dsId = dsId;
        this.dsUUID = dsUUID;
        this.statement = statement;
        this.statementType = statementType;
        if (!PoolStaticCenter.isBlank(sql))
            traceVo = new StatementTrace(dsId, dsUUID, sql, statementType);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().startsWith(Execute)) {//execute method
            if (args == null || args.length == 0) {//PreparedStatement.executeXXX();
                if (traceVo != null) {
                    return SpringBootDataSourceManager.getInstance().traceSqlExecution(traceVo, statement, method, args);
                } else
                    return method.invoke(statement, args);
            } else {//Statement.executeXXXX(sql)
                StatementTrace sqlVo = new StatementTrace(dsId, dsUUID, (String) args[0], statementType);
                return SpringBootDataSourceManager.getInstance().traceSqlExecution(sqlVo, statement, method, args);
            }
        } else {
            return method.invoke(statement, args);
        }
    }
}