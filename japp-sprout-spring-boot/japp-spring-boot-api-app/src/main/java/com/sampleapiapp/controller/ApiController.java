package com.sampleapiapp.controller;

import java.io.IOException;
import java.nio.file.Files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.itdevcloud.japp.core.api.rest.BaseRestController;
import com.itdevcloud.japp.core.common.AppConfigKeys;
import com.itdevcloud.japp.se.common.util.StringUtil;
import com.sampleapiapp.api.bean.LoginRequest;
import com.sampleapiapp.api.bean.LoginResponse;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;

//=====InsertNewImportByCodeGen=====

@RestController
@RequestMapping(value = "/${" + AppConfigKeys.JAPPCORE_APP_API_CONTROLLER_PATH_ROOT + "}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiController extends BaseRestController {

	private static final Logger logger = LogManager.getLogger(ApiController.class);

	@Value("${jappcore.openapi.externalDocs.filename}")
	String fileName;

	@Hidden
	@GetMapping("/open/v1/apidoc")
	@ResponseBody
	public ResponseEntity<Resource> downloadApidoc() {

		if(StringUtil.isEmptyOrNull(fileName)) {
			logger.debug("Downloading API Doc - no API file defined in the property file, do nothing!" );
			return ResponseEntity.notFound().build();
		}

		logger.debug("Downloading API Doc - " + fileName);
		Resource resourceFile = new ClassPathResource("/docs/" + fileName);

		HttpHeaders header = new HttpHeaders();
		header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
		header.add("Cache-Control", "no-cache, no-store, must-revalidate");
		header.add("Pragma", "no-cache");
		header.add("Expires", "0");

		ByteArrayResource resource;
		long length = 0;
		try {
			//logger.debug("Downloading API Doc - " + fileName);
			resource = new ByteArrayResource(Files.readAllBytes(resourceFile.getFile().toPath()));
			length = resourceFile.getFile().length();
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok()
				.headers(header)
				.contentLength(length)
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.body(resource);
	}

	@Operation(summary = "System Client Authentication",
			description = "Authenticate a system client by username and password",
			tags = { "Authentication" }
			)

	@PostMapping("/open/v1/auth")
	LoginResponse login(@RequestBody LoginRequest request) {

		LoginResponse response = (LoginResponse) processRequest(request);

		return response;
	}

	//=====InsertNewAPIByCodeGen=====

}