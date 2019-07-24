package com.fundamentals.whowroteit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

class BookLoader extends AsyncTaskLoader<String> {

    private final String mQueryString;

    BookLoader(@NonNull Context context, String queryString) {
        super(context);
        mQueryString = queryString;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        // SOS: we must call call this for loadInBackground to start
        forceLoad();
    }

    @Nullable
    @Override
    public String loadInBackground() {
        Log.i("WTF", "loadInBackground called!");
        return NetworkUtils.getBookInfo(mQueryString);
    }
}
