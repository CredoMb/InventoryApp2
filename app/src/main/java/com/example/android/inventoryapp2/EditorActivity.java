package com.example.android.inventoryapp2;


import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.example.android.inventoryapp2.data.InventoryContract.InventoryEntry;
import com.google.android.material.textfield.TextInputLayout;


import java.io.InputStream;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Will be used to display messages in the Log
     */
    private static final String TAG = EditorActivity.class.getSimpleName();

    /**
     * EditText field to enter the product's name
     */
    private TextInputEditText mNameInputEditText;

    /**
     * Will be used as the title of the Intent
     * to open the image stored in the phone
     */
    private static final String SELECT_PICTURE = "Select Picture";

    /**
     * This will contain the uri of the image
     * in a String format
     */

    private String mImageUriString;

    /**
     * ImageView to store the product's Image
     * Unfortunatelly,for some reasons, the imageView variable doesn't work.
     * So we couldn't use it inside "onCreate".
     */
    // private ImageView mProductImageView;

    /**
     * Will help us to load the item image onto its
     * ImageView
     */

    private GlideHelperClass mGlideHelper;

    /**
     * EditText field to enter the product's price
     */
    private EditText mPriceEditText;

    /**
     * EditText to enter the Quantity
     */
    private EditText mQuantityEditText;

    /** TextInputLayout of the Quantity EditText*/
    private TextInputLayout mQuantityTextInputLayout;

    /** Button to store the image Button used
     *  to decrease the quantity*/
    private ImageButton mDecreaseIB;

    /** Button to store the image Button used
     *  to increase the quantity*/
    private ImageButton mIncreaseIB;

    /**
     * Will be used inside "incrementOrDecrementQuantity" to determine
     * that the Quantity value must be incremented
     */
    private final String ACTION_INCREMENT = "increment";

    /**
     * Will be used inside "incrementOrDecrementQuantity" to signify
     * that the Quantity value must be decremented
     */
    private final String ACTION_DECREMENT = "decrement";

    /**
     * Will be used as the signature for the supply request
     */
    private String APP_NAME;

    /**
     * EditText field to enter the supplier's name
     */
    private EditText mSupplierEdtiText;

    /**
     * The order button to send an email to the supplier
     */
    private Button mOrderButton;

    /**
     * This will be used to store the product Uri received from the Catalog Activity
     */
    private Uri mItemUri;

    /**
     * Will store the index received from the CatalogActivity
     */
    private int mItemPostion;

    /**
     * Default index for the position received as an intent extra
     * sent by the CatalogActivity
     */
    private int DEFAULT_INDEX = -1;

    /**
     * Will receive the stream for the image that should be set
     * as the thumbnail of the item
     */
    private InputStream mImageStream;

    /**
     * The Id for the Loader of the Editor Activity
     */
    private int EDITOR_LOADER_ID = 2;

    /**
     * Number format
     */
    private String NUMBER_FORMAT = "%d";

    /**
     * Decimal Format
     */
    private String DECIMAL_FORMAT = "%.2f";

    /**
     * Default value for an item with no price
     */
    private int DEFAULT_PRICE_VALUE = 0;

    /**
     * Default value for an item with unknown quantity
     */
    private int DEFAULT_QUANTITY_VALUE = 0;

    /**
     * The Code to use whith the intent that
     * should get an image from the library
     */
    private static final int PICK_IMAGE = 1;

    /**
     * Boolean flag that keeps track of whether the item has been edited (true) or not (false)
     */
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
        // appropriate variables.
        mNameInputEditText = (TextInputEditText) findViewById(R.id.edit_product_name);

        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
        mQuantityTextInputLayout = (TextInputLayout) findViewById(R.id.QuantityOutlinedTextField);

        mDecreaseIB = (ImageButton) findViewById(R.id.decreaseImageButton);
        mIncreaseIB = (ImageButton) findViewById(R.id.increaseImageButton);

        mSupplierEdtiText = (EditText) findViewById(R.id.edit_product_supplier);
        mOrderButton = (Button) findViewById(R.id.order_button);

        // When creating an email intent for the for the supplier
        // APP_NAME will be used as the default signature, at the bottom of the
        // email.
        APP_NAME = getString(R.string.app_name);

        mGlideHelper = new GlideHelperClass(getApplicationContext(), mImageUriString
                , R.drawable.placeholder_image, ((ImageView) findViewById(R.id.product_image_editor)));

        mIncreaseIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set the "ACTION_INCREMENT" option to
                // the incrementOrDecrementQuantity().
                // The function will increment the quantity value
                incrementOrDecrementQuantity(ACTION_INCREMENT);
            }
        });

        mDecreaseIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set the "ACTION_DECREMENT" option to
                // the incrementOrDecrementQuantity().
                // The function will decrement the quantity value
                incrementOrDecrementQuantity(ACTION_DECREMENT);
            }
        });


        // Set a click listener onto the button associated
        // to the Supplier. When clicked, it will open an intent
        // to send an email to the Supplier.

        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an email to request more supply for the product
                submitRequest();
            }
        });

        // Setting the same touch listener in all of the Edit Text will
        // help us know if the user started editing an item.
        // It will prevent the user to accidently quit the activity in
        // the middle of an edition.

        ((ImageView) findViewById(R.id.product_image_editor)).setOnTouchListener(mTouchListener);
        mNameInputEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierEdtiText.setOnTouchListener(mTouchListener);

        // Get the intent from the CatalogActivity
        // Change the title of the Editor Activity based on the action that will occur
        // Get the item Uri from the intent made by the Catalog activity
        mItemUri = getIntent().getData(); // There are no data
        mItemPostion = getIntent().getIntExtra(Intent.EXTRA_INDEX, DEFAULT_INDEX);

        if (mItemUri == null) {
            // If the extra doesn't contain an Uri, the title Activity's should be "Add an Item"
            setTitle(R.string.addItemTitle);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete an item that hasn't been created yet.)
            invalidateOptionsMenu();

        } else {
            // If the extra contains an Uri, the Activity 's title should be "Edit Item"
            setTitle(R.string.editItemTitle);

            // Initialize a loader to read the item data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EDITOR_LOADER_ID, null, this);
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // The thing is that we must create a database because
        String[] projection = {InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_IMAGE,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_SUPPLIER,
        };

        return new CursorLoader(this,
                mItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // If our cursor only contains al least 1 item,
        // it means that the user want to update it.
        if (cursor.moveToFirst()) {

            // Get the name from the cursor and put it on the appropriate edit text
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
            mNameInputEditText.setText(cursor.getString(nameColumnIndex), TextView.BufferType.EDITABLE);

            // Get the uri of the image in a String form.
            // Using GlideHelper set the image onto the
            // item's ImageView
            mImageUriString = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_IMAGE));

            mGlideHelper.setImageLink(mImageUriString);
            mGlideHelper.loadImage();

            // Get the price from the cursor and put it on the appropriate edit text
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
            double priceNumber = cursor.getDouble(priceColumnIndex);

            mPriceEditText.setText(String.valueOf(priceNumber), TextView.BufferType.EDITABLE);

            // Get the quantity from the cursor and put it on the appropriate edit text
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
            int quantityValueNumber = cursor.getInt(quantityColumnIndex);

            mQuantityEditText.setText(String.format(NUMBER_FORMAT, quantityValueNumber)
                    , TextView.BufferType.EDITABLE);

            // Get the supplier from the cursor and put it on the appropriate edit text
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER);
            mSupplierEdtiText.setText(cursor.getString(supplierColumnIndex), TextView.BufferType.EDITABLE);

        }
        // In case we need
        else {
        }
        // Close the cursor to avoid memory licks
        cursor.close();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * This method is called after invalidateOptionsMenu() so that the
     * menu can be updated. I will hide the "delete" option when the
     * user is adding a new item.
     */
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

    /**
     * Receives the image choosed by the user
     * and set it into the image view of the item.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_IMAGE) {
            try {

/*              This code could be use in case
                there's permission problems.

                // Get permanent access to the returned file.
                //
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // Check for the freshest data.
                getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
*/

                // Turn the uri of the image into a String
                // to store it inside the mImageUriString.
                mImageUriString = data.getData().toString();

                // Finally, set the image onto the image view
                mGlideHelper.setImageLink(mImageUriString);
                mGlideHelper.loadImage();

            } catch (Exception e) {
                // If the file is not found
                // details of the exception will be printed
                // on the log.
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            // In case the user want to change the image of the item
            case R.id.action_change_image:

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, SELECT_PICTURE), PICK_IMAGE);

                return true;

            // In case the user clicks on the save icon,
            // save the item by calling the saveItem() method
            case R.id.action_save:
                saveItem();
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
                } else {
                    // Otherwise if there are unsaved changes, setup a dialog to warn the user.
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
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Increments or decrements the value of the item Quantity.
     * This will update the quantity edit text in real time,
     * as the user presses on the "+" or "-" button
     *
     * @param operationCode will help us determine if we need to
     *                      decrement or increment the quantity value.
     */
    private void incrementOrDecrementQuantity(String operationCode) {

        // Get the current value of the Quantity EditText.
        // If the Quantity Edit Text is empty, set the current quantity
        // to zero. Else, use the current value of the Edit Text
        int currentQuantity = TextUtils.isEmpty(mQuantityEditText.getText().toString()) ?
                DEFAULT_QUANTITY_VALUE :
                Integer.parseInt(mQuantityEditText.getText().toString());

        // Based on the value of "operationCode",
        // determine the operation that should be applied to
        // the "currentQuantity" variable. Either increment or decrement its value.
        switch (operationCode) {
            case ACTION_INCREMENT:
                currentQuantity++;
                break;
            case ACTION_DECREMENT:
                // If the quantity is equal to zero,
                // display a toast message and leave the
                // function, without decrementing
                if (currentQuantity == DEFAULT_QUANTITY_VALUE) {
                    Toast.makeText(this, R.string.invalidQuantityMessage, Toast.LENGTH_SHORT).show();
                    return;
                }
                currentQuantity--;
                break;

            // In case the user enters a bad option,
            // display an error message inside the log
            // and leave the function
            default:
                Log.e(TAG, "Unknown argument for the incrementOrDecrementQuantity() method");
                return;
        }

        // Set the updated quantity value onto the EditText.
        mQuantityEditText.setText(String.valueOf(currentQuantity), EditText.BufferType.EDITABLE);
    }

    /**
     * Will create an email intent. The intent will contain a
     * text made of the supplier name and a small description
     * of the article needed.
     */

    private void submitRequest() {

        // Get the item name of the item from its editText
        String itemName = "";

        // Get the name of the supplier from the Edit Text. If the
        // Edit Text doesn't contain any name, then use the default value.
        String supplierName = TextUtils.isEmpty(mSupplierEdtiText.getText()) ?
                getString(R.string.supplier_name_default):
                mSupplierEdtiText.getText().toString();

        // In case the name is empty, show a toast message and
        // quit the function
        if (TextUtils.isEmpty(mNameInputEditText.getText())) {
            Toast.makeText(this, R.string.emptyNameMessage, Toast.LENGTH_LONG).show();
            return;
        } else {
            itemName = mNameInputEditText.getText().toString();
        }

        String message = createSupplyRequest(itemName, supplierName,APP_NAME);

        // Use an intent to launch an email app.
        // Send the order summary in the email body.
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.supply_request_email_subject, itemName));
        intent.putExtra(Intent.EXTRA_TEXT, message);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Create the request summary that should be sent by email to the
     * supplier
     *
     * @param itemName       is the name of the item we shall send a request for.
     * @param emailSignature is the signature that will be placed at the bottom of
     *                       the email body
     */

    private String createSupplyRequest(String itemName, String supplierName, String emailSignature) {

        String supplyMessage = getString(R.string.supply_request_greeting, supplierName);
        supplyMessage += getString(R.string.supply_request_body, itemName);
        supplyMessage += "\n\n";
        supplyMessage += getString(R.string.supply_request_signature, emailSignature);

        return supplyMessage;
    }

    /**
     * This method will be called once the user
     * clicks on "save" icon. It contains instruction
     * to either save or update an item in the database.
     */

    private void saveItem() {

        // Get the text from all the "editTexts" fields
        String nameString = mNameInputEditText.getText().toString().trim();

        // If the uriString is not provided by the user,
        // Use "no_uri" by default.
        String imageStringUri = TextUtils.isEmpty(mImageUriString) ?
                InventoryEntry.DEFAULT_URI
                : mImageUriString;

        String supplierString = mSupplierEdtiText.getText().toString().trim();

        // Verify if the form has a name and it must have
        // a name ! If it doesn'T have a name, show a toaster ...
        if (TextUtils.isEmpty(nameString)) {
            // Initializing the Toast used when the user forget to type the name
            // of the product.
            Toast.makeText(this, R.string.emptyNameMessage, Toast.LENGTH_LONG).show();
            return;
        }

        // If the price number is not provided by the user,
        // don't try to parse the string into an double value. Use 0 by default.
        double priceNumber = TextUtils.isEmpty(mPriceEditText.getText().toString()) ?
                InventoryEntry.DEFAULT_PRICE
                : Double.parseDouble(mPriceEditText.getText().toString().trim());

        // If the quantity number is not provided by the user,
        // don't try to parse the string into an integer value. Use 0 by default.
        int quantityNumber = TextUtils.isEmpty(mQuantityEditText.getText().toString()) ?
                InventoryEntry.DEFAULT_QUANTITY
                : Integer.parseInt(mQuantityEditText.getText().toString().trim());

        // Make sure the user didn't enter a negative value for
        // the quantity. If he did, a toast message will be displayed
        // to prevent him.
        if (quantityNumber < DEFAULT_QUANTITY_VALUE) {
            // Advise the uer to adjust the values of the quantity
            Toast.makeText(this, R.string.invalidQuantityMessage, Toast.LENGTH_LONG).show();
            return;
        }

        // Create a ContentValues to store the informations of the new item
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryEntry.COLUMN_ITEM_IMAGE, imageStringUri);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, priceNumber);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantityNumber);
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
            // For the selection and selection args, pass in null values
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

    /**
     * Prompt the user to confirm that they want to delete this item.
     */
    private void showDeleteConfirmationDialog() {

        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this);
        alertDialog.setMessage(R.string.delete_dialog_msg);

        alertDialog.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

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
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this);
        alertDialog.setMessage(R.string.unsaved_changes_dialog_msg);
        alertDialog.setPositiveButton(R.string.discard, discardButtonClickListener);
        alertDialog.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog

        alertDialog.show();
    }
}
