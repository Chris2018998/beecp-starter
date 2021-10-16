$(function() {
    var language = $("html").attr("lang");
    var dsURL = getContextPath() + '/beecp/getDataSourceList';
    var sqlURL = getContextPath() + '/beecp/getSqlTraceList';

    var sqlTraceList = []; //empty array
    var curSqlPageSize = 10;
    var curSqlPageNo = 1;
    var maxSqlPageNo = 0;
    var dsRefreshTask;
    var sqlRefreshTask;
    $('#ds_monitorTable').tablesorter();
    $('#sql_monitorTable').tablesorter();

    $("#ds_refresh_button").click(function() {
        getDataSourceListFromServer();
    });
    $("#sql_refresh_button").click(function() {
        getSqlListFromServer();
    });
	
	$("#ds_timer_button").click(function() {
        if (dsRefreshTask != null){//stop
		 	clearInterval(dsRefreshTask);
			dsRefreshTask =null;
			var name=(language=='cn')?'启动定时':'Run Timer';
			$("#ds_timer_button").val(name)
		}else{//run
			dsRefreshTask = setInterval(getDataSourceListFromServer, $("#ds_refresh_interval").val());
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
          dsRefreshTask = setInterval(getDataSourceListFromServer, $("#ds_refresh_interval").val());
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
                curSqlPageNo = 1;
                maxSqlPageNo = 0;
                sqlTraceList = [];
                $("#sql_first").attr("disabled", true);
                $("#sql_pre").attr("disabled", true);
                $("#sql_next").attr("disabled", true);
                $("#sql_last").attr("disabled", true);
                $("#sql_monitorTable tr:not(:first)").remove();
                afterLoadSqlTraceList(data);
            }
        });
    };

    function getDataSourceListFromServer() {
        $.ajax({
            type: 'POST',
            url: dsURL,
            dataType: 'json',
            success: function(data) {
                console.info(data);
                $("#ds_monitorTable tr:not(:first)").remove();
                if (data) {
                    $.each(data,
                        function(i, element) {
                            var mode = element.poolMode;
                            var state = element.poolState;

                            if (language == 'cn') {
                                mode = (mode == 'compete') ? '竞争': '公平';
                                if (state == 1) state = "未初始化";
                                else if (state == 2) state = "已启动";
                                else if (state == 3) state = "已关闭";
                                else if (state == 4) state = "重置中";
                            }

                            var tableHtml = "<tr>" + "<td>" + element.dsId + "</td>"
                                + "<td>" + mode + "</td>" + "<td>" + state + "</td>"
                                + "<td>" + element.maxActive + "</td>"
                                + "<td>" + element.idleSize + "</td>"
                                + "<td>" + element.usingSize + "</td>"
                                + "<td>" + element.semaphoreWaiterSize + "</td>"
                                + "<td>" + element.transferWaiterSize + "</td>" + "</tr>";
                            $("#ds_monitorTable").append(tableHtml);
                        });
                    $('#ds_monitorTable').trigger("update");
                }
            }
        });
    };

    function getContextPath() {
        var webFullPath = window.document.location.href;
        var strPath = window.document.location.pathname;
        var pos = webFullPath.indexOf(strPath);
        var webHostPath = webFullPath.substring(0, pos);
        var projectName = strPath.substring(0, strPath.substr(1).indexOf("/") + 1);
        return (webHostPath + projectName);
    }

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

            if (element.execInd) {
                if (!element.execSuccessInd) { //fail
                    bgcolor = " class='sqlExecFail'";
                } else if (element.execSlowInd) { //slow
                    bgcolor = " class='sqlExecSlow'";
                }
            }

            var tableHtml = "<tr " + bgcolor + ">" + "<td>"
                + element.sql + "</td>" + "<td>" + element.dsId
                + "</td>" + "<td>" + element.execStartTime
                + "</td>" + "<td>" + element.execEndTime
                + "</td>" + "<td>" + element.execTookTimeMs
                + "</td>" + "<td>" + element.execSuccessInd
                + "</td>" + "<td>" + element.statementType + '.' + element.methodName + "</td>" + "</tr>";
            $("#sql_monitorTable").append(tableHtml);
            if (++count > curSqlPageSize) break;
        }
        $('#sql_monitorTable').trigger("update");
    }

    $("#ds_refresh_button").trigger("click");
    $("#sql_refresh_button").trigger("click");
});