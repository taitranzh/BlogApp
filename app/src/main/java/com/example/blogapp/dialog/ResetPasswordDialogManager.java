package com.example.blogapp.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.blogapp.R;
import com.example.blogapp.api.ApiService;
import com.example.blogapp.api.RetrofitClient;
import com.example.blogapp.entities.request.ChangePassRequest;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordDialogManager {
    private AlertDialog dialog;

    public void showResetPasswordDialog(Context context, String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_change_pass, null);
        builder.setView(view);

        TextInputEditText editPass = view.findViewById(R.id.edit_pass);
        TextInputEditText editCfPass = view.findViewById(R.id.edit_cfpass);
        TextInputLayout layoutEditPass = view.findViewById(R.id.layout_edit_pass);
        TextInputLayout layoutEditCfPass = view.findViewById(R.id.layout_edit_cfpass);
        Button btnConfirm = view.findViewById(R.id.btn_confirm_pass);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        dialog = builder.create();
        dialog.show();

        btnConfirm.setOnClickListener(v -> {
            String newPassword = editPass.getText().toString().trim();
            String confirmPassword = editCfPass.getText().toString().trim();

            if (newPassword.isEmpty()) {
                layoutEditPass.setError("Vui lòng nhập mật khẩu mới");
                return;
            } else if (!newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")) {
                layoutEditPass.setError("Mật khẩu phải bao gồm cả chữ và số.");
                return;
            } else {
                layoutEditPass.setError(null);
            }

            if (confirmPassword.isEmpty()) {
                layoutEditCfPass.setError("Vui lòng nhập lại mật khẩu");
                return;
            } else if (!confirmPassword.equals(newPassword)) {
                layoutEditCfPass.setError("Mật khẩu không khớp");
                return;
            } else {
                layoutEditCfPass.setError(null);
            }

            resetPassword(context, email, newPassword, confirmPassword);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void resetPassword(Context context, String email, String newPassword, String confirmPassword) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        ChangePassRequest request = new ChangePassRequest(newPassword, confirmPassword);

        apiService.resetPassword(email, request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Đặt lại mật khẩu thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
