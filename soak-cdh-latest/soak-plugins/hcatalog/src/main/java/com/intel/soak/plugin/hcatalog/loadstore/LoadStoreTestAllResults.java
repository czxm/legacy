/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.soak.plugin.hcatalog.loadstore;

import com.intel.soak.plugin.hcatalog.publisher.ResultsPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class containing results (html/json) for all load/store tests
 */
public class LoadStoreTestAllResults {
    private Map<String, LoadStoreTestStatistics> results;
    private static final Logger LOG = LoggerFactory.getLogger(LoadStoreTestStatistics.class);
    private final String htmlOutFileName;
    private final String jsonOutFileName;

    public LoadStoreTestAllResults(final String htmlOutFileName, final String jsonOutFileName) {
        this.htmlOutFileName = htmlOutFileName;
        this.jsonOutFileName = jsonOutFileName;
        results = new HashMap<String, LoadStoreTestStatistics>();
    }

    public void addResult(LoadStoreTestStatistics stats) {
        LOG.info(stats.getFileName() + " Statistics:\n" + stats.toString());
        results.put(stats.getFileName(), stats);
    }

    public void publish() throws Exception {
        for (Map.Entry<String, LoadStoreTestStatistics> hCatStatsEntry : results.entrySet()) {
            String fileName = hCatStatsEntry.getKey();
            LoadStoreTestStatistics stats = hCatStatsEntry.getValue();
            LOG.info(fileName + " Statistics:\n" + stats.toString() +
                    "\nChart URL: " + stats.getChartUrl());
        }
        ResultsPublisher publisher = new LoadStoreResultsPublisher(new ArrayList<LoadStoreTestStatistics>(results.values()),
                htmlOutFileName, jsonOutFileName);
        publisher.publishAll();
    }

    public Map<String, LoadStoreTestStatistics> getResults() {
        return results;
    }
}
