<%-- 
    Document   : index
    Created on : Aug 7, 2008, 4:31:15 PM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<%@ page import="org.jdesktop.wonderland.runner.Runner" %>

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>

<html>
<head>    
<link href="runner.css" rel="stylesheet" type="text/css" media="screen" />
<script src="/wonderland-web-front/javascript/prototype-1.6.0.3.js" type="text/javascript"></script>
<script type="text/javascript">
    var pe;
    
    function updateServices() {
        new Ajax.Request('services/list', { 
            method:'get', 
            requestHeaders: { Accept:'application/json' },
            onSuccess: function(response){
                var services = response.responseText.evalJSON(true);
                if (services.service.length > 1) {
                    for (var i = 0; i < services.service.length; i++) {
                        updateService(services.service[i], i);
                    }
                } else {
                    updateService(services.service, 0);
                }
            }
        });
    }
    
    function updateService(service, index) {
        processStatus(service);
        
        var row = $('runnerTable').down('tr', index + 3);
        if (row == null) {
            row = new Element('tr');
            row.insert(new Element('td', { 'class': 'installed' }));
            row.insert(new Element('td', { 'class': 'installed' }));
            row.insert(new Element('td', { 'class': 'installed' }));
            row.insert(new Element('td', { 'class': 'installed' }));
            $('runnerTable').insert(row);
        }
        
        row.down('td', 0).update(service.name);
        row.down('td', 1).update(service.location);
        row.down('td', 2).update(service.status_text);
        
        var actions = row.down('td', 3);
        actions.update();
        for (var i = 0; i < service.link.length; i++) {
            actions.insert(service.link[i]);
            actions.insert(' ');
        }
    }
    
    function processStatus(service) {
        switch (service.status) {
             case 'NOT_RUNNING':
                service.status_text = 'Not Running';
                service.link = [ new Element('a', { 'href': 'javascript:void(0);',
                                                  'onclick': 'setStatus(\'' + service.name + '\', \'start\')' }).update("start") ];
                break;
             case 'STARTING_UP':
                service.status_text = 'Starting Up';
                service.link = [ new Element('a', { 'href': 'javascript:void(0);',
                                                     'onclick': 'setStatus(\'' + service.name + '\', \'stop\')' }).update("stop") ];
                break;
             case 'RUNNING':
                service.status_text = 'Running';
                service.link = [ new Element('a', { 'href': 'javascript:void(0);',
                                                     'onclick': 'setStatus(\'' + service.name + '\', \'stop\')' }).update("stop"),
                                                                                
                                 new Element('a', { 'href': 'javascript:void(0);',
                                                     'onclick': 'setStatus(\'' + service.name + '\', \'restart\')' }).update("restart") ];
                break;
                
             default:
                service.status_text = service.status;
                service.link = [];
        }
        
        service.link.push(new Element('a', { 'href': '/wonderland-web-front/admin?pageURL=/wonderland-web-runner/run%3faction=edit%26name=' + service.name,
                                             'target': '_top'}).update("edit"));

        // if the service isn't runnable, remove the previous links
        if (service.runnable != "true") {
            service.link = [];
        }
        
        if (service.hasLog) {
            service.link.push(new Element('a', { 'href': '/wonderland-web-front/admin?pageURL=/wonderland-web-runner/run%3faction=log%26name=' + service.name,
                                                 'target': '_top'}).update("log"));
        }
    }
    
    function setStatus(service, action) {
        new Ajax.Request('services/runner/' + service + "/" + action, {
            method:'get', 
            requestHeaders: { Accept:'application/json' },
            onSuccess: function(response){
                updateServices();
            }
        });
    }
    
    function setUpdatePeriod(period) {
        if (pe) { pe.stop(); }
        if (period > 0) {
            pe = new PeriodicalExecuter(updateServices, period);
        }
    
        // clear the list
        $('periods').update("refresh:");
        
        var times = [0, 15, 60];
        for (var i = 0; i < times.length; i++) {
            var timeStr = times[i] + " sec.";
            if (times[i] == 0) {
                timeStr = "none";
            }
            
            if (times[i] == period) {
                $('periods').insert(timeStr);    
            } else {
                $('periods').insert(new Element('a', { 'href': 'javascript:void(0);',
                                                'onclick' : 'setUpdatePeriod(' + times[i] +')'}
                                               ).update(timeStr));
            }
            
            $('periods').insert(' ');
        }
    }
</script>
</head>
<body onload="updateServices(); setUpdatePeriod(15);">
<h1>Wonderland Server Status</h1>

<table class="installed" id="runnerTable">
    <tr>
        <td colspan="3">
            <table>
                <tr><td>
                    <h3>Server Components</h3>
                </td><td>
                     (<a href="/wonderland-web-front/admin?pageURL=/wonderland-web-runner/run%3faction=editRunners" target="_top">edit</a>)
                </td></tr>
            </table>
        </td>
        <td class="refresh" id="periods"></td>
    </tr>
    <tr class="header">
        <td class="installed"><b>Name</b></td>
        <td class="installed"><b>Location</b></td>
        <td class="installed"><b>Status</b></td>
        <td class="installed"><b>Actions</b></td>
    </tr>
</table>    

<a href="javascript:void(0);" onclick="setStatus('all', 'stop')">Stop all</a>
<a href="javascript:void(0);" onclick="setStatus('all', 'start')">Start all</a>
<a href="javascript:void(0);" onclick="setStatus('all', 'restart')">Restart all</a>

</body>