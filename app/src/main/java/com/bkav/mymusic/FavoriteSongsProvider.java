package com.bkav.mymusic;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.HashMap;

public class FavoriteSongsProvider extends ContentProvider {

    private static final String DB_SONGS = "db_songs1";
    private static final String AUTHORITY = "com.bkav.provider";
    private static final int DB_VESION = 1;
    public static final String CONTENT_PATH = "listsongs";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CONTENT_PATH);


    private static final String TABLE_FAVORITESONGS = "favoritesongs";
    public static final String ID_FAVORITE = "id";
    public static final String ID_PROVIDER = "id_provider";
    public static final String FAVORITE = "is_favorite";
    public static final String COUNT = "count_of_play";
    static final String CREATE_TABLE_FAVORITESONGS =
            "create table " + TABLE_FAVORITESONGS + "( " + ID_FAVORITE + " integer primary key autoincrement," +
                    ID_PROVIDER + " integer ," +
                    FAVORITE + " integer default 0, " + //  0 : not like // 1 : stop like // 2 : like
                    COUNT + " integer default 0  );"; // number click // if count =3 => is_favorite=1 expect  is_favorite=1

    private static HashMap<String, String> HASMAP;
    private static final UriMatcher sUriMatcher;
    private static final int URI_ALL_ITEM_CODE = 1;
    private static final int URI_ONE_ITEM_CODE = 2;
    private SQLiteDatabase database;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, CONTENT_PATH, URI_ALL_ITEM_CODE);
        sUriMatcher.addURI(AUTHORITY, CONTENT_PATH + "/#", URI_ONE_ITEM_CODE);
    }

    private static class FavoriteSongsDatabase extends SQLiteOpenHelper {


        public FavoriteSongsDatabase(@Nullable Context context) {
            super(context, DB_SONGS, null, DB_VESION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            //sqLiteDatabase.execSQL(CREATE_TABLE_LISTSONGS);
            sqLiteDatabase.execSQL(CREATE_TABLE_FAVORITESONGS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITESONGS);
            //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTSONGS);
            onCreate(sqLiteDatabase);
        }
    }

    @Override
    public boolean onCreate() {

        FavoriteSongsDatabase mFavoriteSongsDatabase = new FavoriteSongsDatabase(getContext());
        database = mFavoriteSongsDatabase.getWritableDatabase();
        if (database == null)
            return false;
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String seclection, String[] seclectionArg, String orderBy) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(TABLE_FAVORITESONGS);
        switch (sUriMatcher.match(uri)) {
            case URI_ALL_ITEM_CODE:
                queryBuilder.setProjectionMap(HASMAP);
                break;
            case URI_ONE_ITEM_CODE:
                queryBuilder.appendWhere(ID_FAVORITE + "=" + uri.getPathSegments().get(1));
                break;
            default:


        }
        if (orderBy == null || orderBy == "") {
            orderBy = ID_FAVORITE;
        }
        Cursor cursor = queryBuilder.query(database, projection, seclection, seclectionArg, null, null, orderBy);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case URI_ALL_ITEM_CODE:
                return "com.bkav.dir/com.bkav." + CONTENT_PATH;
            case URI_ONE_ITEM_CODE:
                return "com.bkav.item/com.bkav." + CONTENT_PATH;
            default:
                throw new IllegalArgumentException("Unsupported" + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long rowID = database.insert(TABLE_FAVORITESONGS, "", contentValues);

        if (rowID > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }
        throw new SQLException("Failed to add a record into " + uri);

    }

    @Override
    public int delete(Uri uri, String selection, String[] slectionArg) {
        // return mFavoriteSongsDatabase.delete(selection, slectionArg);
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArg) {
        //  return mFavoriteSongsDatabase.update(contentValues, selection, selectionArg);
        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case URI_ALL_ITEM_CODE:
                count = database.update(TABLE_FAVORITESONGS, contentValues, selection, selectionArg);
                break;
            case URI_ONE_ITEM_CODE:
                count = database.update(TABLE_FAVORITESONGS, contentValues,
                        ID_FAVORITE + " = " + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? "AND (" + selection + ')' : ""), selectionArg);
                break;
            default:
                //throw new IllegalAccessException("Unknow URI "+uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
