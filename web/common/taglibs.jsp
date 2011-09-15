<%@ page language="java" errorPage="/error.jsp" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>

<%@ taglib uri="http://www.springframework.org/security/tags" prefix="authz" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ taglib uri="/struts-tags" prefix="s" %>

<%@ taglib uri="/WEB-INF/tlds/edgenius.tld" prefix="edgenius" %>


<c:set var="ctxPath" value="${pageContext.request.contextPath}" scope="request"/>

<c:set var="skinPath"><edgenius:skinPath/></c:set>
