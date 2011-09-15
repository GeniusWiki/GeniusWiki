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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles zip files - expands them to a temporary directory, and 
 * deletes them on request.
 * 
 */
public class ZipFileUtil {

	private static Logger log = LoggerFactory.getLogger(ZipFileUtil.class);
	protected static final String prefix = "geniuswikizip_"; // protected rather than private to suit junit test
	private static int BUFFER_SIZE = 8192;

	public static void expandZipToFolder(InputStream is, String destFolder) throws ZipFileUtilException {
		// got our directory, so write out the input file and expand the zip file
		// this is really a hack - write it out temporarily then read it back in again! urg!!!!
		ZipInputStream zis = new ZipInputStream(is);
	    int count;
	    byte data[] = new byte[ BUFFER_SIZE ];
		ZipEntry entry = null; 
		BufferedOutputStream dest = null;
		String entryName = null;
		
		try {
			
	 		// work through each file, creating a node for each file
	        while( ( entry = zis.getNextEntry() ) != null ) {
	        	entryName = entry.getName();
				if( !entry.isDirectory()){
					
					String destName = destFolder + File.separator + entryName;
					//It must sort out the directory information to current OS. 
					//e.g, if zip file is zipped under windows, but unzip in linux.  
					destName = FileUtil.makeCanonicalPath(destName);
					
					prepareDirectory(destName);
					
			        FileOutputStream fos = new FileOutputStream( destName );
			        dest = new BufferedOutputStream( fos, BUFFER_SIZE );
			        while( (count = zis.read( data, 0, BUFFER_SIZE ) ) != -1 )
			        {
			        	dest.write( data, 0, count );
			        }
		            dest.flush();
		            IOUtils.closeQuietly(dest);
		            dest = null;
			    }else{
					String destName = destFolder + File.separator + entryName;
					destName = FileUtil.makeCanonicalPath(destName);
			    	new File(destName).mkdirs();
			    }
		
	        }
			
		} catch ( IOException ioe ) {
			
			log.error("Exception occured processing entries in zip file. Entry was "+entryName,ioe);
			throw new ZipFileUtilException("Exception occured processing entries in zip file. Entry was "+entryName,ioe);
			
		} finally {
			IOUtils.closeQuietly(dest);
			IOUtils.closeQuietly(zis);
		}
	}



	/**
	 * @param destName
	 */
	private static void prepareDirectory(String destName) {
	
		File destNameFile = new File(destName);
		String path = destNameFile.getParent();
		if(path != null){
			File pathDir = new File(path);
			pathDir.mkdirs();
		}
	}

  
    /**
     * Creates a ZIP file and places it in the current working directory. The zip file is compressed
     * at the default compression level of the Deflater.
     * 
     * @param listToZip. Key is file or directory, value is parent directory which will remove from given file/directory because 
     * compression only save relative directory.  For example, c:\geniuswiki\data\repository\somefile, if value is c:\geniuswiki, then only 
     * \data\repository\somefile will be saved.  It is very important, the value must be canonical path, ie, c:\my document\geniuswiki, 
     * CANNOT like this "c:\my doc~1\" 
     * 
     * 
     */
    public static void createZipFile(String zipFileName, Map<File,String> listToZip, boolean withEmptyDir) throws ZipFileUtilException    {
    	ZipOutputStream zop = null;
		try{
			zop = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFileName)));
			zop.setMethod(ZipOutputStream.DEFLATED);
			zop.setLevel(Deflater.DEFAULT_COMPRESSION);

        	for (Entry<File,String> entry: listToZip.entrySet()) {
        		File file = entry.getKey();
        		if(!file.exists()){
        			log.warn("Unable to find file " + file + " to zip");
        			continue;
        		}
        		if(file.isDirectory()){
        			Collection<File> list = FileUtils.listFiles(file, null, true);
        			for (File src : list) {
        				addEntry(zop,src,createRelativeDir(src.getCanonicalPath(), entry.getValue()));
        			}
        			if(withEmptyDir){
        				final List<File> emptyDirs = new ArrayList<File>();
        				if(file.list().length == 0){
        					emptyDirs.add(file);
        				}else{
        					//I just don't know how quickly to find out all empty sub directories recursively. so use below hack:
        					FileUtils.listFiles(file, FileFilterUtils.falseFileFilter(),new IOFileFilter(){
        						//JDK1.6 @Override
        						public boolean accept(File f) {
        							if(!f.isDirectory())
        								return false;
        							
        							int size = f.listFiles().length;
        							if(size == 0){
        								emptyDirs.add(f);
        							}
        							return true;
        						}
        						
        						//JDK1.6 @Override
        						public boolean accept(File arg0, String arg1) {
        							return true;
        						}
        					});
        				}
        				for (File src : emptyDirs) {
            				addEntry(zop,null,createRelativeDir(src.getCanonicalPath(), entry.getValue()));
            			}
        			}
        		}else{
        			addEntry(zop,file,createRelativeDir(file.getCanonicalPath(),entry.getValue()));
        		}
        		
			}
		}
		catch (IOException e1){
			throw new ZipFileUtilException("An error has occurred while trying to zip the files. Error message is: ", e1);
		}finally{
			try {
				if(zop != null) zop.close(); 
			} catch (Exception e) {
			}
		}
        
    }
    /**
	 * @param canonicalPath
	 * @param value
	 * @return
	 */
	private static String createRelativeDir(String canonicalPath, String parentPath) {
		if(parentPath == null)
			return canonicalPath;
		
		int len = parentPath.length();
		if(!parentPath.endsWith("\\") && !parentPath.endsWith("/"))
			len++;
		return canonicalPath.substring(len);
	}

	private static void addEntry(ZipOutputStream zop, File entry, String entryName) throws IOException{
		if(StringUtils.isBlank(entryName))
			return;
		
		BufferedInputStream source = null;
		if(entry != null){
			source = new BufferedInputStream(new FileInputStream(entry));
		}else{
			//to make this as directory
			if(!entryName.endsWith("/"))
				entryName += "/";
		}
    	ZipEntry zipEntry = new ZipEntry(entryName);
		zop.putNextEntry(zipEntry);
		
		if(entry != null){
			//transfer bytes from file to ZIP file
			byte[] data = new byte[ BUFFER_SIZE ];
			int length;
			while((length = source.read(data)) > 0){
				zop.write(data, 0, length);
			}
	    	IOUtils.closeQuietly(source);
		}
		zop.closeEntry();
		
    }
    
}

