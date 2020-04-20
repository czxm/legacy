package com.intel.cedar.service.client.view.wizard;

public interface IWizardPageTransferListener {
    public boolean onLeave(IWizardPage nextPage);

    public boolean onEnter(IWizardPage prevPage);
}
