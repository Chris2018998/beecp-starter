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
package cn.beecp.boot.test.controller;

import cn.beecp.boot.EnableDataSourceMonitor;
import cn.beecp.boot.EnableMultiDataSource;
import cn.beecp.boot.datasource.DataSourceUtil;
import cn.beecp.boot.test.util.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

/*
 *  DataSource Tester Controller
 *
 *  @author Chris.Liao
 */

@EnableMultiDataSource
@EnableDataSourceMonitor
@SpringBootApplication
@RestController
public class MultiDsController {
    @Autowired
    @Qualifier("ds1")
    private DataSource ds1;
    @Autowired
    @Qualifier("ds2")
    private DataSource ds2;

    public static void main(String[] args) {
        SpringApplication.run(MultiDsController.class, args);
    }

    @GetMapping("/testGetConnection")
    public String testGetConnection(String dsId) throws Exception {
        if (DataSourceUtil.isBlank(dsId)) throw new Exception("DataSource Id cant't be null or empty");
        if (!"ds1".equals(dsId) && !"ds2".equals(dsId))
            throw new Exception("DataSource Id must be one of list(ds1,ds2)");

        DataSource ds = (dsId == "ds1") ? ds1 : ds2;
        return TestUtil.testGetConnection(ds);
    }

    @GetMapping("/testSQL")
    public String testSQL(String dsId, String sql, String type) throws Exception {
        if (DataSourceUtil.isBlank(dsId)) throw new Exception("DataSource Id cant't be null or empty");
        if (!"ds1".equals(dsId) && !"ds2".equals(dsId))
            throw new Exception("DataSource Id must be one of list(ds1,ds2)");
        if (DataSourceUtil.isBlank(sql)) throw new Exception("Execute SQL can't be null or empty");
        if (DataSourceUtil.isBlank(type)) throw new Exception("Execute type't be null or empty");
        if (!"Statement".equalsIgnoreCase(type) && !"PreparedStatement".equalsIgnoreCase(type) && !"CallableStatement".equalsIgnoreCase(type))
            throw new Exception("Execute type must be one of list(Statement,PreparedStatement,CallableStatement)");

        DataSource ds = (dsId == "ds1") ? ds1 : ds2;
        return TestUtil.testSQL(ds, sql, type);
    }
}