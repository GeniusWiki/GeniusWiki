<%@ include file="/common/taglibs.jsp"%>

<html>
	<head>
	    <%@ include file="/common/meta.jsp"%>
		<title><edgenius:title/></title>
		<link rel="stylesheet" type="text/css" media="all" href="${skinPath}/styles/admin.css?v=@TOKEN.SITE.VERSION@" />
		<script type="text/javascript" src="${ctxPath}/widgets/jquery/jquery.min.js?v=@TOKEN.SITE.VERSION@"></script>
		<script type="text/javascript" src="${ctxPath}/static/scripts/admin.js?v=@TOKEN.SITE.VERSION@"></script>
		
		<script type="text/javascript">
			if("true" == "${logout}"){
				window.parent.location.href="<c:url value='/j_spring_security_logout'/>";
			}
					
			function download(type, f){
				$("#dframe").attr("src","<c:url value='/instance/backup!download.do?filename='/>"+f+"&type="+type);
			}
			function deleteConfirm(type,fname){
				if(confirm("<fmt:message key='delete.confirm'/>")){
					location.href="<c:url value='/instance/backup!delete.do?filename='/>" + fname+"&type="+type;
				}
			}
			function editSchedule(editing){
				if(editing){
					$("div.scheduleDisplay").hide();
					$("div.scheduleEditor").show();
				}else{
					$("div.scheduleDisplay").show();
					$("div.scheduleEditor").hide();
				}
				
			}
			function scheduleTypeChange(st){
				$("div.hourDiv").show();
				$("div.minuteDiv").show();
				if(st ==1){
					//mth
					$("div.monthDay").show();
					$("div.weekDay").hide();
				}else if(st ==2){
					//week
					$("div.monthDay").hide();
					$("div.weekDay").show();
				}else if(st ==3){
					//daily
					$("div.monthDay").hide();
					$("div.weekDay").hide();
				}else{
					//default - none of schedule
					$("div.monthDay").hide();
					$("div.weekDay").hide();
					$("div.hourDiv").hide();
					$("div.minuteDiv").hide();
				}
			}
			function submitSchedule(){
				var st = $('input:radio[name=scheduleType]:checked').val();

				var params;
				var err = "";
				var h = $("#hour").val();
				var m = $("#minute").val();
				if(h < 0 || h > 23){
					err = "<fmt:message key='valid.hour'/>";
				}
				if(m < 0 || m > 59){
					if(err != "") err += "\n";
					err += "<fmt:message key='valid.minute'/>";
				}
					
					
				if(st ==1){
					//mth
					var d = $("#dayOfMth").val();
					if(d < 0 || d >31){
						if(err != "") err += "\n";
						err += "<fmt:message key='valid.dayofmonth'/>";
					}
					params = "dayOfMth="+d+"&hours="+h+"&minutes="+m;
				}else if(st ==2){
					//week
					var d = $("#dayOfWeek").val();
					params = "dayOfWeek="+d+"&hours="+h+"&minutes="+m;
				}else if(st ==3){
					//daily
					params = "hours="+h+"&minutes="+m;
				}
				if(err != ""){
					alert(err);
					return;
				}
				$.get("<c:url value='/instance/backup!schedule.do?'/>"+params+"&scheduleType=" +st
					,function(data){
						var status = data.charAt(0);
						data = data.substring(1) 
						if(status == "0"){
							$("div.infotext").text(data);
							editSchedule(false);
						}else{
							$("#scheduleMsg").text(data);
						}
					});
			}
			function initload(){
				<c:set var="st" value="${scheduleType}"/>
				<c:if test="${empty scheduleType|| scheduleType == 0}">
					<c:set var="st" value="1"/>
				</c:if>

				scheduleTypeChange(${st});
				$("#type${st}").attr("checked","checked");
				
				resetURLPanelHeight();
			}

			function restoreConfirm(){
				if(confirm("<fmt:message key='restore.confirm'/>"))
					return true;

				return false;
			}
			function backupConfirm(){
				if(confirm("<fmt:message key='backup.confirm'/>"))
					return true;

				return false;
			}
			function restoreConfirm2(type, fname){
				if(confirm("<fmt:message key='restore.confirm'/>"))
					location.href="<c:url value='/instance/backup!restoreFromName.do?filename='/>" + fname+"&type="+type;
			}
			
		</script>	
	</head>

	<body onload="initload()">
	<input type="hidden" id="urlPanelChildUid" value="backup"/>
	
	<table border="0" cellspacing="5" width="98%" align="center">
	<tr><td>
		<%@ include file="/common/messages.jsp"%>
	</td></tr>
	<tr><td>
		<div class="backupSchedule">
			<div class="scheduleBody">
				<SPAN style="font-weight:bolder;font-size: 17px;"><fmt:message key="backup.schedule"/>: </SPAN> 
				<div class="scheduleDisplay">
					<div class="infotext">
						<c:choose>
							<c:when test="${empty scheduleDisplay}">
								(<fmt:message key="none"/>)
							</c:when>
							<c:otherwise>
								${scheduleDisplay}										
							</c:otherwise>
						</c:choose>
					</div>
					&nbsp;&nbsp;<a href="javascript:editSchedule(true)" title="<fmt:message key="edit.schedule"/>"><fmt:message key="edit"/></a>
				</div>
				<div class="scheduleEditor" style="display:none">
						<input type="radio" name="scheduleType" id="type1" onclick="scheduleTypeChange(1)" value="1"><fmt:message key="monthly"/>
						<input type="radio" name="scheduleType" id="type2" onclick="scheduleTypeChange(2)" value="2"><fmt:message key="weekly"/>
						<input type="radio" name="scheduleType" id="type3" onclick="scheduleTypeChange(3)" value="3"><fmt:message key="daily"/>
						<input type="radio" name="scheduleType" id="type4" onclick="scheduleTypeChange(0)" value="0"><fmt:message key="none.upper"/>
						&nbsp;&nbsp;&nbsp;
						<div class="monthDay"><fmt:message key="date"/>(1-31)<input type="text" id="dayOfMth" name="dayOfMth" maxlength="2" size="2" value="${dayOfMth}"></div>
						<div class="weekDay">
							<select name="dayOfWeek" id="dayOfWeek">
								<option value="SAT" <c:if test="${dayOfWeek == 'SAT'}">selected="selected"</c:if>><fmt:message key="Saturday"/></option>
								<option value="SUN" <c:if test="${dayOfWeek == 'SUN'}">selected="selected"</c:if>><fmt:message key="Sunday"/></option>
								<option value="MON" <c:if test="${dayOfWeek == 'MON'}">selected="selected"</c:if>><fmt:message key="Monday"/></option>
								<option value="TUE" <c:if test="${dayOfWeek == 'TUE'}">selected="selected"</c:if>><fmt:message key="Tuesday"/></option>
								<option value="WED" <c:if test="${dayOfWeek == 'WED'}">selected="selected"</c:if>><fmt:message key="Wednesday"/></option>
								<option value="THU" <c:if test="${dayOfWeek == 'THU'}">selected="selected"</c:if>><fmt:message key="Thursday"/></option>
								<option value="FRI" <c:if test="${dayOfWeek == 'FRI'}">selected="selected"</c:if>><fmt:message key="Friday"/></option>
							</select>
						</div>
						<div class="hourDiv">
							<fmt:message key="hour"/>(0-23)<input type="text" id="hour" name="hour" maxlength="2" size="2" value="${hours}">
						</div>
						<div class="minuteDiv">
							<fmt:message key="minute"/>(0-59)<input type="text" id="minute" name="minute" maxlength="2" size="2" value="${minutes}">
						</div>
						&nbsp;&nbsp;&nbsp;
						<a href="javascript:editSchedule(false)"><fmt:message key="cancel"/></a> &nbsp;
						<a href="javascript:submitSchedule()"><fmt:message key="done"/></a>
						<div class="message-panel error" id="scheduleMsg"></div>
				</div>
			</div>
		</div>	
	</td></tr>
	<tr><td>
		<h2><fmt:message key="backup"/></h2>
		<div class="separator"></div>
		<iframe id="dframe" width="0" height="0" scrolling="no" frameborder="0"   src=""></iframe>
		<form action="<c:url value='/instance/backup!backup.do'/>" id="backupForm" method="post" onSubmit="return backupConfirm()">
			<span style="form-label"><fmt:message key="comment"/></span> <input type="text" name="comment" maxlength="150" style="width:400px"/>
			<input type="submit" value="<fmt:message key='backup.now'/>">
		</form>
		<c:if test="${not empty bList}">
			<table cellpadding="5" border="0" width="95%" align="center">
				<tr>
					<th width="1%" nowrap="nowrap"><fmt:message key="file.name"/></th>
					<th><fmt:message key="comment"/></th>
					<th><fmt:message key="size"/></th>
					<th><fmt:message key="last.modify.date"/></th>
					<th></th>
				</tr>
				<c:forEach var="file" items="${bList}">
					<tr align="center">
						<td width="1%" nowrap="nowrap">${file.name}</td>
						<td>${file.comment}</td>
						<td >${file.size}</td>
						<td >${file.date}</td>
						<td nowrap="nowrap">
							<a href="javascript:;" onclick="download(0, '${file.name}')"><fmt:message key="download"/></a>
							&nbsp;
							<a href="javascript:;" onclick="restoreConfirm2(0,'${file.name}')"><fmt:message key="restore"/></a>
							&nbsp;
							<a href="javascript:;" onclick="deleteConfirm(0, '${file.name}')"><fmt:message key="delete"/></a></td>
						</td>
					</tr>					
				</c:forEach>
			</table>
		</c:if>
	</td></tr>
	<tr><td>
		<h2><fmt:message key="restore"/></h2>
		<div class="separator"></div>
		<form action="<c:url value='/instance/backup!restore.do'/>" id="restoreForm" method="post" enctype="multipart/form-data" onSubmit="return restoreConfirm()">
			<span style="form-label"><fmt:message key="upload.file"/></span> <input type="file" name="restoreFile"/> <input type="submit" value="<fmt:message key='restore.now'/>"/>
		</form>
		<c:if test="${not empty rList}">
			<table cellpadding="5" border="0" width="95%" align="center">
				<tr>
					<th width="1%" nowrap="nowrap"><fmt:message key="file.name"/></th>
					<th><fmt:message key="comment"/></th>
					<th><fmt:message key="size"/></th>
					<th><fmt:message key="last.modify.date"/></th>
					<th></th>
				</tr>
				<c:forEach var="file" items="${rList}">
					<tr align="center">
						<td width="1%" nowrap="nowrap">${file.name}</td>
						<td >${file.comment}</td>
						<td>${file.size}</td>
						<td>${file.date}</td>
						<td nowrap="nowrap">
							<a href="javascript:;" onclick="download(1, '${file.name}')"><fmt:message key="download"/></a>
							&nbsp;
							<a href="javascript:;" onclick="restoreConfirm2(1, '${file.name}')"><fmt:message key="restore"/></a>
							&nbsp;
							<a href="javascript:;" onclick="deleteConfirm(1, '${file.name}')"><fmt:message key="delete"/></a></td>
						</td>
					</tr>					
				</c:forEach>
			</table>
		</c:if>
	</td></tr>
	</table>
	</body>
</html>
