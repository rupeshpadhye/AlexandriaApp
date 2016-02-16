package it.jaschke.alexandria.api;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
/**
 * Created by RUPESH on 2/14/2016.
 */
@SuppressLint("ParcelCreator")
public class BookDetailsReceiver extends ResultReceiver {

    private Receiver mReceiver;

    public BookDetailsReceiver(Handler handler) {
        super(handler);

    }
    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);

    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
