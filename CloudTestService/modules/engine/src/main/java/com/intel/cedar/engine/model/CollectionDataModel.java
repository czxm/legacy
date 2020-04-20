package com.intel.cedar.engine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.intel.cedar.engine.model.event.ChildAddEvent;
import com.intel.cedar.engine.model.event.ChildRemoveEvent;
import com.intel.cedar.engine.model.event.ChildReplaceEvent;

public class CollectionDataModel<T> extends DataModel {
    protected List<T> children = null;

    public CollectionDataModel(IDataModelDocument document) {
        super(document);
    }

    public Iterator<IDataModel> iterate() {
        return (Iterator<IDataModel>) iterateChild();
    }

    protected List<T> getChildrenSafe() {
        if (children == null)
            children = new ArrayList<T>(5);
        return children;
    }

    public int getSize() {
        if (children == null)
            return 0;

        return children.size();
    }

    public List<T> getModelChildren() {
        return getChildrenSafe();
    }

    public T getChild(int index) {
        if (children == null)
            throw new IndexOutOfBoundsException("The collection is empty.");

        return children.get(index);
    }

    public Iterator<T> iterateChild() {
        if (children == null)
            return Collections.EMPTY_LIST.iterator();

        return children.iterator();
    }

    public int findChild(T child) {
        int size = getSize();
        for (int i = 0; i < size; i++) {
            if (children.get(i) == child)
                return i;
        }

        return -1;
    }

    public void buildChild(T child) {
        getChildrenSafe();
        children.add(child);
        initChild((IDataModel) child);
    }

    public void appendChild(T child) {
        insertChild(getSize(), child);
    }

    public void insertChild(int index, T child) {
        getChildrenSafe();
        children.add(index, child);

        if (child != null)
            ((IDataModel) child).onAdded(this);

        // fire event
        fireChildAddEvent(index, child);

        if (child != null)
            ((IDataModel) child).postAdded(this);
    }

    public void removeChild(int index) {
        if (children == null)
            throw new IndexOutOfBoundsException("The collection is empty.");

        T oldChild = children.get(index);
        children.remove(index);

        if (oldChild != null)
            ((IDataModel) oldChild).onRemoved(this);

        // fire event
        fireChildRemoveEvent(index, oldChild);

        if (oldChild != null)
            ((IDataModel) oldChild).postRemoved(this);
    }

    public void removeChild(T child) {
        int index = findChild(child);
        if (index < 0)
            throw new IllegalArgumentException("The child is not found.");

        removeChild(index);
    }

    public void removeAll() {
        while (this.getChildrenSafe().size() > 0)
            this.removeChild(0);
    }

    public void replaceChild(int index, T newChild) {
        if (children == null)
            throw new IndexOutOfBoundsException("The collection is empty.");

        T oldChild = children.get(index);
        children.set(index, newChild);

        if (oldChild != null)
            ((IDataModel) oldChild).onRemoved(this);

        if (newChild != null)
            ((IDataModel) newChild).onAdded(this);

        // fire event
        fireChildReplaceEvent(index, oldChild, newChild);

        if (newChild != null)
            ((IDataModel) newChild).postAdded(this);

        if (oldChild != null)
            ((IDataModel) oldChild).postRemoved(this);
    }

    protected void fireChildAddEvent(int index, T newChild) {
        fireChange(new ChildAddEvent(this, index, (IDataModel) newChild));
    }

    protected void fireChildRemoveEvent(int index, T oldChild) {
        fireChange(new ChildRemoveEvent(this, index, (IDataModel) oldChild));
    }

    protected void fireChildReplaceEvent(int index, T oldChild, T newChild) {
        fireChange(new ChildReplaceEvent(this, index, (IDataModel) oldChild,
                (IDataModel) newChild));
    }
}
