package com.example.classcourse2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class PolicyManager {

    private static final String PREFS_NAME = "policy_prefs";
    private static final String KEY_POLICY_AGREED = "policy_agreed";
    private static final String KEY_POLICY_VERSION = "policy_version";
    private static final String CURRENT_POLICY_VERSION = "1.0";

    /**
     * 检查用户是否已经同意最新版本的政策
     */
    public static boolean hasAgreedToCurrentPolicy(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String agreedVersion = prefs.getString(KEY_POLICY_VERSION, "");
        boolean agreed = prefs.getBoolean(KEY_POLICY_AGREED, false);

        return agreed && CURRENT_POLICY_VERSION.equals(agreedVersion);
    }

    /**
     * 保存用户同意状态
     */
    public static void setPolicyAgreed(Context context, boolean agreed) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (agreed) {
            editor.putBoolean(KEY_POLICY_AGREED, true);
            editor.putString(KEY_POLICY_VERSION, CURRENT_POLICY_VERSION);
        } else {
            editor.putBoolean(KEY_POLICY_AGREED, false);
            editor.remove(KEY_POLICY_VERSION);
        }

        editor.apply();
    }

    /**
     * 检查是否需要显示政策更新提示
     */
    public static boolean shouldShowPolicyUpdate(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String agreedVersion = prefs.getString(KEY_POLICY_VERSION, "");

        // 如果用户同意过旧版本的政策，需要重新同意
        return !agreedVersion.isEmpty() && !CURRENT_POLICY_VERSION.equals(agreedVersion);
    }

    /**
     * 获取政策版本信息
     */
    public static String getPolicyVersion() {
        return CURRENT_POLICY_VERSION;
    }
}