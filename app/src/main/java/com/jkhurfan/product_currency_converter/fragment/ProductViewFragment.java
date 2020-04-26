package com.jkhurfan.product_currency_converter.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.jkhurfan.product_currency_converter.R;
import com.jkhurfan.product_currency_converter.activity.MainActivity;
import com.jkhurfan.product_currency_converter.model.Product;
import com.jkhurfan.product_currency_converter.util.Utils;

public class ProductViewFragment extends Fragment implements View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_EXCHANGE_RATE = "exchange_rate";
    private static final String ARG_BARCODE = "barcode";
    private boolean editingPrice = false, editingCostUSD = false, editingCostLBP = false, editingProfit = false;
    private EditText barcode;
    private EditText name;
    private EditText description;
    private EditText costUSD;
    private EditText costLBP;
    private EditText profit;
    private TextView priceLBP;
    private ProgressBar progressBar;
    private DatabaseReference databaseReference;


    private Double exchangeRate;
    private String barcodeValue;

    private OnFragmentInteractionListener mListener;

    public ProductViewFragment() {
        // Required empty public constructor
    }

    public static ProductViewFragment newInstance(double param1, String param2) {
        ProductViewFragment fragment = new ProductViewFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_EXCHANGE_RATE, param1);
        args.putString(ARG_BARCODE, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exchangeRate = getArguments().getDouble(ARG_EXCHANGE_RATE);
            barcodeValue = getArguments().getString(ARG_BARCODE);
        }
        if (getActivity() != null) {
            databaseReference = ((MainActivity) getActivity()).getDatabaseInstance();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product_view, container, false);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.enableBarcodeFragment();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.save) {
            progressBar.setVisibility(View.VISIBLE);
            if (validateProductForm()) {
                Product product = new Product(
                        barcodeValue,
                        name.getText().toString(),
                        description.getText().toString(),
                        Double.parseDouble(costUSD.getText().toString()),
                        Double.parseDouble(profit.getText().toString())
                );
                databaseReference.child("products").child(product.getBarcode()).setValue(product).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Product successfully updated!", Toast.LENGTH_SHORT).show();
                        if (getActivity() != null) {
                            getActivity().getSupportFragmentManager().popBackStackImmediate();
                        }
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: Could not save product data!", Toast.LENGTH_SHORT).show();
            }

        } else if (view.getId() == R.id.cancel) {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        }
    }

    private boolean validateProductForm() {
        return Utils.validateField(barcode) && Utils.validateField(name) && Utils.validateField(costUSD) && Utils.validateField(profit);
    }

    public interface OnFragmentInteractionListener {
        void enableBarcodeFragment();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = view.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        databaseReference.child("products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Product product = dataSnapshot.child(barcodeValue).getValue(Product.class);
                barcode = view.findViewById(R.id.barcode);
                name = view.findViewById(R.id.name);
                description = view.findViewById(R.id.description);
                costUSD = view.findViewById(R.id.cost_usd);
                costLBP = view.findViewById(R.id.cost_lbp);
                profit = view.findViewById(R.id.profit_rate);
                priceLBP = view.findViewById(R.id.selling_price_lbp);

                barcode.setText(barcodeValue);


                if (product != null) {
                    name.setText(product.getName());
                    description.setText(product.getDescription());
                    costUSD.setText(String.valueOf(product.getCost()));
                    profit.setText(String.valueOf(product.getProfit()));
                    costLBP.setText(String.valueOf(exchangeRate * product.getCost()));
                    priceLBP.setText(String.valueOf(Math.round(exchangeRate * product.getCost() * ((100 + product.getProfit()) / 100))));
                }

                setUpTextWatchers();

                Button saveBtn = view.findViewById(R.id.save);
                saveBtn.setOnClickListener(ProductViewFragment.this);
                Button cancelBtn = view.findViewById(R.id.cancel);
                cancelBtn.setOnClickListener(ProductViewFragment.this);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("getProduct", "The read failed: " + databaseError.getCode());
                view.findViewById(R.id.dialog_layout).setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Could not retrieve product.", Toast.LENGTH_SHORT).show();


            }
        });

    }

    private void setUpTextWatchers() {
        costUSD.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editingCostLBP && !editingProfit && !editingPrice) {
                    editingCostUSD = true;
                    if (editable.length() > 0) {
                        try {
                            costLBP.setText(String.valueOf(Double.parseDouble(editable.toString()) * exchangeRate));
                            double profitValue = Double.parseDouble(profit.getText().toString());
                            priceLBP.setText(String.valueOf(Double.parseDouble(editable.toString()) * exchangeRate * (100 + profitValue) / 100));
                        } catch (Exception ignored) {
                        }
                    }


                    editingCostUSD = false;
                }
            }
        });
        costLBP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editingProfit && !editingCostUSD && !editingPrice) {
                    editingCostLBP = true;
                    if (editable.length() > 0) {
                        try {
                            costUSD.setText(String.valueOf(Double.parseDouble(editable.toString()) / exchangeRate));
                            double profitValue = Double.parseDouble(profit.getText().toString());
                            priceLBP.setText(String.valueOf(Double.parseDouble(editable.toString()) * (100 + profitValue) / 100));
                        } catch (Exception ignored) {
                        }

                    }
                    editingCostLBP = false;
                }
            }
        });
        priceLBP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editingProfit && !editingCostUSD && !editingCostLBP) {
                    editingCostLBP = true;
                    if (editable.length() > 0) {
                        try {
                            double costLBPValue = Double.parseDouble(costLBP.getText().toString());
                            profit.setText(String.valueOf(Double.parseDouble(editable.toString()) / costLBPValue));
                        } catch (Exception ignored) {

                        }

                    }
                    editingCostLBP = false;
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
                if (!editingCostLBP && !editingCostUSD && !editingPrice) {
                    editingProfit = true;
                    if (editable.length() > 0) {
                        try {
                            double costLBPValue = Double.parseDouble(costLBP.getText().toString());
                            priceLBP.setText(String.valueOf((100 + Double.parseDouble(editable.toString())) * costLBPValue / 100));
                        } catch (Exception ignored) {

                        }
                    }
                    editingProfit = false;
                }
            }
        });
    }
}
