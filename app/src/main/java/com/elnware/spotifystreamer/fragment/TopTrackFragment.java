package com.elnware.spotifystreamer.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.elnware.spotifystreamer.R;
import com.elnware.spotifystreamer.activity.MediaPlayerActivity;
import com.elnware.spotifystreamer.fragment.base.MyFragment;
import com.elnware.spotifystreamer.model.ParcelableTrack;
import com.elnware.spotifystreamer.model.PlaylistItem;
import com.elnware.spotifystreamer.util.DataLoader;
import com.elnware.spotifystreamer.util.Response;
import com.elnware.spotifystreamer.util.ImageUtils;
import com.elnware.spotifystreamer.util.RetrofitErrorHandler;
import com.elnware.spotifystreamer.view.MyRecyclerAdapter;
import com.elnware.spotifystreamer.view.MyRecyclerView;
import com.elnware.spotifystreamer.view.MyRecyclerViewHolder;
import com.elnware.spotifystreamer.view.decorator.DividerItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * Fragment that show the top 10 tracks of the artist
 * Created by elnoxvie on 6/20/15.
 */
public class TopTrackFragment extends MyFragment
        implements LoaderManager.LoaderCallbacks<Response<List<Track>>> {
   public static final String EXTRA_ARTIST_ID    = "artist_id";
   public static final String EXTRA_ARTIST_NAME  = "artist_name";
   public static final String EXTRA_ARTIST_IMAGE = "artist_image_url";

   public static final String EXTRA_PLAY_LIST = "play_list";

   public static final int LOADER_ID = 1;

   private static final long ANIM_DURATION = 1000;

   private TopTrackAdapter mTopTrackAdapter;
   private String          mArtistId;
   private String          mArtistName;

   private String                mArtistImage;

   @Bind(R.id.backdrop)
   ImageView             mImageView;

   @Bind(R.id.tv_title)
   TextView              mTvTitle;

   @Bind(R.id.recycler_view)
   MyRecyclerView        mRecyclerView;

   @Bind(android.R.id.empty)
   View                  mEmptyView;

   @Bind(R.id.progress_container)
   LinearLayout          mProgressContainer;

   @Bind(R.id.tv_empty)
   TextView              mEmptyText;

   private DividerItemDecoration mDividerItemDecoration;
   private TopTrackCallback      mCallback;


   public interface TopTrackCallback {
      void onSongSelected(PlaylistItem playlistItem);
   }

   public TopTrackFragment() {
   }

   public static TopTrackFragment newInstance(Bundle bundle) {
      TopTrackFragment fragment = new TopTrackFragment();
      fragment.setArguments(bundle);
      return fragment;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      Bundle bundle;
      if (savedInstanceState != null) {
         bundle = savedInstanceState;
      } else {
         bundle = getArguments();
      }

      mArtistId = bundle.getString(EXTRA_ARTIST_ID);
      mArtistName = bundle.getString(EXTRA_ARTIST_NAME);
      mArtistImage = bundle.getString(EXTRA_ARTIST_IMAGE);
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      outState.putString(EXTRA_ARTIST_ID, mArtistId);
      outState.putString(EXTRA_ARTIST_NAME, mArtistName);
      outState.putString(EXTRA_ARTIST_IMAGE, mArtistImage);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      super.onCreateView(inflater, container, savedInstanceState);
      View view = inflater.inflate(R.layout.fragment_top_songs, container, false);
      ButterKnife.bind(this, view);

      Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);

      if (toolbar != null) {
         getCompatActivity().setSupportActionBar(toolbar);
         getCompatActionBar().setDisplayHomeAsUpEnabled(true);
         getCompatActionBar().setIcon(null);
         getCompatActionBar().setTitle(R.string.top_10_tracks);
      }

      final CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
      toolbarLayout.setTitle(mArtistName);

      if (mArtistImage != null) {
         Glide.with(this)
                 .load(mArtistImage)
                 .asBitmap()
                 .dontAnimate()
                 .into(new BitmapImageViewTarget(mImageView) {

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                       super.onLoadFailed(e, errorDrawable);


                       /**
                        * we will also start the transition after the bitmap is loaded
                        */
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                       super.onResourceReady(resource, glideAnimation);

                       Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                          public void onGenerated(Palette p) {
                             Palette.Swatch swatch = p.getVibrantSwatch();
                             if (swatch != null) {
                                int color = swatch.getTitleTextColor();
                                mTvTitle.setBackgroundColor(swatch.getRgb());
                                mTvTitle.setTextColor(color);
                                toolbarLayout.setContentScrimColor(swatch.getRgb());
                                toolbarLayout.setTitle(mArtistName);
                             }
                          }
                       });

                    }
                 });
      }

      mEmptyText.setText(R.string.msg_no_top_tracks);

      LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
      layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
      mRecyclerView.setLayoutManager(layoutManager);
      mRecyclerView.setHasFixedSize(true);

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
         mDividerItemDecoration = new DividerItemDecoration(getActivity(), null);
         mRecyclerView.addItemDecoration(mDividerItemDecoration);
      }

      mRecyclerView.setEmptyView(mEmptyView);

      Loader loader = getLoaderManager().getLoader(LOADER_ID);

      if (loader != null) {


         //If the task is still running we will display the progress bar
         if (loader.isStarted()) {
            showProgressIndeterminate(true);
         } else {
            showProgressIndeterminate(false);
         }
      }


      mTopTrackAdapter = new TopTrackAdapter(Collections.EMPTY_LIST);
      mTopTrackAdapter.setRecyclerCallbacks(new MySimpleRecyclerCallback());
      mRecyclerView.setAdapter(mTopTrackAdapter);

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
      if (activity instanceof TopTrackCallback){
         mCallback = (TopTrackCallback) activity;
      }else{
         throw new ClassCastException("Activity must implement TopTrackCallback");
      }
   }

   @Override
   public void onStart() {
      super.onStart();
      getLoaderManager().initLoader(LOADER_ID, null, this);
   }

   private void showProgressIndeterminate(boolean isShowing) {
      if (isShowing) {
         mProgressContainer.setVisibility(View.VISIBLE);
         mEmptyView.setVisibility(View.GONE);
         mRecyclerView.setVisibility(View.GONE);
      } else {
         mRecyclerView.setVisibility(View.VISIBLE);
         mProgressContainer.setVisibility(View.GONE);
      }
   }

   @Override
   public Loader<Response<List<Track>>> onCreateLoader(int i, Bundle bundle) {
      showProgressIndeterminate(true);
      return new TopTrackTaskLoader(getActivity(), mArtistId);
   }

   @Override
   public void onLoadFinished(Loader<Response<List<Track>>> loader, Response<List<Track>> response) {
      mTopTrackAdapter.changeData(response.getData());
      if (response.isError()) {
         String standardErrorMessage =
                 RetrofitErrorHandler.getStandardErrorMessage(getActivity(), response.getThrowable());
         mEmptyText.setText(standardErrorMessage);
      } else {
         mEmptyText.setText(getString(R.string.msg_no_top_tracks));
      }

      showProgressIndeterminate(false);
   }


   @Override
   public void onLoaderReset(Loader<Response<List<Track>>> loader) { }


   private class MySimpleRecyclerCallback extends MyRecyclerView.SimpleRecyclerCallbacks {
      @Override
      public void OnItemClick(final View view, int position) {
         super.OnItemClick(view, position);

         ArrayList<ParcelableTrack> tracks = new ArrayList<>();


         for (Track track : mTopTrackAdapter.getItems()) {
            tracks.add(ParcelableTrack.copy(track));
         }

         PlaylistItem playlistItem = new PlaylistItem(tracks, position);
         playlistItem.setSessionId(mArtistId);

         if (hasTwoPanes()){
            mCallback.onSongSelected(playlistItem);
         }else{
            Intent intent = new Intent(getActivity(), MediaPlayerActivity.class);
            intent.putExtra(TopTrackFragment.EXTRA_PLAY_LIST, playlistItem);
            startActivity(intent);
         }
      }
   }

   public static class TopTrackTaskLoader extends DataLoader<Response<List<Track>>> {

      private final SpotifyService mSpotifyService;
      private       String         mArtistId;

      public TopTrackTaskLoader(Context context, String artistId) {
         super(context);
         SpotifyApi spotifyApi = new SpotifyApi();
         mSpotifyService = spotifyApi.getService();
         this.mArtistId = artistId;
      }

      @Override
      public boolean isForceLoad() {
         return true;
      }

      @Override
      public Response<List<Track>> loadInBackground() {

         Response<List<Track>> response = new Response<>();
         response.setData(Collections.EMPTY_LIST);

         Tracks tracks;
         try {
            SharedPreferences mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getContext());
            String spotifyMarket = mSharedPreference.getString(getContext().getString(R.string.pref_key_spotify_market),
                    getContext().getString(R.string.default_spotify_market));
            Map<String, Object> maps = new HashMap<>();
            maps.put(SpotifyService.COUNTRY, spotifyMarket);
            tracks = mSpotifyService.getArtistTopTrack(mArtistId, maps);
         } catch (RetrofitError cause) {
            cause.printStackTrace();
            response.setThrowable(RetrofitErrorHandler.handleError(cause));
            return response;
         }

         if (tracks != null && tracks.tracks != null) {
            response.setData(tracks.tracks);
            return response;
         }

         return response;
      }
   }

   /**
    * Top Track adapter
    */
   public class TopTrackAdapter extends MyRecyclerAdapter<List<Track>,
           TopTrackAdapter.ViewHolder, Track> {

      public TopTrackAdapter(List<Track> data) {
         super(data);
      }

      @Override
      public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
         View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_track,
                 parent, false);
         return new ViewHolder(v);
      }

      @Override
      public void onBindViewHolder(ViewHolder holder, int position) {
         Track track = getItem(position);

         /**
          * we will use resource to store the width setting so that we can modify
          * them on larger device if necessary
          */
         Image image = ImageUtils.getOptimumImage(track.album.images,
                 getResources().getInteger(R.integer.default_list_image_width));

         if (image != null) {
            Glide.with(TopTrackFragment.this)
                    .load(image.url)
                    .error(R.drawable.empty)
                    .into(holder.ivTrackThumbnail);
         } else {
            holder.ivTrackThumbnail.setImageResource(R.drawable.empty);
         }
         holder.tvAlbumName.setText(track.album.name);
         holder.tvTrackName.setText(track.name);
         holder.tvPosition.setText(String.valueOf(++position));

      }

      public class ViewHolder extends MyRecyclerViewHolder {
         final TextView  tvPosition;
         final TextView  tvTrackName;
         final TextView  tvAlbumName;
         final ImageView ivTrackThumbnail;

         public ViewHolder(View v) {
            super(v);
            tvPosition = (TextView) v.findViewById(R.id.tv_position);
            tvTrackName = (TextView) v.findViewById(R.id.tv_track_name);
            tvAlbumName = (TextView) v.findViewById(R.id.tv_album_name);
            ivTrackThumbnail = (ImageView) v.findViewById(R.id.img_track_thumbnail);
         }

         @Override
         public void onClick(View view) {
            super.onClick(view);
            if (getRecyclerCallbacks() != null) {
               getRecyclerCallbacks().OnItemClick(view, getAdapterPosition());
            }
         }
      }
   }
}
