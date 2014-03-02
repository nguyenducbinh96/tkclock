package com.tkclock.adapters;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
	private final Context mCtx;
    
	private static final String DATABASE_NAME = "tkclock";
	private static final int DATABASE_VERSION = 1;
	private Map<String, Boolean> mTableStatus;
              
    public DbHelper(Context context) { 
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);// 1? its Database Version
    	this.mCtx = context;
    	mTableStatus = new HashMap<String, Boolean>();
    	
    	mTableStatus.put(TkDbAlarmMng.TABLE_NAME, true);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		mTableStatus.put(TkDbAlarmMng.TABLE_NAME, false);
		db.execSQL(TkDbAlarmMng.TABLE_CREATE);		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TkDbAlarmMng.TABLE_CREATE);
        onCreate(db);		
	}
	
	private boolean checkTable(SQLiteDatabase db, String tableName)
	{
	    if (tableName == null || db == null || !db.isOpen())
	    {
	        return false;
	    }
	    Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", tableName});
	    if (!cursor.moveToFirst())
	    {
	        return false;
	    }
	    int count = cursor.getInt(0);
	    cursor.close();
	    return count > 0;
	}
	
	public boolean isTableExists(String table_name) {
		return mTableStatus.get(table_name);
	}

	private boolean isDbExist() {
		String path = mCtx.getDatabasePath(DATABASE_NAME).toString();
		File dbFile = new File(path + DATABASE_NAME);
		return dbFile.exists();
	}
}
