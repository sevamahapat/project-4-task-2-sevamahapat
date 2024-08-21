<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="org.bson.Document" %>
<!DOCTYPE html>
<html>
<head>
    <title>Web Service Dashboard</title>
    <style>
        table, th, td {
            border: 1px solid black;
            border-collapse: collapse;
        }
        th, td {
            padding: 10px;
        }
    </style>
</head>
<body>
<h2>Operation Analytics</h2>
<p><b>Average search latency: </b><%= request.getAttribute("avg_latency") + " ms" %></p>
<p><b>Top searched stock: </b><%= request.getAttribute("top_searched") %></p>
<h4> Statistics of most searched stock:</h4>
<p><b>Highest price was $<%= request.getAttribute("highest_price") %> on <%= request.getAttribute("highest_price_date") %></b></p>
<p><b>Lowest price was $<%= request.getAttribute("lowest_price") %> on <%= request.getAttribute("lowest_price_date") %></b></p>
<p><b>Max Volumes Traded was <%= request.getAttribute("highest_volumes") %> on <%= request.getAttribute("highest_volume_date") %></b></p>

<% List<Document> logs = (List<Document>) request.getAttribute("logs");
    if (logs != null) { %>
<h2>Logs</h2>
<table>
    <tr>
        <th>API URL</th>
        <th>Stock Ticker</th>
        <th>Date</th>
        <th>Opening Prices</th>
        <th>Closing Prices</th>
        <th>Highest Prices</th>
        <th>Lowest Prices</th>
        <th>Trading Volumes</th>
        <th>Request Timestamp</th>
        <th>Response Timestamp</th>
        <th>Latency in ms</th>
    </tr>
    <% for (Document log : logs) { %>
    <tr>
        <td><%= log.getString("API URL") %></td>
        <td><%= log.getString("Stock Ticker") %></td>
        <td><%= log.getString("Date") %></td>
        <td><%= log.getDouble("Opening Prices") %></td>
        <td><%= log.getDouble("Closing Prices") %></td>
        <td><%= log.getDouble("Highest Prices") %></td>
        <td><%= log.getDouble("Lowest Prices") %></td>
        <td><%= log.getLong("Trading Volumes") %></td>
        <td><%= log.getString("Request Timestamp") %></td>
        <td><%= log.getString("Response Timestamp") %></td>
        <td><%= log.getLong("Latency in ms") %></td>
    </tr>
    <% } %>
</table>
<% } %>
</body>
</html>