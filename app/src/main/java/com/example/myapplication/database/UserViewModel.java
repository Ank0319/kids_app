package com.example.myapplication.database;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.model.User;

/**
 * UserViewModel类，用于管理用户数据和相关操作
 * 在Fragment和Activity生命周期中保持用户数据
 */
public class UserViewModel extends ViewModel {
    
    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);
    
    // 获取当前用户数据
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }
    
    // 设置当前用户数据
    public void setUser(User user) {
        currentUser.setValue(user);
        isLoggedIn.setValue(user != null);
    }
    
    // 获取登录状态
    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }
    
    // 更新用户信息
    public void updateUserInfo(String username, String nickname, String signature, String avatarUrl) {
        User user = currentUser.getValue();
        if (user != null) {
            user.setUsername(username);
            user.setNickname(nickname);
            user.setSignature(signature);
            user.setAvatarUrl(avatarUrl);
            currentUser.setValue(user);
        }
    }
    
    // 登出操作
    public void logout() {
        currentUser.setValue(null);
        isLoggedIn.setValue(false);
    }
    
    // 获取用户详情
    public void fetchUserDetails(Long userId, UserApiCallback callback) {
        UserApiService apiService = ApiClient.getUserApiService();
        // 实际应用中，这里应该通过网络请求获取用户详情
        // 这里简化处理，仅作示例
        // 注意：在实际应用中，应该使用Repository模式将数据访问与ViewModel分离
        
        // 模拟请求成功
        if (callback != null) {
            User user = new User();
            user.setId(userId);
            user.setUsername("user_" + userId);
            user.setNickname("用户_" + userId);
            user.setSignature("这是一个签名");
            user.setAvatarUrl("");
            callback.onSuccess(user);
        }
    }
    
    // 用户API回调接口
    public interface UserApiCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }
} 