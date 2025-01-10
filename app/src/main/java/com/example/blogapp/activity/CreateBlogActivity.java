package com.example.blogapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.blogapp.R;
import com.example.blogapp.api.ApiService;
import com.example.blogapp.api.RetrofitClient;
import com.example.blogapp.entities.Blog;
import com.example.blogapp.entities.response.BlogImageDto;
import com.example.blogapp.entities.Category;
import com.example.blogapp.entities.request.BlogPostRequest;
import com.example.blogapp.utils.SecureStorage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateBlogActivity extends BaseActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 100;
    private ImageView imgBack, imgPreview;
    private EditText edtTitle, edtShortDescription, edtContent;
    private Switch isVisible;
    private Button btnUpload, btnSubmit;
    private Spinner spinnerCategory;
    private ArrayAdapter<String> categoryAdapter;
    private List<String> categoryList;
    private List<Category> categories = new ArrayList<>();
    private Uri selectedImageUri;
    private String blogId, categoryId, blogTitle, blogShortDescription, blogContent, blogFeaturedImage;
    private boolean visible;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_blog);
        initUI();
        getCategories();
        initListener();
        Intent intent = getIntent();

        blogId = intent.getStringExtra("BLOG_ID");
        blogTitle = intent.getStringExtra("BLOG_TITLE");
        blogShortDescription = intent.getStringExtra("BLOG_SHORT_DESCRIPTION");
        blogContent = intent.getStringExtra("BLOG_CONTENT");
        blogFeaturedImage = intent.getStringExtra("BLOG_FEATURED_IMAGE");
        categoryId = intent.getStringExtra("BLOG_CATEGORY_ID");
        visible = getIntent().getBooleanExtra("BLOG_IS_VISIBLE", false);

        if (blogId != null) {
            edtTitle.setText(blogTitle);
            edtShortDescription.setText(blogShortDescription);
            edtContent.setText(blogContent);
            isVisible.setChecked(visible);

            if (blogFeaturedImage != null) {
                imgPreview.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(blogFeaturedImage)
                        .into(imgPreview);
            }
            btnSubmit.setText("Sửa bài viết");
        } else {
            btnSubmit.setText("Đăng bài viết");
        }
    }

    private void initUI() {
        imgBack = findViewById(R.id.img_back);
        imgPreview = findViewById(R.id.img_preview);
        edtTitle = findViewById(R.id.edt_title);
        edtShortDescription = findViewById(R.id.edt_short_description);
        edtContent = findViewById(R.id.edt_content);
        isVisible = findViewById(R.id.switch_is_visible);
        btnUpload = findViewById(R.id.btn_upload_image);
        btnSubmit = findViewById(R.id.btn_submit);
        spinnerCategory = findViewById(R.id.spinner_category);

        categoryList = new ArrayList<>();
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void initListener() {
        imgBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        btnUpload.setOnClickListener(v -> {
            if (hasStoragePermission()) {
                openGallery();
            } else {
                requestStoragePermission();
            }
        });

        btnSubmit.setOnClickListener(v -> handleSubmit());
    }

    private boolean hasStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Không có quyền truy cập bộ nhớ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCategories() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getCategories(100).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    categoryList.clear();
                    for (Category category : categories) {
                        categoryList.add(category.getName());
                    }
                    categoryAdapter.notifyDataSetChanged();
                }

                if (categoryId != null) {
                    for (int i = 0; i < categories.size(); i++) {
                        if (categories.get(i).getId().equals(categoryId)) {
                            spinnerCategory.setSelection(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Toast.makeText(CreateBlogActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            imgPreview.setVisibility(View.VISIBLE);
                            imgPreview.setImageURI(selectedImageUri);
                        } else {
                            Toast.makeText(CreateBlogActivity.this, "Không thể lấy ảnh từ thư viện", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CreateBlogActivity.this, "Hủy chọn ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
    String getCurrentTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.getDefault());
        return dateFormat.format(new Date());
    }
    private void handleSubmit() {
        String title = edtTitle.getText().toString().trim();
        String shortDescription = edtShortDescription.getText().toString().trim();
        String content = edtContent.getText().toString().trim();
        boolean visible = isVisible.isChecked();
        String authorId = SecureStorage.getInstance(CreateBlogActivity.this).getUserId();;
        String publishedDate = getCurrentTimestamp();

        List<String> selectedCategories = new ArrayList<>();
        int selectedIndex = spinnerCategory.getSelectedItemPosition();
        if (selectedIndex != -1) {
            Category selectedCategory = categories.get(selectedIndex);
            selectedCategories.add(selectedCategory.getId());
        }

        if (imgPreview.getDrawable() != null) {
            uploadImageAndCreateBlogPost(title, shortDescription, content, visible, publishedDate, authorId, selectedCategories);
        } else {
            Toast.makeText(this, "Vui lòng chọn ảnh trước khi đăng bài", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageAndCreateBlogPost(String title, String shortDescription, String content, boolean visible,
                                              String publishedDate, String authorId, List<String> selectedCategories) {
        if (selectedImageUri != null) {
            String filePath = getRealPathFromURI(selectedImageUri);
            if (filePath != null) {
                File file = new File(filePath);

                RequestBody requestFile = RequestBody.create(file, MediaType.parse("multipart/form-data"));
                String newFileName = "new_" + System.currentTimeMillis() + ".jpg";
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", newFileName, requestFile);
                RequestBody fileName = RequestBody.create(file.getName(), MediaType.parse("multipart/form-data"));
                RequestBody titleBody = RequestBody.create(title, MediaType.parse("multipart/form-data"));

                ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                apiService.uploadImage(body, fileName, titleBody).enqueue(new Callback<BlogImageDto>() {
                    @Override
                    public void onResponse(Call<BlogImageDto> call, Response<BlogImageDto> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String featuredImageUrl = response.body().getUrl();
                            sendBlogPostRequest(title, shortDescription, content, featuredImageUrl, visible, publishedDate, authorId, selectedCategories);
                        } else {
                            Toast.makeText(CreateBlogActivity.this, "Lỗi upload ảnh: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BlogImageDto> call, Throwable t) {
                        Toast.makeText(CreateBlogActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Không thể lấy đường dẫn ảnh", Toast.LENGTH_SHORT).show();
            }
        } else if (blogId != null && blogFeaturedImage != null) {
            sendBlogPostRequest(title, shortDescription, content, blogFeaturedImage, visible, publishedDate, authorId, selectedCategories);
        } else {
            Toast.makeText(this, "Vui lòng chọn ảnh trước khi đăng bài", Toast.LENGTH_SHORT).show();
        }
    }
    private void sendBlogPostRequest(String title, String shortDescription, String content, String featuredImageUrl,
                                     boolean visible, String publishedDate, String authorId, List<String> selectedCategories) {
        BlogPostRequest request = new BlogPostRequest(
                title,
                shortDescription,
                content,
                featuredImageUrl,
                publishedDate,
                authorId,
                visible,
                selectedCategories
        );

        if (blogId != null) {
            updateBlogPost(blogId, request);
        } else {
            createBlogPost(request);
        }
    }
    private void createBlogPost(BlogPostRequest request) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.createBlogPost(request).enqueue(new Callback<Blog>() {
            @Override
            public void onResponse(Call<Blog> call, Response<Blog> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CreateBlogActivity.this, "Tạo bài viết thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CreateBlogActivity.this, "Lỗi tạo bài viết: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Blog> call, Throwable t) {
                Toast.makeText(CreateBlogActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateBlogPost(String blogId, BlogPostRequest request) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.updateBlogPost(blogId, request).enqueue(new Callback<Blog>() {
            @Override
            public void onResponse(Call<Blog> call, Response<Blog> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CreateBlogActivity.this, "Sửa bài viết thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CreateBlogActivity.this, "Lỗi sửa bài viết: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Blog> call, Throwable t) {
                Toast.makeText(CreateBlogActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }
}
