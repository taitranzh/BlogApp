package com.example.blogapp.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.blogapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class OtpDialogManager {

    private AlertDialog dialog;
    private CountDownTimer countDownTimer;
    private TextInputLayout layoutEditOtp;
    private TextInputEditText inputOtp;

    public interface OtpListener {
        void onOtpConfirmed(String otp);

        void onResendOtp();
    }

    public void showOtpDialog(Context context, OtpListener listener) {
        if (dialog != null && dialog.isShowing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_otp, null);
        builder.setView(view);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        layoutEditOtp = view.findViewById(R.id.layout_edit_otp);
        inputOtp = view.findViewById(R.id.edit_otp);
        TextView tvTimer = view.findViewById(R.id.tv_timer);
        TextView tvResend = view.findViewById(R.id.tv_resend);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);
        Button btnClose = view.findViewById(R.id.btn_cancel);

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        startCountdownTimer(tvTimer, tvResend);

        btnConfirm.setOnClickListener(v -> {
            String otp = inputOtp.getText().toString().trim();
            if (otp.isEmpty()) {
                layoutEditOtp.setError("Vui lòng nhập mã OTP.");
            } else {
                layoutEditOtp.setError(null);
                listener.onOtpConfirmed(otp);
            }
        });

        tvResend.setOnClickListener(v -> {
            listener.onResendOtp();
            layoutEditOtp.setError(null);
            resetCountdownTimer(tvTimer, tvResend);
        });

        btnClose.setOnClickListener(v ->  dismissDialog());
    }

    private void startCountdownTimer(TextView tvTimer, TextView tvResend) {
        countDownTimer = new CountDownTimer(5 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 1000 / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00");
                tvResend.setVisibility(View.VISIBLE);
                tvTimer.setVisibility(View.GONE);
                setError("Mã OTP đã hết hiệu lực. Vui lòng thực hiện lại");
            }
        }.start();
    }
    private void resetCountdownTimer(TextView tvTimer, TextView tvResend) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        tvTimer.setText("05:00");
        tvTimer.setVisibility(View.VISIBLE);
        tvResend.setVisibility(View.GONE);
        startCountdownTimer(tvTimer, tvResend);
    }
    public void setError(String errorMessage) {
        if (layoutEditOtp != null) {
            layoutEditOtp.setError(errorMessage);
            inputOtp.requestFocus();
        }
    }
    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
    public boolean isDialogShowing() {
        return dialog != null && dialog.isShowing();
    }
}