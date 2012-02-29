<%-- 
    Document   : index
    Created on : Fri Aug 28 14:08:28 EDT 2009 @797 /Internet Time/
    Author     : gritchie
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    
<%@ page import="org.jdesktop.wonderland.runner.Runner" %>

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <link href="/wonderland-web-front/css/base.css" rel="stylesheet" type="text/css" media="screen" />
    <link href="/wonderland-web-front/css/module.css" rel="stylesheet" type="text/css" media="screen" />
    <link href="/wonderland-web-front/css/wonderland-theme/jquery-ui.css" rel="stylesheet" type="text/css"/>
    <script src="/wonderland-web-front/javascript/jquery.min.js"></script>
    <script src="/wonderland-web-front/javascript/jquery-ui.min.js"></script>
    <script type="text/javascript">
    var pe;
    var intervalID;
    var worldInfo;
    
    $(function() {   
        
        $("#dialog").dialog({
                    autoOpen: false,
                    resizable: false,
                    height: 280,
                    modal: true                                        
                });
        
        $("#progressbar").progressbar();
        
        
        updateServices();
        setUpdatePeriod(15);
    });
        
    function updateServices() {

        $.ajax({
            type: 'GET',
            url: 'services/list',
            dataType: 'json',
            success: function(data) {
                var services = data;
                if(services.service.length > 1) {
                    $(".servicerow").remove();
                    for(var i = 0; i < services.service.length; i++) {
                        
                        updateService(services.service[i], i);
                    }
                } else {
                    $(".servicerow").remove();
                    updateService(services.service, 0);
                }
            }
        });
    }
    
    function updateService(service, index) {
        processStatus(service);
        
        $("#runnerTable").append(constructServiceRow(service));
    }
    
    function processStatus(service) {
        service.link = new Array();
        switch (service.status) {
            
             case 'NOT_RUNNING':
                service.status_text = 'Not Running';
                                 
                service.link.push(constructServiceLink(service, "start"));
                break;
             case 'STARTING_UP':
                service.status_text = 'Starting Up';

                service.link.push( constructServiceLink(service, "stop"));
                break;
             case 'RUNNING':
                service.status_text = 'Running';

                service.link.push(constructServiceLink(service, "stop"));
                service.link.push(constructServiceLink(service, "restart"));
                break;
                
             default:
                service.status_text = service.status;
                service.link = [];
        }
        

          service.link.push( "<a href=\"/wonderland-web-front/admin?pageURL=/wonderland-web-runner/run%3faction=edit%26name="+service.name+"\" target=\"_top\">edit</a>");

        // if the service isn't runnable, remove the previous links
        if (service.runnable != "true") {
            service.link = [];
        }
        
        if (service.hasLog) {
            service.link.push(" <a href=\"/wonderland-web-front/admin?pageURL=/wonderland-web-runner/run%3faction=log%26name="+service.name+"\" target=\"_top\">log</a>");
        }
    }
    
    function setStatus(service, action) {
        
        $.ajax({
            type: 'GET',
            url: 'services/runner/'+service+'/'+action,
            dataType: 'json',
            success: function(data) {
                updateServices();
            }
        });
    }
    
    function setUpdatePeriod(period) {
        if (intervalID) { clearInterval(intervalID); }
        if (period > 0) {
            intervalID = setInterval(updateServices, period*1000);
        }
    
        // clear the list
        $("#periods").children().remove();
        $("#periods").text("refresh: ");
        var times = [0, 15, 60];
        for (var i = 0; i < times.length; i++) {
            var timeStr = times[i] + " sec.";
            if (times[i] == 0) {
                timeStr = "never";
            }
            
            if (times[i] == period) {
                $("#periods").append(timeStr);
                
            } else {
                var link = "<a href=\"javascript:void(0);\" onclick=\"setUpdatePeriod(\'"+times[i]+"\')\"'>"+timeStr+"</a>";
                $("#periods").append(link);
            }
            
            $("#periods").append(" ");
        }
    }
    
    function constructServiceRow(service) {
        var row = "<tr class=\"servicerow\" >";
        row += "<td class=\"installed\">"+service.name+"</td>";
        row += "<td class=\"installed\">"+service.location+"</td>";
        row += "<td class=\"installed\">"+service.status_text+"</td>";
        row += "<td class=\"installed\">";
        for(var i = 0; i < service.link.length; i++) {
            row += service.link[i];
        }
        row += "</td>";
        row += "</tr>";
        
        return row;
    }
    
    function constructServiceLink(service, link_text) {
        return "<a href=\"javascript:void(0);\" onclick=\"setStatus(\'"+service.name+"\',\'"+link_text+"\')\">"+link_text+"</a> ";
    }
    


   
</script>

    <title>Manage Server</title>
  </head>

  <body>
      <h2>Manage Server</h2>

      <table class="installed" id="runnerTable">
          <caption>
              <span class="refresh" id="periods"></span><span class="heading">Server Components</span> <a href="/wonderland-web-front/admin?pageURL=/wonderland-web-runner/run%3faction=editRunners" target="_top">(edit)</a>
          </caption>
          <tr class="header">
              <td class="installed">Name</td>
              <td class="installed">Location</td>
              <td class="installed">Status</td>
              <td class="installed">Actions</td>
          </tr>
      </table>

      <div id="actionLinks">
          <a href="javascript:void(0);" onclick="setStatus('all', 'stop')">Stop all</a>, <a href="javascript:void(0);" onclick="setStatus('all', 'start')">Start all</a>, <a href="javascript:void(0);" onclick="setStatus('all', 'restart')">Restart all</a>
      </div>
      <br/>
      <br />

      <c:forEach var="script" items="${requestScope['StatusPageScripts']}">          
          <c:import url="${script.url}" context="${script.context}"/>
      </c:forEach>
  </body>
</html>
