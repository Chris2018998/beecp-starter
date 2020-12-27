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
package cn.beecp.boot;

/*
 *  Util
 *
 *  @author Chris.Liao
 */
public class SystemUtil {

    //Spring dataSource configuration prefix-key name
    public static final String Spring_DS_Prefix = "spring.datasource";

    //Spring dataSource configuration key name
    public static final String Spring_DS_KEY_NameList = "nameList";

    //Spring jndi dataSource configuration key name
    public static final String Spring_DS_KEY_Jndi = "jndiName";

    //indicator:Spring dataSource register as primary datasource
    public static final String Spring_DS_KEY_Primary = "primary";

    //Datasource class name
    public static final String Spring_DS_KEY_DatasourceType = "datasourceType";

    //Datasource attribute set factory
    public static final String Spring_DS_KEY_DatasourceAttributeSetFactory = "datasourceAttributeSetFactory";

    //indicator for sql execution trace
    public static final String Spring_DS_KEY_SQL_Exec_Trace = "sqlExecutionTrace";

    //sql execution trace queue size
    public static final String Spring_DS_KEY_SQL_Exec_Trace_MaxSize = "sqlExecutionTraceMaxSize";

    //timeout ms,when timeout then removed from trace queue
    public static final String Spring_DS_KEY_SQL_Exec_Trace_Timeout = "sqlExecutionTraceTimeout";

    //Low efficiency time
    public static final String Spring_DS_KEY_SQL_Exec_Alert_Time = "sqlExecutionAlertTime";

    //configuration key:Alert_Action class name
    public static final String Spring_DS_KEY_SQL_Exec_Alert_Action = "sqlExecutionAlertAction";

    //Default DataSourceName
    public static final String Default_DS_Class_Name = "cn.beecp.BeeDataSource";


    public static final boolean isBlank(String str) {
        if (str == null) return true;
        int strLen = str.length();
        for (int i = 0; i < strLen; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
