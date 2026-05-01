package com.example.classcourse2;

import java.util.Date;

public class SystemMessage {
    private int id;
    private String title;
    private String subtitle;
    private String description;
    private String actionText;
    private String tag;
    private Date date;
    private int bannerResId;
    private boolean isRead;
    private String actionUrl;
    private int colorResId;

    public SystemMessage() {
    }

    public SystemMessage(int id, String title, String subtitle, String description,
                         String actionText, String tag, Date date, int bannerResId) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
        this.actionText = actionText;
        this.tag = tag;
        this.date = date;
        this.bannerResId = bannerResId;
        this.isRead = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActionText() {
        return actionText;
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getBannerResId() {
        return bannerResId;
    }

    public void setBannerResId(int bannerResId) {
        this.bannerResId = bannerResId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public int getColorResId() {
        return colorResId;
    }

    public void setColorResId(int colorResId) {
        this.colorResId = colorResId;
    }

    // 获取格式化日期
    public String getFormattedDate() {
        if (date == null) return "";

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        return sdf.format(date);
    }

    // 获取相对时间（如：刚刚、1小时前等）
    public String getRelativeTime() {
        if (date == null) return "";

        long diff = System.currentTimeMillis() - date.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "天前";
        } else if (hours > 0) {
            return hours + "小时前";
        } else if (minutes > 0) {
            return minutes + "分钟前";
        } else {
            return "刚刚";
        }
    }
}