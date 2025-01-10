package com.example.blogapp.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.blogapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EmailDialogManager {

    private AlertDialog dialog;
    private TextInputLayout layoutEditEmail;
    private TextInputEditText inputEmail;
    private TextView tvTitle,tvDescription;
    public interface EmailListener {
        void onEmailConfirmed(String email);
    }

    public void showEmailDialog(Context context, Boolean isLock, EmailListener listener) {
        if (dialog != null && dialog.isShowing()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_email, null);
        builder.setView(view);

        tvTitle = view.findViewById(R.id.tv_title);
        tvDescription = view.findViewById(R.id.tv_description);
        layoutEditEmail = view.findViewById(R.id.layout_edit_email);
        inputEmail = view.findViewById(R.id.edit_email);
        Button btnConfirm = view.findViewById(R.id.btn_confirm_email);
        Button btnClose = view.findViewById(R.id.btn_cancel);

        if(isLock){
            tvTitle.setText("Tài khoản của bạn đã bị vô hiệu hóa");
            tvDescription.setText("Vui lòng nhập email để có thể mở khóa tài khoản");
        }else {
            tvTitle.setText("Nhập email");
            tvDescription.setText("Vui lòng nhập email dùng để đăng nhập vào ứng dụng");
        }

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        btnConfirm.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            if (email.isEmpty()) {
                layoutEditEmail.setError("Vui lòng nhập email.");
            } else {
                layoutEditEmail.setError(null);
                listener.onEmailConfirmed(email);
            }
        });

        btnClose.setOnClickListener(v -> dismissDialog());
    }

    public void setError(String errorMessage) {
        if (layoutEditEmail != null) {
            layoutEditEmail.setError(errorMessage);
            inputEmail.requestFocus();
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
