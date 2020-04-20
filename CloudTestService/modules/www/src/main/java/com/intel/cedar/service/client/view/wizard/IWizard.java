package com.intel.cedar.service.client.view.wizard;

public interface IWizard {
    public boolean canFinish();

    public boolean performFinish();

    public boolean performCancel();

    public IWizardPage getNextPage(IWizardPage currentPage);

    public IWizardPage getPreviousPage(IWizardPage currentPage);

    public IWizardPage getCurrentPage();

    public IWizardPage getStartingPage();

    public IWizardPage getPage(String pageName);

    public void showPage(IWizardPage page);

    public void addPage(IWizardPage page);

    public void addPage(int pos, IWizardPage page);

    public void removePage(IWizardPage page);

    public void removePage(int pos);

    public IWizardPage getPage(int pos);

    public IWizardPage[] getPages();

    public void setError(String error);

    public String getError();

    public void clearError();

    public String getName();

    public void setName(String name);

    public void show();

    public void close();

    public void updateUI();
}
