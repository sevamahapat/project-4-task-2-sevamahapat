// Author: Seva Mahapatra(smahapat@andrew.cmu.edu)
// imports necessary Java libraries
package ds.edu.cmu;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.InputFilter;
import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import ds.edu.cmu.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    // Instance of MainActivity for usage within inner classes or methods
    MainActivity me = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call the super class's onCreate method with the saved state
        super.onCreate(savedInstanceState);
        // Set the user interface layout for this activity
        setContentView(R.layout.activity_main);
        /*
         * The click listener will need a reference to this object, so that upon successfully getting results from web service, it
         * can callback to this object with the resulting JSON response.
         */
        final MainActivity ma = this;
        /*
         * Find the "submit" button, and add a listener to it
         */
        Button submitButton = (Button)findViewById(R.id.submit);
        // Finding the EditText for symbol input and applying a filter to make input all caps
        EditText editText = findViewById(R.id.symbol);
        // refernce: https://stackoverflow.com/questions/15961813/in-android-edittext-how-to-force-writing-uppercase
        editText.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

        // Add a listener to the send button
        submitButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View viewParam) {
                // Getting the stock ticker and date from user input
                String stockTicker = ((EditText)findViewById(R.id.symbol)).getText().toString();
                String date = ((EditText)findViewById(R.id.date)).getText().toString();
                System.out.println("searchTerm = " + stockTicker + ", " + date);
                // Creating an instance of GetData to fetch data
                GetData gd = new GetData();
                gd.fetchData(stockTicker, date, me, ma); // Done asynchronously in another thread.  It calls ma.pageReady() in this thread when complete.
            }
        });
    }

    // Method pageReady to handle the response from GetData
    public void pageReady(String response) {
        // Finding and setting up UI elements for displaying results
        TableLayout tableView = (TableLayout)findViewById(R.id.table);
        TableLayout newsTableView = (TableLayout)findViewById(R.id.newsTable);
        String symbol = ((EditText)findViewById(R.id.symbol)).getText().toString();
        TextView feedbackTextView = (TextView)findViewById(R.id.textView);
        TextView textView = (TextView)findViewById(R.id.textView2);
        // Checking if the response is not null
        if (!response.equals("null")) {
            try {
                // Parsing the JSON response
                JSONObject jsonResponse = new JSONObject(response);
                // Getting the TableLayout from the layout
                TableLayout table = findViewById(R.id.table);
                TableLayout newsTable = findViewById(R.id.newsTable);

                // Clear the table for fresh data
                table.removeAllViews();
                newsTable.removeAllViews();

                // Iterating over keys in the JSON object
                Iterator<String> keys = jsonResponse.keys();
                while(keys.hasNext()) {
                    String key = keys.next();
                    // Skipping "status" and "news" keys
                    if(key.equalsIgnoreCase("status") || key.equalsIgnoreCase("news")) {
                        continue; // Skip the rest of the loop and continue with the next iteration
                    }

                    String value = jsonResponse.getString(key); // get the value for each key

                    // code to create dynamic table (taken from ChatGPT)
                    TableRow dataRow = new TableRow(this);
                    addTextToRowWithStyle(dataRow, key.toUpperCase()); // add the key as the first cell
                    addTextToRow(dataRow, value); // add the value as the second cell
                    table.addView(dataRow); // add the TableRow to the TableLayout
                }
                // Handling "news" key in JSON response
                if (jsonResponse.has("news")) {
                    // Getting the news array from JSON
                    JSONArray results = jsonResponse.getJSONArray("news");
                    for (int i = 0; i < results.length(); i++) {
                        // Getting each news item as JSON object
                        JSONObject newsItem = results.getJSONObject(i);
                        System.out.println(newsItem.toString());
                        // Iterating over keys in newsItem
                        Iterator<String> keySet = newsItem.keys();
                        while(keySet.hasNext()) {
                            String key = keySet.next();
                            String value = newsItem.getString(key); // get the value for each key
                            TableRow dataRow = new TableRow(this);
                            addTextToRowWithStyle(dataRow, key); // add the key as the first cell
                            addTextToRow(dataRow, value); // add the value as the second cell
                            newsTable.addView(dataRow); // add the TableRow to the TableLayout
                        }
                    }
                }
                // Handle JSON parsing error
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Setting visibility of tableView and newsTableView to visible
            tableView.setVisibility(View.VISIBLE);
            newsTableView.setVisibility(View.VISIBLE);
            // Setting text of feedbackTextView and textView
            feedbackTextView.setText("Here are the details of stock: " + symbol);
            textView.setText("Top 5 recent news on stock: " + symbol);
        } else {
            // Handling case where response is null
            System.out.println("No data");
            tableView.setVisibility(View.INVISIBLE);
            newsTableView.setVisibility(View.INVISIBLE);
            feedbackTextView.setText("Sorry, I could not find any details of stock: " + symbol);
            textView.setText("");
        }
    }
    // Method to add text to a TableRow with style
    private void addTextToRowWithStyle(TableRow row, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(5, 5, 5, 5);
        row.addView(tv);
    }

    // Method to add text to a TableRow
    private void addTextToRow(TableRow row, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(5, 5, 5, 5);
        row.addView(tv);
    }

}