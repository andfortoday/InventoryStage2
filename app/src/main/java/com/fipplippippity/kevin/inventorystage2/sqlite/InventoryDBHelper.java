package com.fipplippippity.kevin.inventorystage2.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import com.fipplippippity.kevin.inventorystage2.sqlite.InventoryContract.InventoryEntry;

public class InventoryDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "inventory.db";
    private static final int DB_VERSION = 1;

    public InventoryDBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_INV_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COL_PRODUCT_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COL_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COL_QTY + " INTEGER NOT NULL DEFAULT 0, "
                + InventoryEntry.COL_SUPPLIER_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COL_SUPPLIER_PHONE + " TEXT NOT NULL"
                + ");";

        db.execSQL(CREATE_INV_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//      no need for upgrade at this point
    }
}
