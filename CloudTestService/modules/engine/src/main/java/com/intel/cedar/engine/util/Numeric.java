package com.intel.cedar.engine.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import com.intel.cedar.engine.xml.util.Whitespace;

public class Numeric {

    private static final Pattern decimalPattern = Pattern
            .compile("(\\-|\\+)?((\\.[0-9]+)|([0-9]+(\\.[0-9]*)?))");

    public static BigDecimal makeDecimalValue(CharSequence in, boolean validate) {
        String trimmed = Whitespace.trimWhitespace(in).toString();
        try {
            if (validate) {
                if (!decimalPattern.matcher(trimmed).matches()) {
                    return null;
                }
            }
            BigDecimal val = new BigDecimal(trimmed);
            return val;
        } catch (NumberFormatException err) {
            return null;
        }
    }
}
