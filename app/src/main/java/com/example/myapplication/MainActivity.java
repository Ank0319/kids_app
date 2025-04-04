package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.myapplication.database.ApiClient;
import com.example.myapplication.database.UserApiService;
import com.example.myapplication.database.VideoApiService;
import com.example.myapplication.model.ApiResponse;
import com.example.myapplication.model.Video;
import com.example.myapplication.model.VideoPageResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private CardView topNavBar;
    private ConstraintLayout mainLayout;
    private FrameLayout contentContainer;
    private ImageButton searchButton;
    private TextView titleText;
    
    // Keep track of current fragment for potential state restoration
    private Fragment currentFragment;

    // API服务
    private UserApiService userApiService;
    private VideoApiService videoApiService;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化API服务
        userApiService = ApiClient.getUserApiService();
        videoApiService = ApiClient.getVideoApiService();

        // Initialize views
        mainLayout = findViewById(R.id.main_layout);
        topNavBar = findViewById(R.id.top_nav_bar);
        contentContainer = findViewById(R.id.content_container);
        searchButton = findViewById(R.id.search_button);
        titleText = findViewById(R.id.title_text);
        
        // Initialize bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();

        // 设置默认Fragment
        if (savedInstanceState == null) {
            currentFragment = HomeFragment.newInstance();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_container, currentFragment)
                    .commit();
        }

        // 设置底部导航栏点击监听
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                selectedFragment = HomeFragment.newInstance();
                titleText.setText("学习");
                showTopNavBar(true);
            } else if (itemId == R.id.navigation_recommend) {
                selectedFragment = EntertainmentFragment.newInstance();
                titleText.setText("娱乐");
                showTopNavBar(true);
            } else if (itemId == R.id.navigation_me) {
                selectedFragment = ProfileFragment.newInstance();
                showTopNavBar(false);
            }

            if (selectedFragment != null && selectedFragment != currentFragment) {
                currentFragment = selectedFragment;
                fragmentManager.beginTransaction()
                        .replace(R.id.content_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // 设置默认标题和显示顶部导航栏
        titleText.setText("学习");
        showTopNavBar(true);

        // 设置搜索按钮点击监听
        searchButton.setOnClickListener(v -> showSearchDialog());
    }

    private void showSearchDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_search, null);
        EditText searchEditText = dialogView.findViewById(R.id.search_edit_text);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button searchButton = dialogView.findViewById(R.id.search_button);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        searchButton.setOnClickListener(v -> {
            String searchQuery = searchEditText.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                performSearch(searchQuery);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void performSearch(String query) {
        videoApiService.searchVideos(query).enqueue(new Callback<ApiResponse<VideoPageResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<VideoPageResponse>> call,
                                 @NonNull Response<ApiResponse<VideoPageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Video> videos = response.body().getData().getContent();
                    if (videos.isEmpty()) {
                        Toast.makeText(MainActivity.this, "未找到相关视频", Toast.LENGTH_SHORT).show();
                    } else {
                        // 创建搜索结果Fragment并显示
                        SearchResultFragment fragment = SearchResultFragment.newInstance(videos);
                        fragmentManager.beginTransaction()
                                .replace(R.id.content_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "搜索失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<VideoPageResponse>> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows or hides the top navigation bar and adjusts the content container constraints
     */
    private void showTopNavBar(boolean show) {
        if (show) {
            // Show top navigation bar
            topNavBar.setVisibility(View.VISIBLE);
            
            // Reset layout parameters for content container
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) contentContainer.getLayoutParams();
            params.topToBottom = R.id.top_nav_bar;
            params.topToTop = ConstraintSet.UNSET;
            params.topMargin = (int) getResources().getDimension(R.dimen.content_margin);
            contentContainer.setLayoutParams(params);
        } else {
            // Hide top navigation bar
            topNavBar.setVisibility(View.GONE);
            
            // Adjust content container to extend to the top with proper margin
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) contentContainer.getLayoutParams();
            params.topToBottom = ConstraintSet.UNSET;
            params.topToTop = ConstraintSet.PARENT_ID;
            params.topMargin = (int) getResources().getDimension(R.dimen.content_margin);
            contentContainer.setLayoutParams(params);
            
            // Ensure layout is refreshed
            mainLayout.requestLayout();
        }
    }
}