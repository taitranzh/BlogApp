package com.example.blogapp.api;

import android.util.Log;
import com.example.blogapp.MyApp;
import com.example.blogapp.entities.response.LoginResponse;
import com.example.blogapp.utils.SecureStorage;
import com.example.blogapp.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            SecureStorage secureStorage = SecureStorage.getInstance(MyApp.getContext());
                            String accessToken = secureStorage.getAccessToken();

                            Request originalRequest = chain.request();
                            Request.Builder builder = originalRequest.newBuilder();

                            if (accessToken != null && !accessToken.isEmpty()) {
                                builder.header("Authorization", "Bearer " + accessToken);
                            }

                            Request newRequest = builder.build();
                            Response response = chain.proceed(newRequest);

                            if (response.code() == 401) {
                                String refreshToken = secureStorage.getRefreshToken();
                                String userId = secureStorage.getUserId();

                                if (refreshToken != null && !refreshToken.isEmpty()) {
                                    Log.d("RetrofitClient", "Attempting to refresh token...");
                                    LoginResponse newTokens = refreshAccessToken(userId, refreshToken);

                                    if (newTokens != null) {
                                        secureStorage.saveAccessToken(newTokens.getTokenPair().getAccessToken());
                                        secureStorage.saveRefreshToken(newTokens.getTokenPair().getRefreshToken());

                                        Request retryRequest = originalRequest.newBuilder()
                                                .header("Authorization", "Bearer " + newTokens.getTokenPair().getAccessToken())
                                                .build();

                                        return chain.proceed(retryRequest);
                                    } else {
                                        Log.e("RetrofitClient", "Token refresh failed!");
                                        throw new IOException("Unauthorized - Token refresh failed");
                                    }
                                }
                            }

                            return response;
                        }
                    })
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Utils.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    private static LoginResponse refreshAccessToken(String userId, String refreshToken) {
        try {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            Call<LoginResponse> call = apiService.refreshToken(userId, refreshToken);

            retrofit2.Response<LoginResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                Log.d("RetrofitClient", "Token refreshed successfully.");
                return response.body();
            } else {
                Log.e("RetrofitClient", "Failed to refresh token: " + response.errorBody().string());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
