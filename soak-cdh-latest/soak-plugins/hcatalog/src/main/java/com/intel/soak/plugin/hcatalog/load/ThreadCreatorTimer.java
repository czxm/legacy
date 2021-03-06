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

package com.intel.soak.plugin.hcatalog.load;

import org.apache.hadoop.mapred.Reporter;
import com.intel.soak.plugin.hcatalog.load.hadoop.IntervalResult;
import com.intel.soak.plugin.hcatalog.load.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A timer thread that keeps on increasing the number of {@link TaskExecutor} at fixed intervals.
 * It also reports progress to Hadoop so that the task doesn't get killed.
 */
public class ThreadCreatorTimer extends TimerTask {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadCreatorTimer.class);
    private int threadCount;
    private final TimeKeeper timeKeeper;
    private final List<Task> tasks;
    private final List<Future<SortedMap<Long, IntervalResult>>> futures;
    private final Reporter reporter;
    private final SortedMap<Long, Integer> threadCountTimeSeries = new TreeMap<Long, Integer>();
    private final int threadIncrementCount;
    private final List<TaskExecutor> taskExecutors =  new ArrayList<TaskExecutor>();
    enum COUNTERS { NUM_THREADS}

    public ThreadCreatorTimer(TimeKeeper timeKeeper, List<Task> tasks, final int threadIncrementCount,
                              List<Future<SortedMap<Long, IntervalResult>>> futures, Reporter reporter) {
        this.timeKeeper = timeKeeper;
        this.tasks = tasks;
        this.threadIncrementCount = threadIncrementCount;
        this.futures = futures;
        this.reporter = reporter;
        threadCount = 0;
        timeKeeper.updateCheckpoint();
    }

    public void run() {
        LOG.info("About to create " + threadIncrementCount + " more threads.");
        final ExecutorService executorPool = Executors.newFixedThreadPool(threadIncrementCount);
        Collection<TaskExecutor> newTaskExecutors = new ArrayList<TaskExecutor>(threadIncrementCount);
        for (int i = 0; i < threadIncrementCount; i++) {
            newTaskExecutors.add(new TaskExecutor(new TimeKeeper(timeKeeper), tasks));
        }
        taskExecutors.addAll(newTaskExecutors);
        for (TaskExecutor taskExecutor : newTaskExecutors) {
            futures.add(executorPool.submit(taskExecutor));
        }
        threadCount += threadIncrementCount;

        // Reporting
        LOG.info("Current number of threads: " + threadCount);
        reporter.progress();
        final String msg = MessageFormat.format("#Threads: {0}, Progress: {1}%",
                threadCount, timeKeeper.getPercentageProgress());
        LOG.info(msg);
        reporter.setStatus(msg);
        reporter.incrCounter(COUNTERS.NUM_THREADS, threadIncrementCount);

        // Update time series of thread Count
        if(timeKeeper.hasNextCheckpointArrived()) {
            threadCountTimeSeries.put(timeKeeper.getCurrentCheckPoint(), getThreadCount());
            timeKeeper.updateCheckpoint();
        }
    }

    public int getThreadCount() {
        return threadCount;
    }

    public SortedMap<Long, Integer> getThreadCountTimeSeries() {
        return threadCountTimeSeries;
    }

    public List<TaskExecutor> getTaskExecutors() {
        return taskExecutors;
    }
}
