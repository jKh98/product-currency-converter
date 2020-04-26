package com.jkhurfan.product_currency_converter.util;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

public class Utils {
    public static boolean validateField(EditText editText) {
        if (editText.getText() == null || TextUtils.isEmpty(editText.getText().toString())) {
            if (editText.getParent() instanceof TextInputLayout) {
                ((TextInputLayout) editText.getParent()).setError("This field is required.");
            } else {
                editText.setError("This field is required.");
            }
            return false;
        }
        return true;
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}
