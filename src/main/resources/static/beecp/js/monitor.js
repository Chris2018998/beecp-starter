var dsURL = 'getDataSourceList';
var sqlURL = 'getSqlTraceList';
var clearURL = 'clearPool';
var interruptURL = 'interruptPool';
var language = $("html").attr("lang");

function poolClear(dsId){
    $.ajax({
        type: 'POST',
        url: clearURL,
        dataType: 'json',
        contentType: 'application/json',
        data: JSON.stringify({'dsId':dsId}),
        success: function(data) {
           if(data.code==1) {
               //alert(language=='cn'? '清理成功':'Clear success');
               $('#ds_refresh_button').trigger("click");
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
                                var mode = element.poolMode;
                                var state = element.poolState;
                                var creatingCount = element.creatingCount;
                                var creatingTimeoutCount = element.creatingTimeoutCount;
                                var clearButtonDesc;
                                var interruptButtonDesc;

                                if (language == 'cn') {
                                    clearButtonDesc='重置';
                                    interruptButtonDesc='中断';
                                    mode = (mode == 'compete') ? '竞争' : '公平';
                                    if (state == 0) state = "未初始化";
                                    else if (state == 1) state = "启动中";
                                    else if (state == 2) state = "已就绪";
                                    else if (state == 3) state = "关闭中";
                                    else if (state == 4) state = "已关闭";
                                    else if (state == 5) state = "清理中";
                                } else {
                                   clearButtonDesc='Clear';
                                   interruptButtonDesc='Interrupt';
                                   if (state == 0) state = "uninitialized";
                                   else if (state == 1) state = "starting";
                                   else if (state == 2) state = "ready";
                                   else if (state == 3) state = "closing";
                                   else if (state == 4) state = "closed";
                                   else if (state == 5) state = "clearing";
                                }

                                var tableHtml = "<tr>" + "<td>" + element.dsId + "</td>"
                                    + "<td>" + mode + "</td>" + "<td>" + state + "</td>"
                                    + "<td>" + element.poolMaxSize + "</td>"
                                    + "<td>" + element.idleSize + "</td>"
                                    + "<td>" + element.usingSize + "</td>"
                                    + "<td>" + element.semaphoreWaitingSize + "</td>"
                                    + "<td>" + element.transferWaitingSize + "</td>"
                                    + "<td>" + creatingCount + "</td>"
                                    + "<td>" + creatingTimeoutCount + "</td>"
                                    + "<td><input id='pool_clear_button' onclick='poolClear(\""+element.dsId+"\")' type='button' value='"+clearButtonDesc+"'/>";
                                    if(creatingTimeoutCount >0)
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

            if (element.endTimeMs>0) {
                if (!element.successInd) { //fail
                    bgcolor = " class='sqlExecFail'";
                } else if (element.slowInd) { //slow
                    bgcolor = " class='sqlExecSlow'";
                }
            }

            var tableHtml = "<tr " + bgcolor + ">" + "<td>"
                + element.sql + "</td>" + "<td>" + element.dsId
                + "</td>" + "<td>" + element.startTime
                + "</td>" + "<td>" + element.endTime
                + "</td>" + "<td>" + element.tookTimeMs
                + "</td>" + "<td>" + element.successInd
                + "</td>" + "<td>" + element.statementType + '.' + element.methodName + "</td>" + "</tr>";
            $("#sql_monitorTable").append(tableHtml);
            if (++count > curSqlPageSize) break;
        }
        $('#sql_monitorTable').trigger("update");
    }
    //$("#ds_refresh_button").trigger("click");
    //$("#sql_refresh_button").trigger("click");
    getDsListFromServer();
    getSqlListFromServer();
});