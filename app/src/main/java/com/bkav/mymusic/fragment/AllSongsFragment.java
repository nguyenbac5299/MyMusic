package com.bkav.mymusic.fragment;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.bkav.mymusic.FavoriteSongsProvider;
import com.bkav.mymusic.Song;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class AllSongsFragment extends BaseSongListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 1;
    private String SHARED_PREFERENCES_NAME = "com.bkav.mymusic";
    private SharedPreferences mSharePreferences;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_ID, null, this);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.ArtistColumns.ARTIST, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION};
        CursorLoader cursorLoader = new CursorLoader(getContext(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor c) {
        ArrayList<Song> listMusic = new ArrayList<>();
        int id = 0;
        mSharePreferences =   getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        boolean isCreate=mSharePreferences.getBoolean("create_db", false);
        Log.d("create_db F",mSharePreferences.getBoolean("create_db", false)+"//ok");
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            do {
                String path = c.getString(0);
                String album = c.getString(1);
                String artist = c.getString(2);
                String name = c.getString(3);
                String duration = c.getString(4);
                listMusic.add(new Song(id, name, path, artist, Integer.parseInt(duration)));
                //==========//
                if(isCreate==false){
                    ContentValues values = new ContentValues();
                    values.put(FavoriteSongsProvider.ID_PROVIDER, id);
                    values.put(FavoriteSongsProvider.FAVORITE, 0);
                    values.put(FavoriteSongsProvider.COUNT, 0);
                    Uri uri = getActivity().getContentResolver().insert(FavoriteSongsProvider.CONTENT_URI, values);
                    mSharePreferences = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = mSharePreferences.edit();
                    editor.putBoolean("create_db", true);
                    editor.commit();
                }
               // Toast.makeText(getContext(), "add song //" + mMusicService.getmNameSong(), Toast.LENGTH_SHORT).show();
                //========//
                Log.d("info", " Album :" + album);
                Log.d("Path :" + path, " Artist :" + artist + " Duration " + duration);
                id++;
            } while (c.moveToNext());
        }
        mAdapter.updateList(listMusic);
        setSong(listMusic);
        mAdapter.setmTypeSong("AllSong");
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (mAdapter != null) {
            mAdapter.setSong(new ArrayList<Song>());
        }
    }
}
