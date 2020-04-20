package com.intel.cedar.util;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public class Digester {
    public enum Digest {
        GOST3411, Tiger, Whirlpool, MD2, MD4, MD5, RipeMD128, RipeMD160, RipeMD256, RipeMD320, SHA1, SHA224, SHA256, SHA384, SHA512;

        public MessageDigest get() {
            try {
                return MessageDigest.getInstance(this.name());
            } catch (Exception e) {
                System.exit(-4);
                return null;
            }
        }
    }

    public static String getMD5Digest(String file) {
        MessageDigest digest = Digest.MD5.get();
        FileInputStream is = null;
        byte[] buffer = new byte[8192];
        int read = 0;
        try {
            is = new FileInputStream(file);
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            return bigInt.toString(16);
        } catch (Exception e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (Exception e) {
            }
        }
    }
}
