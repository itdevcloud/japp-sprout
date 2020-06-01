package com.itdevcloud.japp.core.frontend;

/**
 * A class represents a front end environment profile.
 * @author YangLi
 *
 */
		
public class FrontendEnvironment {
	private String name;
	private String production;
	private FrontendEnvSettings settings;
	
	/**
	 * Get the name of the environment.
	 * @return environment name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name of the environment.
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Indicate if it is on the production mode. It is a string value.
	 * @return if it is on the production mode
	 */
	public String getProduction() {
		return production;
	}
	
	/**
	 * Set if it is on the production mode or not.
	 * @param production a string true/false
	 */
	public void setProduction(String production) {
		this.production = production;
	}
	
	/**
	 * Get a <code>FrontendEnvSettings</code> object of this profile.
	 * @return FrontendEnvSettings
	 */
	public FrontendEnvSettings getSettings() {
		return settings;
	}
	
	/**
	 * Set a <code>FrontendEnvSettings</code> object.
	 * @param settings FrontendEnvSettings
	 */
	public void setSettings(FrontendEnvSettings settings) {
		this.settings = settings;
	}


}
