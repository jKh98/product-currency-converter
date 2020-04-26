package com.jkhurfan.product_currency_converter.activity;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cursoradapter.widget.CursorAdapter;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jkhurfan.product_currency_converter.R;
import com.jkhurfan.product_currency_converter.fragment.ProductListFragment;
import com.jkhurfan.product_currency_converter.fragment.ProductViewFragment;
import com.jkhurfan.product_currency_converter.model.Product;
import com.jkhurfan.product_currency_converter.util.Utils;

import java.util.ArrayList;
import java.util.List;

import info.androidhive.barcode.BarcodeReader;

public class MainActivity extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener, View.OnClickListener, ProductListFragment.OnListFragmentInteractionListener, ProductViewFragment.OnFragmentInteractionListener {

    BarcodeReader barcodeReader;
    EditText barcodeText;
    EditText exchangeRate;
    ImageButton addProductBtn;
    ImageButton productsListBtn;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        barcodeText = findViewById(R.id.barcode);
        addProductBtn = findViewById(R.id.add_new_product_button);
        searchView = findViewById(R.id.main_search_view);
        exchangeRate = findViewById(R.id.exchange_rate);
        saveRate = findViewById(R.id.save_currency);
        progressBar = findViewById(R.id.progress_bar);
        productsListBtn = findViewById(R.id.products_list_button);
        addProductBtn.setOnClickListener(this);
        saveRate.setOnClickListener(this);
        productsListBtn.setOnClickListener(this);

        databaseInstance = FirebaseDatabase.getInstance().getReference();
        // get the barcode reader instance
        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_scanner);
        retrieveRate();
        retrieveSearchViewData();
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

    private void retrieveSearchViewData() {
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

                mSearchCursor = new MatrixCursor(new String[]{"_id", "name", "description", "cost", "price"});
                mSuggestionAdapter = new CursorAdapter(MainActivity.this, mSearchCursor, false) {
                    @Override
                    public View newView(Context context, Cursor cursor, ViewGroup parent) {
                        return LayoutInflater.from(context).inflate(R.layout.fragment_product, parent, false);
                    }

                    @Override
                    public void bindView(View view, Context context, Cursor cursor) {
                        ((TextView) view.findViewById(R.id.product_name)).setText(cursor.getString(1));
                        ((TextView) view.findViewById(R.id.product_description)).setText(cursor.getString(2));
                        ((TextView) view.findViewById(R.id.product_cost)).setText(cursor.getString(3));
                        ((TextView) view.findViewById(R.id.product_price)).setText(cursor.getString(4));
                    }
                };
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        Utils.hideKeyboardFrom(searchView.getContext(), searchView);
                        searchView.clearFocus();
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        mSearchCursor = new MatrixCursor(new String[]{"_id", "name", "description", "cost", "price"});
                        for (int i = 0; i < mSearchableList.size(); i++) {
                            if (mSearchableList.get(i).getName().toLowerCase().contains(s.toLowerCase())) {
                                mSearchCursor.addRow(new String[]{String.valueOf(i + 1),
                                        mSearchableList.get(i).getName(),
                                        mSearchableList.get(i).getDescription(),
                                        String.valueOf(mSearchableList.get(i).getCost() * getExchangeRate()),
                                        String.valueOf(mSearchableList.get(i).getCost() * (100 + mSearchableList.get(i).getProfit()) / 100 * getExchangeRate())});
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
        openProductViewFragment(barcode.displayValue);
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

        if (view.getId() == R.id.add_new_product_button) {
            if (Utils.validateField(barcodeText)) {
                openProductViewFragment(barcodeText.getText().toString());

            }
        } else if (view.getId() == R.id.save_currency) {
            if (Utils.validateField(exchangeRate))
                progressBar.setVisibility(View.VISIBLE);
            databaseInstance.child("rate").setValue(getExchangeRate()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Currency successfully changed.", Toast.LENGTH_SHORT).show();

                }
            });
        } else if (view.getId() == R.id.products_list_button) {
            openProductsListFragment();
        }
    }

    private void openProductsListFragment() {
        progressBar.setVisibility(View.VISIBLE);
        barcodeReader.pauseScanning();
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Product> array = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    Product product = ds.getValue(Product.class);
                    array.add(product);

                }
//                getSupportFragmentManager().popBackStackImmediate();
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack("ProductViewFragment")
                        .add(R.id.content, ProductListFragment.newInstance(1, array, getExchangeRate()))
                        .commit();
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

    private void openProductViewFragment(String barcode) {
        barcodeReader.pauseScanning();
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("ProductViewFragment")
                .replace(R.id.content, ProductViewFragment.newInstance(getExchangeRate(), barcode))
                .commit();
    }

    @Override
    public void onListFragmentInteraction(Product item) {
        openProductViewFragment(item.getBarcode());

    }


    @Override
    public void enableBarcodeFragment() {
        barcodeReader.resumeScanning();
    }

    double getExchangeRate() {
        double rate = 0.0;
        try {
            rate = Double.parseDouble(exchangeRate.getText().toString());
        } catch (Exception ignored) {

        }
        return rate;
    }
}
