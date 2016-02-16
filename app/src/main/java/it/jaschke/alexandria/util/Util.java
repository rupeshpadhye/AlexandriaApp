package it.jaschke.alexandria.util;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import it.jaschke.alexandria.MainActivity;

/**
 * Created by RUPESH on 2/14/2016.
 */
public final class Util {
    private static final String LOG_TAG=Util.class.getName();
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conManager.getActiveNetworkInfo();
        Log.d(LOG_TAG,""+netInfo.isConnected());
        return ( netInfo != null && netInfo.isConnected() );
    }


    public static void broadCastMessage(Context context,String message)
    {
        Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
        messageIntent.putExtra(MainActivity.MESSAGE_KEY,message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(messageIntent);
    }
}
