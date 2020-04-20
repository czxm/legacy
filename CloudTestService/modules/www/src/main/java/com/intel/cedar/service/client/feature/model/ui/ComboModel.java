package com.intel.cedar.service.client.feature.model.ui;

import java.util.ArrayList;
import java.util.List;

import com.intel.cedar.service.client.feature.model.uitl.UIUtils;
import com.intel.cedar.service.client.feature.view.UIBuilder;

public class ComboModel extends UIBaseNodes implements IBindModelProvider {
    private BindModel bind;
    private int type;

    private List<ComboItemModel> items = new ArrayList<ComboItemModel>();

    public static final int DEPENDS = 0;
    public static final int BIND = 1;
    public static final int ITEM = 2;
    private static final long serialVersionUID = UIUtils
            .getSerialVUID(UIUtils.UI_COMBO);

    public ComboModel() {
        super();
        type = 0;
    }

    @Override
    public void accept(UIBuilder builder) {
        if (builder == null) {
            return;
        }
        builder.visitComboBox(this);
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<String> getValues() {
        List<String> values = new ArrayList<String>();

        switch (type) {
        case BIND:
            values.addAll(bind.getValues());
            break;
        case DEPENDS:
            values.addAll(getDependsValues());
            break;
        case ITEM:
            values.addAll(computeValuesFromItem());
            break;
        default:
        }
        return values;
    }

    @Override
    public void setBind(BindModel bind) {
        this.bind = bind;
        type = BIND;
    }

    @Override
    public BindModel getBind() {
        return bind;
    }

    @Override
    public void setBind(String name) {
        bind = new BindModel(name);
        type = BIND;
    }

    public void addItem(ComboItemModel item) {
        this.items.add(item);
        type = ITEM;
    }

    public List<ComboItemModel> getItems() {
        return this.items;
    }

    @Override
    public void init() {
        super.init();

        if (bind == null) {
            return;
        }

        bind.setFeature(getFeature());
    }

    private List<String> computeValuesFromItem() {
        List<String> values = new ArrayList<String>();
        for (int i = 0; i < items.size(); i++) {
            ComboItemModel item = items.get(i);
            values.add((String) item.getValue());
        }
        return values;
    }

}
