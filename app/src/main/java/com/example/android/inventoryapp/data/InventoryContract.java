package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class InventoryContract {

    // Will be used to display messages in the Log
    private static final String TAG = InventoryContract.class.getSimpleName();

    private InventoryContract() {}

    /* The following informations will serve to build the URI for the Inventory provider */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    /* The base URI that will be used with the InventoryProvider */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /* This will be added to the BASE_CONTENT_URI in order to reach the "inventory" table inside the data base */
    public static final String PATH_INVENTORY ="inventory";

    public static final class InventoryEntry implements BaseColumns {
        /** The content URI to access the inventory table in the data base */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        //The name of the first table of the data base
        public static final String TABLE_NAME = "inventory";
        public static final String _ID = BaseColumns._ID;

        public static final String COLUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_IMAGE = "image";
        public static final String COLUMN_ITEM_PRICE = "price";
        public static final String COLUMN_ITEM_QUANTITY = "quantity";
        public static final String COLUMN_ITEM_SOLD = "sold";
        public static final String COLUMN_ITEM_SHIPPED ="shipped";
        public static final String COLUMN_ITEM_SUPPLIER = "supplier";

        /* Represent the value of the default image */
        public static final int DEFAULT_IMAGE = -1;
        public static final Integer DEFAULT_QUANTITY = 0;
        public static final Double DEFAULT_PRICE = 0.0;
        public static final Integer DEFAULT_SOLD_OR_SHIPPED = 0;
        public static final String DEFAULT_URI ="no_uri";

        /* The MIME type for the all inventory table */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY
                        + "/" + PATH_INVENTORY;

        /* The MIME type for 1 item in the inventory table  */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY
                        +"/" + PATH_INVENTORY;
    }
}
