package com.example.alarmappbyshashisingh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

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

        // Optional: show mp3 file name if available
        if (reminder.mp3File != null && !reminder.mp3File.isEmpty()) {
            holder.mp3Text.setVisibility(View.VISIBLE);
            holder.mp3Text.setText("Tone: " + reminder.mp3File);
        } else {
            holder.mp3Text.setVisibility(View.GONE);
        }
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

        ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            detailText = itemView.findViewById(R.id.detailText);
            timeText = itemView.findViewById(R.id.timeText);

            // Add a TextView in XML for mp3 display (optional)
            mp3Text = itemView.findViewById(R.id.mp3Text);
        }
    }
}
