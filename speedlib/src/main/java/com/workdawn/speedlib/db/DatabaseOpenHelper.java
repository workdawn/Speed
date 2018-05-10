package com.workdawn.speedlib.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.workdawn.speedlib.model.DownloadModel;

/**
 * Created on 2018/4/27.
 * @author workdawn
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "speed.db";
    private final static int DB_VERSION = 1;
    public final static String TABLE_NAME = "downloadrecord";

    public DatabaseOpenHelper (Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + "( id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DownloadModel.COMPLETE + " INTEGER, " +
                DownloadModel.DOWNLOAD_URL + " VARCHAR, " +
                DownloadModel.UNIQUE_ID + " VARCHAR, " +
                DownloadModel.E_TAG + " VARCHAR, " +
                DownloadModel.LAST_MODIFIED + " VARCHAR, " +
                DownloadModel.SAVE_PATH + " VARCHAR, " +
                DownloadModel.FILE_NAME + " VARCHAR, " +
                DownloadModel.FILE_TOTAL_BYTES + " INTEGER, " +
                DownloadModel.START_RANGE + " INTEGER, " +
                DownloadModel.END_RANGE + " INTEGER, " +
                DownloadModel.TOTAL_BYTES + " INTEGER, " +
                DownloadModel.DOWNLOAD_BYTES + " INTEGER, " +
                DownloadModel.THREAD_ID + " INTEGER " +
                ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
