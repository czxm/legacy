package com.intel.cedar.service.client;

import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class RPCInvocation<T> extends RpcProxy<T> {
    private static Command DUMMY = new Command() {

        @Override
        public void execute() {
            // TODO Auto-generated method stub

        }

    };
    private boolean _sessionTerminated;
    private boolean _popupWaiting;
    private boolean _showProgress;
    private boolean _hideProgressAfterSuccess = true;
    private boolean _showConfirm;
    private boolean _showSuccess;
    private Dialog _progressDlg;
    private ServerCallback<T> _serverCallback;
    public Object _loadConfig; // Adapt for EXT-GWT existing mechanism
    public static int MAX_INVOKE_NUM = 10;
    private static int _timeout = 0;

    public RPCInvocation() {
        _sessionTerminated = false;
        _popupWaiting = false;
        _showProgress = false;
        _hideProgressAfterSuccess = true;
        _showConfirm = false;
        _showSuccess = false;
    }

    public RPCInvocation(boolean popupWaiting, boolean showProgress,
            boolean showConfirm, boolean showSuccess) {
        _sessionTerminated = false;
        _popupWaiting = popupWaiting;
        _showProgress = showProgress;
        _showConfirm = showConfirm;
        _showSuccess = showSuccess;
    }

    /**
     * invoke remote interface
     * 
     * @param reInvoke
     *            true to indicate an old invoke
     */
    public void invoke(final boolean reInvoke) {
        if (_sessionTerminated) {
            return;
        }

        if (_showConfirm && !reInvoke) {
            MessageBox.confirm("Confirm", getConfirmMsg(),
                    new Listener<MessageBoxEvent>() {

                        @Override
                        public void handleEvent(MessageBoxEvent be) {
                            if (be.getButtonClicked().getText()
                                    .equalsIgnoreCase("yes")) {
                                invokeImpl(reInvoke);
                            }
                        }

                    }).show();

            return;
        }

        invokeImpl(reInvoke);
    }

    private void invokeImpl(boolean reInvoke) {
        if (!reInvoke) {
            _serverCallback = new ServerCallback<T>(this, _popupWaiting,
                    _showProgress, _showSuccess);
            _serverCallback.setProgressDlgHook(_progressDlg);
            _serverCallback
                    .setHideProgressAfterSuccess(_hideProgressAfterSuccess);
        }
        _serverCallback.exec(reInvoke);
    }

    public void onComplete(T obj) {

    }

    public Command onFailure(Throwable t) {
        return DUMMY;
    }

    public void createPopup() {

    }

    public String getConfirmMsg() {
        return "";
    }

    public String getSuccessMsg() {
        return "Operation Succeed";
    }

    public String getProgressTitle() {
        return "";
    }

    public String getProgressMsg() {
        return "";
    }

    public String getProgressText() {
        return "";
    }

    public void increTimeout() {
        _timeout++;
    }

    public int getTimeout() {
        return _timeout;
    }

    protected void load(Object loadConfig, AsyncCallback<T> callback) {
        // TODO Auto-generated method stub
        if (_sessionTerminated) {
            return;
        }

        _loadConfig = loadConfig;
        ((ServerCallback<T>) callback)
                .setHideProgressAfterSuccess(_hideProgressAfterSuccess);
        ((ServerCallback<T>) callback).exec(false);
    }

    public void load(final DataReader<T> reader, final Object loadConfig,
            final AsyncCallback<T> callback) {
        // invoked indirectly by BasePagingLoader, the customized ServerCallback
        // objects
        // will be created here
        load(loadConfig, new ServerCallback<T>(this, _popupWaiting,
                _showProgress, _showSuccess) {
            public void onFailure(Throwable caught) {
                if (caught.getClass().getName().equals(
                        "CloudEucaReqTimeoutException")) {
                    exec(true);
                } else {
                    callback.onFailure(caught);
                }
            }

            @SuppressWarnings("unchecked")
            public void onSuccess(T result) {
                showSuccessBox();
                onComplete(result);
                try {
                    T data = null;
                    if (reader != null) {
                        data = reader.read(loadConfig, result);
                    } else {
                        data = (T) result;
                    }
                    callback.onSuccess(data);
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }

        });
    }

    public void setProgressDlgHook(Dialog box) {
        if (_showProgress)
            _progressDlg = box;
    }

    public void setHideProgressAfterSuccess(boolean hideProgressAfterSuccess) {
        this._hideProgressAfterSuccess = hideProgressAfterSuccess;
    }

    public abstract void execute(CloudRemoteServiceAsync remoteService,
            AsyncCallback<T> callback);

}
