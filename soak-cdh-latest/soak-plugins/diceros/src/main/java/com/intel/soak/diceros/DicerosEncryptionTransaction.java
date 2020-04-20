package com.intel.soak.diceros;

import com.intel.soak.diceros.utils.Hex;
import com.intel.soak.plugin.annotation.PLUGIN_TYPE;
import com.intel.soak.plugin.annotation.Plugin;
import com.intel.soak.transaction.AbstractTransaction;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author: Joshua Yao (yi.a.yao@intel.com)
 * @since: 7/3/14 10:46 AM
 */
@Plugin(desc="diceros encryption transaction", type = PLUGIN_TYPE.TRANSACTION)
public class DicerosEncryptionTransaction extends AbstractTransaction {

    private Cipher cipher;

    private ByteBuffer inputByteBuffer;
    private ByteBuffer encByteBuffer;

    private byte[] inputByteArray;
    private byte[] encByteArray;

    boolean isDirectBuff;

    private byte[] getKeyOrIV(String name) throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        String value = getParamValue(name);
        byte[] rs = new byte[16];
        if (value == null || value.isEmpty()) {
            sr.nextBytes(rs);
        } else {
            rs = Hex.decode(value);
        }
        return rs;
    }

    @Override
    public boolean beforeExecute() {
        try {
            String inputSizeStr = getParamValue("inputSize");
            int inputSize = inputSizeStr.contains("KB") ?
                    1024 * Integer.valueOf(inputSizeStr.replace("KB", "")) :
                    Integer.valueOf(inputSizeStr);
            inputByteArray = new byte[inputSize];

            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.nextBytes(inputByteArray);

            int encryptResultSize = inputSize;

            String transformation = getParamValue("transformation");
            if (transformation.endsWith("NoPadding") ||
                    transformation.endsWith("NOPADDING")) {
                encryptResultSize = inputSize + 16 - inputSize % 16;
            }

            String provider = getParamValue("provider");
            provider = provider == null || provider.isEmpty() ? "DC" : provider;

            cipher = Cipher.getInstance(transformation, provider);
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(getKeyOrIV("key"), "AES"),
                    new IvParameterSpec(getKeyOrIV("iv")));

            isDirectBuff = Boolean.valueOf(getParamValue("directBuffer"));
            if (isDirectBuff) {
                inputByteBuffer = ByteBuffer.allocateDirect(inputSize);
                inputByteBuffer.put(inputByteArray);
                inputByteBuffer.flip();
                encByteBuffer = ByteBuffer.allocateDirect(encryptResultSize);

            } else {
                encByteArray = new byte[encryptResultSize];
            }
            return true;
        } catch (Throwable e) {
            logger.error("Execute transaction failed: " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean execute() {
        try {
            if (isDirectBuff) {
                cipher.doFinal(inputByteBuffer, encByteBuffer);
            } else {
                cipher.doFinal(inputByteArray, 0, inputByteArray.length, encByteArray, 0);
            }
            return true;
        } catch (Exception e) {
            logger.error("Execute transaction failed: " + e.toString());
            return false;
        }
    }

    @Override
    public boolean afterExecute() {
        try {
            if (isDirectBuff) {
                inputByteBuffer.flip();
                inputByteBuffer.limit(inputByteBuffer.capacity());
                encByteBuffer.flip();
                encByteBuffer.limit(encByteBuffer.capacity());
            }
            return true;
        } catch (Exception e) {
            logger.error("Execute transaction failed: " + e.toString());
            return false;
        }
    }

}
