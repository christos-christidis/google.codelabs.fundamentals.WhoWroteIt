package com.fundamentals.whowroteit;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// SOS: loaders were deprecated in API 28 in favor of ViewModels and LiveData! Makes sense.
public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String> {

    private static final String ARG_QUERY_STRING = "arg_query_string";

    private static final int LOADER_ID = 0;

    private EditText mBookInput;
    private TextView mTitleText;
    private TextView mAuthorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBookInput = findViewById(R.id.bookInput);
        mTitleText = findViewById(R.id.titleText);
        mAuthorText = findViewById(R.id.authorText);

        // SOS: we call initLoader only if the activity restarts w an existent loader to re-associate
        // the activity w that loader. If the loader has already finished, onLoadFinished is called
        // again, re-setting the text-views which would otherwise be reset to empty. HOWEVER.
        // SOS: Above is how it worked (correctly) up to SDK 26. After 27, we get the onLoadFinished,
        // then we get ANOTHER loadInBackground and onLoadFinished again. It's a clusterfuck that I
        // don't know how to fix. Maybe it's a good thing this got deprecated...
        if (getSupportLoaderManager().getLoader(LOADER_ID) != null) {
            Log.i("WTF", "initLoader called!");
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    public void searchBooks(View view) {
        String queryString = mBookInput.getText().toString();

        // close keyboard on Search button press
        InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        // don't forget to check if network is connected
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        if (networkInfo != null && networkInfo.isConnected() && queryString.length() != 0) {
            // SOS: when user presses Search, we create the loader w restartLoader, passing to it
            // the queryString arg that will then be passed to onCreateLoader in order to instantiate
            // our loader.
            Bundle args = new Bundle();
            args.putString(ARG_QUERY_STRING, queryString);
            getSupportLoaderManager().restartLoader(LOADER_ID, args, this);

            mAuthorText.setText("");
            mTitleText.setText(R.string.loading);
        } else {
            if (queryString.length() == 0) {
                mAuthorText.setText("");
                mTitleText.setText(R.string.no_search_term);
            } else {
                mAuthorText.setText("");
                mTitleText.setText(R.string.no_network);
            }
        }
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        Log.i("WTF", "onCreateLoader called!");
        assert args != null;
        String queryString = args.getString(ARG_QUERY_STRING);
        return new BookLoader(this, queryString);
    }

    // SOS: when the loader is done, the result is in data. Note how we update the text-views in this
    // method, ie in the main thread, thus solving the problem of disassociation w the activity that
    // AsyncTasks have when an activity is destroyed & recreated!
    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        Log.i("WTF", "onLoadFinished called!");
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray itemArray = jsonObject.getJSONArray("items");

            int i = 0;
            String title = null;
            String authors = null;
            while (i < itemArray.length() && (authors == null || title == null)) {
                JSONObject book = itemArray.getJSONObject(i);
                JSONObject volumeInfo = book.getJSONObject("volumeInfo");

                try {
                    title = volumeInfo.getString("title");
                    authors = volumeInfo.getString("authors");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                i++;
            }

            if (title != null && authors != null) {
                mTitleText.setText(title);
                mTitleText.setText(authors);
            } else {
                mTitleText.setText(R.string.no_results);
                mTitleText.setText("");
            }
        } catch (JSONException e) {
            mTitleText.setText(R.string.no_results);
            mTitleText.setText("");
            e.printStackTrace();
        }
    }

    // SOS: used for clean-up. Unnecessary here
    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {
    }
}
