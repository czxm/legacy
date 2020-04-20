package com.intel.ca360.loadmeter.driver;

import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.impl.cookie.BasicDomainHandler;
import org.apache.http.impl.cookie.BasicPathHandler;
import org.apache.http.impl.cookie.BrowserCompatSpec;

class BrowserCompatSpecEx extends BrowserCompatSpec{
	public BrowserCompatSpecEx(){
		super();
        registerAttribHandler("path", new BasicPathHandler(){
        	@Override
        	public void validate(Cookie cookie, CookieOrigin origin){
        		match(cookie, origin);
        	}
        });
        registerAttribHandler("domain", new BasicDomainHandler(){
            public boolean match(Cookie cookie, CookieOrigin origin)
            {
                if(cookie == null)
                    throw new IllegalArgumentException("Cookie may not be null");
                if(origin == null)
                    throw new IllegalArgumentException("Cookie origin may not be null");
                String host = origin.getHost();
                String domain = cookie.getDomain();
                if(domain == null)
                    return false;
                if(domain.startsWith("."))
                	return host.endsWith(domain) || host.equals(domain.substring(1));
                else
                    return host.equals(domain);
            }
        });
	}
}