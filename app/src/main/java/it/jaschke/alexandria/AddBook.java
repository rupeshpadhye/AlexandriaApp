package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import it.jaschke.alexandria.api.BookDetailsReceiver;
import it.jaschke.alexandria.constant.AlexandriaConstants;
import it.jaschke.alexandria.dto.VolumeInfo;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;
import it.jaschke.alexandria.util.Util;


public class AddBook extends Fragment implements  BookDetailsReceiver.Receiver {
    private EditText ean;
    private View rootView;
    private BookDetailsReceiver mReceiver;



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (ean != null) {
            outState.putString(AlexandriaConstants.EAN_CONTENT, ean.getText().toString());
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new BookDetailsReceiver(new Handler());
        mReceiver.setReceiver(this);
    }


    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        hideProgress();
        if(resultCode==AlexandriaConstants.BOOK_DETAIL_SUCCESS) {
            VolumeInfo mVolumeInfo = resultData.getParcelable(BookService.BOOK_DETAILS);
            updateView(mVolumeInfo);
        }
        else if(resultCode==AlexandriaConstants.BOOK_SAVE_SUCCESS) {
            Util.broadCastMessage(getContext(), getResources().getString(R.string.book_save_success));
            clearISBNCode();
        }
        else if(resultCode==AlexandriaConstants.NO_INTERNET){
            Util.broadCastMessage(getContext(), getResources().getString(R.string.no_internet));
        }
        else if(resultCode==AlexandriaConstants.BOOK_PRESENT)
        {
            Util.broadCastMessage(getContext(), getResources().getString(R.string.same_book));
        }
        else{
            Util.broadCastMessage(getContext(), getResources().getString(R.string.error));
        }


    }

    private void updateView(VolumeInfo volumeInfo) {
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText(volumeInfo.getTitle());
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText(volumeInfo.getSubtitle());
        ((TextView) rootView.findViewById(R.id.authors)).setLines(volumeInfo.getAuthors().size());

        String authors="";
        for(String author:volumeInfo.getAuthors()) {
            authors+=author+"\n";
        }
        ((TextView) rootView.findViewById(R.id.authors)).setText(authors);

        if (Patterns.WEB_URL.matcher(volumeInfo.getImageLinks().getThumbnail()).matches()) {
            new DownloadImage((ImageView) rootView.findViewById(R.id.bookCover))
                    .execute(volumeInfo.getImageLinks().getThumbnail());
            rootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }
        if(volumeInfo.getCategories()!=null && !volumeInfo.getCategories().isEmpty()) {
            ((TextView) rootView.findViewById(R.id.categories)).setText(volumeInfo.getCategories().toString());
        }
        rootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        ean = (EditText) rootView.findViewById(R.id.ean);


        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String isbn = s.toString();

                if(isbn.length()==0)
                {
                    hideProgress();
                }
                //catch isbn10 numbers
                if (isbn.length() == 10 && !isbn.startsWith("978")) {
                    isbn = "978" + isbn;
                }
                if (isbn.length() < 13) {
                    clearFields();
                    return;
                }
                //Once we have an ISBN, start a book intent
                showProgress();
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, isbn);
                bookIntent.putExtra(BookService.BOOK_DETAILS, mReceiver);
                bookIntent.setAction(BookService.FETCH_BOOK);
                getActivity().startService(bookIntent);

            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Util.isCameraAvailable(getContext())) {
                    try {
                        Intent intent = new Intent(v.getContext(), BarcodeScanner.class);
                        startActivityForResult(intent, AlexandriaConstants.BOOK_BARCODE_READER);
                    } catch (Exception e) {
                        Util.broadCastMessage(getContext(), getResources().getString(R.string.error));
                      e.printStackTrace();

                    }
                } else {
                    Util.broadCastMessage(getContext(), getResources().getString(R.string.no_camera));
                }


            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent bookIntent = new Intent(getActivity(), BookService.class);
                //bookIntent.putExtra("BOOK_DATA", mVolumeInfo);
                bookIntent.putExtra("RECEIVER",mReceiver);
                bookIntent.setAction(BookService.SAVE_BOOK);
                getActivity().startService(bookIntent);
                showProgress();

            }
        });

        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                ean.setText("");
            }
        });

        if (savedInstanceState != null) {
            ean.setText(savedInstanceState.getString(AlexandriaConstants.EAN_CONTENT));
            ean.setHint("");
        }

        return rootView;
    }

    private  void showProgress()
    {
        rootView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.save_button).setClickable(false);
        rootView.findViewById(R.id.delete_button).setClickable(false);
    }

    private  void hideProgress()
    {
        rootView.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setClickable(true);
        rootView.findViewById(R.id.delete_button).setClickable(true);
    }


    private void clearISBNCode()
    {
        ((TextView) rootView.findViewById(R.id.ean)).setText("");
    }
    private void clearFields() {
        ((TextView) rootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) rootView.findViewById(R.id.authors)).setText("");
        ((TextView) rootView.findViewById(R.id.categories)).setText("");
        rootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==AlexandriaConstants.BOOK_BARCODE_READER)
        {
            String isbnNo=data.getStringExtra("ISBN_NO");
            ean.setText(isbnNo);
        }

    }


    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        if(context instanceof Activity) {
            Activity   activity=(Activity)context;
            activity.setTitle(R.string.scan);
        }
    }

}
