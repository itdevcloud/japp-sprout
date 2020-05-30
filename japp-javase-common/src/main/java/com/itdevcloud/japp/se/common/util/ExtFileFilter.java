/*
 * Copyright (c) 2018 the original author(s). All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.itdevcloud.japp.se.common.util;

import java.io.*;
import java.util.*;


/**
 * The class is used in the FileUtil class as a filter to help
 * get the required file. Usually, need set accepted extension or denied 
 * extension before use it. Refer to FileUtil as an example.
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class ExtFileFilter implements FileFilter{
    
    private List<String> acceptedExtensionList;
    private List<String> deniedExtensionList;

    private List<String> acceptedDirList;
    private List<String> deniedDirList;

    //======== get /set methods ===========
    public List<String> getAcceptedFileExtensionList(){
        return this.acceptedExtensionList;
    }
    public void setAcceptedFileExtensionList(List<String> al){
        this.acceptedExtensionList = al;
    }
    public void addAcceptedFileExtension(String value){
        if(this.acceptedExtensionList == null){
            this.acceptedExtensionList = new ArrayList<String>();
        }
        this.acceptedExtensionList.add(value);
    }
    
    public List<String> getDeniedFileExtensionList(){
        return this.deniedExtensionList;
    }
    public void setDeniedFileExtensionList(ArrayList<String> al){
        this.deniedExtensionList = al;
    }
    public void addDeniedFileExtension(String value){
        if(this.deniedExtensionList == null){
            this.deniedExtensionList = new ArrayList<String>();
        }
        this.deniedExtensionList.add(value);
    }
    
    
    public List<String> getAcceptedDirList() {
		return acceptedDirList;
	}
	public void setAcceptedDirList(List<String> acceptedDirList) {
		this.acceptedDirList = acceptedDirList;
	}
    public void addAcceptedDir(String value){
        if(this.acceptedDirList == null){
            this.acceptedDirList = new ArrayList<String>();
        }
        this.acceptedDirList.add(value);
    }

	public List<String> getDeniedDirList() {
		return deniedDirList;
	}
	public void setDeniedDirList(List<String> deniedDirList) {
		this.deniedDirList = deniedDirList;
	}
    public void addDeniedDir(String value){
        if(this.deniedDirList == null){
            this.deniedDirList = new ArrayList<String>();
        }
        this.deniedDirList.add(value);
    }

	/*
     * implement required method for this interface
     */
    public boolean accept(File file) {
        
        String fileName = file.getName();
        if(file.isDirectory()){
            return true;
        }
        if(this.acceptedExtensionList != null){
            for(int i = 0; i < this.acceptedExtensionList.size(); i++){
                if(fileName.endsWith((String)this.acceptedExtensionList.get(i))){
                    return true;
                }
            }//end for
            return false;
        }
        if(this.deniedExtensionList != null){
            for(int i = 0; i < this.deniedExtensionList.size(); i++){
                if(fileName.endsWith((String)this.deniedExtensionList.get(i))){
                    return false;
                }
            }//end for
            return true;
        }
        //if no accept or denied ext listed, return true
        return true;
    }
	/*
     * implement required method for this interface
     */
    public boolean acceptDir(File file) {
        
        String fileName = file.getAbsolutePath();
        if(!file.isDirectory()){
            return true;
        }
        if(!fileName.endsWith(File.separator)){
        	fileName = fileName + File.separator;
        }

        if(this.acceptedDirList != null){
            for(int i = 0; i < this.acceptedDirList.size(); i++){
                if((fileName.indexOf(this.acceptedDirList.get(i))!= -1)){
                    return true;
                }
            }//end for
            return false;
        }
        if(this.deniedDirList != null){
            for(int i = 0; i < this.deniedDirList.size(); i++){
                if(fileName.indexOf(this.deniedDirList.get(i)) != -1){
                    return false;
                }
            }//end for
            return true;
        }
        //if no accept or denied ext listed, return true
        return true;
    }
    
}//end class definition


