package com.intel.cedar.service.client.view;

import java.util.HashMap;

import com.intel.cedar.service.client.feature.view.FeatureView;

public class ViewCache {
    public enum ViewType {
        TYPE_CLOUD_VIEW, TYPE_CLOUD_REGISTRATION, TYPE_CLOUD_EDITOR, TYPE_IMAGE_VIEW, TYPE_INSTANCE_VIEW, TYPE_KEY_VIEW, TYPE_MACHINETYPE_VIEW, TYPE_FEATURE_VIEW, TYPE_CPPCONFORMANCE_VIEW, TYPE_FEATUREMGR_VIEW, TYPE_FEATUREMGR_UPLOAD, TYPE_FEATUREMGR_EDITOR, TYPE_USERMGR_VIEW, TYPE_VOLUME_VIEW, TYPE_PHYSICALHOST_VIEW, TYPE_HISTORY_VIEW, TYPE_FEATUREJOB_VIEW;
    }

    private static HashMap<ViewType, ComponentViewer> viewMap = new HashMap<ViewType, ComponentViewer>();

    public static ComponentViewer createViewer(ViewType type) {
        if (type == ViewType.TYPE_FEATURE_VIEW) {
            return FeatureView.getInstance();
        }
        if (type == ViewType.TYPE_CLOUD_REGISTRATION) {
            return CloudRegistration.getInstance();
        }

        ComponentViewer result = viewMap.get(type);
        if (result == null) {
            switch (type) {
            case TYPE_CLOUD_VIEW:
                result = CloudViewer.getInstance();
                break;
            case TYPE_CLOUD_EDITOR:
                result = CloudEditor.getInstance();
                break;
            case TYPE_IMAGE_VIEW:
                result = ImageViewer.getInstance();
                break;
            case TYPE_INSTANCE_VIEW:
                result = InstanceViewer.getInstance();
                break;
            case TYPE_MACHINETYPE_VIEW:
                result = TypeViewer.getInstance();
                break;
            case TYPE_KEY_VIEW:
                result = KeyPairViewer.getInstance();
                break;
            case TYPE_FEATUREMGR_VIEW:
                result = FeatureManagementView.getInstance();
                break;
            case TYPE_FEATUREMGR_UPLOAD:
                result = FeatureUploadView.getInstance();
                break;
            case TYPE_USERMGR_VIEW:
                result = UserManagementView.getInstance();
                break;
            case TYPE_VOLUME_VIEW:
                result = VolumeView.getInstance();
                break;
            case TYPE_PHYSICALHOST_VIEW:
                result = PhysicalHostView.getInstance();
                break;
            case TYPE_HISTORY_VIEW:
                result = HistoryView.getInstance();
                break;
            case TYPE_FEATUREJOB_VIEW:
                result = FeatureJobView.getInstance();
                break;
            default:
                break;
            }

            viewMap.put(type, result);
        }

        return result;
    }
}
