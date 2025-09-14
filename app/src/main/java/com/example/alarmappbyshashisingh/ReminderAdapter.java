package com.example.alarmappbyshashisingh;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.Executors;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    private List<Reminder> reminderList;

    public ReminderAdapter(List<Reminder> reminderList) {
        this.reminderList = reminderList;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reminder_item, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminderList.get(position);

        holder.titleText.setText(reminder.title);
        holder.detailText.setText(reminder.detail);
        holder.timeText.setText(reminder.time);

        if (reminder.mp3File != null && !reminder.mp3File.isEmpty()) {
            holder.mp3Text.setVisibility(View.VISIBLE);
            holder.mp3Text.setText("Tone: " + reminder.mp3File);
        } else {
            holder.mp3Text.setVisibility(View.GONE);
        }

        // Enable/Disable switch
        holder.enableSwitch.setOnCheckedChangeListener(null);
        holder.enableSwitch.setChecked(reminder.enabled);
        holder.enableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reminder.enabled = isChecked;
            Executors.newSingleThreadExecutor().execute(() -> {
                ReminderDatabase.getInstance(holder.itemView.getContext())
                        .reminderDao()
                        .updateEnabled(reminder.id, isChecked);
            });

            // Cancel alarm if disabled
            if (!isChecked) {
                AlarmManager alarmManager = (AlarmManager) holder.itemView.getContext()
                        .getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(holder.itemView.getContext(), AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        holder.itemView.getContext(),
                        reminder.id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                if (alarmManager != null) alarmManager.cancel(pendingIntent);
            }
        });

        // Delete button
        holder.deleteBtn.setOnClickListener(v -> {
            // Cancel alarm
            AlarmManager alarmManager = (AlarmManager) holder.itemView.getContext()
                    .getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(holder.itemView.getContext(), AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    holder.itemView.getContext(),
                    reminder.id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            if (alarmManager != null) alarmManager.cancel(pendingIntent);

            // Delete from DB
            Executors.newSingleThreadExecutor().execute(() -> {
                ReminderDatabase.getInstance(holder.itemView.getContext())
                        .reminderDao()
                        .deleteById(reminder.id);
            });

            // Remove from list and refresh UI
            reminderList.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    public void removeItem(int position) {
        reminderList.remove(position);
        notifyItemRemoved(position);
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, detailText, timeText, mp3Text;
        SwitchCompat enableSwitch;
        ImageButton deleteBtn;

        ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            detailText = itemView.findViewById(R.id.detailText);
            timeText = itemView.findViewById(R.id.timeText);
            mp3Text = itemView.findViewById(R.id.mp3Text);
            enableSwitch = itemView.findViewById(R.id.enableSwitch);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }

}
