package com.elnware.spotifystreamer.fragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.elnware.spotifystreamer.BuildConfig;
import com.elnware.spotifystreamer.fragment.base.MyFragment;
import com.elnware.spotifystreamer.model.ParcelableArtist;
import com.elnware.spotifystreamer.model.ParcelableImage;
import com.elnware.spotifystreamer.util.DataLoader;
import com.elnware.spotifystreamer.util.Response;
import com.elnware.spotifystreamer.provider.MySuggestionProvider;
import com.elnware.spotifystreamer.R;
import com.elnware.spotifystreamer.util.ImageUtils;
import com.elnware.spotifystreamer.util.RetrofitErrorHandler;
import com.elnware.spotifystreamer.view.MyRecyclerAdapter;
import com.elnware.spotifystreamer.view.MyRecyclerView;
import com.elnware.spotifystreamer.view.MyRecyclerViewHolder;
import com.elnware.spotifystreamer.view.decorator.DividerItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;


/**
 * A search fragment containing a search box on the toolbar
 * to search for artists and display the search results
 */
public class SearchFragment extends MyFragment
        implements LoaderManager.LoaderCallbacks<Response<List<ParcelableArtist>>> {

   public static final int DURATION  = 700;
   public static final int LOADER_ID = 1;

   private static final String LOG_TAG                  = SearchFragment.class.getSimpleName();
   private static final String EXTRA_SEARCH_TEXT        = "search_text";
   private static final String EXTRA_SEARCH_EXPANDED    = "search_expanded";
   private static final String EXTRA_SEARCH_ARTIST_LIST = "parcelable_artist_list";

   private SpotifyService mSpotify;

   private ArtistSearchResultAdapter mySearchResultAdapter;
   @Bind(R.id.progress_container)
   LinearLayout              mProgressContainer;

   @Bind(R.id.recycler_view)
   MyRecyclerView myRecyclerView;

   @Bind(android.R.id.empty)
   View           mEmptyView;

   @Bind(R.id.tv_empty)
   TextView               mEmptyText;

   private String mQueryString;
   private boolean mIsSearchExpanded = false;

   private SearchArtistCallback   mCallback;
   private SearchView             mSearchView;
   private Bundle                 mSavedInstanceState;
   private List<ParcelableArtist> mArtistList;

   public static SearchFragment newInstance() {
      return new SearchFragment();
   }

   public SearchFragment() {
   }

   public interface SearchArtistCallback {
      void onArtistSelected(Bundle bundle);
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      SpotifyApi api = new SpotifyApi();
      mSpotify = api.getService();
      setHasOptionsMenu(true);
      mySearchResultAdapter = new ArtistSearchResultAdapter(new ArrayList<ParcelableArtist>());
      if (savedInstanceState != null) {
         mArtistList = savedInstanceState.getParcelableArrayList(EXTRA_SEARCH_ARTIST_LIST);
      }
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);

      if (mSearchView == null){
         mQueryString = "";
      }else{
         mQueryString = mSearchView.getQuery().toString();
      }

      outState.putString(EXTRA_SEARCH_TEXT, mQueryString);
      outState.putBoolean(EXTRA_SEARCH_EXPANDED, mIsSearchExpanded);
      if (mArtistList != null){
         outState.putParcelableArrayList(EXTRA_SEARCH_ARTIST_LIST, new ArrayList<ParcelableArtist>(mArtistList));
      }

      mySearchResultAdapter.onSaveInstanceState(outState);
   }

   @Override
   public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      mSavedInstanceState = savedInstanceState;
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      super.onCreateView(inflater, container, savedInstanceState);
      View view = inflater.inflate(R.layout.fragment_spotify_search, container, false);
      ButterKnife.bind(this, view);

      mEmptyText.setText(getString(R.string.msg_search_click));

      if (savedInstanceState != null) {
         String searchString = savedInstanceState.getString(EXTRA_SEARCH_TEXT);
         mIsSearchExpanded = savedInstanceState.getBoolean(EXTRA_SEARCH_EXPANDED, false);
         if (searchString == null) {
            searchString = "";
         }
         mQueryString = searchString;
         getCompatActivity().supportInvalidateOptionsMenu();
      }

      LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
      layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
      myRecyclerView.setLayoutManager(layoutManager);

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
         myRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), null));
      }

      myRecyclerView.setHasFixedSize(true);
      myRecyclerView.setEmptyView(mEmptyView);
      myRecyclerView.setAdapter(mySearchResultAdapter);
      if (savedInstanceState != null){
         if (mArtistList != null){
            mySearchResultAdapter.changeData(mArtistList);
         }
      }
      mySearchResultAdapter.setRecyclerCallbacks(new MySimpleRecyclerCallback());

      return view;
   }

   @Override
   public void onDestroyView() {
      super.onDestroyView();
      ButterKnife.unbind(this);
   }

   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);
      if (activity instanceof SearchArtistCallback){
         mCallback = (SearchArtistCallback) activity;
      }else{
         throw new ClassCastException("Activity must implement SearchArtistCallbacks");
      }
   }

   @Override
   public void onStart() {
      super.onStart();
      getLoaderManager().initLoader(LOADER_ID, null, this);
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.search_fragment_menu, menu);

      final MenuItem searchItem = menu.findItem(R.id.actionSearch);

      SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

      mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
      mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

      if (mIsSearchExpanded) {
         MenuItemCompat.expandActionView(searchItem);
         mSearchView.setIconified(false);
      }

      mSearchView.setQuery(mQueryString, false);

      mSearchView.setOnQueryTextListener(mQueryTextListener);
      mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
         @Override
         public boolean onClose() {
            mQueryString = "";
            mIsSearchExpanded = false;

            return false;
         }
      });

      mSearchView.setOnSearchClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            mIsSearchExpanded = true;
         }
      });

      mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
         @Override
         public boolean onSuggestionSelect(int position) {
            String suggestion = (String) mSearchView.getSuggestionsAdapter().getItem(position);
            mSearchView.setQuery(suggestion, true);
            return true;
         }

         @Override
         public boolean onSuggestionClick(int position) {
            Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(position);
            mSearchView.setQuery(cursor.getString(SearchRecentSuggestions.QUERIES_PROJECTION_QUERY_INDEX), true);
            mSearchView.setIconified(true);
            return true;
         }
      });
   }


   private final SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
         mQueryString = query;
         SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                 MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
         suggestions.saveRecentQuery(query, null);
         showProgressIndeterminate(true);


         //Clear Selection to deselect the highlighted
         mySearchResultAdapter.clearSelection();

         getLoaderManager()
                 .restartLoader(LOADER_ID, null, SearchFragment.this)
                 .forceLoad();

         mSearchView.setIconified(true);

         return false;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
         return false;
      }
   };

   private static void log(String tag) {
      if (BuildConfig.DEBUG) {
         Log.d(LOG_TAG, tag);
      }
   }

   private void showProgressIndeterminate(boolean isShowing) {
      if (isShowing) {
         mProgressContainer.setVisibility(View.VISIBLE);
         myRecyclerView.setVisibility(View.GONE);
         mEmptyView.setVisibility(View.GONE);
      } else {
         mProgressContainer.setVisibility(View.GONE);
         myRecyclerView.setVisibility(View.VISIBLE);
      }
   }

   @Override
   public Loader<Response<List<ParcelableArtist>>> onCreateLoader(int id, Bundle args) {
      return new SearchTaskLoader(getActivity(), mQueryString);
   }

   @Override
   public void onLoadFinished(Loader<Response<List<ParcelableArtist>>> loader, Response<List<ParcelableArtist>> response) {

      mArtistList = response.getData();

      mySearchResultAdapter.changeData(response.getData());

      if (mSavedInstanceState != null) {
         mySearchResultAdapter.onRestoreInstanceState(mSavedInstanceState);
         mySearchResultAdapter.notifyDataSetChanged();
      }

      if (!response.isError()) {
         mEmptyText.setText(getString(R.string.msg_no_artist_found));
      } else {
         String standardErrorMessage = RetrofitErrorHandler.getStandardErrorMessage(getActivity(),
                 response.getThrowable());
         mEmptyText.setText(standardErrorMessage);
      }

      showProgressIndeterminate(false);
   }

   @Override
   public void onLoaderReset(Loader<Response<List<ParcelableArtist>>> loader) {

   }

   private class MySimpleRecyclerCallback extends MyRecyclerView.SimpleRecyclerCallbacks {
      @Override
      public void OnItemClick(final View view, int position) {
         super.OnItemClick(view, position);
         final ParcelableArtist artist         = mySearchResultAdapter.getItems().get(position);


         Bundle bundle = new Bundle();
         bundle.putString(TopTrackFragment.EXTRA_ARTIST_ID, artist.id);
         bundle.putString(TopTrackFragment.EXTRA_ARTIST_NAME, artist.name);

         ParcelableImage image = ImageUtils.getOptimumParcelableImage(artist.images, R.integer.default_cover_image_width);

         String imageUrl = "";
         if (image != null){
            imageUrl = image.url;
         }

         if (image != null) {
            bundle.putString(TopTrackFragment.EXTRA_ARTIST_IMAGE, imageUrl);
         }

         if (hasTwoPanes()) {
            mySearchResultAdapter.setItemChecked(position, true);
            mCallback.onArtistSelected(bundle);
         }else{

            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragment_container,
                            TopTrackFragment.newInstance(bundle))
                    .commit();
         }

      }
   }


   /**
    * Artist Result adapter
    */
   public class ArtistSearchResultAdapter extends
           MyRecyclerAdapter<List<ParcelableArtist>, ArtistSearchResultAdapter.ViewHolder, ParcelableArtist> {

      public ArtistSearchResultAdapter(List<ParcelableArtist> data) {
         super(data);
      }

      @Override
      public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
         View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_artist, parent, false);
         return new ViewHolder(view);
      }

      @Override
      public void onBindViewHolder(ViewHolder holder, int position) {
         ParcelableArtist artist = getItem(position);
         holder.tvArtistTitle.setText(artist.name);


         /**
          * we will use resource to store the width setting so that we can modify
          * them on larger device if necessary
          */
         ParcelableImage image = ImageUtils.getOptimumParcelableImage(artist.images,
                 getResources().getInteger(R.integer.default_list_image_width));

         if (image != null) {
            Glide.with(SearchFragment.this)
                    .load(image.url)
                    .error(R.drawable.empty)
                    .into(holder.ivArtistIcon);
         } else {
            holder.ivArtistIcon.setImageResource(R.drawable.empty);
         }


         Drawable drawable = holder.view.getBackground();

         if (isSelected(position)) {
            holder.view.setSelected(true);
         } else {
            holder.view.setSelected(false);
         }
      }

      public class ViewHolder extends MyRecyclerViewHolder {
         final TextView  tvArtistTitle;
         final ImageView ivArtistIcon;
         final View      view;

         public ViewHolder(View view) {
            super(view);
            this.view = view;
            ivArtistIcon = (ImageView) view.findViewById(R.id.img_artist_icon);
            tvArtistTitle = (TextView) view.findViewById(R.id.tv_artist_name);
         }

         @Override
         public void onClick(View view) {
            if (getRecyclerCallbacks() != null) {
               getRecyclerCallbacks().OnItemClick(view, getAdapterPosition());
            }
         }
      }
   }


   /**
    * Task Loader for Searching Artist
    */
   public static class SearchTaskLoader extends DataLoader<Response<List<ParcelableArtist>>> {
      private final SpotifyService mService;
      private       String         mQueryString;

      public SearchTaskLoader(Context context, String queryString) {
         super(context);
         SpotifyApi spotifyApi = new SpotifyApi();
         mService = spotifyApi.getService();
         mQueryString = queryString;
      }


      @Override
      public Response<List<ParcelableArtist>> loadInBackground() {

         Response<List<ParcelableArtist>> response = new Response();
         response.setData(Collections.EMPTY_LIST);

         ArtistsPager results;

         try {
            results = mService.searchArtists(mQueryString);
         } catch (RetrofitError e) {
            e.printStackTrace();
            response.setThrowable(RetrofitErrorHandler.handleError(e));
            return response;
         }

         if (results.artists != null && results.artists.items.size() > 0) {
            List<ParcelableArtist> parcelableArtistList = new ArrayList<>();
            for(Artist artist: results.artists.items){
               parcelableArtistList.add(ParcelableArtist.copy(artist));
            }
            response.setData(parcelableArtistList);
            return response;
         }

         return response;
      }
   }

}
