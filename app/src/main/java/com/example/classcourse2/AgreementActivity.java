package com.example.classcourse2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class AgreementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);

        // 初始化视图
        initViews();
    }

    private void initViews() {
        // 返回按钮
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 同意按钮
        Button btnAgree = findViewById(R.id.btnAgree);
        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 保存用户同意状态
                saveAgreementStatus(true);

                // 返回到上一页
                Intent resultIntent = new Intent();
                resultIntent.putExtra("agreed", true);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        // 不同意按钮
        Button btnDisagree = findViewById(R.id.btnDisagree);
        btnDisagree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 提示用户必须同意协议才能使用应用
                new android.app.AlertDialog.Builder(AgreementActivity.this)
                        .setTitle("提示")
                        .setMessage("您需要同意用户协议才能继续使用本应用")
                        .setPositiveButton("确定", null)
                        .show();
            }
        });
    }

    private void saveAgreementStatus(boolean agreed) {
        // 使用SharedPreferences保存用户同意状态
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("user_agreed", agreed)
                .apply();
    }

    @Override
    public void onBackPressed() {
        // 如果用户没有同意协议，不能返回
        if (isUserAgreed()) {
            super.onBackPressed();
        } else {
            // 提示用户必须同意协议
            new android.app.AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("您需要同意用户协议才能继续使用本应用")
                    .setPositiveButton("确定", null)
                    .show();
        }
    }

    private boolean isUserAgreed() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("user_agreed", false);
    }
}