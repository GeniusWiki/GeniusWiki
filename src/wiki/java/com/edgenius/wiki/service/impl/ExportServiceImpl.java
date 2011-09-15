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
package com.edgenius.wiki.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.cxf.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.XMLResource;

import com.edgenius.core.Constants;
import com.edgenius.core.Global;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.ITicket;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.core.util.DateUtil;
import com.edgenius.core.util.FileUtil;
import com.edgenius.core.util.ZipFileUtil;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.model.History;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.plugin.PluginService;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.service.ExportException;
import com.edgenius.wiki.service.ExportService;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.util.WikiUtil;
import com.lowagie.text.DocumentException;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author Dapeng.Ni
 */
public class ExportServiceImpl implements ExportService, InitializingBean{
	private static final Logger log = LoggerFactory.getLogger(ExportServiceImpl.class);

	private static final String ExportPageHTMLTemplateName = "exportpage.ftl";
	private static final String ExportTemplPre = "export-pre.ftl";
	private static final String ExportTemplPost = "export-post.ftl";
	
	private static final String TMP_EXPORT  = "_export";
	private static final String TMP_EXPORT_TARGET = "_export_target";
	
	private static final String EXPORT_ZIP = "export";
	//can not user page title or spaceUname as invalid characters may exist
	private static final String EXPORT_HTML_FILENAME  = "export.html";
	private static final String EXPORT_PDF_FILENAME = "export.pdf";

	private static final int EXPORT_HTML = 0;
	private static final int EXPORT_PDF = 1;


	private PageService pageService;
	private RenderService renderService;
	private MessageService messageService;
	private FreeMarkerConfigurer templateEngine;
	private RepositoryService repositoryService;  
	private PluginService pluginService;
	
	//this is target zip file directory
	private String targetDir;
	//********************************************************************
	// Method
	//********************************************************************

	//JDK1.6 @Override
	public File exportHTML(String spaceUname, String pageTitle) throws ExportException {

		return export(spaceUname, pageTitle, EXPORT_HTML);
	}

	//JDK1.6 @Override
	public File exportPDF(String spaceUname, String pageTitle) throws ExportException {
		return export(spaceUname, pageTitle, EXPORT_PDF);
	}
	
	//JDK1.6 @Override
	public String exportPageHTML(String target, AbstractPage page){
		
		String creator;
		if(page.getCreator() == null){
			creator = messageService.getMessage(Constants.I18N_ANONYMOUS_USER);
		}else
			creator = page.getCreator().getFullname();
		
		String modifier;
		if(page.getModifier() == null){
			modifier = messageService.getMessage(Constants.I18N_ANONYMOUS_USER);
		}else
			modifier = page.getModifier().getFullname();
		
		
		//covert:page modifiedDate is possible from database directly read out, which is is java.sql.TimeStamp type
		
		User currentUser = WikiUtil.getUser();
		String modifiedDate = DateUtil.toDisplayDate(currentUser, page.getModifiedDate(),messageService);
		String createDate = DateUtil.toDisplayDate(currentUser, page.getCreatedDate(),messageService);
		//Create by {0} on {1} - Last updated by {2} on {3}
		page.setAuthorInfo(messageService.getMessage("print.author.info", new String[]{creator,createDate,modifier,modifiedDate}));
	
		//page did not render yet!!!
		List<RenderPiece> pieces = renderService.renderHTML(target, page);
		
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("text", renderService.renderNativeHTML(page.getSpace() == null? null: page.getSpace().getUnixName(), page.getPageUuid(), pieces));
		map.put("page", page);
		map.put("isHistory", page instanceof History);
		try {
			templateEngine.getConfiguration().setLocale(Global.getDefaultLocale());
			Template t = templateEngine.getConfiguration().getTemplate(ExportPageHTMLTemplateName);
			
			return FreeMarkerTemplateUtils.processTemplateIntoString(t, map);
		} catch (TemplateException e) {
			log.error("Error while processing FreeMarker template ", e);
		} catch (FileNotFoundException e) {
			log.error("Error while open template file ", e);
		} catch (IOException e) {
			log.error("Error while generate Export Content ", e);
		}
		
		//failed
		return null;
		
	}
	//JDK1.6 @Override
	public FileNode getExportFileNode(String filename) {
		try {
			FileNode node = new FileNode();
			node.setFilename(EXPORT_ZIP+".zip");
			node.setContentType("application/x-zip-compressed");
			node.setIdentifier(FileUtil.getFullPath(targetDir,filename));
			node.setFile(new FileInputStream(node.getIdentifier()));
			return node;
		} catch (FileNotFoundException e) {
			log.error("Unable to get export file " + filename,e);
		}
		
		return null;
	}

	//JDK1.6 @Override
	public void afterPropertiesSet() throws Exception {
		//TODO: this will leave a lots of temporary directories
		//create a temporary directory for final target zip file
		targetDir = FileUtil.createTempDirectory(TMP_EXPORT_TARGET);
		File dir = new File(targetDir);
		if(dir.exists() && !dir.isDirectory())
			throw new BeanInitializationException("Unable to create temporary directory for export use:" + targetDir);
		
		dir.mkdirs();
	}
	//********************************************************************
	//               set / get
	//********************************************************************

	/**
	 * @param spaceUname
	 * @param pageTitle
	 * @return
	 * @throws ExportException
	 */
	private File export(String spaceUname, String pageTitle, int type) throws ExportException {
		FileOutputStream fos = null;
		String dir = null;
		try {
			dir = FileUtil.createTempDirectory(TMP_EXPORT);
			String cannoicalDir = new File(dir).getCanonicalPath();
			
			File subdir = new File(FileUtil.getFullPath(dir,ExportService.EXPORT_HTML_SUB_DIR));
			subdir.mkdir();
			File html = new File(FileUtil.getFullPath(dir,EXPORT_HTML_FILENAME));
			
			fos = new FileOutputStream(html,true);
			Map<File, String> list = new HashMap<File, String>();
			User viewer = WikiUtil.getUser();
			
			List<Page> pages = null;
			if(StringUtils.isEmpty(pageTitle)){
				//export all pages in space
				pages = pageService.getPagesInSpace(spaceUname,null,0,viewer);
			}else{
				//export single page
				Page page = pageService.getCurrentPageByTitle(spaceUname, pageTitle);
				if(page != null){
					pages = new ArrayList<Page>();
					pages.add(page);
				}
			}
			
			String skinPath = Global.Skin;
			
			//add first part of whole exported HTML
			Template tmpl = templateEngine.getConfiguration().getTemplate(ExportTemplPre);
			Map<String,String> preMap = new HashMap<String,String>();
			preMap.put("skinpath",skinPath);
			fos.write(FreeMarkerTemplateUtils.processTemplateIntoString(tmpl, preMap).getBytes());
			
			if(pages != null){
				int count = pages.size();
				for (Page page : pages) {

					String content = exportPageHTML(RenderContext.RENDER_TARGET_EXPORT,page);
					if(content != null){
						//save this page attachment - try to save by original name, if failed, try to append -x on file name
						fos.write("<div class=\"print-page\">\n".getBytes());
						//anchor
						fos.write(new StringBuffer("<a href='#' name='")
								.append(StringEscapeUtils.escapeHtml(spaceUname))
								.append("_").append(StringEscapeUtils.escapeHtml(page.getTitle()))
								.append("'></a>").toString().getBytes());

						fos.write(content.getBytes());
						fos.write("\n</div>".getBytes());
						
					
						//download all latest version node - 
						//TODO:is necessary? or only image files? but if there are {attachment} macro? so at moment, just download all
						ITicket ticket = repositoryService.login(spaceUname, spaceUname, spaceUname);
						List<FileNode> nodes = repositoryService.getAllIdentifierNodes(ticket, RepositoryService.TYPE_ATTACHMENT, page.getPageUuid(), false);
						if(nodes != null && nodes.size() > 0){
							
							Set<String> nodeUuids = new HashSet<String>();
							File pdir = new File(subdir,page.getPageUuid());
							if(pdir.mkdir()){
								for (FileNode fileNode : nodes) {
									//skip same nodeUuid -- only download once(latest) file
									if(nodeUuids.contains(fileNode.getNodeUuid()))
										continue;
									nodeUuids.add(fileNode.getNodeUuid());
									
									
									//download to _export/export_files/pageUid/xxx
									FileOutputStream out =null;
									InputStream in = null;
									try {
										File att = new File(pdir,fileNode.getFilename());
										out = new FileOutputStream(att);
										FileNode down = repositoryService.downloadFile(ticket, fileNode.getNodeUuid(), null, null);
										in = down.getFile();
										int len=0;
										byte[] ch = new byte[10240];
										while ((len = in.read(ch)) != -1) {
											out.write(ch,0,len);
										}
										if(type == EXPORT_HTML){
											//put attachment into zip list - pdf does not need zip attachment 
											list.put(att, cannoicalDir);
										}
									} catch (Exception e) {
										log.error("Write attachment export file failed",e);
									}finally{
										if(in != null){
											try {
												in.close();
											} catch (Exception e2) {}
										}
										if(out != null){
											try {
												out.close();
											} catch (Exception e2) {}
										}
									}
								}
							}else{
								AuditLogger.error("Export page attachment failed - unable make sub dir " + (page != null?page.getPageUuid():"") + " on space " + spaceUname);
							}
						}
						
						if(count > 1){
							//print page separator for multiple pages export scenario
							fos.write("<div class=\"print-page-separator\"></div>".getBytes());
						}
					}else{
						AuditLogger.error("Export page failed: " + (page != null?page.getTitle():"") + " on space " + spaceUname);
					}
				}
				
				//export static resource for style sheet or images etc.
				exportResources(cannoicalDir, subdir, list, skinPath);
			}
			
			//append last part of whole exported HTML
			StringWriter post = new StringWriter();
			tmpl = templateEngine.getConfiguration().getTemplate(ExportTemplPost);
			tmpl.dump(post);
			fos.write(post.toString().getBytes());
			if(type == EXPORT_HTML){
				//put exported html in zip list
				list.put(html, cannoicalDir);
			}else{
				//convert HMTL to PDF and zip
				File pdf = convertHTMLtoPDF(dir, html);
				list.put(pdf, cannoicalDir);
			}
			//for final zip file
			String target = "export.zip";
			//failure tolerance
			int retry =0;
			do{
				target = FileUtil.getFullPath(targetDir,EXPORT_ZIP+"-"+RandomStringUtils.randomAlphanumeric(10)+".zip");
				retry++;
			}while(new File(target).exists() && retry < 50);
				
			ZipFileUtil.createZipFile(target, list ,false);
			
			return new File(target);
		} catch (Exception e) {
			log.error("Failed export " + spaceUname + " on " + pageTitle, e);
			throw new ExportException(e);
		} finally{
			if(fos != null){
				try {
					fos.close();
				} catch (IOException e) {}
			}
			if(dir != null){
				try {
					FileUtil.deleteDir(dir);
				} catch (IOException e) {
					log.error("Unable to delete directory " + dir);
				}
			}
		}
	}

	/**
	 * @param cannoicalDir
	 * @param subdir
	 * @param list
	 * @param skinPath
	 */
	private void exportResources(String cannoicalDir, File subdir, Map<File, String> list, String skinPath) {
		try {
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			// Styles
			File styleDir = new File(FileUtil.getFullPath(subdir.getPath(),"skins",skinPath,"styles"));
			if(!styleDir.exists()) styleDir.mkdirs();
			//export render.css and print.css to export_files directory
			String path = FileUtil.getFullPath(Global.ServerInstallRealPath,"skins", 
					skinPath,"/styles/render.css");
			FileUtils.copyFileToDirectory(new File(path), styleDir);
			
			path = FileUtil.getFullPath(Global.ServerInstallRealPath,"skins", 
					skinPath,"/styles/print.css");
			FileUtils.copyFileToDirectory(new File(path), styleDir);
			
			list.put(styleDir, cannoicalDir);
			
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			// render resources, most for images etc. 
			File renderResourceSrc =  new File(FileUtil.getFullPath(Global.ServerInstallRealPath, "skins",skinPath,"render"));
			File renderResourceTgt = new File(FileUtil.getFullPath(subdir.getPath(),"skins",skinPath,"render"));
			if(!renderResourceTgt.exists()) renderResourceTgt.mkdirs();
			
			FileUtils.copyDirectory(renderResourceSrc, renderResourceTgt);
			list.put(renderResourceTgt, cannoicalDir);
			
			
			Map<File, String> pluginResources = pluginService.getPluginResourceForExport();
			if(pluginResources != null && pluginResources.size() > 0){
				//We don't directory zip the file, as we need to add them under "export_files/" directory
				//so we copy them first, then zip
				for (Entry<File, String> entry : pluginResources.entrySet()) {
					File src = entry.getKey();
					File tgt = new File(subdir.getPath(),entry.getValue());
					if(!tgt.exists()) tgt.mkdirs();
					
					if(src.isDirectory()){
						FileUtils.copyDirectory(src, tgt);
					}else{
						FileUtils.copyFileToDirectory(src, tgt);
					}
					list.put(tgt, cannoicalDir);
				}
			}
		} catch (Exception e) {
			AuditLogger.error("Copy render resources failed for export with server install path:" + Global.ServerInstallRealPath,e);
		}
	}
	
	/**
	 * @param html
	 * @return
	 * @throws FileNotFoundException 
	 * @throws DocumentException 
	 */
	private File convertHTMLtoPDF(String dir, File html) throws FileNotFoundException, DocumentException {
		OutputStream os = null;
		FileInputStream is = null;
		File pdf = new File(FileUtil.getFullPath(dir,EXPORT_PDF_FILENAME));
		
        try {
        	//TODO: also need test com.lowagie.text.Document package
//			Document doc = new Document();
//			PdfWriter.getInstance(doc, new FileOutputStream(pdf));
//			doc.open();
//
//			
//			StringBuffer body = removeInvalidTags(FileUtils.readFileToString(html));
//			
//			StyleSheet st = new StyleSheet();
//			java.util.ArrayList htmlElements = HTMLWorker.parseToList(new StringReader(
//					body.toString()), st);
//			
//			for (int k = 0; k < htmlElements.size(); ++k) {
//				doc.add((Element) htmlElements.get(k));
//			}
//			
//			doc.close();
        	ByteArrayOutputStream bos = new ByteArrayOutputStream();
        	Tidy tidy = new Tidy();
        	tidy.setTidyMark(false);
        	tidy.setDocType("auto");
        	tidy.setWrapScriptlets(true);
        	tidy.setXHTML(true);
        	tidy.setEncloseText(true);
        	tidy.setXmlOut(true);
        	
        	is = new FileInputStream(html);
        	tidy.parse(is,bos);

        	os = new FileOutputStream(pdf);
            ITextRenderer renderer = new ITextRenderer();

            Document doc = XMLResource.load(new ByteArrayInputStream(bos.toByteArray())).getDocument();

            renderer.setDocument(doc, "http://www.geniuswiki.com");
            renderer.layout();
            renderer.createPDF(os);
        } finally {
            if (os != null) {
                try {
                    os.close();
                    os = null;
                } catch (IOException e) {
                    // ignore
                }
            }
            if (is != null) {
            	try {
            		is.close();
            		is = null;
            	} catch (IOException e) {
            		// ignore
            	}
            }
        }
		return pdf;
	}

	//********************************************************************
	//               set / get
	//********************************************************************
	public void setPageService(PageService pageService) {
		this.pageService = pageService;
	}

	public void setRenderService(RenderService renderService) {
		this.renderService = renderService;
	}

	public void setTemplateEngine(FreeMarkerConfigurer templateEngine) {
		this.templateEngine = templateEngine;
	}

	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	public void setPluginService(PluginService pluginService) {
		this.pluginService = pluginService;
	}


}
