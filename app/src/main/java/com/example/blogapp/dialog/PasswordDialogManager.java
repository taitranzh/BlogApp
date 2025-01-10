package com.example.blogapp.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.example.blogapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class PasswordDialogManager {

    private AlertDialog dialog;
    private TextInputLayout layoutEditPassword;
    private TextInputEditText inputPassword;
    public interface PasswordListener {
        void onPasswordConfirmed(String password);
    }

    public void showPasswordDialog(Context context, PasswordListener listener) {
        if (dialog != null && dialog.isShowing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_password, null);
        builder.setView(view);

        layoutEditPassword = view.findViewById(R.id.layout_edit_pass);
        inputPassword = view.findViewById(R.id.edit_pass);
        Button btnConfirm = view.findViewById(R.id.btn_confirm_pass);
        Button btnClose = view.findViewById(R.id.btn_cancel);

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        btnConfirm.setOnClickListener(v -> {
            String password = inputPassword.getText().toString().trim();
            if (password.isEmpty()) {
                layoutEditPassword.setError("Vui lòng nhập mật khẩu.");
            } else {
                layoutEditPassword.setError(null);
                listener.onPasswordConfirmed(password);
            }
        });

        btnClose.setOnClickListener(v -> dismissDialog());
    }

    public void setError(String errorMessage) {
        if (layoutEditPassword != null) {
            layoutEditPassword.setError(errorMessage);
            inputPassword.requestFocus();
        }
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isDialogShowing() {
        return dialog != null && dialog.isShowing();
    }
}
