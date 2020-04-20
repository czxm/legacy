/*
 * Copyright 2011-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intel.soak.plugin.mapreduce;

import com.intel.soak.logger.TransactionLogger;
import org.apache.hadoop.conf.Configuration;
import org.springframework.core.io.Resource;

/**
 * Base class exposing setters and handling the so-called Hadoop Generic options (files/libjars/archives) properties. 
 * 
 * @author Costin Leau
 */
/**
 * Modified for soak
 */
public abstract class JobGenericOptions {

    protected TransactionLogger log;

    protected Resource[] files, libJars, archives;
    protected String user;

    public void setTransactionLogger(TransactionLogger log) {
        this.log = log;
    }

    public void setLibs(Resource... libJars) {
        this.libJars = libJars;
    }

    public void setFiles(Resource... files) {
        this.files = files;
    }

    public void setArchives(Resource... archives) {
        this.archives = archives;
    }

    public void buildGenericOptions(Configuration cfg) {
        // set the GenericOptions properties manual to avoid the changes between Hadoop 1.x and 2.x
        cfg.setBoolean("mapred.used.genericoptionsparser", true);

        ConfigurationUtils.addFiles(cfg, files);
        ConfigurationUtils.addLibs(cfg, libJars);
        ConfigurationUtils.addArchives(cfg, archives);
    }

    public void setUser(String user) {
        this.user = user;
    }

    public abstract int runCode() throws Exception;


}
