package com.fipplippippity.kevin.inventorystage2;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fipplippippity.kevin.inventorystage2.sqlite.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int INVENTORY_LOADER = 0;
    public static final String[] INVENTORY_COLUMNS = {
            InventoryEntry._ID,
            InventoryEntry.COL_PRODUCT_NAME,
            InventoryEntry.COL_PRICE,
            InventoryEntry.COL_QTY,
            InventoryEntry.COL_SUPPLIER_NAME,
            InventoryEntry.COL_SUPPLIER_PHONE
    };
    InventoryCursorAdapter inventoryCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fabAddProduct = findViewById(R.id.fab_add_product);
        fabAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentAddProduct = new Intent(MainActivity.this, ProductActivity.class);
                ProductActivity.setProductState(ProductActivity.MAIN_TO_ADD_VIEW);
                startActivity(intentAddProduct);
            }
        });

        ListView inventoryListView = findViewById(R.id.list_main);
        View tvEmpty = findViewById(R.id.tv_empty_view);
        inventoryListView.setEmptyView(tvEmpty);

        inventoryCursorAdapter = new InventoryCursorAdapter(this, null); // null: handled by loader
        inventoryListView.setAdapter(inventoryCursorAdapter);

//        focusable on each list item button in is set to false (in xml) to allow listview onitemclicklistener to work
        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent viewItemDetail = new Intent(MainActivity.this, ProductActivity.class);
                viewItemDetail.setData(ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id));
                ProductActivity.setProductState(ProductActivity.LIST_TO_PROD_VIEW);
                startActivity(viewItemDetail);
            }
        });

        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        final String[] COLUMNS_FOR_DISPLAY = {
                InventoryEntry._ID,
                InventoryEntry.COL_PRODUCT_NAME,
                InventoryEntry.COL_PRICE,
                InventoryEntry.COL_QTY,
        };

        return new CursorLoader(
                this,
                InventoryEntry.CONTENT_URI,
                COLUMNS_FOR_DISPLAY,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        inventoryCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        inventoryCursorAdapter.swapCursor(null);
    }
}