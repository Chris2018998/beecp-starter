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

import cn.beecp.boot.DataSourceId;

import java.lang.reflect.Method;

/*
 *  dyn-dataSourceId setter
 *
 *  @author Chris.Liao
 */
public class DataSourceIdSetter {
    public void setDataSourceId(Method method) {
        DataSourceId annotation = (DataSourceId) method.getAnnotation(DataSourceId.class);
        if (annotation == null) return;
        String dsId = annotation.value();
        if (!DataSourceUtil.isBlank(dsId)) {
            TraceDataSourceMap.getInstance().setCurDsId(dsId.trim());
        }
    }

    public void removeDataSourceId() {
        TraceDataSourceMap.getInstance().removeCurDsId();
    }
}
