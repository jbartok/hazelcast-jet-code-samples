/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
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

import com.hazelcast.cache.ICache;
import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import static com.hazelcast.jet.pipeline.JournalInitialPosition.START_FROM_OLDEST;

/**
 * A pipeline which streams events from an ICache. The
 * values for new entries are extracted and then written
 * to a local IList in the Jet cluster.
 */
public class CacheJournalSource {

    private static final String CACHE_NAME = "cache";
    private static final String SINK_NAME = "list";

    public static void main(String[] args) throws Exception {
        System.setProperty("hazelcast.logging.type", "log4j");
        JetConfig jetConfig = getJetConfig();
        JetInstance jet = Jet.newJetInstance(jetConfig);
        Jet.newJetInstance(jetConfig);

        try {
            Pipeline p = Pipeline.create();
            p.drawFrom(Sources.<Integer, Integer>cacheJournal(CACHE_NAME, START_FROM_OLDEST))
             .withoutTimestamps()
             .map(Entry::getValue)
             .drainTo(Sinks.list(SINK_NAME));

            jet.newJob(p);

            ICache<Integer, Integer> cache = jet.getCacheManager().getCache(CACHE_NAME);
            for (int i = 0; i < 1000; i++) {
                cache.put(i, i);
            }
            TimeUnit.SECONDS.sleep(3);
            System.out.println("Read " + jet.getList(SINK_NAME).size() + " entries from cache journal.");
        } finally {
            Jet.shutdownAll();
        }
    }

    private static JetConfig getJetConfig() {
        JetConfig cfg = new JetConfig();
        cfg.getHazelcastConfig().addCacheConfig(new CacheSimpleConfig().setName(CACHE_NAME));
        // Add an event journal config for cache which has custom capacity of 1000 (default 10_000)
        // and time to live seconds as 10 seconds (default 0 which means infinite)
        cfg.getHazelcastConfig()
           .getCacheEventJournalConfig(CACHE_NAME)
           .setEnabled(true)
           .setCapacity(1000)
           .setTimeToLiveSeconds(10);
        return cfg;
    }

}
