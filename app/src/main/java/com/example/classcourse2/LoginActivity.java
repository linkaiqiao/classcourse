package com.example.classcourse2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword, etCaptcha;
    private TextView tvCaptcha, tvRegister;
    private Button btnLogin;
    private CourseDbHelper dbHelper; // 数据库帮助类
    private String currentCaptcha; // 当前验证码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = CourseDbHelper.getInstance(this);

        // 调试：检查数据库状态
        //dbHelper.debugUsersTable();

        initViews();
        setupClickListeners();
        generateCaptcha(); // 生成初始验证码

        // 检查是否有传递过来的学号
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("student_id")) {
            String studentId = intent.getStringExtra("student_id");
            etUsername.setText(studentId);
        }
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etCaptcha = findViewById(R.id.etCaptcha);
        tvCaptcha = findViewById(R.id.tvCaptcha);
        tvRegister = findViewById(R.id.tvRegister);
        btnLogin = findViewById(R.id.btnLogin);
//        tvRegister.setOnClickListener(v -> {
//            // 这里可以跳转到注册页面
//            Intent intent=new Intent(this,RegisterActivity.class);
//            startActivity(intent);
//            //Toast.makeText(LoginActivity.this, "", Toast.LENGTH_SHORT).show();
//        });
    }

    private void setupClickListeners() {
        // 验证码点击刷新
        tvCaptcha.setOnClickListener(v -> {
            generateCaptcha();
        });

        // 登录按钮点击
        btnLogin.setOnClickListener(v -> {
            attemptLogin();
        });

        // 注册链接点击
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("LoginActivity", "注册链接被点击");

                // 跳转到注册页面
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);

                // 显示调试信息
                Toast.makeText(LoginActivity.this, "跳转到注册页面", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * 生成随机验证码（4位数字）
     */
    private void generateCaptcha() {
        Random random = new Random();
        StringBuilder captcha = new StringBuilder();
        // 生成4位随机数字
        for (int i = 0; i < 4; i++) {
            captcha.append(random.nextInt(10));
        }
        currentCaptcha = captcha.toString();
        tvCaptcha.setText(currentCaptcha);
        // 设置干扰线背景
        setCaptchaBackground();
    }

    /**
     * 设置验证码背景干扰线
     */
    private void setCaptchaBackground() {
        // 这里可以添加更复杂的验证码样式，比如干扰线、扭曲等
        // 简单实现：设置随机颜色
        Random random = new Random();
        int color = Color.argb(30,
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256));
        tvCaptcha.setBackgroundColor(color);
    }

    /**
     * 尝试登录
     */
    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String captcha = etCaptcha.getText().toString().trim();

        // 验证输入是否为空
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("请输入账号");
            etUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("请输入密码");
            etPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(captcha)) {
            etCaptcha.setError("请输入验证码");
            etCaptcha.requestFocus();
            return;
        }

        // 验证验证码是否正确
        if (!captcha.equals(currentCaptcha)) {
            etCaptcha.setError("验证码错误");
            etCaptcha.requestFocus();
            generateCaptcha(); // 刷新验证码
            return;
        }

        // 添加调试信息
        Log.d("LoginDebug", "尝试登录 - 学号: " + username + ", 密码: " + password);
        //dbHelper.debugUsersTable(); // 调试输出用户表内容

        // 验证登录信息
        if (validateLogin(username, password)) {
            // 登录成功，跳转到主页面
            loginSuccess();
        } else {
            // 登录失败
            loginFailed();
        }
    }

    /**
     * 验证登录信息（这里使用模拟数据，实际应该与服务器交互）
     */
    private boolean validateLogin(String username, String password) {
        boolean isValid = dbHelper.validateUser(username, password);
        return isValid;
        //return "admin".equals(username) && "123456".equals(password);
    }

    /**
     * 登录成功处理
     */
    private void loginSuccess() {
        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();

        // 跳转到主页面
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // 关闭登录页面
    }

    /**
     * 登录失败处理
     */
    private void loginFailed() {
        Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show();
        etPassword.setText("");
        etCaptcha.setText("");
        generateCaptcha(); // 刷新验证码
        etPassword.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次回到登录页面时刷新验证码
        generateCaptcha();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 不需要手动关闭数据库，单例会管理生命周期
    }
}