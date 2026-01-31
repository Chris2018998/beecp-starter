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

    void setDsId(String dsId) {
        this.dsId = dsId;
    }

    public String getDsUUID() {
        return dsUUID;
    }

    void setDsUUID(String dsUUID) {
        this.dsUUID = dsUUID;
    }

    //***************************************************************************************************************//
    //                                        1: Unchangeable fields                                                 //
    //***************************************************************************************************************//
    public String getPoolName() {
        return vo.getPoolName();
    }

    public boolean isFairMode() {
        return vo.isFairMode();
    }

    public boolean useThreadLocal() {
        return vo.useThreadLocal();
    }

    //***************************************************************************************************************//
    //                                     2: State`methods                                                           //
    //***************************************************************************************************************//
    public boolean isLazy() {
        return vo.isLazy();
    }

    public boolean isNew() {
        return vo.isNew();
    }

    public boolean isReady() {
        return vo.isReady();
    }

    public boolean isClosing() {
        return vo.isClosing();
    }

    public boolean isStarting() {
        return vo.isStarting();
    }

    public boolean isRestarting() {
        return vo.isRestarting();
    }

    public boolean isRestartFailed() {
        return vo.isRestartFailed();
    }

    public boolean isSuspended() {
        return vo.isSuspended();
    }

    //***************************************************************************************************************//
    //                                     3: Other methods                                                          //
    //***************************************************************************************************************//

    public int getMaxSize() {
        return vo.getMaxSize();
    }

    public int getIdleSize() {
        return vo.getIdleSize();
    }

    public int getBorrowedSize() {
        return vo.getBorrowedSize();
    }

    public int getCreatingSize() {
        return vo.getCreatingSize();
    }

    public int getCreatingTimeoutSize() {
        return vo.getCreatingTimeoutSize();
    }

    public int getSemaphoreSize() {
        return vo.getSemaphoreSize();
    }

    public int getSemaphoreRemainSize() {
        return vo.getSemaphoreRemainSize();
    }

    public int getSemaphoreWaitingSize() {
        return vo.getSemaphoreWaitingSize();
    }

    public int getTransferWaitingSize() {
        return vo.getTransferWaitingSize();
    }

    public boolean isEnabledLogPrinter() {
        return vo.isEnabledLogPrinter();
    }

    public boolean isEnabledLogCache() {
        return vo.isEnabledLogCache();
    }
}
