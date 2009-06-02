<%-- 
    Document   : index
    Created on : Aug 7, 2008, 4:31:15 PM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>

<html>
    <head>
        <link href="runner.css" rel="stylesheet" type="text/css" media="screen" />
    </head>
    <body>
        <h3>Edit ${requestScope['entry'].runnerName}</h3>
        <form action="run">
            <input type="hidden" name="action" value="editForm"/>
            <input type="hidden" name="name" value="${requestScope['entry'].runnerName}"/>
            <input type="hidden" name="class" value="${requestScope['entry'].runnerClass}">
            <input type="hidden" name="location" value="${requestScope['entry'].location}">

            <table>
                <tr>
                    <td><b>Name:</b></td>
                    <td>${requestScope['entry'].runnerName}</td>
                </tr>
                <tr>
                    <td>Class:</td>
                    <td>${requestScope['entry'].runnerClass}</td>
                </tr>
                <tr>
                    <td>Location:</td>
                    <td>${requestScope['entry'].location}</td>
                </tr>
                <tr><td colspan="2" style="height:2em;"></td></tr>
                
                <tr>
                    <td colspan="2"><b>Properties</b></td>
                </tr>
                
                <tr>
                  <td colspan="2">
                    <table>
                        <c:forEach var="prop" items="${requestScope['entry'].runProps}"
                                   varStatus="propStat">
                            <tr>
                                <td><input type="text" name="key-${propStat.count}" value="${prop.key}"/></td>
                                <td><input type="text" name="value-${propStat.count}" value="${prop.value}"/></td>
                            </tr>
                        </c:forEach>
                            <tr>
                                <td><input type="text" name="key-new"/></td>
                                <td><input type="text" name="value-new"/></td>
                            </tr>
                            
                            <tr>
                                <td></td>
                                <td><input type="submit" name="button" value="Add Property"/>
                                    <input type="submit" name="button" value="Restore Defaults"/>
                                </td>
       
                            </tr>
                    </table>
                  </td>
                </tr>
                
            </table>    
            <tr><td colspan="2" style="height:2em;"></td></tr>
            <tr>
                <td><input type="submit" name="button" value="Save"/>
                    <input type="submit" name="button" value="Cancel"/>
                </td>
            </tr>  
        </form>
    </body>
</html>