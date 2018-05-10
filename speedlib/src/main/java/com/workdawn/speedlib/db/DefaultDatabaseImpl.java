package com.workdawn.speedlib.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import com.workdawn.speedlib.model.DownloadModel;


public class DefaultDatabaseImpl implements IDatabase {

    private SQLiteDatabase db;
    private DatabaseOpenHelper databaseOpenHelper = null;

    DefaultDatabaseImpl(Context context){
        databaseOpenHelper = new DatabaseOpenHelper(context);
    }

    @Override
    public SparseArray<DownloadModel> find(String uniqueId) {
        Cursor cursor = null;
        SparseArray<DownloadModel> models = new SparseArray<>();
        db = databaseOpenHelper.getReadableDatabase();
        try {
            String selectSql = "SELECT * FROM " + DatabaseOpenHelper.TABLE_NAME + " WHERE " + DownloadModel.UNIQUE_ID + "=? and " + DownloadModel.COMPLETE + "=?";
            cursor = db.rawQuery(selectSql, new String[]{ uniqueId, "0" });
            while (cursor.moveToNext()){
                DownloadModel downloadModel = new DownloadModel();
                downloadModel.setComplete(cursor.getInt(cursor.getColumnIndex(DownloadModel.COMPLETE)));
                downloadModel.setDownloadBytes(cursor.getLong(cursor.getColumnIndex(DownloadModel.DOWNLOAD_BYTES)));
                downloadModel.setDownloadUrl(cursor.getString(cursor.getColumnIndex(DownloadModel.DOWNLOAD_URL)));
                downloadModel.setEtag(cursor.getString(cursor.getColumnIndex(DownloadModel.E_TAG)));
                downloadModel.setLastModified(cursor.getString(cursor.getColumnIndex(DownloadModel.LAST_MODIFIED)));
                downloadModel.setFileTotalBytes(cursor.getLong(cursor.getColumnIndex(DownloadModel.FILE_TOTAL_BYTES)));
                downloadModel.setSavePath(cursor.getString(cursor.getColumnIndex(DownloadModel.SAVE_PATH)));
                downloadModel.setFileName(cursor.getString(cursor.getColumnIndex(DownloadModel.FILE_NAME)));
                int threadId = cursor.getInt(cursor.getColumnIndex(DownloadModel.THREAD_ID));
                downloadModel.setThreadId(threadId);
                downloadModel.setTotalBytes(cursor.getLong(cursor.getColumnIndex(DownloadModel.TOTAL_BYTES)));
                downloadModel.setUniqueId(cursor.getString(cursor.getColumnIndex(DownloadModel.UNIQUE_ID)));
                downloadModel.setStartRange(cursor.getLong(cursor.getColumnIndex(DownloadModel.START_RANGE)));
                downloadModel.setEndRange(cursor.getLong(cursor.getColumnIndex(DownloadModel.END_RANGE)));
                models.put(threadId, downloadModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return models;
    }

    @Override
    public boolean insert(DownloadModel downloadModel) {
        try {
            db = databaseOpenHelper.getWritableDatabase();
            ContentValues contentValues = processContentValues(downloadModel);
            db.insertOrThrow(DatabaseOpenHelper.TABLE_NAME, null, contentValues);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean delete(String uniqueId) {
        try {
            db = databaseOpenHelper.getWritableDatabase();
            db.delete(DatabaseOpenHelper.TABLE_NAME, DownloadModel.UNIQUE_ID + "=?", new String[]{uniqueId});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String uniqueId, int threadId) {
        try {
            db = databaseOpenHelper.getWritableDatabase();
            db.delete(DatabaseOpenHelper.TABLE_NAME, DownloadModel.UNIQUE_ID + "=? and " + DownloadModel.THREAD_ID + "=?", new String[]{uniqueId, threadId + ""});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(DownloadModel downloadModel) {
        try {
            ContentValues contentValues = processContentValues(downloadModel);
            db = databaseOpenHelper.getWritableDatabase();
            db.update(DatabaseOpenHelper.TABLE_NAME, contentValues, DownloadModel.UNIQUE_ID + "=? and "
                    + DownloadModel.THREAD_ID + "=?",new String[]{downloadModel.getUniqueId(), downloadModel.getThreadId()+""});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private ContentValues processContentValues(DownloadModel downloadModel){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DownloadModel.COMPLETE, downloadModel.getComplete());
        contentValues.put(DownloadModel.DOWNLOAD_BYTES, downloadModel.getDownloadBytes());
        contentValues.put(DownloadModel.DOWNLOAD_URL, downloadModel.getDownloadUrl());
        contentValues.put(DownloadModel.E_TAG, downloadModel.getEtag());
        contentValues.put(DownloadModel.LAST_MODIFIED, downloadModel.getLastModified());
        contentValues.put(DownloadModel.SAVE_PATH, downloadModel.getSavePath());
        contentValues.put(DownloadModel.FILE_NAME, downloadModel.getFileName());
        contentValues.put(DownloadModel.FILE_TOTAL_BYTES, downloadModel.getFileTotalBytes());
        contentValues.put(DownloadModel.THREAD_ID, downloadModel.getThreadId());
        contentValues.put(DownloadModel.TOTAL_BYTES, downloadModel.getTotalBytes());
        contentValues.put(DownloadModel.UNIQUE_ID, downloadModel.getUniqueId());
        contentValues.put(DownloadModel.START_RANGE, downloadModel.getStartRange());
        contentValues.put(DownloadModel.END_RANGE, downloadModel.getEndRange());
        return contentValues;
    }

    @Override
    public void close() {
        if(db != null){
            db.close();
        }
    }
}
