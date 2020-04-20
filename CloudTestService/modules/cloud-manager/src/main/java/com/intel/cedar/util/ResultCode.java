package com.intel.cedar.util;

public enum ResultCode {
    SUCCESS(0), INTERNEL_ERROR(1), NOT_FOUND(2), IN_USE(3);

    ResultCode(int code) {
        this.code = code;
    }

    private int code;
}
