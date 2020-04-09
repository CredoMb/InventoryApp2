package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class InventoryContract {

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

        // When it sold, the quantity decreases
        // When it shipped, the quantity increases

        // Do we have a list of shipped and sold items ?
        // May be the tracking part comes later on the Lesson 4, who knows ?

        //

        /* Represent the value of the default image */
        public static final int DEFAULT_IMAGE = -1;
        public static final Integer DEFAULT_QUANTITY = 0;
        public static final Double DEFAULT_PRICE = 0.0;
        public static final Integer DEFAULT_SOLD_OR_SHIPPED = 0;


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