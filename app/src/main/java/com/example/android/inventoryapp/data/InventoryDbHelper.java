package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {

    // Will be used to display messages in the Log
    private static final String TAG = InventoryDbHelper.class.getSimpleName();

    // The following constant contains the data base version
    public static final int DATABASE_VERSION = 1;

    // The following constant contains the data base name
    public static final String DATABASE_NAME = "products.db";

    public InventoryDbHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    private final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + InventoryEntry.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_INVENTORY_TABLE =  "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_ITEM_IMAGE + " TEXT, "
                + InventoryEntry.COLUMN_ITEM_PRICE + " DOUBLE NOT NULL, "
                + InventoryEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0,"
                + InventoryEntry.COLUMN_ITEM_SOLD + " INTEGER DEFAULT 0,"
                + InventoryEntry.COLUMN_ITEM_SHIPPED + " INTEGER DEFAULT 0,"
                + InventoryEntry.COLUMN_ITEM_SUPPLIER + " TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}