package com.jkhurfan.product_currency_converter.Fragment;

import android.content.Context;
import android.net.Uri;
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
import com.jkhurfan.product_currency_converter.Activity.MainActivity;
import com.jkhurfan.product_currency_converter.DB.Product;
import com.jkhurfan.product_currency_converter.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProductViewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProductViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProductViewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_EXCHANGE_RATE = "exchange_rate";
    private static final String ARG_BARCODE = "barcode";
    boolean editingPrice = false, editingCostUSD = false, editingCostLBP = false, editingProfit = false;
    ProgressBar progressBar;


    // TODO: Rename and change types of parameters
    private Double exchangeRate;
    private String barcodeValue;

    private OnFragmentInteractionListener mListener;

    public ProductViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product_view, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null) {
            final DatabaseReference databaseReference = ((MainActivity) getActivity()).getDatabaseInstance();
            progressBar = view.findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
            databaseReference.child("products").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Product product = dataSnapshot.child(barcodeValue).getValue(Product.class);
                    final double exchangeRateValue = exchangeRate;
                    TextView barcode = view.findViewById(R.id.barcode);
                    final TextView name = view.findViewById(R.id.name);
                    final EditText description = view.findViewById(R.id.description);
                    final EditText costUSD = view.findViewById(R.id.cost_usd);
                    final EditText costLBP = view.findViewById(R.id.cost_lbp);
                    final EditText profit = view.findViewById(R.id.profit_rate);
                    final TextView priceLBP = view.findViewById(R.id.selling_price_lbp);

                    barcode.setText(barcodeValue);


                    if (product != null) {
                        name.setText(product.getName());
                        description.setText(product.getDescription());
                        costUSD.setText(String.valueOf(product.getCost()));
                        profit.setText(String.valueOf(product.getProfit()));
                        costLBP.setText(String.valueOf(exchangeRateValue * product.getCost()));
                        priceLBP.setText(String.valueOf(Math.round(exchangeRateValue * product.getCost() * ((100 + product.getProfit()) / 100))));
                    }

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

                                    double costLBPValue = Double.valueOf(costLBP.getText().toString());
                                    priceLBP.setText(String.valueOf(Double.parseDouble(editable.toString()) * costLBPValue));
                                }
                                editingProfit = false;
                            }
                        }
                    });
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
                                    costLBP.setText(String.valueOf(Double.parseDouble(editable.toString()) * exchangeRateValue));
                                    try {

                                        double profitValue = Double.valueOf(profit.getText().toString());
                                        priceLBP.setText(String.valueOf(Double.parseDouble(editable.toString()) * exchangeRateValue * profitValue));
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
                                    costUSD.setText(String.valueOf(Double.parseDouble(editable.toString()) / exchangeRateValue));
                                    try {
                                        double profitValue = Double.valueOf(profit.getText().toString());
                                        priceLBP.setText(String.valueOf(Double.parseDouble(editable.toString()) * profitValue));
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
                            if (!editingProfit && !editingCostUSD && !editingPrice) {
                                editingCostLBP = true;
                                if (editable.length() > 0) {
                                    try {
                                        double costLBPValue = Double.valueOf(costLBP.getText().toString());
                                        profit.setText(String.valueOf(Double.parseDouble(editable.toString()) / costLBPValue));
                                    } catch (Exception ignored) {

                                    }

                                }
                                editingCostLBP = false;
                            }
                        }
                    });


                    Button saveBtn = view.findViewById(R.id.save);
                    saveBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                if (name.getText() != null && description.getText() != null && costUSD.getText() != null && profit.getText() != null) {
                                    Product product = new Product(
                                            barcodeValue,
                                            name.getText().toString(),
                                            description.getText().toString(),
                                            Double.parseDouble(costUSD.getText().toString()),
                                            Double.parseDouble(profit.getText().toString())
                                    );

                                    progressBar.setVisibility(View.VISIBLE);
                                    databaseReference.child("products").child(product.getBarcode()).setValue(product).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(getContext(), "Product successfully updated", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    });

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
    }
}
