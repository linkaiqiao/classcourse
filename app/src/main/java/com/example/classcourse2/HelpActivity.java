package com.example.classcourse2;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    private LinearLayout llSearchBar;
    private EditText etSearch;
    private boolean isSearchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

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

        // 搜索按钮
        ImageButton btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSearchMode();
            }
        });

        // 搜索相关视图
        llSearchBar = findViewById(R.id.llSearchBar);
        etSearch = findViewById(R.id.etSearch);
        Button btnCancelSearch = findViewById(R.id.btnCancelSearch);

        // 搜索框文本变化监听
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 取消搜索按钮
        btnCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSearchMode();
            }
        });

        // 快速入门项目点击
        setupQuickStartItems();

        // 常见问题点击
        setupFAQItems();

        // 视频教程点击
        setupTutorialItems();

        // 联系支持按钮
        Button btnContactSupport = findViewById(R.id.btnContactSupport);
        btnContactSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactSupport();
            }
        });
    }

    /**
     * 切换搜索模式
     */
    private void toggleSearchMode() {
        isSearchMode = !isSearchMode;

        if (isSearchMode) {
            llSearchBar.setVisibility(View.VISIBLE);
            etSearch.requestFocus();
        } else {
            llSearchBar.setVisibility(View.GONE);
            etSearch.setText("");
            // 重置所有内容显示
            resetContentVisibility();
        }
    }

    /**
     * 执行搜索
     */
    private void performSearch(String query) {
        if (query.isEmpty()) {
            resetContentVisibility();
            return;
        }

        // 根据搜索关键词显示相关内容
        boolean foundMatch = false;

        // 搜索快速入门项目
        foundMatch |= searchQuickStartItems(query);

        // 搜索常见问题
        foundMatch |= searchFAQItems(query);

        // 搜索视频教程
        foundMatch |= searchTutorialItems(query);

        if (!foundMatch) {
            Toast.makeText(this, "未找到相关帮助内容", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 重置所有内容显示
     */
    private void resetContentVisibility() {
        // 显示所有快速入门项目
        findViewById(R.id.itemCreateSchedule).setVisibility(View.VISIBLE);
        findViewById(R.id.itemAddCourse).setVisibility(View.VISIBLE);
        findViewById(R.id.itemWeekNavigation).setVisibility(View.VISIBLE);

        // 显示所有常见问题
        findViewById(R.id.faq1).setVisibility(View.VISIBLE);
        findViewById(R.id.faq2).setVisibility(View.VISIBLE);
        findViewById(R.id.faq3).setVisibility(View.VISIBLE);
        findViewById(R.id.faq4).setVisibility(View.VISIBLE);

        // 显示所有视频教程
//        findViewById(R.id.tutorial1).setVisibility(View.VISIBLE);
//        findViewById(R.id.tutorial2).setVisibility(View.VISIBLE);
    }

    /**
     * 搜索快速入门项目
     */
    private boolean searchQuickStartItems(String query) {
        boolean found = false;
        String lowerQuery = query.toLowerCase();

        // 创建和管理课表
        LinearLayout itemCreateSchedule = findViewById(R.id.itemCreateSchedule);
        String createScheduleText = ((TextView) itemCreateSchedule.findViewById(android.R.id.text1)).getText().toString().toLowerCase();
        if (createScheduleText.contains(lowerQuery) || lowerQuery.contains("课表") || lowerQuery.contains("创建")) {
            itemCreateSchedule.setVisibility(View.VISIBLE);
            found = true;
        } else {
            itemCreateSchedule.setVisibility(View.GONE);
        }

        // 添加和编辑课程
        LinearLayout itemAddCourse = findViewById(R.id.itemAddCourse);
        String addCourseText = ((TextView) itemAddCourse.findViewById(android.R.id.text1)).getText().toString().toLowerCase();
        if (addCourseText.contains(lowerQuery) || lowerQuery.contains("课程") || lowerQuery.contains("添加")) {
            itemAddCourse.setVisibility(View.VISIBLE);
            found = true;
        } else {
            itemAddCourse.setVisibility(View.GONE);
        }

        // 周数导航功能
        LinearLayout itemWeekNavigation = findViewById(R.id.itemWeekNavigation);
        String weekNavText = ((TextView) itemWeekNavigation.findViewById(android.R.id.text1)).getText().toString().toLowerCase();
        if (weekNavText.contains(lowerQuery) || lowerQuery.contains("周数") || lowerQuery.contains("导航")) {
            itemWeekNavigation.setVisibility(View.VISIBLE);
            found = true;
        } else {
            itemWeekNavigation.setVisibility(View.GONE);
        }

        return found;
    }

    /**
     * 搜索常见问题
     */
    private boolean searchFAQItems(String query) {
        boolean found = false;
        String lowerQuery = query.toLowerCase();

        // FAQ 1
        LinearLayout faq1 = findViewById(R.id.faq1);
        String faq1Text = ((TextView) faq1.findViewById(android.R.id.text1)).getText().toString().toLowerCase();
        if (faq1Text.contains(lowerQuery) || lowerQuery.contains("创建") || lowerQuery.contains("课表")) {
            faq1.setVisibility(View.VISIBLE);
            found = true;
        } else {
            faq1.setVisibility(View.GONE);
        }

        // FAQ 2
        LinearLayout faq2 = findViewById(R.id.faq2);
        String faq2Text = ((TextView) faq2.findViewById(android.R.id.text1)).getText().toString().toLowerCase();
        if (faq2Text.contains(lowerQuery) || lowerQuery.contains("冲突") || lowerQuery.contains("时间")) {
            faq2.setVisibility(View.VISIBLE);
            found = true;
        } else {
            faq2.setVisibility(View.GONE);
        }

        // FAQ 3
        LinearLayout faq3 = findViewById(R.id.faq3);
        String faq3Text = ((TextView) faq3.findViewById(android.R.id.text1)).getText().toString().toLowerCase();
        if (faq3Text.contains(lowerQuery) || lowerQuery.contains("切换") || lowerQuery.contains("课表")) {
            faq3.setVisibility(View.VISIBLE);
            found = true;
        } else {
            faq3.setVisibility(View.GONE);
        }

        // FAQ 4
        LinearLayout faq4 = findViewById(R.id.faq4);
        String faq4Text = ((TextView) faq4.findViewById(android.R.id.text1)).getText().toString().toLowerCase();
        if (faq4Text.contains(lowerQuery) || lowerQuery.contains("数据") || lowerQuery.contains("云端")) {
            faq4.setVisibility(View.VISIBLE);
            found = true;
        } else {
            faq4.setVisibility(View.GONE);
        }

        return found;
    }

    /**
     * 搜索视频教程
     */
    private boolean searchTutorialItems(String query) {
        boolean found = false;
        String lowerQuery = query.toLowerCase();

        // 教程1
//        LinearLayout tutorial1 = findViewById(R.id.tutorial1);
//        String tutorial1Text = ((TextView) tutorial1.getChildAt(1)).getText().toString().toLowerCase();
//        if (tutorial1Text.contains(lowerQuery) || lowerQuery.contains("基础") || lowerQuery.contains("操作")) {
//            tutorial1.setVisibility(View.VISIBLE);
//            found = true;
//        } else {
//            tutorial1.setVisibility(View.GONE);
//        }
//
//        // 教程2
//        LinearLayout tutorial2 = findViewById(R.id.tutorial2);
//        String tutorial2Text = ((TextView) tutorial2.getChildAt(1)).getText().toString().toLowerCase();
//        if (tutorial2Text.contains(lowerQuery) || lowerQuery.contains("高级") || lowerQuery.contains("功能")) {
//            tutorial2.setVisibility(View.VISIBLE);
//            found = true;
//        } else {
//            tutorial2.setVisibility(View.GONE);
//        }

        return found;
    }

    /**
     * 设置快速入门项目点击事件
     */
    private void setupQuickStartItems() {
        // 创建和管理课表
        findViewById(R.id.itemCreateSchedule).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpDetail("创建和管理课表",
                        "创建课表：\n" +
                                "1. 点击右上角的'课表管理'按钮\n" +
                                "2. 选择'添加新课表'\n" +
                                "3. 输入课表名称、学年和学期信息\n" +
                                "4. 点击'添加'完成创建\n\n" +
                                "管理课表：\n" +
                                "• 切换课表：在课表管理页面点击要使用的课表\n" +
                                "• 删除课表：长按课表项选择删除（至少保留一个课表）\n" +
                                "• 编辑课表：点击课表名称进行修改");
            }
        });

        // 添加和编辑课程
        findViewById(R.id.itemAddCourse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpDetail("添加和编辑课程",
                        "添加课程：\n" +
                                "1. 点击右下角的'+'按钮\n" +
                                "2. 填写课程名称、上课地点\n" +
                                "3. 选择上课日期和时间段\n" +
                                "4. 设置课程周数\n" +
                                "5. 点击'添加'完成\n\n" +
                                "编辑课程：\n" +
                                "• 点击课程卡片查看详情\n" +
                                "• 长按课程卡片选择'编辑课程'\n" +
                                "• 修改信息后点击'保存'\n\n" +
                                "删除课程：\n" +
                                "• 长按课程卡片选择'删除课程'");
            }
        });

        // 周数导航功能
        findViewById(R.id.itemWeekNavigation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpDetail("周数导航功能",
                        "周数导航功能让您可以查看不同教学周的课程安排：\n\n" +
                                "导航控制：\n" +
                                "• 上一周：点击左侧箭头按钮\n" +
                                "• 下一周：点击右侧箭头按钮\n" +
                                "• 回到本周：点击中间的'回到本周'按钮\n\n" +
                                "自动计算：\n" +
                                "• 系统会根据学期开始日期自动计算当前周数\n" +
                                "• 支持第1周到第20周的课程显示\n\n" +
                                "日期显示：\n" +
                                "• 星期标题会显示对应周的具体日期\n" +
                                "• 方便您了解课程的具体时间安排");
            }
        });
    }

    /**
     * 设置常见问题点击事件
     */
    private void setupFAQItems() {
        // 问题1：如何创建新的课表？
        findViewById(R.id.faq1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpDetail("如何创建新的课表？",
                        "创建新课表的步骤：\n\n" +
                                "1. 进入应用主界面\n" +
                                "2. 点击左上角的'课表管理'按钮（日历图标）\n" +
                                "3. 在弹出的课表管理页面中，点击'添加新课表'\n" +
                                "4. 填写以下信息：\n" +
                                "   • 课表名称（如：大二上学期）\n" +
                                "   • 学年（如：2024-2025）\n" +
                                "   • 学期（第1学期/第2学期等）\n" +
                                "5. 点击'添加'按钮完成创建\n\n" +
                                "提示：您可以创建多个课表来管理不同学期或不同类型的课程。");
            }
        });

        // 问题2：课程时间冲突怎么办？
        findViewById(R.id.faq2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpDetail("课程时间冲突怎么办？",
                        "当您尝试添加的课程与现有课程时间冲突时：\n\n" +
                                "系统会自动检测冲突：\n" +
                                "• 同一时间段不能安排两门课程\n" +
                                "• 系统会提示'该时间段已有课程'\n\n" +
                                "解决方案：\n" +
                                "1. 调整新课程的时间段\n" +
                                "2. 修改或删除原有冲突的课程\n" +
                                "3. 确认课程信息是否正确\n\n" +
                                "预防措施：\n" +
                                "• 添加课程前先查看该时间段的课程安排\n" +
                                "• 合理安排课程时间，避免重叠");
            }
        });

        // 问题3：如何切换不同的课表？
        findViewById(R.id.faq3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpDetail("如何切换不同的课表？",
                        "切换课表的方法：\n\n" +
                                "快速切换：\n" +
                                "1. 点击主界面顶部的课表名称\n" +
                                "2. 在弹出的课表列表中点击要切换的课表\n" +
                                "3. 系统会自动加载该课表的课程信息\n\n" +
                                "通过课表管理切换：\n" +
                                "1. 点击左上角的'课表管理'按钮\n" +
                                "2. 在课表列表中找到目标课表\n" +
                                "3. 点击该课表项即可切换\n\n" +
                                "提示：切换课表后，所有操作都会针对当前选中的课表进行。");
            }
        });

        // 问题4：数据会同步到云端吗？
        findViewById(R.id.faq4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpDetail("数据会同步到云端吗？",
                        "当前版本的数据存储方式：\n\n" +
                                "本地存储：\n" +
                                "• 所有课程数据都存储在设备本地\n" +
                                "• 不会自动同步到云端服务器\n\n" +
                                "数据安全：\n" +
                                "• 请定期备份重要数据\n" +
                                "• 避免清除应用数据或卸载应用\n\n" +
                                "未来计划：\n" +
                                "• 我们正在开发云端同步功能\n" +
                                "• 未来版本将支持多设备数据同步\n\n" +
                                "注意事项：\n" +
                                "• 更换设备时需要手动导出导入数据\n" +
                                "• 建议重要课程信息做好本地备份");
            }
        });
    }

    /**
     * 设置视频教程点击事件
     */
    private void setupTutorialItems() {
        // 教程1：基础操作指南
//        findViewById(R.id.tutorial1).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showVideoTutorial("基础操作指南",
//                        "视频内容概要：\n\n" +
//                                "📋 课程表应用基础操作\n" +
//                                "• 应用界面介绍和功能区域说明\n" +
//                                "• 如何快速添加第一门课程\n" +
//                                "• 课程信息的查看和编辑方法\n" +
//                                "• 基本的周数导航操作\n\n" +
//                                "⏱️ 时长：3分钟\n" +
//                                "🎯 适合人群：新用户入门\n\n" +
//                                "提示：建议按顺序观看所有教程以获得最佳学习效果。");
//            }
//        });
//
//        // 教程2：高级功能详解
//        findViewById(R.id.tutorial2).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showVideoTutorial("高级功能详解",
//                        "视频内容概要：\n\n" +
//                                "⚙️ 高级功能全面解析\n" +
//                                "• 多课表管理和快速切换技巧\n" +
//                                "• 课程冲突检测和解决方法\n" +
//                                "• 学期设置和周数计算原理\n" +
//                                "• 数据备份和恢复的最佳实践\n\n" +
//                                "⏱️ 时长：5分钟\n" +
//                                "🎯 适合人群：有一定使用经验的用户\n\n" +
//                                "提示：观看前请先掌握基础操作内容。");
//            }
//        });
    }

    /**
     * 显示帮助详情对话框
     */
    private void showHelpDetail(String title, String content) {
        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton("确定", null)
                .setNeutralButton("分享", (dialog, which) -> {
                    shareHelpContent(title, content);
                })
                .show();
    }

    /**
     * 显示视频教程信息
     */
    private void showVideoTutorial(String title, String description) {
        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton("观看视频", (dialog, which) -> {
                    // 这里可以集成视频播放功能
                    Toast.makeText(HelpActivity.this, "视频播放功能开发中", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .setNeutralButton("分享", (dialog, which) -> {
                    shareHelpContent(title, description);
                })
                .show();
    }

    /**
     * 分享帮助内容
     */
    private void shareHelpContent(String title, String content) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
            shareIntent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + content + "\n\n——来自课程表应用帮助中心");
            startActivity(Intent.createChooser(shareIntent, "分享帮助信息"));
        } catch (Exception e) {
            Toast.makeText(this, "分享失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 联系支持
     */
    private void contactSupport() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("联系支持")
                .setMessage("您可以通过以下方式联系我们：\n\n" +
                        "📞 客服电话：400-123-4567\n" +
                        "📧 客服邮箱：support@example.com\n" +
                        "🕒 服务时间：工作日 9:00-18:00\n\n" +
                        "我们将竭诚为您服务！")
                .setPositiveButton("打电话", (dialog, which) -> {
                    // 拨打电话
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(android.net.Uri.parse("tel:4001234567"));
                    startActivity(intent);
                })
                .setNeutralButton("发邮件", (dialog, which) -> {
                    // 发送邮件
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(android.net.Uri.parse("mailto:support@example.com"));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "课程表应用 - 帮助支持");
                    startActivity(intent);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (isSearchMode) {
            toggleSearchMode();
        } else {
            super.onBackPressed();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }
}