package com.itdevcloud.japp.core.frontend;

import org.springframework.util.StringUtils;

/**
 * A class represents front end environment settings. 
 *
 */
public class FrontendEnvSettings {
	private String version;
	private String build;
	private String apiURL;
	private String stsAuthURL;
	private String loginURL;
	private String logoutURL;
	private String stsTokenURL;
	private String envJsonUrl;
	private String ipAuthentication;

	/**
	 * Get the version number.
	 * @return the version number.
	 */
	public String getVersion() {
		if(StringUtils.isEmpty(version)) {
			return "n/a";
		}
		return version;
	}
	
	/**
	 * Set the version number.
	 * @param version version number
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Get the build number of this application.
	 * @return build number
	 */
	public String getBuild() {
		if(StringUtils.isEmpty(build)) {
			return "n/a";
		}
		return build;
	}
	
	/**
	 * Set the build number of this application.
	 * @param build build number
	 */
	public void setBuild(String build) {
		this.build = build;
	}
	
	/**
	 * Get the API service's base url.
	 * @return the API service's base url
	 */
	public String getApiURL() {
		return apiURL;
	}
	
	/**
	 * Set the API service's base url.
	 * @param apiURL API service's base url
	 */
	public void setApiURL(String apiURL) {
		this.apiURL = apiURL;
	}
	
	/**
	 * Get the SdcSTS authorization url.
	 * @return the SdcSTS authorization url
	 */
	public String getStsAuthURL() {
		return stsAuthURL;
	}
	
	/**
	 * Set the SdcSTS authorization url.
	 * @param stsAuthURL SdcSTS authorization url
	 */
	public void setStsAuthURL(String stsAuthURL) {
		this.stsAuthURL = stsAuthURL;
	}
	
	/**
	 * Get the login url.
	 * @return login url
	 */
	public String getLoginURL() {
		return loginURL;
	}
	
	/**
	 * Set the login url.
	 * @param loginURL login url
	 */
	public void setLoginURL(String loginURL) {
		this.loginURL = loginURL;
	}
	
	/**
	 * Get the logout url.
	 * @return logout url
	 */
	public String getLogoutURL() {
		return logoutURL;
	}
	
	/**
	 * Set the logout url.
	 * @param logoutURL logout url
	 */
	public void setLogoutURL(String logoutURL) {
		this.logoutURL = logoutURL;
	}
	
	/**
	 * Get the SdcSTS token url.
	 * @return SdcSTS token url
	 */
	public String getStsTokenURL() {
		return stsTokenURL;
	}
	
	/**
	 * Set the SdcSTS token url.
	 * @param stsTokenURL SdcSTS token url
	 */
	public void setStsTokenURL(String stsTokenURL) {
		this.stsTokenURL = stsTokenURL;
	}
	
	/**
	 * Get the current environment's json file url.
	 * @return the current environment's json file url
	 */
	public String getEnvJsonUrl() {
		return envJsonUrl;
	}
	
	/**
	 * Set the current environment's json file url.
	 * @param envJsonUrl current environment's json file url
	 */
	public void setEnvJsonUrl(String envJsonUrl) {
		this.envJsonUrl = envJsonUrl;
	}

	public String getIpAuthentication() {
		return ipAuthentication;
	}

	public void setIpAuthentication(String ipAuthentication) {
		this.ipAuthentication = ipAuthentication;
	}
	

}