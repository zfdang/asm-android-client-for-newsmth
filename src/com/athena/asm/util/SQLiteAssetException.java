package com.athena.asm.util;

import android.database.sqlite.SQLiteException;

public class SQLiteAssetException extends SQLiteException {

	public SQLiteAssetException() {}

    public SQLiteAssetException(String error) {
        super(error);
    }
}
