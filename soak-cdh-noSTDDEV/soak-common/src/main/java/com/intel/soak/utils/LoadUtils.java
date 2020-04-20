package com.intel.soak.utils;

import com.intel.soak.config.ConfigReader;
import com.intel.soak.config.ConfigUtils;
import com.intel.soak.config.SoakConfig;
import com.intel.soak.driver.IDriver;
import com.intel.soak.logger.SoakLogger;
import com.intel.soak.logger.TransactionLogger;
import com.intel.soak.model.*;
import com.intel.soak.plugin.PluginContextCallable;
import com.intel.soak.plugin.Plugins;
import com.intel.soak.plugin.dto.PluginInfo;
import com.intel.soak.transaction.Transaction;
import com.intel.soak.vuser.VUserData;
import com.intel.soak.vuser.VUserFeeder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class LoadUtils {
    private static Logger LOG = LoggerFactory.getLogger(LoadUtils.class);
    

    @SuppressWarnings("unchecked")
    public static <T extends SoakLogger>T createLogger(String bean, Class<? extends SoakLogger> clz){
        T logger = null;
        try {
            logger = (T)SpringBeanFactoryManager.getSystemAppCxt().getBean(bean, clz);
        } catch (Throwable t) {
            logger = null;
            LOG.error(t.getMessage(), t);
        }
        return logger;
    }

    public static boolean validateLoadConfig(LoadConfig config){
        if(config == null){
            return false;
        }
        String prefix = config.getName();
        if(prefix == null){
            prefix = "soak";
        }
        config.setName(prefix + "-" + Long.toString(System.currentTimeMillis()));
        if(config.getInterval() == null){
            config.setInterval(15);
        }
        return true;
    }

    public static IDriver createDriver(Plugins plugins, LoadConfig loadConfig){
        try {
            String driverName = ConfigUtils.getDriverName(loadConfig);
            return plugins.getDriver(loadConfig.getName(), driverName);
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
        return null;
    }


    public static void prepareParams(List<ParamType> params, Plugins plugins, String jobId, VUserData userData) {
        HashMap<String, Object> vars = new HashMap<String, Object>();
        if(userData != null)
            vars.put("user", userData.getUsername());
        vars.put("plugin_home", plugins.getJobRuntimeResourceDir(jobId));
        vars.put("soak_home", SoakConfig.BaseDir.HOME.toString());
        // first pass for built-in variables
        for(ParamType param : params){
            try{
                String newValue = FileUtils.applyStringTemplate(param.getValue(), vars);
                if(newValue != null)
                    param.setValue(newValue);
            }
            catch(Exception e){
                LOG.error(e.getMessage());
            }
        }
        for(ParamType param : params){
            vars.put(param.getName(), param.getValue());
        }
        // now for all user-defined variables

        do{
            boolean changed = false;
            for(ParamType param : params){
                try{
                    String newValue = FileUtils.applyStringTemplate(param.getValue(), vars);
                    if(newValue != null && !newValue.equals(param.getValue())){
                        param.setValue(newValue);
                        changed = true;
                    }
                }
                catch(Exception e){
                    LOG.error(e.getMessage());
                }
            }
            if(!changed)
                break;
        }while(true);
    }

    public static List<ParamType> cloneParams(List<ParamType> params){
        List<ParamType> newParams = new ArrayList<ParamType>();
        for(ParamType p : params){
            ParamType np = new ParamType();
            np.setValue(p.getValue());
            np.setName(p.getName());
            newParams.add(np);
        }
        return newParams;
    }

    public static List<Transaction> createTransactions(Plugins plugins, LoadConfig loadConfig, VUserData data, List<TransactionLogger> loggers, List<TransactionType> transTypes, IDriver driver){
        ArrayList<Transaction> trans = new ArrayList<Transaction>();
        for (int i = 0; i < transTypes.size(); i++) {
            try {
                TransactionType tt = transTypes.get(i);
                Transaction tran = plugins.getTransaction(loadConfig.getName(), tt.getName());
                List<ParamType> params = cloneParams(tt.getParam());
                if(loggers != null)
                    tran.setLogger(loggers.get(i));
                if(data != null){
                    tran.setUserData(data);
                    if(loggers != null){
                        loggers.get(i).setUser(data.getUsername());
                    }
                }
                trans.add(tran);
                if(driver != null){
                    prepareParams(params, plugins, loadConfig.getName(), data);
                    tran.setParams(params);
                    driver.prepareTransaction(tran);
                }
            } catch (Throwable t) {
                LOG.error(t.getMessage(), t);
            }
        }
        return trans;
    }

    public static List<TransactionLogger> createLoggers(int size){
        List<TransactionLogger> loggers = new ArrayList<TransactionLogger>();
        for(int i = 0; i < size; i++){
            TransactionLogger logger = LoadUtils.createLogger(SoakConfig.TxLogger, TransactionLogger.class);
            loggers.add(logger);
        }
        return loggers;
    }

    public static VUserFeeder createUserFeeder(Plugins plugins, LoadConfig config){
        try {
            String vuserfeeder = ConfigUtils.getVUserFeeder(config);
            VUserFeeder feeder = (VUserFeeder) plugins.getPluginObj(config.getName(), vuserfeeder);
            List<ParamType> params = cloneParams(config.getVirtualUserConfig().getParam());
            prepareParams(params, plugins, config.getName(), null);
            feeder.setParams(params);
            return feeder;
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
        return null;
    }


    public static List<PluginInfo> collectPlugins(Plugins plugins, final LoadConfig config) {
        List<String> components = new ArrayList<String>();
        String driver = ConfigUtils.getDriverName(config);
        if (driver != null)
            components.add(driver);
        for (TransactionType tran : ConfigUtils.getTransactions(config)) {
            if (tran.getName() != null) {
                components.add(tran.getName());
            }
        }
        return plugins.getPluginsInfoByComponentIds(components);
    }

    public static void loadPlugins(Plugins plugins, final LoadConfig config) {
        List<PluginInfo> ps = collectPlugins(plugins, config);
        String cp = ConfigUtils.getDriverParam(config, "CLASSPATH");
        plugins.loadAndRegisterPlugins(config.getName(), ps, cp);
    }

    public static <T>T callPluginCallable(ExecutorService executor, String jobId, Plugins plugins, Callable<T> action){
        try{
            Future<T> f = executor.submit(new PluginContextCallable<T>(jobId, plugins, action));
            return f.get();
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public static <T>Future<T> invokePluginCallable(ExecutorService executor, String jobId, Plugins plugins, Callable<T> action){
        return executor.submit(new PluginContextCallable<T>(jobId, plugins, action));
    }

    public static <T>T findNamedObject(T[] items, T item, String method){
        try{
            for(T n : items){
                if(n != null){
                    if(method != null){
                        Method nameMethod = null;
                        for(Method m : n.getClass().getMethods()){
                            if(m.getName().equals(method)){
                                nameMethod = m;
                                break;
                            }
                        }
                        if(nameMethod != null){
                            Object c = nameMethod.invoke(n);
                            if(c != null && c.equals(nameMethod.invoke(item))){
                            return n;
                            }
                        }
                    }
                    else if(n.equals(item)){
                        return n;
                    }
                }
            }
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
    
    public static String[] getTransactionNames(Object[] transactions) {
        int dupCount = 0;
        String[] names = new String[transactions.length];
        for (int i = 0; i < transactions.length; i++) {
            Object t = transactions[i];
            String name = t.getClass().getSimpleName();
            name = name.replace("Transaction", "");
            if(findNamedObject(names, name, null) != null){
                dupCount++;
                names[i] = name + dupCount;
            }
            else{
                names[i] = name;
            }
        }
        return names;
    }

    public static MergeConfig getMergeByLoad(LoadConfig config){
        Plugins plugins = SpringBeanFactoryManager.getSystemAppCxt().getBean("plugins", Plugins.class);
        try{
            SoakConfig soakConfig = SpringBeanFactoryManager.getSystemAppCxt().getBean("soakConfig", SoakConfig.class);
            HashMap<String, Object> vars = new HashMap<String, Object>();
            vars.put("interval", config.getInterval());
            vars.put("nodes", soakConfig.getConfig(SoakConfig.ConfigKey.GangliaHosts));
            vars.put("job", config.getName());
            vars.put("Driver", ConfigUtils.getDriverName(config));
            loadPlugins(plugins, config);
            List<Transaction> txList = createTransactions(plugins, config, null, null,
                    ConfigUtils.getTransactions(config), null);
            String[] txNames = getTransactionNames(txList.toArray(new Transaction[]{}));
            vars.put("TransactionList", txNames);
            BatchConfigType batchConfig = config.getBatchConfig();
            String activeUsers = Integer.toString(config.getVirtualUserConfig().getTotal());
            if(batchConfig != null){
                StringBuilder sb = new StringBuilder("1");
                for(int i = 2; i <= config.getVirtualUserConfig().getTotal(); i++){
                    sb.append(" ");
                    sb.append(Integer.toString(i));
                }
                activeUsers = sb.toString();
            } 
            vars.put("ActiveUsers", activeUsers);
            MergeConfig result = getMergeDefaultConfig(vars);
            if(config.getResult() != null)
                mergePluginConfig(result, config.getResult());
            return result;
        }
        catch(Exception e){
            LOG.error(e.getMessage(), e);
        }
        finally{
            plugins.destroy(config.getName());
        }
        return null;
    }
    
    //get the default mergeconfig
    public static MergeConfig getMergeDefaultConfig(HashMap<String, Object> vars) throws Exception{
        String content = FileUtils.applyTemplate("mergePluginDefault.vm", vars);
        InputStream in = new ByteArrayInputStream(content.getBytes());
        MergeConfig config = new ConfigReader<MergeConfig>().load(in,
                MergeConfig.class);
        return config;
    }

    //implement merge
    public static void mergePluginConfig(MergeConfig to, ResultType with) throws Exception{
        merge(to.getLoadMeasureConfig(), with.getLoadMeasureConfig());
        merge(to.getGangliaMetricsConfig(), with.getGangliaMetricsConfig());
        merge(to.getSummaryConfig(), with.getSummaryConfig());
    }

    public static <M> void merge(List<M> target, List<M> destination, String method) throws Exception {
        for(M m : destination){
            M sameObj = (M)findNamedObject(target.toArray(), m, method);
            if(sameObj == null){
                target.add(m);
            }
            else{
                merge(sameObj, m);
            }
        }
    }
    
    //implement merge of two chartconfig
    public static <M> void merge(M target,M destination) throws Exception{
        if(target != null && destination != null){
            BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass());
            for(PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()){
                if(descriptor.getWriteMethod()!=null){
                    Object overrideValue = descriptor.getReadMethod().invoke(destination);
                    if(overrideValue != null){
                        if(!descriptor.getPropertyType().getPackage().getName().contains("com.intel.soak.model"))
                            descriptor.getWriteMethod().invoke(target,overrideValue);
                        else
                            merge(descriptor.getReadMethod().invoke(target), overrideValue);
                    }
                }
            }
            
            for(MethodDescriptor descriptor : beanInfo.getMethodDescriptors()){
                if(descriptor.getName().equals("getChartConfig")){
                    List<ChartConfigType> destCharts = (List<ChartConfigType>) descriptor.getMethod().invoke(destination);
                    List<ChartConfigType> targetCharts = (List<ChartConfigType>) descriptor.getMethod().invoke(target);
                    merge(targetCharts, destCharts, "getName");
                }
                else if(descriptor.getName().equals("getYSeries") || descriptor.getName().equals("getSecondYSeries")){
                    YSeriesType destSeries = (YSeriesType) descriptor.getMethod().invoke(destination);
                    YSeriesType targetSeries = (YSeriesType) descriptor.getMethod().invoke(target);
                    merge(targetSeries, destSeries);
                }
                else if(descriptor.getName().equals("getYSerie")){
                    List<YSerieType> destSeries = (List<YSerieType>) descriptor.getMethod().invoke(destination);
                    List<YSerieType> targetSeries = (List<YSerieType>) descriptor.getMethod().invoke(target);
                    merge(targetSeries, destSeries, "getLabel");
                }
            }
        }
    }
}
