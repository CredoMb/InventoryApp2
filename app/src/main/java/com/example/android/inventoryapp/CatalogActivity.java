package com.example.android.inventoryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;
import com.example.android.inventoryapp.data.InventoryDbHelper;

import java.util.List;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Tag to display with the LOG message*/
    private String LOG_TAG = CatalogActivity.class.getSimpleName();

    // Id for the Loader
    private static int INVENTORY_LOADER = 1;

    private Button openButton;

    // The cursor Adapter
    private InventoryCursorAdapter mInventoryCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        openButton = (Button) findViewById(R.id.open_editor);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this,EditorActivity.class);
                startActivity(intent);
            }
        });

        // Initialize the mInventoryCursorAdapter
        mInventoryCursorAdapter = new InventoryCursorAdapter(this,null);

        // find the listView and set the CusorAdaptor on it
        ListView itemListView = (ListView) findViewById(R.id.list_view);

        itemListView.setAdapter(mInventoryCursorAdapter);
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                // Create an intent to start the Editor Activity
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Build the Uri of the item that has been clicked on.
                // the Uri will be made of "content://com.example.android.inventoryapp"
                // and the Id of the selected item. For example, if the second item was clicked
                // the Uri would be "content://com.example.android.inventoryapp/inventory/2"
                Uri itemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI,id);

                // Add the Item Uri to the intent as an extra,
                // So that the Editor Activiy would use it to modify the item's informations
                intent.setData(itemUri);

                // Start the Editor Activity
                startActivity(intent);
            }
        });
        // Initiate the loader
        getLoaderManager().initLoader(INVENTORY_LOADER,null,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String [] projection = {InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_SOLD,
                InventoryEntry.COLUMN_ITEM_SHIPPED,
                InventoryEntry.COLUMN_ITEM_SUPPLIER
        };

        // The cursor loader makes the database connection
        // for us !
        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // What do you mean by notification URI ?

        cursor.setNotificationUri(getContentResolver(),InventoryEntry.CONTENT_URI);
        mInventoryCursorAdapter.swapCursor(cursor);

        //  Why is our crusor null ? Why ? Ithink i know why !
        // It's because of the database name may be ?
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mInventoryCursorAdapter.swapCursor(null);
    }

    /** This will inflate the menu on the Catalog Activity */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }
}