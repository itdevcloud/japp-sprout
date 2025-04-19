package com.itdevcloud.japp.core.iaa.service;

import java.security.Key;

import com.itdevcloud.japp.core.api.vo.AuthProviderVO;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppConstant;
import com.itdevcloud.japp.se.common.util.StringUtil;

import io.jsonwebtoken.LocatorAdapter;
import io.jsonwebtoken.ProtectedHeader;

public class JwtKeyLocator extends LocatorAdapter<Key> {

	private String authProvider = null;

	public JwtKeyLocator(String authProvider) {
		super();
		if (StringUtil.isEmptyOrNull(authProvider)) {
			authProvider = AppConstant.AUTH_PROVIDER_NAME_MY_APP;
		}
		this.authProvider = authProvider;
	}

	@Override
	public Key locate(ProtectedHeader header) { // both JwsHeader and JweHeader extend ProtectedHeader
		Key key = null;
		AuthProviderVO authProviderVO = AppComponents.authProviderCache.getAuthProviderInfo(this.authProvider);
		if(authProviderVO == null) {
			return null;
		}
		if (AppConstant.AUTH_PROVIDER_NAME_ENTRAID_OPENID.equalsIgnoreCase(this.authProvider)) {
			String kid = "" + header.get("kid");
			String x5t = "" + header.get("x5t");
			key = authProviderVO.getPublicKey(kid, x5t);
		} else {
			// default self
			key = authProviderVO.getPublicKey();
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
