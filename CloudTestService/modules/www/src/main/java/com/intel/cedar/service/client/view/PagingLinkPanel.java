package com.intel.cedar.service.client.view;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.util.Util;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.TextBox;
import com.intel.cedar.service.client.widget.CedarLabel;

public class PagingLinkPanel extends HorizontalPanel {

    private PagingLoader<?> loader;
    private PagingLoadConfig config;
    private int start, pageSize, totalLength;
    private Label first, prev, next, last;
    private TextBox pageText;
    private LabelToolItem afterText;
    private LabelToolItem displayText;
    private LoadListener loadListener;
    private boolean reuseConfig;
    private LoadEvent renderEvent;
    private Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {

        @Override
        public void handleEvent(ComponentEvent be) {
            Component c = be.getComponent();
            if (be.getType() == Events.Disable) {
                if (c == first) {
                    first.removeStyleName("cedar_label_link");
                    first.setStyleName("cedar_label_link_disable");
                }
                if (c == prev) {
                    prev.removeStyleName("cedar_label_link");
                    prev.setStyleName("cedar_label_link_disable");
                }
                if (c == next) {
                    next.removeStyleName("cedar_label_link");
                    next.setStyleName("cedar_label_link_disable");
                }
                if (c == last) {
                    last.removeStyleName("cedar_label_link");
                    last.setStyleName("cedar_label_link_disable");
                }
            } else if (be.getType() == Events.Enable) {
                if (c == first) {
                    first.removeStyleName("cedar_label_link_disable");
                    first.setStyleName("cedar_label_link");
                }
                if (c == prev) {
                    prev.removeStyleName("cedar_label_link_disalbe");
                    prev.setStyleName("cedar_label_link");
                }
                if (c == next) {
                    next.removeStyleName("cedar_label_link_disable");
                    next.setStyleName("cedar_label_link");
                }
                if (c == last) {
                    last.removeStyleName("cedar_label_link_disalbe");
                    last.setStyleName("cedar_label_link");
                }
            } else if (be.getType() == Events.OnMouseOver) {
                if (c == first) {
                    first.removeStyleName("cedar_label_link");
                    first.setStyleName("cedar_label_link_over");
                }
                if (c == prev) {
                    prev.removeStyleName("cedar_label_link");
                    prev.setStyleName("cedar_label_link_over");
                }
                if (c == next) {
                    next.removeStyleName("cedar_label_link");
                    next.setStyleName("cedar_label_link_over");
                }
                if (c == last) {
                    last.removeStyleName("cedar_label_link");
                    last.setStyleName("cedar_label_link_over");
                }
            } else if (be.getType() == Events.OnMouseOut) {
                if (c == first) {
                    first.removeStyleName("cedar_label_link_over");
                    first.setStyleName("cedar_label_link");
                }
                if (c == prev) {
                    prev.removeStyleName("cedar_label_link_over");
                    prev.setStyleName("cedar_label_link");
                }
                if (c == next) {
                    next.removeStyleName("cedar_label_link_over");
                    next.setStyleName("cedar_label_link");
                }
                if (c == last) {
                    last.removeStyleName("cedar_label_link_over");
                    last.setStyleName("cedar_label_link");
                }
            } else if (be.getType() == Events.OnClick) {
                if (c == first) {
                    first();
                }
                if (c == prev) {
                    previous();
                }
                if (c == next) {
                    next();
                }
                if (c == last) {
                    last();
                }
            }

        }
    };
    private int activePage;
    private int pages;

    public PagingLinkPanel(int pageSize) {
        setTableWidth("100%");
        this.pageSize = pageSize;

        TableData displayTableData = new TableData();
        displayTableData.setWidth("73%");
        displayTableData.setVerticalAlign(VerticalAlignment.MIDDLE);
        displayText = new LabelToolItem();
        displayText.setStyleName("cedar_label");

        TableData leftTableData = new TableData();
        leftTableData.setWidth("3%");
        leftTableData.setHorizontalAlign(HorizontalAlignment.LEFT);
        leftTableData.setVerticalAlign(VerticalAlignment.MIDDLE);

        if (GXT.isIE)
            first = new CedarLabel("|<<");
        else
            first = new CedarLabel("|<");
        first.setToolTip("first page");
        first.setStyleName("cedar_label_link");
        first.addListener(Events.Enable, listener);
        first.addListener(Events.Disable, listener);
        first.addListener(Events.OnMouseOver, listener);
        first.addListener(Events.OnMouseOut, listener);
        first.addListener(Events.OnClick, listener);

        TableData middleTableData = new TableData();
        middleTableData.setWidth("3%");
        middleTableData.setHorizontalAlign(HorizontalAlignment.CENTER);
        middleTableData.setVerticalAlign(VerticalAlignment.MIDDLE);
        if (GXT.isIE)
            prev = new CedarLabel("<<");
        else
            prev = new CedarLabel("<");
        prev.setToolTip("previous page");
        prev.setStyleName("cedar_label_link");
        prev.addListener(Events.Enable, listener);
        prev.addListener(Events.Disable, listener);
        prev.addListener(Events.OnMouseOver, listener);
        prev.addListener(Events.OnMouseOut, listener);
        prev.addListener(Events.OnClick, listener);

        TableData rightTableData = new TableData();
        rightTableData.setWidth("3%");
        rightTableData.setHorizontalAlign(HorizontalAlignment.RIGHT);
        rightTableData.setVerticalAlign(VerticalAlignment.MIDDLE);
        next = new CedarLabel(">");
        next.setToolTip("next page");
        next.setStyleName("cedar_label_link");
        next.addListener(Events.Enable, listener);
        next.addListener(Events.Disable, listener);
        next.addListener(Events.OnMouseOver, listener);
        next.addListener(Events.OnMouseOut, listener);
        next.addListener(Events.OnClick, listener);

        // TableData lastTableData = new TableData();
        // lastTableData.setWidth("4%");
        // lastTableData.setHorizontalAlign(HorizontalAlignment.RIGHT);
        // lastTableData.setVerticalAlign(VerticalAlignment.MIDDLE);
        last = new CedarLabel(">|");
        last.setToolTip("last page");
        last.setStyleName("cedar_label_link");
        last.addListener(Events.Enable, listener);
        last.addListener(Events.Disable, listener);
        last.addListener(Events.OnMouseOver, listener);
        last.addListener(Events.OnMouseOut, listener);
        last.addListener(Events.OnClick, listener);

        TableData beforeTabelData = new TableData();
        beforeTabelData.setWidth("5%");
        beforeTabelData.setHorizontalAlign(HorizontalAlignment.RIGHT);
        beforeTabelData.setVerticalAlign(VerticalAlignment.MIDDLE);
        LabelToolItem beforePage = new LabelToolItem("Page");
        beforePage.setStyleName("cedar_label");

        TableData pageTabelData = new TableData();
        pageTabelData.setWidth("4%");
        pageTabelData.setHorizontalAlign(HorizontalAlignment.RIGHT);
        pageTabelData.setVerticalAlign(VerticalAlignment.MIDDLE);
        pageText = new TextBox();
        pageText.setWidth("32px");
        pageText.addKeyDownHandler(new KeyDownHandler() {
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    onPageChange();
                }
            }
        });

        TableData afterTabelData = new TableData();
        afterTabelData.setWidth("6%");
        afterTabelData.setHorizontalAlign(HorizontalAlignment.LEFT);
        afterTabelData.setVerticalAlign(VerticalAlignment.MIDDLE);
        afterText = new LabelToolItem();
        afterText.setStyleName("cedar_label");

        add(displayText, displayTableData);
        add(first, leftTableData);
        add(prev, middleTableData);
        add(next, middleTableData);
        add(last, rightTableData);
        add(beforePage, beforeTabelData);
        add(pageText, pageTabelData);
        add(afterText, afterTabelData);
    }

    public void bind(PagingLoader<?> loader) {
        if (this.loader != null) {
            this.loader.removeLoadListener(loadListener);
        }
        this.loader = loader;
        if (loader != null) {
            loader.setLimit(pageSize);
            if (loadListener == null) {
                loadListener = new LoadListener() {
                    public void loaderBeforeLoad(LoadEvent le) {

                    }

                    public void loaderLoad(LoadEvent le) {
                        onLoad(le);
                    }

                    public void loaderLoadException(LoadEvent le) {
                    }
                };
            }
            loader.addLoadListener(loadListener);
        }
    }

    public void clear() {
        if (rendered) {
            pageText.setText("");
            afterText.setLabel("");
            displayText.setLabel("");
        }
    }

    protected void onLoad(LoadEvent le) {
        if (!rendered) {
            renderEvent = le;
            return;
        }
        config = le.getConfig();
        PagingLoadResult<?> result = le.getData();
        start = result.getOffset();
        totalLength = result.getTotalLength();
        activePage = (int) Math.ceil((start + pageSize) / pageSize);
        pageText.setText(String.valueOf(activePage));
        pages = totalLength < pageSize ? 1 : (int) Math
                .ceil((double) totalLength / pageSize);
        afterText.setLabel(" of " + pages);

        first.setEnabled(activePage != 1);
        prev.setEnabled(activePage != 1);
        next.setEnabled(activePage != pages);
        last.setEnabled(activePage != pages);

        int tmp = (activePage == pages ? totalLength : (start + pageSize));
        String display = "Displaying " + (start + 1) + "-" + tmp + " of "
                + totalLength;
        if (totalLength == 0) {
            display = "";
        }
        displayText.setLabel(display);
    }

    protected void doLoadRequest(int offset, int limit) {
        if (reuseConfig && config != null) {
            config.setOffset(offset);
            config.setLimit(limit);
            loader.load(config);
        } else {
            loader.setLimit(pageSize);
            loader.load(offset, limit);
        }
    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);

        if (renderEvent != null) {
            onLoad(renderEvent);
            renderEvent = null;
        }
    }

    public void first() {
        doLoadRequest(0, pageSize);
    }

    public void previous() {
        doLoadRequest(Math.max(0, start - pageSize), pageSize);
    }

    public void next() {
        doLoadRequest(start + pageSize, pageSize);
    }

    public void last() {
        int extra = totalLength % pageSize;
        int lastStart = extra > 0 ? (totalLength - extra) : totalLength
                - pageSize;
        doLoadRequest(lastStart, pageSize);
    }

    public void setActivePage(int page) {
        if (page > pages) {
            last();
            return;
        }
        if (page != activePage && page > 0 && page <= pages) {
            doLoadRequest(--page * pageSize, pageSize);
        } else {
            pageText.setText(String.valueOf((int) activePage));
        }
    }

    protected void onPageChange() {
        String value = pageText.getText();
        if (value.equals("") || !Util.isInteger(value)) {
            pageText.setText(String.valueOf((int) activePage));
            return;
        }
        int p = Integer.parseInt(value);
        setActivePage(p);
    }
}
