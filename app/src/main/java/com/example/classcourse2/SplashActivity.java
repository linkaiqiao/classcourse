package com.example.classcourse2;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.classcourse2.MainActivity;
import com.example.classcourse2.PolicyManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 检查用户是否同意最新版本的政策
        if (!PolicyManager.hasAgreedToCurrentPolicy(this)) {
            // 跳转到政策同意页面
            Intent intent = new Intent(this, PrivacyActivity.class);
            startActivityForResult(intent, REQUEST_CODE_POLICY);
        } else {
            // 直接进入主界面
            startMainActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_POLICY) {
            if (resultCode == RESULT_OK) {
                // 用户同意了政策，进入主界面
                PolicyManager.setPolicyAgreed(this, true);
                startMainActivity();
            } else {
                // 用户不同意政策，退出应用
                finish();
            }
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private static final int REQUEST_CODE_POLICY = 1002;
}