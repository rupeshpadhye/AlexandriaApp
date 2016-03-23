//-----------------------------------------------------------------------------
package it.jaschke.alexandria;
//-----------------------------------------------------------------------------
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;
import it.jaschke.alexandria.util.Util;
//-----------------------------------------------------------------------------
/**
 *  Fragments showing book details
 */
//-----------------------------------------------------------------------------
public class BookDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EAN_KEY = "EAN";
    private static  final int LOADER_ID = 10;
    private View rootView;
    private String ean;
    private String bookTitle;
    private boolean  IS_TABLET=false;
    private ShareActionProvider shareActionProvider;
    private Intent shareIntent;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    public void updateView(String isbn) {
        ean=isbn;
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            ean = arguments.getString(BookDetail.EAN_KEY);
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }

         IS_TABLET = Util.isTablet(getActivity());
        rootView = inflater.inflate(R.layout.fragment_full_book, container, false);
        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);

                if (IS_TABLET) {
                    clearFields();
                } else {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
        return rootView;
    }


     private void clearFields() {
        ((TextView) getView().findViewById(R.id.fullBookTitle)).setText("");
        ((TextView) getView().findViewById(R.id.fullBookSubTitle)).setText("");
        ((TextView) getView().findViewById(R.id.authors)).setText("");
         ((TextView) getView().findViewById(R.id.categories)).setText("");
         ((TextView) getView().findViewById(R.id.fullBookDesc)).setText("");
        getView().findViewById(R.id.fullBookCover).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (null != shareIntent) {
            shareActionProvider.setShareIntent(shareIntent);
        }

    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );
    }


    public Intent createShareIntent() {
        shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + bookTitle);
        return shareIntent;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.fullBookTitle)).setText(bookTitle);

        if(shareActionProvider!=null) {
            shareActionProvider.setShareIntent(createShareIntent());
        }
        rootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        if(bookSubTitle!=null) {
            ((TextView) rootView.findViewById(R.id.fullBookSubTitle)).setText(bookSubTitle);
        }
        String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        if(desc!=null){
            ((TextView) rootView.findViewById(R.id.fullBookDesc)).setText(desc);
        }

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if(authors!=null){
            String[] authorsArr = authors.split(",");
            ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",","\n"));
        }


        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            new DownloadImage((ImageView) rootView.findViewById(R.id.fullBookCover)).execute(imgUrl);
            rootView.findViewById(R.id.fullBookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        if(IS_TABLET){
            rootView.findViewById(R.id.backButton).setVisibility(View.INVISIBLE);
        }



    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        //no need
    }

    @Override
    public void onPause() {
        super.onDestroyView();
        if(IS_TABLET && rootView.findViewById(R.id.right_container)==null){
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------