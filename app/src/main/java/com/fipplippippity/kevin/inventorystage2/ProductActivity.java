package com.fipplippippity.kevin.inventorystage2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.fipplippippity.kevin.inventorystage2.sqlite.InventoryContract.InventoryEntry;

public class ProductActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public final static int MAIN_TO_ADD_VIEW = 0,
            LIST_TO_PROD_VIEW = 1,
            PROD_VIEW_TO_PROD_EDIT = 2,
            PROD_EDIT_TO_PROD_VIEW = 3;
    private final static int NUM_OF_STATES = 4;

    private static boolean[] productState = {
            false,  /*MAIN_TO_ADD_VIEW*/
            false,  /*LIST_TO_PROD_VIEW*/
            false,  /*PROD_VIEW_TO_PROD_EDIT*/
            false   /*PROD_EDIT_TO_PROD_VIEW*/
    };
    private static int currentState;
    private final int MAX_QTY = 300000;
    private final int SAVE_RETURN_SUCCESS = 1;
    private final int SAVE_RETURN_FAIL = 2;
    ContentValues saveInventoryRowValues;
    private MenuItem menuDelete,
            menuEdit,
            menuCancel,
            menuAccept;

    private EditText etProductName, etPrice, etQty, etSupplierName, etSupplierPhone;
    private FloatingActionButton fabMinus, fabPlus, fabPhone;
    private Uri currentInventoryURI;
    private String currentDBProductName;
    private int currentDBProductPrice;
    private int currentDBProductQty;
    private String currentDBSupplierName;
    private String currentDBSupplierPhone;
    private String currentETProductName,
            currentETProductPrice,
            currentETProductQty,
            currentETSupplierName,
            currentETSupplierPhone;
    private boolean dialogClicked = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_detail);

        etProductName = findViewById(R.id.et_product_name);
        etPrice = findViewById(R.id.et_product_price);
        etQty = findViewById(R.id.et_product_qty);
        etSupplierName = findViewById(R.id.et_supplier_name);
        etSupplierPhone = findViewById(R.id.et_supplier_phone);
        fabMinus = findViewById(R.id.fab_qty_minus);
        fabPlus = findViewById(R.id.fab_qty_plus);
        fabPhone = findViewById(R.id.fab_phone_order);

        fabMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentETProductQty = etQty.getText().toString().trim();
                if (TextUtils.isEmpty(currentETProductQty)) {
                    currentETProductQty = "0";
                }
                int txtQty = Integer.valueOf(currentETProductQty);
                if (txtQty >= 1) {
                    txtQty--;
                }
                currentETProductQty = String.valueOf(txtQty);
                etQty.setText(currentETProductQty);
            }
        });

        fabPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentETProductQty = etQty.getText().toString().trim();
                if (TextUtils.isEmpty(currentETProductQty)) {
                    currentETProductQty = "0";
                }
                int txtQty = Integer.valueOf(currentETProductQty);
                if (txtQty < MAX_QTY) {
                    txtQty++;
                }
                currentETProductQty = String.valueOf(txtQty);
                etQty.setText(currentETProductQty);
            }
        });

        switch (currentState) {
//            this *is* called when going back to main and then opening add again
            case MAIN_TO_ADD_VIEW:
                setTitle(getString(R.string.product_title_add_new));
                fabPhone.hide();
                lockEditables(false);
                break;

            case LIST_TO_PROD_VIEW:
                currentInventoryURI = getIntent().getData();
                setTitle(getString(R.string.product_title_view_current));
                fabPhone.show();
                lockEditables(true);
                getLoaderManager().initLoader(MainActivity.INVENTORY_LOADER, null, this);
                break;
//
////          todo: these two are *not* called when this change is made. need to update accordingly.
//            case PROD_VIEW_TO_PROD_EDIT:
//                setTitle(getString(R.string.product_title_edit_current));
//                break;
//
//            case PROD_EDIT_TO_PROD_VIEW:
//                setTitle(getString(R.string.product_title_view_current));
//                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (menuDelete == null && menuEdit == null && menuCancel == null && menuAccept == null) {
            menuDelete = menu.findItem(R.id.menu_product_delete);
            menuEdit = menu.findItem(R.id.menu_product_edit);
            menuCancel = menu.findItem(R.id.menu_product_cancel);
            menuAccept = menu.findItem(R.id.menu_product_accept);
        }
//        moving from main (fa button) to add
        setupMenus(currentState);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_product_accept:
//              ie if you clicked the fa button on mainactivity to get here to add a new product
//              now you are clicking to accept changes/addition
                if (currentState == MAIN_TO_ADD_VIEW) {
//                    todo: save to db, etc.
                    getCurrentTextFields();
                    if (allFieldsEmpty() || anyFieldEmpty()) {
                        Toast.makeText(this, getString(R.string.err_product_save_empty_fields), Toast.LENGTH_SHORT).show();
                        break;
                    }

                    int returnSaveInventory = saveInventory();
                    if (returnSaveInventory == SAVE_RETURN_FAIL) {
                        break;
                    }
                    resetProductState();
                    lockEditables(true);
                    NavUtils.navigateUpFromSameTask(ProductActivity.this);

//                  ie if you came from view by clicking the edit button, and want to accept changes
                } else if (currentState == PROD_VIEW_TO_PROD_EDIT) {
                    getCurrentTextFields();
                    if (allFieldsEmpty() || anyFieldEmpty()) {
                        Toast.makeText(this, getString(R.string.err_product_save_empty_fields), Toast.LENGTH_SHORT).show();
                        break;
                    } else if (!changesMade()) {
//                        todo: hardcoding
                        setProductState(PROD_EDIT_TO_PROD_VIEW);
                        resetTextFieldsAfterEditCancel();
                        setupMenus(PROD_EDIT_TO_PROD_VIEW);
                        lockEditables(true);
                        fabPhone.show();
                        setTitle(getString(R.string.product_title_view_current));
                        Toast.makeText(this, "no changes were made", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    int returnSaveInventory = saveInventory();
                    if (returnSaveInventory == SAVE_RETURN_FAIL) {
                        break;
                    }
                    setProductState(PROD_EDIT_TO_PROD_VIEW);
                    setupMenus(PROD_EDIT_TO_PROD_VIEW);
                    lockEditables(true);
                    fabPhone.show();
                    setTitle(getString(R.string.product_title_view_current));
                }
                return true;

            case R.id.menu_product_cancel:

//              ie if you came from main/fa button to add a new product, but now want to cancel edits
                if (currentState == MAIN_TO_ADD_VIEW) {
//                    if no changes were made to main-add screen
                    if (!changesMade()) {
                        resetProductState();
                        lockEditables(true);
                        NavUtils.navigateUpFromSameTask(ProductActivity.this);
                        return true;
                    } else { // if changes /were/ made to main-add screen
                        DialogInterface.OnClickListener discardButtonClickListener =
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // User clicked "Discard" button, navigate to parent activity.
                                        resetProductState();
                                        lockEditables(true);
                                        NavUtils.navigateUpFromSameTask(ProductActivity.this);
                                    }
                                };
                        // Show a dialog that notifies the user they have unsaved changes
                        dialogUnsavedChanges(discardButtonClickListener);
                        return true;
                    }
//                  ie if you came from view to edit, but now want to cancel edits
                } else if (currentState == PROD_VIEW_TO_PROD_EDIT) {
//                    if no changes were made to edit screen
                    if (!changesMade()) {
                        setProductState(PROD_EDIT_TO_PROD_VIEW);
                        resetTextFieldsAfterEditCancel();
                        lockEditables(true);
                        setupMenus(PROD_EDIT_TO_PROD_VIEW);
                        setTitle(getString(R.string.product_title_view_current));
                        fabPhone.show();
                    } else {
                        DialogInterface.OnClickListener discardButtonClickListener =
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // User clicked "Discard" button, navigate to parent activity.
                                        setProductState(PROD_EDIT_TO_PROD_VIEW);
                                        resetTextFieldsAfterEditCancel();
                                        lockEditables(true);
                                        setupMenus(PROD_EDIT_TO_PROD_VIEW);
                                        setTitle(getString(R.string.product_title_view_current));
                                        fabPhone.show();
                                    }
                                };
                        // Show a dialog that notifies the user they have unsaved changes
                        dialogUnsavedChanges(discardButtonClickListener);
                        return true;
                    }
                }
                return true;


            case R.id.menu_product_delete:
                deleteInventoryRow();
                resetProductState();
                lockEditables(true);
                NavUtils.navigateUpFromSameTask(ProductActivity.this);
                return true;

            case R.id.menu_product_edit:
//                translation: if you were already in edit view, changed the state as if you just
//                entered there by clicking a list item (ie go back to read-only mode of the product)
                if (currentState == LIST_TO_PROD_VIEW || currentState == PROD_EDIT_TO_PROD_VIEW) {
                    setupMenus(PROD_VIEW_TO_PROD_EDIT);
                    setProductState(PROD_VIEW_TO_PROD_EDIT);
                    lockEditables(false);
                    setTitle(getString(R.string.product_title_edit_current));
                    fabPhone.hide();
                    return true;
                }

            case android.R.id.home:
                if (currentState == MAIN_TO_ADD_VIEW) {
//                    if changes weren't made on main->add
                    if (!changesMade()) {
                        navHomeOrBack(false);
                        return true;
                    } else { // if changes were made on main->add
                        DialogInterface.OnClickListener discardButtonClickListener =
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // User clicked "Discard" button, navigate to parent activity.
                                        navHomeOrBack(false);
                                    }
                                };
                        // Show a dialog that notifies the user they have unsaved changes
                        dialogUnsavedChanges(discardButtonClickListener);
                        return true;
                    }
                } else {  // if currentstate is other than main-add
                    if (!changesMade()) {
                        navHomeOrBack(false);
                        return true;
                    } else {
                        DialogInterface.OnClickListener discardButtonClickListener =
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // User clicked "Discard" button, navigate to parent activity.
                                        resetTextFieldsAfterEditCancel();
                                        dialogClicked = true;
                                        navHomeOrBack(false);
                                    }
                                };
                        // Show a dialog that notifies the user they have unsaved changes
                        dialogUnsavedChanges(discardButtonClickListener);
                        return true;
                    }
                }
        }

        return super.onOptionsItemSelected(item);
    }

    private void navHomeOrBack(boolean backPressed) {

        if (currentState == MAIN_TO_ADD_VIEW || currentState == LIST_TO_PROD_VIEW || currentState == PROD_EDIT_TO_PROD_VIEW) {
            resetProductState();
            if (!backPressed) {
                NavUtils.navigateUpFromSameTask(ProductActivity.this);
            } else if (dialogClicked) {
                NavUtils.navigateUpFromSameTask(ProductActivity.this);
                dialogClicked = false;
            }

        } else if (currentState == PROD_VIEW_TO_PROD_EDIT) {
//          go back to view / read-only
            setupMenus(PROD_EDIT_TO_PROD_VIEW);
            lockEditables(true);
            setProductState(PROD_EDIT_TO_PROD_VIEW);
            setTitle(getString(R.string.product_title_view_current));
            fabPhone.show();
        }
    }

    @Override
    public void onBackPressed() {

        if (!changesMade()) {
            navHomeOrBack(true);
        } else {
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            resetTextFieldsAfterEditCancel();
                            dialogClicked = true;
                            navHomeOrBack(true);
                        }
                    };
            dialogUnsavedChanges(discardButtonClickListener);
        }
        if (currentState == MAIN_TO_ADD_VIEW) {
            if (dialogClicked == false) {
                return;
            } else {
                super.onBackPressed();
                dialogClicked = false;
            }
        } else if (currentState == LIST_TO_PROD_VIEW) {
            super.onBackPressed();
        }
    }

    private void setupMenus(int forWhichScreen) {
        switch (forWhichScreen) {

            case MAIN_TO_ADD_VIEW:
                menuDelete.setVisible(false);
                menuEdit.setVisible(false);
                menuCancel.setVisible(true);
                menuAccept.setVisible(true);
                break;

            case LIST_TO_PROD_VIEW:
                menuDelete.setVisible(true);
                menuEdit.setVisible(true);
                menuCancel.setVisible(false);
                menuAccept.setVisible(false);
                break;

            case PROD_VIEW_TO_PROD_EDIT:
                menuDelete.setVisible(false);
                menuEdit.setVisible(false);
                menuCancel.setVisible(true);
                menuAccept.setVisible(true);
                break;

            case PROD_EDIT_TO_PROD_VIEW:
                menuDelete.setVisible(true);
                menuEdit.setVisible(true);
                menuCancel.setVisible(false);
                menuAccept.setVisible(false);
                break;

            default:
                Toast.makeText(ProductActivity.this, getString(R.string.err_intended_menu_state) + ":\n"
                        + "MAIN_TO_ADD_VIEW: " + productState[MAIN_TO_ADD_VIEW] + "\n"
                        + "LIST_TO_PROD+VIEW: " + productState[LIST_TO_PROD_VIEW] + "\n"
                        + "PROD_VIEW_TO_PROD_EDIT: " + productState[PROD_VIEW_TO_PROD_EDIT] + "\n"
                        + "PROD_EDIT_TO_PROD_VIEW: " + productState[PROD_EDIT_TO_PROD_VIEW] + "\n", Toast.LENGTH_LONG).show();
                break;
        }
    }

    public static void setProductState(int fromTo) {

        currentState = fromTo;

        switch (fromTo) {
            case MAIN_TO_ADD_VIEW:
                productState[MAIN_TO_ADD_VIEW] = true;
                productState[LIST_TO_PROD_VIEW] = false;
                productState[PROD_VIEW_TO_PROD_EDIT] = false;
                productState[PROD_EDIT_TO_PROD_VIEW] = false;
                break;

            case LIST_TO_PROD_VIEW:
                productState[MAIN_TO_ADD_VIEW] = false;
                productState[LIST_TO_PROD_VIEW] = true;
                productState[PROD_VIEW_TO_PROD_EDIT] = false;
                productState[PROD_EDIT_TO_PROD_VIEW] = false;
                break;

            case PROD_VIEW_TO_PROD_EDIT:
                productState[MAIN_TO_ADD_VIEW] = false;
                productState[LIST_TO_PROD_VIEW] = false;
                productState[PROD_VIEW_TO_PROD_EDIT] = true;
                productState[PROD_EDIT_TO_PROD_VIEW] = false;
                break;

            case PROD_EDIT_TO_PROD_VIEW:
                productState[MAIN_TO_ADD_VIEW] = false;
                productState[LIST_TO_PROD_VIEW] = false;
                productState[PROD_VIEW_TO_PROD_EDIT] = false;
                productState[PROD_EDIT_TO_PROD_VIEW] = true;
                break;
        }
    }

    private void resetProductState() {
        for (int i = MAIN_TO_ADD_VIEW; i < NUM_OF_STATES; i++) {
            productState[i] = false;
        }
    }

    private boolean lockEditables(boolean toLock) {
        if (toLock) {

            etProductName.setEnabled(false);
            etPrice.setEnabled(false);
            fabMinus.setEnabled(false);
            fabMinus.hide();
            etQty.setEnabled(false);
            fabPlus.setEnabled(false);
            fabPlus.hide();
            etSupplierName.setEnabled(false);
            etSupplierPhone.setEnabled(false);

        } else {

            etProductName.setEnabled(true);
            etPrice.setEnabled(true);
            fabMinus.setEnabled(true);
            fabMinus.show();
            etQty.setEnabled(true);
            fabPlus.setEnabled(true);
            fabPlus.show();
            etSupplierName.setEnabled(true);
            etSupplierPhone.setEnabled(true);

        }

        return toLock;
    }

    private void deleteInventoryRow() {
        if (currentInventoryURI != null) {
//          uri will contain information for row to be deleted
            int rowsDeleted = getContentResolver().delete(currentInventoryURI, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.err_product_deleting) + " " + currentInventoryURI, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_item_deleted), Toast.LENGTH_SHORT).show();
            }
        }
//        todo: keep this...? was used calling from dialog on previous app
        finish();
    }

    private void getCurrentTextFields() {
        currentETProductName = etProductName.getText().toString().trim();
        currentETProductPrice = etPrice.getText().toString().trim();
        currentETProductQty = etQty.getText().toString().trim();
        currentETSupplierName = etSupplierName.getText().toString().trim();
        currentETSupplierPhone = etSupplierPhone.getText().toString().trim();
    }

    private void resetTextFieldsAfterEditCancel() {
        etProductName.setText(currentDBProductName);
        etPrice.setText(String.valueOf(currentDBProductPrice));
        etQty.setText(String.valueOf(currentDBProductQty));
        etSupplierName.setText(currentDBSupplierName);
        etSupplierPhone.setText(currentDBSupplierPhone);
    }

    private boolean allFieldsEmpty() {
        return TextUtils.isEmpty(currentETProductName) && TextUtils.isEmpty(currentETProductPrice) && TextUtils.isEmpty(currentETProductQty) && TextUtils.isEmpty(currentETSupplierName) && TextUtils.isEmpty(currentETSupplierPhone);
    }

    private boolean anyFieldEmpty() {
        return TextUtils.isEmpty(currentETProductName) || TextUtils.isEmpty(currentETProductPrice) || TextUtils.isEmpty(currentETProductQty) || TextUtils.isEmpty(currentETSupplierName) || TextUtils.isEmpty(currentETSupplierPhone);
    }

    private boolean changesMade() {

        getCurrentTextFields();

        if (currentState != MAIN_TO_ADD_VIEW) {
            boolean theSame = TextUtils.equals(currentDBProductName, currentETProductName)
                    && currentDBProductPrice == Integer.valueOf(currentETProductPrice)
                    && currentDBProductQty == Integer.valueOf(currentETProductQty)
                    && TextUtils.equals(currentDBSupplierName, currentETSupplierName)
                    && TextUtils.equals(currentDBSupplierPhone, currentETSupplierPhone);
            return !theSame;

        } else {
            boolean isEmpty = TextUtils.equals("", currentETProductName)
                    && TextUtils.equals("", currentETProductPrice)
                    && TextUtils.equals("", currentETProductQty)
                    && TextUtils.equals("", currentETSupplierName)
                    && TextUtils.equals("", currentETSupplierPhone);
            return !isEmpty;
        }

    }

    private void dialogUnsavedChanges(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_unsaved_changes_q);
        builder.setPositiveButton(R.string.dialog_unsaved_discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.dialog_unsaved_keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "keep editing" button, so dismiss the dialog
                // and continue editing the product
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private int saveInventory() {

//      error checking for all this:
//      found that keyboard prevents putting in negatives (qty/price), as well as non-integer-text; so correct data isn't an issue.
//      empty fields have already been taken care of. no real reason to validate strings for product name, supplier name
//
        if (currentInventoryURI == null && currentState == MAIN_TO_ADD_VIEW) {
            saveInventoryRowValues = addNewRow(new ContentValues());
//          add new item to db
            Uri insertURI = getContentResolver().insert(InventoryEntry.CONTENT_URI, saveInventoryRowValues);
            if (insertURI == null) {
                Toast.makeText(this, getString(R.string.err_product_save_error_saving), Toast.LENGTH_SHORT).show();
                return SAVE_RETURN_FAIL;
            } else {
                Toast.makeText(this, getString(R.string.product_save_insert_success), Toast.LENGTH_SHORT).show();
                return SAVE_RETURN_SUCCESS;
            }
        } else if (currentInventoryURI != null && currentState == PROD_VIEW_TO_PROD_EDIT) {
//          update item in db
            saveInventoryRowValues = addNewRow(new ContentValues());
            int rowsToUpdate = getContentResolver().update(currentInventoryURI, saveInventoryRowValues, null, null);
            if (rowsToUpdate == 0) {
                Toast.makeText(this, getString(R.string.err_product_save_updating), Toast.LENGTH_SHORT).show();
                return SAVE_RETURN_FAIL;
            } else {
                Toast.makeText(this, getString(R.string.product_save_update_success), Toast.LENGTH_SHORT).show();
                return SAVE_RETURN_SUCCESS;
            }
        }
        Toast.makeText(this, "unknown error", Toast.LENGTH_SHORT).show();
        return SAVE_RETURN_FAIL;
    }

    public ContentValues addNewRow(ContentValues rowData) {

        rowData.put(InventoryEntry.COL_PRODUCT_NAME, currentETProductName);
        rowData.put(InventoryEntry.COL_PRICE, currentETProductPrice);
        rowData.put(InventoryEntry.COL_QTY, currentETProductQty);
        rowData.put(InventoryEntry.COL_SUPPLIER_NAME, currentETSupplierName);
        rowData.put(InventoryEntry.COL_SUPPLIER_PHONE, currentETSupplierPhone);

        return rowData;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                currentInventoryURI,
                MainActivity.INVENTORY_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor rowData) {
        if (rowData == null || rowData.getCount() < 1) {
            return;
        }

        if (rowData.moveToFirst()) {
            int indexProductName = rowData.getColumnIndex(InventoryEntry.COL_PRODUCT_NAME);
            int indexProductPrice = rowData.getColumnIndex(InventoryEntry.COL_PRICE);
            int indexProductQty = rowData.getColumnIndex(InventoryEntry.COL_QTY);
            int indexSupplierName = rowData.getColumnIndex(InventoryEntry.COL_SUPPLIER_NAME);
            int indexSupplierPhone = rowData.getColumnIndex(InventoryEntry.COL_SUPPLIER_PHONE);

            currentDBProductName = rowData.getString(indexProductName);
            currentDBProductPrice = rowData.getInt(indexProductPrice);
            currentDBProductQty = rowData.getInt(indexProductQty);
            currentDBSupplierName = rowData.getString(indexSupplierName);
            currentDBSupplierPhone = rowData.getString(indexSupplierPhone);

            etProductName.setText(currentDBProductName);
            etPrice.setText(String.valueOf(currentDBProductPrice));
            etQty.setText(String.valueOf(currentDBProductQty));
            etSupplierName.setText(currentDBSupplierName);
            etSupplierPhone.setText(currentDBSupplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        etProductName.setText("");
        etPrice.setText("0");
        etQty.setText("0");
        etSupplierName.setText("");
        etSupplierPhone.setText("");
    }
}