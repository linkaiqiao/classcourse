package com.example.classcourse2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class FeedbackActivity extends AppCompatActivity {

    private static final int MAX_IMAGES = 3;
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;

    private RadioGroup rgFeedbackType;
    private EditText etContent;
    private EditText etContact;
    private TextView tvWordCount;
    private LinearLayout llImageContainer;
    private Button btnSubmit;

    private List<Uri> selectedImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

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

        // 获取视图
        //rgFeedbackType = findViewById(R.id.rgFeedbackType);
        etContent = findViewById(R.id.etContent);
        etContact = findViewById(R.id.etContact);
        tvWordCount = findViewById(R.id.tvWordCount);
        llImageContainer = findViewById(R.id.llImageContainer);
        btnSubmit = findViewById(R.id.btnSubmit);

        // 设置文字变化监听
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateWordCount();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 添加图片按钮点击
        View btnAddImage = findViewById(R.id.btnAddImage);
        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImages.size() < MAX_IMAGES) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(FeedbackActivity.this,
                            "最多只能上传" + MAX_IMAGES + "张图片",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 提交按钮点击
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFeedback();
            }
        });
    }

    /**
     * 更新字数统计
     */
    private void updateWordCount() {
        int count = etContent.getText().toString().length();
        tvWordCount.setText(count + "/500");

        if (count > 500) {
            tvWordCount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            tvWordCount.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    /**
     * 从相册选择图片
     */
    private void pickImageFromGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "无法打开相册", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                addImageToContainer(imageUri);
            }
        }
    }

    /**
     * 添加图片到容器
     */
    private void addImageToContainer(Uri imageUri) {
        if (selectedImages.size() >= MAX_IMAGES) {
            return;
        }

        selectedImages.add(imageUri);

        // 创建图片项
        LinearLayout imageItem = new LinearLayout(this);
        imageItem.setOrientation(LinearLayout.VERTICAL);
        imageItem.setLayoutParams(new LinearLayout.LayoutParams(
                100, 100));

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageURI(imageUri);
        imageView.setBackgroundResource(R.drawable.bg_rounded_white);

        // 删除按钮
        ImageView deleteButton = new ImageView(this);
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(24, 24));
        deleteButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llImageContainer.removeView(imageItem);
                selectedImages.remove(imageUri);
            }
        });

        imageItem.addView(imageView);
        imageItem.addView(deleteButton);

        // 添加到容器
        llImageContainer.addView(imageItem, 1);
    }

    /**
     * 提交反馈
     */
    private void submitFeedback() {
        // 获取反馈类型
        String feedbackType = "功能建议";
        int selectedId = rgFeedbackType.getCheckedRadioButtonId();
        if (selectedId == R.id.rbProblem) {
            feedbackType = "问题反馈";
        } else if (selectedId == R.id.rbOther) {
            feedbackType = "其他";
        }

        // 获取反馈内容
        String content = etContent.getText().toString().trim();
        if (content.length() < 10) {
            etContent.setError("反馈内容不能少于10个字");
            etContent.requestFocus();
            return;
        }

        if (content.length() > 500) {
            etContent.setError("反馈内容不能超过500个字");
            etContent.requestFocus();
            return;
        }

        // 获取联系方式
        String contact = etContact.getText().toString().trim();

        // 这里应该将反馈提交到服务器
        // 暂时模拟提交成功
        simulateSubmitFeedback(feedbackType, content, contact);
    }

    /**
     * 模拟提交反馈
     */
    private void simulateSubmitFeedback(String type, String content, String contact) {
        // 显示加载中
        btnSubmit.setEnabled(false);
        btnSubmit.setText("提交中...");

        // 模拟网络请求延迟
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btnSubmit.setEnabled(true);
                btnSubmit.setText("提交");

                // 显示成功提示
                new android.app.AlertDialog.Builder(FeedbackActivity.this)
                        .setTitle("提交成功")
                        .setMessage("感谢您的反馈！我们会认真考虑您的建议。")
                        .setPositiveButton("确定", (dialog, which) -> {
                            finish();
                        })
                        .show();

                // 在实际应用中，这里应该将数据发送到服务器
                // sendToServer(type, content, contact, selectedImages);
            }
        }, 1500);
    }

    /**
     * 发送反馈到服务器（示例代码）
     */
    private void sendToServer(String type, String content, String contact, List<Uri> images) {
        // 这里需要根据你的后端API实现
        // 可能需要使用Retrofit、Volley等网络库
    }

    @Override
    public void onBackPressed() {
        // 检查是否有未保存的内容
        String content = etContent.getText().toString().trim();
        if (!content.isEmpty()) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("您有未提交的反馈内容，确定要离开吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        super.onBackPressed();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}