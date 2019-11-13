package com.bkav.mymusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MediaPlaybackService extends Service {
    private static final String NOTIFICATION_CHANNEL_ID = "1";
    public static final String ACTION_PERVIOUS = "xxx.yyy.zzz.ACTION_PERVIOUS";
    public static final String ACTION_PLAY = "xxx.yyy.zzz.ACTION_PLAY";
    public static final String ACTION_NEXT = "xxx.yyy.zzz.ACTION_NEXT";
    private Binder binder = new MusicBinder();
    private MediaPlayer mMediaPlayer = null;
    private Listenner mListenner;
    private String mPath = "";
    private String mArtist = "";
    private String mNameSong = "";
    private int mPositionCurrent = 0;
    private int mindex;
    private int mLoopSong;// mLoopSong =0 (ko lap)// mLoopSong=-1 (lap ds) //mLoopSong =1 (lap 1)
    private boolean mShuffleSong;
    private List<Song> mListAllSong = new ArrayList<>();
    private String SHARED_PREFERENCES_NAME = "com.bkav.mymusic";
    private SharedPreferences mSharePreferences;
    private int mDuration;
    private String mURL = "content://com.bkav.provider";
    private Uri mURISong = Uri.parse(mURL);
    private ConnectSeviceFragmentInterface mConnectSeviceFragment2;

    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // Pause playback
                mMediaPlayer.pause();
                showNotification(mNameSong,mArtist, mPath);
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                mMediaPlayer.start();
                showNotification(mNameSong,mArtist, mPath);
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
              playingSong();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mSharePreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);// move Service
        mPositionCurrent = mSharePreferences.getInt("position", 0);
        mLoopSong = mSharePreferences.getInt("mLoopSong", 0);
        mShuffleSong = mSharePreferences.getBoolean("mShuffleSong", false);
        Gson gson = new Gson();
        String json = mSharePreferences.getString("Songs", "");
        if (!json.isEmpty()) {
            Type type = new TypeToken<ArrayList<Song>>() {
            }.getType();
            mListAllSong = gson.fromJson(json, type);
            for (int i = 0; i <= mListAllSong.size() - 1; i++) {//
                if (mPositionCurrent == mListAllSong.get(i).getId()) {
                    mindex = i;
                    mNameSong = mListAllSong.get(i).getName();
                    mArtist = mListAllSong.get(i).getSinger();
                    mPath = mListAllSong.get(i).getFile();
                    mDuration = mListAllSong.get(i).getDuration();
                }
            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isMusicPlay()) {
            Log.d("getAction", intent.getAction() + "");
            switch (intent.getAction()) {
                case ACTION_PERVIOUS:
                    previousSong();
                    break;
                case ACTION_NEXT:
                    nextSong();
                    break;
                case ACTION_PLAY:
                    if (mMediaPlayer.isPlaying()) {
                        pauseSong();
                    } else {
                        playingSong();
                    }
                    mConnectSeviceFragment2.onActionConnectSeviceFragment();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void getListenner(Listenner listenner) {
        this.mListenner = listenner;
    }

    public void getListenner2(ConnectSeviceFragmentInterface listenner2) {
        this.mConnectSeviceFragment2 = listenner2;
    }

    public String getmNameSong() {
        return mNameSong;
    }

    public MediaPlayer getmMediaPlayer() {
        return mMediaPlayer;
    }

    public String getmPath() {
        return mPath;
    }

    public String getmArtist() {
        return mArtist;
    }

    public int getmLoopSong() {
        return mLoopSong;
    }

    public void setMindex(int mindex) {
        this.mindex = mindex;
    }

    public void setmLoopSong(int mLoopSong) {
        this.mLoopSong = mLoopSong;
    }

    public boolean ismShuffleSong() {
        return mShuffleSong;
    }

    public void setmShuffleSong(boolean mShuffleSong) {
        this.mShuffleSong = mShuffleSong;
    }

    public void setmListAllSong(List<Song> mListAllSong) {
        this.mListAllSong = mListAllSong;
    }

    public List<Song> getmListAllSong() {
        return mListAllSong;
    }

    public void showNotification(String nameSong, String artist, String path) {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, ActivityMusic.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent previousIntent = new Intent(ACTION_PERVIOUS);
        PendingIntent previousPendingIntent = null;

        Intent playIntent = new Intent(ACTION_PLAY);
        PendingIntent playPendingIntent = null;

        Intent nextIntent = new Intent(ACTION_NEXT);
        PendingIntent nextPendingIntent = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            previousPendingIntent = PendingIntent.getForegroundService(this, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            playPendingIntent = PendingIntent.getService(getApplicationContext(), 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            nextPendingIntent = PendingIntent.getService(getApplicationContext(), 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        RemoteViews mCustomContentView = new RemoteViews(getPackageName(), R.layout.sub_notification);
        RemoteViews mCustomBigContentView = new RemoteViews(getPackageName(), R.layout.notification);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_music_note_black_24dp);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setCustomContentView(mCustomContentView);
        builder.setCustomBigContentView(mCustomBigContentView);
        builder.setContentIntent(pendingIntent);
        mCustomBigContentView.setTextViewText(R.id.text_name_play_song, nameSong);
        mCustomBigContentView.setTextViewText(R.id.text_name_singer, artist);
        mCustomBigContentView.setImageViewResource(R.id.image_play,isPlaying() ? R.drawable.ic_pause_circle_filled_black_50dp : R.drawable.ic_play_circle_filled_black_50dp);
        mCustomBigContentView.setOnClickPendingIntent(R.id.image_previous, previousPendingIntent);
        mCustomBigContentView.setOnClickPendingIntent(R.id.image_play, playPendingIntent);
        mCustomBigContentView.setOnClickPendingIntent(R.id.image_next, nextPendingIntent);
        if (imageArtist(path) != null) {
            mCustomBigContentView.setImageViewBitmap(R.id.image_album, imageArtist(path));
        } else
            mCustomBigContentView.setImageViewResource(R.id.image_album, R.drawable.default_cover_art);
/////========
        mCustomContentView.setImageViewResource(R.id.image_play, isPlaying() ? R.drawable.ic_pause_circle_filled_black_50dp : R.drawable.ic_play_circle_filled_black_50dp);
        mCustomContentView.setOnClickPendingIntent(R.id.image_previous, previousPendingIntent);
        mCustomContentView.setOnClickPendingIntent(R.id.image_play, playPendingIntent);
        mCustomContentView.setOnClickPendingIntent(R.id.image_next, nextPendingIntent);
        if (imageArtist(path) != null) {
            mCustomContentView.setImageViewBitmap(R.id.image_album, imageArtist(path));
        } else
            mCustomContentView.setImageViewResource(R.id.image_album, R.drawable.default_cover_art);
        startForeground(1, builder.build());
    }

    public void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel musicServiceChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Music Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            musicServiceChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(musicServiceChannel);
        }
    }

    public boolean isPlaying() {
        if (mMediaPlayer.isPlaying())
            return true;
        else
            return false;
    }

    public void seekToSong(int getProgress) {
        mMediaPlayer.seekTo(getProgress);
    }

    public int getmPosition() {
        return mPositionCurrent;
    }

    public void setmPosition(int mPosition) {
        this.mPositionCurrent = mPosition;
    }

    public int getCurrentPositionSong() {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDurationSong() {
        return mMediaPlayer.getDuration();
    }

    public void playSong(int mPosition) {
        mPositionCurrent = mPosition;
        mMediaPlayer = new MediaPlayer();
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        try {
            //  Log.d("play song", mPosition + "//" + mListAllSong.size());
            for (int i = 0; i <= mListAllSong.size() - 1; i++) {
                if (mListAllSong.get(i).getId() == mPosition) {
                    mindex = i;
                    Uri content_uri = Uri.parse(mListAllSong.get(i).getFile());
                    mMediaPlayer.setDataSource(getApplicationContext(), content_uri);
                    mMediaPlayer.prepare();
                    mMediaPlayer.setWakeMode(getApplicationContext(),
                            PowerManager.PARTIAL_WAKE_LOCK);
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    focusSevice();
                    mPath = mListAllSong.get(i).getFile();
                    mNameSong = mListAllSong.get(i).getName();
                    mArtist = mListAllSong.get(i).getSinger();
                    mDuration = mMediaPlayer.getDuration();

                    showNotification(mListAllSong.get(i).getName(), mListAllSong.get(i).getSinger(), mPath);
                    if (mListenner != null)
                        mListenner.onItemListenner();
                    mConnectSeviceFragment2.onActionConnectSeviceFragment();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ///SharedPreferences
        mSharePreferences = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharePreferences.edit();
        editor.putInt("position", this.mPositionCurrent);
        editor.putInt("mLoopSong", mLoopSong);
        editor.putBoolean("mShuffleSong", mShuffleSong);
        Gson gson = new Gson();
        String json = gson.toJson(mListAllSong);
        editor.putString("Songs", json);
        editor.commit();

    }

    public int actionLike() {
        String selection = " id_provider =" + mPositionCurrent;
        Cursor c1 = getContentResolver().query(mURISong, null, selection, null, null);
        if (c1.moveToFirst() && c1 != null) {
            do {
                return c1.getInt(c1.getColumnIndex(FavoriteSongsProvider.FAVORITE));
            } while (c1.moveToNext());
        }
        return 0;
    }

    public void playingSong() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
          focusSevice();
        }
        if (mListenner != null) {
            mListenner.onItemListenner();
        }
        showNotification(mNameSong, mArtist, mPath);
    }

    public void pauseSong() {
        mMediaPlayer.pause();
        if (mListenner != null) {
            mListenner.onItemListenner();
        }
        showNotification(mNameSong, mArtist, mPath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH);
        }
    }

    public void previousSong() {
        mMediaPlayer.pause();
        if (getCurrentPositionSong() <= 3000) {
            if (mShuffleSong == true) {
                mindex = actionShuffleSong();
                //  mPositionCurrent = mListAllSong.get(mindex).getId();
            } else {
                if (mindex == 0) {
                    mindex = mListAllSong.size() - 1;
                } else
                    mindex--;
            }
            mPositionCurrent = mListAllSong.get(mindex).getId();
            playSong(mPositionCurrent);
            // mListenner.actionNotification();
        } else {
            playSong(mPositionCurrent);
        }
    }

    public void nextSong() {
        mMediaPlayer.pause();
        if (mShuffleSong == true) {
            mindex = actionShuffleSong();
        } else {
            if (mindex == mListAllSong.size() - 1)
                mindex = 0;
            else
                mindex++;
        }
        mPositionCurrent = mListAllSong.get(mindex).getId();
        playSong(mPositionCurrent);
        //  mListenner.actionNotification();
    }

    public int actionShuffleSong() {
        Random rd = new Random();
        int result = rd.nextInt(mListAllSong.size() - 1);
        return result;
    }

    public String getDuration() {
        SimpleDateFormat formmatTime = new SimpleDateFormat("mm:ss");
        return formmatTime.format(mMediaPlayer.getDuration());
    }

    public boolean isMusicPlay() {
        if (mMediaPlayer != null)
            return true;
        return false;
    }

    public void onCompletionSong() {
        mMediaPlayer.pause();
        if (mLoopSong == 0) {
            if (mShuffleSong == true) {
                mindex = actionShuffleSong();
                mPositionCurrent = mListAllSong.get(mindex).getId();
                playSong(mPositionCurrent);
            } else {
                if (mindex < mListAllSong.size() - 1) {
                    mindex++;
                    mPositionCurrent = mListAllSong.get(mindex).getId();
                    playSong(mPositionCurrent);
                }
                if(mindex == mListAllSong.size() - 1){
                    mMediaPlayer.pause();
                }
            }
        } else {
            if (mLoopSong == -1) {
                if (mShuffleSong == true) {
                    mindex = actionShuffleSong();
                    mPositionCurrent = mListAllSong.get(mindex).getId();
                    playSong(mPositionCurrent);
                } else {
                    if (mindex == mListAllSong.size() - 1) {
                        mindex = 0;
                    } else {
                        mindex++;
                    }
                }
            }
            mPositionCurrent = mListAllSong.get(mindex).getId();
            playSong(mPositionCurrent);
        }

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

    //=============focus
    public void focusSevice() {
        AudioManager audioManager = (AudioManager) getApplication().getSystemService(Context.AUDIO_SERVICE);
        int res = audioManager.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, // Music streaming
                AudioManager.AUDIOFOCUS_GAIN); // Permanent focus
        if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
           mMediaPlayer.start();
        }
    }
    //==============
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public interface Listenner {
        void onItemListenner();
        // void actionNotification();
    }

    public interface ConnectSeviceFragmentInterface {
        void onActionConnectSeviceFragment();
    }

    class MusicBinder extends Binder {
        public MediaPlaybackService getMusicBinder() {
            return MediaPlaybackService.this;
        }
    }
}
