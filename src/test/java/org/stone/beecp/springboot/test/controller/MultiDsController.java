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
package org.stone.beecp.springboot.test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.stone.beecp.springboot.SpringBootRestResponse;
import org.stone.beecp.springboot.annotation.DsId;
import org.stone.beecp.springboot.annotation.EnableDsMonitor;
import org.stone.beecp.springboot.annotation.EnableMultiDs;
import org.stone.beecp.springboot.factory.SpringBootDataSourceException;
import org.stone.beecp.springboot.test.util.ServerSideUtil;

import javax.sql.DataSource;

import static org.stone.beecp.springboot.SpringBootRestResponse.CODE_FAILED;
import static org.stone.beecp.springboot.SpringBootRestResponse.CODE_SUCCESS;
import static org.stone.tools.CommonUtil.isBlank;

/*
 *  DataSource Tester Controller
 *
 *  @author Chris Liao
 */
@EnableMultiDs
@EnableDsMonitor
@SpringBootApplication
@RestController
public class MultiDsController {
    @Autowired
    @Qualifier("ds1")
    private DataSource ds1;
    @Autowired
    @Qualifier("ds2")
    private DataSource ds2;
    @Autowired
    @Qualifier("combineDs")
    private DataSource combineDs;

    @GetMapping("/testGetConnection")
    public SpringBootRestResponse testGetConnection(String dsId) throws Exception {
        try {
            if (isBlank(dsId))
                throw new SpringBootDataSourceException("DataSource Id cant't be null or empty");
            if (!"ds1".equals(dsId) && !"ds2".equals(dsId))
                throw new SpringBootDataSourceException("DataSource Id must be one of list(ds1,ds2)");

            DataSource ds = "ds1".equals(dsId) ? ds1 : ds2;
            return new SpringBootRestResponse(CODE_SUCCESS, ServerSideUtil.testGetConnection(ds), "OK");
        } catch (Throwable e) {
            return new SpringBootRestResponse(CODE_FAILED, e, "Failed");
        }
    }

    @GetMapping("/testSQL")
    public SpringBootRestResponse testSQL(String dsId, String sql, String type, String slowInd) throws Exception {
        try {
            if (isBlank(dsId))
                throw new SpringBootDataSourceException("DataSource Id cant't be null or empty");
            if (!"ds1".equals(dsId) && !"ds2".equals(dsId))
                throw new SpringBootDataSourceException("DataSource Id must be one of list(ds1,ds2)");
            if (isBlank(sql))
                throw new SpringBootDataSourceException("Execute SQL can't be null or empty");
            if (isBlank(type))
                throw new SpringBootDataSourceException("Execute type't be null or empty");
            if (!"Statement".equalsIgnoreCase(type) && !"PreparedStatement".equalsIgnoreCase(type) && !"CallableStatement".equalsIgnoreCase(type))
                throw new SpringBootDataSourceException("Execute type must be one of list(Statement,PreparedStatement,CallableStatement)");

            DataSource ds = "ds1".equals(dsId) ? ds1 : ds2;
            return new SpringBootRestResponse(CODE_SUCCESS, ServerSideUtil.testSQL(ds, sql, type, slowInd), "Ok");
        } catch (Throwable e) {
            return new SpringBootRestResponse(CODE_FAILED, e, "Failed");
        }
    }

    @GetMapping("/testGetConnection1")
    @DsId("ds1")
    public SpringBootRestResponse testCombineDs1() throws Exception {
        try {
            return new SpringBootRestResponse(CODE_SUCCESS, ServerSideUtil.testGetConnection(combineDs), "OK");
        } catch (Throwable e) {
            return new SpringBootRestResponse(CODE_FAILED, e, "Failed");
        }
    }

    @GetMapping("/testGetConnection2")
    @DsId("ds2")
    public SpringBootRestResponse testCombineDs2() throws Exception {
        try {
            return new SpringBootRestResponse(CODE_SUCCESS, ServerSideUtil.testGetConnection(combineDs), "OK");
        } catch (Throwable e) {
            return new SpringBootRestResponse(CODE_FAILED, e, "Failed");
        }
    }

    @GetMapping("/testExecSQL1")
    @DsId("ds1")
    public SpringBootRestResponse testExecSQL1(String sql, String type, String slowInd) throws Exception {
        try {
            return new SpringBootRestResponse(CODE_SUCCESS, ServerSideUtil.testSQL(combineDs, sql, type, slowInd), "OK");
        } catch (Throwable e) {
            return new SpringBootRestResponse(CODE_FAILED, e, "Failed");
        }
    }

    @GetMapping("/testExecSQL2")
    @DsId("ds2")
    public SpringBootRestResponse testExecSQL2(String sql, String type, String slowInd) throws Exception {
        try {
            return new SpringBootRestResponse(CODE_SUCCESS, ServerSideUtil.testSQL(combineDs, sql, type, slowInd), "OK");
        } catch (Throwable e) {
            return new SpringBootRestResponse(CODE_FAILED, e, "Failed");
        }
    }
}