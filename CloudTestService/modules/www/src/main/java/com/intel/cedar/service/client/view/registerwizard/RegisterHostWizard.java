package com.intel.cedar.service.client.view.registerwizard;

import com.intel.cedar.service.client.model.NavigationModel;
import com.intel.cedar.service.client.view.wizard.IWizardPage;
import com.intel.cedar.service.client.view.wizard.IWizardPageTransferListener;
import com.intel.cedar.service.client.view.wizard.impl.AbstractWizard;

public class RegisterHostWizard extends AbstractWizard {

    private static String REG_HOST_TITLE = "Register Host";
    private static String EDIT_HOST_TITLE = "Edit Host";

    private IWizardPage typeConfigWizardPage;
    private IWizardPage generalSettingWizardPage;

    public RegisterHostWizard() {
        setSize(600, 400);
        setName(REG_HOST_TITLE);
        setOverviewPanelWidth(.25f);

        addPage(createTypeConfigPage());
        addPage(createGeneralSettingPage());
    }

    public IWizardPage createTypeConfigPage() {
        typeConfigWizardPage = new TypeConfigWizardPage();
        typeConfigWizardPage
                .setPageTransferListener(new IWizardPageTransferListener() {

                    @Override
                    public boolean onLeave(IWizardPage nextPage) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public boolean onEnter(IWizardPage prevPage) {
                        // TODO Auto-generated method stub
                        return false;
                    }
                });

        return typeConfigWizardPage;
    }

    public IWizardPage createGeneralSettingPage() {
        generalSettingWizardPage = new GeneralSettingWizardPage();
        generalSettingWizardPage
                .setPageTransferListener(new IWizardPageTransferListener() {

                    @Override
                    public boolean onLeave(IWizardPage nextPage) {
                        // TODO Auto-generated method stub
                        return false;
                    }

                    @Override
                    public boolean onEnter(IWizardPage prevPage) {
                        // TODO Auto-generated method stub
                        return false;
                    }
                });

        return generalSettingWizardPage;
    }

    // later substitute with host model
    public RegisterHostWizard(NavigationModel model) {

    }

    @Override
    public boolean performCancel() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean performFinish() {
        // TODO Auto-generated method stub
        return false;
    }

}
