package com.example.classcourse2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.classcourse2.MainActivity;
import com.example.classcourse2.R;

public class ProfileActivity extends AppCompatActivity {
private ImageButton btnback;
private Button btnprofile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        // 初始化视图
        initViews();
        //setupBottomNavigation();
        setupClickListeners();
    }

    private void initViews() {
        // 获取视图组件
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        LinearLayout btnLoginRegister = findViewById(R.id.btnLoginRegister);
         btnback =findViewById(R.id.btnback);
         btnprofile=findViewById(R.id.btnprofile);

        // 设置按钮点击监听
        btnSettings.setOnClickListener(v -> {
            // 跳转到设置页面
            Toast.makeText(this, "设置功能开发中", Toast.LENGTH_SHORT).show();
        });

        btnLoginRegister.setOnClickListener(v -> {
            // 跳转到登录/注册页面
            Toast.makeText(this, "登录/注册功能开发中", Toast.LENGTH_SHORT).show();
        });
        btnback.setOnClickListener(v -> {

            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);

});
        btnprofile.setOnClickListener(v -> {
            // 跳转到登录/注册页面
            Intent intent=new Intent(this,LoginActivity.class);
            startActivity(intent);

        });

    }

    private void setupClickListeners() {

        findViewById(R.id.itemCustomerService).setOnClickListener(v -> {
            Intent intent=new Intent(this,CustomerServiceActivity.class);
            startActivity(intent);
            //Toast.makeText(this, "联系客服功能开发中", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.itemHelp).setOnClickListener(v -> {
           Intent intent=new Intent(this,HelpActivity.class);
           startActivity(intent);
            //Toast.makeText(this, "帮助说明功能开发中", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.itemFeedback).setOnClickListener(v -> {
            Intent intent=new Intent(this,FeedbackActivity.class);
            startActivity(intent);
           //Toast.makeText(this, "意见反馈功能开发中", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.itemPrivacy).setOnClickListener(v -> {
            Intent intent =new Intent(this,PrivacyActivity.class);
            startActivity(intent);
            //overridePendingTransition(android.R.anim.slide_in_right, android.R.anim.slide_out_left);
            //Toast.makeText(this, "隐私政策功能开发中", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.itemAgreement).setOnClickListener(v -> {
            Intent intent=new Intent(this,AgreementActivity.class);
            startActivity(intent);
            //Toast.makeText(this, "用户协议功能开发中", Toast.LENGTH_SHORT).show();
        });
    }

//    private void setupBottomNavigation() {
//        LinearLayout navSchedule = findViewById(R.id.nav_schedule);
//        LinearLayout navProfile = findViewById(R.id.nav_profile);
//
//        // 设置当前选中的导航项
//        setNavItemSelected(navSchedule, false);
//
//        setNavItemSelected(navProfile, true);
//
//        // 设置点击事件
//        navSchedule.setOnClickListener(v -> {
//            // 跳转到课程表页面
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        });
//
//
//
//        navProfile.setOnClickListener(v -> {
//            // 当前页面，不需要操作
//        });
//    }
//
//    private void setNavItemSelected(LinearLayout navItem, boolean selected) {
//        View iconView = navItem.getChildAt(0);
//        TextView textView = (TextView) navItem.getChildAt(1);
//
//        if (selected) {
//            // 已选中状态
//            if (iconView instanceof ImageButton) {
//                ((ImageButton) iconView).setColorFilter(getResources().getColor(R.color.primary_color));
//            } else if (iconView instanceof ImageView) {
//                ((ImageView) iconView).setColorFilter(getResources().getColor(R.color.primary_color));
//            }
//            textView.setTextColor(getResources().getColor(R.color.primary_color));
//            textView.setTypeface(textView.getTypeface(), android.graphics.Typeface.BOLD);
//        } else {
//            // 未选中状态
//            if (iconView instanceof ImageButton) {
//                ((ImageButton) iconView).setColorFilter(getResources().getColor(R.color.nav_unselected));
//            } else if (iconView instanceof ImageView) {
//                ((ImageView) iconView).setColorFilter(getResources().getColor(R.color.nav_unselected));
//            }
//            textView.setTextColor(getResources().getColor(R.color.nav_unselected));
//            textView.setTypeface(textView.getTypeface(), android.graphics.Typeface.NORMAL);
//        }
//    }
//}\
}