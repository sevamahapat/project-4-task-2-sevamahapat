// Author: Seva Mahapatra(smahapat@andrew.cmu.edu)
// imports necessary Java libraries
package com.example.project4webservice;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

@WebServlet(name = "StockServlet", urlPatterns = {"/api", "/dashboard"})
public class StockServlet extends HttpServlet {
    // MongoDB connection string and settings
    String connectionString = "mongodb://smahapat:Smahapat1998@ac-4wlphe0-shard-00-00.bij0npa.mongodb.net:27017,ac-4wlphe0-shard-00-01.bij0npa.mongodb.net:27017,ac-4wlphe0-shard-00-02.bij0npa.mongodb.net:27017/test?w=majority&retryWrites=true&tls=true&authMechanism=SCRAM-SHA-1";

    // Create a ServerApi object
    ServerApi serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build();

    // Create a MongoClientSettings object and apply the connection string and serverApi
    MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(connectionString))
            .serverApi(serverApi)
            .build();

    public void init() { }
    // doGet method to handle GET requests
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("doGet method called");
        // Identify the request path and route to the appropriate method
        String requestURL = request.getServletPath();
        if (requestURL.equals("/api")) {
            fetchAPIData(request, response);
        } else if (requestURL.equals("/dashboard")) {
            fetchLogDataForDashboard(request, response);
        }
    }
    // Method to fetch API data based on request parameters
    public void fetchAPIData(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("fetchAPIData method called");
        JsonObject jsonObject = null;
        String requestTimestamp = String.valueOf(System.currentTimeMillis());
        // api key
        String apiKey = "i8DFRmzx9ZzDghdpNGGQQQnByI5FlvcZ";
        // stocks ticker parameter
        String stocksTicker = request.getParameter("symbol");
        // date
        String date = request.getParameter("date");
        String params = stocksTicker + " " + date;
        // api response timestamp
        String responseTimestamp = "";
        // store the api response data
        StringBuilder apiResponse = new StringBuilder();
        // store the api response data
        StringBuilder apiResponse2 = new StringBuilder();
        // api endpoint
        String apiUrl1 = "https://api.polygon.io/v1/open-close/" + stocksTicker + "/" + date + "?adjusted=true&apiKey=" + apiKey;
        String apiUrl2 = "https://api.polygon.io/v2/reference/news?ticker=" + stocksTicker + "&published_utc=" + date + "&order=desc&limit=5&sort=published_utc&apiKey=" + apiKey;
        try {
            // Create a URL for the api endpoint
            URL url = new URL(apiUrl1);
            // Create an HttpURLConnection object
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Set the HTTP method
            connection.setRequestMethod("GET");
            // Send the request
            connection.connect();
            // Get the response code
            int responseCode = connection.getResponseCode();

            // Check the response code
            if (responseCode == 200) {
                // to read the contents of the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                // read each line of the response and store it in the apiResponse string
                while ((line = reader.readLine()) != null) {
                    apiResponse.append(line);
                }
                // close the reader
                reader.close();
            } else {
                apiResponse = null;
            }
            // Disconnect the connection
            connection.disconnect();
            // Create a URL for second api endpoint
            url = new URL(apiUrl2);
            // Create an HttpURLConnection object
            connection = (HttpURLConnection) url.openConnection();
            // Set the HTTP method
            connection.setRequestMethod("GET");
            // Send the request
            connection.connect();
            // Get the response code
            responseCode = connection.getResponseCode();

            // Check the response code
            if (responseCode == 200) {
                // to read the contents of the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                // read each line of the response and store it in the apiResponse string
                while ((line = reader.readLine()) != null) {
                    apiResponse2.append(line);
                }
                // close the reader
                reader.close();
            } else {
                apiResponse2 = null;
            }
            // Disconnect the connection
            connection.disconnect();
            responseTimestamp = String.valueOf(System.currentTimeMillis());
            if (apiResponse != null) {
                // Create a new Gson object
                Gson gson = new Gson();
                // Convert the first api response string to a JsonObject
                jsonObject = gson.fromJson(String.valueOf(apiResponse), JsonObject.class);
                if (apiResponse2 != null) {
                    // Convert the second api response string to a JsonObject
                    JsonObject jsonObject2 = gson.fromJson(String.valueOf(apiResponse2), JsonObject.class);
                    System.out.println(jsonObject);
                    System.out.println(jsonObject2);
                    jsonObject.add("news", getNewsArray(jsonObject2));
                }
                // log the data
                logData(params, apiUrl1, jsonObject, requestTimestamp, responseTimestamp);
            }
            // Setting the response content type to JSON
            response.setContentType("application/json");

            // Writing the JSON response to the PrintWriter
            PrintWriter out = response.getWriter();
            out.println(jsonObject);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Method to fetch news array from the JSON object
    public JsonArray getNewsArray(JsonObject jsonObject) {
        // Creating a JSON array to hold the news items
        JsonArray newsArray = new JsonArray();
        // Iterate through the 'results' array in your jsonResponse
        if (jsonObject.has("results")) {
            JsonArray results = jsonObject.getAsJsonArray("results");
            for (int i = 0; i < results.size(); i++) {
                // Getting the news item at index 'i'
                JsonObject newsItem = results.get(i).getAsJsonObject();

                // Creating a new JSON object for each news item with only the required fields
                JsonObject refinedItem = new JsonObject();
                refinedItem.add("Title", newsItem.get("title"));
                refinedItem.add("Author", newsItem.get("author"));
                refinedItem.add("Timestamp", newsItem.get("published_utc"));
                refinedItem.add("description", newsItem.get("description"));

                // Adding this refined item to the news array
                newsArray.add(refinedItem);
            }
        }
        return newsArray;
    }
    // Method to log API data to MongoDB
    public void logData(String requestParams, String apiUrl, JsonObject apiResponse, String requestTimestamp, String responseTimestamp) {
        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                // Send a ping to confirm a successful connection
                MongoDatabase database = mongoClient.getDatabase("test");
                database.runCommand(new Document("ping", 1));
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
                MongoCollection<Document> collection = database.getCollection("logs");

                // Write the string to the database by appending all useful information
                collection.insertOne(new Document()
                        .append("_id", new ObjectId())
                        .append("Stock Ticker", requestParams.split(" ")[0])
                        .append("Date", requestParams.split(" ")[1])
                        .append("API URL", apiUrl)
                        .append("Opening Prices", apiResponse.get("open").getAsDouble())
                        .append("Closing Prices", apiResponse.get("close").getAsDouble())
                        .append("Highest Prices", apiResponse.get("high").getAsDouble())
                        .append("Lowest Prices", apiResponse.get("low").getAsDouble())
                        .append("Trading Volumes", apiResponse.get("volume").getAsLong())
                        .append("Request Timestamp", formatDate(requestTimestamp))
                        .append("Response Timestamp", formatDate(responseTimestamp))
                        .append("Latency in ms", Long.parseLong(responseTimestamp) - Long.parseLong(requestTimestamp)));
            } catch (MongoException e) {
                e.printStackTrace();
            }
        }
    }
    // Method to fetch log data for the dashboard
    public void fetchLogDataForDashboard(HttpServletRequest request, HttpServletResponse response) {
        // Connecting to MongoDB and fetching log data
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            // Get the logs collection from the test database
            MongoDatabase database = mongoClient.getDatabase("test");
            MongoCollection<Document> collection = database.getCollection("logs");
            // Get all the documents from the collection
            List<Document> logs = collection.find().into(new ArrayList<>());
            // Create a list to store the latency values and ticker values
            List<Long> latencyValues = new ArrayList<>();
            List<String> tickerValues = new ArrayList<>();
            // Extract the values of the specific columns from each document
            for (Document log : logs) {
                if (log.containsKey("Latency in ms")) {  // Check if the key exists
                    Long value = log.getLong("Latency in ms");
                    latencyValues.add(value);
                }
                if (log.containsKey("Stock Ticker")) {
                    String value = log.getString("Stock Ticker");
                    tickerValues.add(value);
                }
            }
            // Calculate the average latency
            long sum = 0;
            for (Long value : latencyValues) {
                sum += value;
            }
            double avg_latency = (double) sum / latencyValues.size();

            // Get the most searched ticker
            String most_searched_ticker = getMostSearchedTicker(tickerValues);

            // Get the documents for the most searched ticker
            Bson filter = Filters.eq("Stock Ticker", most_searched_ticker);
            FindIterable<Document> documents = collection.find(filter);

            double highestPrice = 0.0;
            String highestPriceDate = "";
            Long maxVolume = 0L;
            String maxVolumeDate = "";
            double lowestPrice = Double.MAX_VALUE;
            String lowestPriceDate = "";

            // Iterate through the documents to find the highest price, highest volume and lowest price
            MongoCursor<Document> cursor = documents.iterator();
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                double currentPrice = doc.getDouble("Highest Prices");
                double currentLowPrice = doc.getDouble("Lowest Prices");
                String currentDate = doc.getString("Date");
                Long currentVolume = doc.getLong("Trading Volumes");

                // Check if the current price is greater than the highest price found so far
                if (currentPrice > highestPrice) {
                    highestPrice = currentPrice;
                    highestPriceDate = currentDate;
                }
                // Check if the current volume is greater than the highest volumes found so far
                if (currentVolume > maxVolume) {
                    maxVolume = currentVolume;
                    maxVolumeDate = currentDate;
                }
                // Check if the current price is less than the lowest price found so far
                if (currentLowPrice < lowestPrice) {
                    lowestPrice = currentLowPrice;
                    lowestPriceDate = currentDate;
                }
            }

            // Set the request attributes
            request.setAttribute("logs", logs);
            request.setAttribute("avg_latency", String.format("%.2f", avg_latency));
            request.setAttribute("top_searched", most_searched_ticker);
            request.setAttribute("highest_volumes", maxVolume);
            request.setAttribute("highest_volume_date", maxVolumeDate);
            request.setAttribute("highest_price", String.format("%.2f", highestPrice));
            request.setAttribute("highest_price_date", highestPriceDate);
            request.setAttribute("lowest_price", String.format("%.2f", lowestPrice));
            request.setAttribute("lowest_price_date", lowestPriceDate);
            // Forward the request to 'result.jsp'
            RequestDispatcher dispatcher = request.getRequestDispatcher("result.jsp");
            dispatcher.forward(request, response);

            // Handle exceptions
        } catch (MongoException | ServletException | IOException e) {
            e.printStackTrace();
        }
    }
    // Method to get the most searched ticker
    private static String getMostSearchedTicker(List<String> values) {
        // Creating a HashMap to store the frequency of occurrence each ticker
        Map<String, Integer> freqMap = new HashMap<>();
        // Iterate through the list of tickers and store the frequency of occurrence in the HashMap
        for (String value : values) {
            freqMap.put(value, freqMap.getOrDefault(value, 0) + 1);
        }
        String mostSearched = null;
        int maxFreq = 0;
        // Iterate through the HashMap to find the ticker with the highest frequency of occurrence
        for (Map.Entry<String, Integer> entry : freqMap.entrySet()) {
            if (entry.getValue() > maxFreq) {
                maxFreq = entry.getValue();
                mostSearched = entry.getKey();
            }
        }
        return mostSearched;
    }
    // Method to format the timestamp (taken from ChatGPT)
    private static String formatDate(String timestamp) {
        // Convert the string to a long
        long timestamp_modified = Long.parseLong(timestamp);

        // Create a date object from the timestamp
        Date date = new Date(timestamp_modified);

        // Create a SimpleDateFormat instance with your desired format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Format the date to a readable string
        String formattedDate = sdf.format(date);

        return formattedDate;
    }
}