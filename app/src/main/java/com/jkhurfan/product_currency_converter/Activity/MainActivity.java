package com.jkhurfan.product_currency_converter.Activity;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cursoradapter.widget.CursorAdapter;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jkhurfan.product_currency_converter.DB.Product;
import com.jkhurfan.product_currency_converter.Fragment.ProductViewFragment;
import com.jkhurfan.product_currency_converter.R;

import java.util.ArrayList;
import java.util.List;

import info.androidhive.barcode.BarcodeReader;

public class MainActivity extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener, View.OnClickListener {

    BarcodeReader barcodeReader;
    TextView barcodeText;
    TextView exchangeRate;
    Button addProductBtn;
    Button saveRate;
    ProgressBar progressBar;

    private SearchView searchView;
    private CursorAdapter mSuggestionAdapter;
    private MatrixCursor mSearchCursor;
    private ArrayList<Product> mSearchableList = new ArrayList<>();

    public DatabaseReference getDatabaseInstance() {
        return databaseInstance;
    }

    private DatabaseReference databaseInstance;

    boolean editingPrice = false, editingCostUSD = false, editingCostLBP = false, editingProfit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        barcodeText = findViewById(R.id.barcode);
        addProductBtn = findViewById(R.id.add_new_product_button);
        searchView = findViewById(R.id.main_search_view);
        exchangeRate = findViewById(R.id.exchange_rate);
        saveRate = findViewById(R.id.save_currency);
        progressBar = findViewById(R.id.progress_bar);

        barcodeText.setText(" ");
        addProductBtn.setOnClickListener(this);
        saveRate.setOnClickListener(this);

        databaseInstance = FirebaseDatabase.getInstance().getReference();
        // get the barcode reader instance
        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_scanner);
        retrieveRate();
        retrieveDBData();
    }

    private void retrieveRate() {
        progressBar.setVisibility(View.VISIBLE);
        databaseInstance.child("rate").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                exchangeRate.setText(String.valueOf(dataSnapshot.getValue()));
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Could not retrieve exchange rate", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void retrieveDBData() {
        progressBar.setVisibility(View.VISIBLE);
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
                                openProductViewFragment(((Product) obj).getBarcode());
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
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Could not retrieve all products", Toast.LENGTH_SHORT).show();
            }
        };
        databaseInstance.child("products").addListenerForSingleValueEvent(eventListener);
    }

    @Override
    public void onScanned(Barcode barcode) {
        barcodeReader.playBeep();
        barcodeText.setText(barcode.displayValue);
        openProductViewFragment(barcode.displayValue);

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

        CharSequence barcodeCS = barcodeText.getText();
        if (view.getId() == R.id.add_new_product_button) {
            if (barcodeCS != null && !barcodeCS.toString().equals("")) {
                openProductViewFragment(barcodeCS.toString());

            }
        } else if (view.getId() == R.id.save_currency) {
            if (exchangeRate.getText() != null && !exchangeRate.getText().toString().equals(" "))
                progressBar.setVisibility(View.VISIBLE);
            databaseInstance.child("rate").setValue(Double.parseDouble(exchangeRate.getText().toString())).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Currency successfully changed.", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    private void openProductViewFragment(String barcode) {
        getSupportFragmentManager().popBackStackImmediate();
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("ProductViewFragment")
                .add(R.id.content, ProductViewFragment.newInstance(Double.parseDouble(exchangeRate.getText().toString()), barcode))
                .commit();
    }
}
