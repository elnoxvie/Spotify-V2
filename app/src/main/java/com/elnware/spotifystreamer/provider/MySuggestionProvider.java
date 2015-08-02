package com.elnware.spotifystreamer.provider;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Provider to store the recent query values
 *
 * Created by elnoxvie on 6/21/15.
 */
public class MySuggestionProvider extends SearchRecentSuggestionsProvider {
   public final static String AUTHORITY = "com.elnware.spotifystreamer.provider.MySuggestionProvider";
   public final static int MODE = DATABASE_MODE_QUERIES;

   public MySuggestionProvider() {
      setupSuggestions(AUTHORITY, MODE);
   }
}
