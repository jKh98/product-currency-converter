package com.jkhurfan.product_currency_converter.Activity;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.jkhurfan.product_currency_converter.DB.DatabaseHelper;
import com.jkhurfan.product_currency_converter.DB.Product;
import com.jkhurfan.product_currency_converter.R;

import java.util.List;

import info.androidhive.barcode.BarcodeReader;

public class MainActivity extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener, View.OnClickListener {

    BarcodeReader barcodeReader;
    TextView barcodeText;
    Button getPriceBtn, addProductBtn;
    DatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        barcodeText = findViewById(R.id.barcode);
        getPriceBtn = findViewById(R.id.get_price_button);
        addProductBtn = findViewById(R.id.add_new_product_button);
        getPriceBtn.setOnClickListener(this);
        addProductBtn.setOnClickListener(this);

        helper = new DatabaseHelper();

        // get the barcode reader instance
        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_scanner);
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

        final TextView name = findViewById(R.id.name);
        final EditText costUSD = findViewById(R.id.cost_usd);
        final EditText costLBP = findViewById(R.id.cost_lbp);
        final EditText rateUSD = findViewById(R.id.usd_rate);
        final EditText profit = findViewById(R.id.profit_rate);
        final TextView priceLBP = findViewById(R.id.selling_price_lbp);

        helper.getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Product product = dataSnapshot.child(s).getValue(Product.class);
                if (product != null) {
                    name.setText(product.getName());
                    costUSD.setText(String.valueOf(product.getCost()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("getProduct", "The read failed: " + databaseError.getCode());
                findViewById(R.id.dialog_layout).setVisibility(View.GONE);
            }
        });

        rateUSD.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    double rateUSDValue = Double.parseDouble(editable.toString());
                    double costUSDValue = Double.parseDouble(costUSD.getText().toString());
                    double profitRateValue = Double.parseDouble(profit.getText().toString());
                    costLBP.setText(String.valueOf(rateUSDValue * costUSDValue));
                    priceLBP.setText(String.valueOf(rateUSDValue * costUSDValue * (100 + profitRateValue) / 100));
                } catch (Exception ignored) {
                }
            }
        });

        profit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    double rateUSDValue = Double.parseDouble(editable.toString());
                    double costUSDValue = Double.parseDouble(costUSD.getText().toString());
                    double profitRateValue = Double.parseDouble(profit.getText().toString());
                    costLBP.setText(String.valueOf(rateUSDValue * costUSDValue));
                    priceLBP.setText(String.valueOf(rateUSDValue * costUSDValue * (100 + profitRateValue) / 100));
                } catch (Exception ignored) {
                }
            }
        });

        dialog.show();
    }

    private void openNewProductDialog(String s) {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setTitle("Price Details");
        dialog.setContentView(R.layout.new_product_dialog);

        dialog.show();
    }

}
