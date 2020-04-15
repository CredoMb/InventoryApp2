package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;
import com.example.android.inventoryapp.data.InventoryDbHelper;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * EditText field to enter the product's name
     */
    private EditText mNameEditText;

    /**
     * ImageView to store the product's Image
     */
    private ImageView mProductImageView;

    /**
     * EditText field to enter the product's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the product'S quantity
     */
    private TextView mQuantityTextView;

    /**
     * TextView that displays the number of product sold
     */
    private TextView mSoldTextView;

    /**
     * TextView that displays the number of product shipped by the supplier
     */
    private TextView mShippedTextView;

    /**
     * EditText Field to enter the number of sold Items.
     * Will only be visible when adding a new item
     */

    private EditText mSoldEditText;
    /**
     * EditText Field to enter the number of shipped Items.
     * Will only be visible when adding a new item
     */
    private EditText mShippedEditText;

    /**
     * EditText field to enter the supplier's name
     */
    private EditText mSupplierEdtiText;


    /**
     * The default value of the weight for each item
     */
    private int ZERO = 0;

    /**
     * Variable that contains the integer "1"
     */
    private int ONE = 1;

    /**
     * Represent the maximum items that could be
     * sold
     */
    private int MAX_ITEM_TO_SALE = 1000;

    /**
     * Represent the maximum items that could be
     * shipped
     */
    private int MAX_ITEM_TO_SHIP = 1000;

    /**
     * This will be used to store the product Uri received from the Catalog Activity
     */
    private Uri mItemUri;

    /**
     * The Id for the Loader of the Editor Activity
     */
    private int EDITOR_LOADER_ID = 2;

    /**
     * Number format
     */
    private String NUMBER_FORMAT = "%d";

    /** Decimal Format*/
    private String DECIMAL_FORMAT = "%.2f";

    /**
     * Default value for an item with no price
     */
    private int DEFAULT_PRICE_VALUE = 0;

    /**
     * Default value for an item with unknown quantity
     */
    private int DEFAULT_QUANTITY_VALUE = 0;

    /** Boolean flag that keeps track of whether the item has been edited (true) or not (false) */
    private boolean mItemHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mItemHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find the view related to the database column and store them into
        // appropriate variables
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mProductImageView = (ImageView) findViewById(R.id.product_iv);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQuantityTextView = (TextView) findViewById(R.id.product_quantity_tv);

        mSoldEditText = (EditText) findViewById(R.id.edit_product_sold);
        mShippedEditText = (EditText) findViewById(R.id.edit_product_shipped);

        mSupplierEdtiText = (EditText) findViewById(R.id.edit_product_supplier);

        // Setting the same touch listener in all of the Edit Text will
        // help us know if the user started editing an item.
        // It will prevent the user to accidently quit the activity in
        // the middle of an edition.

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mShippedEditText.setOnTouchListener(mTouchListener);
        mSoldEditText.setOnTouchListener(mTouchListener);
        mSupplierEdtiText.setOnTouchListener(mTouchListener);

        // Get the intent from the CatalogActivity
        // Change the title of the Editor Activity based on the action that will occur
        // Get the item Uri from the intent made by the Catalog activity
        mItemUri = getIntent().getData(); // There are no data

        if (mItemUri == null) {
            // If the extra doesn't contain an Uri, the title Activity's should be "Add an Item"
            setTitle(R.string.addItemTitle);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a item that hasn't been created yet.)
            invalidateOptionsMenu();

        } else {
            // If the extra contains an Uri, the Activity 's title should be "Edit Item"
            setTitle(R.string.editItemTitle);

            // initiate the loader here

            // Initialize a loader to read the item data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EDITOR_LOADER_ID, null, this);

        }

        // Initiate the loader. Why do that here ? Why bro ?
        /*getLoaderManager().initLoader(EDITOR_LOADER_ID, null, this);*/
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // The thing is that we must create a database because
        String[] projection = {InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_SOLD,
                InventoryEntry.COLUMN_ITEM_SHIPPED,
                InventoryEntry.COLUMN_ITEM_SUPPLIER,
        };

        return new CursorLoader(this,
                mItemUri,
                projection,
                null,
                null,
                null);
    }

    // The database name has nothing to do with it, bitch !!!
    // But why is this bugging ?

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // If our cursor only contains 1 item
        // this means that the user want to update informations related
        // to that item. Then, show all the information related to that item
        // inside the edit text views.

        if (cursor.moveToFirst()) {

            // Move the cursor to the concerned row before getting data from it
           // cursor.moveToFirst();

            // Get the name from the cursor and put it on the appropriate edit text
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
            mNameEditText.setText(cursor.getString(nameColumnIndex), TextView.BufferType.EDITABLE);

            // Get the price from the cursor and put it on the appropriate edit text
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
            double priceNumber = cursor.getDouble(priceColumnIndex);
            mPriceEditText.setText(String.format(DECIMAL_FORMAT, priceNumber), TextView.BufferType.EDITABLE);

            // Get the shipped value from the cursor and put it on the appropriate edit text
            int shippedColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SHIPPED);
            int shippedNumber = cursor.getInt(shippedColumnIndex);
            mShippedEditText.setText(String.format(NUMBER_FORMAT, shippedNumber), TextView.BufferType.EDITABLE);

            // Get the sold value from the cursor and put it on the appropriate edit text
            int soldColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SOLD);
            int soldNumber = cursor.getInt(soldColumnIndex);
            mSoldEditText.setText(String.format(NUMBER_FORMAT, soldNumber), TextView.BufferType.EDITABLE);

            // Get the quantity from the cursor and put it on the appropriate edit text
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
            int quantityNumber = cursor.getInt(quantityColumnIndex);
            mQuantityTextView.setText(String.format(NUMBER_FORMAT, quantityNumber), TextView.BufferType.EDITABLE);

            // Get the supplier from the cursor and put it on the appropriate edit text
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER);
            mSupplierEdtiText.setText(cursor.getString(supplierColumnIndex), TextView.BufferType.EDITABLE);


        }
        // Here the user wanna do what ?
        else {
            // Couper de la
            //
        }
        // Close the cursor to avoid memory licks
        cursor.close();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /**
     * Is called when the decrement button of
     * the sold item is pressed.
     */


    /**This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated. I will hide the "delete" option when the
     * user is adding a new item.*/
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (mItemUri == null) {
        MenuItem menuItem = menu.findItem(R.id.action_delete);
        menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * This will inflate the menu on the Editor Activity
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // saveItem()
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
               saveItem(); // what to do now ?
               // finish();

                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();

                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                }
                else {
                    // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                    // Create a click listener to handle the user confirming that
                    // changes should be discarded.
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // User clicked "Discard" button, navigate to parent activity.
                                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                                }
                            };

                    // Show a dialog that notifies the user they have unsaved changes
                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;}
        }
        return super.onOptionsItemSelected(item);
    }

    // This method provides the Quantity by soustracting the
    // Sold items from the shipped items.
    private String quantityLeft(int shipped, int sold) {

        // Find the left Quantity by soustracting the sold
        // items from the shipped.
        int leftQuantity = shipped - sold;

        return String.format(NUMBER_FORMAT, leftQuantity);
    }

    /**
     * The method that will be called once the user
     * clicks on "save" icon. This method contains instruction
     * to either save or update a item in the database*/

    private void saveItem() {

        // Get the text from all the "editTexts" fields
        String nameString = mNameEditText.getText().toString().trim(); // Should not be null
        String supplierString = mSupplierEdtiText.getText().toString().trim();

        // Verify if the form has a name and it must have
        // a name ! If it doesn'T have a name, show a toaster ...
        if (TextUtils.isEmpty(nameString)) {
            // Initializing the Toast used when the user forget to type the name
            // of the product
            Toast.makeText(this, R.string.emptyNameMessage, Toast.LENGTH_LONG).show();

            return;
            // mNameEditText.requestFocus();
            // Also, is the quantity or the sold/shipped number higher than 1000 ?
            // Sold can not be bellow the shipped, it doesn'T make sens
            // If the shipped is bellow, it should give an error
        }

        // Check if this is supposed to be a new item
        // and check if all the fields in the editor are blank.
        // If the mItemUri is null, this means that the user clicked on the
        // button to create a new item.
        if (mItemUri == null &&
                TextUtils.isEmpty(nameString)
                && TextUtils.isEmpty(mSoldEditText.getText().toString().trim()) &&
                TextUtils.isEmpty(mShippedEditText.getText().toString().trim())
                && TextUtils.isEmpty(supplierString)) {
            // Since no fields were modified, we can return early without creating a new item.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // If the shipped number is not provided by the user,
        // don't try to parse the string into an integer value. Use 0 by default.
        int shippedNumber = TextUtils.isEmpty(mShippedEditText.getText().toString()) ?
                InventoryEntry.DEFAULT_SOLD_OR_SHIPPED
                : Integer.parseInt(mShippedEditText.getText().toString().trim());

        // If the sold number is not provided by the user,
        // don't try to parse the string into an integer value. Use 0 by default.
        int soldNumber = TextUtils.isEmpty(mSoldEditText.getText().toString()) ?
                InventoryEntry.DEFAULT_SOLD_OR_SHIPPED
                : Integer.parseInt(mSoldEditText.getText().toString().trim());

        // If the price number is not provided by the user,
        // don't try to parse the string into an integer value. Use 0 by default.
        double priceNumber = TextUtils.isEmpty(mPriceEditText.getText().toString()) ?
                InventoryEntry.DEFAULT_PRICE
                : Double.parseDouble(mPriceEditText.getText().toString().trim());

        int leftQuantity = Integer.parseInt(quantityLeft(shippedNumber, soldNumber));

        // Set the Quantity based on the number of shipped and sold items.
        // If the difference between the shipped and sold item is greater than 0,
        // set the quantity TextView with the value of that difference.
        // Else, show a Toast to signify that the quantity can't be
        // negative
        int quantityNumber = DEFAULT_QUANTITY_VALUE;

        if (leftQuantity >= DEFAULT_QUANTITY_VALUE) {
            quantityNumber = leftQuantity;
            mQuantityTextView.setText(String.valueOf(leftQuantity));
        }
        else {
            // Advise the uer to adjust the values of
            // shipped and sold items.
            Toast.makeText(this, R.string.invalidQuantityMessage, Toast.LENGTH_LONG).show();
            return;
        }

        // Create a ContentValues to store the informations of the new item
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, priceNumber);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantityNumber);
        values.put(InventoryEntry.COLUMN_ITEM_SOLD, soldNumber);
        values.put(InventoryEntry.COLUMN_ITEM_SHIPPED, shippedNumber);
        values.put(InventoryEntry.COLUMN_ITEM_SUPPLIER, supplierString);

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
            if (rowsAffected == 0){
                // zero row affected means that no changes
                // were made.
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            }
            else {
                // If the rowsAffected is not equal to zero,
                // this means that the change was made.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    /**
     * Prompt the user to confirm that they want to delete this item.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mItemUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mItemUri, null, null);

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

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


}
