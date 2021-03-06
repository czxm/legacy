/*
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package com.intel.ca360.loadmeter.driver;

import org.apache.http.annotation.Immutable;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.impl.auth.SpnegoTokenGenerator;
import org.apache.http.params.HttpParams;

/**
 * SPNEGO (Simple and Protected GSSAPI Negotiation Mechanism) authentication
 * scheme factory.
 *
 * @since 4.1
 */
@Immutable
public class MyNegotiateSchemeFactory implements AuthSchemeFactory {

    private final SpnegoTokenGenerator spengoGenerator;
    private final boolean stripPort;

    public MyNegotiateSchemeFactory(final SpnegoTokenGenerator spengoGenerator, boolean stripPort) {
        super();
        this.spengoGenerator = spengoGenerator;
        this.stripPort = stripPort;
    }

    public MyNegotiateSchemeFactory(final SpnegoTokenGenerator spengoGenerator) {
        this(spengoGenerator, false);
    }

    public MyNegotiateSchemeFactory() {
        this(null, false);
    }

    public AuthScheme newInstance(final HttpParams params) {
        return new MyNegotiateScheme(this.spengoGenerator, this.stripPort);
    }

    public boolean isStripPort() {
        return stripPort;
    }

    public SpnegoTokenGenerator getSpengoGenerator() {
        return spengoGenerator;
    }

}
