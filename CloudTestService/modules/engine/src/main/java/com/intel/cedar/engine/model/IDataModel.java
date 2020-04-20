package com.intel.cedar.engine.model;

import java.util.Iterator;

import com.intel.cedar.engine.model.event.IChangeNotifier;

public interface IDataModel extends IChangeNotifier {

    /**
     * Get the document of the data model
     * 
     * @return IDataModelDocument
     */
    public IDataModelDocument getDocument();

    /*
     * Get parent of the model, null if no parent
     */
    public IDataModel getParent();

    /*
     * Iterate child data model
     */
    public Iterator<IDataModel> iterate();

    /**
     * Called after the model was added to parent
     * 
     * @param parent
     */
    public void onAdded(IDataModel parent);

    /**
     * Called after the model was removed from parent
     * 
     * @param parent
     */
    public void onRemoved(IDataModel parent);

    /**
     * Called after the model was added to parent and added child event has been
     * fired
     * 
     * @param parent
     */
    public void postAdded(IDataModel parent);

    /**
     * Called after the model was removed from parent and the child event has
     * been fired
     * 
     * @param parent
     */
    public void postRemoved(IDataModel parent);

    /**
     * Called after the model loaded
     */
    public void onLoaded() throws DataModelException;
}
