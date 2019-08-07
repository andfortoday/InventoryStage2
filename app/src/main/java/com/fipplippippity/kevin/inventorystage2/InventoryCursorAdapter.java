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
        final int productIDIndex = cursor.getColumnIndex(InventoryEntry._ID);
        String productName = cursor.getString(productNameIndex);
        int productPrice = Integer.valueOf(cursor.getString(productPriceIndex));
        final int productQty = Integer.valueOf(cursor.getString(productQtyIndex));
        final long productID = Long.valueOf(cursor.getString(productIDIndex));

        btnSaleItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (productQty >= 1) {
                    int newProductQty = productQty;
                    newProductQty--;
                    if (updateQTY(context, newProductQty, productID) == 0) {
                        Toast.makeText(context, context.getString(R.string.err_main_sale_btn_saving), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, context.getString(R.string.info_sale_successful), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        tvProductName.setText(productName);
        tvProductPrice.setText(context.getString(R.string.list_pre_dollar) + String.valueOf(productPrice));
        tvProductQty.setText(context.getString(R.string.list_pre_qty) + String.valueOf(productQty));
    }

    private int updateQTY(Context passTheContext, int passTheQty, long passTheID) {

        ContentValues newQty = new ContentValues();
        Uri qtyURI = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, passTheID);
        newQty.put(InventoryEntry.COL_QTY, passTheQty);
        int qtyRow = passTheContext.getContentResolver().update(qtyURI, newQty, null, null);

        return qtyRow;
    }
}
