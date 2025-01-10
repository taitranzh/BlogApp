package com.example.blogapp.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.blogapp.R;
import com.example.blogapp.api.ApiService;
import com.example.blogapp.api.RetrofitClient;
import com.example.blogapp.entities.request.EmailRequest;
import com.example.blogapp.entities.request.LoginRequest;
import com.example.blogapp.entities.request.OtpRequest;
import com.example.blogapp.entities.response.LoginResponse;
import com.example.blogapp.dialog.EmailDialogManager;
import com.example.blogapp.dialog.OtpDialogManager;
import com.example.blogapp.dialog.ResetPasswordDialogManager;
import com.example.blogapp.utils.SecureStorage;
import com.example.blogapp.utils.Utils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {
    TextInputEditText editPassword, editEmail;
    TextView txtRegister, txtForgotPass;
    Button btnLogin;
    private AlertDialog loadingDialog;
    private OtpDialogManager otpDialogManager;
    private int incorrectPasswordCount = 0;
    TextInputLayout passwordLayout, emailLayout;
    private EmailDialogManager emailDialogManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        otpDialogManager = new OtpDialogManager();
        emailDialogManager = new EmailDialogManager();
        initUI();
        initListener();
    }

    private void initUI() {
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        txtRegister = findViewById(R.id.txt_register);
        btnLogin = findViewById(R.id.btn_login);
        emailLayout = findViewById(R.id.txt_emaillayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        txtForgotPass = findViewById(R.id.tv_forgot_pass);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(LayoutInflater.from(this).inflate(R.layout.dialog_loading, null));
        builder.setCancelable(false);
        loadingDialog = builder.create();
    }

    private void initListener() {
        btnLogin.setOnClickListener(v -> onClickLogin());
        txtRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        txtForgotPass.setOnClickListener(v -> showForgotPassMfa());
    }

    private void onClickLogin() {
        String strEmail = editEmail.getText().toString().trim();
        String strPassword = editPassword.getText().toString().trim();

        if (strEmail.isEmpty()) {
            emailLayout.setError("Vui lòng nhập email.");
            return;
        } else if (!isValidEmail(strEmail)) {
            emailLayout.setError("Email không hợp lệ.");
            return;
        } else {
            emailLayout.setError(null);
        }

        if (strPassword.isEmpty()) {
            passwordLayout.setError("Vui lòng nhập mật khẩu");
            return;
        } else {
            passwordLayout.setError(null);
        }

        loadingDialog.show();
        LoginRequest loginRequest = new LoginRequest(strEmail, strPassword);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulLogin(response.body());
                } else if (response.code() == 400) {
                    handleLoginError(response.errorBody());
                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSuccessfulLogin(LoginResponse loginResponse) {
        String accessToken = loginResponse.getTokenPair().getAccessToken();
        String refreshToken = loginResponse.getTokenPair().getRefreshToken();
        String userId = getUserIdFromToken(accessToken);

        SecureStorage.getInstance(LoginActivity.this).saveAccessToken(accessToken);
        SecureStorage.getInstance(LoginActivity.this).saveRefreshToken(refreshToken);
        SecureStorage.getInstance(LoginActivity.this).saveUserId(userId);

        saveLoginState(true);

        Toast.makeText(LoginActivity.this, "Đăng nhập thành công.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void handleLoginError(ResponseBody errorBody) {
        if (errorBody == null) {
            Toast.makeText(LoginActivity.this, "Lỗi không xác định!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String errorMessage = errorBody.string();
            if (errorMessage.contains("User not found!")) {
                emailLayout.setError("Tài khoản không tồn tại");
                passwordLayout.setError(null);
            } else if (errorMessage.contains("Password is incorrect!")) {
                incorrectPasswordCount++;
                passwordLayout.setError("Mật khẩu không chính xác");
                emailLayout.setError(null);
                if (incorrectPasswordCount >= 5) {
                    lockAccount(editEmail.getText().toString().trim());
                    showAccountLockedDialog();
                }
            } else if (errorMessage.contains("Please open your mail to verify!")) {
                showOtpLoginDialog(editEmail.getText().toString().trim());
            } else if (errorMessage.contains("Account is locked!")) {
                showAccountLockedDialog();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void lockAccount(String email) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.lockAccount(email).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Tài khoản đã bị khóa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi khi khóa tài khoản", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showAccountLockedDialog() {
        if (emailDialogManager.isDialogShowing()) {
            return;
        }
        emailDialogManager.showEmailDialog(LoginActivity.this,true, email -> unlockAccount(email));
    }

    private void unlockAccount(String email) {
        loadingDialog.show();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.unlockAccount(email).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    if (emailDialogManager.isDialogShowing()) {
                        emailDialogManager.dismissDialog();
                    }
                    showUnlockAccountDialog(email);
                } else if (response.code() == 400) {
                    emailDialogManager.setError("Tài khoản không tồn tại");
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Lỗi khi gửi Email xác thực", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUnlockAccountDialog(String email) {
        if (otpDialogManager.isDialogShowing()) {
            return;
        }
        otpDialogManager.showOtpDialog(LoginActivity.this, new OtpDialogManager.OtpListener() {
            @Override
            public void onOtpConfirmed(String otp) {
                verifyUnlockOtp(email, otp);
            }

            @Override
            public void onResendOtp() {
                unlockAccount(email);
                Toast.makeText(LoginActivity.this, "OTP đã được gửi lại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyUnlockOtp(String email, String otp) {
        loadingDialog.show();
        OtpRequest otpRequest = new OtpRequest(email, otp);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.unlockAccountVerify(otpRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    if (otpDialogManager != null) {
                        otpDialogManager.dismissDialog();
                    }
                    Toast.makeText(LoginActivity.this, "Mở khóa tài khoản thành công. Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
                } else {
                    otpDialogManager.setError("Mã OTP không chính xác");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingDialog.dismiss();
            }
        });
    }

    private void showOtpLoginDialog(String email) {
        if (otpDialogManager.isDialogShowing()) {
            return;
        }
        otpDialogManager.showOtpDialog(LoginActivity.this, new OtpDialogManager.OtpListener() {
            @Override
            public void onOtpConfirmed(String otp) {
                verifyLoginOtp(email, otp);
            }

            @Override
            public void onResendOtp() {
                onClickLogin();
                Toast.makeText(LoginActivity.this, "OTP đã được gửi lại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyLoginOtp(String email, String otp) {
        loadingDialog.show();
        OtpRequest otpRequest = new OtpRequest(email, otp);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.loginMfaVerify(otpRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulLogin(response.body());
                } else {
                    otpDialogManager.setError("Mã OTP không chính xác");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Lỗi kết nối.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showForgotPassMfa(){
        if (emailDialogManager.isDialogShowing()) {
            return;
        }
        emailDialogManager.showEmailDialog(LoginActivity.this, false, email -> sendOtpForgotPass(email));
    }
    private void saveLoginState(boolean isLoggedIn) {
        SharedPreferences sharedPreferences = getSharedPreferences(Utils.PREF_LOGIN, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Utils.KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }
    private void sendOtpForgotPass(String email) {
        loadingDialog.show();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        EmailRequest emailRequest = new EmailRequest(email);

        apiService.sendOtpForgotPass(emailRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful()) {
                    if (emailDialogManager.isDialogShowing()) {
                        emailDialogManager.dismissDialog();
                    }
                    Toast.makeText(LoginActivity.this, "OTP đã được gửi thành công!", Toast.LENGTH_SHORT).show();
                    showOtpForgotPassDialog(email);
                } else if (response.code() == 400) {
                    emailDialogManager.setError("Email không hợp lệ!");
                } else {
                    Toast.makeText(LoginActivity.this, "Đã xảy ra lỗi!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showOtpForgotPassDialog(String email) {
        if (otpDialogManager.isDialogShowing()) return;

        otpDialogManager.showOtpDialog(LoginActivity.this, new OtpDialogManager.OtpListener() {
            @Override
            public void onOtpConfirmed(String otp) {
                verifyOtpForgotPass(email, otp);
            }

            @Override
            public void onResendOtp() {
                sendOtpForgotPass(email);
                Toast.makeText(LoginActivity.this, "OTP đã được gửi lại!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void verifyOtpForgotPass(String email, String otp) {
        loadingDialog.show();
        OtpRequest otpRequest = new OtpRequest(email, otp);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.verifyOtpForgotPass(otpRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful()) {
                    if (otpDialogManager != null) {
                        otpDialogManager.dismissDialog();
                    }
                    Toast.makeText(LoginActivity.this, "OTP xác thực thành công!", Toast.LENGTH_SHORT).show();
                    ResetPasswordDialogManager resetPasswordDialogManager = new ResetPasswordDialogManager();
                    resetPasswordDialogManager.showResetPasswordDialog(LoginActivity.this, email);
                } else {
                    otpDialogManager.setError("Mã OTP không chính xác!");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Lỗi kết nối.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getUserIdFromToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getClaim("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier").asString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
