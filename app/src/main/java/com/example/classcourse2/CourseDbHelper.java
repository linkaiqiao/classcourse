package com.example.classcourse2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CourseDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "course_schedule.db";
    private static final int DATABASE_VERSION = 6;

    // 单例实例
    private static CourseDbHelper instance;
    private SQLiteDatabase database;
    public static final String TABLE_PERIOD_SETTINGS = "period_settings";
    // 表名和列名常量
    public static final String TABLE_COURSES = "courses";
    public static final String TABLE_SCHEDULES = "schedules";
    public static final String TABLE_USERS = "users";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_START_SLOT = "start_slot";
    public static final String COLUMN_END_SLOT = "end_slot";
    public static final String COLUMN_WEEK = "week";
    public static final String COLUMN_SEMESTER = "semester";
    public static final String COLUMN_SCHEDULE_ID = "schedule_id";

    public static final String COLUMN_SCHEDULE_NAME = "schedule_name";
    public static final String COLUMN_SCHEDULE_SEMESTER = "semester";
    public static final String COLUMN_SCHEDULE_YEAR = "year";
    public static final String COLUMN_IS_ACTIVE = "is_active";
    public static final String COLUMN_CREATE_TIME = "create_time";

    public static final String COLUMN_STUDENT_ID = "student_id";
    public static final String COLUMN_PASSWORD = "password";
    public static final String TABLE_TIME_SETTINGS = "time_settings";

    public static final String COLUMN_SETTING_ID = "setting_id";
    public static final String COLUMN_MORNING_START_TIME = "morning_start_time";
    public static final String COLUMN_MORNING_DURATION = "morning_duration";
    public static final String COLUMN_MORNING_BREAK = "morning_break";
    public static final String COLUMN_AFTERNOON_START_TIME = "afternoon_start_time";
    public static final String COLUMN_AFTERNOON_DURATION = "afternoon_duration";
    public static final String COLUMN_AFTERNOON_BREAK = "afternoon_break";
    public static final String COLUMN_EVENING_START_TIME = "evening_start_time";
    public static final String COLUMN_EVENING_DURATION = "evening_duration";
    public static final String COLUMN_IS_DEFAULT = "is_default";

    // 节数配置相关列
    public static final String COLUMN_PERIOD_ID = "period_id";
    public static final String COLUMN_MORNING_PERIODS = "morning_periods";
    public static final String COLUMN_AFTERNOON_PERIODS = "afternoon_periods";
    public static final String COLUMN_EVENING_PERIODS = "evening_periods";
    public static final String COLUMN_TOTAL_PERIODS = "total_periods";

    public static final String TABLE_REMINDER_SETTINGS = "reminder_settings";
    // 提醒设置相关列
    public static final String COLUMN_REMINDER_ID = "reminder_id";
    public static final String COLUMN_COURSE_ID = "course_id";
    public static final String COLUMN_REMIND_MINUTES = "remind_minutes"; // 提前提醒的分钟数
    public static final String COLUMN_IS_ENABLED = "is_enabled"; // 提醒是否启用
    public static final String COLUMN_REMIND_TIME = "remind_time"; // 实际提醒时间
    public static final String COLUMN_IS_SCHEDULED = "is_scheduled"; // 是否已安排

    // 创建提醒设置表的SQL
    private static final String CREATE_REMINDER_SETTINGS_TABLE =
            "CREATE TABLE " + TABLE_REMINDER_SETTINGS + " (" +
                    COLUMN_REMINDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_COURSE_ID + " INTEGER NOT NULL, " +
                    COLUMN_REMIND_MINUTES + " INTEGER DEFAULT 10, " + // 默认提前10分钟
                    COLUMN_IS_ENABLED + " INTEGER DEFAULT 1, " + // 默认启用
                    COLUMN_REMIND_TIME + " INTEGER, " + // 实际提醒时间戳
                    COLUMN_IS_SCHEDULED + " INTEGER DEFAULT 0, " + // 默认未安排
                    "FOREIGN KEY (" + COLUMN_COURSE_ID + ") REFERENCES " +
                    TABLE_COURSES + "(" + COLUMN_ID + "))";


    // 创建节数配置表的SQL
    private static final String CREATE_PERIOD_SETTINGS_TABLE =
            "CREATE TABLE " + TABLE_PERIOD_SETTINGS + " (" +
                    COLUMN_PERIOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MORNING_PERIODS + " INTEGER DEFAULT 4, " +
                    COLUMN_AFTERNOON_PERIODS + " INTEGER DEFAULT 4, " +
                    COLUMN_EVENING_PERIODS + " INTEGER DEFAULT 4, " +
                    COLUMN_TOTAL_PERIODS + " INTEGER DEFAULT 12, " +
                    COLUMN_SCHEDULE_ID + " INTEGER NOT NULL, " +
                    "FOREIGN KEY (" + COLUMN_SCHEDULE_ID + ") REFERENCES " +
                    TABLE_SCHEDULES + "(" + COLUMN_ID + "))";

    // 创建时间设置表的SQL
    private static final String CREATE_TIME_SETTINGS_TABLE =
            "CREATE TABLE " + TABLE_TIME_SETTINGS + " (" +
                    COLUMN_SETTING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MORNING_START_TIME + " TEXT NOT NULL, " +
                    COLUMN_MORNING_DURATION + " INTEGER NOT NULL, " +
                    COLUMN_MORNING_BREAK + " INTEGER NOT NULL, " +
                    COLUMN_AFTERNOON_START_TIME + " TEXT NOT NULL, " +
                    COLUMN_AFTERNOON_DURATION + " INTEGER NOT NULL, " +
                    COLUMN_AFTERNOON_BREAK + " INTEGER NOT NULL, " +
                    COLUMN_EVENING_START_TIME + " TEXT NOT NULL, " +
                    COLUMN_EVENING_DURATION + " INTEGER NOT NULL, " +
                    COLUMN_IS_DEFAULT + " INTEGER DEFAULT 0, " +
                    COLUMN_SCHEDULE_ID + " INTEGER NOT NULL, " +
                    "FOREIGN KEY (" + COLUMN_SCHEDULE_ID + ") REFERENCES " +
                    TABLE_SCHEDULES + "(" + COLUMN_ID + "))";

    // SQL语句
    private static final String CREATE_COURSES_TABLE =
            "CREATE TABLE " + TABLE_COURSES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_LOCATION + " TEXT NOT NULL, " +
                    COLUMN_DATE + " INTEGER NOT NULL, " +
                    COLUMN_START_SLOT + " INTEGER NOT NULL, " +
                    COLUMN_END_SLOT + " INTEGER NOT NULL, " +
                    COLUMN_WEEK + " INTEGER, " +
                    COLUMN_SEMESTER + " TEXT, " +
                    COLUMN_SCHEDULE_ID + " INTEGER NOT NULL DEFAULT 1, " +
                    "FOREIGN KEY (" + COLUMN_SCHEDULE_ID + ") REFERENCES " +
                    TABLE_SCHEDULES + "(" + COLUMN_ID + "))";

    private static final String CREATE_SCHEDULES_TABLE =
            "CREATE TABLE " + TABLE_SCHEDULES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SCHEDULE_NAME + " TEXT NOT NULL, " +
                    COLUMN_SCHEDULE_SEMESTER + " TEXT NOT NULL, " +
                    COLUMN_SCHEDULE_YEAR + " TEXT NOT NULL, " +
                    COLUMN_IS_ACTIVE + " INTEGER DEFAULT 0, " +
                    COLUMN_CREATE_TIME + " INTEGER NOT NULL" +
                    ")";

    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_STUDENT_ID + " TEXT UNIQUE NOT NULL, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL)";

    // 私有构造函数
    private CourseDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // 获取单例实例
    public static synchronized CourseDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CourseDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    // 获取数据库连接（单例管理）
    public synchronized SQLiteDatabase getDatabase() {
        if (database == null || !database.isOpen()) {
            database = getWritableDatabase();
        }
        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SCHEDULES_TABLE);
        db.execSQL(CREATE_COURSES_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
        createDefaultSchedule(db);
        addTestUser(db);
        createDefaultTimeSettings(db);
        db.execSQL(CREATE_PERIOD_SETTINGS_TABLE); // 新增
        createDefaultPeriodSettings(db); // 新增
        db.execSQL(CREATE_REMINDER_SETTINGS_TABLE); // 新增


    }
    private void createDefaultTimeSettings(SQLiteDatabase db) {
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_MORNING_START_TIME, "08:00");
            values.put(COLUMN_MORNING_DURATION, 45);
            values.put(COLUMN_MORNING_BREAK, 10);
            values.put(COLUMN_AFTERNOON_START_TIME, "14:00");
            values.put(COLUMN_AFTERNOON_DURATION, 45);
            values.put(COLUMN_AFTERNOON_BREAK, 10);
            values.put(COLUMN_EVENING_START_TIME, "19:00");
            values.put(COLUMN_EVENING_DURATION, 45);
            values.put(COLUMN_IS_DEFAULT, 1);
            values.put(COLUMN_SCHEDULE_ID, 1);

            long result = db.insert(TABLE_TIME_SETTINGS, null, values);
            Log.d("TimeSettings", "创建默认时间设置，结果: " + result);
        } catch (Exception e) {
            Log.e("TimeSettings", "创建默认时间设置错误: " + e.getMessage());
        }
    }
    public static class TimeSetting {
        public long settingId;
        public String morningStartTime;
        public int morningDuration;
        public int morningBreak;
        public String afternoonStartTime;
        public int afternoonDuration;
        public int afternoonBreak;
        public String eveningStartTime;
        public int eveningDuration;
        public boolean isDefault;
        public long scheduleId;
        public TimeSetting() {
            // 默认值
            this.morningStartTime = "08:00";
            this.morningDuration = 45;
            this.morningBreak = 10;
            this.afternoonStartTime = "14:00";
            this.afternoonDuration = 45;
            this.afternoonBreak = 10;
            this.eveningStartTime = "19:00";
            this.eveningDuration = 45;
            this.isDefault = true;
        }
    }
    public TimeSetting getTimeSettingBySchedule(long scheduleId) {
        SQLiteDatabase db = getDatabase();
        TimeSetting setting = null;

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_TIME_SETTINGS, null,
                    COLUMN_SCHEDULE_ID + " = ?",
                    new String[]{String.valueOf(scheduleId)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                setting = new TimeSetting();
                setting.settingId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SETTING_ID));
                setting.morningStartTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MORNING_START_TIME));
                setting.morningDuration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MORNING_DURATION));
                setting.morningBreak = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MORNING_BREAK));
                setting.afternoonStartTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AFTERNOON_START_TIME));
                setting.afternoonDuration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AFTERNOON_DURATION));
                setting.afternoonBreak = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AFTERNOON_BREAK));
                setting.eveningStartTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENING_START_TIME));
                setting.eveningDuration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENING_DURATION));
                setting.isDefault = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_DEFAULT)) == 1;
                setting.scheduleId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SCHEDULE_ID));

                Log.d("TimeSettings", "从数据库加载时间设置成功");
            } else {
                Log.d("TimeSettings", "数据库中没有找到时间设置，返回null");
            }
        } catch (Exception e) {
            Log.e("TimeSettings", "加载时间设置错误: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return setting;
    }
    public boolean saveTimeSetting(TimeSetting setting) {
        if (setting == null) {
            Log.e("TimeSettings", "保存时间设置失败：setting为null");
            return false;
        }

        SQLiteDatabase db = getDatabase();

        try {
            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(COLUMN_MORNING_START_TIME, setting.morningStartTime);
            values.put(COLUMN_MORNING_DURATION, setting.morningDuration);
            values.put(COLUMN_MORNING_BREAK, setting.morningBreak);
            values.put(COLUMN_AFTERNOON_START_TIME, setting.afternoonStartTime);
            values.put(COLUMN_AFTERNOON_DURATION, setting.afternoonDuration);
            values.put(COLUMN_AFTERNOON_BREAK, setting.afternoonBreak);
            values.put(COLUMN_EVENING_START_TIME, setting.eveningStartTime);
            values.put(COLUMN_EVENING_DURATION, setting.eveningDuration);
            values.put(COLUMN_IS_DEFAULT, setting.isDefault ? 1 : 0);
            values.put(COLUMN_SCHEDULE_ID, setting.scheduleId);

            // 检查是否已存在该课表的时间设置
            TimeSetting existing = getTimeSettingBySchedule(setting.scheduleId);
            int rowsAffected;

            if (existing != null && existing.settingId != 0) {
                // 更新现有设置
                rowsAffected = db.update(TABLE_TIME_SETTINGS, values,
                        COLUMN_SETTING_ID + " = ?",
                        new String[]{String.valueOf(existing.settingId)});
                Log.d("TimeSettings", "更新现有时间设置，影响行数: " + rowsAffected);
            } else {
                // 插入新设置
                long result = db.insert(TABLE_TIME_SETTINGS, null, values);
                rowsAffected = result != -1 ? 1 : 0;
                Log.d("TimeSettings", "插入新时间设置，结果: " + result);
            }

            db.setTransactionSuccessful();
            return rowsAffected > 0;

        } catch (Exception e) {
            Log.e("TimeSettings", "保存时间设置错误: " + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }

    private TimeSetting getDefaultTimeSetting() {
        TimeSetting setting = new TimeSetting();
        setting.morningStartTime = "08:00";
        setting.morningDuration = 45;
        setting.morningBreak = 10;
        setting.afternoonStartTime = "14:00";
        setting.afternoonDuration = 45;
        setting.afternoonBreak = 10;
        setting.eveningStartTime = "19:00";
        setting.eveningDuration = 45;
        setting.isDefault = true;
        return setting;
    }
    @Override

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseUpgrade", "正在升级数据库: 从版本 " + oldVersion + " 到 " + newVersion);

        if (oldVersion < 2) {
            try {
                db.beginTransaction();
                db.execSQL(CREATE_SCHEDULES_TABLE);
                db.execSQL("ALTER TABLE " + TABLE_COURSES + " ADD COLUMN " +
                        COLUMN_SCHEDULE_ID + " INTEGER NOT NULL DEFAULT 1");
                createDefaultSchedule(db);

                ContentValues values = new ContentValues();
                values.put(COLUMN_SCHEDULE_ID, 1);
                db.update(TABLE_COURSES, values, null, null);

                db.setTransactionSuccessful();
                Log.d("DatabaseUpgrade", "数据库升级到版本2完成");
            } finally {
                db.endTransaction();
            }
        }

        if (oldVersion < 3) {
            try {
                db.beginTransaction();
                db.execSQL(CREATE_USERS_TABLE);
                addTestUser(db);
                db.setTransactionSuccessful();
                Log.d("DatabaseUpgrade", "数据库升级到版本3，添加用户表");
            } finally {
                db.endTransaction();
            }
        }

        // 添加时间设置表的升级
        if (oldVersion < 4) {
            try {
                db.beginTransaction();
                db.execSQL(CREATE_TIME_SETTINGS_TABLE);
                createDefaultTimeSettings(db);
                db.setTransactionSuccessful();
                Log.d("DatabaseUpgrade", "数据库升级到版本4，添加时间设置表");
            } finally {
                db.endTransaction();
            }
        }
        if (oldVersion < 5) {
            try {
                db.beginTransaction();
                // 创建时间设置表（如果之前没创建）
                try {
                    db.execSQL(CREATE_TIME_SETTINGS_TABLE);
                    Log.d("DatabaseUpgrade", "创建 time_settings 表");
                } catch (Exception e) {
                    Log.d("DatabaseUpgrade", "time_settings 表已存在");
                }

                // 创建节数设置表
                try {
                    db.execSQL(CREATE_PERIOD_SETTINGS_TABLE);
                    Log.d("DatabaseUpgrade", "创建 period_settings 表");
                } catch (Exception e) {
                    Log.d("DatabaseUpgrade", "period_settings 表已存在");
                }

                // 创建默认设置
                createDefaultTimeSettings(db);
                createDefaultPeriodSettings(db);

                db.setTransactionSuccessful();
                Log.d("DatabaseUpgrade", "数据库升级到版本5，添加节数设置表");
            } finally {
                db.endTransaction();
            }
        }
        if (oldVersion < 6) {
            try {
                db.beginTransaction();

                // 创建提醒设置表
                db.execSQL(CREATE_REMINDER_SETTINGS_TABLE);
                Log.d("DatabaseUpgrade", "创建 reminder_settings 表");

                db.setTransactionSuccessful();
                Log.d("DatabaseUpgrade", "数据库升级到版本6，添加提醒设置表");
            } finally {
                db.endTransaction();
            }
        }
    }

    // 用户管理方法
    public boolean registerUser(String studentId, String password) {
        SQLiteDatabase db = getDatabase();

        try {
            // 检查用户是否已存在
            Cursor cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_STUDENT_ID},
                    COLUMN_STUDENT_ID + " = ?",
                    new String[]{studentId},
                    null, null, null);

            boolean exists = cursor.getCount() > 0;
            cursor.close();

            if (exists) {
                return false;
            }

            ContentValues values = new ContentValues();
            values.put(COLUMN_STUDENT_ID, studentId);
            values.put(COLUMN_PASSWORD, password);

            long result = db.insert(TABLE_USERS, null, values);
            return result != -1;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isUserExists(String studentId) {
        SQLiteDatabase db = getDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_STUDENT_ID},
                    COLUMN_STUDENT_ID + " = ?",
                    new String[]{studentId},
                    null, null, null);

            return cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean validateUser(String studentId, String password) {
        SQLiteDatabase db = getDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_ID},
                    COLUMN_STUDENT_ID + " = ? AND " + COLUMN_PASSWORD + " = ?",
                    new String[]{studentId, password},
                    null, null, null);

            return cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void debugUsersTable() {
        SQLiteDatabase db = getDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
            Log.d("UserDebug", "用户表记录数: " + cursor.getCount());

            if (cursor.moveToFirst()) {
                do {
                    String studentId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_ID));
                    String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                    Log.d("UserDebug", "用户: " + studentId + ", 密码: " + password);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // 课表管理方法
    private void createDefaultSchedule(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCHEDULE_NAME, "默认课表");
        values.put(COLUMN_SCHEDULE_SEMESTER, "第1学期");
        values.put(COLUMN_SCHEDULE_YEAR, "2025-2026");
        values.put(COLUMN_IS_ACTIVE, 1);
        values.put(COLUMN_CREATE_TIME, System.currentTimeMillis());
        db.insert(TABLE_SCHEDULES, null, values);
    }

    private void addTestUser(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_STUDENT_ID, "admin");
        values.put(COLUMN_PASSWORD, "123456");
        db.insert(TABLE_USERS, null, values);
    }

    public long getSemesterStartDate(String year, String semester) {
        Calendar cal = Calendar.getInstance();

        // 解析年份
        int startYear = Calendar.getInstance().get(Calendar.YEAR);
        try {
            if (year != null && !year.trim().isEmpty()) {
                String cleanedYear = year.replaceAll("[^0-9-]", "");
                if (!cleanedYear.isEmpty()) {
                    startYear = Integer.parseInt(cleanedYear.split("-")[0]);
                }
            }
        } catch (NumberFormatException e) {
            Log.e("YearParse", "年份解析失败: " + year, e);
        }

        // ✅ 根据您的需求：将3月1日设为学期开始
        // 无论是什么学期，都从3月1日开始
        cal.set(startYear, Calendar.MARCH, 1);  // 3月1日

        // 将日期调整到当周周一
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek != Calendar.MONDAY) {
            int daysToMonday = (Calendar.MONDAY - dayOfWeek + 7) % 7;
            cal.add(Calendar.DAY_OF_MONTH, daysToMonday);
        }

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Log.d("SemesterStart", "学期开始日期（调整后）：" +
                new SimpleDateFormat("yyyy-MM-dd EEEE", Locale.getDefault()).format(cal.getTime()));

        return cal.getTimeInMillis();
//        Calendar cal = Calendar.getInstance();
//        String cleanedYear = year.replaceAll("[^0-9-]", "");
//
//        if (cleanedYear.isEmpty()) {
//            cleanedYear = "2025-2026";
//        }
//
//        int startYear;
//        try {
//            startYear = Integer.parseInt(cleanedYear.split("-")[0]);
//        } catch (NumberFormatException e) {
//            startYear = Calendar.getInstance().get(Calendar.YEAR);
//        }
//
//        switch (semester) {
//            case "第1学期": cal.set(startYear, Calendar.SEPTEMBER, 1); break;
//            case "第2学期": cal.set(startYear + 1, Calendar.FEBRUARY, 15); break;
//            case "暑假学期": cal.set(startYear + 1, Calendar.JULY, 1); break;
//            case "寒假学期": cal.set(startYear + 1, Calendar.JANUARY, 15); break;
//            default: cal.set(startYear, Calendar.SEPTEMBER, 1);
//        }
//
//        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
//        cal.set(Calendar.HOUR_OF_DAY, 0);
//        cal.set(Calendar.MINUTE, 0);
//        cal.set(Calendar.SECOND, 0);
//        cal.set(Calendar.MILLISECOND, 0);
//
//        return cal.getTimeInMillis();
    }

    public int calculateCurrentWeek(String year, String semester) {
        long semesterStart = getSemesterStartDate(year, semester);
        long currentTime = System.currentTimeMillis();

        // 添加调试日志
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd EEEE", Locale.getDefault());
        Log.d("WeekCalculation", "学期开始日期：" + sdf.format(new Date(semesterStart)));
        Log.d("WeekCalculation", "当前日期：" + sdf.format(new Date(currentTime)));

        long diff = currentTime - semesterStart;
        int weeks = (int) (diff / (7 * 24 * 60 * 60 * 1000L)) + 1;
        weeks = Math.max(1, Math.min(30, weeks));

        Log.d("WeekCalculation", "数据库计算的周数：" + weeks);
        return weeks;
    }
    // 其他课表管理方法（保持原有逻辑，但使用单例数据库连接）
    public long addSchedule(String name, String semester, String year) {
        SQLiteDatabase db = getDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCHEDULE_NAME, name);
        values.put(COLUMN_SCHEDULE_SEMESTER, semester);
        values.put(COLUMN_SCHEDULE_YEAR, year);
        values.put(COLUMN_IS_ACTIVE, 0);
        values.put(COLUMN_CREATE_TIME, System.currentTimeMillis());
        return db.insert(TABLE_SCHEDULES, null, values);
    }

    public List<Schedule> getAllSchedules() {
        List<Schedule> schedules = new ArrayList<>();
        SQLiteDatabase db = getDatabase();
        Cursor cursor = db.query(TABLE_SCHEDULES, null, null, null, null, null, COLUMN_CREATE_TIME + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Schedule schedule = new Schedule();
                schedule.id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                schedule.name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SCHEDULE_NAME));
                schedule.semester = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SCHEDULE_SEMESTER));
                schedule.year = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SCHEDULE_YEAR));
                schedule.isActive = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)) == 1;
                schedule.createTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATE_TIME));
                schedules.add(schedule);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return schedules;
    }

    public boolean setActiveSchedule(long scheduleId) {
        SQLiteDatabase db = getDatabase();
        try {
            db.beginTransaction();
            ContentValues resetValues = new ContentValues();
            resetValues.put(COLUMN_IS_ACTIVE, 0);
            db.update(TABLE_SCHEDULES, resetValues, null, null);

            ContentValues activeValues = new ContentValues();
            activeValues.put(COLUMN_IS_ACTIVE, 1);
            int rowsAffected = db.update(TABLE_SCHEDULES, activeValues,
                    COLUMN_ID + " = ?", new String[]{String.valueOf(scheduleId)});

            db.setTransactionSuccessful();
            return rowsAffected > 0;
        } finally {
            db.endTransaction();
        }
    }
    // 删除课表及其相关课程
    public boolean deleteSchedule(long scheduleId) {
        SQLiteDatabase db = getDatabase();

        try {
            db.beginTransaction();

            // 先删除该课表下的所有课程
            db.delete(TABLE_COURSES,
                    COLUMN_SCHEDULE_ID + " = ?",
                    new String[]{String.valueOf(scheduleId)});

            // 删除课表
            int rowsAffected = db.delete(TABLE_SCHEDULES,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(scheduleId)});

            db.setTransactionSuccessful();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }
    public Schedule getActiveSchedule() {
        SQLiteDatabase db = getDatabase();
        Schedule schedule = null;
        Cursor cursor = db.query(TABLE_SCHEDULES, null, COLUMN_IS_ACTIVE + " = 1", null, null, null, null);

        if (cursor.moveToFirst()) {
            schedule = new Schedule();
            schedule.id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
            schedule.name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SCHEDULE_NAME));
            schedule.semester = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SCHEDULE_SEMESTER));
            schedule.year = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SCHEDULE_YEAR));
            schedule.isActive = true;
            schedule.createTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATE_TIME));
        }
        cursor.close();
        return schedule;
    }
    // 在 CourseDbHelper 类中添加批量添加方法
    public int addCoursesInBatch(List<Course> courses, long scheduleId) {
        SQLiteDatabase db = getDatabase();
        int successCount = 0;

        try {
            db.beginTransaction();

            for (Course course : courses) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME, course.name);
                values.put(COLUMN_LOCATION, course.location);
                values.put(COLUMN_DATE, course.date);
                values.put(COLUMN_START_SLOT, course.startSlot);
                values.put(COLUMN_END_SLOT, course.endSlot);
                values.put(COLUMN_WEEK, course.week);
                values.put(COLUMN_SEMESTER, course.semester);
                values.put(COLUMN_SCHEDULE_ID, scheduleId);

                long result = db.insert(TABLE_COURSES, null, values);
                if (result != -1) {
                    successCount++;
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return successCount;
    }
    // 批量删除课程
    public boolean deleteCoursesInBatch(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = getDatabase();

        try {
            db.beginTransaction();

            // 构建 IN 语句的参数
            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < courseIds.size(); i++) {
                if (i > 0) placeholders.append(",");
                placeholders.append("?");
            }

            String[] args = new String[courseIds.size()];
            for (int i = 0; i < courseIds.size(); i++) {
                args[i] = String.valueOf(courseIds.get(i));
            }

            // 执行批量删除
            int rowsAffected = db.delete(TABLE_COURSES,
                    COLUMN_ID + " IN (" + placeholders.toString() + ")", args);

            db.setTransactionSuccessful();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // 根据课表ID批量删除课程
    public boolean deleteCoursesByScheduleId(long scheduleId) {
        SQLiteDatabase db = getDatabase();

        try {
            db.beginTransaction();
            int rowsAffected = db.delete(TABLE_COURSES,
                    COLUMN_SCHEDULE_ID + " = ?",
                    new String[]{String.valueOf(scheduleId)});

            db.setTransactionSuccessful();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // 根据周数批量删除课程
    public boolean deleteCoursesByWeek(long scheduleId, int week) {
        SQLiteDatabase db = getDatabase();

        try {
            db.beginTransaction();
            int rowsAffected = db.delete(TABLE_COURSES,
                    COLUMN_SCHEDULE_ID + " = ? AND " + COLUMN_WEEK + " = ?",
                    new String[]{String.valueOf(scheduleId), String.valueOf(week)});

            db.setTransactionSuccessful();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // 根据周数范围批量删除课程
    public boolean deleteCoursesByWeekRange(long scheduleId, int startWeek, int endWeek) {
        SQLiteDatabase db = getDatabase();

        try {
            db.beginTransaction();
            int rowsAffected = db.delete(TABLE_COURSES,
                    COLUMN_SCHEDULE_ID + " = ? AND " + COLUMN_WEEK + " BETWEEN ? AND ?",
                    new String[]{String.valueOf(scheduleId),
                            String.valueOf(startWeek),
                            String.valueOf(endWeek)});

            db.setTransactionSuccessful();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
        }
    }

    // 课程管理方法（同样使用单例数据库连接）
    public long addCourse(Course course, long scheduleId) {
        SQLiteDatabase db = getDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, course.name);
        values.put(COLUMN_LOCATION, course.location);
        values.put(COLUMN_DATE, course.date);
        values.put(COLUMN_START_SLOT, course.startSlot);
        values.put(COLUMN_END_SLOT, course.endSlot);
        values.put(COLUMN_WEEK, course.week);
        values.put(COLUMN_SEMESTER, course.semester);
        values.put(COLUMN_SCHEDULE_ID, scheduleId);
        return db.insert(TABLE_COURSES, null, values);
    }

    // 其他方法类似，都使用 getDatabase() 而不是新建连接...

    // 关闭数据库连接（可选，通常不需要手动调用）
    public synchronized void closeDatabase() {
        if (database != null && database.isOpen()) {
            database.close();
            database = null;
        }
    }
    // 在 CourseDbHelper 中添加
    public static class PeriodSetting {
        public long periodId;
        public int morningPeriods;
        public int afternoonPeriods;
        public int eveningPeriods;
        public int totalPeriods;
        public long scheduleId;

        public PeriodSetting() {
            this.morningPeriods = 4;
            this.afternoonPeriods = 4;
            this.eveningPeriods = 4;
            this.totalPeriods = 12;
        }
    }

    // 创建默认节数设置
    private void createDefaultPeriodSettings(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_MORNING_PERIODS, 4);
        values.put(COLUMN_AFTERNOON_PERIODS, 4);
        values.put(COLUMN_EVENING_PERIODS, 4);
        values.put(COLUMN_TOTAL_PERIODS, 12);
        values.put(COLUMN_SCHEDULE_ID, 1);
        db.insert(TABLE_PERIOD_SETTINGS, null, values);
    }

    // 获取课表的节数设置
    public PeriodSetting getPeriodSettingBySchedule(long scheduleId) {
        SQLiteDatabase db = getDatabase();
        PeriodSetting setting = null;

        Cursor cursor = db.query(TABLE_PERIOD_SETTINGS, null,
                COLUMN_SCHEDULE_ID + " = ?",
                new String[]{String.valueOf(scheduleId)}, null, null, null);

        if (cursor.moveToFirst()) {
            setting = new PeriodSetting();
            setting.periodId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PERIOD_ID));
            setting.morningPeriods = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MORNING_PERIODS));
            setting.afternoonPeriods = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AFTERNOON_PERIODS));
            setting.eveningPeriods = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EVENING_PERIODS));
            setting.totalPeriods = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_PERIODS));
            setting.scheduleId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SCHEDULE_ID));
        } else {
            // 没有找到设置，返回默认值
            setting = new PeriodSetting();
            setting.scheduleId = scheduleId;
        }

        cursor.close();
        return setting;
    }
    // 在 CourseDbHelper 中添加
    public static class ReminderSetting {
        public long reminderId;
        public long courseId;
        public int remindMinutes; // 提前提醒分钟数
        public boolean isEnabled; // 是否启用
        public long remindTime; // 实际提醒时间
        public boolean isScheduled; // 是否已安排

        public ReminderSetting() {
            this.remindMinutes = 10; // 默认10分钟
            this.isEnabled = true;
            this.isScheduled = false;
        }
    }

    // 获取课程的提醒设置
    public ReminderSetting getReminderSettingForCourse(long courseId) {
        SQLiteDatabase db = getDatabase();
        ReminderSetting setting = null;

        Cursor cursor = db.query(TABLE_REMINDER_SETTINGS, null,
                COLUMN_COURSE_ID + " = ?",
                new String[]{String.valueOf(courseId)}, null, null, null);

        if (cursor.moveToFirst()) {
            setting = new ReminderSetting();
            setting.reminderId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_ID));
            setting.courseId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COURSE_ID));
            setting.remindMinutes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMIND_MINUTES));
            setting.isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ENABLED)) == 1;
            setting.remindTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_REMIND_TIME));
            setting.isScheduled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_SCHEDULED)) == 1;
        } else {
            // 没有找到设置，返回默认值
            setting = new ReminderSetting();
            setting.courseId = courseId;
        }

        cursor.close();
        return setting;
    }

    // 保存提醒设置
    public boolean saveReminderSetting(ReminderSetting setting) {
        SQLiteDatabase db = getDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_COURSE_ID, setting.courseId);
        values.put(COLUMN_REMIND_MINUTES, setting.remindMinutes);
        values.put(COLUMN_IS_ENABLED, setting.isEnabled ? 1 : 0);
        values.put(COLUMN_REMIND_TIME, setting.remindTime);
        values.put(COLUMN_IS_SCHEDULED, setting.isScheduled ? 1 : 0);

        // 检查是否已存在该课程的提醒设置
        ReminderSetting existing = getReminderSettingForCourse(setting.courseId);
        if (existing != null && existing.reminderId != 0) {
            // 更新现有设置
            int rows = db.update(TABLE_REMINDER_SETTINGS, values,
                    COLUMN_REMINDER_ID + " = ?",
                    new String[]{String.valueOf(existing.reminderId)});
            return rows > 0;
        } else {
            // 插入新设置
            long result = db.insert(TABLE_REMINDER_SETTINGS, null, values);
            return result != -1;
        }
    }

    // 获取所有需要提醒的课程
    public List<Course> getCoursesWithReminders(long scheduleId) {
        SQLiteDatabase db = getDatabase();
        List<Course> courses = new ArrayList<>();

        String query = "SELECT c.*, r." + COLUMN_REMIND_MINUTES + ", r." + COLUMN_IS_ENABLED +
                " FROM " + TABLE_COURSES + " c " +
                " LEFT JOIN " + TABLE_REMINDER_SETTINGS + " r " +
                " ON c." + COLUMN_ID + " = r." + COLUMN_COURSE_ID +
                " WHERE c." + COLUMN_SCHEDULE_ID + " = ?" +
                " AND r." + COLUMN_IS_ENABLED + " = 1" +
                " ORDER BY c." + COLUMN_DATE;

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(scheduleId)});

        if (cursor.moveToFirst()) {
            do {
                Course course = new Course();
                course.id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                course.name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                course.location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION));
                course.date = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                course.startSlot = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_START_SLOT));
                course.endSlot = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_END_SLOT));
                course.week = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WEEK));
                course.semester = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SEMESTER));
                course.scheduleId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SCHEDULE_ID));

                // 获取提醒设置
                int remindMinutesIndex = cursor.getColumnIndex(COLUMN_REMIND_MINUTES);
                if (remindMinutesIndex != -1 && !cursor.isNull(remindMinutesIndex)) {
                    course.remindMinutes = cursor.getInt(remindMinutesIndex);
                } else {
                    course.remindMinutes = 10; // 默认值
                }

                courses.add(course);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return courses;
    }
    // 保存节数设置
    public boolean savePeriodSetting(PeriodSetting setting) {
        SQLiteDatabase db = getDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_MORNING_PERIODS, setting.morningPeriods);
        values.put(COLUMN_AFTERNOON_PERIODS, setting.afternoonPeriods);
        values.put(COLUMN_EVENING_PERIODS, setting.eveningPeriods);
        values.put(COLUMN_TOTAL_PERIODS, setting.totalPeriods);
        values.put(COLUMN_SCHEDULE_ID, setting.scheduleId);

        // 检查是否已存在该课表的节数设置
        PeriodSetting existing = getPeriodSettingBySchedule(setting.scheduleId);
        if (existing != null && existing.periodId != 0) {
            // 更新现有设置
            int rows = db.update(TABLE_PERIOD_SETTINGS, values,
                    COLUMN_PERIOD_ID + " = ?",
                    new String[]{String.valueOf(existing.periodId)});
            return rows > 0;
        } else {
            // 插入新设置
            long result = db.insert(TABLE_PERIOD_SETTINGS, null, values);
            return result != -1;
        }
    }
}