package com.intel.soak.main;

import com.intel.soak.SoakContainer;
import com.intel.soak.bootstrap.Bootstrap;
import com.intel.soak.config.ConfigUtils;
import com.intel.soak.config.SoakConfig;
import com.intel.soak.model.LoadConfig;
import com.intel.soak.model.MergeConfig;
import com.intel.soak.utils.SoakServerUtils;
import com.intel.soak.utils.SpringBeanFactoryManager;
import com.intel.soak.utils.ThreadUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class Runner {

    protected static Log LOG = LogFactory.getLog(Runner.class);

    private static String getUsage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: sh run.sh <load_config> <merge_config>\n");
        sb.append("	<load_config>  : Specify load config file path\n");
        sb.append(" <merge_config>  : Specify merge config file path\n");
        return sb.toString();
    }

    private static void initSoakContainer(String[] args, List<LoadConfig> loadList)
            throws Throwable {
        try {
            List<Object> list = ConfigUtils.parseParams(args);
            SoakContainer container = SpringBeanFactoryManager.getSystemAppCxt()
                    .getBean(SoakConfig.Container, SoakContainer.class);

            for (LoadConfig config : loadList) {
                container.submit(config);
            }

            while (container.list().size() > 0) {
                ThreadUtils.sleep(5);
            }
        } catch (Exception e) {
            LOG.error("Error while initializing soak container.", e);
            throw e;
        }
    }

    public static void main(String[] args) throws Throwable {
        try {
            if (!SoakServerUtils.parseArguments(args, getUsage(), System.out)) {
                System.exit(0);
            }

            Bootstrap.start();

            List<Object> list = ConfigUtils.parseParams(args);
            List<LoadConfig> loadList = ConfigUtils.collectConfig(list, LoadConfig.class);

            initSoakContainer(args, loadList);

            List<MergeConfig> immediateMerge = ConfigUtils.genImmediateMergeConfigs(loadList);
            SoakServerUtils.generateReport(immediateMerge);

            List<MergeConfig> mergeList = ConfigUtils.collectConfig(list, MergeConfig.class);
            SoakServerUtils.generateReport(mergeList);

        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        } finally {
            Bootstrap.stop();
        }
    }

}
