package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class About extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_about, container, false);
        return view;
    }


    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        if(context instanceof Activity) {
            Activity   activity=(Activity)context;
            activity.setTitle(R.string.about);
        }
    }

}
