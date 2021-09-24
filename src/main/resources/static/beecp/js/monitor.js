var poolURL = getContextPath() + '/beecp/getPoolList';
var sqlURL = getContextPath() + '/beecp/getSqlTraceList';

function getContextPath() {
    var webFullPath = window.document.location.href;
    var strPath = window.document.location.pathname;
    var pos = webFullPath.indexOf(strPath);
    var webHostPath = webFullPath.substring(0, pos);
    var projectName = strPath.substring(0, strPath.substr(1).indexOf("/") + 1);
    return (webHostPath + projectName);
}

var sqlTraceList = [];//empty array
var curSqlPageSize = 10;
var curSqlPageNo = 1;
var maxSqlPageNo = 0;

function afterLoadSqlTraceList(data) {//after get result from server
    if (data) {
        sqlTraceList = data;
        $("#total-sql").val(sqlTraceList.length);
        maxSqlPageNo = parseInt(sqlTraceList.length / curSqlPageSize);
        if (data.length % curSqlPageSize > 0) maxSqlPageNo++;
        if (data.length > 0) showSqlTracePage();
    }
}

function showSqlTracePage() {//show sql page List
    var startIndex = (curSqlPageNo - 1) * curSqlPageSize;
    var endIndex = sqlTraceList.length;
    $("#sql-monitorTable tr:not(:first)").remove();

    if (maxSqlPageNo > 1) {
        if (curSqlPageNo == 1) {//at first page
            $("#sql-first").attr("disabled", true);
            $("#sql-pre").attr("disabled", true);
            $("#sql-next").attr("disabled", false);
            $("#sql-last").attr("disabled", false);
        } else if (curSqlPageNo == maxSqlPageNo) {//at end page
            $("#sql-first").attr("disabled", false);
            $("#sql-pre").attr("disabled", false);
            $("#sql-next").attr("disabled", true);
            $("#sql-last").attr("disabled", true);
        } else {//at middle page
            $("#sql-first").attr("disabled", false);
            $("#sql-pre").attr("disabled", false);
            $("#sql-next").attr("disabled", false);
            $("#sql-last").attr("disabled", false);
        }
    }

    var count = 0;
    for (var i = startIndex; i < endIndex; i++) {
        var element = sqlTraceList[i];
        var bgcolor = "";

        if (element.execInd) {
            if (!element.execSuccessInd) {//fail
                bgcolor = " class='sqlExecFail'";
            } else if (element.execSlowInd) {//slow
                bgcolor = " class='sqlExecSlow'";
            }
        }

        var tableHtml = "<tr " + bgcolor + ">" +
            "<td>" + element.sql + "</td>" +
            "<td>" + element.dsId + "</td>" +
            "<td>" + element.execStartTime + "</td>" +
            "<td>" + element.execEndTime + "</td>" +
            "<td>" + element.execTookTimeMs + "</td>" +
            "<td>" + element.execSuccessInd + "</td>" +
            "<td>" + element.statementType + '.' + element.methodName + "</td>" +
            "</tr>";
        $("#sql-monitorTable").append(tableHtml);
        if (++count > curSqlPageSize) break;
    }
    $('#sql-monitorTable').trigger("update");
}



