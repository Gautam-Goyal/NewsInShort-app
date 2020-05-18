package com.example.news_inshort;


import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    //Declaring Variables
    private ListView list_view;
    ArrayList<HashMap<String, String>> newsList;
    ArrayList<HashMap<String, Drawable>> imageList;

    //Variable for NewsAdapter
    private static NewsAdapter mAdapter;
    private static List<String> webPages = new ArrayList<>();
    TextView warningTV;
    ImageView warningIV;
    private static final int NEWS_LOADER_ID = 1;
    private String author;
    private static String webUrl;

    //Storing the url in url of String type
    private String url = "http://content.guardianapis.com/search";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        newsList = new ArrayList<>();
        imageList = new ArrayList<>();

        // Find a reference to the {@link ListView} in the layout
        list_view = findViewById(R.id.list);
        warningTV = findViewById(R.id.warning);
        warningIV = findViewById(R.id.warning_image);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.

            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle.
            Loader<String> loader = loaderManager.getLoader(NEWS_LOADER_ID);
            if (loader == null) {
                loaderManager.initLoader(NEWS_LOADER_ID, null, this);
            } else {
                loaderManager.restartLoader(NEWS_LOADER_ID, null, this);
            }
        } else {
            View loadingIndicator = findViewById(R.id.progress_bar);
            loadingIndicator.setVisibility(View.GONE);
            warningTV.setText(getString(R.string.noInternet));
            warningIV.setImageResource(R.drawable.ic_no_internet);
            list_view.setEmptyView(warningTV);
            list_view.setEmptyView(warningIV);
        }

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // Convert the String URL into a URI object (to pass into the Intent constructor)

                Uri newsUri = Uri.parse(webPages.get(position));

                // Create a new intent to view the news URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);
                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(websiteIntent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                boolean isIntentSafe = activities.size() > 0;

                if (isIntentSafe) {
                    // Send the intent to launch a new activity
                    startActivity(websiteIntent);
                }
                newsList.clear();
                imageList.clear();
                webPages.clear();
            }
        });

    }

    @SuppressLint("StaticFieldLeak")
    //Starts automatic as it is related to asynchronous class
    @Override
    public Loader<String> onCreateLoader(int i, Bundle bundle) {
        // TODO: Create a new loader for the given URL
        return new AsyncTaskLoader<String>(this) {

            String resultFromHttp;

            @Override
            public String loadInBackground() {
                HttpHandler handler = new HttpHandler();

                Uri baseuri=Uri.parse(url);
                // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
                Uri.Builder uriBuilder = baseuri.buildUpon();

                uriBuilder.appendQueryParameter("order-by","newest");
                uriBuilder.appendQueryParameter("show-tags","contributor");
                uriBuilder.appendQueryParameter("page-size","20");
                uriBuilder.appendQueryParameter("show-fields","thumbnail");
                uriBuilder.appendQueryParameter("api-key","9c158251-1c33-454e-af58-7a1dfa15a186");

                url=uriBuilder.toString();


                String jsonString = "";
                try {
                    //Calling makehttprequest method in HttpHandler.java
                    jsonString = handler.makeHttpRequest(createUrl(url));
                } catch (IOException e) {
                    return null;
                }

                if (jsonString != null) {
                    try {

                        JSONObject jsonObject = new JSONObject(jsonString);
                        JSONObject response = jsonObject.getJSONObject(getString(R.string.response));

                        // Extract the JSONArray associated with the key called "results",
                        JSONArray results = response.getJSONArray(getString(R.string.results));
                        // looping through all Contacts
                        for (int i = 0; i < results.length(); i++) {

                            try {
                                JSONObject article = results.getJSONObject(i);

                                //Extract the values for the key called webtitle
                                String title = article.getString(getString(R.string.webTitle));
                                String date = article.getString(getString(R.string.webPublicationDate));
                                String section = article.getString(getString(R.string.sectionName));
                                webUrl = article.getString(getString(R.string.webUrl));

                                // For a given article, extract the JSONObject associated with the
                                // key called "fields"
                                JSONObject field = article.getJSONObject(getString(R.string.fields));

                                String thumbnail = field.getString(getString(R.string.thumbnail));

                                //Calling LoadImageFromWebOperations method and passing thumbnail as parameter pf type String
                                Drawable image = LoadImageFromWebOperations(thumbnail);
                                // Extract the JSONArray associated with the key called "tags" inside the objects of array of key results.
                                JSONArray tags = article.getJSONArray(getString(R.string.tags));

                                for (int j = 0; j < tags.length(); j++) {
                                    try {

                                        JSONObject authorInfo = tags.getJSONObject(j);
                                        author = authorInfo.getString(getString(R.string.webTitle));


                                    } catch (final JSONException e) {
                                        e.printStackTrace();
                                    }
                                }


                                HashMap<String, String> result = new HashMap<>();

                                // add each child node to HashMap key => value
                                result.put(getString(R.string.title), title);
                                result.put(getString(R.string.webPublicationDate), date);
                                result.put(getString(R.string.sectionName), section);
                                result.put(getString(R.string.author), author);

                                HashMap<String, Drawable> iv = new HashMap<>();
                                iv.put(getString(R.string.thumbnail), image);

                                // adding a news to our news list
                                newsList.add(result);
                                imageList.add(iv);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            webPages.add(webUrl);
                                            //Calling NewsAdapter Constructor and storing info in it,s variable so that it can be accessed by our adapter.
                                            mAdapter = new NewsAdapter(MainActivity.this, newsList, imageList);
                                            if(mAdapter!=null) {
                                                mAdapter.notifyDataSetChanged();
                                            }
                                            list_view.setAdapter(mAdapter);
                                            View loadingIndicator = findViewById(R.id.progress_bar);
                                            loadingIndicator.setVisibility(View.GONE);
                                        }catch(IllegalStateException e){
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } catch (final JSONException e) {

                                //Calling runOnUiThread method for notifyDataSetChanged to work which acts as an alternative to
                                // add/remove... methods of an adapter
                                //Due to for loop for every parsed data runOnUiThread is  called which is necessary for notifyDataSetChanged to work
                                //in order to call it on Ui thread
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Shows error in the toast form when there is parsingerror
                                        Toast.makeText(getApplicationContext(),
                                                getString(R.string.parsingError) + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                        }
                    } catch (final JSONException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        getString(R.string.parsingError) + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.notGetJson),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

                return null;
            }

            //CreateUrl method which converts the String url into an object of URL class
            private URL createUrl(String stringUrl) {
                URL url;
                try {
                    url = new URL(stringUrl);
                } catch (MalformedURLException exception) {//Catching an exception
                    return null;
                }
                return url;
            }

            private Drawable LoadImageFromWebOperations(String url) {
                try {
                    InputStream is = (InputStream) new URL(url).getContent();
                    return Drawable.createFromStream(is, getString(R.string.thumbnail));
                } catch (Exception e) {
                    return null;
                }
            }

            //Automatically called after getloaderManager is activated
            @Override
            protected void onStartLoading() {
                if (resultFromHttp != null) {
                    //To skip loadInBackground call
                    deliverResult(resultFromHttp);
                } else {
                    forceLoad();
                }
            }
        };
    }

    @SuppressLint("ResourceType")
    public void onLoadFinished(Loader<String> loader, String data) {
        // TODO: Update the UI with the result

        if (newsList != null && !newsList.isEmpty()) {

            warningIV.setVisibility(View.GONE);
            View loadingIndicator = findViewById(R.id.progress_bar);
            loadingIndicator.setVisibility(View.GONE);
        } else {
            View loadingIndicator = findViewById(R.id.progress_bar);
            loadingIndicator.setVisibility(View.GONE);
            warningTV.setText(getString(R.string.noNews));
            list_view.setEmptyView(warningTV);
            list_view.setEmptyView(warningIV);

        }

    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        // TODO: Loader reset, so we can clear out our existing data.

        newsList.clear();
        imageList.clear();
        webPages.clear();
    }

    public void onResume() {
        newsList.clear();
        imageList.clear();
        webPages.clear();
        super.onResume();
    }
}




