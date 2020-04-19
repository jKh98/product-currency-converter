package com.jkhurfan.product_currency_converter.Activity;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cursoradapter.widget.CursorAdapter;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.jkhurfan.product_currency_converter.DB.DatabaseHelper;
import com.jkhurfan.product_currency_converter.DB.Product;
import com.jkhurfan.product_currency_converter.R;

import java.util.ArrayList;
import java.util.List;

import info.androidhive.barcode.BarcodeReader;

public class MainActivity extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener, View.OnClickListener {

    BarcodeReader barcodeReader;
    TextView barcodeText;
    Button getPriceBtn, addProductBtn;
    DatabaseHelper helper;

    private SearchView searchView;
    private CursorAdapter mSuggestionAdapter;
    private MatrixCursor mSearchCursor;
    private ArrayList<Product> mSearchableList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        barcodeText = findViewById(R.id.barcode);
        getPriceBtn = findViewById(R.id.get_price_button);
        addProductBtn = findViewById(R.id.add_new_product_button);
        searchView = findViewById(R.id.main_search_view);

        barcodeText.setText(" ");
        getPriceBtn.setOnClickListener(this);
        addProductBtn.setOnClickListener(this);
        helper = new DatabaseHelper();
        // get the barcode reader instance
        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_scanner);
        initSearchView();
    }

    private void initSearchView() {

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Product> array = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    Product product = ds.getValue(Product.class);
                    array.add(product);

                }

                mSearchableList.addAll(array);

                mSearchCursor = new MatrixCursor(new String[]{"_id", "name", "description"});
                mSuggestionAdapter = new CursorAdapter(MainActivity.this, mSearchCursor, false) {
                    @Override
                    public View newView(Context context, Cursor cursor, ViewGroup parent) {
                        return LayoutInflater.from(context).inflate(R.layout.view_search, parent, false);
                    }

                    @Override
                    public void bindView(View view, Context context, Cursor cursor) {
                        ((TextView) view.findViewById(R.id.searchable_name)).setText(cursor.getString(1));
                    }
                };
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
//                    Utils.hideKeyboardFrom(searchView.getContext(), searchView);
                        searchView.clearFocus();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        mSearchCursor = new MatrixCursor(new String[]{"_id", "name", "description"});
                        for (int i = 0; i < mSearchableList.size(); i++) {
                            if (mSearchableList.get(i).getName().toLowerCase().contains(s.toLowerCase())) {
                                mSearchCursor.addRow(new String[]{String.valueOf(i + 1), String.valueOf(mSearchableList.get(i).getName()), String.valueOf(mSearchableList.get(i).getDescription())});
                            }
                        }
                        mSuggestionAdapter.swapCursor(mSearchCursor);
                        return false;
                    }
                });

                searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                    @Override
                    public boolean onSuggestionClick(int position) {
                        mSearchCursor.moveToPosition(position);
                        String name = mSearchCursor.getString(1);
                        for (Object obj : mSearchableList) {
                            if (((Product) obj).getName().contains(name)) {
                                openPriceDialog(((Product) obj).getBarcode());
                                break;
                            }
                        }
                        searchView.clearFocus();
                        return false;
                    }

                    @Override
                    public boolean onSuggestionSelect(int position) {
                        return true;
                    }
                });
                searchView.setSuggestionsAdapter(mSuggestionAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        helper.getReference().addListenerForSingleValueEvent(eventListener);
    }

    @Override
    public void onScanned(Barcode barcode) {
        barcodeReader.playBeep();
        barcodeText.setText(barcode.displayValue);
    }

    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {

    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    @Override
    public void onScanError(String errorMessage) {
        Toast.makeText(getApplicationContext(), "Error occurred while scanning " + errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraPermissionDenied() {
        finish();
    }

    @Override
    public void onClick(View view) {

        CharSequence barcodeString = barcodeText.getText();
        switch (view.getId()) {
            case R.id.get_price_button:
                if (barcodeString != null && !barcodeString.toString().equals("")) {
                    openPriceDialog(barcodeString.toString());
                }
                break;
            case R.id.add_new_product_button:
                if (barcodeString != null && !barcodeString.toString().equals("")) {
                    openNewProductDialog(barcodeString.toString());
                }
                break;
        }
    }

    private void openPriceDialog(final String s) {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setTitle("Price Details");
        dialog.setContentView(R.layout.price_details_dialog);

        final TextView name = dialog.findViewById(R.id.name);
        final EditText costUSD = dialog.findViewById(R.id.cost_usd);
        final EditText costLBP = dialog.findViewById(R.id.cost_lbp);
        final EditText rateUSD = dialog.findViewById(R.id.usd_rate);
        final EditText profit = dialog.findViewById(R.id.profit_rate);
        final TextView priceLBP = dialog.findViewById(R.id.selling_price_lbp);

        helper.getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Product product = dataSnapshot.child(s).getValue(Product.class);
                if (product != null) {
                    name.setText(product.getName());
                    costUSD.setText(String.valueOf(product.getCost()));
                }
                else{
                    dialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("getProduct", "The read failed: " + databaseError.getCode());
                findViewById(R.id.dialog_layout).setVisibility(View.GONE);
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    double rateUSDValue = Double.parseDouble(rateUSD.getText().toString());
                    double costUSDValue = Double.parseDouble(costUSD.getText().toString());
                    double profitRateValue = Double.parseDouble(profit.getText().toString());
                    costLBP.setText(String.valueOf(rateUSDValue * costUSDValue));
                    priceLBP.setText(String.valueOf(Math.round(rateUSDValue * costUSDValue * ((100 + profitRateValue) / 100))));
                } catch (Exception ignored) {
                }

            }
        };

        rateUSD.addTextChangedListener(textWatcher);
        profit.addTextChangedListener(textWatcher);

        Button okBtn = dialog.findViewById(R.id.ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        adjustDialogWidth(dialog);

    }

    private void openNewProductDialog(final String s) {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setTitle("Price Details");
        dialog.setContentView(R.layout.new_product_dialog);

        final TextView barcode = dialog.findViewById(R.id.barcode);
        final TextView name = dialog.findViewById(R.id.name);
        final EditText description = dialog.findViewById(R.id.description);
        final EditText costUSD = dialog.findViewById(R.id.cost_usd);

        barcode.setText(s);
        helper.getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Product product = dataSnapshot.child(s).getValue(Product.class);
                if (product != null) {
                    name.setText(product.getName());
                    description.setText(product.getDescription());
                    costUSD.setText(String.valueOf(product.getCost()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("getProduct", "The read failed: " + databaseError.getCode());
                findViewById(R.id.dialog_layout).setVisibility(View.GONE);
            }
        });


        Button saveBtn = dialog.findViewById(R.id.save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (name.getText() != null && description.getText() != null && costUSD.getText() != null) {
                        Product product = new Product(
                                s,
                                name.getText().toString(),
                                description.getText().toString(),
                                Double.parseDouble(costUSD.getText().toString())
                        );
                        helper.addNewProduct(product);
                        dialog.dismiss();
                    }
                } catch (Exception ignored) {
                }
            }
        });

        dialog.show();
        adjustDialogWidth(dialog);

    }

    private void adjustDialogWidth(Dialog dialog) {
//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//        dialog.getWindow().setLayout((6 * metrics.widthPixels) / 7, (4 * metrics.heightPixels) / 5);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }


}
