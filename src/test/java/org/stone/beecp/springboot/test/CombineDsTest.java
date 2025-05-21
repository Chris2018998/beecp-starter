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
package org.stone.beecp.springboot.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.stone.beecp.springboot.test.controller.MultiDsController;

import static org.stone.beecp.springboot.test.util.ClientSideUtil.testExecuteSQL;
import static org.stone.beecp.springboot.test.util.ClientSideUtil.testGetConnection;

@SpringBootTest(classes = MultiDsController.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("prop_conf")
@AutoConfigureMockMvc
public class CombineDsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void test1GetDs1Conn() throws Exception {
        Assertions.assertTrue(testGetConnection("ds1", mockMvc, "/testGetConnection1"), "Not found dataSource(ds1) in trace list");
    }

    @Test
    public void test2GetDs2Conn() throws Exception {
        Assertions.assertTrue(testGetConnection("ds2", mockMvc, "/testGetConnection2"), "Not found dataSource(ds2) in trace list");
    }

    @Test
    public void test3SqlStatement() throws Exception {
        Assertions.assertTrue(testExecuteSQL("ds1", "select * from TEST_USER", "Statement", mockMvc, 0, "/testExecSQL1"), "target sql not in trace list");
    }

    @Test
    public void test4SqlPreparedStatement() throws Exception {
        Assertions.assertTrue(testExecuteSQL("ds1", "select * from TEST_USER2", "PreparedStatement", mockMvc, 0, "/testExecSQL1"), "target sql not in trace list");
    }

    @Test
    public void test5SqlCallableStatement() throws Exception {
        Assertions.assertTrue(testExecuteSQL("ds2", "{call BEECP_HELLO()}", "CallableStatement", mockMvc, 0, "/testExecSQL2"), "target sql not in trace list");
    }
}
