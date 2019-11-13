package com.bkav.mymusic.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

public class FavoriteSongsFragment extends BaseSongListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID = 1;
    private ArrayList<Song> mListAllSong =new ArrayList<>();

    public FavoriteSongsFragment(ArrayList<Song> mListAllSong) {
        this.mListAllSong = mListAllSong;
       // Log.e("song Favo",mListAllSong.size()+"//");
    }

    public FavoriteSongsFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       // Toast.makeText(getContext(), "F//"+mMusicService, Toast.LENGTH_SHORT).show();
        getLoaderManager().initLoader(LOADER_ID, null, this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String URL = "content://com.bkav.provider";
        Uri uriSongs = Uri.parse(URL);
        return new CursorLoader(getContext(),uriSongs, null, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        ArrayList<Song> mListFavoriteSongs = new ArrayList<>();
        Song song =null;
        if (cursor.moveToFirst()) {
            do {

                for(int i=0;i<mListAllSong.size();i++){
                     if(mListAllSong.get(i).getId()== cursor.getInt(cursor.getColumnIndex(FavoriteSongsProvider.ID_PROVIDER))){
                        if( cursor.getInt(cursor.getColumnIndex(FavoriteSongsProvider.FAVORITE)) == 2){
                            song = new Song( mListAllSong.get(i).getId(),
                                    mListAllSong.get(i).getName(),
                                    mListAllSong.get(i).getFile(),
                                    mListAllSong.get(i).getSinger(),
                                    mListAllSong.get(i).getDuration());
                            if(song !=null)
                                 mListFavoriteSongs.add(song);
                            else
                                getActivity().getContentResolver().delete(FavoriteSongsProvider.CONTENT_URI,FavoriteSongsProvider.ID_PROVIDER +"= "+mListAllSong.get(i).getId(),null);

                         }
                     }
                }
            } while (cursor.moveToNext());

        }
         mAdapter.updateList(mListFavoriteSongs);
         setSong(mListFavoriteSongs);
        mAdapter.setmTypeSong("FavoriteSong");
       // mAdapter.setmListFavoriteSongs(mListAllSong);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if(mAdapter!=null){
            mAdapter.setSong(new ArrayList<Song>());
        }
    }
}
