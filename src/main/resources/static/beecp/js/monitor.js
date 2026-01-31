var dsURL = 'getDataSourceList';
var sqlURL = 'getSqlTraceList';
var restartURL = 'restartPool';
var interruptURL = 'interruptPool';
var language = $("html").attr("lang");

function poolRestart(dsId){
    $.ajax({
        type: 'POST',
        url: restartURL,
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify({'dsId':dsId}),
        success: function(data) {
           if(data.code==1) {
               alert(language=='cn'? '重启成功':'Restart success');
               getDsListFromServer();
               getSqlListFromServer();
          }
        }
    });
}

function poolInterrupt(dsId){
     $.ajax({
        type: 'POST',
        url: interruptURL,
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify({'dsId':dsId}),
        success: function(data) {
            //alert(language=='cn'? '中断成功':'Interrupt success');
            $('#ds_refresh_button').trigger("click");
        }
     });
 }

$(function() {
    var refreshMsg = language=='cn'? '刷新成功':'Refresh success';

    var sqlTraceList = []; //empty array
    var curSqlPageSize = 10;
    var curSqlPageNo = 1;
    var maxSqlPageNo = 0;
    var dsRefreshTask;
    var sqlRefreshTask;
    $('#ds_monitorTable').tablesorter();
    $('#sql_monitorTable').tablesorter();

    $("#ds_refresh_button").click(function() {
        getDsListFromServer();
        alert(refreshMsg);
    });
    $("#sql_refresh_button").click(function() {
        getSqlListFromServer();
        alert(refreshMsg);
    });
	
	$("#ds_timer_button").click(function() {
        if (dsRefreshTask != null){//stop
		 	clearInterval(dsRefreshTask);
			dsRefreshTask =null;
			var name=(language=='cn')?'启动定时':'Run Timer';
			$("#ds_timer_button").val(name)
		}else{//run
			dsRefreshTask = setInterval(getDsListFromServer, $("#ds_refresh_interval").val());
			var name=(language=='cn')?'停止定时':'Stop Timer';
			$("#ds_timer_button").val(name);
		}
    });
	$("#sql_timer_button").click(function() {
        if (sqlRefreshTask != null){//stop
		 	clearInterval(sqlRefreshTask);
			sqlRefreshTask =null;
			var name=(language == 'cn')?'启动定时':'Run Timer';
			$("#sql_timer_button").val(name);
		}else{//run
		    sqlRefreshTask = setInterval(getSqlListFromServer, $("#sql_refresh_interval").val()); 
			var name=(language=='cn')?'停止定时':'Stop Timer';
			$("#sql_timer_button").val(name);
		}
    });
    $("#ds_refresh_interval").click(function() {
        if (dsRefreshTask != null){ 
		  clearInterval(dsRefreshTask);
          dsRefreshTask = setInterval(getDsListFromServer, $("#ds_refresh_interval").val());
		}
    });
    $("#sql_refresh_interval").click(function() {
        if (sqlRefreshTask != null){
			clearInterval(sqlRefreshTask);
			sqlRefreshTask = setInterval(getSqlListFromServer, $("#sql_refresh_interval").val());
		}
    });
	
    $("#page_size").change(function() {
        curSqlPageSize = $("#page_size").val();
        curSqlPageNo = 1;
        showSqlTracePage(curSqlPageNo);
    });
    $("#sql_first").click(function() { //move to first page
        curSqlPageNo = 1;
        showSqlTracePage(curSqlPageNo);
    });
    $("#sql_pre").click(function() { //move to pre page
        curSqlPageNo = curSqlPageNo - 1;
        showSqlTracePage(curSqlPageNo);
    });
    $("#sql_next").click(function() { //move to next page
        curSqlPageNo = curSqlPageNo + 1;
        showSqlTracePage(curSqlPageNo);
    });
    $("#sql_last").click(function() { //move to last page
        curSqlPageNo = maxSqlPageNo;
        showSqlTracePage(curSqlPageNo);
    });
    $('#tabs a').click(function(e) {
        e.preventDefault();
        $('#tabs li').removeClass("current").removeClass("hoverItem");
        $(this).parent().addClass("current");
        $("#content div").removeClass("show");
        $('#' + $(this).attr('title')).addClass('show');
    });

    $('#tabs a').hover(function() {
            if (!$(this).parent().hasClass("current")) {
                $(this).parent().addClass("hoverItem");
            }
        },
        function() {
            $(this).parent().removeClass("hoverItem");
        });

    function getSqlListFromServer() {
        $.ajax({
            type: 'POST',
            url: sqlURL,
            dataType: 'json',
            success: function(data) {
                if(data.code==3) {
                    window.location.href ="login.html";
                }else if(data.code==2) {
                    alert("Error:"+data.message);
                }else if(data.code==1) {
                    curSqlPageNo = 1;
                    maxSqlPageNo = 0;
                    sqlTraceList = [];
                    $("#sql_first").attr("disabled", true);
                    $("#sql_pre").attr("disabled", true);
                    $("#sql_next").attr("disabled", true);
                    $("#sql_last").attr("disabled", true);
                    $("#sql_monitorTable tr:not(:first)").remove();

                    afterLoadSqlTraceList(data.result);
                }
            }
        });
    };

    function getDsListFromServer() {
        $.ajax({
            type: 'POST',
            url: dsURL,
            dataType: 'json',
            success: function(data) {
                console.info(data);
                $("#ds_monitorTable tr:not(:first)").remove();
                if (data) {
                    if(data.code==3) {
                        window.location.href = "login.html";
                    }else if(data.code==2) {
                        alert("Error:"+data.message);
                    }else if(data.code==1) {
                        $.each(data.result,
                            function (i, element) {
                                var isFairMode = element.fairMode;
                                var stateCode=-1;
				var stateDesc;
                               
							   if(element.lazy)stateCode=1;
                                //else if(element.new)state=0;
                                else if(element.starting)stateCode=1;
                                else if(element.ready)stateCode=2;
                                else if(element.closing)stateCode=3;
                                else if(element.restarting)stateCode=5;
                                else if(element.restartFailed)stateCode=6;
                                else if(element.suspended)stateCode=7;
                                else stateCode=4;

                                var creatingSize = element.creatingSize;
                                var creatingTimeoutSize = element.creatingTimeoutSize;
                                var clearButtonDesc;
                                var interruptButtonDesc;

                                if (language == 'cn') {
                                    clearButtonDesc='重启';
                                    interruptButtonDesc='中断';
                                    mode = isFairMode?'公平':'非公平';

                                    if (stateCode == -1) stateDesc = "未初始化";
                                    else if (stateCode == 0) stateDesc = "未初始化";
                                    else if (stateCode == 1) stateDesc = "启动中";
                                    else if (stateCode == 2) stateDesc = "已就绪";
                                    else if (stateCode == 3) stateDesc = "关闭中";
                                    else if (stateCode == 4) stateDesc = "已关闭";
                                    else if (stateCode == 5) stateDesc = "重启中";
                                    else if (stateCode == 6) stateDesc = "重启失败";
                                    else if (stateCode == 7) stateDesc = "已挂起";
                                } else {
                                   clearButtonDesc='Restart';
                                   interruptButtonDesc='Interrupt';
                                   mode = isFairMode?'fair':'unfair';
                                   if (stateCode == -1) stateDesc = "uninitialized";
                                   else  if (stateCode == 0) stateDesc = "new";
                                   else if (stateCode == 1) stateDesc = "starting";
                                   else if (stateCode == 2) stateDesc = "ready";
                                   else if (stateCode == 3) stateDesc = "closing";
                                   else if (stateCode == 4) stateDesc = "closed";
                                   else if (stateCode == 5) stateDesc = "restarting";
                                   else if (stateCode == 6) stateDesc = "restart_failed";
                                   else if (stateCode == 7) stateDesc = "suspended";
                                }

                                var tableHtml = "<tr>" + "<td>" + element.dsId + "</td>"
                                    + "<td>" + mode + "</td>" + "<td>" + stateDesc + "</td>"
                                    + "<td>" + element.maxSize + "</td>"
                                    + "<td>" + element.idleSize + "</td>"
                                    + "<td>" + element.borrowedSize + "</td>"
                                    + "<td>" + element.semaphoreWaitingSize + "</td>"
                                    + "<td>" + element.transferWaitingSize + "</td>"
                                    + "<td>" + creatingSize + "</td>"
                                    + "<td>" + creatingTimeoutSize + "</td>"
                                    + "<td><input id='pool_restart_button' onclick='poolRestart(\""+element.dsId+"\")' type='button' value='"+clearButtonDesc+"'/>";
                                    if(creatingTimeoutSize >0)
                                        tableHtml= tableHtml + "<input id='pool_interrupt_button' onclick='poolInterrupt(\""+element.dsId+"\")' type='button' value='"+interruptButtonDesc+"'/>";

                                    tableHtml= tableHtml + "</td></tr>";
                                    $("#ds_monitorTable").append(tableHtml);
                            });
                        $('#ds_monitorTable').trigger("update");
                    }
                }
            }
        });
    };

    function afterLoadSqlTraceList(data) { //after get result from server
        if (data) {
            sqlTraceList = data;
            $("#total_sql").val(sqlTraceList.length);
            maxSqlPageNo = parseInt(sqlTraceList.length / curSqlPageSize);
            if (data.length % curSqlPageSize > 0) maxSqlPageNo++;
            if (data.length > 0) showSqlTracePage();
        }
    }

    function showSqlTracePage() { //show sql page List
        var startIndex = (curSqlPageNo - 1) * curSqlPageSize;
        var endIndex = sqlTraceList.length;
        $("#sql_monitorTable tr:not(:first)").remove();

        if (maxSqlPageNo > 1) {
            if (curSqlPageNo == 1) { //at first page
                $("#sql_first").attr("disabled", true);
                $("#sql_pre").attr("disabled", true);
                $("#sql_next").attr("disabled", false);
                $("#sql_last").attr("disabled", false);
            } else if (curSqlPageNo == maxSqlPageNo) { //at end page
                $("#sql_first").attr("disabled", false);
                $("#sql_pre").attr("disabled", false);
                $("#sql_next").attr("disabled", true);
                $("#sql_last").attr("disabled", true);
            } else { //at middle page
                $("#sql_first").attr("disabled", false);
                $("#sql_pre").attr("disabled", false);
                $("#sql_next").attr("disabled", false);
                $("#sql_last").attr("disabled", false);
            }
        }

        var count = 0;
        for (var i = startIndex; i < endIndex; i++) {
            var element = sqlTraceList[i];
            var bgcolor = "";

            if (element.exception) { //fail
                bgcolor = " class='sqlExecFail'";
            } else if (element.slowInd) { //slow
                bgcolor = " class='sqlExecSlow'";
            }

            var tookTimeMs;
            if(element.endTime>0)tookTimeMs=element.endTime-element.startTime;
            var tableHtml = "<tr " + bgcolor + ">" + "<td>"
                + element.sql + "</td>" + "<td>" + element.poolName
                + "</td>" + "<td>" + formatDateFromMilliseconds(element.startTime)
                + "</td>" + "<td>" + formatDateFromMilliseconds(element.endTime)
                + "</td>" + "<td>" + tookTimeMs
                + "</td>" + "<td>" + element.exception
                + "</td>" + "<td>" + element.method + "</td>" + "</tr>";

            $("#sql_monitorTable").append(tableHtml);
            if (++count > curSqlPageSize) break;
        }
        $('#sql_monitorTable').trigger("update");
    }

    function formatDateFromMilliseconds(milliseconds) {
        var date = new Date(milliseconds);
        var year = date.getFullYear();
        var month = (date.getMonth() + 1).toString().padStart(2, '0');
        var day = date.getDate().toString().padStart(2, '0');
        var hours = date.getHours().toString().padStart(2, '0');
        var minutes = date.getMinutes().toString().padStart(2, '0');
        var seconds = date.getSeconds().toString().padStart(2, '0');
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    }

    //$("#ds_refresh_button").trigger("click");
    //$("#sql_refresh_button").trigger("click");
    getDsListFromServer();
    getSqlListFromServer();
});