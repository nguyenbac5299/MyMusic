package com.bkav.mymusic;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> implements Filterable {
    private List<Song> mListSong = new ArrayList<>();
    private List<Song> mSong;
    //private ArrayList<Song> mListFavoriteSongs = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context mContext;
    private OnClickItemView mClickItemView;
    private MediaPlaybackService mMusicService;
    private String mTypeSong = "";


    public MusicAdapter(OnClickItemView mClickItemView, Context context) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        this.mClickItemView = mClickItemView;

    }

    public void setmMusicService(MediaPlaybackService mMusicService) {
        this.mMusicService = mMusicService;
    }

    public void setmTypeSong(String mTypeSong) {
        this.mTypeSong = mTypeSong;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_recyclerview, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        if (mSong != null) {
            final Song current = mSong.get(position);
            holder.mStt.setText((position+1) + "");
            holder.mNameSong.setText(current.getName());
            SimpleDateFormat formmatTime = new SimpleDateFormat("mm:ss");
            holder.mHours.setText(formmatTime.format(current.getDuration()));

            final Song finalCurrent = current;
            holder.mConstraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClickItemView.clickItem(finalCurrent, position);
                }
            });

            if (mMusicService != null) {
              //  mMusicService.setmListAllSong(mSong);
                if (mMusicService.getmNameSong().equals(mSong.get(position).getName())) {
                    holder.mStt.setText("");
                    holder.mNameSong.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                    holder.mStt.setBackgroundResource(R.drawable.ic_equalizer_black_24dp);
                    //holder.mStt.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_equalizer_black_24dp, 0, 0, 0);

                } else {
                    holder.mNameSong.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                    holder.mStt.setBackgroundResource(R.drawable.ic_equalizer_while_24dp);
                    //    holder.mStt.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_equalizer_while_24dp, 0, 0, 0);
                }
            }


            holder.mMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(mContext, holder.mMore);
                    if (mTypeSong.equals("AllSong")) {
                        popupMenu.inflate(R.menu.add_song);
                    }
                    if (mTypeSong.equals("FavoriteSong")) {
                        popupMenu.inflate(R.menu.remove_song);
                    }

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.addFavorite:
                                    ContentValues values = new ContentValues();
                                    values.put(FavoriteSongsProvider.FAVORITE, 2);
                                    mContext.getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, values, FavoriteSongsProvider.ID_PROVIDER + "= " + current.getId(), null);
                                    Toast.makeText(mContext, "addFavorite song //" + mMusicService.getmNameSong(), Toast.LENGTH_SHORT).show();
                                    return true;
                                case R.id.removeFavorite:
                                    ContentValues values1 = new ContentValues();
                                    values1.put(FavoriteSongsProvider.FAVORITE, 1);
                                    values1.put(FavoriteSongsProvider.COUNT, 0);
                                    mContext.getContentResolver().update(FavoriteSongsProvider.CONTENT_URI, values1, FavoriteSongsProvider.ID_PROVIDER + "= " + current.getId(), null);

                                    Toast.makeText(mContext, "removeFavorite song //" + mMusicService.getmNameSong(), Toast.LENGTH_SHORT).show();// lôi
                                    return true;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();

                }
            });
        } else {
            holder.mNameSong.setText("No Song");
        }
    }


    @Override
    public int getItemCount() {
        if (mSong != null)
            return mSong.size();
        else
            return 0;
    }

    public void setSong(List<Song> songs) {
        mSong = songs;
        Log.d("size2", songs.size() + "//");
        notifyDataSetChanged();
    }

    public void updateList(List<Song> songs) {
        mSong = songs;
        mListSong = new ArrayList<>(mSong);
        notifyDataSetChanged();
    }

    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Song> filterList = new ArrayList<>();
            if (charSequence == null || charSequence.length() == 0) {
                filterList.addAll(mListSong);
            } else {
                String filterPattern = unAccent(charSequence.toString().toLowerCase().trim());

                for (Song song : mListSong) {
                    if (unAccent(song.getName().toLowerCase()).contains(filterPattern)) {
                        filterList.add(song);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filterList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mSong.clear();
            mSong.addAll((Collection<? extends Song>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public static String unAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replaceAll("Đ", "D").replace("đ", "d");
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mNameSong, mHours;
        ImageButton mMore;
        TextView mStt;
        ConstraintLayout mConstraintLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mConstraintLayout = itemView.findViewById(R.id.constraintLayoutItem);
            mNameSong = itemView.findViewById(R.id.text_name_song);
            mHours = itemView.findViewById(R.id.hours);
            mStt = itemView.findViewById(R.id.text_ID);
            mMore = itemView.findViewById(R.id.image_more);
        }
    }

    public interface OnClickItemView {
        void clickItem(Song song, int index);
    }
}
