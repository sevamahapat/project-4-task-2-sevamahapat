// Author: Seva Mahapatra(smahapat@andrew.cmu.edu)
// imports necessary Java libraries
package ds.edu.cmu;

import android.app.Activity;
import android.os.Build;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetData {
    MainActivity ma = null;  // Declaration of MainActivity object for callback purposes
    String stockTicker = null;   // Stores the stock ticker to be sent to the web service
    String date = null;    // Stores the date to be sent to the web service
    String response = null;   // Stores the response from the web service

    // fetchData method to initialize values and start the background task
    public void fetchData(String stockTicker, String date, Activity activity, MainActivity ma) {
        this.ma = ma;  // Assigning the MainActivity object
        this.stockTicker = stockTicker;  // Setting the stock ticker
        this.date = date;  // Setting the date
        // Starting the background task to fetch data
        new BackgroundTask(activity).execute();
    }
    // Inner class BackgroundTask to handle network operations on a separate thread
    private class BackgroundTask {

        private Activity activity; // The UI thread
        // Constructor for BackgroundTask
        public BackgroundTask(Activity activity) {
            this.activity = activity;
        }
        // Method to start the background thread
        private void startBackground() {
            new Thread(new Runnable() {
                public void run() {
                    // Performing network operations in the background
                    doInBackground();
                    // This is magic: activity should be set to MainActivity.this
                    // then this method uses the UI thread
                    activity.runOnUiThread(new Runnable() {
                        // Method to handle post execution tasks
                        public void run() {
                            onPostExecute();
                        }
                    });
                }
            }).start();
        }
        // Method to start the background process
        private void execute(){
            // Calling startBackground to initiate the thread
            startBackground();
        }
        // Background task to fetch data
        // Fetching data and storing the response
        private void doInBackground() {
            response = fetchData(stockTicker, date);
        }
        // Method called after the background task completes
        // Calling MainActivity's method with the response data
        public void onPostExecute() {
            ma.pageReady(response);
        }

        /*
         * Method to fetch data from the server based on the stock ticker and date
         * Returns a String response from the server
         */
        private String fetchData(String stockTicker, String date) {
            try {
                // Creating a URL with the stock ticker and date parameters
                URL url = new URL("https://super-memory-qr9974vwvq4cxr74-8080.app.github.dev/api?symbol=" + stockTicker + "&date=" + date);
                // Opening an HttpURLConnection for the URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                // Setting the request method to GET
                connection.setRequestMethod("GET");

                // Getting the response code from the server
                int responseCode = connection.getResponseCode();
                // Checking if the response code is OK
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Reader to read the response from the server
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    // Reading the server's response
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();  // Closing the reader
                    System.out.println(response);
                    return response.toString();  // Returning the response as a string
                } else {
                    // Returning an error message if response code is not OK
                    return "Error: Server responded with code " + responseCode;
                }

                // Handling exceptions
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
