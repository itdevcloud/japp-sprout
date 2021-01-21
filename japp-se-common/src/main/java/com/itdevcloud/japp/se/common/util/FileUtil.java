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
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Class Definition
 *
 * @author Marvin Sun
 * @since 1.0.0
 */
public class FileUtil {
    
	private static Logger logger = Logger.getLogger(FileUtil.class.getName());
	
	public static void copy(String source, String destination)
    throws IOException {
        copy(source, destination, null);
    }
    
    public static void copy(String source, String destination, FileFilter filter)
    throws IOException {
        if (source == null || source.trim().equals("") ||
            destination == null || destination.trim().equals("")){
            return;
        } 
        File inputFile = new File(source);
        File outputFile = new File(destination);
        String tmpStr = null;

        if(!inputFile.exists()){
            return;
        }
        if(inputFile.isFile()){
            //destination can be a file or dir
            if(outputFile.isDirectory()){
                tmpStr =  outputFile.getPath() + File.separatorChar + inputFile.getName();
                copyFile(source, tmpStr);
            }else{
                //destination is a file or does not exist
                copyFile(source, destination);
            }//end if-else(outputFile.isDirectory())
            return;
        }else {
            //input file is a directory
            if(outputFile.isFile()){
                throw new IOException("Scource file < " + source + " > is a directory, so "+
                "destination < " + destination + " > should be a dictionary. ");
            }else{
                //destination is a file or does not exist
                copyDir(source, destination, filter);
            }//end if-else(outputFile.isDirectory())
            return;
        }//end if-else(inputFile.isFile())
    }//end copy() 

    private static void copyFile(String source, String destination)
    throws IOException {
        if (source == null || source.trim().equals("") ||
            destination == null || destination.trim().equals("")){
            return;
        } 
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try{
            File inputFile = new File(source);
            File outputFile = new File(destination);
            if(!inputFile.isFile() || outputFile.isDirectory()){
                throw new IOException("Scource file < " + source + " > should be a file and "+
                "destination < " + destination + " > should not be a dictionary. ");
            }
            in = new BufferedInputStream(new FileInputStream(inputFile));
            out = new BufferedOutputStream(new FileOutputStream(outputFile));

            System.out.println("Copy <" + source + " > to < " + destination + " >......");
            
            int c;
            while ((c = in.read()) != -1){
               out.write(c);
            }
            in.close();
            out.close();
        }catch(IOException e) {
            throw new IOException("Exception occurs during file copy process! " + 
            "scource file < " + source + ", destination > " + destination + " >. "+
            "Detail information: \n" + e);
        }finally {
            if (in != null) {
                try {
                   in.close();
                }catch(IOException e){
                    e.printStackTrace(); 
                }//end try-catch
             }//end if
            in = null;
            if (out != null) {
                try {
                   out.close();
                }catch(IOException e){
                    e.printStackTrace();
                }//end try-catch
             }//end if
             out = null;
        }//end try-catch
    }//end copyFile() 
    
    private static void copyDir(String source, String destination, FileFilter filter)
    throws IOException {
        if (source == null || source.trim().equals("") ||
            destination == null || destination.trim().equals("")){
            return;
        } 

        File inputFile = new File(source);
        File outputFile = new File(destination);
        if(!inputFile.isDirectory() || outputFile.isFile()){
            throw new IOException("Scource file < " + source + " > should be a directory and "+
            "destination < " + destination + " > should not be a file. ");
        }
        System.out.println("Copy directory <" + source + " > to < " + destination + " >......");
        //create destination dir when neccessary
        if(!outputFile.exists()){
            outputFile.mkdirs();
        }
        //begin copy process
        String destPath = outputFile.getAbsolutePath();
        String sourcePath = inputFile.getAbsolutePath();
        int len = sourcePath.length();
        String tmpStr = null;
        File tmpFile = null;
        File[] files = null;
        if(filter == null){
            files = inputFile.listFiles();   
        }else{
            files = inputFile.listFiles(filter);   
        }
        for (int i = 0; i < files.length; i++) { 
            tmpFile = files[i]; 
            tmpStr = destPath + tmpFile.getAbsolutePath().substring(len);
            if (tmpFile.isFile()) { 
                copyFile(tmpFile.getAbsolutePath(), tmpStr);
            }else{
                copyDir(tmpFile.getAbsolutePath(), tmpStr, filter);
            }
        }//end for
        return;    
    }//end copyDir() 

    /**
     * oldFileOrDir and newFileOrDir should be both dir or both file name
     *
     */
    public static boolean rename(String oldFileOrDir, String newFileOrDir)
    throws IOException {
        if (oldFileOrDir == null || oldFileOrDir.trim().equals("") ||
            newFileOrDir == null || newFileOrDir.trim().equals("")){
            return true;
        } 
        File oldFile = new File(oldFileOrDir);
        if(!oldFile.exists()){
            throw new IOException("Old file or directory < " + oldFileOrDir + " > does not exists. ");
        }
        File newFile = new File(newFileOrDir);
        if(newFile.exists()){
            throw new IOException("New file or directory < " + newFileOrDir + " > already exists. ");
        }
        if(oldFile.isDirectory()){
            //old is directory, then new name is a dirctory too
            return oldFile.renameTo(newFile);
        }else{
            //old file is a file, so newFile must be a file name
            String newPath = null;
            int idx = newFileOrDir.lastIndexOf(File.separatorChar);
            if(idx != -1){
                newPath = newFileOrDir.substring(0, idx); 
            }
            if(newPath != null){
                File newPathFile = new File(newPath);
                if(!newPathFile.isDirectory()){
                    newPathFile.mkdirs();
                }//end if
            }//end if(newPath != null)
            return oldFile.renameTo(newFile);
        }//end if-else
    }//end rename() 
    
    public static boolean move(String oldFileOrDir, String destDir)
    throws IOException {
        if (oldFileOrDir == null || oldFileOrDir.trim().equals("") ||
            destDir == null || destDir.trim().equals("")){
            return true;
        } 
        
        File oldFile = new File(oldFileOrDir);
        if(!oldFile.exists()){
            throw new IOException("Old file or directory < " + oldFileOrDir + " > does not exists.");
        }
        
        File newFile = new File(destDir);
        if(newFile.isFile()){
            throw new IOException("Destination Directory  < " + destDir + " > is a file not a directory.");
        }

        if(!newFile.isDirectory()){
            newFile.mkdirs();
        }
        String newFullFileName = newFile.getAbsolutePath() + File.separator + oldFile.getName();
        return oldFile.renameTo(new File(newFullFileName));
    }//end move() 
    
    public static List<String> list(String fileOrDir, FileFilter filter){
        
        if (fileOrDir == null || fileOrDir.trim().equals("")){
            return null;
        } 
        File oriFile = new File(fileOrDir);
        ArrayList<String> files = new ArrayList<String>();
        File[] tmpFiles = null;
        File tmpFile = null;
        
        if(filter == null){
            tmpFiles = oriFile.listFiles();   
        }else{
            tmpFiles = oriFile.listFiles(filter);   
        }
        for (int i = 0; i < tmpFiles.length; i++) { 
            tmpFile = tmpFiles[i]; 
            if (tmpFile.isFile()) { 
                files.add(tmpFile.getAbsolutePath());
            }else{
                files.addAll(list(tmpFile.getAbsolutePath(), filter));
            }
        }//end for
        return files;
    }//end list

    public static List<File> listFiles(String fileOrDir, FileFilter filter){
        
        if (fileOrDir == null || (fileOrDir = fileOrDir.trim()).equals("")){
            return null;
        } 
        File oriFile = new File(fileOrDir);
        ArrayList<File> files = new ArrayList<File>();
        File[] tmpFiles = null;
        File tmpFile = null;
        
        if(filter == null){
            tmpFiles = oriFile.listFiles();   
        }else{
            tmpFiles = oriFile.listFiles(filter);   
        }
        for (int i = 0; i < tmpFiles.length; i++) { 
            tmpFile = tmpFiles[i]; 
            if (tmpFile.isFile()) { 
                files.add(tmpFile);
            }else{
                files.addAll(listFiles(tmpFile.getAbsolutePath(), filter));
            }
        }//end for
        return files;
    }//end listFiles
    
    public static boolean delete(String fileOrDir){
        if (fileOrDir == null || fileOrDir.trim().equals("")){
            return true;
        } 
        File file = new File(fileOrDir);
        if(!file.exists()){
        	return true;
        }

        if (file.isFile()) {   
            return file.delete();
        }else{
            //delete files in the directory:  
            File[] files = file.listFiles(); 
            File subFile = null;
            for (int i = 0; i < files.length; i++) { 
            	subFile = files[i]; 
                if (subFile.isFile()) { 
                	subFile.delete(); 
                }else{
                    if(!delete(subFile.getAbsolutePath())){
                        return false;
                    }
                }
            }//end for
            file.delete();
        }//end if-else
        return true;
    }//deleteDirectory()

	public static Map<String, String> getFileListingInClassPath(String path,
			boolean includeSubPackage, FileFilter fileFilter) {
		return getFileListingInClassPath(null, path,
				 includeSubPackage, fileFilter);
	}

	public static Map<String, String> getFileListingInClassPath(Class clazz, String path,
			boolean includeSubPackage, FileFilter fileFilter) {
		
		logger.fine("FileUtil.getFileListingInClassPath()........begin.....");
		Map<String, String> map = new HashMap<String, String>();
		
		if (clazz == null) {
			clazz = FileUtil.class;
		}
		if(StringUtil.isEmptyOrNull(path)){
			throw new RuntimeException("FindFiles()...path parameter should not be null or empty, check code!");
		}
		URL dirURL = clazz.getClassLoader().getResource(path);
		if (dirURL == null) {
			logger.fine("FileUtil.getFileListingInClassPath()...Can not find the path in classpath, return empty map.");
			return map;
		}
		String fileSimpleName = null;
		String fileFullName = null;
		try {
			if (dirURL != null && dirURL.getProtocol().equals("file")) {
				logger.fine("FileUtil.getFileListingInClassPath()... path is a file path.....");
				/* A file path: easy enough */
				String[] fileNames = new File(dirURL.toURI()).list();
				for (String fileName : fileNames) {
					//System.out.println("fileName = " + fileName);
					fileFullName = fileName;
					int idx = fileName.lastIndexOf("/");
					if (idx != -1) {
						fileSimpleName = fileName.substring(idx + 1);
					} else {
						fileSimpleName = fileFullName;
					}
					if (fileFilter != null) {
						if (fileFilter.accept(new File(fileFullName))) {
							map.put(fileSimpleName, fileFullName);
						}
					}
					logger.fine("FileUtil.getFileListingInClassPath()... fileSimpleName = " + fileSimpleName);
				}//end for
			}else if (dirURL.getProtocol().equals("jar")) {
				logger.fine("FileUtil.getFileListingInClassPath()... path is a jar path.....");
				/* A JAR path */
				String jarPath = dirURL.getPath().substring(5,
						dirURL.getPath().indexOf("!"));
				logger.fine("FileUtil.getFileListingInClassPath()...jarPath = " + jarPath);
				JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
				Enumeration<JarEntry> entries = jar.entries();
				new HashSet<String>();
				JarEntry entry = null;
				String fname = null;
				while (entries.hasMoreElements()) {
					entry = entries.nextElement();
					fname = entry.getName();
					logger.fine("FileUtil.getFileListingInClassPath()...find entry fname = " + fname);
					// filter according to the path
					if (fname.startsWith(path)) {
						fname = fname.substring(path.length());
						fname = (fname.startsWith("/")?fname.substring(1):fname);
						
						if (!includeSubPackage && fname.indexOf("/") != -1) {
							// in sub package, do nothing
							continue;
						}
						if (!entry.isDirectory()) {
							if(path.endsWith("/")){
								fileFullName = path + fname;
							}else{
								fileFullName = path + "/" + fname;
							}
							int idx = fname.lastIndexOf("/");
							if (idx != -1) {
								fileSimpleName = fname.substring(idx + 1);
							} else {
								fileSimpleName = fname;
							}
							if (fileFilter != null) {
								if (fileFilter.accept(new File(fileFullName))) {
									map.put(fileSimpleName, fileFullName);
								}
							} else {
								map.put(fileSimpleName, fileFullName);
							}
						}
					}
				}//end while
			}else if (dirURL.getProtocol().equals("zip")) {
				logger.fine("FileUtil.getFileListingInClassPath()... path is a zip path.....");
				/* A JAR path */
				String zipPath = dirURL.getPath().substring(0,
						dirURL.getPath().indexOf("!"));
				logger.fine("FileUtil.getFileListingInClassPath()...zipPath = " + zipPath);
				ZipFile zip = new ZipFile(URLDecoder.decode(zipPath, "UTF-8"));
				Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
				Set<String> result = new HashSet<String>();
				ZipEntry entry = null;
				String fname = null;
				while (entries.hasMoreElements()) {
					entry = entries.nextElement();
					fname = entry.getName();
					logger.fine("FileUtil.getFileListingInClassPath()...find entry fname = " + fname);
					// filter according to the path
					if (fname.startsWith(path)) {
						fname = fname.substring(path.length());
						fname = (fname.startsWith("/")?fname.substring(1):fname);
						
						if (!includeSubPackage && fname.indexOf("/") != -1) {
							// in sub package, do nothing
							continue;
						}
						if (!entry.isDirectory()) {
							if(path.endsWith("/")){
								fileFullName = path + fname;
							}else{
								fileFullName = path + "/" + fname;
							}
							int idx = fname.lastIndexOf("/");
							if (idx != -1) {
								fileSimpleName = fname.substring(idx + 1);
							} else {
								fileSimpleName = fname;
							}
							if (fileFilter != null) {
								if (fileFilter.accept(new File(fileFullName))) {
									map.put(fileSimpleName, fileFullName);
								}
							} else {
								map.put(fileSimpleName, fileFullName);
							}
						}
					}
				}//end while
			}else{
				//none file or non jar/zip, do nothing
				logger.warning("FileUtil.getFileListingInClassPath()... does NOT support this Protocol - " + dirURL.getProtocol());
			}//end if-else
		} catch (Throwable t) {
			t.printStackTrace();
			throw new RuntimeException(t);
		}
		return map;
	}

	public static List<Class<?>> getClassesInPackage(String pkgName) {
		if(StringUtil.isEmptyOrNull(pkgName)){
			return null;
		}
	    List<Class<?>> classes = new ArrayList<Class<?>>();
	    // Get a File object for the package
	    File directory = null;
	    String fullPath = null;
	    String resourcePath = pkgName.replace('.', '/');
	    String packagePath = "/" + resourcePath;
	    URL resource = Thread.currentThread().getContextClassLoader().getResource(packagePath);
	     	
	    if (resource == null) {
	        throw new RuntimeException("No resource found for " + packagePath);
	    }
	    fullPath = resource.getFile();
	    logger.info("Full Directory/Resource Path: " + fullPath);
	    try {
	        directory = new File(resource.toURI());
	    } catch (URISyntaxException e) {
	        throw new RuntimeException(pkgName + " (" + resource + ") does not appear to be a valid URL / URI.  Strange, since we got it from the system...", e);
	    } catch (IllegalArgumentException e) {
	        directory = null;
	    }
	    if (directory != null && directory.exists()) {
	        // Get the list of the files contained in the package
	        String[] files = directory.list();
	        for (int i = 0; i < files.length; i++) {
	            // we are only interested in .class files
	            if (files[i].endsWith(".class")) {
	                // removes the .class extension
	                String className = pkgName + '.' + files[i].substring(0, files[i].length() - 6);
	                try {
	                    classes.add(Class.forName(className));
	                }catch (Exception e) {
	                    throw CommonUtil.getRuntimeException(e);
	                }
	            }
	        }
	    }else if(fullPath.indexOf(".jar!") >= 0){
	    	// resource is a jar file
	    	String jarPath = null;
	        try {
	            jarPath = fullPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
	            JarFile jarFile = new JarFile(jarPath);         
	            Enumeration<JarEntry> entries = jarFile.entries();
	            while(entries.hasMoreElements()) {
	                JarEntry entry = entries.nextElement();
	                String entryName = entry.getName();
	                if(entryName.startsWith(resourcePath) && entryName.length() > (resourcePath.length() + "/".length()) && entryName.endsWith(".class")) {
	                    String className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
	                    try {
	                        classes.add(Class.forName(className));
	                    } 
	                    catch (Exception e) {
	                        throw CommonUtil.getRuntimeException(e);
	                    }
	                }
	            }
	        } catch (Exception e) {
	            throw new RuntimeException(pkgName + " (" + jarPath + ") does not appear to be a valid package", e);
	        }
	    }else{
            throw new RuntimeException("getClassesInPackage() - only support local directory or jar files. Code need to be enhance to deal with this kind of path: " + fullPath);
	    }
	    logger.info("Find Classes in the package <" + pkgName + ">: \n" + classes);
	    return classes;
	}
	
	public static String getFileContentAsString(String fileName) {
		if (StringUtil.isEmptyOrNull(fileName)) {
			return null;
		}
		StringBuilder contents = new StringBuilder();
		BufferedReader input = null;
		try {
			InputStreamReader inReader = null;
	        File inputFile = new File(fileName);
	        if(!inputFile.exists() || !inputFile.isFile()){
	        	if(!fileName.startsWith("/")) {
	        		fileName = "/" + fileName;
	        	}
	        	inReader = new InputStreamReader(
						FileUtil.class.getResourceAsStream("/" + fileName));
	        }else {
	        	inReader = new InputStreamReader(new FileInputStream(inputFile));
	        }
			input = new BufferedReader(inReader);
			String line = null; // not declared within while loop
			while ((line = input.readLine()) != null) {
				contents.append(line);
				contents.append("\n");
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception e) {
					e.printStackTrace();
				    logger.severe(CommonUtil.getStackTrace(e));
				}
			}
		}

		return contents.toString();
	}
   
    public static void main(String[] args) {
        try{
            //FileUtil.rename("E:\\tmp3\\test2.xls", "E:\\test3.xls");
            //FileUtil.move("E:\\tmp2", "E:\\tmp1");
            
            //FileUtil.copy("E:\\tmp1\\copy2.txt", "E:\\tmp1\\copy1.txt");
            ExtFileFilter filter = new ExtFileFilter();
            //filter.addAcceptedFileExtension(".xls");
            FileUtil.copy("E:\\tmp1", "E:\\tmp3", filter);
            
            //filter.addAcceptedFileExtension(".xls");
            List list = FileUtil.list("E:\\tmp3", filter);
            System.out.println("List file result = \n" + list);
            list = FileUtil.list("E:\\tmp3", null);
            System.out.println("List file result = \n" + list);
            
            
    		ExtFileFilter fileFilter = new ExtFileFilter();
    		fileFilter.addAcceptedFileExtension(".xsd");
    		Map<String, String> map = getFileListingInClassPath("wsdls", true, fileFilter);

    		
        }catch(Exception e){
            e.printStackTrace();
        }finally{
        }
    }//end main()
}//end class definition
