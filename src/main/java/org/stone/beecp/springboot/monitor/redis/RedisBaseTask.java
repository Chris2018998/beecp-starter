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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

/**
 * Redis base task
 *
 * @author Chris Liao
 */
abstract class RedisBaseTask implements Runnable {
    protected final static String RedisKeyPrefix = "BeeDs_Monitor_";
    protected final Logger Log;
    protected final JedisPool pool;

    RedisBaseTask(JedisPool pool) {
        this.pool = pool;
        this.Log = LoggerFactory.getLogger(this.getClass());
    }
}
