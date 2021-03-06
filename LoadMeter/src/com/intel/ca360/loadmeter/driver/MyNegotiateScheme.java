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
 */

package com.intel.ca360.loadmeter.driver;

import java.io.IOException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.InvalidCredentialsException;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.impl.auth.AuthSchemeBase;
import org.apache.http.impl.auth.SpnegoTokenGenerator;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharArrayBuffer;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;


/**
 * SPNEGO (Simple and Protected GSSAPI Negotiation Mechanism) authentication
 * scheme.
 *
 * @since 4.1
 */
public class MyNegotiateScheme extends AuthSchemeBase {

    enum State {
        UNINITIATED,
        CHALLENGE_RECEIVED,
        TOKEN_GENERATED,
        FAILED,
    }

    private static final String SPNEGO_OID       = "1.3.6.1.5.5.2";
    private static final String KERBEROS_OID     = "1.2.840.113554.1.2.2";

    private final Log log = LogFactory.getLog(getClass());

    private final SpnegoTokenGenerator spengoGenerator;

    private final boolean stripPort;

    private GSSContext gssContext = null;

    /** Authentication process state */
    private State state;

    /** base64 decoded challenge **/
    private byte[] token;

    private Oid negotiationOid = null;

    /**
     * Default constructor for the Negotiate authentication scheme.
     *
     */
    public MyNegotiateScheme(final SpnegoTokenGenerator spengoGenerator, boolean stripPort) {
        super();
        this.state = State.UNINITIATED;
        this.spengoGenerator = spengoGenerator;
        this.stripPort = stripPort;
    }

    public MyNegotiateScheme(final SpnegoTokenGenerator spengoGenerator) {
        this(spengoGenerator, false);
    }

    public MyNegotiateScheme() {
        this(null, false);
    }

    /**
     * Tests if the Negotiate authentication process has been completed.
     *
     * @return <tt>true</tt> if authorization has been processed,
     *   <tt>false</tt> otherwise.
     *
     */
    public boolean isComplete() {
        return this.state == State.TOKEN_GENERATED || this.state == State.FAILED;
    }

    /**
     * Returns textual designation of the Negotiate authentication scheme.
     *
     * @return <code>Negotiate</code>
     */
    public String getSchemeName() {
        return "Negotiate";
    }

    @Deprecated
    public Header authenticate(
            final Credentials credentials,
            final HttpRequest request) throws AuthenticationException {
        return authenticate(credentials, request, null);
    }

    protected GSSManager getManager() {
        return GSSManager.getInstance();
    }
    
    static GSSCredential getCredential(Subject subject) throws PrivilegedActionException {
	    PrivilegedExceptionAction<GSSCredential> action = new PrivilegedExceptionAction<GSSCredential>() {
	        public GSSCredential run() throws GSSException {
	            return GSSManager.getInstance().createCredential(null, Integer.MAX_VALUE, new Oid("1.3.6.1.5.5.2"), 1);
	        }
	    };
	    return (GSSCredential)Subject.doAs(subject, action);
    }    
    
    /**
     * Produces Negotiate authorization Header based on token created by
     * processChallenge.
     *
     * @param credentials Never used be the Negotiate scheme but must be provided to
     * satisfy common-httpclient API. Credentials from JAAS will be used instead.
     * @param request The request being authenticated
     *
     * @throws AuthenticationException if authorisation string cannot
     *   be generated due to an authentication failure
     *
     * @return an Negotiate authorisation Header
     */
    @Override
    public Header authenticate(
            final Credentials credentials,
            final HttpRequest request,
            final HttpContext context) throws AuthenticationException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        if (state != State.CHALLENGE_RECEIVED) {
            throw new IllegalStateException(
                    "Negotiation authentication process has not been initiated");
        }
        try {
            String key = null;
            if (isProxy()) {
                key = ExecutionContext.HTTP_PROXY_HOST;
            } else {
                key = ExecutionContext.HTTP_TARGET_HOST;
            }
            HttpHost host = (HttpHost) context.getAttribute(key);
            if (host == null) {
                throw new AuthenticationException("Authentication host is not set " +
                        "in the execution context");
            }
            String authServer;
            if (!this.stripPort && host.getPort() > 0) {
                authServer = host.toHostString();
            } else {
                authServer = host.getHostName();
            }

            if (log.isDebugEnabled()) {
                log.debug("init " + authServer);
            }
            
            GSSCredential cred = null;         
            try{
                if(credentials instanceof KerbCredential){
                	cred = getCredential(((KerbCredential)credentials).getSubject());
                }
            }
            catch(Exception e){
            	throw new GSSException(GSSException.BAD_MECH);
            }
            
            /* Using the SPNEGO OID is the correct method.
             * Kerberos v5 works for IIS but not JBoss. Unwrapping
             * the initial token when using SPNEGO OID looks like what is
             * described here...
             *
             * http://msdn.microsoft.com/en-us/library/ms995330.aspx
             *
             * Another helpful URL...
             *
             * http://publib.boulder.ibm.com/infocenter/wasinfo/v7r0/index.jsp?topic=/com.ibm.websphere.express.doc/info/exp/ae/tsec_SPNEGO_token.html
             *
             * Unfortunately SPNEGO is JRE >=1.6.
             */

            /** Try SPNEGO by default, fall back to Kerberos later if error */
            negotiationOid  = new Oid(SPNEGO_OID);

            boolean tryKerberos = false;
            try {           
                GSSManager manager = getManager();
                GSSName serverName = manager.createName("HTTP/" + authServer, null);
                gssContext = manager.createContext(
                        serverName.canonicalize(negotiationOid), negotiationOid, cred,
                        GSSContext.DEFAULT_LIFETIME);
                gssContext.requestMutualAuth(true);
                gssContext.requestCredDeleg(true);
            } catch (GSSException ex){
                // BAD MECH means we are likely to be using 1.5, fall back to Kerberos MECH.
                // Rethrow any other exception.
                if (ex.getMajor() == GSSException.BAD_MECH ){
                    log.debug("GSSException BAD_MECH, retry with Kerberos MECH");
                    tryKerberos = true;
                } else {
                    throw ex;
                }

            } catch (Exception e){
            	throw new GSSException(GSSException.BAD_MECH);
            }
            
            if (tryKerberos){
                /* Kerberos v5 GSS-API mechanism defined in RFC 1964.*/
                log.debug("Using Kerberos MECH " + KERBEROS_OID);
                negotiationOid  = new Oid(KERBEROS_OID);
                GSSManager manager = getManager();
                GSSName serverName = manager.createName("HTTP/" + authServer, null);
                gssContext = manager.createContext(
                        serverName.canonicalize(negotiationOid), negotiationOid, cred,
                        GSSContext.DEFAULT_LIFETIME);
                gssContext.requestMutualAuth(true);
                gssContext.requestCredDeleg(true);
            }
            if (token == null) {
                token = new byte[0];
            }
            token = gssContext.initSecContext(token, 0, token.length);
            if (token == null) {
                state = State.FAILED;
                throw new AuthenticationException("GSS security context initialization failed");
            }

            /*
             * IIS accepts Kerberos and SPNEGO tokens. Some other servers Jboss, Glassfish?
             * seem to only accept SPNEGO. Below wraps Kerberos into SPNEGO token.
             */
            if (spengoGenerator != null && negotiationOid.toString().equals(KERBEROS_OID)) {
                token = spengoGenerator.generateSpnegoDERObject(token);
            }

            state = State.TOKEN_GENERATED;
            String tokenstr = new String(Base64.encodeBase64(token, false));
            if (log.isDebugEnabled()) {
                log.debug("Sending response '" + tokenstr + "' back to the auth server");
            }
            return new BasicHeader("Authorization", "Negotiate " + tokenstr);
        } catch (GSSException gsse) {
            state = State.FAILED;
            if (gsse.getMajor() == GSSException.DEFECTIVE_CREDENTIAL
                    || gsse.getMajor() == GSSException.CREDENTIALS_EXPIRED)
                throw new InvalidCredentialsException(gsse.getMessage(), gsse);
            if (gsse.getMajor() == GSSException.NO_CRED )
                throw new InvalidCredentialsException(gsse.getMessage(), gsse);
            if (gsse.getMajor() == GSSException.DEFECTIVE_TOKEN
                    || gsse.getMajor() == GSSException.DUPLICATE_TOKEN
                    || gsse.getMajor() == GSSException.OLD_TOKEN)
                throw new AuthenticationException(gsse.getMessage(), gsse);
            // other error
            throw new AuthenticationException(gsse.getMessage());
        } catch (IOException ex){
            state = State.FAILED;
            throw new AuthenticationException(ex.getMessage());
        }
    }


    /**
     * Returns the authentication parameter with the given name, if available.
     *
     * <p>There are no valid parameters for Negotiate authentication so this
     * method always returns <tt>null</tt>.</p>
     *
     * @param name The name of the parameter to be returned
     *
     * @return the parameter with the given name
     */
    public String getParameter(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter name may not be null");
        }
        return null;
    }

    /**
     * The concept of an authentication realm is not supported by the Negotiate
     * authentication scheme. Always returns <code>null</code>.
     *
     * @return <code>null</code>
     */
    public String getRealm() {
        return null;
    }

    /**
     * Returns <tt>true</tt>.
     * Negotiate authentication scheme is connection based.
     *
     * @return <tt>true</tt>.
     */
    public boolean isConnectionBased() {
        return true;
    }

    @Override
    protected void parseChallenge(
            final CharArrayBuffer buffer,
            int beginIndex, int endIndex) throws MalformedChallengeException {
        String challenge = buffer.substringTrimmed(beginIndex, endIndex);
        if (log.isDebugEnabled()) {
            log.debug("Received challenge '" + challenge + "' from the auth server");
        }
        if (state == State.UNINITIATED) {
            token = new Base64().decode(challenge.getBytes());
            state = State.CHALLENGE_RECEIVED;
        } else {
            log.debug("Authentication already attempted");
            state = State.FAILED;
        }
    }

}
