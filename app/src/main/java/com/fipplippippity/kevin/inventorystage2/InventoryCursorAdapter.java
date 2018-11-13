package com.fipplippippity.kevin.inventorystage2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.fipplippippity.kevin.inventorystage2.sqlite.InventoryContract.InventoryEntry;

public class InventoryCursorAdapter extends CursorAdapter {

    final static int ZERO_FLAGS = 0;
    private static int productQty, productIDIndex;
    private static long productID;

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, ZERO_FLAGS);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView tvProductName = view.findViewById(R.id.tv_product_name),
                tvProductPrice = view.findViewById(R.id.tv_product_price),
                tvProductQty = view.findViewById(R.id.tv_product_quantity);
        Button btnSaleItem = view.findViewById(R.id.btn_product_sale);

        int productNameIndex = cursor.getColumnIndex(InventoryEntry.COL_PRODUCT_NAME);
        int productPriceIndex = cursor.getColumnIndex(InventoryEntry.COL_PRICE);
        int productQtyIndex = cursor.getColumnIndex(InventoryEntry.COL_QTY);
        productIDIndex = cursor.getColumnIndex(InventoryEntry._ID);
        String productName = cursor.getString(productNameIndex);
        int productPrice = Integer.valueOf(cursor.getString(productPriceIndex));
        productQty = Integer.valueOf(cursor.getString(productQtyIndex));

        btnSaleItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                productID = Long.valueOf(cursor.getString(productIDIndex));
                if (productQty >= 1) {
                    productQty--;
                    updateQTY(context);
                }
            }
        });

        tvProductName.setText(productName);
//        todo: hardcoding
        tvProductPrice.setText("$" + String.valueOf(productPrice));
        tvProductQty.setText("qty: " + String.valueOf(productQty));
    }

    private int updateQTY(Context qtyContext) {

        ContentValues newQty = new ContentValues();
        Uri qtyURI = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, productID);
        newQty.put(InventoryEntry.COL_QTY, productQty);
        int qtyRow = qtyContext.getContentResolver().update(qtyURI, newQty, null, null);
        if (qtyRow != 0) {
            qtyContext.getContentResolver().notifyChange(qtyURI, null);
        } else {
//            todo hardcoding
            Toast.makeText(qtyContext, "error with updating the product quantity/sale", Toast.LENGTH_SHORT).show();
        }
        return qtyRow;
    }
}
