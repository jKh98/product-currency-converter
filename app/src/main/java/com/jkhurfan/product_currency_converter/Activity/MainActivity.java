package com.jkhurfan.product_currency_converter.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.jkhurfan.product_currency_converter.DatabaseHelper;
import com.jkhurfan.product_currency_converter.Product;
import com.jkhurfan.product_currency_converter.R;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        DatabaseHelper helper = new DatabaseHelper();
        final TextView text = findViewById(R.id.test);

        helper.getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                text.setText(dataSnapshot.child("bhlbk").getValue(Product.class).getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("getProduct","The read failed: " + databaseError.getCode());
            }
        });    }


}
