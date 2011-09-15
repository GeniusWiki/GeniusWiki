<div class="macroCalendar">
    <div id="caltoolbar${calID}" class="ctoolbar">
    
        <div class="calname">
            <div>${calName}</div>
        </div>
        <#if !readonly>
	        <div id="faddbtn${calID}" class="fbutton">
	            <div><span title='Create new event' class="addcal">New</span></div>
	        </div>
	        <div class="btnseparator"></div>
	    </#if>
         <div id="showtodaybtn${calID}" class="fbutton">
            <div><span title='Return today' class="showtoday">Today</span></div>
        </div>
           <div class="btnseparator"></div>
		<div id="showdaybtn${calID}" class="fbutton <#if view == "day">fcurrent</#if>">
            <div><span class="showdayview" >Day</span></div>
        </div>
        <div  id="showweekbtn${calID}" class="fbutton <#if view == "week">fcurrent</#if>">
            <div><span  class="showweekview">Week</span></div>
        </div>
          <div  id="showmonthbtn${calID}" class="fbutton <#if view == "month">fcurrent</#if>">
            <div><span class="showmonthview">Month</span></div>
        </div>
			<div class="btnseparator"></div>
         <div  id="showreflashbtn${calID}" class="fbutton">
            <div><span title='Refresh' class="showdayflash">Refresh</span></div>
            </div>
         <div class="btnseparator"></div>
        <div id="sfprevbtn${calID}" title="Previous"  class="fbutton">
          <span class="fprev"></span>
        </div>
        <div id="sfnextbtn${calID}" title="Next" class="fbutton">
            <span class="fnext"></span>
        </div>
        <div class="fshowdatep fbutton">
            <div>
                <input type="hidden" name="txtshow" id="hdtxtshow${calID}" />
                <span id="txtdatetimeshow${calID}"></span>
            </div>
        </div>
        <div>
            <div id="loadingpannel${calID}" class="dvloading" style="display: none;">Loading</div>
            <div id="errorpannel${calID}" class="dverror" style="display: none;">Sorry, unable to load data</div>
         </div>
        <div class="clear"></div>              
	</div>
	<div class="dvCalBody">
	    <div class="t1 chromeColor">
            &nbsp;</div>
        <div class="t2 chromeColor">
            &nbsp;</div>
	   	 <div id="dvCalMain${calID}" class="calmain printborder">
	        <div id="gridcontainer${calID}" style="overflow-y: visible;" class="dvGridContainer"></div>
	    </div>
        <div class="t1 chromeColor">
            &nbsp;</div>
        <div class="t2 chromeColor">
            &nbsp;</div>
     </div>
     
   <#-- below <link>/<script src> must under above div, which is request from IE8 -->
   <link href="${resourcePath}/themes/default/main.css" rel="stylesheet" type="text/css" /> 
   <link href="${resourcePath}/themes/default/calendar.css" rel="stylesheet" type="text/css" /> 
   <link href="${contextPath}/widgets/jquery/dropdown/dropdown.css" rel="stylesheet" type="text/css" /> 
   <link href="${contextPath}/widgets/jquery/datepicker/dp.css" rel="stylesheet" type="text/css" /> 
   <link href="${contextPath}/widgets/jquery/colorselect/colorselect.css" rel="stylesheet" type="text/css" /> 
   
   <script src="${resourcePath}/xgcalendar_lang_en_US.js" type="text/javascript"></script>  
   <script src="${resourcePath}/jquery.calendar.js?v=1.3" type="text/javascript"></script>
   <script src="${contextPath}/widgets/jquery/datepicker/datepicker_lang_en_US.js" type="text/javascript"></script> 
   <script src="${contextPath}/widgets/jquery/datepicker/jquery.datepicker.js" type="text/javascript"></script>
   <script src="${contextPath}/widgets/jquery/dropdown/jquery.dropdown.js" type="text/javascript"></script>
   <script src="${contextPath}/widgets/jquery/colorselect/colorselect_lang_en_US.js" type="text/javascript"></script>
   <script src="${contextPath}/widgets/jquery/colorselect/jquery.colorselect.js" type="text/javascript"></script>
      
   <script type="text/javascript">
   		<!--
        $(document).ready(function() {
           var op${calID} = {
                view: "${view}",
                theme:3,
                weekstartday: ${weekStartDay},
                height:"${height}",
                showday: new Date(),
                EditCmdhandler:Edit,
                DeleteCmdhandler:Delete,
                ViewCmdhandler:View,    
                onWeekToDay: weekToDay${calID},
                onBeforeRequestData: cal_beforerequest${calID},
                onAfterRequestData: cal_afterrequest${calID},
                onRequestDataError: cal_onerror${calID}, 
                autoload: true,
                url: "${contextPath}/ext/calendar.do",
                quickAddUrl: "${contextPath}/ext/calendar!quickAdd.do",
                quickUpdateUrl:"${contextPath}/ext/calendar!quickUpdate.do",
                quickDeleteUrl: "${contextPath}/ext/calendar!delete.do",
                extParam:[{"name":"calendarName",value:"${calName}"},{"name":"pageUuid",value:"${pageUuid}"},{"name":"weekStartDay",value:"${weekStartDay}"},{"name":"viewType",value:"${view}"}],
                readonly: ${readonly?string}
            };
            
            var p${calID} = $("#gridcontainer${calID}").bcalendar(op${calID}).BcalGetOp();
            
            $("#caltoolbar${calID}").noSelect();
			
            $("#hdtxtshow${calID}").datepicker({ picker: "#txtdatetimeshow${calID}", showtarget: $("#txtdatetimeshow${calID}"),
            		onReturn:function(r){                          
                            var p = $("#gridcontainer${calID}").BCalGoToday(r).BcalGetOp();
                            if (p && p.datestrshow) {
                                $("#txtdatetimeshow${calID}").text(p.datestrshow);
                            }
                     } 
            });
            function cal_beforerequest${calID}(type){
                var t="Loading data...";
                switch(type)
                {
                    case 1:
                        t="Loading data...";
                        break;
                    case 2:                      
                    case 3:  
                    case 4:    
                        t="Processing";                                   
                        break;
                }
                $("#errorpannel${calID}").hide();
                $("#loadingpannel${calID}").html(t).show();    
            }
            function cal_afterrequest${calID}(type){
              switch(type)
                {
                    case 1:
		                $("#loadingpannel${calID}").hide();
                        if (p${calID} && p${calID}.datestrshow) {
              			  $("#txtdatetimeshow${calID}").text(p${calID}.datestrshow);
         				}
                        break;
                    case 2:
                    case 3:
                    case 4:
		      			$("#loadingpannel${calID}").html("Success!");
		                window.setTimeout(function(){ $("#loadingpannel${calID}").hide();},2000);
		      			break;
                }          
            }
            function cal_onerror${calID}(type,data){
           		  $("#errorpannel${calID}").show();
            }
            function Edit(data){
                //var eurl="/CM/EditCalendar/{0}?start={2}&end={3}&isallday={4}&title={1}";
            	if(data){
            		if(StrFormat("{0}",data) == "0"){
	            		//new event - but has start/end time
            			var stDate = strtodate(StrFormat("{2}",data)).getTime();
            			var enDate = strtodate(StrFormat("{3}",data)).getTime();
            			var allD = StrFormat("{4}",data)=="0"?false:true;
	                    gwtNewEventDialog("${pageUuid}", "${calName}",stDate, enDate,allD,StrFormat("{1}",data), function(){
            	 			$("#gridcontainer${calID}").BCalReload();
            			});
	            	} else {
	            	    //edit - only pass in id
	            	    gwtEditEventDialog(StrFormat("{0}",data),function(){
            	 			$("#gridcontainer${calID}").BCalReload();
            			});
	            	}
	            }
            }   
            function View(data){
            	if(data){
            		<#-- if view only page, gwt is not initialized so block this dialog popup -->
            		if (gwtViewEventDialog && typeof (gwtViewEventDialog) == "function") 
            			gwtViewEventDialog(StrFormat("{0}",data));
            	}
            }

            function Delete(data,callback){      
            	if(confirm("Do you want to delete this event?")){
            		callback(0)
            	}
            }
            <#if !readonly>
	            //open new event dialog
	            $("#faddbtn${calID}").click(function(e) {
	            	gwtNewEventDialog("${pageUuid}", "${calName}",-1,-1,false,"",function(){
	            	 	$("#gridcontainer${calID}").BCalReload();
	            	});
	            });
	        </#if>
            function weekToDay${calID}(option){            
               if (option && option.datestrshow) {
                    $("#txtdatetimeshow${calID}").text(option.datestrshow);
                }
                $("#caltoolbar${calID} div.fcurrent").each(function() {
                    $(this).removeClass("fcurrent");
                })
                $("#showdaybtn${calID}").addClass("fcurrent");
            }
          
           	//Show day view
            $("#showdaybtn${calID}").click(function(e) {
                //document.location.href="#day";
                $("#caltoolbar${calID} div.fcurrent").each(function() {
                    $(this).removeClass("fcurrent");
                })
                $(this).addClass("fcurrent");
                var p = $("#gridcontainer${calID}").BCalSwtichview("day").BcalGetOp();
                if (p && p.datestrshow) {
                    $("#txtdatetimeshow${calID}").text(p.datestrshow);
                }
            });
            //Show week view
            $("#showweekbtn${calID}").click(function(e) {
                //document.location.href="#week";
                $("#caltoolbar${calID} div.fcurrent").each(function() {
                    $(this).removeClass("fcurrent");
                })
                $(this).addClass("fcurrent");
                var p = $("#gridcontainer${calID}").BCalSwtichview("week").BcalGetOp();
                if (p && p.datestrshow) {
                    $("#txtdatetimeshow${calID}").text(p.datestrshow);
                }

            });
            //Show month view
            $("#showmonthbtn${calID}").click(function(e) {
                //document.location.href="#month";
                $("#caltoolbar${calID} div.fcurrent").each(function() {
                    $(this).removeClass("fcurrent");
                })
                $(this).addClass("fcurrent");
                var p = $("#gridcontainer${calID}").BCalSwtichview("month").BcalGetOp();
                if (p && p.datestrshow) {
                    $("#txtdatetimeshow${calID}").text(p.datestrshow);
                }
            });
             //return to today
            $("#showtodaybtn${calID}").click(function(e) {
                var p = $("#gridcontainer${calID}").BCalGoToday().BcalGetOp();
                if (p && p.datestrshow) {
                    $("#txtdatetimeshow${calID}").text(p.datestrshow);
                }
            });
              //previous
            $("#sfprevbtn${calID}").click(function(e) {
                var p = $("#gridcontainer${calID}").BCalPrev().BcalGetOp();
                if (p && p.datestrshow) {
                    $("#txtdatetimeshow${calID}").text(p.datestrshow);
                }
 
            });
            //next
            $("#sfnextbtn${calID}").click(function(e) {
                var p = $("#gridcontainer${calID}").BCalNext().BcalGetOp();
                if (p && p.datestrshow) {
                    $("#txtdatetimeshow${calID}").text(p.datestrshow);
                }
            });
            //refresh
            $("#showreflashbtn${calID}").click(function(e){
                $("#gridcontainer${calID}").BCalReload();
            });
            
        });

        function initEventDialog(colorVal){
        	$("#stpartdate").datepicker({ picker: "<button id='startcalpick' class='calpick'></button>"});    
        	$("#etpartdate").datepicker({ picker: "<button id='endcalpick' class='calpick'></button>"}); 
        	
            $("#calendarcolor").colorselect({ title: "ColorPicker", index: colorVal, hiddenid: "colorvalue" });
            
            var arrT = [];
            var tt = "{0}:{1}";
            for (var i = 0; i < 24; i++) {
                arrT.push({ text: StrFormat(tt, [i >= 10 ? i : "0" + i, "00"]) }, { text: StrFormat(tt, [i >= 10 ? i : "0" + i, "30"]) });
            }
            $("#stparttime").dropdown({
            	id:"divstimedrop",
                dropheight: 200,
                dropwidth:60,
                selectedchange: function() { },
                items: arrT
            });
            
            $("#etparttime").dropdown({
            	id:"divetimedrop",
                dropheight: 200,
                dropwidth:60,
                selectedchange: function() { },
                items: arrT
            });
      
            //hide bubble since it bothers the new popup dialog
      		$("#bbit-cal-buddle").css("visibility", "hidden");
        }
        function StrFormat(temp, dataarry) {
        	return temp.replace(/\{([\d]+)\}/g, function(s1, s2) { var s = dataarry[s2]; if (typeof (s) != "undefined") { if (s instanceof (Date)) { return s.getTimezoneOffset() } else { return (s); } } else { return ""; } });
        }
        function strtodate(str) {

            var arr = str.split(" ");
            var arr2 = arr[0].split(i18n.xgcalendar.dateformat.separator);
            var arr3 = arr[1].split(":");

            var y = arr2[i18n.xgcalendar.dateformat.year_index];
            var m = arr2[i18n.xgcalendar.dateformat.month_index].indexOf("0") == 0 ? arr2[i18n.xgcalendar.dateformat.month_index].substr(1, 1) : arr2[i18n.xgcalendar.dateformat.month_index];
            var d = arr2[i18n.xgcalendar.dateformat.day_index].indexOf("0") == 0 ? arr2[i18n.xgcalendar.dateformat.day_index].substr(1, 1) : arr2[i18n.xgcalendar.dateformat.day_index];
            var h = arr3[0].indexOf("0") == 0 ? arr3[0].substr(1, 1) : arr3[0];
            var n = arr3[1].indexOf("0") == 0 ? arr3[1].substr(1, 1) : arr3[1];
            return new Date(y, parseInt(m) - 1, d, h, n);
        }
        -->
    </script>      
</div>