package com.example.blogapp.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.blogapp.R;
import com.example.blogapp.api.ApiService;
import com.example.blogapp.api.RetrofitClient;
import com.example.blogapp.entities.UserInfo;
import com.example.blogapp.entities.request.OtpRequest;
import com.example.blogapp.dialog.OtpDialogManager;
import com.example.blogapp.dialog.PasswordDialogManager;
import com.example.blogapp.utils.SecureStorage;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserInfoActivity extends BaseActivity {
    private TextView tvUsername;
    private EditText edtEmail, edtFullname, edtAddress;
    private Button btn_save;
    private ImageView imgBack;
    private OtpDialogManager otpDialogManager;
    private AlertDialog loadingDialog;
    private PasswordDialogManager passwordDialogManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        otpDialogManager = new OtpDialogManager();
        passwordDialogManager = new PasswordDialogManager();
        initUI();
        getUserInfo();
        initListener();
    }

    private void initUI() {
        edtAddress = findViewById(R.id.edt_address);
        edtEmail = findViewById(R.id.edt_email);
        edtFullname = findViewById(R.id.edt_fullname);
        btn_save = findViewById(R.id.btn_save);
        imgBack = findViewById(R.id.img_back);
        tvUsername = findViewById(R.id.tv_username);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(LayoutInflater.from(this).inflate(R.layout.dialog_loading, null));
        builder.setCancelable(false);
        loadingDialog = builder.create();
    }

    private void initListener() {
        btn_save.setOnClickListener(v -> checkMfaVerified());
        imgBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        if (otpDialogManager != null && otpDialogManager.isDialogShowing()) {
            otpDialogManager.dismissDialog();
        }
    }
    private void checkMfaVerified() {
        loadingDialog.show();
        String userId = SecureStorage.getInstance(UserInfoActivity.this).getUserId();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.mfaVerify(userId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            String responseBody = response.body().string();
                            if (responseBody.contains("User current mfa verified")) {
                                updateUserInfo();
                            } else {
                                showPasswordDialog();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorMessage = response.errorBody().string();
                            if (errorMessage.contains("User is not mfa verified")) {
                                showPasswordDialog();
                            } else {
                                Toast.makeText(UserInfoActivity.this, "Lỗi kết nối MFA.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(UserInfoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showPasswordDialog() {
        if (passwordDialogManager.isDialogShowing()) {
            return;
        }
        passwordDialogManager.showPasswordDialog(UserInfoActivity.this, password -> verifyPassword(password));
    }
    private void sendEmailOtp() {
        loadingDialog.show();
        String email = edtEmail.getText().toString();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.sendEmailOtp(email).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loadingDialog.dismiss();
                if(response.isSuccessful()){
                    showOtpDialog(email);
                } else {
                    Toast.makeText(UserInfoActivity.this, "Gửi email thất bại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(UserInfoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyPassword(String password) {
        loadingDialog.show();
        String userId = SecureStorage.getInstance(UserInfoActivity.this).getUserId();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.verifyPassword(userId, password).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null && response.body().string().contains("Password is correct")) {
                            if (passwordDialogManager.isDialogShowing()) {
                                passwordDialogManager.dismissDialog();
                            }
                            sendEmailOtp();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    passwordDialogManager.setError("Mật khẩu không đúng");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(UserInfoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateUserInfo() {
        String username = tvUsername.getText().toString();
        String email = edtEmail.getText().toString();
        String fullname = edtFullname.getText().toString();
        String address = edtAddress.getText().toString();

        if (fullname.isEmpty() || address.isEmpty()) {
            Toast.makeText(UserInfoActivity.this, "Vui lòng nhập đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
            return;
        }

        UserInfo userInfo = new UserInfo(email, username, fullname, address);
        String userId = SecureStorage.getInstance(UserInfoActivity.this).getUserId();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.updateUserInfo(userId, userInfo).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null && response.body()) {
                    Toast.makeText(UserInfoActivity.this, "Cập nhật thông tin thành công.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserInfoActivity.this, "Cập nhật thông tin thất bại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(UserInfoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserInfo() {
        String userId = SecureStorage.getInstance(UserInfoActivity.this).getUserId();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getUserInfo(userId).enqueue(new Callback<UserInfo>() {
            @Override
            public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserInfo userInfo = response.body();
                    tvUsername.setText(userInfo.getUserName());
                    edtEmail.setText(userInfo.getEmail());
                    edtFullname.setText(userInfo.getFullName());
                    edtAddress.setText(userInfo.getAddress());
                } else {
                    Toast.makeText(UserInfoActivity.this, "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserInfo> call, Throwable t) {
                Toast.makeText(UserInfoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOtpDialog(String email) {
        if (otpDialogManager.isDialogShowing()) {
            return;
        }
        otpDialogManager.showOtpDialog(UserInfoActivity.this, new OtpDialogManager.OtpListener() {
            @Override
            public void onOtpConfirmed(String otp) {
                verifyOtp(email, otp);
            }

            @Override
            public void onResendOtp() {
                sendEmailOtp();
                Toast.makeText(UserInfoActivity.this, "OTP đã được gửi lại!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void verifyOtp(String email, String otp) {
        loadingDialog.show();
        OtpRequest otpRequest = new OtpRequest(email, otp);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.verifyUpdateUser(otpRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful()) {
                    updateUserInfo();
                    if (otpDialogManager != null) {
                        otpDialogManager.dismissDialog();
                    }
                } else {
                    otpDialogManager.setError("Mã OTP không chính xác");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(UserInfoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
