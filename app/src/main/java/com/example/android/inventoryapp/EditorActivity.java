package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.example.android.inventoryapp.data.InventoryContract;
import com.google.android.material.textfield.TextInputEditText;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.InputStream;
import java.net.URI;
import java.text.NumberFormat;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * EditText field to enter the product's name
     */
    private TextInputEditText mNameInputEditText;

    /**
     * This will contain the uri of the image
     * in a String format*/

    private String mImageUriString;

    /**
     * ImageView to store the product's Image
     * Unfortunatelly,for some reasons, the imageView variable doesn't work.
     * So we couldn't use it inside "onCreate".
     */
    // private ImageView mProductImageView;

    /**
     * EditText field to enter the product's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the product'S quantity
     */
    private TextView mQuantityTextView;

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

    /** Will store the index received from the CatalogActivity*/
    private int mItemPostion;

    /** Default index for the position received as an intent extra
     *  sent by the CatalogActivity*/
    private int DEFAULT_INDEX = -1;

    /** Will receive the stream for the image that should be set
     *  as the thumbnail of the item*/
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

        // Get the ImageView that will contain the thumbnail of the
        // product.
        ;

        // Find the view related to the database column and store them into
        // appropriate variables
        mNameInputEditText = (TextInputEditText) findViewById(R.id.edit_product_name);

        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mQuantityTextView = (TextView) findViewById(R.id.product_quantity_tv);

        mSoldEditText = (EditText) findViewById(R.id.edit_product_sold);
        mShippedEditText = (EditText) findViewById(R.id.edit_product_shipped);

        mSupplierEdtiText = (EditText) findViewById(R.id.edit_product_supplier);

        // Set a click listener onto the product image view.
        // When clicked, it will start an intent to find a picture
        // from the device's files.

        // This use of the Product imageView is temporary, until we find a solution
        // because storing the ImageView inside a variable doesn't work.
        ((ImageView) findViewById(R.id.product_image_editor)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        // Setting the same touch listener in all of the Edit Text will
        // help us know if the user started editing an item.
        // It will prevent the user to accidently quit the activity in
        // the middle of an edition.

        ((ImageView) findViewById(R.id.product_image_editor)).setOnTouchListener(mTouchListener);
        mNameInputEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mShippedEditText.setOnTouchListener(mTouchListener);
        mSoldEditText.setOnTouchListener(mTouchListener);
        mSupplierEdtiText.setOnTouchListener(mTouchListener);

        // Get the intent from the CatalogActivity
        // Change the title of the Editor Activity based on the action that will occur
        // Get the item Uri from the intent made by the Catalog activity
        mItemUri = getIntent().getData(); // There are no data
        mItemPostion = getIntent().getIntExtra(Intent.EXTRA_INDEX,DEFAULT_INDEX);

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
            mNameInputEditText.setText(cursor.getString(nameColumnIndex), TextView.BufferType.EDITABLE);

            // Get the uri of the image in a String form.
            // Turn the String into an Uri and set the image onto the
            // item's ImageView
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_IMAGE);
            Log.e("the image index",String.valueOf(imageColumnIndex));
            mImageUriString = cursor.getString(imageColumnIndex);

            Uri imageUri = Uri.parse(mImageUriString);
            setItemImage(imageUri, ((ImageView) findViewById(R.id.product_image_editor)));

            // Get the price from the cursor and put it on the appropriate edit text
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
            double priceNumber = cursor.getDouble(priceColumnIndex);

            mPriceEditText.setText(String.valueOf(priceNumber), TextView.BufferType.EDITABLE);

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

            mQuantityTextView.setText(getString(R.string.quantity_text) +
                            String.format(NUMBER_FORMAT, quantityNumber)
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
     * and set it into the image view of the product.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_IMAGE) {
            try {
                // Turn the uri of the image into a String
                // to store it inside the mImageUriString.
                mImageUriString = data.getData().toString();
                // Use the Uri to set the image onto its ImageView
                setItemImage(data,((ImageView) findViewById(R.id.product_image_editor)));

            } catch (Exception e) {
                // If the file is not found
                // details of the exception will be printed
                // on the log.
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**Set the image of the item using its Uri path*/
    public void setItemImage(Intent imageIntent, ImageView itemIv) {
        // Get the Uri of attached to the intent
        Uri imagePath = imageIntent.getData();


        try {
        // Get the Image as an InputStream by using its "URI".
        InputStream imageStream = getContentResolver().openInputStream(imagePath);

        // Turns the imageStream to a Bitmap
        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
        itemIv.setImageBitmap(selectedImage);}

        catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
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

    // This method provides the Quantity by soustracting the
    // Sold items from the shipped items.
    private String quantityLeft(int shipped, int sold) {

        // Find the left Quantity by soustracting the sold
        // items from the shipped.
        int leftQuantity = shipped - sold;

        return String.format(NUMBER_FORMAT, leftQuantity);
    }

    /**
     * This method will be called once the user
     * clicks on "save" icon. It contains instruction
     * to either save or update an item in the database
     */

    private void saveItem() {

        // Get the text from all the "editTexts" fields
        String nameString = mNameInputEditText.getText().toString().trim();
        String imageStringUri = mImageUriString;
        String supplierString = mSupplierEdtiText.getText().toString().trim();

        // Verify if the form has a name and it must have
        // a name ! If it doesn'T have a name, show a toaster ...
        if (TextUtils.isEmpty(nameString)) {
            // Initializing the Toast used when the user forget to type the name
            // of the product.
            Toast.makeText(this, R.string.emptyNameMessage, Toast.LENGTH_LONG).show();

            return;
        }

        // Check if it's a new item
        // and check if all the fields are blank.
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
        } else {
            // Advise the uer to adjust the values of
            // shipped and sold items.
            Toast.makeText(this, R.string.invalidQuantityMessage, Toast.LENGTH_LONG).show();
            return;
        }

        // Create a ContentValues to store the informations of the new item
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryEntry.COLUMN_ITEM_IMAGE,imageStringUri);
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

        // If the Image is null, finish the activity right away
        /* if (mImageStream == null) {
            finish();
        } */

        //  Start an intent to open back the CatalogActivity.
        // Add the position and the corresponding Image of the item.
        // This will help to update the item's thumbNail
        // inside of the CatalogActivity.

        // The following will bot be needed anymore, I think.
        //
        /*Intent intent = new Intent(EditorActivity.this, CatalogActivity.class);

        Bitmap selectedImage = BitmapFactory.decodeStream(mImageStream);

        // Send the image and the position of the item to the CatalogActivity.
        // This will help the CatalogActivity to update the image of the given item
        intent.putExtra(Intent.EXTRA_STREAM,selectedImage);
        intent.putExtra(Intent.EXTRA_INDEX,mItemPostion);

        startActivity(intent); */
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
