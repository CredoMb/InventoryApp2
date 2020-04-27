package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryProvider extends ContentProvider {

    // Will be used to display messages in the Log
    private static final String TAG = InventoryProvider.class.getSimpleName();

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * The dataBase helper object
     */
    private InventoryDbHelper mDbHelper;

    /**
     * The inventory Id for the URi matcher
     */
    private static final int INVENTORY = 100;
    private static final int INVENTORY_ID = 101;

    /**
     * The Inventory Uri matcher
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * Constant used when no row has been updated
     */
    private static final int NO_ROW_UPDATED = 0;


    static {
        // Adding URI patterns to our uriMatcher
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    @Override
    public boolean onCreate() {
        // TODO: Create and initialize a InventoryDbHelper object to gain access to the Inventory database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.

        mDbHelper = new InventoryDbHelper(getContext());
        // SQLiteDatabase db = mDbHelper.getReadableDatabase();

        return true;
    }

    /**
     * Perform the query with the given URI.
     * Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // For the Inventory code, query the Inventory table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the Inventory table.
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection,
                        null, null, null, null, sortOrder);

                // The projection is the row demanded by the "query". Do you get it ? I do!
                // Like the row we target, we want to have.
                // The selection is the content of the "where" clause
                // The selection args are the value that goes with each "?" inside the selection

                break;
            case INVENTORY_ID:
                // For the Inventory_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.InventoryApp/Inventory/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = InventoryContract.InventoryEntry._ID + "=?"; // this mean where _id = ?
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))}; // the value to replace the "?"

                // This will perform a query on the Inventory table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        return cursor;
    }

    /**
     * Insert new data into the data base by using the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Item requires a name");
        }

        // Check that the imageUri is not null
        String imageStringUri = values.getAsString(InventoryEntry.COLUMN_ITEM_IMAGE);
        if (imageStringUri == null) {
            throw new IllegalArgumentException("Image String Uri should not be null");
        }

        // Check that the price is valid
        Double price = values.getAsDouble(InventoryEntry.COLUMN_ITEM_PRICE);
        if (price < InventoryEntry.DEFAULT_PRICE) {
            throw new IllegalArgumentException("The price shouldn't have a negative value");
        }

        // Check that the number of shipped items is valid
        Integer shipped = values.getAsInteger(InventoryEntry.COLUMN_ITEM_SHIPPED);
        if (shipped < InventoryEntry.DEFAULT_SOLD_OR_SHIPPED) {
            throw new IllegalArgumentException("The shipped value shouldn't be negative");
        }

        // Check that the number of sold items is valid
        Integer sold = values.getAsInteger(InventoryEntry.COLUMN_ITEM_SOLD);
        if (sold < InventoryEntry.DEFAULT_SOLD_OR_SHIPPED || sold > shipped) {
            throw new IllegalArgumentException("The sold value shouldn't be negative " +
                    "or greater than the shipped value");
        }

        // Check that the quantity is valid
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_ITEM_QUANTITY);
        if (quantity < InventoryEntry.DEFAULT_QUANTITY) {
            throw new IllegalArgumentException("The quantity shouldn't have a negative value");
        }

        // Insert a new item into the inventory database table with the given ContentValues
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notifies the changes to the activity,
        // so the activity can update the UI with new content
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data base with the data in the ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                // For the INVENTORY_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Helper method used to communicate with the database through the
     * database helper and update it.
     *
     * @param uri           represent the uri of the item or group of items that should be update
     * @param values        contains the name of the rows that should be updated
     * @param selection     is the condition that will be used to help us
     *                      narrow our search and find a specific category of items.
     * @param selectionArgs The value of the condition we've entered in the selection.
     */
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the {@link InventoryEntry#COLUMN_ITEM_NAME} key is present,
        // check that the name value is valid.
        if (values.containsKey(InventoryEntry.COLUMN_ITEM_NAME)) {
            // Check that the name is not empty
            String name = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
            if (TextUtils.isEmpty(name)) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }

        // If the {@link InventoryEntry#COLUMN_ITEM_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(InventoryEntry.COLUMN_ITEM_PRICE)) {
            // Check that the price is valid
            Double price = values.getAsDouble(InventoryEntry.COLUMN_ITEM_PRICE);
            if (price < InventoryEntry.DEFAULT_PRICE) {
                throw new IllegalArgumentException("The price shouldn't have a negative value");
            }
        }

        // If the {@link InventoryEntry#COLUMN_ITEM_SHIPPED} key is present,
        // check that the name value is valid.
        if (values.containsKey(InventoryEntry.COLUMN_ITEM_SHIPPED)) {
            // Check that the shipped value is not negative
            Integer shipped = values.getAsInteger(InventoryEntry.COLUMN_ITEM_SHIPPED);
            if (shipped < InventoryEntry.DEFAULT_SOLD_OR_SHIPPED) {
                throw new IllegalArgumentException("The shipped value shouldn't be negative");
            }
        }

        // If the {@link InventoryEntry#COLUMN_ITEM_SOLD} key is present,
        // check that the name value is valid.
        if (values.containsKey(InventoryEntry.COLUMN_ITEM_SOLD)) {
            Integer sold = values.getAsInteger(InventoryEntry.COLUMN_ITEM_SOLD);
            // Check that the sold value is not negative
            if (sold < InventoryEntry.DEFAULT_SOLD_OR_SHIPPED
                    || sold > values.getAsInteger(InventoryEntry.COLUMN_ITEM_SHIPPED)) {

                throw new IllegalArgumentException("The sold value shouldn't be negative " +
                        "or greater than the shipped value");
            }
        }

        // If the {@link InventoryEntry#COLUMN_ITEM_QUANTITY} key is present,
        // check that the name value is valid.
        if (values.containsKey(InventoryEntry.COLUMN_ITEM_QUANTITY)) {
            // Check that the quantity is valid
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_ITEM_QUANTITY);
            if (quantity < InventoryEntry.DEFAULT_QUANTITY) {
                throw new IllegalArgumentException("The quantity shouldn't have a negative value");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete from the database, the element specified by the Uri
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */

    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;

            case INVENTORY_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }

    }

    private Cursor randomCurs() {
        return new Cursor() {
            @Override
            public int getCount() {
                return 0;
            }

            @Override
            public int getPosition() {
                return 0;
            }

            @Override
            public boolean move(int i) {
                return false;
            }

            @Override
            public boolean moveToPosition(int i) {
                return false;
            }

            @Override
            public boolean moveToFirst() {
                return false;
            }

            @Override
            public boolean moveToLast() {
                return false;
            }

            @Override
            public boolean moveToNext() {
                return false;
            }

            @Override
            public boolean moveToPrevious() {
                return false;
            }

            @Override
            public boolean isFirst() {
                return false;
            }

            @Override
            public boolean isLast() {
                return false;
            }

            @Override
            public boolean isBeforeFirst() {
                return false;
            }

            @Override
            public boolean isAfterLast() {
                return false;
            }

            @Override
            public int getColumnIndex(String s) {
                return 0;
            }

            @Override
            public int getColumnIndexOrThrow(String s) throws IllegalArgumentException {
                return 0;
            }

            @Override
            public String getColumnName(int i) {
                return null;
            }

            @Override
            public String[] getColumnNames() {
                return new String[0];
            }

            @Override
            public int getColumnCount() {
                return 0;
            }

            @Override
            public byte[] getBlob(int i) {
                return new byte[0];
            }

            @Override
            public String getString(int i) {
                return null;
            }

            @Override
            public void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer) {

            }

            @Override
            public short getShort(int i) {
                return 0;
            }

            @Override
            public int getInt(int i) {
                return 0;
            }

            @Override
            public long getLong(int i) {
                return 0;
            }

            @Override
            public float getFloat(int i) {
                return 0;
            }

            @Override
            public double getDouble(int i) {
                return 0;
            }

            @Override
            public int getType(int i) {
                return 0;
            }

            @Override
            public boolean isNull(int i) {
                return false;
            }

            @Override
            public void deactivate() {

            }

            @Override
            public boolean requery() {
                return false;
            }

            @Override
            public void close() {

            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public void registerContentObserver(ContentObserver contentObserver) {

            }

            @Override
            public void unregisterContentObserver(ContentObserver contentObserver) {

            }

            @Override
            public void registerDataSetObserver(DataSetObserver dataSetObserver) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

            }

            @Override
            public void setNotificationUri(ContentResolver contentResolver, Uri uri) {

            }

            @Override
            public Uri getNotificationUri() {
                return null;
            }

            @Override
            public boolean getWantsAllOnMoveCalls() {
                return false;
            }

            @Override
            public void setExtras(Bundle bundle) {

            }

            @Override
            public Bundle getExtras() {
                return null;
            }

            @Override
            public Bundle respond(Bundle bundle) {
                return null;
            }
        };
    }

}
