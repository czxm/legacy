package com.intel.cedar.service.client.feature.view;

import java.util.List;

public interface TUI {
    public boolean isContainer();

    public void addChild(TUI obj);

    public List<TUI> getChildren();

    public UIType getType();

    public void accept(UIBuilder builder);
}
