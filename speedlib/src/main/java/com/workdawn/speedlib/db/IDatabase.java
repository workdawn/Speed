package com.workdawn.speedlib.db;

import android.util.SparseArray;

import com.workdawn.speedlib.model.DownloadModel;

public interface IDatabase {

    SparseArray<DownloadModel> find(String uniqueId);

    boolean insert(DownloadModel downloadModel);

    boolean delete(String uniqueId);

    boolean delete(String uniqueId, int threadId);

    boolean update(DownloadModel downloadModel);

    void close();
}
