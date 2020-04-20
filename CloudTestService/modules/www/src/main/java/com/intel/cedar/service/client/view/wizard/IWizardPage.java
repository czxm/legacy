package com.intel.cedar.service.client.view.wizard;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

public interface IWizardPage {
    public IWizardPage getNextPage();

    public IWizardPage getPreviousPage();

    public IWizard getWizard();

    public boolean isPageComplete();

    public void setWizard(IWizard wizard);

    public String getDescription();

    public void setDescription(String desc);

    public String getErrorMessage();

    public void setErrorMessage(String error);

    public void clearErrorMessage();

    public LayoutContainer getContainer();

    public void setName(String name);

    public String getName();

    public void reset();

    public boolean validatePage();

    public IWizardPageTransferListener getPageTransferListener();

    public void setPageTransferListener(IWizardPageTransferListener listener);
}
