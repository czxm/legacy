package com.intel.bigdata.agent;

import akka.actor.ActorSystem;
import akka.agent.Agent;
import com.intel.bigdata.common.protocol.AgentConfig;
import com.intel.bigdata.common.util.SpringExtension;
import com.intel.bigdata.common.util.platform.*;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * The application configuration.
 */
@Configuration
@Import(com.intel.bigdata.agent.ganglia.SpringAppConfiguration.class)
class SpringAppConfiguration {

	// the application context is needed to initialize the Akka Spring Extension
	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * Actor system singleton for this application.
	 */
	@Bean
	public ActorSystem actorSystem() {
		ActorSystem system = ActorSystem.create("agent", ConfigFactory.load().getConfig("agent"));
		// initialize the application context in the Akka Spring Extension
		SpringExtension.SpringExtProvider.get(system).initialize(applicationContext);
		return system;
	}

    @Bean
    public ProcessManager processManager() {
        return new ProcessManagerImpl();
    }

    @Bean
    public PackageManager packageManager() {
        return new YumPackageManagerImpl();
    }

    @Bean
    public LocalFS localFS() {
        return new LocalFSImpl();
    }

    @Bean(name = "agentConfig")
    public Agent<AgentConfig> agentConfig() {
        return Agent.<AgentConfig>create(ConfigManager.readAgentConfig(), actorSystem().dispatcher());
    }

}
