package com.example.classcourse2;

public class TimeSetting {
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

    public TimeSetting(String morningStartTime, int morningDuration, int morningBreak,
                       String afternoonStartTime, int afternoonDuration, int afternoonBreak,
                       String eveningStartTime, int eveningDuration) {
        this.morningStartTime = morningStartTime;
        this.morningDuration = morningDuration;
        this.morningBreak = morningBreak;
        this.afternoonStartTime = afternoonStartTime;
        this.afternoonDuration = afternoonDuration;
        this.afternoonBreak = afternoonBreak;
        this.eveningStartTime = eveningStartTime;
        this.eveningDuration = eveningDuration;
        this.isDefault = false;
    }

    // Getter 和 Setter 方法
    public long getSettingId() {
        return settingId;
    }

    public void setSettingId(long settingId) {
        this.settingId = settingId;
    }

    public String getMorningStartTime() {
        return morningStartTime;
    }

    public void setMorningStartTime(String morningStartTime) {
        this.morningStartTime = morningStartTime;
    }

    public int getMorningDuration() {
        return morningDuration;
    }

    public void setMorningDuration(int morningDuration) {
        this.morningDuration = morningDuration;
    }

    public int getMorningBreak() {
        return morningBreak;
    }

    public void setMorningBreak(int morningBreak) {
        this.morningBreak = morningBreak;
    }

    public String getAfternoonStartTime() {
        return afternoonStartTime;
    }

    public void setAfternoonStartTime(String afternoonStartTime) {
        this.afternoonStartTime = afternoonStartTime;
    }

    public int getAfternoonDuration() {
        return afternoonDuration;
    }

    public void setAfternoonDuration(int afternoonDuration) {
        this.afternoonDuration = afternoonDuration;
    }

    public int getAfternoonBreak() {
        return afternoonBreak;
    }

    public void setAfternoonBreak(int afternoonBreak) {
        this.afternoonBreak = afternoonBreak;
    }

    public String getEveningStartTime() {
        return eveningStartTime;
    }

    public void setEveningStartTime(String eveningStartTime) {
        this.eveningStartTime = eveningStartTime;
    }

    public int getEveningDuration() {
        return eveningDuration;
    }

    public void setEveningDuration(int eveningDuration) {
        this.eveningDuration = eveningDuration;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(long scheduleId) {
        this.scheduleId = scheduleId;
    }
}