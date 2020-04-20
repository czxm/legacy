package com.intel.cedar.pool;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class Resource {
    private ResourceRequest request;
    private List<ResourceItem> items = new ArrayList<ResourceItem>();

    public Resource(ResourceRequest req) {
        this.request = req;
    }

    public ResourceRequest getRequest() {
        return this.request;
    }

    public List<ResourceItem> getResources() {
        return this.items;
    }

    public List<ComputeNode> getComputeNodes() {
        List result = Lists.newArrayList();
        for (ResourceItem i : items) {
            result.add(i.getNode());
        }
        return result;
    }

    public void addResourceItem(ResourceItem item) {
        items.add(item);
    }

    public int getResourceCount() {
        return items.size();
    }
}
