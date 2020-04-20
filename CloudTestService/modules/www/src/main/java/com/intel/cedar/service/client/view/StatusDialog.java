package com.intel.cedar.service.client.view;

import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.intel.cedar.service.client.resources.Resources;

public class StatusDialog extends Window {

    public enum DialogType {
        INFO("info"), ALERT("alert"), ERROR("error");

        String title = "info";

        DialogType(String type) {
            title = type;
        }

        public String getTitle() {
            return title;
        }
    }

    private final int defaultWidth = 450;
    private final int defaultHeight = 180;
    private DialogType type;

    public StatusDialog() {

    }

    public StatusDialog(DialogType type) {
        this.type = type;
    }

    public void onRender(Element element, int index) {
        super.onRender(element, index);
        this.setHeading(type.getTitle());
        // this.setIcon(getImage(type));
        this.setSize(defaultWidth, defaultHeight);
    }

    public void setType(DialogType type) {
        this.type = type;
    }

    public DialogType getType() {
        return type;
    }

    public static AbstractImagePrototype getImage(DialogType type) {
        AbstractImagePrototype abstractImage = null;
        ;
        switch (type) {
        case INFO:
            abstractImage = AbstractImagePrototype.create(Resources.ICONS
                    .info16x16());
            break;
        case ALERT:
            abstractImage = AbstractImagePrototype.create(Resources.ICONS
                    .alert16x16());
            break;
        case ERROR:
            abstractImage = AbstractImagePrototype.create(Resources.ICONS
                    .error16x16());
            break;
        default:
            break;
        }

        return abstractImage;
    }
}
