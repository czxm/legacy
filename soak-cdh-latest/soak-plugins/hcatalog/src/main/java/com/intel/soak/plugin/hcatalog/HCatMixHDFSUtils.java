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

package com.intel.soak.plugin.hcatalog;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

/**
 * Author: malakar
 */
public class HCatMixHDFSUtils{
    private static Configuration conf;
    private static FileSystem dfs;
    static {
        try {
            conf = new Configuration();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't initialize DFS client");

        }
    }

    public static boolean exists(String path) throws IOException {
        Path dstPath = new Path(path) ;
        dfs = dstPath.getFileSystem(conf);
        return dfs.exists(dstPath);
    }

    public static boolean deleteRecursive(String path) throws IOException {
        Path dstPath = new Path(path) ;
        dfs = dstPath.getFileSystem(conf);
        return dfs.delete(dstPath, true);
    }
}
