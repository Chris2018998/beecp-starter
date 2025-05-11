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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.stone.beecp.springboot.test.controller.MultiDsController;
import org.stone.beecp.springboot.test.util.ClientSideUtil;


@SpringBootTest(classes = MultiDsController.class)
@ActiveProfiles("prop_conf")
@WebAppConfiguration
public class PropConfigTest {
    private final String connTakeTestURL = "/testGetConnection";
    private final String executeSQLUrl = "/testSQL";
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc getMockMvc() {
        if (mockMvc == null) {
            this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }
        return mockMvc;
    }

    @Test
    public void test1GetDs1Conn() throws Exception {
        Assertions.assertTrue(ClientSideUtil.testGetConnection("ds1", getMockMvc(), connTakeTestURL), "Not found dataSource(ds1) in trace list");
    }

    @Test
    public void test2GetDs2Conn() throws Exception {
        Assertions.assertTrue(ClientSideUtil.testGetConnection("ds2", getMockMvc(), connTakeTestURL), "Not found dataSource(ds2) in trace list");
    }

    @Test
    public void test3SqlStatement() throws Exception {
        Assertions.assertTrue(ClientSideUtil.testExecuteSQL("ds1", "select * from TEST_USER", "Statement", getMockMvc(), 0, executeSQLUrl), "target sql not in trace list");
    }

    @Test
    public void test4SqlPreparedStatement() throws Exception {
        Assertions.assertTrue(ClientSideUtil.testExecuteSQL("ds1", "select * from TEST_USER2", "PreparedStatement", getMockMvc(), 0, executeSQLUrl), "target sql not in trace list");
    }

    @Test
    public void test5SqlCallableStatement() throws Exception {
        Assertions.assertTrue(ClientSideUtil.testExecuteSQL("ds1", "{call BEECP_HELLO()}", "CallableStatement", getMockMvc(), 0, executeSQLUrl), "target sql not in trace list");
    }

    @Test
    public void test6SqlError() throws Exception {
        Assertions.assertTrue(ClientSideUtil.testExecuteSQL("ds1", "select * from TEST_USER3", "PreparedStatement", getMockMvc(), 1, executeSQLUrl), "target sql not in trace list");
    }

    @Test
    public void test7SqlSlow() throws Exception {
        Assertions.assertTrue(ClientSideUtil.testExecuteSQL("ds2", "select * from TEST_USER2", "PreparedStatement", getMockMvc(), 2, executeSQLUrl), "target sql not in trace list");
    }
}
