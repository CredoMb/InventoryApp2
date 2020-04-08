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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
     * Button to decrement the number of sales
     */

    private Button mSalesDecrement;

    /**
     * Button to increment the number of sales
     */
    private Button mSalesIncrement;

    /**
     * Button to decrement the number shipped item
     */
    private Button mShippedDecrement;

    /**
     * Button to increment the number of shipped item
     */
    private Button mShippedIncrement;

    /**
     * Data base helper that will provide an access to the shelter dataBase
     */
    InventoryDbHelper mDbHelper;

    /**
     * The toast to display after a saving attempt to the dataBase
     */
    private Toast mSavingStateToast;

    /**
     * The toast to display after a delete attempt to the dataBase
     */
    private Toast mdeleteStateToast;

    /**
     * The toast that signifies to the user that the name field
     * must have a value
     */
    private Toast mEmptyNameToast;

    /**
     * The default value of the weight for each pet
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

    /**
     * Default value for an item with no price
     */
    private int DEFAULT_PRICE_VALUE = 0;

    /**
     * Default value for an item with unknown quantity
     */
    private int DEFAULT_QUANTITY_VALUE = 0;

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

        mSoldTextView = (TextView) findViewById(R.id.product_sold_tv);
        mShippedTextView = (TextView) findViewById(R.id.product_shipped_tv);

        /**
         * Will be used to build the
         * dialog that will serve to modify either the
         * sold or the shipped value
         */

        // Get the EditText from the activity_editor.xml
        // Detach it from it original parent, so It could be attached
        // to the dialog

        final EditText editTextDialog = (EditText) findViewById(R.id.alertDialog_edit_text);// new EditText(this);
        ((ViewGroup) editTextDialog.getParent()).removeView(editTextDialog);

        final AlertDialog mDialog = new AlertDialog.Builder(this).setView(editTextDialog)
                .setTitle("Shipped Value")
                .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        mShippedTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Set the title of the dialog
                // And create an editText that will be
                // post in it.
                // (EditText) findViewById(R.id.alertDialog_edit_text);
                mDialog.setView(editTextDialog);
                editTextDialog.setVisibility(View.VISIBLE);

                final int shippedEditTextVal = !TextUtils.isEmpty(editTextDialog.getText()) ?
                        InventoryEntry.DEFAULT_SOLD_OR_SHIPPED
                        : Integer.parseInt(editTextDialog.getText().toString());

                // Set a listener for when the dialog will show
                // After that, set a click listener on the positive Button
                mDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        // Get the positive button from the dialog
                        // and set a click listener.
                        // If the user tries to click on the positive button
                        // when the value in the field is inferior of to the sold item
                        // display a Toast and don't quit the dialog
                        Button okButton = ((AlertDialog) mDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        okButton.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                // If the shipped value entered is bellow than the sold value,
                                // then don't close the dialog and display a Toast,
                                // so the user could type a new value
                                if (shippedEditTextVal < Integer.parseInt(mSoldTextView.getText().toString())) {
                                    Toast.makeText(EditorActivity.this, "Value can't be bellow " + mSoldTextView.getText().toString()
                                            , Toast.LENGTH_LONG);
                                } else {
                                    //Dismiss once the shipped value is greater
                                    // then the sold value.
                                    mDialog.dismiss();
                                }
                                ((ViewGroup) editTextDialog.getParent()).removeView(editTextDialog);
                            }
                        });

                        // Set a behavior on the negative button for the dialog
                        // Once it clicked, the dialog should close it self
                        Button cancelButton = ((AlertDialog) mDialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                        cancelButton.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                mDialog.cancel();
                                ((ViewGroup) editTextDialog.getParent()).removeView(editTextDialog);
                            }
                        });
                        mDialog.show();
                    }
                });
            }
        });

                mSoldTextView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // Set the title of the dialog
                        // and place an editText inside

                        mDialog.setView(editTextDialog);
                        editTextDialog.setVisibility(View.VISIBLE);
                        mDialog.show();

                        /*final int soldEditTextVal = !TextUtils.isEmpty(editTextDialog.getText()) ?
                                InventoryEntry.DEFAULT_SOLD_OR_SHIPPED
                                : Integer.parseInt(editTextDialog.getText().toString());*/

                        // Set a listener for when the dialog will show.
                        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                            @Override
                            public void onShow(DialogInterface dialogInterface) {

                                // Get the positive button from the dialog
                                // and set a click listener.
                                // If the user tries to click on the positive button
                                // when the value in the field is inferior of to the sold item
                                // display a Toast and don't quit the dialog
                                Button okButton = ((AlertDialog) mDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                                okButton.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View view) {
                                        // If the sold value entered is greater than the shipped value,
                                        // then don't close the dialog and display a Toast,
                                        // so the user could correct his value
                                        int soldEditTextVal = TextUtils.isEmpty(editTextDialog.getText()) ?
                                                InventoryEntry.DEFAULT_SOLD_OR_SHIPPED
                                                : Integer.parseInt(editTextDialog.getText().toString());

                                        if (soldEditTextVal > Integer.parseInt(mShippedTextView.getText().toString())) {
                                            Toast.makeText(EditorActivity.this, "Value can't be greater than " + mSoldTextView.getText().toString(),
                                                    Toast.LENGTH_LONG);
                                        } else {
                                            //Dismiss once the shipped value is greater
                                            // then the sold value.
                                            mDialog.dismiss();
                                        }
                                        ((ViewGroup) editTextDialog.getParent()).removeView(editTextDialog);
                                    }
                                });

                                // Set a behavior on the negative button for the dialog
                                // Once it clicked, the dialog should close it self
                                Button cancelButton = ((AlertDialog) mDialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                                cancelButton.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View view) {
                                        mDialog.cancel();
                                       // ((ViewGroup) editTextDialog.getParent()).removeView(editTextDialog);
                                    }
                                });

                            }
                        });
                    }
                });

                mSoldEditText = (EditText) findViewById(R.id.edit_product_sold);
                mShippedEditText = (EditText) findViewById(R.id.edit_product_shipped);

                mSupplierEdtiText = (EditText) findViewById(R.id.edit_product_supplier);

                // Buttons to increment and decrement
                // the shipped and sold quantity.
                // These Buttons have an effect on the
                // number of the Sales TextView
                // or the Shipped TextView

       /* mSalesDecrement = (Button) findViewById(R.id.sales_decrement);
        mSalesIncrement = (Button) findViewById(R.id.sales_increment);

        mShippedDecrement = (Button) findViewById(R.id.shipped_decrement);
        mShippedIncrement = (Button) findViewById(R.id.shipped_increment);
        */
                // Get the intent from the CatalogActivity
                // Change the title of the Editor Activity based on the action that will occur
                // Get the item Uri from the intent made by the Catalog activity
                mItemUri = getIntent().getData(); // There are no daya

                if (mItemUri != null) {
                    // If the extra contains an Uri, the Activity 's title should be "Edit Item"
                    setTitle(R.string.editItemTitle); // If there's no Uri = add button was pressed!


                    // Only keep the sold and shipped textViewsvisible
                    // By removing the sold and shipped fields
                    mShippedEditText.setVisibility(View.GONE);
                    mSoldEditText.setVisibility(View.GONE);

                } else {
                    // If the extra doesn't contain an Uri, the title Activity's should be "Add an Item"
                    setTitle(R.string.addItemTitle);

                    // Only keep the sold and shipped fields visible
                    // By removing the sold and shipped textViews
                    mSoldTextView.setVisibility(View.GONE);
                    mShippedTextView.setVisibility(View.GONE);

                    // Invalidate the options menu, so the "Delete" menu option can be hidden.
                    // (It doesn't make sense to delete a pet that hasn't been created yet.)
                    invalidateOptionsMenu();

                    // Create a dataBase if it doesn't exist
                    mItemUri = InventoryEntry.CONTENT_URI;
                }

                // Add event listeners on every button so they
                // can update the textView they are associated with.
                // Ex: If the mSalesDecrement is pressed, it will
                // decrease the number of sales appearing in the mSoldTextView

        /*mSalesDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decrementSoldItems();
            }
        });

        mSalesIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementSoldItems();
            }
        });

        mShippedIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incrementShippedItems();
            }
        });

        mShippedDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decrementShippedItems();
            }
        });*/
                // Initiate the loader
                getLoaderManager().initLoader(EDITOR_LOADER_ID, null, this);
            }

            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

                // The thing is that we must create a database because it doe
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

                // If our cursor only contains 1 pet
                // this means that the user want to update informations related
                // to that pet. Then, show all the information related to that pet
                // inside the edit text views.

                // What if the user want to add something ?

                if (mItemUri != InventoryEntry.CONTENT_URI) {

                    // Move the cursor to the concerned row before getting data from it
                    cursor.moveToFirst();

                    // Get the name from the cursor and put it on the appropriate edit text
                    int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
                    mNameEditText.setText(cursor.getString(nameColumnIndex), TextView.BufferType.EDITABLE);

                    // Get the price from the cursor and put it on the appropriate edit text
                    int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
                    int priceNumber = cursor.getInt(priceColumnIndex);
                    mPriceEditText.setText(String.format(NUMBER_FORMAT, priceNumber), TextView.BufferType.EDITABLE);

                    // Get the quantity from the cursor and put it on the appropriate edit text
                    int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
                    int quantityNumber = cursor.getInt(quantityColumnIndex);
                    mQuantityTextView.setText(String.format(NUMBER_FORMAT, quantityNumber), TextView.BufferType.EDITABLE);

                    // Get the number of sold item from the cursor and put it on the appropriate edit text
                    int soldColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SOLD);
                    int soldNumber = cursor.getInt(soldColumnIndex);
                    mSoldTextView.setText(String.format(NUMBER_FORMAT, soldNumber), TextView.BufferType.EDITABLE);

                    // Get the number of shipped items from the cursor and put it on the appropriate edit text
                    int shippedColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SHIPPED);
                    int shippedNumber = cursor.getInt(shippedColumnIndex);
                    mShippedTextView.setText(String.format(NUMBER_FORMAT, shippedNumber), TextView.BufferType.EDITABLE);

                    // Get the supplier from the cursor and put it on the appropriate edit text
                    int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER);
                    mSupplierEdtiText.setText(cursor.getString(supplierColumnIndex), TextView.BufferType.EDITABLE);

                }
                // Here the user wanna do what ?
                else {
                   // Couper de la
                   //
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }

            /**
             * Is called when the decrement button of
             * the sold item is pressed.
             */


            /**This will make certain options invisible  */
    /*@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        //if (mPetUri == PetEntry.CONTENT_URI) {
        MenuItem menuItem = menu.findItem(R.id.action_delete);
        menuItem.setVisible(false);
        //}
        return true;
    }*/

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

                        if (TextUtils.isEmpty(mNameEditText.getText())) {
                            // Initializing the Toast used when the user forget to type the name
                            // of the product
                            mEmptyNameToast = Toast.makeText(this, R.string.emptyNameMessage, Toast.LENGTH_LONG);
                            mEmptyNameToast.show();

                            // mNameEditText.requestFocus();
                            // Also, is the quantity or the sold/shipped number higher than 1000 ?
                            // Sold can not be bellow the shipped, it doesn'T make sens
                            // If the shipped is bellow, it should give an error
                        } else {
                            saveItem();
                            finish();
                        }
                /*
                if (!allFieldsAreEmpty()) {
                    savePet();
                }*/
                        // We should remove the finish ?

                /*Intent intent = new Intent(EditorActivity.this,CatalogActivity.class);
                startActivity(intent);*/
                        return true;

                    // Respond to a click on the "Delete" menu option
                    case R.id.action_delete:
                        // Open the dialog to confirm the deletion
                        // Call the deletePet method to delete the correct
                        // pet

                        // Delete pet : Verify the Edit mode --> delete
                        // --> Show a toast after completion

                        return true;
                    // Respond to a click on the "Up" arrow button in the app bar
                    case android.R.id.home:
                /*// Navigate back to parent activity (CatalogActivity)
                if (mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                }
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
                return true;*/
                }
                return super.onOptionsItemSelected(item);
            }

            // This method provides the Quantity by soustracting the
            // Sold items from the shipped items.
            private String quantityLeft(TextView shippedView, TextView soldView) {

                // Pass a view as a paramater, so We can know weither
                // to get the textView Content or The EditText Content

                // How to also include the textViews for the sold and shipped items ?
                // May be IDK !
                int leftQuantity;
                int soldNumber = TextUtils.isEmpty(soldView.getText().toString()) ?
                        InventoryEntry.DEFAULT_SOLD_OR_SHIPPED
                        : Integer.parseInt(soldView.getText().toString());

                int shippedNumber = TextUtils.isEmpty(shippedView.getText().toString()) ?
                        InventoryEntry.DEFAULT_SOLD_OR_SHIPPED
                        : Integer.parseInt(shippedView.getText().toString());

                // if there are no remaining shipped items, then the
                // quantity should be "0"
                if (TextUtils.isEmpty(soldView.getText())) {
                    leftQuantity = DEFAULT_QUANTITY_VALUE;
                } else {
                    leftQuantity = shippedNumber - soldNumber;
                }
                return String.format(NUMBER_FORMAT, leftQuantity);
            }

            // Once the user clicks on the "save" button
            // Verify if the form has a name and it must have
            // a name ! If it doesn'T have a name, show a toaster ...
            private void saveItem() {

                // Get the text from all the "editTexts" fields
                String nameString = mNameEditText.getText().toString().trim(); // Should not be null
                int soldNumber = Integer.parseInt(mSoldTextView.getText().toString().trim());
                int shippedNumber = Integer.parseInt(mShippedTextView.getText().toString().trim());
                String supplierString = mSupplierEdtiText.getText().toString().trim();

                // If the user want to add a new item,
                // get the values of the shipped and sold item from
                // their EditText not their TextViews

                if (mItemUri == InventoryEntry.CONTENT_URI) {
                    soldNumber = Integer.parseInt(mSoldEditText.getText().toString().trim());
                    shippedNumber = Integer.parseInt(mShippedEditText.getText().toString().trim());
                }

                // Set the sold and shipped values on their textViews
                // If the EditText is not empty, then save it value inside the
                // corresponding TextView.
                // Example : If the user enters a number of 10 in the shipped item
                // EditText, store that value inside the shipped item TextView

                // We should delete this !

        /*if (TextUtils.isEmpty(mShippedEditText.getText())){
            mShippedTextView.setText(mShippedEditText.getText());
        }
        if (TextUtils.isEmpty(mSoldEditText.getText())){
            mSoldTextView.setText(mSoldEditText.getText());
        }*/

                // By default, the price and the quantity are equal to "0"
                double priceNumber = DEFAULT_PRICE_VALUE;
                int quantityNumber = DEFAULT_QUANTITY_VALUE;

                // Verify if the Edit text for the price is not empty
                // then set the price variable with the value from the price Edit Text
                if (!mPriceEditText.getText().toString().isEmpty()) {
                    priceNumber = Double.parseDouble(mPriceEditText.getText().toString().trim());
                }

                // Set the Quantity based on the number of shipped and sold items
                // If the difference between the shipped and sold item is not equal to
                // zero, then set the quantity view with the value of the difference between shipped and sold

                if (!quantityLeft(mShippedEditText, mSoldEditText)
                        .equals(String.format(NUMBER_FORMAT, DEFAULT_QUANTITY_VALUE))) {

                    mQuantityTextView.setText(quantityLeft(mShippedEditText, mSoldEditText));
                    quantityNumber = Integer.parseInt(quantityLeft(mShippedEditText, mSoldEditText).trim());
                }

                // Create a ContentValues to store the informations of the new pet
                ContentValues newPetValues = new ContentValues();
                newPetValues.put(InventoryEntry.COLUMN_ITEM_NAME, nameString);
                newPetValues.put(InventoryEntry.COLUMN_ITEM_PRICE, priceNumber);
                newPetValues.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantityNumber);
                newPetValues.put(InventoryEntry.COLUMN_ITEM_SOLD, soldNumber);
                newPetValues.put(InventoryEntry.COLUMN_ITEM_SHIPPED, shippedNumber);
                newPetValues.put(InventoryEntry.COLUMN_ITEM_SUPPLIER, supplierString);


                Uri newRowUri = null;
                int updatedPet = 0;

                // Check if the "mItemUri" point to
                // one element or to the entire data base.
                // If it point to the entire database, this means
                // that we need to insert a new Pet,
                // other wise, modifications should be made only
                // on 1 pet

                if (mItemUri == InventoryEntry.CONTENT_URI) {
                    // Insert the newPetValues inside the "inventory" table of the data base
                    newRowUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, newPetValues);
                } else {

                    // updatedPet = getContentResolver().update(mPetUri, newPetValues, null, null);
                }

                // This is only adding a pet but not updating it
                // Go inside the "insert" of the content provider to check if
                // it takes into account case where it's a new pet
        /*
        if (newRowUri == null && updatedPet == 0) {
            mSavingStateToast.setText(R.string.unsuccessful_save_text);
        } else if (newRowUri != null || updatedPet > 0) {
            mSavingStateToast.setText(R.string.successful_save_text);
        }*/
                if (newRowUri != null || updatedPet > 0) {
                    mSavingStateToast = Toast.makeText(this, R.string.successful_save_text, Toast.LENGTH_SHORT);
                }
                mSavingStateToast.show();
            }
        }
