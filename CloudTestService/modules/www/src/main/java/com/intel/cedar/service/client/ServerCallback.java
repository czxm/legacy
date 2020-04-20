package com.intel.cedar.service.client;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Popup;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.intel.cedar.service.client.exception.CedarUIException;
import com.intel.cedar.service.client.view.ErrorDialog;

public class ServerCallback<T> implements AsyncCallback<T> {

    private RPCInvocation<T> _invocation;
    private boolean _popupWaiting;
    private boolean _showProgress;
    private boolean _hideProgressAfterSuccess;
    private boolean _showSuccess;

    private MessageBox _successBox;

    private Dialog _progressDlg;
    private MessageBox _progressBox;

    private Popup _popup;

    public ServerCallback(RPCInvocation<T> invocation, boolean popupWaiting,
            boolean showProgress, boolean showSuccess) {
        _invocation = invocation;
        _popupWaiting = popupWaiting;
        _showProgress = showProgress;
        _showSuccess = showSuccess;
    }

    public void exec(boolean reInvoke) {
        CloudRemoteServiceAsync remoteService = CloudRemoteServiceAsync.Util
                .getInstance();
        showProgressBox(reInvoke);
        _invocation.execute(remoteService, this);
    }

    public void onSuccess(T obj) {
        showSuccessBox();
        _invocation.onComplete(obj);
    }

    public void onFailure(Throwable t) {
        Command cbCmd = _invocation.onFailure(t);
        handleFailure(t, cbCmd);
    }

    public void showProgressBox(boolean reInvoke) {
        if (reInvoke)
            return;
        hidePopup();
        hideSuccessBox();
        // the priority of popup is larger than messagebox
        if (_popupWaiting) {
            _popup = new Popup();
            _popup.setSize(100, 50);
            _popup.setStyleName("loading");
            _popup.show();
            return;
        }
        if (_showProgress) {
            _progressBox = MessageBox
                    .wait(_invocation.getProgressTitle(), _invocation
                            .getProgressMsg(), _invocation.getProgressText());
        }
    }

    public void showSuccessBox() {
        hidePopup();
        hideProgressDlg();
        hideProgressBox();

        if (_showSuccess) {
            _successBox = MessageBox.info("Success", _invocation
                    .getSuccessMsg(), new Listener<MessageBoxEvent>() {
                public void handleEvent(MessageBoxEvent mbe) {

                }
            });
        }
    }

    public void hidePopup() {
        if (_popup != null)
            _popup.hide();
    }

    public void hideSuccessBox() {
        if (_successBox != null)
            _successBox.close();
    }

    public void hideProgressDlg() {
        if (_progressDlg != null && _hideProgressAfterSuccess)
            _progressDlg.hide();
    }

    public void hideProgressBox() {
        if (_progressBox != null && _hideProgressAfterSuccess)
            _progressBox.close();
    }

    protected void handleFailure(Throwable t, Command cb) {
        if (_progressDlg != null)
            _progressDlg.hide();
        if (_progressBox != null)
            _progressBox.close();

        if (t instanceof CedarUIException) {
            showErrorDialog((CedarUIException) t);
        }
    }

    public void setProgressDlgHook(Dialog box) {
        if (_showProgress)
            _progressDlg = box;
    }

    public void setHideProgressAfterSuccess(boolean hideProgressAfterSuccess) {
        _hideProgressAfterSuccess = hideProgressAfterSuccess;
    }

    private void showErrorDialog(CedarUIException e) {
        new ErrorDialog(e.getMessage(), e.getTraces()).show();
    }

}
