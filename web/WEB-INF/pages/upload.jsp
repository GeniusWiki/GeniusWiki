<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/common/taglibs.jsp"%>
<html>
<head>
	<%@ include file="/common/meta.jsp"%>
	<link rel="stylesheet" type="text/css" media="all" href="<c:url value="/static/styles/simple-page.css?v=@TOKEN.SITE.VERSION@"/>" /> 
	<title><fmt:message key="app.name" /> - <fmt:message key="upload"/></title>
	
	<link rel="stylesheet" href="${ctxPath}/widgets/jquery/jquery-upload/blueimp/css/bootstrap.min.css">
	<link rel="stylesheet" href="${ctxPath}/widgets/jquery/jquery-upload/blueimp/css/bootstrap-responsive.min.css">
	<link rel="stylesheet" href="${ctxPath}/widgets/jquery/jquery-upload/blueimp/css/bootstrap-image-gallery.min.css">
	<!--[if lt IE 9]><script src="${ctxPath}/widgets/html5shiv/html5.js"></script><![endif] -->
	
	<link rel="stylesheet" href="${ctxPath}/widgets/jquery/jquery-upload/css/jquery.fileupload-ui.css">

</head>
<body>
<div class="container">
	<div class="well">
        <h3>Drag and drop files into this dialog or use below "Add files..." button to choose multiple files.</h3>
    </div>
	<p></p>    
    <form id="fileupload" action="${ctxPath}/pages/upload" method="post" enctype="multipart/form-data">
    	<input type="hidden" name="pageUuid" value="${pageUuid}"/>
    	<input type="hidden" name="spaceUname" value="${spaceUname}"/>
    	
        <div class="row fileupload-buttonbar">
            <div class="span7">
                <span class="btn btn-success fileinput-button">
                    <i class="icon-plus icon-white"></i>
                    <span>Add files...</span>
                    <input type="file" name="files" multiple>
                </span>
                <button type="submit" class="btn btn-primary start">
                    <i class="icon-upload icon-white"></i>
                    <span>Start upload</span>
                </button>
                <button type="reset" class="btn btn-warning cancel">
                    <i class="icon-ban-circle icon-white"></i>
                    <span>Cancel upload</span>
                </button>
                <button type="button" class="btn btn-danger delete">
                    <i class="icon-trash icon-white"></i>
                    <span>Delete</span>
                </button>
                <input type="checkbox" class="toggle">
            </div>
            <!-- The global progress information -->
            <div class="span5 fileupload-progress fade">
                <!-- The global progress bar -->
                <div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100">
                    <div class="bar" style="width:0%;"></div>
                </div>
                <!-- The extended global progress information -->
                <div class="progress-extended">&nbsp;</div>
            </div>
        </div>
        <!-- The loading indicator is shown during file processing -->
        <div class="fileupload-loading"></div>
        <br>
        <!-- The table listing the files available for upload/download -->
        <table role="presentation" class="table table-striped"><tbody class="files" data-toggle="modal-gallery" data-target="#modal-gallery"></tbody></table>
    </form>
    
</div>
<!-- modal-gallery is the modal dialog used for the image gallery -->
<div id="modal-gallery" class="modal modal-gallery hide fade" data-filter=":odd">
    <div class="modal-header">
        <a class="close" data-dismiss="modal">&times;</a>
        <h3 class="modal-title"></h3>
    </div>
    <div class="modal-body"><div class="modal-image"></div></div>
    <div class="modal-footer">
        <a class="btn modal-download" target="_blank">
            <i class="icon-download"></i>
            <span>Download</span>
        </a>
        <a class="btn btn-success modal-play modal-slideshow" data-slideshow="5000">
            <i class="icon-play icon-white"></i>
            <span>Slideshow</span>
        </a>
        <a class="btn btn-info modal-prev">
            <i class="icon-arrow-left icon-white"></i>
            <span>Previous</span>
        </a>
        <a class="btn btn-primary modal-next">
            <span>Next</span>
            <i class="icon-arrow-right icon-white"></i>
        </a>
    </div>
</div>
<!-- The template to display files available for upload -->
<script id="template-upload" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
    <tr class="template-upload fade">
        <td class="preview"><span class="fade"></span></td>
        <td class="name"><span>{%=file.name%}</span></td>
        <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
        {% if (file.error) { %}
            <td class="error" colspan="2"><span class="label label-important">{%=locale.fileupload.error%}</span> {%=locale.fileupload.errors[file.error] || file.error%}</td>
        {% } else if (o.files.valid && !i) { %}
            <td>
                <div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0"><div class="bar" style="width:0%;"></div></div>
            </td>
            <td class="start">{% if (!o.options.autoUpload) { %}
                <button class="btn btn-primary">
                    <i class="icon-upload icon-white"></i>
                    <span>{%=locale.fileupload.start%}</span>
                </button>
            {% } %}</td>
        {% } else { %}
            <td colspan="2"></td>
        {% } %}
        <td class="cancel">{% if (!i) { %}
            <button class="btn btn-warning">
                <i class="icon-ban-circle icon-white"></i>
                <span>{%=locale.fileupload.cancel%}</span>
            </button>
        {% } %}</td>
    </tr>
{% } %}
</script>
<!-- The template to display files available for download -->
<script id="template-download" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
    <tr class="template-download fade">
        {% if (file.error) { %}
            <td></td>
            <td class="name"><span>{%=file.name%}</span></td>
            <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
            <td class="error" colspan="2"><span class="label label-important">{%=locale.fileupload.error%}</span> {%=locale.fileupload.errors[file.error] || file.error%}</td>
        {% } else { %}
            <td class="preview">{% if (file.thumbnail_url) { %}
                <a href="{%=file.url%}" title="{%=file.name%}" rel="gallery" download="{%=file.name%}"><img src="{%=file.thumbnail_url%}"></a>
            {% } %}</td>
            <td class="name">
                <a href="{%=file.url%}" title="{%=file.name%}" rel="{%=file.thumbnail_url&&'gallery'%}" download="{%=file.name%}">{%=file.name%}</a>
            </td>
            <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
            <td colspan="2"></td>
        {% } %}
        <td class="delete">
            <button class="btn btn-danger" data-type="{%=file.delete_type%}" data-url="{%=file.delete_url%}">
                <i class="icon-trash icon-white"></i>
                <span>{%=locale.fileupload.destroy%}</span>
            </button>
            <input type="checkbox" name="delete" value="1">
        </td>
    </tr>
{% } %}
</script>

<script type="text/javascript" src='${ctxPath}/widgets/jquery/jquery.min.js?v=@TOKEN.SITE.VERSION@'></script>
<script type="text/javascript" src='${ctxPath}/widgets/jquery/jquery-ui/jquery-ui-widget.min.js?v=@TOKEN.SITE.VERSION@'></script>

<script src="${ctxPath}/widgets/jquery/jquery-upload/blueimp/tmpl.min.js"></script>
<script src="${ctxPath}/widgets/jquery/jquery-upload/blueimp/load-image.min.js"></script>
<script src="${ctxPath}/widgets/jquery/jquery-upload/blueimp/canvas-to-blob.min.js"></script>

<script src="${ctxPath}/widgets/jquery/jquery-upload/js/jquery.fileupload.js?v=@TOKEN.SITE.VERSION@"></script>
<script src="${ctxPath}/widgets/jquery/jquery-upload/js/jquery.fileupload-fp.js?v=@TOKEN.SITE.VERSION@"></script>
<script src="${ctxPath}/widgets/jquery/jquery-upload/js/jquery.fileupload-ui.js?v=@TOKEN.SITE.VERSION@"></script>
<script src="${ctxPath}/widgets/jquery/jquery-upload/js/locale.js?v=@TOKEN.SITE.VERSION@"></script>
<script src="${ctxPath}/widgets/jquery/jquery-upload/js/main.js?v=@TOKEN.SITE.VERSION@"></script>
</body> 
</html>
