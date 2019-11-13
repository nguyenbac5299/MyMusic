package com.bkav.mymusic.fragment;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bkav.mymusic.ActivityMusic;
import com.bkav.mymusic.FavoriteSongsProvider;
import com.bkav.mymusic.MediaPlaybackFragment;
import com.bkav.mymusic.MediaPlaybackService;
import com.bkav.mymusic.MusicAdapter;
import com.bkav.mymusic.R;
import com.bkav.mymusic.Song;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class BaseSongListFragment extends Fragment implements MusicAdapter.OnClickItemView {

    private RecyclerView mRecyclerView;
    protected MusicAdapter mAdapter;
    private ImageButton mClickPlay;
    private TextView mNameSong, mArtist;
    private ImageView mdisk;
    private ConstraintLayout constraintLayout;
    protected MediaPlaybackService mMusicService;
    private SharedPreferences mSharePreferences;
    private String SHARED_PREFERENCES_NAME = "com.bkav.mymusic";
    private boolean mExitService = false;
    private ArrayList<Song> mListAllSong = new ArrayList<>();
    private int position = 0;
    private MediaPlaybackFragment mMediaPlaybackFragment = new MediaPlaybackFragment();
    private String mURL = "content://com.bkav.provider";
    private Uri mURISong = Uri.parse(mURL);


    public void setSong(List<Song> songs) {
        this.mListAllSong = (ArrayList<Song>) songs;
        mAdapter.setSong(songs);
        if (mExitService == true) {
            updateUI();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.setmMusicService(mMusicService);
    }


    void initView(View view) {
        mRecyclerView = view.findViewById(R.id.recyclerview);
        mClickPlay = view.findViewById(R.id.image_play);
        mArtist = view.findViewById(R.id.text_name_singer);
     mdisk = view.findViewById(R.id.image_small_album);
        mNameSong = view.findViewById(R.id.text_name_play_song);
        mNameSong.setSelected(true);
        constraintLayout = view.findViewById(R.id.layout_controls);
        if (getActivity().findViewById(R.id.fragment_playback) != null)
            constraintLayout.setVisibility(View.GONE);
        else
            constraintLayout.setVisibility(View.VISIBLE);
    }


    @Override
    public void onStart() {
        super.onStart();
        mAdapter = new MusicAdapter(this, getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (mMusicService != null)
            mRecyclerView.scrollToPosition(mMusicService.getmPosition());
    }

    public Bitmap imageArtist(String path) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        byte[] data = mediaMetadataRetriever.getEmbeddedPicture();
        if (data != null) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_songs_fragment, container, false);
        initView(view);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        setHasOptionsMenu(true);
        mSharePreferences = this.getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);// move Service
        position = mSharePreferences.getInt("position", 0);
        Gson gson = new Gson();

        String json = mSharePreferences.getString("Songs", "");
        if (!json.isEmpty()) {
            Type type = new TypeToken<ArrayList<Song>>() {
            }.getType();
            mListAllSong = gson.fromJson(json, type);

            for (int i = 0; i <= mListAllSong.size() - 1; i++) {
                if (position== mListAllSong.get(i).getId()) {
                    mNameSong.setText(mListAllSong.get(i).getName());
                    mArtist.setText(mListAllSong.get(i).getSinger());
                    if (!mListAllSong.get(i).getFile().equals(""))
                        if (imageArtist(mListAllSong.get(i).getFile()) != null) {
                            mdisk.setImageBitmap(imageArtist(mListAllSong.get(i).getFile()));
                        } else
                            mdisk.setImageResource(R.drawable.default_cover_art);
                }
            }
        }

        mClickPlay.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                if (mMusicService != null) {
                    if (mMusicService.isMusicPlay()) {
                        if (mMusicService.isPlaying()) {
                            mMusicService.pauseSong();
                        } else {
                            mMusicService.playingSong();
                        }
                        updateUI();
                    } else {
                        mMusicService.setmPosition(mSharePreferences.getInt("position", 0));
                        mMusicService.playSong(mSharePreferences.getInt("position", 0));
                        updateUI();
                    }
                }
            }
        });

        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity().findViewById(R.id.fragment_list_song) != null) {
                    if (!mMusicService.isMusicPlay()) {
                        mMusicService.playSong(position);
                    }
                    mMediaPlaybackFragment.setmMusicService(mMusicService);
                    getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_list_song, mMediaPlaybackFragment).commit();
                }
            }
        });


        ((ActivityMusic) getActivity()).setiConnectActivityAndBaseSong(new ActivityMusic.IConnectActivityAndBaseSong() {
            @Override
            public void connectActivityAndBaseSong() {
                mMusicService = ((ActivityMusic) getActivity()).mMusicService;
                // Log.e("service2", "connectActivityAndBaseSong: "+mMusicService.getmNameSong());
                mAdapter.setmMusicService(mMusicService);
                updateUI();
                mMusicService.getListenner(new MediaPlaybackService.Listenner() {
                    @Override
                    public void onItemListenner() {
                        updateUI();
                    }
                });
            }
        });

        if (((ActivityMusic) getActivity()).mMusicService != null) {
            mMusicService = ((ActivityMusic) getActivity()).mMusicService;
            updateUI();
            mMusicService.getListenner(new MediaPlaybackService.Listenner() {
                @Override
                public void onItemListenner() {
                    updateUI();
                }
            });
        }

        return view;
    }


    public void updateUI() {
        if (mMusicService.isMusicPlay()) {
            Log.d("notification", "ok" + mMusicService.getmNameSong() + "//" + mMusicService.isPlaying());
            mRecyclerView.scrollToPosition(mMusicService.getmPosition());
             UpdateTime();
            if (mMusicService.isPlaying()) {
                mClickPlay.setBackgroundResource(R.drawable.ic_pause);
            } else {
                mClickPlay.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
            }

            if (!mMusicService.getmPath().equals(""))
                if (mMusicService.imageArtist(mMusicService.getmPath()) != null) {
                    mdisk.setImageBitmap(mMusicService.imageArtist(mMusicService.getmPath()));
                } else
                    mdisk.setImageResource(R.drawable.default_cover_art);

            mNameSong.setText(mMusicService.getmNameSong());
            mArtist.setText(mMusicService.getmArtist());

        }
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    public void UpdateTime() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMusicService.getmMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer media) {
                        mMusicService.onCompletionSong();
                        updateUI();
                    }
                });
                handler.postDelayed(this, 500);
            }
        }, 100);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    @Override
    public void clickItem(Song songs, int position) {
        //  Log.d("size",mListAllSong.size()+"//"+position+"///"+mMusicService);
        mMusicService.setmListAllSong(mListAllSong);
        if (mMusicService.isMusicPlay()) {
            mMusicService.pauseSong();
        }
        mMusicService.setMindex(position);
        mMusicService.playSong(songs.getId());
        mNameSong.setText(songs.getName());
        mArtist.setText(songs.getSinger());
        ///===========///
        String selection = " id_provider =" + songs.getId();
        Cursor c = getActivity().managedQuery(mURISong, null, selection, null, null);
        if (c.moveToFirst()) {
            do {
                //Log.d("ID",c.getString(c.getColumnIndex("id_provider")));
                if (c.getInt(c.getColumnIndex(FavoriteSongsProvider.FAVORITE)) != 1)
                    if (c.getInt(c.getColumnIndex(FavoriteSongsProvider.COUNT)) < 2) {
                        ContentValues values = new ContentValues();
                        values.put(FavoriteSongsProvider.COUNT, c.getInt(c.getColumnIndex(FavoriteSongsProvider.COUNT)) + 1);
                        getActivity().getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, values, FavoriteSongsProvider.ID_PROVIDER + "= " + songs.getId(), null);
                        //   Log.d("ID",c.getString(c.getColumnIndex(FavoriteSongsProvider.COUNT))+"//"+c.getString(c.getColumnIndex(FavoriteSongsProvider.FAVORITE)));
                    } else {
                        if (c.getInt(c.getColumnIndex(FavoriteSongsProvider.COUNT)) == 2) {
                            ContentValues values = new ContentValues();
                            values.put(FavoriteSongsProvider.COUNT, 0);
                            values.put(FavoriteSongsProvider.FAVORITE, 2);
                            getActivity().getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, values, FavoriteSongsProvider.ID_PROVIDER + "= " + songs.getId(), null);
                            //   Log.d("ID1", c.getString(c.getColumnIndex(FavoriteSongsProvider.COUNT)) + "//" + c.getString(c.getColumnIndex(FavoriteSongsProvider.FAVORITE)));
                        }
                    }

            } while (c.moveToNext());

        }
        Log.d("click :", songs.getName() + "//" + songs.getId());
        updateUI();
    }


}
