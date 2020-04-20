/*
 * INTEL CONFIDENTIAL
 * Copyright 2009 Intel Corporation All Rights Reserved. 
 * 
 * The source code contained or described herein and all documents related to the 
 * source code ("Material") are owned by Intel Corporation or its suppliers or 
 * licensors. Title to the Material remains with Intel Corporation or its suppliers 
 * and licensors. The Material contains trade secrets and proprietary and 
 * confidential information of Intel or its suppliers and licensors. The Material 
 * is protected by worldwide copyright and trade secret laws and treaty provisions. 
 * No part of the Material may be used, copied, reproduced, modified, published, 
 * uploaded, posted, transmitted, distributed, or disclosed in any way without 
 * Intel's prior express written permission.
 * 
 * No license under any patent, copyright, trade secret or other intellectual 
 * property right is granted to or conferred upon you by disclosure or delivery of 
 * the Materials, either expressly, by implication, inducement, estoppel or 
 * otherwise. Any license under such intellectual property rights must be express 
 * and approved by Intel in writing.
 */
package com.intel.splat.identityservice.utils;

import java.io.ByteArrayOutputStream;
import java.security.AlgorithmParameters;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

/**
 * Utility class for encoding and decoding passwords.
 * 
 * @author wlpoziom
 * @since 2.1
 */
/*
 * This utility encapsulates the algorithm for encoding passwords. This encoding
 * generates keys and has an algorithm for burying keys within the encoded
 * value. This means that each password will be encrypted with a unique key. As
 * such, this algorithm is really what should be kept secret.
 * 
 * Implementation notes:
 * 
 * TODO Currently, this class is inherently vulnerable in that it can be reverse
 * engineered quite easily. So, in the future we need to take some steps to
 * protect this algorithm. This may mean obfuscating or pushing some of the
 * implementation into native code that would be harder to reverse engineer.
 * 
 * This class makes minimal use of constants so that they may not be reflected
 * to gather information about the algorithm. This makes the implementation
 * somewhat brittle. If something changes (particularly the lengths of things)
 * be sure to examine all side effects.
 * 
 * This class is not a high performance algorithm. it is assumed it is used
 * during config editing and component initialization. If it turns out to be in
 * the scope of transaction processing numerous optimizations could be added.
 * 
 * Exceptions thrown by this implementation will be vague as not to give
 * information about the implementation.
 */
public class PasswordUtil
{
	private static final SecureRandom s_random;

	static
	{
		try
		{
			s_random = SecureRandom.getInstance("SHA1PRNG");
		}
		catch (final Exception e)
		{
			throw new Error(e.getMessage(), e);
		}
	}

	/**
	 * Decode a password. This password must have been
	 * {@linkplain #encode(String) encoded} previously.
	 * 
	 * @param encodedPassword
	 *            the encoded password
	 * @return the clear text password
	 * @throws Exception
	 *             if an error occurs
	 */
	public static String decode(String encodedPassword) throws Exception
	{
		if (encodedPassword == null || encodedPassword.length() == 0)
		{
			throw new IllegalArgumentException();
		}

		/*
		 * Decode the base 64
		 */
		byte[] encrypted = Base64.decode(encodedPassword);

		/*
		 * Extract password. The password is scattered across a s_random value.
		 */
		byte[] passwordHex = extractPassword(encrypted);

		/*
		 * skip the s_random value; again limiting the use of constants so they
		 * can't be reflected.
		 */
		int pos = 32;

		/*
		 * generate the key
		 */
		final byte[] digestedPassword = generateKey(passwordHex);

		/*
		 * generate the key
		 */
		final KeySpec keySpec = new DESedeKeySpec(digestedPassword);
		final SecretKey key = SecretKeyFactory.getInstance("DESede").generateSecret(keySpec);

		/*
		 * Skip over the tags. For this there should only be the end tag.
		 */
		pos = skipTags(encrypted, pos);

		/*
		 * Grab the initialization vector.
		 */
		final IvParameterSpec iv = new IvParameterSpec(encrypted, pos, 8);
		pos += 8;

		/*
		 * create and initialize the cipher
		 */
		final Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key, iv);

		/*
		 * Do the decrypt
		 */
		final byte[] clear = cipher.doFinal(encrypted, pos, encrypted.length - pos);
		final String clearString = new String(clear, 0, clear.length);
		return clearString;
	}

	/**
	 * Encodes a password.
	 * 
	 * @param password
	 *            the password to be encoded.
	 * @return the encoded password
	 * @throws Exception
	 *             if an error occurs
	 */
	public static String encode(String password) throws Exception
	{
		if (password == null)
		{
			throw new IllegalArgumentException();
		}

		/*
		 * The format is base 64 encoded result of:
		 * 
		 * 
		 * 32 bytes of s_random (in which the password is scattered)
		 * 
		 * tag(s) -- 3 byte length + data. There is always at least an end tag.
		 * 
		 * 8 byte initialization vector </li>
		 * 
		 * encrypted data
		 */
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream(128);

		/*
		 * generate the random bytes that start it off.
		 */
		final byte[] random = new byte[32];
		s_random.nextBytes(random);
		buffer.write(random);

		/*
		 * Add the end tag Note here I am hardcoding the length. If the length
		 * of end tag changes please change here.
		 */
		buffer.write(new byte[]
			{ 0x30, 0x31, 0x36 });
		buffer.write(getEndTag());

		/*
		 * extract the password from the random bytes. Note this is in hex
		 * characters. i don';t understand the fascination with hex characters.
		 * Seems like it makes easier for folks to read which is a bad thing.
		 */
		final byte[] hexPassword = extractPassword(random);

		/*
		 * Build the key
		 */
		final byte[] digestedKey = generateKey(hexPassword);
		final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
		final SecretKey key = keyFactory.generateSecret(new DESedeKeySpec(digestedKey));

		/*
		 * build and initialize the cipher
		 */
		final Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key, s_random);

		/*
		 * Yank the initialization vector from the cipher.
		 */
		AlgorithmParameters params = cipher.getParameters();
		IvParameterSpec iv = params.getParameterSpec(IvParameterSpec.class);
		if (iv == null)
		{
			/*
			 * intentionally vague
			 */
			throw new Exception("encryption error");
		}
		buffer.write(iv.getIV());

		/*
		 * Now do the encryption
		 */
		byte[] encrypted = cipher.doFinal(password.getBytes());

		buffer.write(encrypted);

		/*
		 * finally, base 64 encode the value
		 */
		byte[] base64 = Base64.encode(buffer.toByteArray());
		return new String(base64);
	}

	/**
	 * provides an encoded password given a clear text password.
	 */
	public static void main(String[] args) throws Exception
	{
		for (final String arg : args)
		{
			final String encoded = encode(arg);
			final String decoded = decode(encoded);
			if (!arg.equals(decoded))
			{
				System.err.println("decoded value (\"" + decoded + "\") differs from original(\""
						+ arg + "\").  There is some problem with the algorithm.");
				System.exit(-1);
			}
			System.out.println("\"" + decoded + "\"=" + encoded);
		}
	}

	/**
	 * @param encrypted
	 * @return
	 */
	private static byte[] extractPassword(byte[] encrypted)
	{
		/*
		 * These are the indices by which the password seed is scattered among
		 * the 32 byte s_random prefix. This must match the {@code indices}
		 * array in {@code cbrsrc/cbrcore/src/oam/util/cfgcrypt/UT_ccrypt.H}.
		 * 
		 * note put here so cannot be obtained via reflection etc.
		 */
		final int[] PASSWORD_INDICES = new int[]
			{ 3, 8, 4, 9, 22, 27, 17, 20 };

		/* 
		 * pull the password out
		 */
		final byte[] password = new byte[PASSWORD_INDICES.length];
		for (int i = 0; i < PASSWORD_INDICES.length; i++)
		{
			password[i] = encrypted[PASSWORD_INDICES[i]];
		}

		/*
		 * encode as hex characters
		 */
		byte[] passwordHex = Hex.encode(password);
		return passwordHex;
	}

	/**
	 * @param passwordHex
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws DigestException
	 */
	private static byte[] generateKey(byte[] passwordHex) throws NoSuchAlgorithmException,
			DigestException
	{
		/*
		 * password is constructed from two overlapping digests.The first being
		 * from the password extrcted above. The other is actually a digest of
		 * and empty string.
		 */
		final byte[] digestedPassword = new byte[32];
		MessageDigest sha1 = MessageDigest.getInstance("SHA1");

		/*
		 * Digest the password in first 20
		 */
		sha1.update(passwordHex);
		sha1.digest(digestedPassword, 0, digestedPassword.length);

		/*
		 * Digest the constant ("") in the last 20.
		 */
		sha1.update(new byte[0]);
		sha1.digest(digestedPassword, 12, digestedPassword.length - 12);
		return digestedPassword;
	}

	/**
	 * @return
	 */
	private static byte[] getEndTag()
	{
		/*
		 * end tag delimiter. Here so cannot be reflected.
		 * 
		 * If the length of this array changes be sure to change the length in
		 * the encode method.
		 * 
		 * This is "??&*%?_?" (without quotes) in hex as bytes
		 */
		// final String END_TAG_STRING = new String(new char[]
		// { '3', 'f', '3', 'f', '2', '6', '2', 'a', '2', '5', '3', 'f', '5',
		// 'f', '3', 'f' });
		final byte[] END_TAG = new byte[]
			{ 0x33, 0x66, 0x33, 0x66, 0x32, 0x36, 0x32, 0x61, 0x32, 0x35, 0x33, 0x66, 0x35, 0x66,
					0x33, 0x66 };

		return END_TAG;
	}

	private static boolean isEqual(byte[] a1, int o1, byte[] a2, int o2, int len)
	{
		for (int i = 0; i < len; ++i)
		{
			if (a1[o1++] != a2[o2++])
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Skips over tags. Tags are some information accompanying an encrypted
	 * thing. Not used for simple passwords but an end tag is always minimally
	 * present.
	 * <p>
	 * Tags are int the form: 3 digit length + data.
	 * </p>
	 * 
	 * @param encrypted
	 *            the encrypted value which includes some header items including
	 *            the password
	 * @param pos
	 *            the offset to start looking for tags
	 * 
	 * @return the next position in the encrypted array
	 */
	private static int skipTags(final byte[] encrypted, int pos)
	{
		final byte[] endTag = getEndTag();

		final int TAG_LENGTH_LENGTH = 3;

		/*
		 * skip until hit end tag
		 */
		int tagSize;
		while (true)
		{
			tagSize = Integer.parseInt(new String(encrypted, pos, TAG_LENGTH_LENGTH));
			pos += TAG_LENGTH_LENGTH;
			try
			{
				if (isEqual(endTag, 0, encrypted, pos, tagSize))
				{
					break;
				}
			}
			finally
			{
				pos += tagSize;
			}
		}
		return pos;
	}

	/**
	 * Creates new instance. Forbidden
	 */
	private PasswordUtil()
	{
		super();
	}
}
