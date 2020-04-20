package com.intel.cedar.service.client.feature.model.ui;

import java.io.Serializable;
import java.util.List;

import com.intel.cedar.service.client.feature.view.UIBuilder;

public interface IUINode extends Serializable {

    /**
     * set the object id
     */
    public void setID(String id);

    /**
     * get the UI object ID
     */
    public String getID();

    /**
     * set the label
     */

    public void setLabel(String label);

    /**
     * get the UI label
     */
    public String getLabel();

    /**
     * show or hide this UI item true for show, false for hide
     */

    public boolean isShow();

    /**
     * set the show on select value
     */
    public void setShowOnSelect(String dependedValue);

    /**
     * get the show on select value
     */
    public String getShowOnSelect();

    /**
     * return whether this UI item is static true for static, false for dynamic
     */
    public boolean isStaticWidget();

    /**
     * set the parent
     */
    public void setParent(IUINode parent);

    /**
     * get the parent
     */
    public IUINode getParent();

    /**
     * iterate the next sibling UI object, return null if there is no sibling or
     * the last sibling has been iterated
     */
    public IUINode next();

    /**
     * iterate the children, return null if there is no children or the last
     * children have been iterated
     */
    public IUINode nextChild();

    /**
     * is this a container
     */
    public boolean isContainer();

    /**
     * get the siblings
     * 
     */
    public List<IUINode> getSiblings();

    /**
	 * 
	 */
    public void accept(UIBuilder builder);

    /**
     * get the values of the item
     */
    public List<String> getValues();

    /**
     * initialize the node
     */
    public void init();
}
