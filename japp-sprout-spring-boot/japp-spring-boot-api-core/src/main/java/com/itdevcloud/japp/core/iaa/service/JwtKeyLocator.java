package com.itdevcloud.japp.core.iaa.service;

import java.security.Key;

import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.se.common.util.StringUtil;

import io.jsonwebtoken.LocatorAdapter;
import io.jsonwebtoken.ProtectedHeader;

public class JwtKeyLocator extends LocatorAdapter<Key> {

	private String jwtIssuer = null;

	public JwtKeyLocator(String jwtIssuer) {
		super();
		if (StringUtil.isEmptyOrNull(jwtIssuer)) {
			jwtIssuer = "SELF";
		}
		this.jwtIssuer = jwtIssuer;
	}

	@Override
	public Key locate(ProtectedHeader header) { // both JwsHeader and JweHeader extend ProtectedHeader
		Key key = null;
		if ("SELF".equalsIgnoreCase(this.jwtIssuer)) {
			key = AppComponents.pkiKeyCache.getJappPublicKey();
		} else if ("ENTRA_ID".equalsIgnoreCase(this.jwtIssuer)) {
			String kid = "" + header.get("kid");
			String x5t = "" + header.get("x5t");
			key = AppComponents.aadJwksCache.getAadPublicKey(kid, x5t);
		} else {
			// default self
			key = AppComponents.pkiKeyCache.getJappPublicKey();
		}
		return key;
	}
	
//    @Override
//    public Key locate(JwsHeader header) {
//        String keyId = header.getKeyId(); //or any other parameter that you need to inspect
//        return lookupSignatureVerificationKey(keyId); //implement me
//    }
//
//    @Override
//    public Key locate(JweHeader header) {
//        String keyId = header.getKeyId(); //or any other parameter// that you need to inspect
//        return lookupDecryptionKey(keyId); //implement me
//    }
}
