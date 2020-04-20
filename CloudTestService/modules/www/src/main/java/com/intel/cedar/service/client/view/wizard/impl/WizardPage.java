package com.intel.cedar.service.client.view.wizard.impl;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.intel.cedar.service.client.view.wizard.IWizard;
import com.intel.cedar.service.client.view.wizard.IWizardPage;
import com.intel.cedar.service.client.view.wizard.IWizardPageTransferListener;

public abstract class WizardPage extends LayoutContainer implements IWizardPage {
    IWizard wizard;
    String desc;
    String error;
    String name;

    IWizardPageTransferListener listener = new EmptyWizardPageTransferListener(
            this);

    static public class EmptyWizardPageTransferListener implements
            IWizardPageTransferListener {
        IWizardPage owner;

        public EmptyWizardPageTransferListener(IWizardPage owner) {
            this.owner = owner;
        }

        @Override
        public boolean onEnter(IWizardPage prevPage) {
            owner.getWizard().showPage(owner);
            return true;
        }

        @Override
        public boolean onLeave(IWizardPage nextPage) {
            return true;
        }

    }

    @Override
    public IWizardPage getNextPage() {
        if (wizard != null) {
            return wizard.getNextPage(this);
        }

        return null;
    }

    @Override
    public IWizardPage getPreviousPage() {
        if (wizard != null) {
            return wizard.getPreviousPage(this);
        }

        return null;
    }

    @Override
    public IWizard getWizard() {
        return wizard;
    }

    @Override
    public void setDescription(String desc) {
        this.desc = desc;
    }

    @Override
    public void setErrorMessage(String error) {
        this.error = error;
        wizard.updateUI();
    }

    @Override
    public void clearErrorMessage() {
        error = null;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setWizard(IWizard wizard) {
        this.wizard = wizard;
    }

    @Override
    public String getDescription() {
        return desc;
    }

    @Override
    public String getErrorMessage() {
        return error;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean validatePage() {
        return isPageComplete();
    }

    @Override
    public IWizardPageTransferListener getPageTransferListener() {
        return listener;
    }

    @Override
    public void setPageTransferListener(IWizardPageTransferListener listener) {
        if (listener != null) {
            this.listener = listener;
        }
    }

    @Override
    public LayoutContainer getContainer() {
        return this;
    }
}
