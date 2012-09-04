package com.edgenius.wiki.webapp.action;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.edgenius.core.Constants;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.util.DateUtil;
import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.util.WikiUtil;
import com.google.gson.Gson;

//********************************************************************
//               Private class
//********************************************************************
//This class is only for mapping Page attachment object to JSON for JQuery upload format.
public class FileObject {

    public String name;
    public String author;
    public String date;
    public long size;
    public String error = "";
    public String url = "";
    public String delete_url = "";
    
    public static String toAttachmentsJson(List<FileNode> files, String spaceUname, MessageService messageService) throws UnsupportedEncodingException {
        // convert fileNode to json that for JS template in upload.jsp.
        Gson gson = new Gson();
        List<FileObject> items = new ArrayList<FileObject>();
        for (FileNode fileNode : files) {
            items.add(FileObject.fromNode(fileNode, spaceUname, messageService));
        }
        return gson.toJson(items);
    }
    
    public static FileObject fromNode(FileNode node, String spaceUname, MessageService messageService) throws UnsupportedEncodingException{
        FileObject item = new FileObject();
        item.name = node.getFilename();
        item.size = node.getSize();
        item.author = node.getCreateor();
        item.date = DateUtil.toDisplayDate(WikiUtil.getUser(), new Date(node.getDate()),messageService);
        item.url = WebUtil.getPageRepoFileUrl(WebUtil.getHostAppURL(),spaceUname, node.getFilename(), node.getNodeUuid(), true);
        item.delete_url = WebUtil.getHostAppURL() + "pages/pages!deleteAttachment.do?s=" + URLEncoder.encode(spaceUname, Constants.UTF8) 
                + "&u=" + URLEncoder.encode(node.getIdentifier(), Constants.UTF8)
                + "&nodeUuid=" + URLEncoder.encode(node.getNodeUuid(), Constants.UTF8);
        return item;
    }
}