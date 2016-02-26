package it.jaschke.alexandria.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.dto.BookInfo;
import it.jaschke.alexandria.dto.VolumeInfo;
import it.jaschke.alexandria.util.Util;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class BookService extends IntentService {

    private final String LOG_TAG = BookService.class.getSimpleName();

    public static final String FETCH_BOOK = "it.jaschke.alexandria.services.action.FETCH_BOOK";
    public static final String DELETE_BOOK = "it.jaschke.alexandria.services.action.DELETE_BOOK";

    public static final String SAVE_BOOK = "it.jaschke.alexandria.services.action.SAVE_BOOK";

    public static final String EAN = "it.jaschke.alexandria.services.extra.EAN";
    public static final String BOOK_DETAILS = "BOOK_DETAILS";

    public BookService() {
        super("Alexandria");
    }

    private static VolumeInfo volumeInfo;

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (FETCH_BOOK.equals(action)) {
                ResultReceiver rec = intent.getParcelableExtra("BOOK_DETAILS");
                final String ean = intent.getStringExtra(EAN);
                fetchBook(ean, rec);
            } else if (DELETE_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                deleteBook(ean);
            } else if (SAVE_BOOK.equals(action)) {
                //VolumeInfo volumeInfo = intent.getParcelableExtra("BOOK_DATA"); Rupesh Padhye need to change
                ResultReceiver rec = intent.getParcelableExtra("RECEIVER");
                saveBook(volumeInfo,rec);
            }
        }
    }


    private String getBookDetails(String ean) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJsonString = null;

        try {
            final String FORECAST_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
            final String QUERY_PARAM = "q";
            final String ISBN_PARAM = "isbn:" + ean;

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                    .build();
            URL url = new URL(builtUri.toString());
            Log.d(LOG_TAG, builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return bookJsonString;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            bookJsonString = buffer.toString();

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }

        }

        Log.d(LOG_TAG, "bookJsonString" + bookJsonString);
        return bookJsonString;
    }

    private void saveBook(VolumeInfo volumeInfo,ResultReceiver resultReceiver) {

        String ean = volumeInfo.getIndustryIdentifiers().get(1).getIdentifier();
        String title = volumeInfo.getTitle();
        String subtitle = volumeInfo.getSubtitle();
        String desc = volumeInfo.getDescription();
        String imgUrl = volumeInfo.getImageLinks().getThumbnail();
        writeBackBook(ean, title, subtitle, desc, imgUrl);

        if (volumeInfo.getAuthors()!=null && !volumeInfo.getAuthors().isEmpty()) {
            writeBackAuthors(ean, volumeInfo.getAuthors());
        }
        if (volumeInfo.getCategories() !=null && !volumeInfo.getCategories().isEmpty()) {
            writeBackCategories(ean, volumeInfo.getCategories());
        }

        Bundle bundle=new Bundle();
        resultReceiver.send(1,bundle);
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void deleteBook(String ean) {
        if (ean != null) {
            getContentResolver().delete(AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)), null, null);
        }
    }

    private boolean isBookPreviouslyAdded(String isbn) {
        boolean isBookPresent = false;
        Cursor bookEntry = getContentResolver().query(
                AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(isbn)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if (bookEntry.getCount() > 0) {
            Log.d(LOG_TAG, "book already in local");
            isBookPresent = true;
        }
        bookEntry.close();
        return isBookPresent;
    }

    /**
     * Handle action fetchBook in the provided background thread with the provided
     * parameters.
     */
    private void fetchBook(String ean, ResultReceiver rec) {

        if (ean.length() != 13) {
            return;
        }

        if (isBookPreviouslyAdded(ean)) {
            Util.broadCastMessage(getApplicationContext(), getApplication().getResources().getString(R.string.same_book));
            return;
        }

        String response = getBookDetails(ean);

        if (response == null && !Util.isNetworkConnected(getApplicationContext())) {
            Util.broadCastMessage(getApplicationContext(), getApplication().getResources().getString(R.string.no_internet));
            return;
        } else if (response == null) {
            Util.broadCastMessage(getApplicationContext(), getApplication().getResources().getString(R.string.not_found));
            return;
        }

        Gson gson = new GsonBuilder().create();
        BookInfo bookInfo = gson.fromJson(response, BookInfo.class);
        Log.d(LOG_TAG, bookInfo.toString());

        if(bookInfo.getItems()==null || bookInfo.getItems().isEmpty())
        {
            Util.broadCastMessage(getApplicationContext(), getApplication().getResources().getString(R.string.not_found));
            return;
        }
        volumeInfo=bookInfo.getItems().get(0).getVolumeInfo();

        Bundle bundle = new Bundle();
        bundle.putParcelable("BOOK_DETAILS", bookInfo.getItems().get(0).getVolumeInfo());
        rec.send(0, bundle);
    }

    private void writeBackBook(String ean, String title, String subtitle, String desc, String imgUrl) {
        ContentValues values = new ContentValues();
        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, desc);
        getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI, values);
    }

    private void writeBackAuthors(String ean, List<String> authors) {
        ContentValues values = new ContentValues();
        for(String author:authors){
            values.put(AlexandriaContract.AuthorEntry._ID, ean);
            values.put(AlexandriaContract.AuthorEntry.AUTHOR, author);
            getContentResolver().insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
            values = new ContentValues();
        }
    }

    private void writeBackCategories(String ean, List<String> categories) {
        ContentValues values = new ContentValues();
        for(String category:categories){
            values.put(AlexandriaContract.CategoryEntry._ID, ean);
            values.put(AlexandriaContract.CategoryEntry.CATEGORY,category);
            getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
            values = new ContentValues();
        }
    }
}