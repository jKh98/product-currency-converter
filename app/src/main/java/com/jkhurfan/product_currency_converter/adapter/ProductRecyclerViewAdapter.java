package com.jkhurfan.product_currency_converter.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.jkhurfan.product_currency_converter.R;
import com.jkhurfan.product_currency_converter.fragment.ProductListFragment.OnListFragmentInteractionListener;
import com.jkhurfan.product_currency_converter.model.Product;

import java.util.List;

public class ProductRecyclerViewAdapter extends RecyclerView.Adapter<ProductRecyclerViewAdapter.ViewHolder> {

    private final List<Product> mValues;
    private double exchangeRate;
    private final OnListFragmentInteractionListener mListener;

    public ProductRecyclerViewAdapter(List<Product> products, double exchangeRate, OnListFragmentInteractionListener listener) {
        mValues = products;
        mListener = listener;
        this.exchangeRate = exchangeRate;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mNameView.setText(mValues.get(position).getName());
        holder.mDescriptionView.setText(mValues.get(position).getDescription());
        holder.mCostView.setText(String.valueOf(Math.round(mValues.get(position).getCost() * exchangeRate)));
        holder.mPriceView.setText(String.valueOf(Math.round(mValues.get(position).getCost() * exchangeRate * (100 + mValues.get(position).getProfit()) / 100)));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mNameView;
        final TextView mDescriptionView;
        final TextView mCostView;
        final TextView mPriceView;
        Product mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = view.findViewById(R.id.product_name);
            mDescriptionView = view.findViewById(R.id.product_description);
            mCostView = view.findViewById(R.id.product_cost);
            mPriceView = view.findViewById(R.id.product_price);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mCostView.getText() + "'";
        }
    }
}
