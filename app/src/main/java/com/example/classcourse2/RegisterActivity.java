package com.example.classcourse2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etStudentId, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;
    private CourseDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = CourseDbHelper.getInstance(this);
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etStudentId = findViewById(R.id.etStudentId);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void setupClickListeners() {
        // 注册按钮点击
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });

        // 返回登录链接点击
        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回登录页面
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * 尝试注册
     */
    private void attemptRegister() {
        String studentId = etStudentId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 验证输入是否为空
        if (TextUtils.isEmpty(studentId)) {
            etStudentId.setError("请输入账号");
            etStudentId.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("请输入密码");
            etPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("请确认密码");
            etConfirmPassword.requestFocus();
            return;
        }

        // 验证密码是否一致
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("两次输入的密码不一致");
            etConfirmPassword.requestFocus();
            return;
        }

        // 验证密码长度（可选）
        if (password.length() < 6) {
            etPassword.setError("密码长度至少6位");
            etPassword.requestFocus();
            return;
        }

        // 执行注册
        if (dbHelper.registerUser(studentId, password)) {
            // 注册成功
            Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();

            // 返回登录页面，并传递学号
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("student_id", studentId);
            startActivity(intent);
            finish();
        } else {
            // 注册失败（用户名已存在）
            etStudentId.setError("该账号已存在");
            etStudentId.requestFocus();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 不需要手动关闭数据库
    }
    }

