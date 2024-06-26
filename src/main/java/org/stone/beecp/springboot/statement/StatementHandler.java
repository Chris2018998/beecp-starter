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
package org.stone.beecp.springboot.statement;

import org.stone.beecp.springboot.SpringBootDataSourceManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Statement;

/**
 * @author Chris Liao
 */
class StatementHandler implements InvocationHandler {
    private static final String Execute = "execute";
    private final String dsId;
    private final String dsUUID;
    private final Statement statement;
    private final String statementType;
    private final SpringBootDataSourceManager dsManager = SpringBootDataSourceManager.getInstance();
    private final StatementTrace traceVo;

    StatementHandler(Statement statement, String statementType, String dsId, String dsUUID, StatementTrace traceVo) {
        this.dsId = dsId;
        this.dsUUID = dsUUID;
        this.statement = statement;
        this.statementType = statementType;
        this.traceVo = traceVo;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().startsWith(Execute)) {//execute method
            if (args == null || args.length == 0) {//PreparedStatement.executeXXX();
                if (traceVo != null)
                    return dsManager.traceSqlExecution(traceVo, statement, method, args);
                else
                    return method.invoke(statement, args);
            } else {//Statement.executeXXXX(sql)
                StatementTrace traceVo = new StatementTrace(dsId, dsUUID, (String) args[0], statementType);
                return dsManager.traceSqlExecution(traceVo, statement, method, args);
            }
        } else {
            return method.invoke(statement, args);
        }
    }
}
