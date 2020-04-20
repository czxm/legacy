package com.intel.cedar.service.client.view;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Image;
import com.intel.cedar.service.client.resources.Resources;

public class ErrorDialog extends StatusDialog {
    private Boolean isExpanded = false;

    private String errInfo = "Exception occurred while invoking remote functions";

    private String detailMsg = "Exception Trace";

    private final static String BGCSTYLE = "background-color:transparent";

    public ErrorDialog() {
        super(DialogType.ERROR);
    }

    public ErrorDialog(String errorInfo, String errorDetail) {
        super(DialogType.ERROR);
        errInfo = errorInfo;
        detailMsg = errorDetail;
    }

    public void onRender(Element element, int index) {
        super.onRender(element, index);
        setLayout(new FitLayout());

        final ContentPanel cp = new ContentPanel();
        cp.setHeaderVisible(false);
        cp.setBodyBorder(false);
        cp.setLayout(new BorderLayout());

        ContentPanel west = new ContentPanel();
        BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 60);
        west.setHeaderVisible(false);
        west.setBodyBorder(false);
        west.setBodyStyle("background-color:transparent");
        Image img = new Image(Resources.ICONS.error48x48());
        west.add(img);
        cp.add(west, westData);

        final ContentPanel center = new ContentPanel();
        BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
        center.setHeaderVisible(false);
        center.setBodyStyle(BGCSTYLE);
        center.setBodyBorder(false);
        center.setBorders(false);
        center.setLayout(new BorderLayout());
        ContentPanel centerNorth = new ContentPanel();
        BorderLayoutData centerNorthData = new BorderLayoutData(
                LayoutRegion.NORTH, 100);
        centerNorthData.setMargins(new Margins(0, 0, 10, 0));
        centerNorth.setHeaderVisible(false);
        centerNorth.setBodyStyle(BGCSTYLE);
        centerNorth.setBodyBorder(false);
        centerNorth.setBorders(false);
        centerNorth.addText(errInfo);
        center.add(centerNorth, centerNorthData);
        final ContentPanel centerSouth = new ContentPanel();
        BorderLayoutData centerSouthData = new BorderLayoutData(
                LayoutRegion.CENTER);
        centerSouth.setHeaderVisible(false);
        centerSouth.setBodyStyle(BGCSTYLE);
        centerSouth.setVisible(false);
        centerSouth.setBodyBorder(false);
        centerSouth.setBorders(false);
        center.add(centerSouth, centerSouthData);
        cp.add(center, centerData);

        cp.getButtonBar().setSpacing(15);
        cp.setButtonAlign(HorizontalAlignment.RIGHT);
        final Button detail = new Button();
        detail.setText("Detail >>");
        detail.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (isExpanded == false) {
                    ErrorDialog.this.setSize(450, 400);
                    centerSouth.addText(detailMsg);
                    centerSouth.setVisible(true);
                    detail.setText("<< Detail");
                    ErrorDialog.this.layout();

                    isExpanded = true;
                } else {
                    ErrorDialog.this.setSize(450, 180);
                    detail.setText("Detail >>");
                    centerSouth.removeAll();
                    centerSouth.setVisible(false);
                    ErrorDialog.this.layout();

                    isExpanded = false;
                }
            }

        });

        cp.addButton(detail);
        cp.addButton(new Button("Close", new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                ErrorDialog.this.hide();
            }

        }));

        add(cp);
    }
}
