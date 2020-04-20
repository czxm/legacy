package com.intel.cedar.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.zip.Adler32;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.util.encoders.UrlBase64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hashes {
    private static Logger LOG = LoggerFactory.getLogger(Hashes.class);

    public static byte[] getPemBytes(final Object o) {
        PEMWriter pemOut;
        ByteArrayOutputStream pemByteOut = new ByteArrayOutputStream();
        try {
            pemOut = new PEMWriter(new OutputStreamWriter(pemByteOut));
            pemOut.writeObject(o);
            pemOut.close();
        } catch (IOException e) {
            LOG.error("", e);// this can never happen
        }
        return pemByteOut.toByteArray();
    }

    public static X509Certificate getPemCert(final byte[] o) {
        X509Certificate x509 = null;
        PEMReader in = null;
        ByteArrayInputStream pemByteIn = new ByteArrayInputStream(o);
        in = new PEMReader(new InputStreamReader(pemByteIn));
        try {
            x509 = (X509Certificate) in.readObject();
        } catch (IOException e) {
            LOG.error("", e);// this can never happen
        }
        return x509;
    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public enum Digest {
        GOST3411, Tiger, Whirlpool, MD2, MD4, MD5, RipeMD128, RipeMD160, RipeMD256, RipeMD320, SHA1, SHA224, SHA256, SHA384, SHA512;

        public MessageDigest get() {
            try {
                return MessageDigest.getInstance(this.name());
            } catch (Exception e) {
                LOG.error("", e);
                System.exit(-4);
                return null;
            }
        }
    }

    public enum Mac {
        HmacSHA1, HmacSHA256
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

    public static String hashPassword(String password)
            throws NoSuchAlgorithmException {
        byte[] fp = Digest.MD5.get().digest(password.getBytes());
        return getHexString(fp);
    }

    public static String getDigestBase64(String input, Digest hash,
            boolean randomize) {
        byte[] inputBytes = input.getBytes();
        byte[] digestBytes = null;
        MessageDigest digest = hash.get();
        digest.update(inputBytes);
        if (randomize) {
            SecureRandom random = new SecureRandom();
            random.setSeed(System.currentTimeMillis());
            byte[] randomBytes = random.generateSeed(inputBytes.length);
            digest.update(randomBytes);
        }
        digestBytes = digest.digest();
        return new String(UrlBase64.encode(digestBytes));
    }

    public static String base64encode(String input) {
        return new String(UrlBase64.encode(input.getBytes()));
    }

    public static String base64decode(String input) {
        return new String(UrlBase64.decode(input.getBytes()));
    }

    public static String getFingerPrint(Key privKey) {
        try {
            byte[] fp = Digest.SHA1.get().digest(privKey.getEncoded());
            StringBuffer sb = new StringBuffer();
            for (byte b : fp)
                sb.append(String.format("%02X:", b));
            return sb.substring(0, sb.length() - 1).toLowerCase();
        } catch (Exception e) {
            LOG.error("", e);
            return null;
        }
    }

    public static String getHexString(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString().toLowerCase();
    }

    public static String getRandom(int size) {
        SecureRandom random = new SecureRandom();
        random.setSeed(System.nanoTime());
        byte[] randomBytes = new byte[size];
        random.nextBytes(randomBytes);
        return new String(UrlBase64.encode(randomBytes));
    }

    public static String generateId(final String userId, final String prefix) {
        Adler32 hash = new Adler32();
        String key = userId + (System.currentTimeMillis() * Math.random());
        hash.update(key.getBytes());
        String imageId = String.format("%s-%08X", prefix, hash.getValue());
        return imageId;
    }

    public static byte[] hexToBytes(String data) {
        int k = 0;
        byte[] results = new byte[data.length() / 2];
        for (int i = 0; i < data.length();) {
            results[k] = (byte) (Character.digit(data.charAt(i++), 16) << 4);
            results[k] += (byte) (Character.digit(data.charAt(i++), 16));
            k++;
        }

        return results;
    }

    public static String bytesToHex(byte[] data) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            buffer.append(byteToHex(data[i]));
        }
        return (buffer.toString());
    }

    public static String byteToHex(byte data) {
        StringBuffer hexString = new StringBuffer();
        hexString.append(toHex((data >>> 4) & 0x0F));
        hexString.append(toHex(data & 0x0F));
        return hexString.toString();
    }

    public static char toHex(int value) {
        if ((0 <= value) && (value <= 9))
            return (char) ('0' + value);
        else
            return (char) ('a' + (value - 10));
    }

    public static String getHexSignature() {
        try {
            /*
             * Signature signer = Signature.getInstance( "SHA256withRSA" );
             * signer.initSign( SystemCredentialProvider.getCredentialProvider(
             * Component.eucalyptus ).getPrivateKey( ) ); signer.update(
             * "cedar".getBytes( ) ); byte[] sig = signer.sign( );
             */
            String hexSig = bytesToHex("secret".getBytes());
            return hexSig;
        } catch (Exception e) {
            LOG.error("", e);
            System.exit(-5);
            return null;
        }
    }
}
