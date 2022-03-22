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

import static cn.beecp.boot.datasource.SpringBootDataSourceUtil.tryToCloseDataSource;

/*
 *  DataSource holder
 *
 *  @author Chris.Liao
 */
class DataSourceHolder {
    private String dsId;
    private boolean jndiDs;
    private boolean primary;
    private SpringRegDataSource ds;

    DataSourceHolder(String dsId, SpringRegDataSource ds, boolean jndiDs) {
        this.ds = ds;
        this.dsId = dsId;
        this.jndiDs = jndiDs;
    }

    public SpringRegDataSource getDs() {
        return ds;
    }

    public String getDsId() {
        return dsId;
    }

    public boolean isJndiDs() {
        return jndiDs;
    }

    public void setJndiDs(boolean jndiDs) {
        this.jndiDs = jndiDs;
    }

    boolean isPrimary() {
        return primary;
    }

    void setPrimary(boolean primary) {
        this.primary = primary;
    }

    void close() {
        if (!jndiDs)
            tryToCloseDataSource(ds);
    }
}
