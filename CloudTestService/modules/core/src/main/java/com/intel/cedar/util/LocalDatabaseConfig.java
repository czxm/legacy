package com.intel.cedar.util;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.hibernate.MappingException;
import org.hibernate.ejb.Ejb3Configuration;
import org.hsqldb.ServerConstants;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class LocalDatabaseConfig {
    private static LocalDatabaseConfig singleton = new LocalDatabaseConfig();
    private static Logger LOG = LoggerFactory
            .getLogger(LocalDatabaseConfig.class);

    public static LocalDatabaseConfig getInstance() {
        return singleton;
    }

    public static void setInstance(LocalDatabaseConfig dbConfig) {
        singleton = dbConfig;
    }

    public static List<String> getContexts() {
        List<String> contexts = Lists.newArrayList();
        for (Internal i : Internal.values()) {
            contexts.add(i.getDatabaseName());
        }
        return contexts;
    }

    enum Internal {
        general;
        public String getDatabaseName() {
            return "cedar_" + this.name();
        }

        public Properties getProperties() {
            Properties props = new Properties();
            props.setProperty(ServerConstants.SC_KEY_DATABASE + "."
                    + this.ordinal(), SubDirectory.DB.toString()
                    + File.separator + this.getDatabaseName());
            props.setProperty(ServerConstants.SC_KEY_DBNAME + "."
                    + this.ordinal(), this.getDatabaseName());
            return props;
        }
    }

    public static Properties getProperties() {
        Properties props = new Properties();
        props.setProperty(ServerConstants.SC_KEY_NO_SYSTEM_EXIT, Boolean.TRUE
                .toString());
        props.setProperty(ServerConstants.SC_KEY_PORT, "9001");
        props.setProperty(ServerConstants.SC_KEY_REMOTE_OPEN_DB, Boolean.TRUE
                .toString());
        // props.setProperty( ServerConstants.SC_KEY_TLS, Boolean.TRUE.toString(
        // ) );
        for (LocalDatabaseConfig.Internal i : LocalDatabaseConfig.Internal
                .values()) {
            props.putAll(i.getProperties());
        }
        return props;
    }

    public static String makeUri(String address) {
        return String.format("jdbc:hsqldb:hsql://%s:%d/", address, 9001);
    }

    public static void configurePool(Ejb3Configuration config, String context)
            throws ClassNotFoundException, ProxoolException {
        Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
        Properties poolProps = new Properties();
        poolProps.put("proxool.simultaneous-build-throttle", "48");
        poolProps.put("proxool.minimum-connection-count", "16");
        poolProps.put("proxool.maximum-connection-count", "128");
        // poolProps.put("proxool.house-keeping-test-sql",
        // "SELECT * FROM COUNTERS;");
        poolProps.put("user", "sa");
        poolProps.put("password", Hashes.getHexSignature());

        String dbDriver = "org.hsqldb.jdbcDriver";
        String url = "proxool." + context + ":" + dbDriver + ":"
                + makeUri("127.0.0.1") + context;
        Log.info("Proxool config for " + context);
        ProxoolFacade.registerConnectionPool(url, poolProps);

        config.setProperty("hibernate.bytecode.use_reflection_optimizer",
                "true");
        config.setProperty("hibernate.cglib.use_reflection_optimizer", "true");
        config.setProperty("hibernate.dialect",
                "org.hibernate.dialect.HSQLDialect");
        config.setProperty("hibernate.connection.provider_class",
                "org.hibernate.connection.ProxoolConnectionProvider");
        config.setProperty("hibernate.proxool.pool_alias", context);
        config.setProperty("hibernate.proxool.existing_pool", "true");
    }

    public static void configureCache(Ejb3Configuration config, String context) {
        config.setProperty("hibernate.cache.provider_class",
                "org.hibernate.cache.NoCacheProvider");
        config.setProperty("hibernate.cache.provider_class",
                "net.sf.ehcache.hibernate.SingletonEhCacheProvider");
        config.setProperty("hibernate.cache.region_prefix", context + "_cache");
        config.setProperty("hibernate.cache.use_second_level_cache", "true");
        config.setProperty("hibernate.cache.use_query_cache", "true");
        config.setProperty("hibernate.cache.use_structured_entries", "true");
    }

    public static void configureHibernate(Ejb3Configuration config,
            String context) {
        config
                .setProperty("hibernate.archive.autodetection",
                        "jar, class, hbm");
        config.setProperty("hibernate.show_sql", "false");
        config.setProperty("hibernate.format_sql", "false");
        // config.setProperty("hibernate.connection.autocommit", "true");
        config.setProperty("hibernate.hbm2ddl.auto", "update");
        config.setProperty("hibernate.generate_statistics", "false");
        config.setProperty("javax.persistence.validation.mode", "none");
    }

    public static void configureEntities(Ejb3Configuration config,
            String context) throws MappingException, ClassNotFoundException {
        config.addAnnotatedClass(Class
                .forName("com.intel.cedar.core.entities.CloudInfo"));
        config.addAnnotatedClass(Class
                .forName("com.intel.cedar.core.entities.MachineInfo"));
        config.addAnnotatedClass(Class
                .forName("com.intel.cedar.core.entities.MachineTypeInfo"));
        config.addAnnotatedClass(Class
                .forName("com.intel.cedar.core.entities.VolumeInfo"));
        config.addAnnotatedClass(Class
                .forName("com.intel.cedar.core.entities.InstanceInfo"));
        config.addAnnotatedClass(Class
                .forName("com.intel.cedar.core.entities.GatewayInfo"));
        config.addAnnotatedClass(Class
                .forName("com.intel.cedar.core.entities.PhysicalNodeInfo"));
        config.addAnnotatedClass(Class
                .forName("com.intel.cedar.core.entities.NATInfo"));
        config.addAnnotatedClass(Class
                .forName("com.intel.cedar.core.entities.KeyPairDescription"));
        config.addAnnotatedClass(Class
                .forName("com.intel.cedar.core.entities.MachineMappingInfo"));
        config.addAnnotatedClass(Class
                .forName("com.intel.cedar.core.entities.CloudNodeInfo"));
        config.addAnnotatedClass(Class
                .forName("com.intel.cedar.feature.FeatureInfo"));
        config.addAnnotatedClass(Class
                .forName("com.intel.cedar.tasklet.TaskletInfo"));
        config.addAnnotatedClass(Class
                .forName("com.intel.cedar.engine.HistoryInfo"));
        config.addAnnotatedClass(Class
                .forName("com.intel.cedar.engine.FeaturePropsInfo"));
        config.addAnnotatedClass(Class
                .forName("com.intel.cedar.user.UserInfo"));
        config.addAnnotatedClass(Class
                .forName("com.intel.cedar.user.SessionInfo"));
    }
}
