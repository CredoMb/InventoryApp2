package com.example.android.inventoryapp2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.ActionMode;
import android.widget.RelativeLayout;

import com.example.android.inventoryapp2.data.InventoryContract.InventoryEntry;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Tag to display with the LOG message
     */
    private String LOG_TAG = CatalogActivity.class.getSimpleName();

    /**
     * ID to identify the Loader of that proceed to
     * the data base query
     */
    private static int INVENTORY_LOADER = 1;

    /**
     * Will store the floating Button used to
     * add a new item
     */
    private FloatingActionButton fabNewItem;

    /**
     * The cursor Adapter for the ListView
     */
    private InventoryCursorAdapter mInventoryCursorAdapter;

    /**
     * Will contain the groupView for the empty state
     */
    private RelativeLayout emptyGroupView;

    /**
     * Variable that will store the listview
     */
    ListView mItemsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        fabNewItem = (FloatingActionButton) findViewById(R.id.fab);
        fabNewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Initialize the mInventoryCursorAdapter
        mInventoryCursorAdapter = new InventoryCursorAdapter(this, null);

        // Store the empty state group view in a variable
        emptyGroupView = (RelativeLayout) findViewById(R.id.empty_group_view);

        // find the listView and set the CusorAdaptor on it
        mItemsListView = (ListView) findViewById(R.id.list_view);

        // Set the empty state groupView onto it listView
        mItemsListView.setEmptyView(emptyGroupView);

        mItemsListView.setAdapter(mInventoryCursorAdapter);

        // Everytime an item is clicked, the code
        // inside "OnItemClick" will be executed.
        mItemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                // Create an intent to start the Editor Activity
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Build the Uri of the item that has been clicked on.
                // the Uri will be made of "content://com.example.android.inventoryapp2"
                // and the Id of the selected item. For example, if the second item was clicked
                // the Uri would be "content://com.example.android.inventoryapp2/inventory/2"
                Uri itemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                // Add the Item Uri to the intent as an extra,
                // So that the Editor Activiy would use it to modify the item's informations
                intent.setData(itemUri);

                // Add the position of the item to the intent
                intent.putExtra(Intent.EXTRA_INDEX, position);

                // Start the Editor Activity
                startActivity(intent);
            }
        });

        /**
         * The following code defines the behaviour of
         * the list view whenever the user select one or multiple
         * items.
         * */

        mItemsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mItemsListView.setMultiChoiceModeListener((new AbsListView.MultiChoiceModeListener() {


            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean
                    checked) {

                // Get the total number of selected items
                final int checkedItems = mItemsListView.getCheckedItemCount();

                // Show the number of selected items
                // in the app bar
                mode.setTitle(String.format("%d", checkedItems));
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_action_mode, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {
                    // Define the behavior for when the
                    // user clicks on the "DELETE" button
                    case R.id.action_delete_modal:

                        // Get the ids of all the checked Items.
                        long[] checkedItemsIds = mItemsListView.getCheckItemIds();

                        // Show the dialog to confirm the deletion
                        // of the checked Items
                        showDeleteConfirmationDialog(checkedItemsIds, mode);
                        return true;

                    // Define the behavior for when the
                    // user clicks on the "SELECT ALL" option
                    case R.id.action_select_all:

                        // Iterate through the list and select each
                        // item.
                        for (int i = 0; i < mItemsListView.getCount(); i++) {
                            mItemsListView.setItemChecked(i, true);
                        }
                        return true;

                }
                mode.finish();
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        }));

        // Initiate the loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_IMAGE,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_SUPPLIER
        };

        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        cursor.setNotificationUri(getContentResolver(), InventoryEntry.CONTENT_URI);
        mInventoryCursorAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mInventoryCursorAdapter.swapCursor(null);
    }

    /**
     * This will inflate the menu on the Catalog Activity
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    /**
     * Prompt the user to confirm that they want to delete the checked items.
     */
    private void showDeleteConfirmationDialog(long[] checkedItemsIds, final ActionMode mode) {

        final long[] ItemsIds = checkedItemsIds;
        String deleteMsg = getString(R.string.delete_dialog_msg);

        if (ItemsIds.length > 1) {
            deleteMsg = getString(R.string.delete_dialog_msg_group);
        }

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this);
        alertDialog.setMessage(deleteMsg);
        alertDialog.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItems(ItemsIds);
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                    mode.finish();
                }
            }
        });

        // Create and show the AlertDialog

        alertDialog.show();

    }

    /**m
     * Will delete selected items of the listView.
     * <p>
     * Will be called in the showDeleteConfirmationDialog(),
     * which will be used inside the AbsListView.MultiChoiceModeListener()
     * of the listView
     */

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
}