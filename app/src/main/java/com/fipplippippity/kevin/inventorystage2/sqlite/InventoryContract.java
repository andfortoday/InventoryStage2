package com.fipplippippity.kevin.inventorystage2.sqlite;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class InventoryContract {

    public static final String CONTENT_AUTHORITY = "com.fipplippippity.kevin.inventorystage2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY); // content://"com.fipplippippity.kevin.inventorystage2"
    public static final String PATH_INVENTORY = "inventory";                                // content://"com.fipplippippity.kevin.inventorystage2/inventory"
    private InventoryContract() {
    }

    public static final class InventoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY); // content://"com.fipplippippity.kevin.inventorystage2/inventory"

        public static final String TABLE_NAME = "inventory",
                _ID = BaseColumns._ID,
                COL_PRODUCT_NAME = "product_name",
                COL_PRICE = "price",
                COL_QTY = "quantity",
                COL_SUPPLIER_NAME = "supplier_name",
                COL_SUPPLIER_PHONE = "supplier_phone_number";

        public static final String CONTENT_LIST_MIME_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                        + CONTENT_AUTHORITY + "/"
                        + PATH_INVENTORY;

        public static final String CONTENT_ITEM_MIME_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                        + CONTENT_AUTHORITY + "/"
                        + PATH_INVENTORY;

    }

}
