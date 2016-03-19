package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.util.Util;

//added Currently no books available  message if books not stored in locale or wrong criteria
//removed deprecated method onAttach(Activity) to onAttach(context)
public class ListOfBooks extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private BookListAdapter bookListAdapter;
    private ListView bookList;
    private int position = ListView.INVALID_POSITION;
    private EditText searchText;
   // private Cursor mCursor;
    private static final String  LOG_TAG=ListOfBooks.class.getName();

    private static final int LOADER_ID = 10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "ListBooks On create");

        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);
        searchText = (EditText) rootView.findViewById(R.id.searchText);
        rootView.findViewById(R.id.searchButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListOfBooks.this.restartLoader();
                    }
                }
        );

        getLoaderManager().initLoader(LOADER_ID, null, this);
        bookListAdapter = new BookListAdapter(getActivity(), null, 0);

        bookList = (ListView) rootView.findViewById(R.id.listOfBooks);
        bookList.setAdapter(bookListAdapter);

        bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = bookListAdapter.getCursor();

                if (cursor != null && cursor.moveToPosition(position)) {
                    Util.hideSoftKeyboard(getContext(), getView());
                    ((Callback) getActivity())
                            .onItemSelected(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID)));


                }
            }
        });


        return rootView;
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            final String selection = AlexandriaContract.BookEntry.TITLE + " LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";
            String searchString = searchText.getText().toString();
            Log.d(LOG_TAG,searchString);
            if (searchString.length() > 0) {
                searchString = "%" + searchString + "%";
                return new CursorLoader(
                        getActivity(),
                        AlexandriaContract.BookEntry.CONTENT_URI,
                        null,
                        selection,
                        new String[]{searchString, searchString},
                        null
                );
            }
            else {
                Log.d(LOG_TAG, "get all books->");
                return new CursorLoader(
                        getActivity(),
                        AlexandriaContract.BookEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );
            }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        bookListAdapter.swapCursor(data);
            Log.d(LOG_TAG, "onLoadFinished" + bookListAdapter.getCount());
            if (bookListAdapter.getCount() == 0
                    &&
                    searchText.getText().toString()!=null
                    &&
                    !searchText.getText().toString().isEmpty()
                    ) {
                Snackbar.make(getView(), getResources().getString(R.string.no_result), Snackbar.LENGTH_LONG).show();
            }
            if (position != ListView.INVALID_POSITION) {
                bookList.smoothScrollToPosition(position);
            }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookListAdapter.swapCursor(null);
    }




    //removed deprecated method onAttach(Activity) to onAttach(context) -Rupesh P
    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        if(context instanceof Activity) {
            Activity   activity=(Activity)context;
            activity.setTitle(R.string.books);
        }
    }
}
