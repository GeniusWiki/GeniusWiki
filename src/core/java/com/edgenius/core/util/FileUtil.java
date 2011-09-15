/* 
 * =============================================================
 * Copyright (C) 2007-2011 Edgenius (http://www.edgenius.com)
 * =============================================================
 * License Information: http://www.edgenius.com/licensing/edgenius/2.0/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * http://www.gnu.org/licenses/gpl.txt
 *  
 * ****************************************************************
 */
package com.edgenius.core.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.NumberFormat;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import com.edgenius.core.Installation;

/**
 * General File Utilities
 */
public class FileUtil {

	
	static{
		Installation.refreshInstallation();
	}
	public static final String TEMP_DIR = getFullPath(System.getProperty("java.io.tmpdir"),"geniuswiki", Installation.INSTANCE_ID);

	public static boolean isEmptyDirectory(String directoryName) throws FileUtilException {
		
		if(directoryName == null || directoryName.length() == 0)
			throw new FileUtilException("A directory name must be specified");
		
		File dir = new File(directoryName);
		if(dir.exists()) {
			if(dir.listFiles().length > 0)
				return false;
			else 
				return true;
		}
		
		return true;
	}

	public static File createTempFile(String suffix) throws FileUtilException{
		String tempSysDirName = TEMP_DIR;
		if ( tempSysDirName == null )
			throw new FileUtilException("No temporary directory known to the server. [System.getProperty( \"java.io.tmpdir\" ) returns null. ]\n Cannot create file.");

		return new File(tempSysDirName+File.separator + System.currentTimeMillis()+ suffix);
		
	}
	
	
	/**
	 * Create a temporary directory with the name in the form
	 * geniuswiki_timestamp_suffix inside the default temporary-file directory 
	 * for the system.
	 */
	public static String createTempDirectory(String suffix) throws FileUtilException {
	    
		String tempSysDirName = TEMP_DIR;
		if ( tempSysDirName == null )
			throw new FileUtilException("No temporary directory known to the server. [System.getProperty( \"java.io.tmpdir\" ) returns null. ]\n Cannot upload package.");
	
		String tempDirName = getFullPath(tempSysDirName, "temp", System.currentTimeMillis()+""+suffix);
		
		// try 100 slightly different variations. If I can't find a unique
		// one in ten tries, then give up.
		File tempDir = new File(tempDirName);
		int i = 0;
		while ( tempDir.exists() && i < 100 ) {
			tempDirName = getFullPath(tempSysDirName, "temp", System.currentTimeMillis()+"_"+i+suffix);
			tempDir = new File(tempDirName);
		}
		if ( tempDir.exists() )
			throw new FileUtilException("Unable to create temporary directory. The temporary filename/directory that we would use to extract files already exists: "
					+tempDirName);
		
		tempDir.mkdirs();
		return tempDirName;
	}
	
	public static boolean createDirectory(String directoryName) throws FileUtilException
	{
		boolean isCreated = false;
		//check directoryName to see if its empty or null
		if (directoryName == null || directoryName.length() == 0)
			throw new FileUtilException("A directory name must be specified");
	
		File dir = new File(directoryName);
		isCreated = dir.exists() ? false : dir.mkdirs();
		
		return isCreated;
	}
	public static boolean directoryExist(String directoryToCheck)
	{
		File dir = new File(directoryToCheck);
		return dir.exists();
	}
	
	/**
	 * get file name from a string which may include directory information.
	 * For example : "c:\\dir\\ndp\\pp.txt"; will return pp.txt.?
	 * If file has no path infomation, then just return input fileName.
	 * 
	 */
	public static String getFileName(String fileName){
		if(fileName == null)
			return "";
			
		fileName = fileName.trim();

		int dotPos = fileName.lastIndexOf("/");
		int dotPos2 = fileName.lastIndexOf("\\");
		dotPos = Math.max(dotPos,dotPos2);
		if (dotPos == -1){
			return fileName;
		}
		return fileName.substring(dotPos + 1, fileName.length());
		
	}	
	/** 
	 * Get file directory info.
	 * @param fileName with path info.
	 * @return return only path info with the given fileName
	 */
	public static String getFileDirectory(String fileName){
		if(fileName == null)
			return "";
		
		fileName = fileName.trim();

		int dotPos = fileName.lastIndexOf("/");
		int dotPos2 = fileName.lastIndexOf("\\");
		dotPos = Math.max(dotPos,dotPos2);
		if (dotPos == -1){
			return "";
		}
		//return the last char is '/'
		return fileName.substring(0,dotPos+1);
	
	}
	/**
	 * Merge two input parameter into full path and adjust File.separator to 
	 * OS default separator as well.
	 * 
	 * @param path 
	 * @param file could be file name,or sub directory path.
	 * @return
	 */
	public static String getFullPath(String ... path ){
		if(path == null || path.length == 0)
			return "";
			
		if(path.length == 1)
			return makeCanonicalPath(path[0]);
		
		StringBuffer fullpath = new StringBuffer(path[0]);
		
		for(int idx=1;idx<path.length;idx++){
			if(StringUtils.isBlank(path[idx]))
				continue;
			
			if(path[idx].endsWith(File.separator))
				fullpath.append(path[idx]);
			else
				fullpath.append(File.separator).append(path[idx]);
			
		}
		
		return makeCanonicalPath(fullpath.toString());
		
	}
	
	public static String makeCanonicalPath(String pathfile){
		if(File.separator.indexOf("\\") != -1){
			pathfile = pathfile.replaceAll("\\/","\\\\");
			pathfile = pathfile.replaceAll("\\\\+","\\\\");
		}else{
			pathfile = pathfile.replaceAll("\\\\",File.separator);
			pathfile = pathfile.replaceAll("\\/+",File.separator);
		}
		return pathfile;
	}
	
  public static void copyFile(File in, File out) throws Exception {
	     FileChannel sourceChannel = new FileInputStream(in).getChannel();
	     FileChannel destinationChannel = new FileOutputStream(out).getChannel();
	     sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
	     sourceChannel.close();
	     destinationChannel.close();
   }
	/**
	 * get file extension name from a String, such as from "textabc.doc", return "doc"
	 * fileName also can contain directory information.
	 */
	public static String getFileExtension(String fileName) {
		if(fileName == null)
			return "";
			
		fileName = fileName.trim();
		int dotPos = fileName.lastIndexOf(".");
		if (dotPos == -1)
			return "";
		return fileName.substring(dotPos + 1, fileName.length());
	}
	
	/**
	 * Check whether file is executable according to its extenstion and executable extension name list from Edgenius configuaration.
	 * @param filename
	 * @return
	 */
//	  public static boolean isExecutableFile(String filename){
//		  String extname = FileUtil.getFileExtension(filename);
//		  log.debug("Check executable file for extension name " + extname);
//		  
//		  if(StringUtils.isBlank(extname))
//			  return false;
//		  extname = "." + extname;
//		  
//		  String exeListStr = Global.ExecuteFileExt;
//		  String[] extList = StringUtils.split(exeListStr, ',');
//		  boolean executable = false;
//		  for (String ext : extList) {
//			if(StringUtils.equalsIgnoreCase(ext, extname)){
//				executable = true;
//				break;
//			}
//		}
//		  
//		  return executable;
//	  }
	  
	  public static String convertHumanSize(long byteSize){
		  String unit = "";
		  float size = (float) byteSize;
		  if(size > 1024){
			  size = size/1024;
			  unit = "K";
		  }
		  if(size > 1024){
			  size = size/1024;
			  unit = "M";
		  }
		  if(size > 1024){
			  size = size/1024;
			  unit = "G";
		  }
		  
		  NumberFormat format = NumberFormat.getNumberInstance();
		  format.setMaximumFractionDigits(1);
		  String ret = format.format(size);
		  
		  return ret + unit;
	  }

	/**
	 * @param oldFileNodeDir
	 * @param newFileNodeDir
	 * @return 
	 */
	public static boolean rename(String oldFileNodeDir, String newFileNodeDir) {
		File oldF = new File(oldFileNodeDir);
		File newF = new File(newFileNodeDir);
		return oldF.renameTo(newF);
		
	}
	/**
	 * Wrapper of Spring DefaultResourceLoader. 
	 * The location must start with "classpath:" or "file://", otherwise, this method
	 * may throw file not found exception.
	 * Important: The file must be in file system. This means it won't work if it is in jar or zip file etc.
	 * Please getFileInputStream() to get input stream directly if they are in jar or zip
	 *
	 * @param location
	 * @return
	 * @throws IOException 
	 */
	public static File getFile(String location) throws IOException{
		DefaultResourceLoader loader = new DefaultResourceLoader();
		Resource res = loader.getResource(location);
		try {
			return res.getFile();
		} catch (IOException e) {
			throw(e);
		}
	}
	/**
	 * 
	 * @param location
	 * @return
	 * @throws IOException 
	 */
	public static InputStream getFileInputStream(String location) throws IOException {
		//Don't user DefaultResourceLoader directly, as test it try to find host "c" if using method resource.getInputStream()
		// while location is "file://c:/var/test" etc.
		
		DefaultResourceLoader loader = new DefaultResourceLoader();
		Resource res = loader.getResource(location);
		if(ResourceUtils.isJarURL(res.getURL())){
			//it is in jar file, we just assume it won't be changed in runtime, so below method is safe.
			try {
				return res.getInputStream();
			} catch (IOException e) {
				throw(e);
			}
		}else{
			//in Tomcat, the classLoader cache the input stream even using thread scope classloader, but it is still failed
			//if the reload in same thread. For example, DataRoot class save and reload in same thread when install.
			//So, we assume if the file is not inside jar file, we will always reload the file into a new InputStream from file system.
			
			//if it is not jar resource, then try to refresh the input stream by file system
			return new FileInputStream(res.getFile());
		}
	}
	
	/**
	 * Get a file output stream in file system. 
	 * @param location
	 * @return
	 * @throws IOException
	 */
	public static FileOutputStream getFileOutputStream(String location) throws IOException {
		if(location.startsWith("file://") || location.startsWith("classpath:"))
			return new FileOutputStream(getFile(location));
		
		return new FileOutputStream(location); 
		
	}

	/**
	 * @param location
	 * @return
	 * @throws IOException 
	 */
	public static Properties loadProperties(String location) throws IOException {
		InputStream is = null;
		try {
			//!!!don't user Spring PropertiesLoaderUtils, as it can not handle "file://c:/var/test" format 
			is = getFileInputStream(location);
			Properties prop = new Properties();
			prop.load(is);
			return prop;
		} catch (IOException e) {
			throw(e);
		} finally{
			if(is != null)
				try {
					is.close();
				} catch (Exception e) {
					//nothing
				}
		}
	}

	/**
	 * IMPORTANT: this location must be Spring resource format, such as, file://c:/abc.txt
	 * or classpath:abc.properties etc.  If there is not protocol prefix, this method may not 
	 * return correct value (always false).
	 * @param location
	 * @return
	 */
	public static boolean exist(String location) {
		DefaultResourceLoader loader = new DefaultResourceLoader();
		Resource res = loader.getResource(location);
		return res.exists();
	}

	/**
	 * Delete all files and sub-directories and given directory itself.
	 * @param dir
	 * @throws IOException 
	 * @throws IOException
	 */
	public static void deleteDir(String dir) throws IOException{
		try {
			FileUtils.cleanDirectory(new File(dir));
			if(!new File(dir).delete())
				new File(dir).deleteOnExit();
		} catch (Exception e) {
			throw new IOException("Delete directory failed in " + e.toString());
		}
	}


}
