package     com.example.classcourse2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class PrivacyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

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

        // 可以添加其他交互功能，比如复制政策内容等
        setupContentInteraction();
    }

    private void setupContentInteraction() {
        // 可以添加长按复制等功能
        View contentView = findViewById(R.id.tvPrivacyContent);
        contentView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 复制政策内容到剪贴板
                copyToClipboard();
                return true;
            }
        });
    }

    private void copyToClipboard() {
        try {
            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(
                    "隐私政策",
                    getString(R.string.privacy_policy_content)
            );
            clipboard.setPrimaryClip(clip);

            // 显示复制成功提示
            android.widget.Toast.makeText(this, "隐私政策内容已复制到剪贴板",
                    android.widget.Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        // 可以添加返回时的特殊处理
        super.onBackPressed();

        // 添加页面切换动画
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}