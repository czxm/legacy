package com.intel.cedar.engine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.intel.cedar.engine.model.event.AbstractModelNotifier;
import com.intel.cedar.engine.model.event.ChangeEvent;
import com.intel.cedar.engine.model.event.IChangeListener;
import com.intel.cedar.engine.model.event.ModelAdapterFactoryRegistry;
import com.intel.cedar.engine.model.event.PropertyChangeEvent;
import com.intel.cedar.engine.model.event.RemovedEvent;

public class DataModel extends AbstractModelNotifier implements IDataModel {
    protected IDataModelDocument document;
    protected IDataModel parent;
    private List<IChangeListener> listeners;

    public DataModel(IDataModelDocument document) {
        this.document = document;
    }

    protected void setDocument(IDataModelDocument document) {
        this.document = document;
    }

    public IDataModelDocument getDocument() {
        return this.document;
    }

    public void setParent(IDataModel parent) {
        this.parent = parent;
    }

    public IDataModel getParent() {
        return this.parent;
    }

    public Iterator<IDataModel> iterate() {
        return Collections.EMPTY_LIST.iterator();
    }

    public void addChangeListener(IChangeListener listener) {
        if (listener == null)
            return;

        getChangeListenersSafe().add(listener);
    }

    public void removeChangeListener(IChangeListener listener) {
        if (listener == null)
            return;

        if (listeners == null)
            return;

        listeners.remove(listener);
    }

    private List<IChangeListener> getChangeListenersSafe() {
        if (listeners == null) {
            listeners = new ArrayList<IChangeListener>();
        }
        return listeners;
    }

    protected Iterator<IChangeListener> iterateChangeListeners() {
        if (listeners == null) {
            return Collections.EMPTY_LIST.iterator();
        }

        List copy = new ArrayList<IChangeListener>(listeners);
        return copy.iterator();
    }

    public void removeAllChangeListener() {
        if (listeners == null)
            return;

        listeners.clear();
    }

    protected void fireChange(ChangeEvent evt) {
        // notify the listeners first
        Iterator<IChangeListener> iter = iterateChangeListeners();
        while (iter.hasNext()) {
            IChangeListener listener = iter.next();
            listener.notifyChange(evt);
        }

        // notify adapters
        notifyChange(evt);
    }

    protected void firePropertyChange(String property, Object oldValue,
            Object newValue) {
        fireChange(new PropertyChangeEvent(this, property, oldValue, newValue));
    }

    protected void fireRemovedEvent(IDataModel parent) {
        fireChange(new RemovedEvent(this, parent));
    }

    protected void initChild(IDataModel child) {
        if (child != null) {
            child.onAdded(this);
            child.postAdded(this);
        }
    }

    /**
     * Called when added to parent
     * 
     * @param parent
     */

    public void onAdded(IDataModel parent) {
        setDocument(parent.getDocument());
        setParent(parent);
    }

    /**
     * Called when removed from parent
     * 
     * @param parent
     */
    public void onRemoved(IDataModel parent) {
        setParent(null);
        fireRemovedEvent(parent);
    }

    /**
     * Called after the model was added to parent and added child event has been
     * fired
     * 
     * @param parent
     */
    public void postAdded(IDataModel parent) {
        // do nothing by default
    }

    /**
     * Called after the model was removed from parent and the child event has
     * been fired
     * 
     * @param parent
     */
    public void postRemoved(IDataModel parent) {
        // do nothing by default
    }

    /**
     * Called after the model loaded
     */
    public void onLoaded() throws DataModelException {
        // call its children for post load
        Iterator<IDataModel> iter = iterate();
        while (iter.hasNext()) {
            IDataModel child = iter.next();
            if (child != null)
                child.onLoaded();
        }
    }

    public ModelAdapterFactoryRegistry getModelAdapterFactoryRegistry() {
        IDataModelDocument document = getDocument();
        if (document != null) {
            ModelAdapterFactoryRegistry reg = document.getFactoryRegistry();
            if (reg != null)
                return reg;
        }
        return null;
    }

}
