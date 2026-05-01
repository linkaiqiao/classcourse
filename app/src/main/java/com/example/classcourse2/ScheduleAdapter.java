package com.example.classcourse2;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

class ScheduleAdapter extends ArrayAdapter<Schedule> {
    private Context context;
    private List<Schedule> schedules;
    private CourseDbHelper dbHelper;
    private OnScheduleChangeListener listener;

    public interface OnScheduleChangeListener {
        void onScheduleChanged();
    }

    public ScheduleAdapter(Context context, List<Schedule> schedules, CourseDbHelper dbHelper, OnScheduleChangeListener listener) {
        super(context, R.layout.item_schedule, schedules);
        this.context = context;
        this.schedules = schedules;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_schedule, parent, false);
            holder = new ViewHolder();
            holder.tvScheduleName = convertView.findViewById(R.id.tvScheduleName);
            holder.tvScheduleInfo = convertView.findViewById(R.id.tvScheduleInfo);
            holder.ivActive = convertView.findViewById(R.id.ivActive);
            holder.btnDelete = convertView.findViewById(R.id.btnDeleteSchedule);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Schedule schedule = schedules.get(position);

        holder.tvScheduleName.setText(schedule.name);
        holder.tvScheduleInfo.setText(schedule.year + " " + schedule.semester);
        holder.ivActive.setVisibility(schedule.isActive ? View.VISIBLE : View.GONE);

        // 设置点击切换课表
        convertView.setOnClickListener(v -> {
            switchToSchedule(schedule);
        });

        // 设置删除按钮点击事件
        holder.btnDelete.setOnClickListener(v -> {
            showDeleteConfirmDialog(schedule, position);
        });

        // 如果是最后一个课表，禁用删除按钮
        if (schedules.size() <= 1) {
            holder.btnDelete.setEnabled(false);
            holder.btnDelete.setAlpha(0.3f);
        } else {
            holder.btnDelete.setEnabled(true);
            holder.btnDelete.setAlpha(1.0f);
        }

        return convertView;
    }

    private void switchToSchedule(Schedule schedule) {
        dbHelper.setActiveSchedule(schedule.id);
        if (listener != null) {
            listener.onScheduleChanged();
        }
    }

    private void showDeleteConfirmDialog(Schedule schedule, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("确认删除");
        builder.setMessage("确定要删除课表 '" + schedule.name + "' 吗？\n此操作将删除该课表下的所有课程，且无法恢复！");

        builder.setPositiveButton("删除", (dialog, which) -> {
            deleteSchedule(schedule, position);
        });

        builder.setNegativeButton("取消", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteSchedule(Schedule schedule, int position) {
        boolean success = dbHelper.deleteSchedule(schedule.id);
        if (success) {
            // 从列表中移除
            schedules.remove(position);
            notifyDataSetChanged();

            // 如果删除的是当前活跃课表，切换到第一个课表
            if (schedule.isActive && !schedules.isEmpty()) {
                Schedule newActive = schedules.get(0);
                dbHelper.setActiveSchedule(newActive.id);
            }

            Toast.makeText(context, "课表删除成功", Toast.LENGTH_SHORT).show();

            if (listener != null) {
                listener.onScheduleChanged();
            }
        } else {
            Toast.makeText(context, "课表删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    private static class ViewHolder {
        TextView tvScheduleName;
        TextView tvScheduleInfo;
        ImageView ivActive;
        ImageButton btnDelete;
    }
}