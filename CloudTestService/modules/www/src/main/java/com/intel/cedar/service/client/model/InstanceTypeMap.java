package com.intel.cedar.service.client.model;

import java.io.Serializable;
import java.util.HashMap;

public class InstanceTypeMap implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    public static HashMap<InstanceBean.InstanceTypeBean, HWMetricBean> _map = new HashMap<InstanceBean.InstanceTypeBean, HWMetricBean>();

    static {
        _map.put(InstanceBean.InstanceTypeBean.DEFAULT, new HWMetricBean("1",
                "512", "5"));
        _map.put(InstanceBean.InstanceTypeBean.MEDIUM_HCPU, new HWMetricBean(
                "1", "1024", "10"));
        _map.put(InstanceBean.InstanceTypeBean.LARGE, new HWMetricBean("2",
                "2048", "10"));
        _map.put(InstanceBean.InstanceTypeBean.XLARGE, new HWMetricBean("2",
                "4096", "20"));
        _map.put(InstanceBean.InstanceTypeBean.XLARGE_HCPU, new HWMetricBean(
                "4", "4096", "40"));
    }

    public InstanceTypeMap() {

    }

    public HWMetricBean get(InstanceBean.InstanceTypeBean type) {
        return _map.get(type);
    }

    public void set(InstanceBean.InstanceTypeBean type, HWMetricBean value) {
        _map.put(type, value);
    }

}
