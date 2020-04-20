package com.intel.cedar.service.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ContextListener implements ServletContextListener {
	private static ApplicationContext factory;
	/**
	 * Initialize Spring application context
	*/
	private void initSpring(ServletContextEvent evt) {
		factory = WebApplicationContextUtils.getWebApplicationContext(evt.getServletContext());
	}
	  
	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		initSpring(event);
	}
	
	public static ApplicationContext getFactory() {
		return factory;
	}

}
