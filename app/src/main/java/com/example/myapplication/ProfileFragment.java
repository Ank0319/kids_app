package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.myapplication.model.User;
import com.example.myapplication.database.UserViewModel;

import static android.content.Context.MODE_PRIVATE;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ProfileFragment extends Fragment {

    private ImageView profileBackground;
    private ImageView profileAvatar;
    private TextView userName;
    private TextView accountId;
    private TextView ipLocation;
    private TextView userBio;
    private Button btnEditProfile;
    private Button btnSetBackground;
    private CardView historyNavCard;
    private CardView likesNavCard;
    private ImageButton btnSettings;
    private PopupWindow settingsPopupWindow;
    private UserViewModel userViewModel;
    private SharedPreferences sharedPreferences;
    private boolean isLogin = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        // 初始化视图
        initViews(view);
        
        // 初始化UserViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        
        // 初始化SharedPreferences
        sharedPreferences = getActivity().getSharedPreferences("user_info", MODE_PRIVATE);
        isLogin = sharedPreferences.getBoolean("is_login", false);
        
        // 设置点击事件
        setupClickListeners();
        
        // 判断用户是否已登录
        updateLoginState();
        
        // 如果已登录，加载用户数据
        if (isLogin) {
            loadUserData();
        }
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 设置默认值 
        userName.setText(R.string.default_username);
        accountId.setText(R.string.default_account_id);
        ipLocation.setText(R.string.default_ip_location);
        
        // 设置点击监听器
        btnEditProfile.setOnClickListener(v -> {
            if (isLogin) {
                Toast.makeText(getContext(), "编辑资料功能暂未实现", Toast.LENGTH_SHORT).show();
            } else {
                // 提示用户需要登录
                Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnSetBackground.setOnClickListener(v -> {
            if (isLogin) {
                Toast.makeText(getContext(), "设置背景功能暂未实现", Toast.LENGTH_SHORT).show();
            } else {
                // 提示用户需要登录
                Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
            }
        });
        
        profileAvatar.setOnClickListener(v -> {
            if (isLogin) {
                Toast.makeText(getContext(), "查看头像功能暂未实现", Toast.LENGTH_SHORT).show();
            } else {
                // 提示用户需要登录
                Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
            }
        });
        
        // 设置历史记录导航点击事件
        historyNavCard.setOnClickListener(v -> navigateToHistoryPage());
        
        // 设置我的喜欢导航点击事件
        likesNavCard.setOnClickListener(v -> navigateToLikesPage());
    }
    
    private void initViews(View view) {
        profileBackground = view.findViewById(R.id.profile_background);
        profileAvatar = view.findViewById(R.id.profile_avatar);
        userName = view.findViewById(R.id.user_name);
        accountId = view.findViewById(R.id.account_id);
        ipLocation = view.findViewById(R.id.ip_location);
        userBio = view.findViewById(R.id.user_bio);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnSetBackground = view.findViewById(R.id.btn_set_background);
        historyNavCard = view.findViewById(R.id.history_nav_card);
        likesNavCard = view.findViewById(R.id.likes_nav_card);
        btnSettings = view.findViewById(R.id.btn_settings);
    }
    
    private void setupClickListeners() {
        // 编辑资料按钮
        btnEditProfile.setOnClickListener(v -> {
            if (isLogin) {
                Toast.makeText(getContext(), "编辑资料功能暂未实现", Toast.LENGTH_SHORT).show();
            } else {
                // 提示用户需要登录
                Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
            }
        });
        
        // 设置背景按钮
        btnSetBackground.setOnClickListener(v -> {
            if (isLogin) {
                Toast.makeText(getContext(), "设置背景功能暂未实现", Toast.LENGTH_SHORT).show();
            } else {
                // 提示用户需要登录
                Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
            }
        });
        
        // 头像点击事件
        profileAvatar.setOnClickListener(v -> {
            if (isLogin) {
                Toast.makeText(getContext(), "查看头像功能暂未实现", Toast.LENGTH_SHORT).show();
            } else {
                // 提示用户需要登录
                Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
            }
        });
        
        // 设置按钮点击事件
        btnSettings.setOnClickListener(v -> showSettingsMenu(v));
        
        // 历史记录导航点击事件
        historyNavCard.setOnClickListener(v -> navigateToHistoryPage());
        
        // 喜欢导航点击事件
        likesNavCard.setOnClickListener(v -> navigateToLikesPage());
    }
    
    /**
     * 显示设置菜单
     */
    private void showSettingsMenu(View anchorView) {
        // 使用自定义布局创建PopupWindow
        View menuView = LayoutInflater.from(getContext()).inflate(R.layout.custom_settings_menu, null);
        final PopupWindow popupWindow = new PopupWindow(
                menuView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        
        // 设置背景，允许点击外部区域关闭
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        
        // 根据登录状态显示/隐藏相应选项
        View loginOption = menuView.findViewById(R.id.menu_login);
        View logoutOption = menuView.findViewById(R.id.menu_logout);
        View registerOption = menuView.findViewById(R.id.menu_register);
        
        loginOption.setVisibility(isLogin ? View.GONE : View.VISIBLE);
        logoutOption.setVisibility(isLogin ? View.VISIBLE : View.GONE);
        registerOption.setVisibility(isLogin ? View.GONE : View.VISIBLE);
        
        // 设置点击事件
        menuView.findViewById(R.id.menu_parent_settings).setOnClickListener(v -> {
            Toast.makeText(getContext(), "家长控制功能暂未实现", Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });
        
        loginOption.setOnClickListener(v -> {
            navigateToLogin();
            popupWindow.dismiss();
        });
        
        logoutOption.setOnClickListener(v -> {
            logout();
            popupWindow.dismiss();
        });
        
        registerOption.setOnClickListener(v -> {
            navigateToRegister();
            popupWindow.dismiss();
        });
        
        // 计算显示位置并显示
        popupWindow.showAsDropDown(anchorView, 0, 0);
    }
    
    /**
     * 导航到历史记录页面
     */
    private void navigateToHistoryPage() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_container, HistoryFragment.newInstance())
                .addToBackStack(null)  // 添加到返回栈，以便可以返回
                .commit();
    }
    
    /**
     * 导航到我的喜欢页面
     */
    private void navigateToLikesPage() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_container, LikesFragment.newInstance())
                .addToBackStack(null)  // 添加到返回栈，以便可以返回
                .commit();
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }
    
    private void logout() {
        // 清除用户信息
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("user_id");
        editor.remove("username");
        editor.remove("nickname");
        editor.remove("avatarUrl");
        editor.remove("signature");
        editor.putBoolean("is_login", false);
        editor.apply();
        
        // 更新ViewModel
        userViewModel.logout();
        
        // 更新UI状态
        isLogin = false;
        updateLoginState();
        
        // 提示用户
        Toast.makeText(getContext(), "已退出登录", Toast.LENGTH_SHORT).show();
    }
    
    private void navigateToRegister() {
        Intent intent = new Intent(getActivity(), RegisterActivity.class);
        startActivity(intent);
    }
    
    private void updateLoginState() {
        if (isLogin) {
            // 已登录状态
            btnEditProfile.setVisibility(View.VISIBLE);
            btnSetBackground.setVisibility(View.VISIBLE);
        } else {
            // 未登录状态
            btnEditProfile.setVisibility(View.VISIBLE); // 点击后提示需要登录
            btnSetBackground.setVisibility(View.VISIBLE); // 点击后提示需要登录
            
            // 重置用户信息显示
            userName.setText("未登录");
            accountId.setText("点击右上角设置进行登录");
            ipLocation.setText("");
            userBio.setText("登录后查看更多信息");
            profileAvatar.setImageResource(R.drawable.avatar); // 使用drawable资源中的avatar
        }
    }
    
    private void loadUserData() {
        // 从SharedPreferences加载用户信息
        SharedPreferences sp = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String userId = sp.getString("user_id", "0");
        String username = sp.getString("username", "");
        String nickname = sp.getString("nickname", "");
        String avatarUrl = sp.getString("avatarUrl", "");
        String signature = sp.getString("signature", "");
        String backgroundUrl = sp.getString("backgroundUrl", "");
        
        // 更新UI显示
        userName.setText(nickname.isEmpty() ? getString(R.string.default_username) : nickname);
        accountId.setText(username);
        ipLocation.setText("中国");  // 实际应用中可能需要从服务器获取
        userBio.setText(signature.isEmpty() ? getString(R.string.default_bio) : signature);
        
        // 加载头像
        if (avatarUrl.isEmpty()) {
            profileAvatar.setImageResource(R.drawable.avatar);
        } else {
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .circleCrop()
                .into(profileAvatar);
        }
        
        // 加载背景图
        if (backgroundUrl.isEmpty()) {
            profileBackground.setImageResource(R.drawable.background);
        } else {
            Glide.with(this)
                .load(backgroundUrl)
                .placeholder(R.drawable.background)
                .error(R.drawable.background)
                .into(profileBackground);
        }
        
        // 更新ViewModel中的用户数据
        User user = new User();
        user.setId(Long.parseLong(userId));
        user.setUsername(username);
        user.setNickname(nickname.isEmpty() ? getString(R.string.default_username) : nickname);
        user.setSignature(signature.isEmpty() ? getString(R.string.default_bio) : signature);
        user.setAvatarUrl(avatarUrl.isEmpty() ? String.valueOf(R.drawable.avatar) : avatarUrl);
        user.setBackgroundUrl(backgroundUrl.isEmpty() ? String.valueOf(R.drawable.background) : backgroundUrl);
        userViewModel.setUser(user);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // 检查登录状态是否改变
        boolean currentLoginState = sharedPreferences.getBoolean("is_login", false);
        if (currentLoginState != isLogin) {
            isLogin = currentLoginState;
            updateLoginState();
            
            if (isLogin) {
                loadUserData();
            }
        } else if (isLogin) {
            // 即使登录状态没变，但已登录的情况下也重新加载数据
            // 这样确保从登录页面返回时数据是最新的
            loadUserData();
        }
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }
} 