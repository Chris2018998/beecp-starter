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
package cn.beecp.boot.datasource.monitor.redis;

import cn.beecp.boot.datasource.SpringBootDataSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static cn.beecp.boot.datasource.SpringBootDataSourceUtil.object2String;

/**
 * Redis push task
 *
 * @author Chris.Liao
 */

public class RedisPushTask implements Runnable {
    private final JedisPool pool;
    private final int expireSeconds;
    private final RedisPackage dataPackage;
    private final Logger Log = LoggerFactory.getLogger(RedisPushTask.class);
    private final SpringBootDataSourceManager dataSourceManager = SpringBootDataSourceManager.getInstance();

    public RedisPushTask(JedisPool pool, int expireSeconds) {
        this.pool = pool;
        this.expireSeconds = expireSeconds;
        this.dataPackage = new RedisPackage();
    }

    public void run() {
        Jedis jedis = null;
        try {
            dataPackage.setDsList(dataSourceManager.getPoolMonitorVoList());
            dataPackage.setSqlList(dataSourceManager.getSqlExecutionList());
            String jsonPackage = object2String(dataPackage);
            jedis = pool.getResource();
            jedis.setex(dataPackage.getPackageUUID(), expireSeconds, jsonPackage);
        } catch (Throwable e) {
            Log.error("Failed to send to redis,cause:", e);
        } finally {
            if (jedis != null) jedis.close();
        }
    }
}