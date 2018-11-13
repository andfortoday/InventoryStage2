package com.fipplippippity.kevin.inventorystage2.sqlite;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fipplippippity.kevin.inventorystage2.R;
import com.fipplippippity.kevin.inventorystage2.sqlite.InventoryContract.InventoryEntry;

public class InventoryProvider extends ContentProvider {

    private static final int INVENTORY_BASE_RETURN_CODE = 95;
    private static final String INVENTORY_BASE_PATH = "inventory";

    private static final int INVENTORY_ROW_ID_RETURN_CODE = 96;
    private static final String INVENTORY_ROW_ID_PATH = "inventory/#";

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    static {
//        match up the paths with the codes
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, INVENTORY_BASE_PATH, INVENTORY_BASE_RETURN_CODE);
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, INVENTORY_ROW_ID_PATH, INVENTORY_ROW_ID_RETURN_CODE);
    }

    private InventoryDBHelper inventoryDBHelper;

    @Override
    public boolean onCreate() {
        inventoryDBHelper = new InventoryDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        /* final..?*/
        int matchReturn = uriMatcher.match(uri);
        switch (matchReturn) {
            case INVENTORY_BASE_RETURN_CODE:
                return InventoryEntry.CONTENT_LIST_MIME_TYPE;
            case INVENTORY_ROW_ID_RETURN_CODE:
                return InventoryEntry.CONTENT_ITEM_MIME_TYPE;
            default:
                throw new IllegalStateException(getContext().getString(R.string.err_provider_mime_unknown_uri)
                        + " " + uri + getContext().getString(R.string.err_provider_mime_match) + " " + matchReturn);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase sqlDB = inventoryDBHelper.getReadableDatabase();
        Cursor dbCursor;

        int matchReturn = uriMatcher.match(uri);
        switch (matchReturn) {

            case INVENTORY_BASE_RETURN_CODE:
                dbCursor = sqlDB.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
//                no need to close cursor in provider: https://stackoverflow.com/questions/4547461/closing-the-database-in-a-contentprovider
                break;

            case INVENTORY_ROW_ID_RETURN_CODE:
//              override selection and selectionArgs
//                with a specific row id
                selection = InventoryEntry._ID + "=?";
//              and with a single row given by the ID in the URI
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                dbCursor = sqlDB.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException(getContext().getString(R.string.err_provider_query_unknown_uri) + " " + uri);
        }
        dbCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return dbCursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int matchReturn = uriMatcher.match(uri);
        switch (matchReturn) {
            case INVENTORY_BASE_RETURN_CODE:
                return insertInventoryRow(uri, contentValues);

            default:
                throw new IllegalArgumentException(getContext().getString(R.string.err_provider_insert_not_suppt));
        }
    }

    private Uri insertInventoryRow(Uri uri, ContentValues contentValues) {

//      all of the error-checking stuff is done before it gets to the provider
//      ie, on the ui side

        SQLiteDatabase sqlDB = inventoryDBHelper.getWritableDatabase();

        long insertReturn = sqlDB.insert(InventoryEntry.TABLE_NAME, null, contentValues);
        if (insertReturn == -1) {
            Log.e(LOG_TAG, getContext().getString(R.string.err_provider_insert_fail) + " " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, insertReturn);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {

        /*final...?*/
        int matchReturn = uriMatcher.match(uri);
        switch (matchReturn) {

            case INVENTORY_BASE_RETURN_CODE:
//              update all rows that match the selection and selection args
                return updateInventoryRow(uri, contentValues, selection, selectionArgs);

            case INVENTORY_ROW_ID_RETURN_CODE:
//              override selection and selectionArgs
//              with a specific row id
                selection = InventoryEntry._ID + "=?";
//              and with a single row given by the ID in the URI
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventoryRow(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException(getContext().getString(R.string.err_provider_update_not_suppt) + " " + uri);
        }
    }

    private int updateInventoryRow(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

//      all that error-checking stuff has been taken care of on the ui side

        SQLiteDatabase sqlDB = inventoryDBHelper.getWritableDatabase();
        int rowsUpdated = sqlDB.update(InventoryEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase sqlDB = inventoryDBHelper.getWritableDatabase();
        int rowsDeleted;

        /*final..?*/
        int matchReturn = uriMatcher.match(uri);
        switch (matchReturn) {
            case INVENTORY_BASE_RETURN_CODE:
//              delete all rows that match the selection and selection args
                rowsDeleted = sqlDB.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case INVENTORY_ROW_ID_RETURN_CODE:
//              delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = sqlDB.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new IllegalArgumentException(getContext().getString(R.string.err_provider_delete_not_suppt) + " " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }
}
