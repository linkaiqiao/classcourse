# classcourse（上课啦！）

> 一款支持多课程管理、多课程表管理、上课提醒的Android课程表应用


## 功能特点

- **周视图课程表** :清晰展示周一至周五课程，支持连堂显示
- **课程表基本操作** ：增、删、改、查
- **课程提醒**：自定课程提醒，支持提前 5/10/15/30/60 分钟提醒，可批量管理。
- **多课程表管理** ：支持创建多个课程表，适用不同学期/班级
- **自定义上课时间**；支持自定义上课时间，课间休息时间，上课时长
- **自定义课程节数**：支持自定义上午上课、下午、晚上的上课节数
- **批量操作**：批量添加、批量删除
- **手势交互**：支持左右滑动切换周次，长按支持编辑修改课程信息

## 技术栈
- **开啊发语言**：Java
- **架构模式**：MVC
- **数据存储**：SQLite
- **UI框架**：原生XML+Material Design

## 项目结构
app/src/main/java/com/example/classcourse2/
|---AgreementActivity
|---CourdrDbHelp
|---CustomerSevviceActivity
|---FeedbackActivity
|---HelpActivity
|---LoginActivity
|---MainActivity
|---PolicyManager
|---PrivacyActivity
|---ProfileActivity
|---RegisterActivity
|---RemindReceiver
|---Shedule
|---SheduleAdapter
|---SplashActivity
|---SystemMessage
|---SystemMessageActivity
|---SystemMessageAdapter
|---TimeSetting

## 核心实现

### 1.动态绘制课表
、、、 Java
  private void createCourseTable() {
   llCourseTableContainer.removeAllViews();//清理容器：移除所有现有视图，准备重新构建
        if (currentPeriodSetting == null) {
            loadPeriodSettings();
        }
        int totalPeriods = currentPeriodSetting.totalPeriods;//获取总节数
        Log.d("CourseTable", "创建课程表，总节数: " + totalPeriods);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;//获取屏幕宽度
        int timeColumnWidth = dpToPx(80);//时间栏固定宽度
        int courseColumnWidth = (screenWidth - timeColumnWidth) / 5;//计算
        //循环创建每节课的行
        for (int slot = 1; slot <= totalPeriods; slot++) {
            LinearLayout tableRow = new LinearLayout(this);//创建单个表格行
            // 设置行布局参数：宽度填满父容器，高度固定80dp
            tableRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(80)
            ));
            tableRow.setOrientation(LinearLayout.HORIZONTAL);// 水平排列子视图
            tableRow.setBackgroundColor(Color.parseColor("#FFFFFF"));//白色背景板
            addTimeSlotCell(tableRow, slot, timeColumnWidth);
            for (int day = 0; day < 5; day++) {
                addCourseCell(tableRow, day, slot, courseColumnWidth);
            }
            //添加最左侧的时间单元格
            llCourseTableContainer.addView(tableRow);
            if (slot < totalPeriods) {
                // 创建分隔线视图
                View separator = new View(this);
                separator.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(1)
                ));
                separator.setBackgroundColor(Color.parseColor("#E0E0E0"));
                llCourseTableContainer.addView(separator);
            }
        }
    }

### 2.手势导航

* 实现原理：
* 1. 使用 GestureDetector 检测滑动手势
* 2. 通过 onFling() 方法识别快速滑动动作
* 3. 判断滑动方向（水平 vs 垂直）和距离
* 4. 调用相应的周数切换方法

* 手势参数说明：
* - SWIPE_MIN_DISTANCE: 最小滑动距离（60像素）
* - SWIPE_MIN_VELOCITY: 最小滑动速度（100像素/秒）

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

### 3.课程表的基本操作

