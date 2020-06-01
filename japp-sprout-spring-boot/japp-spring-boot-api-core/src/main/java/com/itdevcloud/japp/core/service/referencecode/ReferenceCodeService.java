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
import com.itdevcloud.japp.core.service.customization.AppFactoryComponentI;

@Component
public class ReferenceCodeService implements AppFactoryComponentI {
	
	private Logger logger = LogManager.getLogger(ReferenceCodeService.class);

	public ArrayList<ReferenceCode> getReferenceCodeListByEntityType(String type) {
		return AppComponents.referenceCodeCache.getReferenceCodeListByEntityType(type);
	}

	public ReferenceCode getReferenceCodeByCode(String type, String code) {
		return AppComponents.referenceCodeCache.getReferenceCodeByCode(type, code);
	}

	public ReferenceCode getReferenceCodeById(String id) {
		return AppComponents.referenceCodeCache.getReferenceCodeById(id);
	}

	public List<ReferenceCode> getChildrenReferenceCodeListByParentId(String parentId) {
		return AppComponents.referenceCodeCache.getChildrenReferenceCodeListByParentId(parentId);
	}
	public List<ReferenceCode> getChildrenReferenceCodeListByParentCode(String parentType, String parentCode) {
		return AppComponents.referenceCodeCache.getChildrenReferenceCodeListByParentCode(parentType, parentCode);
	}


}
