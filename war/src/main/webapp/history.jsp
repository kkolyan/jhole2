<%@ page import="net.kkolyan.jhole2.war.dual.SessionManager" %>
<%@ page import="net.kkolyan.jhole2.war.dual.SessionController" %>
<%@ page import="net.kkolyan.jhole2.remoting.LocalRawEndpoint" %>
<%@ page import="net.kkolyan.jhole2.socket.SocketConnection" %>
<%@ page import="net.kkolyan.jhole2.monitoring.Monitoring" %>
<%@ page import="net.kkolyan.jhole2.monitoring.HistoryEntry" %>
<%@ page import="net.kkolyan.jhole2.monitoring.HistorySection" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html><head>

    <style type="text/css">
        body {
            margin: 0;
        }
        table {
            width: 100%;
            border-collapse: collapse;
        }

        .black-bottom td {
            border-top: solid 1px black;
        }

        td, th {
            border: solid 1px #CCC;
            border-bottom: none;
        }

        table form {
            display: inline;
        }

        ul.error {
            border: red 3px solid;
            font-weight: bold;
        }

        .weak {
            padding: 0 10px;
            margin: 0;
            color: #CCC;
        }

        td label {
            margin: 0;
        }

        td {
            padding: 0 5px;
        }

        a.selected {
            font-weight: bold;
            background: #e1e1e1;
            padding: 0 5px;
        }
    </style>
</head>
<body>

<%
    Monitoring monitoring = (Monitoring) application.getAttribute(Monitoring.class.getName());
%>

<%
    for (HistoryEntry entry: monitoring.getEntries()) {
        for (HistorySection section: entry.getSections()) {

        }
    }
%>
<table>
    <%int i = 0;%>
    <% for (HistoryEntry entry: monitoring.getEntries()) {%>
    <tr class="black-top">
        <td colspan="3"><%=entry.getName()%></td>
    </tr>
    <% for (HistorySection section: entry.getSections()) {%>
    <tr>
        <td>
            <a target="display" href="/monitoring/<%=i++%>/<%=section.getName()%>.txt"><%=section.getName()%></a>
        </td>
        <td><%=section.getContent().getBytes().length%></td>
        <td><%
        int lb = section.getContent().indexOf('\n');
            if (lb > 0 && lb < 100) {
                %><%=section.getContent().substring(0, lb)%><%
            }
        %>
        </td>
    </tr>
    <% } %>
    <% } %>
</table>
</body>
</html>