package com.itdevcloud.japp.se.common.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import com.itdevcloud.japp.se.common.service.JulLogger;
import com.itdevcloud.japp.se.common.vo.PkiPemVO;
import com.itdevcloud.japp.se.common.vo.PkiSignAndEncryptVO;

public class PkiUtil {
	private static final JulLogger logger = JulLogger.getLogger(PkiUtil.class.getName());

	public static final String certPrefix = "-----BEGIN CERTIFICATE-----";
	public static final String certSuffix = "-----END CERTIFICATE-----";
	public static final String publicKeyPrefix = "-----BEGIN PUBLIC KEY-----";
	public static final String publicKeySuffix = "-----END PUBLIC KEY-----";

	public static final String PUBLIC_KEY_ALGORITHM_RSA = "RSA";

	public static final String CERTIFICATE_TYPE_X059 = "X.509";

	private static final String SIGNING_ALGORITHM = "SHA256withRSA";

	public static PublicKey getPublicKeyFromString(String publicKeyStr, String algorithm) {
		if (StringUtil.isEmptyOrNull(publicKeyStr)) {
			logger.warning("getPublicKeyFromString() - publicKeyStr is null, return null.......");
			return null;
		}
		if (StringUtil.isEmptyOrNull(algorithm)) {
			algorithm = PUBLIC_KEY_ALGORITHM_RSA;
		}
		publicKeyStr = publicKeyStr.replaceAll("(\\r|\\n)", "");
		publicKeyStr = publicKeyStr.replaceAll(PkiUtil.publicKeyPrefix, "");
		publicKeyStr = publicKeyStr.replaceAll(PkiUtil.publicKeySuffix, "");
		try {
			byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
			PublicKey publicKey = keyFactory.generatePublic(keySpec);
			return publicKey;
		} catch (Throwable t) {
			logger.severe("Can not get public key from the String, error: " + t, t);
			return null;
		}
	}

	public static Certificate getCertificateFromString(String certStr) {
		if (StringUtil.isEmptyOrNull(certStr)) {
			logger.warning("getCertificateFromString() - certStr is null, return null.......");
			return null;
		}
		ByteArrayInputStream in = null;
		BufferedInputStream bis = null;
		if (!certStr.startsWith(PkiUtil.certPrefix)) {
			certStr = PkiUtil.certPrefix + "\n" + certStr + "\n" + PkiUtil.certSuffix;
		}
		try {
			// logger.debug(".........certStr=" + certStr);
			in = new ByteArrayInputStream(certStr.getBytes(StandardCharsets.UTF_8));
			bis = new BufferedInputStream(in);
			CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_TYPE_X059);

			while (bis.available() <= 0) {
				// logger.debug(".........certStr=" + certStr);
				String err = "Can't Parse certificate: ...Stop....!";
				logger.severe(err);
				return null;
			}
			Certificate cert = cf.generateCertificate(bis);
			bis.close();
			bis = null;
			in.close();
			in = null;
			return cert;
		} catch (Throwable t) {
			logger.severe("Can not get public key from the String, error: " + t, t);
			return null;
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static PublicKey getPublicKeyFromCertificate(Certificate certificate) {
		if (certificate == null) {
			logger.warning("getPublicKeyFromCertificate() - certificate is null, return null.......");
			return null;
		}
		return certificate.getPublicKey();
	}
 
	public static PkiPemVO getPublicKeyPemString(PublicKey publicKey) {
		if (publicKey == null) {
			logger.warning("getPublicKeyPemString() - public Key is null, return null.......");
			return null;
		}
		PkiPemVO pkiPemVO = new PkiPemVO();
		try {
			byte[] bytes = publicKey.getEncoded();
			String encodedKey = null;
			if (bytes != null) {
				encodedKey = new String(Base64.getEncoder().encodeToString(bytes));
				encodedKey = publicKeyPrefix + System.lineSeparator() + encodedKey + System.lineSeparator()
						+ publicKeySuffix;
				pkiPemVO.setPublicKey(encodedKey);
				pkiPemVO.setAlgorithm(publicKey.getAlgorithm());
				pkiPemVO.setKeyFormat(publicKey.getFormat());
				return pkiPemVO;
			} else {
				logger.warning("getPublicKeyPemString() - can not get publicKey encoded bytes, return null.......");
				return null;
			}
		} catch (Throwable t) {
			logger.severe("Can not get public key from the String, error: " + t, t);
			return null;
		}
	}

	public static PkiPemVO getCertificateAndPublicKeyPemString(Certificate certificate, boolean includesKey) {
		if (certificate == null) {
			logger.warning("getCertificatePemString() - certificate is null, return null.......");
			return null;
		}
		PkiPemVO pkiPemVO = null;
		if(includesKey) {
			pkiPemVO = getPublicKeyPemString(certificate.getPublicKey());
		}
		if (pkiPemVO == null) {
			pkiPemVO = new PkiPemVO();
		}
		try {
			byte[] bytes = certificate.getEncoded();
			String encodedCert = null;
			if (bytes != null) {
				logger.warning("getCertificatePemString() - can not get encoded bytes, return null.......");
				encodedCert = new String(Base64.getEncoder().encodeToString(bytes));
				encodedCert = certPrefix + System.lineSeparator() + encodedCert + System.lineSeparator()
						+ certSuffix;
				pkiPemVO.setCertificate(encodedCert);
				pkiPemVO.setCertificateType(certificate.getType());
				return pkiPemVO;
			} else {
				logger.warning(
						"getCertificateAndPublicKeyPemString() - can not get certificate encoded bytes, return null.......");
				return null;
			}
		} catch (Throwable t) {
			logger.severe("Can not get public key from the String, error: " + t, t);
			return null;
		}
	}

	public static PkiSignAndEncryptVO signAndEncrypt(PrivateKey privateKey, String message, boolean sign,
			boolean encrypt) {
		if (StringUtil.isEmptyOrNull(message) || privateKey == null) {
			logger.warning("signAndEncrypt() - privateKey and/or message is null, do nothing, return null.......");
			return null;
		}
		PkiSignAndEncryptVO pkiSignAndEncryptVO = new PkiSignAndEncryptVO();
		try {
			if (encrypt) {
				String encryptedMessage = rsaEncrypt(privateKey, message);
				pkiSignAndEncryptVO.setEncryptedMessage(encryptedMessage);
			}
			if (sign) {
				String signature = sign(privateKey, message);
				pkiSignAndEncryptVO.setSignature(signature);
			}
			pkiSignAndEncryptVO.setMessage(message);
			return pkiSignAndEncryptVO;
		} catch (Throwable t) {
			logger.severe("signAndEncrypt() Can not sign and encrypt the message, error: " + t, t);
			return null;
		}
	}

	public static String sign(PrivateKey privateKey, String message) {
		if (StringUtil.isEmptyOrNull(message) || privateKey == null) {
			logger.warning("sign() - privateKey and/or message is null, do nothing, return null.......");
			return null;
		}

		try {
			Signature signature = Signature.getInstance(SIGNING_ALGORITHM);
			signature.initSign(privateKey);
			byte[] textBytes = StringUtil.getBytes(message);
			signature.update(textBytes);
			byte[] signatureBytes = signature.sign();

			return StringUtil.getString(Base64.getEncoder().encode(signatureBytes));
		} catch (Throwable t) {
			logger.severe("Can not sign the message, error: " + t, t);
			return null;
		}
	}

	public static boolean verifySignature(PublicKey publicKey, String signatureStr, String message) {
		if (publicKey == null || StringUtil.isEmptyOrNull(signatureStr) || StringUtil.isEmptyOrNull(message)) {
			logger.warning(
					"verifySignature() - publicKey, signatureStr and/or message is null, do nothing, return false.......");
			return false;
		}

		try {
			Signature signature = Signature.getInstance(SIGNING_ALGORITHM);
			signature.initVerify(publicKey);
			byte[] textBytes = StringUtil.getBytes(message);

			signature.update(textBytes);
			byte[] signatureBytes = Base64.getDecoder().decode(signatureStr);

			boolean isCorrect = signature.verify(signatureBytes);

			return isCorrect;
		} catch (Throwable t) {
			logger.severe("verifySignature() - Can not verify the message signature, error: " + t, t);
			return false;
		}
	}

	public static String rsaEncrypt(PrivateKey privateKey, String message) {
		if (privateKey == null || StringUtil.isEmptyOrNull(message)) {
			logger.warning(
					"rsaEncrypt() - publicKey, privateKey and/or message is null, do nothing, return null.......");
			return null;
		}
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			byte[] textBytes = StringUtil.getBytes(message);
			byte[] encryptedBytes = cipher.doFinal(textBytes);
			return StringUtil.getString(Base64.getEncoder().encode(encryptedBytes));
		} catch (Throwable t) {
			logger.severe("rsaEncrypt() - Can not encrypt the message, error: " + t, t);
			return null;
		}
	}

	public static String rsaDecrypt(PublicKey publicKey, String encryptedMessage) {
		if (publicKey == null || StringUtil.isEmptyOrNull(encryptedMessage)) {
			logger.warning("rsaDecrypt() - publicKey and/or encryptedMessage is null, do nothing, return null.......");
			return null;
		}
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
			byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMessage);
			byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

			return StringUtil.getString(decryptedBytes);
		} catch (Throwable t) {
			logger.severe("rsaDecrypt() - Can not dencrypt the message, error: " + t, t);
			return null;
		}
	}

}