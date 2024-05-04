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

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.stone.beecp.springboot.test.controller.MultiDsController;

import static org.stone.beecp.springboot.test.util.ClientSideUtil.testExecuteSQL;
import static org.stone.beecp.springboot.test.util.ClientSideUtil.testGetConnection;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MultiDsController.class)
@ActiveProfiles("prop_conf")
@WebAppConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCombineDs {
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void test1GetDs1Conn() throws Exception {
        Assert.assertTrue("Not found dataSource(ds1) in trace list", testGetConnection("ds1", mockMvc, "/testGetConnection1"));
    }

    @Test
    public void test2GetDs2Conn() throws Exception {
        Assert.assertTrue("Not found dataSource(ds2) in trace list", testGetConnection("ds2", mockMvc, "/testGetConnection2"));
    }

    @Test
    public void test3SqlStatement() throws Exception {
        Assert.assertTrue("target sql not in trace list", testExecuteSQL("ds1", "select * from TEST_USER", "Statement", mockMvc, 0, "/testExecSQL1"));
    }

    @Test
    public void test4SqlPreparedStatement() throws Exception {
        Assert.assertTrue("target sql not in trace list", testExecuteSQL("ds1", "select * from TEST_USER2", "PreparedStatement", mockMvc, 0, "/testExecSQL1"));
    }

    @Test
    public void test5SqlCallableStatement() throws Exception {
        Assert.assertTrue("target sql not in trace list", testExecuteSQL("ds2", "{call BEECP_HELLO()}", "CallableStatement", mockMvc, 0, "/testExecSQL2"));
    }
}
