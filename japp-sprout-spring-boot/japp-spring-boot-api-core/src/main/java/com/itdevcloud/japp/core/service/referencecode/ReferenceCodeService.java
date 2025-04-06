package com.itdevcloud.japp.core.service.referencecode;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.itdevcloud.japp.core.api.vo.ReferenceCode;
import com.itdevcloud.japp.core.cahce.ReferenceCodeCache;
import com.itdevcloud.japp.core.common.AppComponents;
import com.itdevcloud.japp.core.common.AppFactory;
import org.apache.logging.log4j.Logger;
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;
import com.itdevcloud.japp.se.common.util.StringUtil;

@Component
public class ReferenceCodeService implements AppFactoryComponentI {
	
	private static final Logger logger = LogManager.getLogger(ReferenceCodeService.class);

	
	public List<ReferenceCode> getReferenceCodeList(String codeDomain, String codeType) {
		 return AppComponents.referenceCodeCache.getReferenceCodeList(codeDomain, codeType);
	}
	

	public ReferenceCode getReferenceCode(String codeDomain, String codeType, String codeName) {
		 return AppComponents.referenceCodeCache.getReferenceCode(codeDomain, codeType, codeName);
	}

	public ReferenceCode getReferenceCode(long pk) {
		 return AppComponents.referenceCodeCache.getReferenceCode(pk);
	}


	public ReferenceCode getParent(String codeDomain, String codeType, String codeName) {
		 return AppComponents.referenceCodeCache.getParent(codeDomain, codeType, codeName);
	}

	public ReferenceCode getParent(long pk) {
		 return AppComponents.referenceCodeCache.getParent(pk);
	}

	public List<ReferenceCode> getChildren(long parentCodeId) {
		 return AppComponents.referenceCodeCache.getChildren(parentCodeId);
	}
	
	public List<ReferenceCode> getChildren(String codeDomain, String codeType, String codeName) {
		 return AppComponents.referenceCodeCache.getChildren(codeDomain, codeType, codeName);
	}



	

}
