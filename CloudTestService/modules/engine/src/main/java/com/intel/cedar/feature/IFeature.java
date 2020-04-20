package com.intel.cedar.feature;

import java.io.InputStream;

public interface IFeature {
    public void onInit(Environment env) throws Exception;

    public void onFinalize(Environment env) throws Exception;

    public String getReportBody(Environment env) throws Exception;

    public String getReportTitle(Environment env) throws Exception;
    
    public String getReportFootnote(Environment env) throws Exception;
    
    public InputStream getReportCSS(Environment env) throws Exception;
    
    public INotifyConfig getNotifyConfig(Environment env) throws Exception;
}
