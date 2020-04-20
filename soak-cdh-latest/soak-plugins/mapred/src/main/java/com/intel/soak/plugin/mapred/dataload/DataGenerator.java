package com.intel.soak.plugin.mapred.dataload;

import com.intel.soak.plugin.mapred.dataload.seq.SeqDataCriterionGenerator;
import com.intel.soak.plugin.mapred.dataload.seq.SeqDataGenerator;
import com.intel.soak.plugin.mapred.dataload.txt.TxtDataCriterionGenerator;
import com.intel.soak.plugin.mapred.dataload.txt.TxtDataGenerator;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 11/12/13
 * Time: 10:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataGenerator {

    public void gen(DataLoaderConfig config) {
        genTxt(config);
        genSeq(config);
    }

    private void genTxt(DataLoaderConfig config) {
        IDataGenerator dataGen = new TxtDataGenerator();
        dataGen.generate(config);
        IDataGenerator criterionGen = new TxtDataCriterionGenerator();
        criterionGen.generate(config);
    }

    private void genSeq(DataLoaderConfig config) {
        IDataGenerator dataGen = new SeqDataGenerator();
        dataGen.generate(config);
        IDataGenerator criterionGen = new SeqDataCriterionGenerator();
        criterionGen.generate(config);
    }

}
