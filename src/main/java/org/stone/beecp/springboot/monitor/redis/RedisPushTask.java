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
package org.stone.beecp.springboot.monitor.redis;

import org.stone.beecp.springboot.SpringBootDataSourceManager;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

/**
 * Redis push task
 *
 * @author Chris Liao
 */

public class RedisPushTask extends RedisBaseTask {
    private final int expireSeconds;
    private final RedisPackage dataPackage;
    private final SpringBootDataSourceManager dsManager = SpringBootDataSourceManager.getInstance();

    public RedisPushTask(JedisPool pool, int expireSeconds) {
        super(pool);
        this.expireSeconds = expireSeconds;
        this.dataPackage = new RedisPackage(RedisKeyPrefix + UUID.randomUUID());
    }

    public void run() {//abandon
//        Jedis jedis = null;
//        try {
//            dataPackage.setDsList(dsManager.getPoolMonitorVoList());
//            dataPackage.setSqlList(dsManager.getSqlExecutionList());
//            String jsonPackage = SpringBootDataSourceUtil.object2String(dataPackage);
//            jedis = pool.getResource();
//            jedis.setex(dataPackage.getPackageUUID(), expireSeconds, jsonPackage);
//        } catch (Throwable e) {
//            Log.error("Failed send monitor-package to redis,cause:", e);
//        } finally {
//            if (jedis != null) jedis.close();
//        }
    }
}