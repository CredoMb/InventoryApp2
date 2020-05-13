package com.example.android.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;
import java.text.NumberFormat;


public class InventoryCursorAdapter extends CursorAdapter {

    /** Will be used to display messages in the Log*/
    private static final String TAG = InventoryCursorAdapter.class.getSimpleName();

    private String NUMBER_FORMAT = "%d";
    private String LEFT_TAG = " left";
    // To help us laod photo using Glide library
    private GlideHelperClass glideHelper;

    // The code to request the "READ_EXTERNAL_STORAGE" permission
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;


    /** Will be used inside the "String.Format" to precise the
     *  conversion from integer to String */

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */

    InventoryCursorAdapter(Context context, Cursor c) {
        super(context,c,0);

        // Request the permission to read external storage
        // and open documents.
        // For instance, the permission to read photos from the
        // Gallery.
        ActivityCompat.requestPermissions(
                (Activity) context,
                new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

        //Manifest.permission.ACTI
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }


    /**
     * This method binds the item data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find the textViews of an item_layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name_tv);
        TextView priceTextView = (TextView) view.findViewById(R.id.price_tv);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_tv);
        ImageView itemThumbnail = (ImageView) view.findViewById(R.id.catalog_product_iv);

        // Get the name, the price and the quantity from the cursor
        String name =  cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_NAME));
        Float price = cursor.getFloat(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_PRICE));
        String quantity = String.format(NUMBER_FORMAT,cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_QUANTITY)));
        String imageUriString = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_IMAGE));

        // Initialize the Glide Helper
        glideHelper = new GlideHelperClass(context.getApplicationContext(),imageUriString,R.drawable.placeholder_image,itemThumbnail);
        // Set the image onto the item ImageView
        glideHelper.loadImage();

        // Put the name, the price and the quantity inside the corresponding textViews
        nameTextView.setText(name);

        // Format the price to appear with the currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        priceTextView.setText(currencyFormat.format(price));
        quantityTextView.setText(quantity + LEFT_TAG);
    }

}
