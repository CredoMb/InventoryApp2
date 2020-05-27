package com.example.android.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
        super(context,c,0);

        // Request the permission to read external storage
        // and open documents.
        // For instance, the permission to read photos from the
        // Gallery.
        ActivityCompat.requestPermissions(
                (Activity) context,
                new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
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
        String name =  cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_NAME));
        Float price = cursor.getFloat(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_PRICE));
        final String quantity = String.format(NUMBER_FORMAT,cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_QUANTITY)));
        String imageUriString = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_IMAGE));
        TextView saleTextView = (TextView) view.findViewById(R.id.sale_textview);

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

        saleTextView.setOnClickListener(new View.OnClickListener() {
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
                    Uri itemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI,itemId);

                    // Update the Quantity of the current item with
                    // it new value. Return an int to indicate the number
                    // of row updated.
                    int rowsAffected = context.getContentResolver().
                            update(itemUri, values, null, null);

                    // Les changements affectent d'autres affaireds

                } else {
                    // Advise the uer to adjust the values of
                    // shipped and sold items.
                    Toast.makeText(context, R.string.invalidQuantityMessage, Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

        // This will be used inside of the onClick method
        // of the saleTextView. It will indicate weither or
        // not the quantity has been updated.
        final int rowsAffected = 0;

    }

    /*// I don't know when the "getview" method is actually called by the framework. So
    // How Am I making this work ?
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        *//*if(convertView == null) {

        }*//*

        // get the cursor with the data of the current element
        // This is the general cursor not the cursor for the specific element
        // we are pointing to...
        final Cursor cursor = getCursor();

        // Get the Quantity from the data base
        final String quantity = String.format(NUMBER_FORMAT,cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_QUANTITY)));

        // Get the context of the current adaptor
        final Context context = convertView.getContext();

        TextView saleTextView = (TextView) convertView.findViewById(R.id.sale_button);
        final TextView quantityTextView = (TextView) convertView.findViewById(R.id.quantity_tv);

        // Add a click listener to the sale TextView
        // whenever it clicked, it will
        // decrease the quantity value.
        *//*
        saleTextView.setOnClickListener(new View.OnClickListener() {
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

                    // Create a selection
                    // The call to the update method of the content resolver
                    // will return the number of rows that were affected by the operation.
                    // In our case, It should be one.

                    // Get the Id of the current item
                    int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry._ID));

                    // By using the CONTENT_URI and the item's id,
                    // build the URI of the item we need to update
                    Uri itemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI,itemId);

                    // Update the Quantity of the current item with
                    // it new value. Return an int to indicate the number
                    // of row updated.
                    int rowsAffected = context.getContentResolver().
                            update(itemUri, values, null, null);

                } else {
                    // Advise the uer to adjust the values of
                    // shipped and sold items.
                    Toast.makeText(context, R.string.invalidQuantityMessage, Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });*//*
        return super.getView(position, convertView, parent);
    }
*/
    /*

    private void saveItem() {

        int leftQuantity = Integer.parseInt(quantityLeft(shippedNumber, soldNumber));

        // Set the Quantity based on the number of shipped and sold items.
        // If the difference between the shipped and sold item is greater than 0,
        // set the quantity TextView with the value of that difference.
        // Else, show a Toast to signify that the quantity can't be
        // negative
        int quantityNumber = DEFAULT_QUANTITY_VALUE;

        if (leftQuantity >= DEFAULT_QUANTITY_VALUE) {
            quantityNumber = leftQuantity;
            mQuantityValueTextView.setText(String.valueOf(leftQuantity));
        } else {
            // Advise the uer to adjust the values of
            // shipped and sold items.
            Toast.makeText(this, R.string.invalidQuantityMessage, Toast.LENGTH_LONG).show();
            return;
        }

        // Create a ContentValues to store the informations of the new item
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantityNumber);

        // Check if the "mItemUri" is null.
        //  If it's "null", then we need to add
        //  a new item, otherwise, we should modify
        //  an existing item.

        if (mItemUri == null) {
            // This is a NEW item, so we should insert a new item into the database.
            // This will return the content URI for the item newly inserted.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display the corresponding.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }   // If the mItemUri is not null, we need to update the
        // item instead of creating a new one.

        else {
            // The content URI of the item is: mItemUri.
            // Update the item and pass in the new ContentValues.
            // Pass in null for the selection and selection args
            // because mItemUri will already identify the correct row in the database that
            // we want to modify.

            // The call to the update method of the content resolver
            // will return the number of rows that were affected by the operation.
            // In our case, It should be one.
            int rowsAffected = getContentResolver().update(mItemUri, values, null, null);

            // Check if the update was successfully made
            // and display an adequate message to the user.
            if (rowsAffected == 0) {
                // zero row affected means that no changes
                // were made.
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // If the rowsAffected is not equal to zero,
                // this means that the change was made.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();

    }

    public void deleteItems(long[] checkedItemsIds) {

        // Get the number of checked Items.
        int checkedItemsCount = checkedItemsIds.length;

        // Build the selection that will be used
        // after the "WHERE" clause to query the database.
        String selection = InventoryEntry._ID + " IN (";

        for (int i = 0; i < checkedItemsCount; i++) {
            // Add the Ids to the selection
            selection += String.valueOf(checkedItemsIds[i]);
            // As long as there are still ids inside of the
            // array, separate them by a ","
            if (i < checkedItemsCount - 1) {
                selection += ",";

                // Close the "WHERE ID IN (..." with a ")"
            } else {
                selection += ")";
            }
        }

        // With the content resolver, delete
        // the selected items from the database.
        getContentResolver().delete(InventoryEntry.CONTENT_URI, selection, null);
    }

    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mItemUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mItemUri, null, null);

            // Create a selector :

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
*/

    // I don't know what time is it.

}
