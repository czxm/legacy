package com.intel.cedar.engine;

import java.util.HashMap;

import com.intel.cedar.engine.impl.ExtensiveEngine;

public class EngineFactory {
    private static HashMap<String, Class<?>> supportedEngines;
    private static HashMap<String, IEngine> engines;
    private static EngineFactory singleton;

    public static synchronized EngineFactory getInstance() {
        if (singleton == null) {
            singleton = new EngineFactory();
        }
        return singleton;
    }

    private EngineFactory() {
        supportedEngines = new HashMap<String, Class<?>>();
        supportedEngines.put("builtin", ExtensiveEngine.class);
        engines = new HashMap<String, IEngine>();
    }

    protected IEngine createEngine(String engine) {
        Class<?> clz = (Class<?>) supportedEngines.get(engine);
        if (clz != null) {
            try {
                return (IEngine) clz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("can't create engine");
            }
        } else
            throw new RuntimeException("can't create engine");
    }

    public IEngine getEngine(String engine) {
        if (!engines.containsKey(engine))
            engines.put(engine, createEngine(engine));
        return engines.get(engine);
    }

    public IEngine getEngine() {
        return getEngine("builtin");
    }
}
