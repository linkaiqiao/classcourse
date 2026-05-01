package com.example.classcourse2;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LinearLayout llCourseTableContainer;
    private AlarmManager alarmManager;

    private CourseDbHelper.PeriodSetting currentPeriodSetting;
    private static final int SWIPE_MIN_DISTANCE = 60;    // 最小滑动距离
    private static final int SWIPE_THRESHOLD_VELOCITY = 100 ;  // 最小滑动速度
    private static final float SWIPE_MIN_DISTANCE_RATIO = 0.1f;  // 新增：基于屏幕宽度的最小距离比例

    private GestureDetector gestureDetector;
    private View.OnTouchListener gestureListener;
    private TextView swipeHintText;
    private PopupMenu settingsPopupMenu;
    private ImageButton btnSettings;

    private TextView tvWeekInfo;
    private TextView tvSemesterInfo;
    private CourseDbHelper dbHelper;
    private List<Course> courses = new ArrayList<>();
    private List<Schedule> schedules = new ArrayList<>();
    private long[] weekDaysMillis = new long[5];
    private Schedule currentSchedule; // 当前显示的课表
    private int currentWeek = 1; // 当前周数

    private TextView tvMonday, tvTuesday, tvWednesday, tvThursday, tvFriday;
    private TextView tvCurrentScheduleName;
    private CourseDbHelper.TimeSetting currentTimeSetting;
    private TimePicker tpMorningStartTime;
    private TimePicker tpAfternoonStartTime;
    private TimePicker tpEveningStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = CourseDbHelper.getInstance(this);
        initViews();
        setupWeekNavigation();
        checkDatabaseState();
        setupAddCourseButton();
        setupScheduleManagementButton();
        loadCurrentSchedule();
        displayWeeklyCourseTable();
        setupBottomNavigation();
        loadTimeSettings();
        loadPeriodSettings();
        setupCourseTableSwipeGesture();  // 课程表容器设置手势
        initReminderSettings();
        setupSettingsDropdown();
    }
    private void setupSettingsDropdown() {
        btnSettings = findViewById(R.id.btnSettings);

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                showSettingsDropdown(v);
            });
        }
    }

    // 4. 显示设置下拉菜单
    private void showSettingsDropdown(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.settings_menu, popupMenu.getMenu());

        // 设置菜单项点击事件
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_period_settings) {
                // 课程节数设置
                if (currentSchedule != null) {
                    showPeriodSettingDialog();
                } else {
                    Toast.makeText(this, "请先选择课表", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (id == R.id.menu_time_settings) {
                // 上课时间设置
                showTimeSettingDialog();
                return true;
            } else if (id == R.id.menu_reminder_settings) {
                // 提醒设置
                showAllRemindersDialog();
                return true;
            }
//            } else if (id == R.id.menu_about) {
//                // 关于
//                showAboutDialog();
//                return true;
//            }
            return false;
        });

        popupMenu.show();
    }
    private void showAllRemindersDialog() {
        if (currentSchedule == null) {
            Toast.makeText(this, "请先选择课表", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取所有设置了提醒的课程
        List<Course> tempCoursesWithReminders = new ArrayList<>();

        try {
            // 确保数据库表存在
            if (dbHelper != null) {
                tempCoursesWithReminders = dbHelper.getCoursesWithReminders(currentSchedule.id);
            }
        } catch (Exception e) {
            Log.e("ReminderDialog", "获取提醒课程失败: " + e.getMessage());
            Toast.makeText(this, "获取提醒设置失败，请重试", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提醒设置管理");
        builder.setIcon(R.drawable.ic_notification);
        List<Course> coursesWithReminders = tempCoursesWithReminders;
        if (coursesWithReminders.isEmpty()) {
            // 没有设置提醒的课程
            View emptyView = getLayoutInflater().inflate(R.layout.dialog_no_reminders, null);
            builder.setView(emptyView);

            builder.setPositiveButton("关闭", (dialog, which) -> {
                dialog.dismiss();
            });

            // 可选：添加"去设置"按钮
            builder.setNegativeButton("去设置", (dialog, which) -> {
                // 可以引导用户去设置提醒
                Toast.makeText(this, "请在课程表界面长按课程进行设置", Toast.LENGTH_SHORT).show();
            });
        } else {
            // 有提醒设置的课程，显示列表
            String[] items = new String[coursesWithReminders.size()];
            for (int i = 0; i < coursesWithReminders.size(); i++) {
                Course course = coursesWithReminders.get(i);
                CourseDbHelper.ReminderSetting setting = dbHelper.getReminderSettingForCourse(course.id);

                // 格式化显示信息
                String startTime = getStartTime(course.startSlot);
                String endTime = getEndTime(course.endSlot);
                String reminderStatus = (setting != null && setting.isEnabled) ? "已开启" : "已关闭";
                int remindMinutes = (setting != null) ? setting.remindMinutes : 10;

                items[i] = String.format("%s\n%s %s - %s\n提前%d分钟提醒 (%s)",
                        course.name,
                        getWeekdayFromDate(course.date),
                        startTime, endTime,
                        remindMinutes,
                        reminderStatus);
            }


            builder.setItems(items, (dialog, which) -> {
                // 点击课程项，进入该课程的提醒设置
                showReminderSettingDialog(coursesWithReminders.get(which));
            });

            // 添加操作按钮
            builder.setPositiveButton("关闭", (dialog, which) -> {
                dialog.dismiss();
            });

            // 可以添加批量操作
            builder.setNeutralButton("批量操作", (dialog, which) -> {
                showBatchReminderOperationsDialog(coursesWithReminders);
            });
        }

        // 添加统计信息
        int totalCount = coursesWithReminders.size();
        int enabledCount = 0;
        for (Course course : coursesWithReminders) {
            CourseDbHelper.ReminderSetting setting = dbHelper.getReminderSettingForCourse(course.id);
            if (setting != null && setting.isEnabled) {
                enabledCount++;
            }
        }

        String message = String.format("共%d个课程，%d个已开启提醒", totalCount, enabledCount);
        builder.setMessage(message);

        AlertDialog dialog = builder.create();
        dialog.show();

        if (!coursesWithReminders.isEmpty() && dialog.getListView() != null) {
            dialog.getListView().setDividerHeight(1);
            dialog.getListView().setDivider(getResources().getDrawable(android.R.color.darker_gray, null));
        }
        // 如果需要，可以定制对话框的样式
//        dialog.getListView().setDividerHeight(1);
//        dialog.getListView().setDivider(getResources().getDrawable(android.R.color.darker_gray, null));
    }
    // 批量操作对话框
    private void showBatchReminderOperationsDialog(List<Course> courses) {
        String[] batchOptions = {
                "全部开启提醒",
                "全部关闭提醒",
                "统一设置提前时间"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("批量操作");
        builder.setItems(batchOptions, (dialog, which) -> {
            switch (which) {
                case 0: // 全部开启提醒
                    enableAllReminders(courses, true);
                    break;
                case 1: // 全部关闭提醒
                    enableAllReminders(courses, false);
                    break;
                case 2: // 统一设置提前时间
                    showUnifiedReminderTimeDialog(courses);
                    break;
            }
        });
        builder.show();
    }
    private void enableAllReminders(List<Course> courses, boolean enable) {
        int count = 0;
        for (Course course : courses) {
            CourseDbHelper.ReminderSetting setting = dbHelper.getReminderSettingForCourse(course.id);
            if (setting == null) {
                setting = new CourseDbHelper.ReminderSetting();
                setting.courseId = course.id;
                setting.remindMinutes = 10; // 默认10分钟
            }
            setting.isEnabled = enable;
            if (dbHelper.saveReminderSetting(setting)) {
                count++;

                // 如果启用，安排提醒
                if (enable) {
                    scheduleReminder(course, setting);
                } else {
                    // 如果禁用，取消提醒
                    cancelReminder(course.id);
                }
            }
        }

        String message = String.format("已%s%d个课程的提醒", enable ? "开启" : "关闭", count);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // 刷新对话框
        showAllRemindersDialog();
    }

    // 统一设置提前时间
    private void showUnifiedReminderTimeDialog(List<Course> courses) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("统一设置提前提醒时间");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_unified_reminder, null);
        builder.setView(dialogView);

        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroupTime);
        EditText etCustomTime = dialogView.findViewById(R.id.etCustomTime);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioCustom) {
                etCustomTime.setVisibility(View.VISIBLE);
                etCustomTime.requestFocus();
            } else {
                etCustomTime.setVisibility(View.GONE);
            }
        });

        builder.setPositiveButton("确定", (dialog, which) -> {
            int minutes = 10; // 默认

            int checkedId = radioGroup.getCheckedRadioButtonId();
            if (checkedId == R.id.radio5min) {
                minutes = 5;
            } else if (checkedId == R.id.radio10min) {
                minutes = 10;
            } else if (checkedId == R.id.radio15min) {
                minutes = 15;
            } else if (checkedId == R.id.radio30min) {
                minutes = 30;
            } else if (checkedId == R.id.radioCustom && !etCustomTime.getText().toString().isEmpty()) {
                try {
                    minutes = Integer.parseInt(etCustomTime.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "请输入有效的分钟数", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (minutes < 1 || minutes > 1440) {
                Toast.makeText(this, "提前时间应在1-1440分钟之间", Toast.LENGTH_SHORT).show();
                return;
            }

            int count = 0;
            for (Course course : courses) {
                CourseDbHelper.ReminderSetting setting = dbHelper.getReminderSettingForCourse(course.id);
                if (setting == null) {
                    setting = new CourseDbHelper.ReminderSetting();
                    setting.courseId = course.id;
                    setting.isEnabled = true;
                }
                setting.remindMinutes = minutes;

                if (dbHelper.saveReminderSetting(setting)) {
                    count++;

                    // 如果提醒是启用的，重新安排提醒
                    if (setting.isEnabled) {
                        scheduleReminder(course, setting);
                    }
                }
            }

            Toast.makeText(this, String.format("已为%d个课程设置提前%d分钟提醒", count, minutes), Toast.LENGTH_SHORT).show();
            showAllRemindersDialog();
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    // 辅助方法：从日期获取星期
    private String getWeekdayFromDate(long dateMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateMillis);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        String[] weekdays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        return weekdays[dayOfWeek - 1];
    }
    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("关于课程表");
        builder.setMessage("版本: 1.0.0\n\n" +
                "功能: 课程表管理、上课提醒、自定义时间设置\n\n" +
                "开发者: Your Name");
        builder.setPositiveButton("确定", null);
        builder.show();
    }
    private void initReminderSettings() {
        // 检查并安排所有提醒
        if (currentSchedule != null) {
            scheduleAllReminders();
        }
    }
    private void setupCourseTableSwipeGesture() {
        // 获取课程表容器
        LinearLayout courseTableContainer = findViewById(R.id.llCourseTableContainer);

        if (courseTableContainer == null) {
            Log.e("SwipeGesture", "课程表容器未找到");
            return;
        }

        // 同时获取 ScrollView 和 HorizontalScrollView
        ScrollView scrollView = findViewById(R.id.scrollView);
        HorizontalScrollView horizontalScrollView = null;

        // 查找 HorizontalScrollView
        if (scrollView != null) {
            for (int i = 0; i < ((ViewGroup) scrollView).getChildCount(); i++) {
                View child = ((ViewGroup) scrollView).getChildAt(i);
                if (child instanceof HorizontalScrollView) {
                    horizontalScrollView = (HorizontalScrollView) child;
                    break;
                }
            }
        }

        Log.d("SwipeGesture", "为课程表容器设置手势识别");

        // 创建手势检测器
        // 3. 创建手势检测器
        // GestureDetector 是Android系统提供的手势识别工具类
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_MIN_DISTANCE = 60;//最小滑动距离
            private static final int SWIPE_MIN_VELOCITY = 100;//最小滑动速度

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                try {
                    //MotionEvent e1为滑动开始触摸事件（起始点）
                    //MotionEvent e2为滑动结束触摸事件（结束点）
                    if (e1 == null || e2 == null) return false;

                    float diffX = e2.getX() - e1.getX();//计算水平滑动距离。向右为正，向左为负数
                    float diffY = e2.getY() - e1.getY();//计算垂直滑动距离

                    Log.d("SwipeGesture", "课程表区域滑动: X距离=" + diffX + ", Y距离=" + diffY);

                    // 判断是否为水平滑动（水平距离大于垂直距离）
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        // 满足最小距离或速度条件
                        if (Math.abs(diffX) > SWIPE_MIN_DISTANCE || Math.abs(velocityX) > SWIPE_MIN_VELOCITY) {
                            //判断滑动距离大于最小滑动距离，判断滑动速度大于最下滑动速度
                            if (diffX > 0) {
                                //判断水平方向是向左还向右
                                Log.d("SwipeGesture", "右滑 -> 上一周");
                                switchToPreviousWeek();
                            } else {
                                Log.d("SwipeGesture", "左滑 -> 下一周");
                                switchToNextWeek();
                            }
                            return true;
                        }
                    }
                } catch (Exception e) {
                    Log.e("SwipeGesture", "手势错误: " + e.getMessage());
                }
                return false;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }
        });

        // 创建触摸监听器
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean handled = gestureDetector.onTouchEvent(event);

                // 当手势处理水平滑动时，请求父容器不要拦截触摸事件
                if (handled) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }

                // 确保 View 能响应点击事件
                v.performClick();

                return false;
            }
        };

        // 设置触摸监听器到课程表容器
        courseTableContainer.setOnTouchListener(touchListener);
        courseTableContainer.setClickable(true);
        courseTableContainer.setFocusable(true);

        // 如果 HorizontalScrollView 存在，也设置监听
        if (horizontalScrollView != null) {
            horizontalScrollView.setOnTouchListener(touchListener);
            horizontalScrollView.setClickable(true);
            horizontalScrollView.setFocusable(true);
        }
    }
    private void setupFullScreenGesture() {
        // 获取整个窗口的根视图
        View rootView = getWindow().getDecorView().getRootView();

        rootView.setOnTouchListener(new View.OnTouchListener() {
            private float startX = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        return true;

                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float diffX = endX - startX;

                        // 判断滑动距离
                        if (Math.abs(diffX) > dpToPx(80)) {
                            if (diffX > 0) {
                                // 右滑 - 上一周
                                switchToPreviousWeek();
                            } else {
                                // 左滑 - 下一周
                                switchToNextWeek();
                            }
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
    }

    private void setupSwipeGesture() {
        // 创建手势识别器
        gestureDetector = new GestureDetector(this, new GestureListener());

        // 创建触摸监听器
        gestureListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };

        // 为课程表容器设置触摸监听
        ScrollView scrollView = findViewById(R.id.scrollView);
        if (scrollView != null) {
            scrollView.setOnTouchListener(gestureListener);
        }

        // 如果没有 ScrollView，为 LinearLayout 设置
        LinearLayout mainLayout = findViewById(R.id.mainLayout);
        if (mainLayout != null) {
            mainLayout.setOnTouchListener(gestureListener);
        }
    }
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                // 计算滑动距离
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                float minDistance = screenWidth * SWIPE_MIN_DISTANCE_RATIO;
                // 如果是水平滑动，且水平距离大于垂直距离
                if (Math.abs(diffX) > Math.abs(diffY) * 1.5) {
                    boolean isLongEnough = Math.abs(diffX) > Math.min(SWIPE_MIN_DISTANCE, minDistance);
                    boolean isFastEnough = Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY;
                    // 满足最小距离和速度条件
                    if (isLongEnough || isFastEnough) {

                        if (diffX > 0) {
                            // 从左向右滑动 - 切换到上一周
                            switchToPreviousWeek();
                        } else {
                            // 从右向左滑动 - 切换到下一周
                            switchToNextWeek();
                        }
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.e("Gesture", "手势识别错误: " + e.getMessage());
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true; // 必须返回true，否则onFling不会被调用
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // 实时跟踪滑动，提供视觉反馈
            if (Math.abs(distanceX) > Math.abs(distanceY) * 1.5) {
                // 显示滑动提示
                showSwipeHint(distanceX > 0);
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }
    private void setupContentViewSwipe() {
        // 获取Activity的内容视图
        View contentView = findViewById(android.R.id.content);
        if (contentView != null) {
            contentView.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(MainActivity.this,
                        new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                                float diffX = e2.getX() - e1.getX();

                                if (Math.abs(diffX) > dpToPx(50)) {
                                    if (diffX > 0) {
                                        switchToPreviousWeek();
                                    } else {
                                        switchToNextWeek();
                                    }
                                    return true;
                                }
                                return false;
                            }

                            @Override
                            public boolean onDown(MotionEvent e) {
                                return true;
                            }
                        });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
        }
    }
    private void showSwipeHint(boolean swipeRight) {
        if (swipeHintText != null) {
            String hint = swipeRight ? "← 上一周" : "下一周 →";
            swipeHintText.setText(hint);
            swipeHintText.setVisibility(View.VISIBLE);

            // 自动隐藏
            new Handler().postDelayed(() -> {
                if (swipeHintText != null) {
                    swipeHintText.setVisibility(View.GONE);
                }
            }, 1000);
        }
    }
    // 切换到上一周
    private void switchToPreviousWeek() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int previousWeek = Math.max(1, currentWeek - 1);//计算上一周的周数，最小值为1，公式为currentWeek - 1
                // 2. 检查周数是否实际发生变化
                if (previousWeek != currentWeek) {
                    // 3. 更新当前周数状态
                    currentWeek = previousWeek;
                    updateWeekInfo();//更新显示周数信息
                    displayWeeklyCourseTable();//重新显示课程表
                    // 添加滑动动画效果
                    animateWeekTransition(false);
                }
            }
        });
    }

    // 切换到下一周
    private void switchToNextWeek() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int nextWeek = Math.min(30, currentWeek + 1);//计算下一周的周数，最大值为30，公式为currentWeek + 1
                if (nextWeek != currentWeek) {
                    //坚持周数是否实际发生变化
                    currentWeek = nextWeek;//更新当前周数
                    updateWeekInfo();//更新显示周数信息
                    displayWeeklyCourseTable();//刷新课程表

                    // 添加滑动动画效果
                    animateWeekTransition(true);
                }
            }
        });
    }
    private void animateCourseTableChange(boolean slideToLeft) {
        LinearLayout courseTableContainer = findViewById(R.id.llCourseTableContainer);
        if (courseTableContainer == null) return;

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int animationDistance = screenWidth / 6;  // 适中的动画距离

        // 设置初始位置
        courseTableContainer.setTranslationX(slideToLeft ? animationDistance : -animationDistance);
        courseTableContainer.setAlpha(0.7f);

        // 执行动画
        courseTableContainer.animate()
                .translationX(0)
                .alpha(1.0f)
                .setDuration(250)
                .setInterpolator(new OvershootInterpolator(0.5f))
                .withStartAction(() -> {
                    // 动画开始时的操作
                    //playSwipeSound();
                })
                .withEndAction(() -> {
                    // 动画结束时的清理
                    courseTableContainer.setTranslationX(0);
                })
                .start();
    }
    // 切换动画效果
    private void animateWeekTransition(boolean slideToLeft) {
        LinearLayout llCourseTableContainer = findViewById(R.id.llCourseTableContainer);
        if (llCourseTableContainer == null) return;

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int animationDistance = screenWidth / 2;

        // 初始位置
        llCourseTableContainer.setTranslationX(slideToLeft ? animationDistance : -animationDistance);
        llCourseTableContainer.setAlpha(0.3f);

        // 动画
        llCourseTableContainer.animate()
                .translationX(0)
                .alpha(1.0f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // 显示周数提示
        showWeekHint();
    }

    // 显示周数切换提示
    private void showWeekHint() {
        String message = "第" + currentWeek + "周";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // 或者使用Snackbar显示更优雅的提示
       // showCustomWeekToast(message);
    }

    // 加载节数设置
    private void loadPeriodSettings() {
        if (currentSchedule != null) {
            currentPeriodSetting = dbHelper.getPeriodSettingBySchedule(currentSchedule.id);
        }

        if (currentPeriodSetting == null) {
            currentPeriodSetting = new CourseDbHelper.PeriodSetting();
        }

        Log.d("PeriodSettings", "加载节数设置: 上午" + currentPeriodSetting.morningPeriods +
                "节, 下午" + currentPeriodSetting.afternoonPeriods + "节, 晚上" +
                currentPeriodSetting.eveningPeriods + "节, 总计" + currentPeriodSetting.totalPeriods + "节");
    }

    // 显示节数设置对话框
    private void showPeriodSettingDialog() {
        if (currentSchedule == null) {
            Toast.makeText(this, "请先选择课表", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("课程节数设置 - " + currentSchedule.name);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.period_setting_dialog, null);
        builder.setView(dialogView);

        EditText etMorningPeriods = dialogView.findViewById(R.id.etMorningPeriods);
        EditText etAfternoonPeriods = dialogView.findViewById(R.id.etAfternoonPeriods);
        EditText etEveningPeriods = dialogView.findViewById(R.id.etEveningPeriods);
        TextView tvTotalPeriods = dialogView.findViewById(R.id.tvTotalPeriods);
        TextView tvPreview = dialogView.findViewById(R.id.tvPreview);

        // 设置当前值
        etMorningPeriods.setText(String.valueOf(currentPeriodSetting.morningPeriods));
        etAfternoonPeriods.setText(String.valueOf(currentPeriodSetting.afternoonPeriods));
        etEveningPeriods.setText(String.valueOf(currentPeriodSetting.eveningPeriods));

        // 更新预览
        updatePeriodPreview(tvTotalPeriods, tvPreview,
                currentPeriodSetting.morningPeriods,
                currentPeriodSetting.afternoonPeriods,
                currentPeriodSetting.eveningPeriods);

        // 监听输入变化
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int morning = parseOrZero(etMorningPeriods.getText().toString());
                    int afternoon = parseOrZero(etAfternoonPeriods.getText().toString());
                    int evening = parseOrZero(etEveningPeriods.getText().toString());
                    updatePeriodPreview(tvTotalPeriods, tvPreview, morning, afternoon, evening);
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }

            private int parseOrZero(String text) {
                try {
                    return Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        };

        etMorningPeriods.addTextChangedListener(textWatcher);
        etAfternoonPeriods.addTextChangedListener(textWatcher);
        etEveningPeriods.addTextChangedListener(textWatcher);

        builder.setPositiveButton("保存", (dialog, which) -> {
            if (savePeriodSettings(etMorningPeriods, etAfternoonPeriods, etEveningPeriods)) {
                loadPeriodSettings();
                displayWeeklyCourseTable();
                Toast.makeText(this, "节数设置已保存，课程表已调整", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }
    // 显示时间设置对话框
    private void showTimeSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("上课时间设置");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.time_setting_dialog, null);
        builder.setView(dialogView);

        // 获取控件引用
        EditText etMorningDuration = dialogView.findViewById(R.id.etMorningDuration);
        EditText etMorningBreak = dialogView.findViewById(R.id.etMorningBreak);
        EditText etAfternoonDuration = dialogView.findViewById(R.id.etAfternoonDuration);
        EditText etAfternoonBreak = dialogView.findViewById(R.id.etAfternoonBreak);
        EditText etEveningDuration = dialogView.findViewById(R.id.etEveningDuration);
        TimePicker tpMorningStartTime = dialogView.findViewById(R.id.tpMorningStartTime);
        TimePicker tpAfternoonStartTime = dialogView.findViewById(R.id.tpAfternoonStartTime);
        TimePicker tpEveningStartTime = dialogView.findViewById(R.id.tpEveningStartTime);
        // 设置当前值
        etMorningDuration.setText(String.valueOf(currentTimeSetting.morningDuration));
        etMorningBreak.setText(String.valueOf(currentTimeSetting.morningBreak));
        etAfternoonDuration.setText(String.valueOf(currentTimeSetting.afternoonDuration));
        etAfternoonBreak.setText(String.valueOf(currentTimeSetting.afternoonBreak));
        etEveningDuration.setText(String.valueOf(currentTimeSetting.eveningDuration));

        // 设置 TimePicker 为 24 小时制
        tpMorningStartTime.setIs24HourView(true);
        tpAfternoonStartTime.setIs24HourView(true);
        tpEveningStartTime.setIs24HourView(true);

        builder.setPositiveButton("保存", (dialog, which) -> {
            // 验证并保存设置
            if (saveTimeSettings(tpMorningStartTime, etMorningDuration, etMorningBreak,
                    tpAfternoonStartTime, etAfternoonDuration, etAfternoonBreak,
                    tpEveningStartTime, etEveningDuration)) {
                loadTimeSettings();
                displayWeeklyCourseTable();
                Toast.makeText(this, "时间设置已保存", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void loadTimeSettings() {
        if (currentSchedule != null) {
            currentTimeSetting = dbHelper.getTimeSettingBySchedule(currentSchedule.id);
            Log.d("TimeSettings", "加载课表 " + currentSchedule.name + " 的时间设置");
        }

        // 如果仍然为null，创建默认设置
        if (currentTimeSetting == null) {
            Log.d("TimeSettings", "创建默认时间设置");
            currentTimeSetting = new CourseDbHelper.TimeSetting();
            currentTimeSetting.morningStartTime = "08:00";
            currentTimeSetting.morningDuration = 45;
            currentTimeSetting.morningBreak = 10;
            currentTimeSetting.afternoonStartTime = "14:00";
            currentTimeSetting.afternoonDuration = 45;
            currentTimeSetting.afternoonBreak = 10;
            currentTimeSetting.eveningStartTime = "19:00";
            currentTimeSetting.eveningDuration = 45;
            currentTimeSetting.isDefault = true;

            // 如果有当前课表，保存默认设置到数据库
            if (currentSchedule != null) {
                currentTimeSetting.scheduleId = currentSchedule.id;
                dbHelper.saveTimeSetting(currentTimeSetting);
            }
        }

        Log.d("TimeSettings", "当前时间设置: " +
                "上午 " + currentTimeSetting.morningStartTime +
                ", 下午 " + currentTimeSetting.afternoonStartTime +
                ", 晚上 " + currentTimeSetting.eveningStartTime);
    }

    // 更新预览显示
    private void updatePeriodPreview(TextView tvTotal, TextView tvPreview,
                                     int morning, int afternoon, int evening) {
        int total = morning + afternoon + evening;
        tvTotal.setText("总计：" + total + "节课");

        StringBuilder preview = new StringBuilder("预览：");
        if (morning > 0) {
            preview.append("上午").append(morning).append("节 (1-").append(morning).append(") ");
        }
        if (afternoon > 0) {
            int start = morning + 1;
            int end = morning + afternoon;
            preview.append("下午").append(afternoon).append("节 (").append(start).append("-").append(end).append(") ");
        }
        if (evening > 0) {
            int start = morning + afternoon + 1;
            int end = morning + afternoon + evening;
            preview.append("晚上").append(evening).append("节 (").append(start).append("-").append(end).append(")");
        }
        tvPreview.setText(preview.toString());
    }

    // 保存节数设置
    private boolean savePeriodSettings(EditText etMorning, EditText etAfternoon, EditText etEvening) {
        try {
            int morning = Integer.parseInt(etMorning.getText().toString());
            int afternoon = Integer.parseInt(etAfternoon.getText().toString());
            int evening = Integer.parseInt(etEvening.getText().toString());
            int total = morning + afternoon + evening;

            // 验证输入
            if (morning < 0 || afternoon < 0 || evening < 0) {
                Toast.makeText(this, "节数不能为负数", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (total > 20) {
                Toast.makeText(this, "总节数不能超过20节", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (total == 0) {
                Toast.makeText(this, "至少需要设置一节课程", Toast.LENGTH_SHORT).show();
                return false;
            }

            // 保存设置
            currentPeriodSetting.morningPeriods = morning;
            currentPeriodSetting.afternoonPeriods = afternoon;
            currentPeriodSetting.eveningPeriods = evening;
            currentPeriodSetting.totalPeriods = total;
            currentPeriodSetting.scheduleId = currentSchedule.id;

            boolean success = dbHelper.savePeriodSetting(currentPeriodSetting);

            if (success) {
                Log.d("PeriodSettings", "保存成功: 上午" + morning + "节, 下午" + afternoon +
                        "节, 晚上" + evening + "节, 总计" + total + "节");
            }

            return success;

        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    private void setupBottomNavigation() {
        LinearLayout navSchedule = findViewById(R.id.nav_schedule);
        LinearLayout navMessage = findViewById(R.id.nav_message);
        LinearLayout navProfile = findViewById(R.id.nav_profile);

        setNavItemSelected(navSchedule, true);
        setNavItemSelected(navMessage, false);
        setNavItemSelected(navProfile, false);

        navSchedule.setOnClickListener(v -> {
            setNavItemSelected(navSchedule, true);
            setNavItemSelected(navMessage, false);
            setNavItemSelected(navProfile, false);
        });

        navMessage.setOnClickListener(v -> {
            setNavItemSelected(navSchedule, false);
            setNavItemSelected(navMessage, true);
            setNavItemSelected(navProfile, false);
            Intent intent = new Intent(this, SystemMessageActivity.class);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            setNavItemSelected(navSchedule, false);
            setNavItemSelected(navMessage, false);
            setNavItemSelected(navProfile, true);
            showProfileFragment();
        });
    }

    private void setNavItemSelected(LinearLayout navItem, boolean selected) {
        ImageView icon = (ImageView) navItem.getChildAt(0);
        TextView text = (TextView) navItem.getChildAt(1);

        if (selected) {
            icon.setColorFilter(ContextCompat.getColor(this, R.color.primary_color));
            text.setTextColor(ContextCompat.getColor(this, R.color.primary_color));
            text.setTypeface(null, Typeface.BOLD);
        } else {
            icon.setColorFilter(ContextCompat.getColor(this, R.color.nav_unselected));
            text.setTextColor(ContextCompat.getColor(this, R.color.nav_unselected));
            text.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void showProfileFragment() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void setupWeekNavigation() {
        ImageButton btnPrevWeek = findViewById(R.id.btnPrevWeek);
        ImageButton btnNextWeek = findViewById(R.id.btnNextWeek);
        Button btnCurrentWeek = findViewById(R.id.btnCurrentWeek);

        if (btnPrevWeek != null) {
            btnPrevWeek.setOnClickListener(v -> {
                int previousWeek = Math.max(1, currentWeek - 1);
                if (previousWeek != currentWeek) {
                    currentWeek = previousWeek;
                    updateWeekInfo();
                    displayWeeklyCourseTable();
                   // Toast.makeText(this, "切换到上一周（第" + currentWeek + "周）", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnNextWeek != null) {
            btnNextWeek.setOnClickListener(v -> {
                int nextWeek = Math.min(30, currentWeek + 1);
                if (nextWeek != currentWeek) {
                    currentWeek = nextWeek;
                    updateWeekInfo();
                    displayWeeklyCourseTable();
                    //Toast.makeText(this, "切换到下一周（第" + currentWeek + "周）", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnCurrentWeek != null) {
            btnCurrentWeek.setOnClickListener(v -> {
                // 显示加载中
                //Toast.makeText(this, "正在计算当前周...", Toast.LENGTH_SHORT).show();

                // 获取当前真实的日期
                Calendar now = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.getDefault());
                String currentDate = dateFormat.format(now.getTime());

                // 记录之前的周数用于比较
                int oldWeek = currentWeek;

                // 使用统一的周数计算方法
                int newWeek = calculateDefaultCurrentWeek();

                // 如果计算出的周数不同，更新
                if (newWeek != oldWeek) {
                    currentWeek = newWeek;

                    // 更新界面
                    updateWeekInfo();
                    displayWeeklyCourseTable();

                    // 显示详细提示
                    String message = String.format(
                            "已回到本周\n" +
                                    "当前日期：%s\n" +
                                    "当前周数：第%d周",
                            currentDate, currentWeek
                    );

                   // Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                    Log.d("WeekNavigation",
                            "回到本周成功：" +
                                    "\n原周数：" + oldWeek +
                                    "\n新周数：" + newWeek +
                                    "\n当前日期：" + currentDate
                    );
                } else {
                    // 如果已经是当前周，显示提示
                    String message = String.format(
                            "当前已经是本周\n" +
                                    "当前日期：%s\n" +
                                    "当前周数：第%d周",
                            currentDate, currentWeek
                    );
                   // Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    private void initViews() {
        llCourseTableContainer = findViewById(R.id.llCourseTableContainer);
        tvWeekInfo = findViewById(R.id.tvWeekInfo);
        tvSemesterInfo = findViewById(R.id.tvSemesterInfo);
        tvCurrentScheduleName = findViewById(R.id.tvCurrentScheduleName);

        tvMonday = findViewById(R.id.tvMonday);
        tvTuesday = findViewById(R.id.tvTuesday);
        tvWednesday = findViewById(R.id.tvWednesday);
        tvThursday = findViewById(R.id.tvThursday);
        tvFriday = findViewById(R.id.tvFriday);
    }

    private void setupScheduleManagementButton() {
        ImageButton btnScheduleManagement = findViewById(R.id.btnScheduleManagement);
        btnScheduleManagement.setOnClickListener(v -> showScheduleManagementDialog());
    }

    private void loadCurrentSchedule() {
        currentSchedule = dbHelper.getActiveSchedule();
        if (currentSchedule == null) {
            List<Schedule> allSchedules = dbHelper.getAllSchedules();
            if (!allSchedules.isEmpty()) {
                currentSchedule = allSchedules.get(0);
                dbHelper.setActiveSchedule(currentSchedule.id);
            }
        }

        if (currentSchedule != null) {
            // ✅ 每次都重新计算当前周
            currentWeek = calculateDefaultCurrentWeek();
            //calculateAndSetCurrentWeek();
            loadCoursesFromDatabase();
            updateScheduleInfo();

            // 日志输出当前周
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Log.d("CurrentWeek", "当前周: " + currentWeek + ", 当前日期: " + sdf.format(new Date()));
        } else {
            // 如果没有课表，也要确保有时间设置
            loadTimeSettings();
        }
    }


    private void calculateAndSetCurrentWeek() {
        // 记录点击"回到本周"的时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm EEEE", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        Log.d("BackToCurrent", "用户点击回到本周，当前系统时间: " + currentTime);

        // 统一使用 calculateDefaultCurrentWeek() 方法
        int newWeek = calculateDefaultCurrentWeek();

        Log.d("BackToCurrent",
                "当前系统时间: " + currentTime +
                        ", 计算出的当前周: " + newWeek +
                        ", 原来显示的周: " + currentWeek
        );

        // 只有当周数不同时才更新
        if (newWeek != currentWeek) {
            currentWeek = newWeek;

            // 更新界面
            updateWeekInfo();
            displayWeeklyCourseTable();

            // 显示成功消息
            Toast.makeText(this,
                    "已回到本周（第" + currentWeek + "周）\n当前时间: " + currentTime,
                    Toast.LENGTH_LONG
            ).show();

            Log.d("BackToCurrent", "成功回到本周，设置当前周为: " + currentWeek);
        } else {
            Toast.makeText(this, "当前已经是本周（第" + currentWeek + "周）", Toast.LENGTH_SHORT).show();
        }
    }
    private int getCurrentWeekFromToday() {
        // 直接调用 calculateDefaultCurrentWeek() 确保计算逻辑一致
        return calculateDefaultCurrentWeek();
    }
    private boolean saveTimeSettings(TimePicker tpMorning, EditText etMorningDuration, EditText etMorningBreak,
                                     TimePicker tpAfternoon, EditText etAfternoonDuration, EditText etAfternoonBreak,
                                     TimePicker tpEvening, EditText etEveningDuration) {
        try {
            // 从 TimePicker 获取时间
            currentTimeSetting.morningStartTime = formatTime(tpMorning.getHour(), tpMorning.getMinute());
            currentTimeSetting.afternoonStartTime = formatTime(tpAfternoon.getHour(), tpAfternoon.getMinute());
            currentTimeSetting.eveningStartTime = formatTime(tpEvening.getHour(), tpEvening.getMinute());

            // 获取其他设置
            currentTimeSetting.morningDuration = Integer.parseInt(etMorningDuration.getText().toString());
            currentTimeSetting.morningBreak = Integer.parseInt(etMorningBreak.getText().toString());
            currentTimeSetting.afternoonDuration = Integer.parseInt(etAfternoonDuration.getText().toString());
            currentTimeSetting.afternoonBreak = Integer.parseInt(etAfternoonBreak.getText().toString());
            currentTimeSetting.eveningDuration = Integer.parseInt(etEveningDuration.getText().toString());

            currentTimeSetting.scheduleId = currentSchedule != null ? currentSchedule.id : 1;

            return dbHelper.saveTimeSetting(currentTimeSetting);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
            return false;
        } catch (Exception e) {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    private String formatTime(int hour, int minute) {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }
    private int  calculateDefaultCurrentWeek() {

        Calendar now = Calendar.getInstance();
        Calendar semesterStart = getFirstMondayOfCurrentSemester();
        if (semesterStart != null) {
            long diff = now.getTimeInMillis() - semesterStart.getTimeInMillis();
            int weeks = (int) (diff / (7 * 24 * 60 * 60 * 1000L)) + 1;

            // 如果当前时间早于学期开始，返回1
            if (weeks < 1) {
                return 1;
            }

            return Math.min(30, weeks); // 限制在1-30周
        }

        return 1; // 默认第1周

    }
    private Calendar getFirstMondayOfCurrentSemester() {
        Calendar calendar = Calendar.getInstance();

        // 判断当前学期
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // 1-12月
        int currentYear = calendar.get(Calendar.YEAR);

        // ✅ 用户需求：将3月1日设为学期开始
        // 这里我们假设只有一个学期，从3月1日开始
        Calendar semesterStart = Calendar.getInstance();
        semesterStart.set(Calendar.YEAR, currentYear);
        semesterStart.set(Calendar.MONTH, Calendar.MARCH); // 3月
        semesterStart.set(Calendar.DAY_OF_MONTH, 1); // 1号
        semesterStart.set(Calendar.HOUR_OF_DAY, 0);
        semesterStart.set(Calendar.MINUTE, 0);
        semesterStart.set(Calendar.SECOND, 0);
        semesterStart.set(Calendar.MILLISECOND, 0);

        // 如果3月1日不是周一，调整到当周周一
        int dayOfWeek = semesterStart.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek != Calendar.MONDAY) {
            int daysToMonday = (Calendar.MONDAY - dayOfWeek + 7) % 7;
            if (daysToMonday > 0) {
                semesterStart.add(Calendar.DAY_OF_MONTH, daysToMonday);
            } else {
                semesterStart.add(Calendar.DAY_OF_MONTH, 7 + daysToMonday);
            }
        }

        Log.d("SemesterStart", "学期开始日期（调整后）：" +
                new SimpleDateFormat("yyyy-MM-dd EEEE", Locale.getDefault()).format(semesterStart.getTime()));

        return semesterStart;
    }
    // 显示批量删除对话框
    private void showBatchDeleteDialog() {
        if (currentSchedule == null) {
            Toast.makeText(this, "请先选择课表", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("批量删除课程 - " + currentSchedule.name);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_batch_delete, null);
        builder.setView(dialogView);

        CheckBox cbCurrentWeek = dialogView.findViewById(R.id.cbDeleteCurrentWeek);
        CheckBox cbWeekRange = dialogView.findViewById(R.id.cbDeleteWeekRange);
        CheckBox cbAllWeeks = dialogView.findViewById(R.id.cbDeleteAllWeeks);
        EditText etStartWeek = dialogView.findViewById(R.id.etDeleteStartWeek);
        EditText etEndWeek = dialogView.findViewById(R.id.etDeleteEndWeek);
        LinearLayout weekRangeLayout = dialogView.findViewById(R.id.llDeleteWeekRange);

        // 设置周数选择逻辑
        cbCurrentWeek.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbWeekRange.setChecked(false);
                cbAllWeeks.setChecked(false);
                weekRangeLayout.setVisibility(View.GONE);
                etStartWeek.setText(String.valueOf(currentWeek));
                etEndWeek.setText(String.valueOf(currentWeek));
            }
        });

        cbWeekRange.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbCurrentWeek.setChecked(false);
                cbAllWeeks.setChecked(false);
                weekRangeLayout.setVisibility(View.VISIBLE);
                etStartWeek.setText("1");
                etEndWeek.setText("20");
            } else {
                weekRangeLayout.setVisibility(View.GONE);
            }
        });

        cbAllWeeks.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbCurrentWeek.setChecked(false);
                cbWeekRange.setChecked(false);
                weekRangeLayout.setVisibility(View.GONE);
                etStartWeek.setText("1");
                etEndWeek.setText("20");
            }
        });

        // 默认选择本周
        cbCurrentWeek.setChecked(true);

        builder.setPositiveButton("删除", (dialog, which) -> {
            int startWeek, endWeek;

            try {
                if (cbCurrentWeek.isChecked()) {
                    startWeek = currentWeek;
                    endWeek = currentWeek;
                } else if (cbWeekRange.isChecked() || cbAllWeeks.isChecked()) {
                    startWeek = Integer.parseInt(etStartWeek.getText().toString());
                    endWeek = Integer.parseInt(etEndWeek.getText().toString());

                    if (startWeek < 1 || endWeek > 20 || startWeek > endWeek) {
                        Toast.makeText(this, "周数范围应为1-20周，且开始周不能大于结束周", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(this, "请选择删除范围", Toast.LENGTH_SHORT).show();
                    return;
                }

                showDeleteConfirmationDialog(startWeek, endWeek, cbAllWeeks.isChecked());

            } catch (NumberFormatException e) {
                Toast.makeText(this, "周数格式错误", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    // 显示删除确认对话框
    private void showDeleteConfirmationDialog(int startWeek, int endWeek, boolean isAllWeeks) {
        String message;
        if (isAllWeeks) {
            message = "确定要删除" + currentSchedule.name + "的所有课程吗？\n此操作不可撤销！";
        } else if (startWeek == endWeek) {
            message = "确定要删除第" + startWeek + "周的所有课程吗？";
        } else {
            message = "确定要删除第" + startWeek + "周到第" + endWeek + "周的所有课程吗？";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认批量删除");
        builder.setMessage(message);

        builder.setPositiveButton("确定删除", (dialog, which) -> {
            performBatchDelete(startWeek, endWeek, isAllWeeks);
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    // 执行批量删除操作
    private void performBatchDelete(int startWeek, int endWeek, boolean isAllWeeks) {
        boolean success;

        if (isAllWeeks) {
            // 删除整个课表的所有课程
            success = dbHelper.deleteCoursesByScheduleId(currentSchedule.id);
        } else if (startWeek == endWeek) {
            // 删除单周课程
            success = dbHelper.deleteCoursesByWeek(currentSchedule.id, startWeek);
        } else {
            // 删除周数范围内的课程
            success = dbHelper.deleteCoursesByWeekRange(currentSchedule.id, startWeek, endWeek);
        }

        if (success) {
            // 刷新数据
            loadCoursesFromDatabase();
            displayWeeklyCourseTable();

            String message;
            if (isAllWeeks) {
                message = "已删除所有课程";
            } else if (startWeek == endWeek) {
                message = "已删除第" + startWeek + "周的所有课程";
            } else {
                message = "已删除第" + startWeek + "周到第" + endWeek + "周的所有课程";
            }

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    // 选择多个课程进行批量删除
    private void showMultiSelectDeleteDialog() {
        if (currentSchedule == null) {
            Toast.makeText(this, "请先选择课表", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取当前周的所有课程
        List<Course> currentWeekCourses = new ArrayList<>();
        for (Course course : courses) {
            if (course.week == currentWeek && course.scheduleId == currentSchedule.id) {
                currentWeekCourses.add(course);
            }
        }

        if (currentWeekCourses.isEmpty()) {
            Toast.makeText(this, "当前周没有课程", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择要删除的课程（第" + currentWeek + "周）");

        // 创建多选列表
        String[] courseNames = new String[currentWeekCourses.size()];
        boolean[] checkedItems = new boolean[currentWeekCourses.size()];

        for (int i = 0; i < currentWeekCourses.size(); i++) {
            Course course = currentWeekCourses.get(i);
            courseNames[i] = course.name + " - " + course.location + " (第" + course.startSlot + "-" + course.endSlot + "节)";
        }

        builder.setMultiChoiceItems(courseNames, checkedItems, (dialog, which, isChecked) -> {
            // 更新选择状态
            checkedItems[which] = isChecked;
        });

        builder.setPositiveButton("删除选中", (dialog, which) -> {
            List<Long> selectedCourseIds = new ArrayList<>();
            for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i]) {
                    selectedCourseIds.add(currentWeekCourses.get(i).id);
                }
            }

            if (selectedCourseIds.isEmpty()) {
                Toast.makeText(this, "请选择要删除的课程", Toast.LENGTH_SHORT).show();
                return;
            }

            showMultiDeleteConfirmationDialog(selectedCourseIds, currentWeekCourses);
        });

        builder.setNegativeButton("取消", null);
        builder.setNeutralButton("全选", (dialog, which) -> {
            // 全选逻辑可以在自定义对话框中实现
            // 这里简化处理，提示用户使用多选功能
            Toast.makeText(this, "请逐个选择要删除的课程", Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    private void showMultiDeleteConfirmationDialog(List<Long> courseIds, List<Course> courses) {
        StringBuilder message = new StringBuilder("确定要删除以下课程吗？\n\n");
        for (int i = 0; i < courses.size(); i++) {
            if (courseIds.contains(courses.get(i).id)) {
                Course course = courses.get(i);
                message.append("• ").append(course.name)
                        .append(" - ").append(course.location)
                        .append(" (第").append(course.startSlot).append("-").append(course.endSlot).append("节)\n");
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认删除");
        builder.setMessage(message.toString());

        builder.setPositiveButton("确定删除", (dialog, which) -> {
            boolean success = dbHelper.deleteCoursesInBatch(courseIds);
            if (success) {
                loadCoursesFromDatabase();
                displayWeeklyCourseTable();
                Toast.makeText(this, "已删除选中的" + courseIds.size() + "个课程", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }
    private void loadCoursesFromDatabase() {
        if (currentSchedule != null) {
            Log.d("CourseLoad", "加载课表ID: " + currentSchedule.id + " 的课程");
            courses = getCoursesBySchedule(currentSchedule.id);
            Log.d("CourseLoad", "找到课程数量: " + courses.size());

            if (courses.isEmpty()) {
                Log.d("CourseLoad", "课程为空，添加示例数据");
                addSampleCoursesToDatabase();
                courses = getCoursesBySchedule(currentSchedule.id);
                Log.d("CourseLoad", "添加示例数据后课程数量: " + courses.size());
            }
        } else {
            Log.d("CourseLoad", "当前课表为null");
        }
    }

    // 新增方法：根据课表ID获取课程
    private List<Course> getCoursesBySchedule(long scheduleId) {
        List<Course> courseList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getDatabase();

        Cursor cursor = db.query(
                CourseDbHelper.TABLE_COURSES,
                null,
                CourseDbHelper.COLUMN_SCHEDULE_ID + " = ?",
                new String[]{String.valueOf(scheduleId)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            do {
                Course course = new Course();
                course.id = cursor.getLong(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_ID));
                course.name = cursor.getString(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_NAME));
                course.location = cursor.getString(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_LOCATION));
                course.date = cursor.getLong(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_DATE));
                course.startSlot = cursor.getInt(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_START_SLOT));
                course.endSlot = cursor.getInt(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_END_SLOT));
                course.week = cursor.getInt(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_WEEK));
                course.semester = cursor.getString(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_SEMESTER));
                course.scheduleId = cursor.getLong(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_SCHEDULE_ID));
                courseList.add(course);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return courseList;
    }

    // 新增方法：根据日期和课表ID获取课程
    private List<Course> getCoursesByDate(long dateMillis, long scheduleId) {
        List<Course> courseList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getDatabase();

        // 获取指定日期的开始和结束时间（一天的开始和结束）
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateMillis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long endOfDay = cal.getTimeInMillis();

        Cursor cursor = db.query(
                CourseDbHelper.TABLE_COURSES,
                null,
                CourseDbHelper.COLUMN_DATE + " >= ? AND " + CourseDbHelper.COLUMN_DATE + " <= ? AND " +
                        CourseDbHelper.COLUMN_SCHEDULE_ID + " = ?",
                new String[]{String.valueOf(startOfDay), String.valueOf(endOfDay), String.valueOf(scheduleId)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            do {
                Course course = new Course();
                course.id = cursor.getLong(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_ID));
                course.name = cursor.getString(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_NAME));
                course.location = cursor.getString(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_LOCATION));
                course.date = cursor.getLong(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_DATE));
                course.startSlot = cursor.getInt(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_START_SLOT));
                course.endSlot = cursor.getInt(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_END_SLOT));
                course.week = cursor.getInt(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_WEEK));
                course.semester = cursor.getString(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_SEMESTER));
                course.scheduleId = cursor.getLong(cursor.getColumnIndexOrThrow(CourseDbHelper.COLUMN_SCHEDULE_ID));
                courseList.add(course);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return courseList;
    }

    // 新增方法：检查课程冲突
    private boolean courseExists(long dateMillis, int startSlot, int endSlot, long scheduleId) {
        SQLiteDatabase db = dbHelper.getDatabase();

        Cursor cursor = db.query(
                CourseDbHelper.TABLE_COURSES,
                null,
                CourseDbHelper.COLUMN_DATE + " = ? AND " +
                        CourseDbHelper.COLUMN_SCHEDULE_ID + " = ? AND " +
                        "((? BETWEEN " + CourseDbHelper.COLUMN_START_SLOT + " AND " + CourseDbHelper.COLUMN_END_SLOT + ") OR " +
                        "(? BETWEEN " + CourseDbHelper.COLUMN_START_SLOT + " AND " + CourseDbHelper.COLUMN_END_SLOT + "))",
                new String[]{String.valueOf(dateMillis), String.valueOf(scheduleId),
                        String.valueOf(startSlot), String.valueOf(endSlot)},
                null, null, null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    private void addSampleCoursesToDatabase() {
        if (currentSchedule == null) return;

        Log.d("SampleData", "为课表 " + currentSchedule.name + " 添加示例数据");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        addCourseToDatabase(calendar, "移动终端应用开发", "实训楼B-310", 1, 2, 15,
                currentSchedule.year + " " + currentSchedule.semester);
    }

    private void addCourseToDatabase(Calendar date, String name, String location,
                                     int startSlot, int endSlot, int week, String semester) {
        Course course = new Course();
        course.name = name;
        course.location = location;
        course.date = date.getTimeInMillis();
        course.startSlot = startSlot;
        course.endSlot = endSlot;
        course.week = week;
        course.semester = semester;
        course.scheduleId = currentSchedule.id;

        dbHelper.addCourse(course, currentSchedule.id);
    }

    private void updateWeekInfo() {
        if (currentSchedule != null) {
            tvSemesterInfo.setText(currentSchedule.year + "\n" + currentSchedule.semester);
            tvWeekInfo.setText("第" + currentWeek + "周");
            tvCurrentScheduleName.setText(currentSchedule.name);
        } else {
            tvWeekInfo.setText("第" + currentWeek + "周");
            tvCurrentScheduleName.setText("无课表");
        }
    }

    private void updateScheduleInfo() {
        if (currentSchedule != null) {
            tvSemesterInfo.setText(currentSchedule.year + "\n" + currentSchedule.semester);
            tvWeekInfo.setText("第" + currentWeek + "周");
            tvCurrentScheduleName.setText(currentSchedule.name);
        } else {
            tvCurrentScheduleName.setText("无课表");
        }
    }

    private void showScheduleManagementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("课表管理");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_schedule_management, null);
        builder.setView(dialogView);

        Button btnAddSchedule = dialogView.findViewById(R.id.btnAddSchedule);
        ListView lvSchedules = dialogView.findViewById(R.id.lvSchedules);

        schedules = dbHelper.getAllSchedules();

        final AlertDialog dialog = builder.create();

        ScheduleAdapter adapter = new ScheduleAdapter(this, schedules, dbHelper,
                new ScheduleAdapter.OnScheduleChangeListener() {
                    @Override
                    public void onScheduleChanged() {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }

                        new android.os.Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                currentSchedule = dbHelper.getActiveSchedule();
                                if (currentSchedule != null) {
                                    loadCoursesFromDatabase();
                                    updateScheduleInfo();
                                    displayWeeklyCourseTable();
                                }
                            }
                        }, 100);
                    }
                });

        lvSchedules.setAdapter(adapter);

        btnAddSchedule.setOnClickListener(v -> {
            dialog.dismiss();
            showAddScheduleDialog();
        });

        dialog.show();
    }

    private void showAddScheduleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加新课表");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_schedule, null);
        builder.setView(dialogView);

        EditText etScheduleName = dialogView.findViewById(R.id.etScheduleName);
        EditText etYear = dialogView.findViewById(R.id.etYear);
        Spinner spinnerSemester = dialogView.findViewById(R.id.spinnerSemester);

        String[] semesters = {"第1学期", "第2学期", "暑假学期", "寒假学期"};
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, semesters);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(semesterAdapter);

        builder.setPositiveButton("添加", (dialog, which) -> {
            String scheduleName = etScheduleName.getText().toString().trim();
            String year = etYear.getText().toString().trim();
            String semester = spinnerSemester.getSelectedItem().toString();

            if (scheduleName.isEmpty() || year.isEmpty()) {
                Toast.makeText(this, "请填写完整的课表信息", Toast.LENGTH_SHORT).show();
                return;
            }

            addNewSchedule(scheduleName, semester, year);
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void addNewSchedule(String name, String semester, String year) {
        long newId = dbHelper.addSchedule(name, semester, year);
        if (newId != -1) {
            Schedule newSchedule = new Schedule();
            newSchedule.id = newId;
            newSchedule.name = name;
            newSchedule.semester = semester;
            newSchedule.year = year;

            switchToSchedule(newSchedule);
            Toast.makeText(this, "课表添加成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "课表添加失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void switchToSchedule(Schedule schedule) {
        dbHelper.setActiveSchedule(schedule.id);
        currentSchedule = schedule;
        loadCoursesFromDatabase();
        loadTimeSettings(); // 重新加载时间设置
        loadPeriodSettings(); // 重新加载节数设置
        displayWeeklyCourseTable();
        updateScheduleInfo();
        Toast.makeText(this, "已切换到：" + schedule.name, Toast.LENGTH_SHORT).show();

    }

    private void setupAddCourseButton() {
        ImageButton btnAddCourse = findViewById(R.id.btnAddCourse);
        btnAddCourse.setOnClickListener(v -> {
            if (currentSchedule == null) {
                Toast.makeText(this, "请先选择课表", Toast.LENGTH_SHORT).show();
                return;
            }
            showAddCourseDialog();
        });
    }

    private void showAddCourseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加课程 - " + currentSchedule.name + " 第" + currentWeek + "周");
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null);
        builder.setView(dialogView);

        EditText etCourseName = dialogView.findViewById(R.id.etCourseName);
        EditText etLocation = dialogView.findViewById(R.id.etLocation);
        Spinner spinnerDay = dialogView.findViewById(R.id.spinnerDay);
        Spinner spinnerStartSlot = dialogView.findViewById(R.id.spinnerStartSlot);
        Spinner spinnerEndSlot = dialogView.findViewById(R.id.spinnerEndSlot);
        // 新增周数选择控件
        CheckBox cbCurrentWeek = dialogView.findViewById(R.id.cbCurrentWeek);
        CheckBox cbAllWeeks = dialogView.findViewById(R.id.cbAllWeeks);
        EditText etStartWeek = dialogView.findViewById(R.id.etStartWeek);
        EditText etEndWeek = dialogView.findViewById(R.id.etEndWeek);
        Spinner spinnerWeekInterval = dialogView.findViewById(R.id.spinnerWeekInterval);

        setupSpinners(spinnerDay, spinnerStartSlot, spinnerEndSlot);
        setupWeekSpinner(spinnerWeekInterval);
        // 设置周数选择逻辑
        cbCurrentWeek.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbAllWeeks.setChecked(false);
                etStartWeek.setEnabled(false);
                etEndWeek.setEnabled(false);
                spinnerWeekInterval.setEnabled(false);
                etStartWeek.setText(String.valueOf(currentWeek));
                etEndWeek.setText(String.valueOf(currentWeek));
            }
        });

        cbAllWeeks.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbCurrentWeek.setChecked(false);
                etStartWeek.setEnabled(true);
                etEndWeek.setEnabled(true);
                spinnerWeekInterval.setEnabled(true);
                etStartWeek.setText("1");
                etEndWeek.setText("20");
            }
        });
        // 默认选择本周
        cbCurrentWeek.setChecked(true);

        builder.setPositiveButton("添加", (dialog, which) -> {
            String courseName = etCourseName.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            int dayIndex = spinnerDay.getSelectedItemPosition();
            int startSlot = spinnerStartSlot.getSelectedItemPosition() + 1;
            int endSlot = spinnerEndSlot.getSelectedItemPosition() + 1;
            int weekInterval = spinnerWeekInterval.getSelectedItemPosition() + 1;

            if (courseName.isEmpty() || location.isEmpty()) {
                Toast.makeText(MainActivity.this, "请填写完整的课程信息", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startSlot > endSlot) {
                Toast.makeText(MainActivity.this, "结束时间不能早于开始时间", Toast.LENGTH_SHORT).show();
                return;
            }
            int startWeek, endWeek;
            try {
                startWeek = Integer.parseInt(etStartWeek.getText().toString());
                endWeek = Integer.parseInt(etEndWeek.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "周数格式错误", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startWeek < 1 || endWeek > 20 || startWeek > endWeek) {
                return;
            }

            // 添加课程到多个周数
            addCourseToMultipleWeeks(courseName, location, dayIndex, startSlot, endSlot,
                    startWeek, endWeek, weekInterval);


//            Calendar calendar = Calendar.getInstance();
//            int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
//            int daysFromMonday = (currentDayOfWeek + 5) % 7;
//            calendar.add(Calendar.DAY_OF_MONTH, dayIndex - daysFromMonday);
//            calendar.set(Calendar.HOUR_OF_DAY, 0);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.set(Calendar.SECOND, 0);
//            calendar.set(Calendar.MILLISECOND, 0);
//
//            // 使用新的课程冲突检查方法
//            if (courseExists(calendar.getTimeInMillis(), startSlot, endSlot, currentSchedule.id)) {
//                Toast.makeText(MainActivity.this, "该时间段已有课程", Toast.LENGTH_SHORT).show();
//                return;
//            }

//            Course newCourse = new Course();
//            newCourse.name = courseName;
//            newCourse.location = location;
//            newCourse.date = calendar.getTimeInMillis();
//            newCourse.startSlot = startSlot;
//            newCourse.endSlot = endSlot;
//            newCourse.week = currentWeek;
//            newCourse.semester = currentSchedule.year + " " + currentSchedule.semester;
//            newCourse.scheduleId = currentSchedule.id;
//
//            addNewCourse(newCourse);
        });
//
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    private void setupWeekSpinner(Spinner spinnerWeekInterval) {
        String[] intervals = new String[10];
        for (int i = 0; i < 10; i++) {
            intervals[i] = String.valueOf(i + 1);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, intervals);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWeekInterval.setAdapter(adapter);
        spinnerWeekInterval.setSelection(0); // 默认间隔1周
    }

    // 新增方法：添加课程到多个周数
    // 修改现有的 addCourseToMultipleWeeks 方法
    // 修改 addCourseToMultipleWeeks 方法
    private void addCourseToMultipleWeeks(String courseName, String location, int dayIndex,
                                          int startSlot, int endSlot, int startWeek, int endWeek,
                                          int weekInterval) {
        // 日志记录开始
        Log.d("BatchAdd", "开始批量添加课程: " + courseName +
                ", 周数范围: " + startWeek + "-" + endWeek +
                ", 间隔: " + weekInterval + "周");

        int successCount = 0;
        int conflictCount = 0;

        // 遍历所有周数
        for (int week = startWeek; week <= endWeek; week += weekInterval) {
            Log.d("BatchAdd", "处理第" + week + "周...");

            // 计算该周的具体日期
            Calendar calendar = calculateDateForWeekAndDay(week, dayIndex);
            if (calendar == null) {
                Log.e("BatchAdd", "第" + week + "周日期计算失败，跳过");
                conflictCount++;
                continue;
            }

            // 格式化日期用于日志
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd EEEE", Locale.getDefault());
            String dateStr = sdf.format(calendar.getTime());
            Log.d("BatchAdd", "第" + week + "周日期: " + dateStr);

            // 检查课程冲突
            if (courseExists(calendar.getTimeInMillis(), startSlot, endSlot, currentSchedule.id)) {
                Log.w("BatchAdd", "第" + week + "周时间冲突，跳过");
                conflictCount++;
                continue;
            }

            // 创建课程对象
            Course newCourse = new Course();
            newCourse.name = courseName;
            newCourse.location = location;
            newCourse.date = calendar.getTimeInMillis();
            newCourse.startSlot = startSlot;
            newCourse.endSlot = endSlot;
            newCourse.week = week;  // ✅ 正确的周数
            newCourse.semester = currentSchedule.year + " " + currentSchedule.semester;
            newCourse.scheduleId = currentSchedule.id;

            // 添加到数据库
            long newId = dbHelper.addCourse(newCourse, currentSchedule.id);
            if (newId != -1) {
                Log.d("BatchAdd", "第" + week + "周课程添加成功，ID: " + newId);
                successCount++;
            } else {
                Log.e("BatchAdd", "第" + week + "周课程添加失败");
                conflictCount++;
            }
        }

        // 刷新UI
        if (successCount > 0) {
            loadCoursesFromDatabase();
            displayWeeklyCourseTable();

            String message = String.format(
                    "批量添加完成：成功%d个，冲突跳过%d个",
                    successCount, conflictCount
            );

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            Log.d("BatchAdd", message);
        } else if (conflictCount > 0) {
            Toast.makeText(this, "添加失败，所有周数都存在时间冲突", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "添加失败，请检查输入", Toast.LENGTH_SHORT).show();
        }
    }

    // 新增方法：根据周数和星期计算具体日期
    private Calendar calculateDateForWeekAndDay(int week, int dayIndex) {
        // 获取学期开始日期
        Calendar semesterStart = getFirstMondayOfCurrentSemester();

        // 计算目标周的周一
        Calendar targetMonday = (Calendar) semesterStart.clone();
        targetMonday.add(Calendar.WEEK_OF_YEAR, week - 1);

        // 调整到指定星期
        targetMonday.add(Calendar.DAY_OF_MONTH, dayIndex);

        // 设置时间为0点
        targetMonday.set(Calendar.HOUR_OF_DAY, 0);
        targetMonday.set(Calendar.MINUTE, 0);
        targetMonday.set(Calendar.SECOND, 0);
        targetMonday.set(Calendar.MILLISECOND, 0);

        return targetMonday;
    }
    // ✅ 新增：使用统一的周数计算方法
    private Calendar calculateDateByWeekNumber(int targetWeek, int dayIndex) {
        // 获取当前周一的日期
        Calendar mondayOfCurrentWeek = getMondayOfCurrentWeek();

        if (mondayOfCurrentWeek == null) {
            return null;
        }

        // 计算当前真实周数
        int currentRealWeek = calculateRealCurrentWeek();

        // 计算周数差
        int weekDiff = targetWeek - currentRealWeek;

        // 复制日历对象
        Calendar targetDate = (Calendar) mondayOfCurrentWeek.clone();

        // 调整到目标周
        targetDate.add(Calendar.WEEK_OF_YEAR, weekDiff);

        // 调整到指定星期（0=周一, 1=周二, ..., 4=周五）
        targetDate.add(Calendar.DAY_OF_MONTH, dayIndex);

        // 设置时间为0点
        targetDate.set(Calendar.HOUR_OF_DAY, 0);
        targetDate.set(Calendar.MINUTE, 0);
        targetDate.set(Calendar.SECOND, 0);
        targetDate.set(Calendar.MILLISECOND, 0);

        // 添加详细日志
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd EEEE", Locale.getDefault());
        Log.d("DateCalc", "周数计算详情: " +
                "目标周=" + targetWeek +
                ", 当前真实周=" + currentRealWeek +
                ", 周数差=" + weekDiff +
                ", 计算日期=" + sdf.format(targetDate.getTime()));

        return targetDate;
    }

    // ✅ 新增：获取当前周的周一
    private Calendar getMondayOfCurrentWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysToMonday = 0;

        switch (dayOfWeek) {
            case Calendar.SUNDAY:    daysToMonday = 1; break;
            case Calendar.MONDAY:    daysToMonday = 0; break;
            case Calendar.TUESDAY:   daysToMonday = -1; break;
            case Calendar.WEDNESDAY: daysToMonday = -2; break;
            case Calendar.THURSDAY:  daysToMonday = -3; break;
            case Calendar.FRIDAY:    daysToMonday = -4; break;
            case Calendar.SATURDAY:  daysToMonday = -5; break;
        }

        calendar.add(Calendar.DAY_OF_MONTH, daysToMonday);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

    // 备用方法：基于当前周计算日期
    private Calendar calculateDateBasedOnCurrentWeek(int targetWeek, int dayIndex) {
        Calendar calendar = Calendar.getInstance();

        // 找到当前周的周一
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // 计算周数差
        int currentWeek = calculateDefaultCurrentWeek();
        int weekDiff = targetWeek - currentWeek;

        // 调整到目标周
        calendar.add(Calendar.WEEK_OF_YEAR, weekDiff);

        // 调整到指定星期
        calendar.add(Calendar.DAY_OF_MONTH, dayIndex);

        return calendar;
    }    private void setupSpinners(Spinner spinnerDay, Spinner spinnerStartSlot, Spinner spinnerEndSlot) {
        String[] days = {"周一", "周二", "周三", "周四", "周五"};
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(dayAdapter);

        String[] slots = new String[12];
        for (int i = 0; i < 12; i++) {
            slots[i] = "第" + (i + 1) + "节 ";
        }

        ArrayAdapter<String> slotAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, slots);
        slotAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStartSlot.setAdapter(slotAdapter);
        spinnerEndSlot.setAdapter(slotAdapter);

        spinnerStartSlot.setSelection(0);
        spinnerEndSlot.setSelection(0);
    }

    private void displayWeeklyCourseTable() {
        if (currentTimeSetting == null) {
            Log.d("CourseTable", "currentTimeSetting为null，重新加载");
            loadTimeSettings();
        }
        llCourseTableContainer.removeAllViews();
        calculateWeekDays();
        updateWeekdayHeaders();
        createCourseTable();
    }

    private void updateWeekdayHeaders() {
        if (weekDaysMillis.length >= 7) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());

            tvMonday.setText("周一\n" + dateFormat.format(new Date(weekDaysMillis[0])));
            tvTuesday.setText("周二\n" + dateFormat.format(new Date(weekDaysMillis[1])));
            tvWednesday.setText("周三\n" + dateFormat.format(new Date(weekDaysMillis[2])));
            tvThursday.setText("周四\n" + dateFormat.format(new Date(weekDaysMillis[3])));
            tvFriday.setText("周五\n" + dateFormat.format(new Date(weekDaysMillis[4])));
            TextView tvSaturday = findViewById(R.id.tvSaturday);
            TextView tvSunday = findViewById(R.id.tvSunday);
            if (tvSaturday != null) {
                tvSaturday.setText("周六\n" + dateFormat.format(new Date(weekDaysMillis[5])));
            }
            if (tvSunday != null) {
                tvSunday.setText("周日\n" + dateFormat.format(new Date(weekDaysMillis[6])));
            }
        }
    }

    private void calculateWeekDays() {
        // 获取学期开始日期（3月1日后的第一个周一）
        Calendar semesterStart = getFirstMondayOfCurrentSemester();

        // 计算目标周的周一日期
        Calendar targetMonday = (Calendar) semesterStart.clone();
        targetMonday.add(Calendar.WEEK_OF_YEAR, currentWeek - 1);

        // 设置为0点
        targetMonday.set(Calendar.HOUR_OF_DAY, 0);
        targetMonday.set(Calendar.MINUTE, 0);
        targetMonday.set(Calendar.SECOND, 0);
        targetMonday.set(Calendar.MILLISECOND, 0);
        weekDaysMillis = new long[7];
        // 计算周一至周五的日期
        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) targetMonday.clone();
            day.add(Calendar.DAY_OF_MONTH, i);
            weekDaysMillis[i] = day.getTimeInMillis();

            // 调试日志
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd EEEE", Locale.getDefault());
            Log.d("WeekCalc", "第" + currentWeek + "周 星期" + (i+1) + ": " + sdf.format(day.getTime()));
        }
    }
    private int calculateRealCurrentWeek() {
        Calendar now = Calendar.getInstance();

        // 如果是开发测试，可以固定一个周数
        // 如果是真实使用，计算当前是本学期第几周

        // 简单实现：返回本周是今年的第几周
        int currentWeekOfYear = now.get(Calendar.WEEK_OF_YEAR);

        // 假设学期从第9周开始（3月初）
        int semesterStartWeek = 9; // 可以调整

        int realWeek = Math.max(1, currentWeekOfYear - semesterStartWeek + 1);
        return Math.min(30, realWeek); // 限制在1-30周
    }
    private void createCourseTable() {
        llCourseTableContainer.removeAllViews();

        if (currentPeriodSetting == null) {
            loadPeriodSettings();
        }

        int totalPeriods = currentPeriodSetting.totalPeriods;
        Log.d("CourseTable", "创建课程表，总节数: " + totalPeriods);

        // ✅ 不再手动计算宽度，让权重系统自动分配

        for (int slot = 1; slot <= totalPeriods; slot++) {
            // 创建表格行
            LinearLayout tableRow = new LinearLayout(this);
            tableRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, // 宽度填满
                    dpToPx(80) // 高度固定
            ));
            tableRow.setOrientation(LinearLayout.HORIZONTAL);
            tableRow.setBackgroundColor(Color.parseColor("#FFFFFF"));

            // ✅ 1. 添加时间单元格（固定宽度）
            addTimeSlotCellWithWeight(tableRow, slot, 80); // 固定80dp

            // ✅ 2. 添加5个课程单元格（使用权重）
            for (int day = 0; day < 7; day++) {
                addCourseCellWithWeight(tableRow, day, slot);
            }

            llCourseTableContainer.addView(tableRow);

            // 添加分隔线
            if (slot < totalPeriods) {
                View separator = new View(this);
                LinearLayout.LayoutParams separatorParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(1)
                );
                // 让分隔线与星期标题对齐（左边留出时间标题的宽度）
                separatorParams.setMargins(dpToPx(80), 0, 0, 0);
                separator.setLayoutParams(separatorParams);
                separator.setBackgroundColor(Color.parseColor("#E0E0E0"));
                llCourseTableContainer.addView(separator);
            }
        }
    }

    private void addTimeSlotCellWithWeight(LinearLayout row, int slot, int widthDp) {
        TextView timeCell = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(widthDp), // 固定宽度
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        timeCell.setLayoutParams(params);

        // 获取动态计算的时间
        String startTime = getStartTime(slot);
        String endTime = getEndTime(slot);

        timeCell.setText("第" + slot + "节\n" + startTime + "\n" + endTime);
        timeCell.setTextSize(12);
        timeCell.setTextColor(Color.parseColor("#666666"));
        timeCell.setGravity(Gravity.CENTER);
        timeCell.setBackgroundColor(Color.parseColor("#F5F5F5"));
        timeCell.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        timeCell.setLineSpacing(dpToPx(2), 1.0f);

        row.addView(timeCell);
    }
    private void addCourseCellWithWeight(LinearLayout row, int dayIndex, int slot) {
        LinearLayout courseCell = new LinearLayout(this);

        // ✅ 关键：使用权重布局参数
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, // 宽度设为0dp，由weight决定
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        params.weight = 1.0f; // 权重为1，与星期标题匹配
        params.setMargins(0, dpToPx(1), 0, dpToPx(1));

        courseCell.setLayoutParams(params);
        courseCell.setOrientation(LinearLayout.VERTICAL);
        courseCell.setBackgroundColor(Color.parseColor("#000000"));
        courseCell.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
        courseCell.setGravity(Gravity.CENTER);
        courseCell.setMinimumHeight(dpToPx(80));

        // 查找该单元格的课程
        List<Course> cellCourses = findCoursesForCell(dayIndex, slot);

        if (!cellCourses.isEmpty()) {
            Course course = cellCourses.get(0);
            displayCourseInCell(courseCell, course, slot);

            // 设置点击和长按事件
            courseCell.setOnClickListener(v -> showCourseDetails(course));
            courseCell.setOnLongClickListener(v -> {
                showCourseOptions(course);
                return true;
            });
        } else {
            // 无课程时显示空白
            courseCell.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }

        row.addView(courseCell);
    }
//    private void addCourseCell(LinearLayout row, int dayIndex, int slot, int width) {
//        LinearLayout courseCell = new LinearLayout(this);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                width,
//                LinearLayout.LayoutParams.MATCH_PARENT
//        );
//        params.setMargins(0, dpToPx(1), 0, dpToPx(1));
//
//        courseCell.setLayoutParams(params);
//        courseCell.setOrientation(LinearLayout.VERTICAL);
//        courseCell.setBackgroundColor(Color.parseColor("#FAFAFA"));
//        courseCell.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
//        courseCell.setGravity(Gravity.CENTER);
//
//        List<Course> cellCourses = findCoursesForCell(dayIndex, slot);
//
//        if (!cellCourses.isEmpty()) {
//            Course course = cellCourses.get(0);
//            displayCourseInCell(courseCell, course, slot);
//
//            courseCell.setOnClickListener(v -> showCourseDetails(course));
//            courseCell.setOnLongClickListener(v -> {
//                showCourseOptions(course);
//                return true;
//            });
//        } else {
//            courseCell.setBackgroundColor(Color.parseColor("#FFFFFF"));
//        }
//
//        row.addView(courseCell);
//    }

    private List<Course> findCoursesForCell(int dayIndex, int slot) {
        List<Course> cellCourses = new ArrayList<>();
        if (currentSchedule == null) {
            return cellCourses;
        }
        long dayMillis = weekDaysMillis[dayIndex];
        List<Course> dailyCourses = getCoursesByDate(dayMillis, currentSchedule.id);
        for (Course course : dailyCourses) {
            if (course.startSlot <= slot && course.endSlot >= slot) {
                cellCourses.add(course);
            }
        }
        return cellCourses;
    }

    private void displayCourseInCell(LinearLayout cell, Course course, int currentSlot) {
        cell.removeAllViews();
        cell.setMinimumHeight(dpToPx(80));

        // ✅ 简单方案：连堂课程的每一节都显示完整的课程信息
        TextView nameText = new TextView(this);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameText.setLayoutParams(nameParams);

        // 始终显示课程名称
        nameText.setText(course.name);
        nameText.setTextSize(10);
        nameText.setTextColor(Color.parseColor("#FFFFFF"));
        nameText.setMaxLines(2);
        nameText.setEllipsize(TextUtils.TruncateAt.END);
        nameText.setTypeface(null, Typeface.BOLD);
        nameText.setGravity(Gravity.CENTER);
        nameText.setLineSpacing(dpToPx(2), 1.0f);

        TextView locationText = new TextView(this);
        LinearLayout.LayoutParams locationParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        locationText.setLayoutParams(locationParams);
        locationText.setText(course.location);
        locationText.setTextSize(8);
        locationText.setTextColor(Color.parseColor("#FFFFFF"));
        locationText.setSingleLine(true);
        locationText.setEllipsize(TextUtils.TruncateAt.END);
        locationText.setGravity(Gravity.CENTER);

        // 设置背景颜色
        int colorRes = getCourseColor(course.name);
        cell.setBackgroundColor(ContextCompat.getColor(this, colorRes));

        // 添加到单元格
        cell.addView(nameText);
        cell.addView(locationText);
    }
    private int getCourseColor(String courseName) {
        int hash = courseName.hashCode();
        int colorIndex = Math.abs(hash) % 5;

        int[] colors = {
                R.color.course_color_1,
                R.color.course_color_2,
                R.color.course_color_3,
                R.color.course_color_4,
                R.color.course_color_5
        };

        return colors[colorIndex];
    }

    private String getStartTime(int slot) {
        return calculateTimeForSlot(slot, true);
    }

    private String getEndTime(int slot) {
        return calculateTimeForSlot(slot, false);
    }
    private String calculateTimeForSlot(int slot, boolean isStart) {
        if (currentTimeSetting == null || currentPeriodSetting == null) {
            return isStart ? "08:00" : "09:00";
        }

        // 判断时段
        if (slot >= 1 && slot <= currentPeriodSetting.morningPeriods) {
            // 上午课程
            int relativeSlot = slot - 1;
            return calculatePeriodTime(currentTimeSetting.morningStartTime,
                    currentTimeSetting.morningDuration, currentTimeSetting.morningBreak,
                    relativeSlot, isStart);
        }
        else if (slot <= currentPeriodSetting.morningPeriods + currentPeriodSetting.afternoonPeriods) {
            // 下午课程
            int relativeSlot = slot - currentPeriodSetting.morningPeriods - 1;
            return calculatePeriodTime(currentTimeSetting.afternoonStartTime,
                    currentTimeSetting.afternoonDuration, currentTimeSetting.afternoonBreak,
                    relativeSlot, isStart);
        }
        else if (slot <= currentPeriodSetting.totalPeriods) {
            // 晚上课程
            int relativeSlot = slot - currentPeriodSetting.morningPeriods - currentPeriodSetting.afternoonPeriods - 1;
            return calculatePeriodTime(currentTimeSetting.eveningStartTime,
                    currentTimeSetting.eveningDuration, 0,
                    relativeSlot, isStart);
        }

        return isStart ? "08:00" : "09:00";
    }
    private String calculatePeriodTime(String startTime, int duration, int breakTime,
                                       int periodIndex, boolean isStart) {
        try {
            String[] timeParts = startTime.split(":");
            int hours = Integer.parseInt(timeParts[0]);
            int minutes = Integer.parseInt(timeParts[1]);

            // 计算总分钟数
            int totalMinutes;
            if (isStart) {
                // 开始时间：基础时间 + (课时长度 + 课间休息) * 节数
                totalMinutes = (duration + breakTime) * periodIndex;
            } else {
                // 结束时间：开始时间 + 课时长度
                totalMinutes = (duration + breakTime) * periodIndex + duration;
            }

            // 计算小时和分钟
            hours += totalMinutes / 60;
            minutes += totalMinutes % 60;

            // 处理分钟进位
            if (minutes >= 60) {
                hours += minutes / 60;
                minutes = minutes % 60;
            }

            return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);

        } catch (Exception e) {
            return "08:00";
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void showCourseOptions(Course course) {
        String[] options = {"编辑课程", "删除课程", "提醒设置", "批量删除课程"}; // 添加提醒设置选项

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(course.name);
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    showEditCourseDialog(course);
                    break;
                case 1:
                    showDeleteConfirmDialog(course);
                    break;
                case 2: // 提醒设置
                    showReminderSettingDialog(course);
                    break;
                case 3: // 批量删除
                    showBatchDeleteDialog();
                    break;
            }
        });
        builder.show();
    }
    // 显示提醒设置对话框
    private void showReminderSettingDialog(Course course) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("上课提醒设置");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.reminder_setting_dialog, null);
        builder.setView(dialogView);

        // 获取控件引用
        TextView tvCourseInfo = dialogView.findViewById(R.id.tvCourseInfo);
        TextView tvCourseTime = dialogView.findViewById(R.id.tvCourseTime);
        SwitchCompat switchReminder = dialogView.findViewById(R.id.switchReminder);
        Button btn5min = dialogView.findViewById(R.id.btn5min);
        Button btn10min = dialogView.findViewById(R.id.btn10min);
        Button btn15min = dialogView.findViewById(R.id.btn15min);
        Button btn30min = dialogView.findViewById(R.id.btn30min);
        Button btn60min = dialogView.findViewById(R.id.btn60min);
        Button btnCustom = dialogView.findViewById(R.id.btnCustom);
        LinearLayout llCustomTime = dialogView.findViewById(R.id.llCustomTime);
        EditText etCustomMinutes = dialogView.findViewById(R.id.etCustomMinutes);
        TextView tvNextClassTime = dialogView.findViewById(R.id.tvNextClassTime);
        TextView tvReminderPreview = dialogView.findViewById(R.id.tvReminderPreview);

        // 加载当前提醒设置
        CourseDbHelper.ReminderSetting currentSetting = dbHelper.getReminderSettingForCourse(course.id);

        // ✅ 修复：使用数组存储值
        final int[] remindMinutesArr = {10}; // 初始值
        boolean isEnabled = true;

        if (currentSetting != null) {
            remindMinutesArr[0] = currentSetting.remindMinutes;
            isEnabled = currentSetting.isEnabled;
        }

        // 设置课程信息
        tvCourseInfo.setText(course.name);

        // 格式化上课时间
        String startTime = getStartTime(course.startSlot);
        String endTime = getEndTime(course.endSlot);
        String timeText = String.format("上课时间：%s - %s", startTime, endTime);
        tvCourseTime.setText(timeText);

        // 设置开关状态
        switchReminder.setChecked(isEnabled);

        // 更新预览
        updateReminderPreview(tvReminderPreview, course, remindMinutesArr[0], isEnabled);
        updateNextClassTime(tvNextClassTime, course);

        // ✅ 修复：使用数组的快速选择按钮点击事件
        View.OnClickListener quickSelectListener = v -> {
            int id = v.getId();
            int minutes = 0;
            if (id == R.id.btn5min) {
                minutes = 5;
            } else if (id == R.id.btn10min) {
                minutes = 10;
            } else if (id == R.id.btn15min) {
                minutes = 15;
            } else if (id == R.id.btn30min) {
                minutes = 30;
            } else if (id == R.id.btn60min) {
                minutes = 60;
            }

            if (minutes > 0) {
                remindMinutesArr[0] = minutes; // ✅ 可以修改数组内容
                updateReminderPreview(tvReminderPreview, course, remindMinutesArr[0], switchReminder.isChecked());
            }
        };

        btn5min.setOnClickListener(quickSelectListener);
        btn10min.setOnClickListener(quickSelectListener);
        btn15min.setOnClickListener(quickSelectListener);
        btn30min.setOnClickListener(quickSelectListener);
        btn60min.setOnClickListener(quickSelectListener);

        // 自定义按钮点击事件
        btnCustom.setOnClickListener(v -> {
            if (llCustomTime.getVisibility() == View.VISIBLE) {
                llCustomTime.setVisibility(View.GONE);
            } else {
                llCustomTime.setVisibility(View.VISIBLE);
                etCustomMinutes.requestFocus();
            }
        });

        // 自定义输入监听
        etCustomMinutes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int minutes = Integer.parseInt(s.toString());
                    if (minutes > 0 && minutes <= 1440) { // 最多提前一天
                        updateReminderPreview(tvReminderPreview, course, minutes, switchReminder.isChecked());
                    }
                } catch (NumberFormatException e) {
                    // 忽略
                }
            }
        });

        // 开关状态变化监听
        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateReminderPreview(tvReminderPreview, course, remindMinutesArr[0], isChecked);
        });

        builder.setPositiveButton("保存", (dialog, which) -> {
            int selectedMinutes = remindMinutesArr[0]; // 从数组中获取值

            // 获取自定义时间
            if (llCustomTime.getVisibility() == View.VISIBLE &&
                    !TextUtils.isEmpty(etCustomMinutes.getText())) {
                try {
                    selectedMinutes = Integer.parseInt(etCustomMinutes.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "请输入有效的分钟数", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // 验证时间范围
            if (selectedMinutes < 1 || selectedMinutes > 1440) {
                Toast.makeText(this, "提醒时间应在1-1440分钟之间", Toast.LENGTH_SHORT).show();
                return;
            }

            // 保存提醒设置
            saveReminderSetting(course, selectedMinutes, switchReminder.isChecked());
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }
    // 更新提醒预览
    private void updateReminderPreview(TextView tvPreview, Course course, int remindMinutes, boolean isEnabled) {
        if (!isEnabled) {
            tvPreview.setText("提醒已关闭");
            tvPreview.setTextColor(Color.parseColor("#999999"));
            return;
        }

        // 计算提醒时间
        Calendar courseTime = Calendar.getInstance();
        courseTime.setTimeInMillis(course.date);

        // 设置课程具体时间
        Calendar reminderTime = (Calendar) courseTime.clone();
        String startTime = getStartTime(course.startSlot);
        String[] timeParts = startTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        reminderTime.set(Calendar.HOUR_OF_DAY, hour);
        reminderTime.set(Calendar.MINUTE, minute);
        reminderTime.set(Calendar.SECOND, 0);

        // 减去提前提醒的分钟数
        reminderTime.add(Calendar.MINUTE, -remindMinutes);

        // 格式化显示
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault());
        String previewText = String.format("提前%d分钟提醒：%s",
                remindMinutes, sdf.format(reminderTime.getTime()));

        tvPreview.setText(previewText);
        tvPreview.setTextColor(ContextCompat.getColor(this, R.color.primary_color));
    }

    // 更新下次上课时间
    private void updateNextClassTime(TextView tvNextTime, Course course) {
        Calendar now = Calendar.getInstance();
        Calendar courseTime = Calendar.getInstance();
        courseTime.setTimeInMillis(course.date);

        // 设置课程具体时间
        String startTime = getStartTime(course.startSlot);
        String[] timeParts = startTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        courseTime.set(Calendar.HOUR_OF_DAY, hour);
        courseTime.set(Calendar.MINUTE, minute);
        courseTime.set(Calendar.SECOND, 0);

        // 计算时间差
        long diffMillis = courseTime.getTimeInMillis() - now.getTimeInMillis();
        long diffMinutes = diffMillis / (60 * 1000);

        if (diffMinutes <= 0) {
            tvNextTime.setText("课程已结束或正在进行中");
        } else if (diffMinutes < 60) {
            tvNextTime.setText(String.format("下次上课：%d分钟后", diffMinutes));
        } else if (diffMinutes < 24 * 60) {
            tvNextTime.setText(String.format("下次上课：%d小时后", diffMinutes / 60));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault());
            tvNextTime.setText("下次上课：" + sdf.format(courseTime.getTime()));
        }
    }

    // 保存提醒设置
    private void saveReminderSetting(Course course, int remindMinutes, boolean isEnabled) {
        CourseDbHelper.ReminderSetting setting = new CourseDbHelper.ReminderSetting();
        setting.courseId = course.id;
        setting.remindMinutes = remindMinutes;
        setting.isEnabled = isEnabled;

        // 计算实际提醒时间
        if (isEnabled) {
            setting.remindTime = calculateReminderTime(course, remindMinutes);
        } else {
            setting.remindTime = 0;
        }

        setting.isScheduled = false; // 需要重新安排

        boolean success = dbHelper.saveReminderSetting(setting);

        if (success) {
            if (isEnabled) {
                // 安排提醒
                scheduleReminder(course, setting);
                Toast.makeText(this,
                        String.format("已设置提前%d分钟提醒", remindMinutes),
                        Toast.LENGTH_SHORT).show();
            } else {
                // 取消提醒
                cancelReminder(course.id);
                Toast.makeText(this, "已关闭课程提醒", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    // 计算提醒时间
    private long calculateReminderTime(Course course, int remindMinutes) {
        Calendar courseTime = Calendar.getInstance();
        courseTime.setTimeInMillis(course.date);

        // 设置课程具体时间
        String startTime = getStartTime(course.startSlot);
        String[] timeParts = startTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        courseTime.set(Calendar.HOUR_OF_DAY, hour);
        courseTime.set(Calendar.MINUTE, minute);
        courseTime.set(Calendar.SECOND, 0);

        // 减去提前提醒的分钟数
        courseTime.add(Calendar.MINUTE, -remindMinutes);

        return courseTime.getTimeInMillis();
    }

    // 安排提醒
    private void scheduleReminder(Course course, CourseDbHelper.ReminderSetting setting) {
        if (alarmManager == null || setting.remindTime <= 0) {
            return;
        }

        // 创建提醒Intent
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("course_id", course.id);
        intent.putExtra("course_name", course.name);
        intent.putExtra("course_location", course.location);
        intent.putExtra("remind_minutes", setting.remindMinutes);
        intent.putExtra("start_time", getStartTime(course.startSlot));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) course.id, // 使用课程ID作为请求码
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 设置闹钟
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                setting.remindTime,
                pendingIntent
        );

        // 更新数据库中的安排状态
        setting.isScheduled = true;
        dbHelper.saveReminderSetting(setting);

        Log.d("Reminder", String.format("已安排课程提醒：%s 提前%d分钟",
                course.name, setting.remindMinutes));
    }

    // 取消提醒
    private void cancelReminder(long courseId) {
        if (alarmManager == null) {
            return;
        }

        // 取消对应的PendingIntent
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) courseId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        Log.d("Reminder", "已取消课程提醒，课程ID：" + courseId);
    }

    // 安排所有提醒
    private void scheduleAllReminders() {
        if (currentSchedule == null) {
            return;
        }

        // 获取所有需要提醒的课程
        List<Course> courses = dbHelper.getCoursesWithReminders(currentSchedule.id);

        for (Course course : courses) {
            CourseDbHelper.ReminderSetting setting = dbHelper.getReminderSettingForCourse(course.id);
            if (setting != null && setting.isEnabled && !setting.isScheduled) {
                // 计算提醒时间
                setting.remindTime = calculateReminderTime(course, setting.remindMinutes);
                scheduleReminder(course, setting);
            }
        }
    }

    private void showEditCourseDialog(Course course) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("编辑课程");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_course, null);
        builder.setView(dialogView);

        // 获取控件引用
        EditText etCourseName = dialogView.findViewById(R.id.etCourseName);
        EditText etLocation = dialogView.findViewById(R.id.etLocation);
        Spinner spinnerDay = dialogView.findViewById(R.id.spinnerDay);
        Spinner spinnerStartSlot = dialogView.findViewById(R.id.spinnerStartSlot);
        Spinner spinnerEndSlot = dialogView.findViewById(R.id.spinnerEndSlot);

        // 隐藏周数选择区域（编辑时只编辑单周）
        LinearLayout weekSelectionLayout = dialogView.findViewById(R.id.llWeekSelection);
        CheckBox cbCurrentWeek = dialogView.findViewById(R.id.cbCurrentWeek);
        CheckBox cbAllWeeks = dialogView.findViewById(R.id.cbAllWeeks);
        EditText etStartWeek = dialogView.findViewById(R.id.etStartWeek);
        EditText etEndWeek = dialogView.findViewById(R.id.etEndWeek);
        Spinner spinnerWeekInterval = dialogView.findViewById(R.id.spinnerWeekInterval);

        weekSelectionLayout.setVisibility(View.GONE);
        cbCurrentWeek.setChecked(true);
        cbCurrentWeek.setEnabled(false);
        cbAllWeeks.setEnabled(false);

        // 设置原有数据
        etCourseName.setText(course.name);
        etLocation.setText(course.location);
        setupSpinners(spinnerDay, spinnerStartSlot, spinnerEndSlot);

        // 根据课程日期计算星期
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(course.date);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int dayIndex = (dayOfWeek + 5) % 7; // 转换为0-4（周一到周五）
        spinnerDay.setSelection(dayIndex);

        spinnerStartSlot.setSelection(course.startSlot - 1);
        spinnerEndSlot.setSelection(course.endSlot - 1);

        builder.setPositiveButton("保存", (dialog, which) -> {
            String courseName = etCourseName.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            int dayIndexNew = spinnerDay.getSelectedItemPosition();
            int startSlot = spinnerStartSlot.getSelectedItemPosition() + 1;
            int endSlot = spinnerEndSlot.getSelectedItemPosition() + 1;

            if (courseName.isEmpty() || location.isEmpty()) {
                Toast.makeText(MainActivity.this, "请填写完整的课程信息", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startSlot > endSlot) {
                Toast.makeText(MainActivity.this, "结束时间不能早于开始时间", Toast.LENGTH_SHORT).show();
                return;
            }

            // 更新课程信息（保持原有周数）
            course.name = courseName;
            course.location = location;
            course.startSlot = startSlot;
            course.endSlot = endSlot;

            updateCourse(course);
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showDeleteConfirmDialog(Course course) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认删除");
        builder.setMessage("确定要删除课程 '" + course.name + "' 吗？");
        builder.setPositiveButton("删除", (dialog, which) -> {
            deleteCourse(course.id);
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showCourseDetails(Course course) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(course.name);
        builder.setMessage("地点: " + course.location +
                "\n时间: 第" + course.startSlot + "-" + course.endSlot + "节" +
                "\n周次: 第" + course.week + "周" +
                "\n学期: " + course.semester);
        builder.setPositiveButton("确定", null);
        builder.show();
    }

    public void addNewCourse(Course course) {
        // 检查课程冲突
        if (courseExists(course.date, course.startSlot, course.endSlot, currentSchedule.id)) {
            Toast.makeText(this, "该时间段已有课程", Toast.LENGTH_SHORT).show();
            return;
        }

        long newId = dbHelper.addCourse(course, currentSchedule.id);
        if (newId != -1) {
            course.id = newId;
            courses.add(course);
            displayWeeklyCourseTable();
            Toast.makeText(this, "课程添加成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "课程添加失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateCourse(Course course) {
        SQLiteDatabase db = dbHelper.getDatabase();
        ContentValues values = new ContentValues();
        values.put(CourseDbHelper.COLUMN_NAME, course.name);
        values.put(CourseDbHelper.COLUMN_LOCATION, course.location);
        values.put(CourseDbHelper.COLUMN_START_SLOT, course.startSlot);
        values.put(CourseDbHelper.COLUMN_END_SLOT, course.endSlot);

        int rowsAffected = db.update(CourseDbHelper.TABLE_COURSES, values,
                CourseDbHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(course.id)});

        if (rowsAffected > 0) {
            displayWeeklyCourseTable();
            Toast.makeText(this, "课程更新成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "课程更新失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteCourse(long courseId) {
        SQLiteDatabase db = dbHelper.getDatabase();
        int rowsAffected = db.delete(CourseDbHelper.TABLE_COURSES,
                CourseDbHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(courseId)});

        if (rowsAffected > 0) {
            courses.removeIf(course -> course.id == courseId);
            displayWeeklyCourseTable();
            Toast.makeText(this, "课程删除成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "课程删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkDatabaseState() {
        List<Schedule> allSchedules = dbHelper.getAllSchedules();
        Log.d("DBState", "所有课表数量: " + allSchedules.size());

        for (Schedule schedule : allSchedules) {
            List<Course> scheduleCourses = getCoursesBySchedule(schedule.id);
            Log.d("DBState", "课表 " + schedule.name + " 有 " + scheduleCourses.size() + " 门课程");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.closeDatabase();
        }
    }
}



    // Course类定义
// 在Course类中添加scheduleId字段
    class Course {
        long id;
        long date;
        String name;
        String location;
        int startSlot;
        int endSlot;
        int week;
        String semester;
        long scheduleId; // 新增：所属课表ID
         int remindMinutes = 10; // 默认提前10分钟提醒
        boolean isReminderEnabled = true; // 提醒是否启用
        boolean isScheduled = false; // 是否已安排提醒

        public Course() {
        }

        public Course(long id, long date, String name, String location,
                      int startSlot, int endSlot, int week, String semester, long scheduleId) {
            this.id = id;
            this.date = date;
            this.name = name;
            this.location = location;
            this.startSlot = startSlot;
            this.endSlot = endSlot;
            this.week = week;
            this.semester = semester;
            this.scheduleId = scheduleId;
        }
    }

