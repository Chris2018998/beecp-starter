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
package org.stone.beecp.springboot;

import org.stone.beecp.BeeConnectionPoolMonitorVo;

/*
 * monitor vo
 *
 * @author Chris Liao
 */
public class SpringConnectionPoolMonitorVo implements BeeConnectionPoolMonitorVo {
    private final BeeConnectionPoolMonitorVo vo;
    private String dsId;
    private String dsUUID;

    public SpringConnectionPoolMonitorVo(BeeConnectionPoolMonitorVo vo) {
        this.vo = vo;
    }

    public String getDsId() {
        return dsId;
    }

    public void setDsId(String dsId) {
        this.dsId = dsId;
    }

    public String getDsUUID() {
        return dsUUID;
    }

    public void setDsUUID(String dsUUID) {
        this.dsUUID = dsUUID;
    }

    public String getPoolName() {
        return vo.getPoolName();
    }

    public String getPoolMode() {
        return vo.getPoolMode();
    }

    public int getPoolMaxSize() {
        return vo.getPoolMaxSize();
    }

    public int getPoolState() {
        return vo.getPoolState();
    }

    public int getIdleSize() {
        return vo.getIdleSize();
    }

    public int getBorrowedSize() {
        return vo.getBorrowedSize();
    }

    public int getSemaphoreWaitingSize() {
        return vo.getSemaphoreWaitingSize();
    }

    public int getTransferWaitingSize() {
        return vo.getTransferWaitingSize();
    }

    public int getCreatingCount() {
        return vo.getCreatingCount();
    }

    public int getCreatingTimeoutCount() {
        return vo.getCreatingTimeoutCount();
    }
}