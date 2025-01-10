package com.example.blogapp.api;


import com.example.blogapp.entities.Blog;
import com.example.blogapp.entities.Category;
import com.example.blogapp.entities.Comment;
import com.example.blogapp.entities.request.ChangePassRequest;
import com.example.blogapp.entities.request.EmailRequest;
import com.example.blogapp.entities.response.BlogImageDto;
import com.example.blogapp.entities.request.BlogPostRequest;
import com.example.blogapp.entities.request.CommentRequest;
import com.example.blogapp.entities.request.LoginRequest;
import com.example.blogapp.entities.request.OtpRequest;
import com.example.blogapp.entities.response.LoginResponse;
import com.example.blogapp.entities.UserInfo;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/api/Auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
    @POST("/api/Auth/login-mfa-verify")
    Call<LoginResponse> loginMfaVerify(@Body OtpRequest otpRequest);
    @POST("/api/Auth/lock-account")
    Call<ResponseBody> lockAccount(@Query("email") String email);
    @POST("/api/Auth/unlock-account")
    Call<ResponseBody> unlockAccount(@Query("email") String email);
    @POST("/api/Auth/unlock-account-verify")
    Call<ResponseBody> unlockAccountVerify(@Body OtpRequest otpRequest);
    @POST("/api/Auth/register-mfa")
    Call<ResponseBody> registerMfa(@Body LoginRequest loginRequest);
    @POST("/api/Auth/register-mfa-verify")
    Call<LoginResponse> registerMfaVerify(@Body OtpRequest otpRequest);
    @GET("/api/BlogPosts")
    Call<List<Blog>> getBlogs(@Query("pageSize") int pageSize);
    @GET("/api/BlogPosts")
    Call<List<Blog>> getSearchBlogs(@Query("query") String query, @Query("pageSize") int pageSize);
    @GET("/api/BlogPosts/get-popular-posts")
    Call<List<Blog>> getBlogsHot(@Query("postNumbers") int postNumbers);
    @GET("/api/Categories")
    Call<List<Category>> getCategories(@Query("pageSize") int pageSize);
    @GET("/api/BlogPosts/get-by-category")
    Call<List<Blog>> getBlogsByCategory(@Query("categoryId") String categoryId, @Query("pageSize") int pageSize);
    @GET("/api/BlogPosts/get-by-author")
    Call<List<Blog>> getBlogsByAuthor(@Query("authorId") String authorId, @Query("pageSize") int pageSize);
    @GET("/api/BlogPosts/get-posts-user-liked")
    Call<List<Blog>> getBlogsLiked(@Query("userId") String userId, @Query("pageSize") int pageSize);
    @GET("/api/Users/{userId}")
    Call<UserInfo> getUserInfo(@Path("userId") String userId);
    @PUT("/api/Users/{userId}")
    Call<Boolean> updateUserInfo(@Path("userId") String userId, @Body UserInfo userInfo);
    @POST("/api/Users/send-mail-update-user")
    Call<ResponseBody> sendEmailOtp(@Body String email);
    @POST("/api/Users/verify-update-user")
    Call<ResponseBody> verifyUpdateUser(@Body OtpRequest otpRequest);
    @DELETE("/api/BlogPosts/user/post/{id}")
    Call<Void> deleteBlog(
            @Path("id") String postId
    );
    @POST("/api/BlogPosts/user/post/send-mail-delete/{id}")
    Call<ResponseBody> deletePostSendEmail(@Path("id") String postId, @Query("authorId") String userId);
    @POST("api/BlogPosts/user/delete-post-verify/{id}")
    Call<Void> deletePostVerify(@Path("id") String postId, @Query("userId") String userId, @Body String otp);
    @GET("/api/BlogPosts/{id}")
    Call<Blog> getBlogDetails(@Path("id") String blogId);

    @GET("/api/BlogPosts/check-liked/{url}")
    Call<Boolean> checkLiked(
            @Path("url") String urlHandle,
            @Query("userId") String userId
    );
    @GET("/api/BlogPosts/get-like-post/{url}")
    Call<Integer> getLikeCount(
            @Path("url") String urlHandle
    );
    @GET("/api/BlogPosts/like-post/{url}")
    Call<Integer> likePost(
            @Path("url") String urlHandle,
            @Query("userId") String userId
    );
    @GET("/api/Comment/count/{postId}")
    Call<Integer> getCommentCount(@Path("postId") String postId);
    @GET("/api/Comment/{postUrl}")
    Call<List<Comment>> getComments(@Path("postUrl") String postUrl);
    @GET("/api/Comment/get-subcomments")
    Call<List<Comment>> getSubComments(@Query("postId") String postId, @Query("parentId") String parentId);
    @POST("/api/Comment")
    Call<Integer> postComment(@Body CommentRequest commentRequest);
    @DELETE("/api/Comment/{id}")
    Call<Void> deleteComment(@Path("id") String id);
    @POST("/api/BlogPosts")
    Call<Blog> createBlogPost(@Body BlogPostRequest request);
    @Multipart
    @POST("/api/Images")
    Call<BlogImageDto> uploadImage(
            @Part MultipartBody.Part file,
            @Part("fileName") RequestBody fileName,
            @Part("title") RequestBody title
    );
    @PUT("/api/BlogPosts/{id}")
    Call<Blog> updateBlogPost(@Path("id") String id, @Body BlogPostRequest blogPostRequest);
    @POST("/api/Auth/refresh-token")
    Call<LoginResponse> refreshToken(
            @Query("uid") String userId,
            @Query("rt") String refreshToken
    );
    @POST("/api/Users/mfa-verified")
    Call<ResponseBody> mfaVerify(@Query("userId") String userId);
    @POST("/api/Users/verify-password-before-change")
    Call<ResponseBody> verifyPassword(@Query("userId") String userId, @Body String password);
    @POST("/api/Auth/forgot-password")
    Call<ResponseBody> sendOtpForgotPass(@Body EmailRequest emailRequest);
    @POST("/api/Auth/verify-forgot-password")
    Call<ResponseBody> verifyOtpForgotPass(@Body OtpRequest otpRequest);
    @POST("/api/Auth/reset-password")
    Call<ResponseBody> resetPassword(@Query("email") String email, @Body ChangePassRequest changePassRequest );
}

