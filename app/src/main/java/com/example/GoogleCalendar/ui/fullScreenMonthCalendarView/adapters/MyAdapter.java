package com.example.GoogleCalendar.ui.fullScreenMonthCalendarView.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.GoogleCalendar.R;
import com.example.GoogleCalendar.models.DayModel;
import com.example.GoogleCalendar.models.EventDataModel;
import com.example.GoogleCalendar.ui.MainActivity;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MonthViewHolder> {

    private final Activity activity;
    private final ArrayList<DayModel> dayModels;
    private final int singleItemHeight;

    public MyAdapter(Activity activity, ArrayList<DayModel> dayModels, int singleItemHeight) {
        this.activity = activity;
        this.dayModels = dayModels;
        this.singleItemHeight = singleItemHeight;
    }

    @Override
    public MonthViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        View view;
        if (viewType == 0) {
            view = activity.getLayoutInflater().inflate(R.layout.monthgriditemlspace, parent, false);
        } else {
            view = activity.getLayoutInflater().inflate(R.layout.monthgriditem, parent, false);
        }

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = singleItemHeight;
        view.setLayoutParams(layoutParams);
        return new MonthViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MonthViewHolder holder, int position) {
        holder.textView.setText(dayModels.get(position).getDay() + "");
        if (dayModels.get(position).isToday()) {
            holder.textView.setBackgroundResource(R.drawable.smallcircle);
            holder.textView.setTextColor(Color.WHITE);
        } else if (dayModels.get(position).isenable()) {
            holder.textView.setTextColor(Color.BLACK);
            holder.textView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            holder.textView.setBackgroundColor(Color.TRANSPARENT);
            holder.textView.setTextColor(activity.getResources().getColor(R.color.lightblack));
        }

        EventDataModel events[] = dayModels.get(position).getEvents();
        if (events != null) {
            if (events.length == 1) {
                holder.event1.setVisibility(View.VISIBLE);
                holder.event2.setVisibility(View.GONE);
                holder.event3.setVisibility(View.GONE);
                holder.event2.setText("");
                holder.event3.setText("");
            } else if (events.length == 2) {
                holder.event1.setVisibility(View.VISIBLE);
                holder.event2.setVisibility(View.VISIBLE);
                holder.event3.setVisibility(View.GONE);
                holder.event3.setText("");
            } else {
                holder.event1.setVisibility(View.VISIBLE);
                holder.event2.setVisibility(View.VISIBLE);
                holder.event3.setVisibility(View.VISIBLE);
            }
            for (int i = 0; i < dayModels.get(position).getEvents().length; i++) {
                if (i == 0) holder.event1.setText(events[0].getEventName());
                else if (i == 1) holder.event2.setText(events[1].getEventName());
                else holder.event3.setText(events[2].getEventName());
            }
        } else {
            holder.event1.setVisibility(View.GONE);
            holder.event2.setVisibility(View.GONE);
            holder.event3.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return 42;
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 7 == 0) return 0;
        else return 1;
    }

    class MonthViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private TextView event1;
        private TextView event2;
        private TextView event3;

        public MonthViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView8);
            event1 = itemView.findViewById(R.id.event1);
            event2 = itemView.findViewById(R.id.event2);
            event3 = itemView.findViewById(R.id.event3);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity mainActivity = (MainActivity) activity;
                    if (mainActivity != null) {
                        DayModel dayModel = dayModels.get(getAdapterPosition());
                        mainActivity.selectdateFromMonthPager(dayModel.getYear(), dayModel.getMonth(), dayModel.getDay());
                    }
                }
            });
        }
    }
}