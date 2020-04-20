package com.intel.cedar.service.client.view.wizard.impl;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.ListViewSelectionModel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.intel.cedar.service.client.model.NavigationModel;
import com.intel.cedar.service.client.view.wizard.IWizard;
import com.intel.cedar.service.client.view.wizard.IWizardPage;

public abstract class AbstractWizard extends Window implements IWizard {
    private static final String PREVIOUS = "Previous";
    private static final String NEXT = "Next";
    private static final String FINISH = "Finish";
    private static final String CANCEL = "Cancel";

    private List<IWizardPage> pages;
    private int currentPageIndex;
    private LayoutContainer contentContainer;
    private LayoutContainer stepsOverviewContainer;
    private Button btnPre;
    private Button btnNext;
    private Button btnFini;
    private Button btnCancel;
    private boolean isInit;
    private String name;
    private String error;

    private ListView<NavigationModel> stepsListView;
    private ListStore<NavigationModel> stepsListStore;
    private float stepsOverviewPanelWidth = 0;
    Label pageName;
    Label pageDesc;

    public AbstractWizard() {
        super();
        contentContainer = new LayoutContainer();
        stepsOverviewContainer = new LayoutContainer();
        pages = new ArrayList<IWizardPage>();
        setModal(true);
        setWidth(600);
        setHeight(500);
        isInit = false;
    }

    public void setOverviewPanelWidth(float width) {
        if (width >= 0 && width < 1) {
            stepsOverviewPanelWidth = width;
        }
    }

    public LayoutContainer stepsOverviewContainer() {
        return stepsOverviewContainer;
    }

    public LayoutContainer contentContainer() {
        return contentContainer;
    }

    private class WizardButtonEventListener extends
            SelectionListener<ButtonEvent> {

        @Override
        public void componentSelected(ButtonEvent ce) {
            Button button = ce.getButton();
            if (button.getText().equals(PREVIOUS)) {
                if (getCurrentPage() != null) {
                    IWizardPage page = getPreviousPage(getCurrentPage());
                    IWizardPage oldpage = getCurrentPage();
                    if (!oldpage.getPageTransferListener().onLeave(page)) {
                        return;
                    }
                    if (page != null) {
                        page.getPageTransferListener().onEnter(oldpage);
                    }
                }
            } else if (button.getText().equals(NEXT)) {
                if (getCurrentPage() != null && getCurrentPage().validatePage()) {
                    IWizardPage page = getNextPage(getCurrentPage());
                    IWizardPage oldpage = getCurrentPage();
                    if (!oldpage.getPageTransferListener().onLeave(page)) {
                        return;
                    }
                    if (page != null) {
                        page.getPageTransferListener().onEnter(oldpage);
                    }
                }
            } else if (button.getText().equals(FINISH)) {
                if (getCurrentPage() != null && getCurrentPage().validatePage()) {
                    if (performFinish()) {
                        hide(button);
                    }
                }
            } else if (button.getText().equals(CANCEL)) {
                performCancel();
                hide(button);
            }
        }

    }

    private void chooseWizardStepDesc(IWizardPage page) {
        if (stepsOverviewPanelWidth != 0) {
            for (int i = 0; i < stepsListStore.getCount(); i++) {
                if (stepsListStore.getAt(i).getName().equals(page.getName())) {
                    stepsListView.getSelectionModel().select(i, true);
                }
            }
        }
    }

    private void initView() {
        if (getName() != null)
            this.setHeading(getName());

        // add pages
        contentContainer().setLayout(new RowLayout(Orientation.VERTICAL));
        stepsOverviewContainer().setLayout(new FitLayout());

        // add caption area
        createCaption();

        // add content area
        createContent();

        // create buttons
        createButtons();

        // add view to window
        setLayout(new RowLayout(Orientation.HORIZONTAL));
        if (stepsOverviewPanelWidth != 0) {
            add(stepsOverviewContainer(), new RowData(stepsOverviewPanelWidth,
                    1));
        }
        add(contentContainer(), new RowData(1 - stepsOverviewPanelWidth, 1));
    }

    protected void createContent() {
        LayoutContainer contentPanel = new LayoutContainer();
        contentPanel.setLayout(new FitLayout());
        contentContainer().add(contentPanel,
                new RowData(1, 1, new Margins(0, 4, 0, 4)));

        if (stepsOverviewPanelWidth != 0) {
            ContentPanel stepsOverviewPanel = new ContentPanel();
            stepsOverviewPanel.setHeaderVisible(false);
            stepsOverviewPanel.setBorders(false);
            stepsOverviewPanel.setBodyBorder(false);
            stepsOverviewPanel.setLayout(new FitLayout());
            stepsOverviewContainer().add(stepsOverviewPanel,
                    new RowData(1, 1, new Margins(4, 4, 4, 4)));

            stepsListView = new ListView<NavigationModel>();
            stepsListView.setId("wizard-step-overview");
            stepsListView.setDisplayProperty("name");
            ListViewSelectionModel<NavigationModel> lvsm = new ListViewSelectionModel<NavigationModel>();
            lvsm.setSelectionMode(SelectionMode.SINGLE);
            stepsListView.setSelectionModel(lvsm);
            stepsListView.setBorders(false);
            stepsListView.setHeight("100%");
            stepsListStore = new ListStore<NavigationModel>();
            stepsListView.setStore(stepsListStore);
            stepsListView.setTemplate(getStepsTemplate());
            stepsListView.setStyleName("wizard-list");
            stepsListView.setItemSelector("li.wizard-item");
            stepsListView.setSelectStyle("wizard-item-selected");
            // stepsListView.setOverStyle("wizard-item-over");
            lvsm
                    .addSelectionChangedListener(new SelectionChangedListener<NavigationModel>() {

                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<NavigationModel> se) {
                            // showPage(stepsListStore.indexOf(se.getSelectedItem()));
                        }
                    });
            stepsListView.show();

            stepsOverviewContainer().add(stepsOverviewPanel);
            stepsOverviewPanel.add(stepsListView);
        }

        for (IWizardPage page : pages) {
            contentPanel.add(page.getContainer());
            if (stepsOverviewPanelWidth != 0) {
                stepsListStore.add(new NavigationModel(page.getName()));
            }
        }
    }

    private void createCaption() {
        ContentPanel captionPanel = new ContentPanel();
        captionPanel.setHeaderVisible(false);
        captionPanel.setLayout(new VBoxLayout());

        pageName = new Label();
        pageName.setStyleAttribute("font-size", "14px");
        pageName.setStyleAttribute("font-weight", "700");
        pageDesc = new Label();
        pageDesc.setStyleAttribute("font-style", "italic");

        captionPanel.add(pageName);
        captionPanel.add(pageDesc);

        contentContainer().add(captionPanel,
                new RowData(1, 60, new Margins(4, 4, 4, 4)));
    }

    private void createButtons() {
        btnPre = new Button(PREVIOUS);
        btnNext = new Button(NEXT);
        btnFini = new Button(FINISH);
        btnCancel = new Button(CANCEL);

        btnPre.setId("buttonPre");
        btnNext.setId("buttonNext");
        btnPre.setId("buttonFinish");
        btnPre.setId("buttonCancel");

        addButton(btnPre);
        addButton(btnNext);
        addButton(btnFini);
        addButton(btnCancel);

        updateButtonStatus();

        btnPre.addSelectionListener(new WizardButtonEventListener());
        btnNext.addSelectionListener(new WizardButtonEventListener());
        btnFini.addSelectionListener(new WizardButtonEventListener());
        btnCancel.addSelectionListener(new WizardButtonEventListener());
    }

    private void updateButtonStatus() {
        btnPre.setEnabled(getPreviousPage(getCurrentPage()) != null);

        boolean condition = getCurrentPage() != null
                && getCurrentPage().isPageComplete()
                && getNextPage(getCurrentPage()) != null;
        btnNext.setEnabled(condition);

        btnFini.setEnabled(canFinish());
    }

    protected native String getStepsTemplate()/*-{
                                              return ['<ul>','<tpl for=".">',
                                              '<li class="wizard-item"><span class="wizard-text">{name}</span></li>',
                                              '</tpl>',
                                              '</ul>'].join("");
                                              }-*/;

    @Override
    public void show() {
        if (!isInit) {
            initView();
            isInit = true;
        } else {
            // reset status
            reset();
        }
        // show current page
        showPage(currentPageIndex);

        super.show();
    }

    @Override
    public void close() {
        hide();
    }

    private void showPage(int index) {
        if (index < 0 || index > pages.size() - 1) {
            return; // invalid index
        }

        for (int i = 0; i < pages.size(); i++) {
            pages.get(i).getContainer().setVisible(i == index);
            if (pages.get(i).getContainer().getLayout() != null)
                pages.get(i).getContainer().getLayout().layout();
        }

        currentPageIndex = index;

        if (getCurrentPage() != null) {
            IWizardPage page = getCurrentPage();
            pageName.setText(page.getName());
            pageDesc.setText("    " + page.getDescription());
            chooseWizardStepDesc(page);
        }

        updateButtonStatus();
    }

    private void reset() {
        // reset all wizard page
        resetAllPages();

        currentPageIndex = 0;
    }

    private void resetAllPages() {
        for (IWizardPage page : pages) {
            page.reset();
        }
        clearError();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean canFinish() {
        boolean result = !pages.isEmpty();
        for (IWizardPage page : pages) {
            result = result && page.isPageComplete();
        }
        return result;
    }

    @Override
    public IWizardPage getNextPage(IWizardPage currentPage) {
        IWizardPage result = null;
        int index = findPage(currentPage);
        if (index < pages.size() - 1 && !pages.isEmpty()) {
            result = pages.get(index + 1);
        }
        return result;
    }

    private int findPage(IWizardPage currentPage) {
        int i;
        for (i = 0; i < pages.size(); i++) {
            if (pages.get(i).equals(currentPage)) {
                break;
            }
        }
        return i;
    }

    @Override
    public IWizardPage getPage(String pageName) {
        IWizardPage result = null;
        for (IWizardPage page : pages) {
            if (page.getName().equals(pageName)) {
                result = page;
                break;
            }
        }
        return result;
    }

    @Override
    public void addPage(IWizardPage page) {
        pages.add(page);
        page.setWizard(this);
    }

    @Override
    public void addPage(int pos, IWizardPage page) {
        pages.add(pos, page);
        page.setWizard(this);
    }

    @Override
    public void removePage(IWizardPage page) {
        pages.remove(page);
    }

    @Override
    public void removePage(int pos) {
        if (pos < pages.size()) {
            pages.remove(pos);
        }
    }

    @Override
    public IWizardPage getPage(int pos) {
        if (pages.size() > pos) {
            return pages.get(pos);
        }
        return null;
    }

    @Override
    public IWizardPage[] getPages() {
        return (IWizardPage[]) pages.toArray();
    }

    @Override
    public IWizardPage getPreviousPage(IWizardPage currentPage) {
        IWizardPage result = null;
        int index = findPage(currentPage);
        if (index > 0 && index < pages.size()) {
            result = pages.get(index - 1);
        }
        return result;
    }

    @Override
    public IWizardPage getStartingPage() {
        IWizardPage result = null;
        if (!pages.isEmpty()) {
            result = pages.get(0);
        }

        return result;
    }

    @Override
    public IWizardPage getCurrentPage() {
        if (currentPageIndex < 0 || currentPageIndex > pages.size() - 1)
            return null;
        return pages.get(currentPageIndex);
    }

    @Override
    public String getError() {
        return error;
    }

    @Override
    public void setError(String error) {
        this.error = error;
        pageDesc.setText(this.error);
    }

    @Override
    public void clearError() {
        this.error = "";
        if (getCurrentPage() != null
                && getCurrentPage().getDescription() != null)
            pageDesc.setText(getCurrentPage().getDescription());
        else
            pageDesc.setText("");
    }

    @Override
    public void updateUI() {
        updateButtonStatus();
        if (getCurrentPage() != null) {
            if (getCurrentPage().getErrorMessage() != null) {
                setError(getCurrentPage().getErrorMessage());
            } else {
                clearError();
            }
        }
    }

    @Override
    public void showPage(IWizardPage page) {
        int index = findPage(page);
        if (index < pages.size())
            showPage(findPage(page));
    }
}
