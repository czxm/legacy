package com.intel.cedar.service.client.feature.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImageMap {
    private static HashMap<String, String> inner = new HashMap<String, String>();

    static {
        inner.put("CPP.Conf",
                "resources/images/cloudtestservice/cpp.conformance.shadow.png");
        inner.put("JAVA.Nightly",
                "resources/images/cloudtestservice/java.nightly.shadow.png");
        inner.put("JAVA.PV",
                "resources/images/cloudtestservice/java.pv.shadow.png");
        inner.put("SplitPoint",
                "resources/images/cloudtestservice/splitpoint.shadow.png");

        inner.put("Cloud", "resources/images/cloudtestservice/cloud_48.png");
        inner.put("Image", "resources/images/cloudtestservice/image_48.png");
        inner.put("Instance",
                "resources/images/cloudtestservice/instance_48.png");
        inner.put("Volume", "resources/images/cloudtestservice/volume_48.png");
        inner.put("Key", "resources/images/cloudtestservice/key_48.png");
        inner.put("Type", "resources/images/cloudtestservice/type_48.png");
        inner.put("Launch", "resources/images/cloudtestservice/launch_48.png");
        inner.put("Host", "resources/images/cloudtestservice/host_48.png");

        inner
                .put("Users",
                        "resources/images/cloudtestservice/user/user_information_48.png");
        inner.put("Features",
                "resources/images/cloudtestservice/feature/features_48.png");
        inner.put("Jobs", "resources/images/cloudtestservice/job_48.png");
        inner
                .put("History",
                        "resources/images/cloudtestservice/history_48.png");
    }

    public static String getImagePath(String id) {
        return inner.get(id);
    }

    public static List<ImageModel> getImageModels(List<String> ids) {
        List<ImageModel> res = new ArrayList<ImageModel>();
        for (String str : ids) {
            ImageModel model = new ImageModel(str, inner.get(str));
            res.add(model);
        }

        return res;
    }

    public static List<ImageModel> getImageModels(String[] ids) {
        List<ImageModel> res = new ArrayList<ImageModel>();
        for (String str : ids) {
            ImageModel model = new ImageModel(str, inner.get(str));
            res.add(model);
        }

        return res;
    }
}
