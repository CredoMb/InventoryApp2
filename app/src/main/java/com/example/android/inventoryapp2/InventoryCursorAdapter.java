package com.example.android.inventoryapp2;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.android.inventoryapp2.data.InventoryContract.InventoryEntry;

import java.text.NumberFormat;


public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Will be used to display messages in the Log
     */
    private static final String TAG = InventoryCursorAdapter.class.getSimpleName();

    private String NUMBER_FORMAT = "%d";
    private String LEFT_TAG = " left";
    // To help us laod photo using Glide library
    private GlideHelperClass glideHelper;

    // The code to request the "READ_EXTERNAL_STORAGE" permission
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;
    // Represent the default value of the quantity
    // in  the database.
    private static final int DEFAULT_QUANTITY_VALUE = 0;

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */

    InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);

        // Request the permission to read external storage
        // and open documents.
        // For instance, the permission to read photos from the
        // Gallery.
        ActivityCompat.requestPermissions(
                (Activity) context,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

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
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
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
    public void bindView(View view, final Context context, final Cursor cursor) {

        // Get the position of the current item in the
        // listView.
        final int currentPosition = cursor.getPosition();

        // Find the Views of an item_layout.
        // Each view will be stored in a variable.
        TextView nameTextView = (TextView) view.findViewById(R.id.name_tv);
        TextView priceTextView = (TextView) view.findViewById(R.id.price_tv);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_tv);
        ImageView itemThumbnail = (ImageView) view.findViewById(R.id.catalog_product_iv);

        // Get the name, the price and the quantity from the cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_NAME));
        Float price = cursor.getFloat(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_PRICE));
        final String quantity = String.format(NUMBER_FORMAT, cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_QUANTITY)));
        String imageUriString = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_IMAGE));
        ImageButton saleImageButton = (ImageButton) view.findViewById(R.id.sale_ImageButton);

        // Initialize the Glide Helper
        glideHelper = new GlideHelperClass(context.getApplicationContext(), imageUriString, R.drawable.placeholder_image, itemThumbnail);
        // Set the image onto the item ImageView
        glideHelper.loadImage();

        // Put the name, the price and the quantity inside the corresponding textViews
        nameTextView.setText(name);
        quantityTextView.setText(quantity + LEFT_TAG);
        // Format the price to appear with the currency
        // before putting it into the TextView
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        priceTextView.setText(currencyFormat.format(price));


        // Decrease the quantity value every time the
        // sale TextView is clicked
        saleImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get the value of the new quantity
                // by decrementing the old one.
                int leftQuantityNumber = Integer.parseInt(quantity) - 1;

                // Turn the new quantity value to a
                // String
                String leftQuantityText = String.valueOf(
                        leftQuantityNumber);

                // Create a content value. This will help us
                // later to indicate the database column to update
                ContentValues values = new ContentValues();

                // Make sure that the left quantity is equal or
                // greater than 0. This will insure to not set
                // a negative value for the quantity.
                if (leftQuantityNumber >= DEFAULT_QUANTITY_VALUE) {

                    quantityTextView.setText(leftQuantityText + LEFT_TAG);

                    // Indicate that only the quantity should be update
                    // and pass in the value to update with.
                    values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, leftQuantityNumber);

                    // Get the Id of the current item
                    long itemId = getItemId(currentPosition);

                    // By using the CONTENT_URI and the item's id,
                    // build the URI of the item we need to update
                    Uri itemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, itemId);

                    // Update the Quantity of the current item with
                    // it new value.
                    context.getContentResolver().
                            update(itemUri, values, null, null);

                } else {
                    // Advise the uer to adjust the values of
                    // shipped and sold items.
                    Toast.makeText(context, R.string.invalidQuantityMessage, Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
    }
}
