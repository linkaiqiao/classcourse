# 上课啦！

> 一款支持多课程管理、课程表管理、上课提醒的 Android 课程表应用

## 功能特点

- **周视图课程表** - 清晰展示周一至周五课程，支持连堂显示
- **课程表基本操作** - 增、删、改、查
- **课程提醒** - 自定义提醒时间，支持提前 5/10/15/30/60 分钟提醒，可批量管理
- **多课程表管理** - 支持创建多个课程表，适用于不同学期/班级
- **自定义上课时间** - 支持自定义上课时间、课间休息时间、上课时长
- **自定义课程节数** - 支持自定义上午、下午、晚上的上课节数
- **批量操作** - 批量添加、批量删除课程
- **手势交互** - 支持左右滑动切换周次，长按编辑修改课程信息

## 技术栈

- **开发语言**：Java
- **架构模式**：MVC
- **数据存储**：SQLite
- **UI框架**：原生 XML + Material Design

## 项目结构

```
app/src/main/java/com/example/classcourse2/
├── MainActivity.java          # 主界面，课程表展示与交互
├── CourseDbHelper.java       # 数据库助手，管理课程数据
├── LoginActivity.java        # 登录页面
├── RegisterActivity.java     # 注册页面
├── ProfileActivity.java       # 个人中心
├── Schedule.java             # 课程实体类
├── ScheduleAdapter.java      # 课程列表适配器
├── ReminderReceiver.java     # 闹钟提醒接收器
├── TimeSetting.java          # 时间设置
├── AgreementActivity.java    # 用户协议
├── PrivacyActivity.java      # 隐私政策
├── FeedbackActivity.java     # 反馈页面
├── HelpActivity.java         # 帮助页面
├── CustomerServiceActivity.java  # 客服页面
├── SystemMessageActivity.java   # 系统消息
└── SplashActivity.java        # 启动页
```

## 快速开始

### 环境要求

- Android Studio Hedgehog 或更高版本
- JDK 11 或更高版本
- Android SDK 21 (Android 5.0) 或更高版本

### 运行项目

1. 克隆项目到本地
2. 用 Android Studio 打开项目
3. 等待 Gradle 同步完成
4. 连接设备或启动模拟器
5. 点击运行按钮

## 应用截图

<img width="1200" height="2670" alt="753bec804dd627dc240d6cb93e3bb40e" src="https://github.com/user-attachments/assets/75ca1515-454d-44da-b218-a5bfb3c72d4a" />
详细应用截图：https://linkaiqiao.github.io/

## License

This project is for educational purposes.
