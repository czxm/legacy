package com.intel.cedar.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.ejb.EntityManagerFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.emory.mathcs.backport.java.util.Collections;

public class DatabaseUtil {
    static Logger LOG = LoggerFactory.getLogger(DatabaseUtil.class);
    public static int MAX_FAIL = 5;
    private static int failCount = 0;
    private static Map<String, EntityManagerFactoryImpl> emf = new ConcurrentSkipListMap<String, EntityManagerFactoryImpl>();
    private static List<Exception> illegalAccesses = Collections
            .synchronizedList(Lists.newArrayList());
    private static boolean accessEnabled = false;

    public static void enableAccess() throws Exception {
        if (!accessEnabled) {
            for (String context : LocalDatabaseConfig.getContexts()) {
                Ejb3Configuration config = new Ejb3Configuration();
                LocalDatabaseConfig.configureHibernate(config, context);
                LocalDatabaseConfig.configurePool(config, context);
                LocalDatabaseConfig.configureCache(config, context);
                LocalDatabaseConfig.configureEntities(config, context);
                try {
                    DatabaseUtil.registerPersistenceContext(context, config);
                } catch (Throwable t) {
                    t.printStackTrace();
                    System.exit(1);
                }
            }
            accessEnabled = true;
        }
    }

    public static EntityManagerFactoryImpl registerPersistenceContext(
            final String persistenceContext, final Ejb3Configuration config) {
        synchronized (EntityWrapper.class) {
            if (illegalAccesses != null && !illegalAccesses.isEmpty()) {
                for (Exception e : illegalAccesses) {
                    LOG.error("", e);
                }
                System.exit(1);
            } else if (!emf.containsKey(persistenceContext)) {
                illegalAccesses = null;
                EntityManagerFactoryImpl entityManagerFactory = (EntityManagerFactoryImpl) config
                        .buildEntityManagerFactory();// Persistence.createEntityManagerFactory(
                // persistenceContext );
                LOG.info("-> Setting up persistence context for : "
                        + persistenceContext);
                emf.put(persistenceContext, entityManagerFactory);
            }
            return emf.get(persistenceContext);
        }
    }

    public static EntityManagerFactoryImpl getEntityManagerFactory(
            final String persistenceContext) {
        if (!emf.containsKey(persistenceContext)) {
            RuntimeException e = new RuntimeException(
                    "Attempting to access an entity wrapper before the database has been configured.");
            illegalAccesses = illegalAccesses == null ? Collections
                    .synchronizedList(Lists.newArrayList()) : illegalAccesses;
            illegalAccesses.add(e);
            throw e;
        }
        return emf.get(persistenceContext);
    }
}
