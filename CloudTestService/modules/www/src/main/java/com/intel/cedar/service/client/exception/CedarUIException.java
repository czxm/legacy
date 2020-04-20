package com.intel.cedar.service.client.exception;

import com.intel.cedar.service.client.model.CedarOperation;

public class CedarUIException extends Exception {
    private static final long serialVersionUID = -1649547830881870913L;

    private CedarOperation op;

    private ExceptionSeverity severity = ExceptionSeverity.ERROR;

    private String traces;

    public CedarUIException() {
        super("Internal Error");
    }

    public CedarUIException(String msg, String traces) {
        super(msg);
        this.traces = traces;
    }

    public CedarUIException(CedarOperation op, String msg, String traces) {
        super(msg);
        this.setOp(op);
        this.traces = traces;
    }

    public CedarUIException(CedarOperation op, ExceptionSeverity severity,
            String msg, String traces) {
        super(msg);
        this.setOp(op);
        this.setSeverity(severity);
        this.traces = traces;
    }

    public void setSeverity(ExceptionSeverity severity) {
        this.severity = severity;
    }

    public ExceptionSeverity getSeverity() {
        return severity;
    }

    public void setOp(CedarOperation op) {
        this.op = op;
    }

    public CedarOperation getOp() {
        return op;
    }

    public void setTraces(String traces) {
        this.traces = traces;
    }

    public String getTraces() {
        return traces;
    }
}
