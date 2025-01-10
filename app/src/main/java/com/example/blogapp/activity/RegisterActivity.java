package com.example.blogapp.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.blogapp.R;
import com.example.blogapp.api.ApiService;
import com.example.blogapp.api.RetrofitClient;
import com.example.blogapp.entities.request.LoginRequest;
import com.example.blogapp.entities.request.OtpRequest;
import com.example.blogapp.entities.response.LoginResponse;
import com.example.blogapp.dialog.OtpDialogManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {
    TextInputEditText editPassword, editEmail, editCfPassword;
    TextInputLayout passwordLayout, cfPasswordLayout, emailLayout;
    TextView txtLogin;
    Button btnRegister;
    private AlertDialog loadingDialog;
    private OtpDialogManager otpDialogManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        otpDialogManager = new OtpDialogManager();
        initUI();
        initLintener();
    }
    private void initUI(){
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        editCfPassword = findViewById(R.id.edit_cf_password);
        txtLogin = findViewById(R.id.txt_login);
        btnRegister = findViewById(R.id.btn_regsiter);
        emailLayout = findViewById(R.id.txt_emaillayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        cfPasswordLayout = findViewById(R.id.cfPasswordLayout);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(LayoutInflater.from(this).inflate(R.layout.dialog_loading, null));
        builder.setCancelable(false);
        loadingDialog = builder.create();
    }

    private void initLintener(){
        btnRegister.setOnClickListener(v -> onClickRegister());
        txtLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        editEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                if (!isValidEmail(email)) {
                    emailLayout.setError("Email không hợp lệ.");
                } else {
                    emailLayout.setError(null);
                }
            }
        });

        editPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString().trim();
                if (!isValidPassword(password)) {
                    passwordLayout.setError("Mật khẩu phải chứa ít nhất một chữ cái và một chữ số.");
                } else {
                    passwordLayout.setError(null);
                }
            }
        });

        editCfPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String password = editPassword.getText().toString().trim();
                String confirmPassword = s.toString().trim();

                if (!confirmPassword.equals(password)) {
                    cfPasswordLayout.setError("Mật khẩu không trùng khớp.");
                } else {
                    cfPasswordLayout.setError(null);
                }
            }
        });
    }

    private void onClickRegister() {
        String strEmail = editEmail.getText().toString().trim();
        String strPassword = editPassword.getText().toString().trim();
        String strCfPassword = editCfPassword.getText().toString().trim();
        boolean isValid = true;

        if (strEmail.isEmpty()) {
            emailLayout.setError("Vui lòng nhập email.");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }

        if (strPassword.isEmpty()) {
            passwordLayout.setError("Vui lòng nhập mật khẩu.");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        if (strCfPassword.isEmpty()) {
            cfPasswordLayout.setError("Vui lòng nhập lại mật khẩu.");
            isValid = false;
        }  else {
            cfPasswordLayout.setError(null);
        }

        if (isValid) {
            registerMfa(strEmail, strPassword);
        }
    }
    private void registerMfa(String email, String password){
        loadingDialog.show();
        LoginRequest loginRequest = new LoginRequest(email, password);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.registerMfa(loginRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful()) {
                    showOtpDialog(email, password);
                } else {
                    handleError(response);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showOtpDialog(String email, String password) {
        if (otpDialogManager.isDialogShowing()) {
            return;
        }

        otpDialogManager.showOtpDialog(RegisterActivity.this, new OtpDialogManager.OtpListener() {
            @Override
            public void onOtpConfirmed(String otp) {
                verifyOtp(email, otp);
            }

            @Override
            public void onResendOtp() {
                registerMfa(email, password);
                Toast.makeText(RegisterActivity.this, "OTP đã được gửi lại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyOtp(String email, String otp) {
        loadingDialog.show();
        OtpRequest otpRequest = new OtpRequest(email, otp);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.registerMfaVerify(otpRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công.", Toast.LENGTH_SHORT).show();
                    if (otpDialogManager != null) {
                        otpDialogManager.dismissDialog();
                    }
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    otpDialogManager.setError("Mã OTP không chính xác");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void handleError(Response<?> response) {
        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
            if (errorBody.contains("Email already exist!")) {
                emailLayout.setError("Email đã tồn tại. Vui lòng sử dụng email khác.");
            } else {
                Toast.makeText(this, "Lỗi: " + errorBody, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi đọc phản hồi từ máy chủ.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$");
    }
}