package com.example.GoogleCalendar.ui.dropDownCalendarView.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.GoogleCalendar.models.DayModel;
import com.example.GoogleCalendar.ui.MainActivity;
import com.example.GoogleCalendar.models.MessageEvent;
import com.example.GoogleCalendar.R;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.LocalDate;

import java.util.ArrayList;

class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {
    private ArrayList<DayModel> dayModels;
    private LayoutInflater mInflater;
    private int firstday;
    private int month, year;

    public DayAdapter(Context context, ArrayList<DayModel> dayModels, int firstday, int month, int year) {
        this.mInflater = LayoutInflater.from(context);
        this.dayModels = dayModels;
        this.firstday = firstday;
        this.month = month;
        this.year = year;
    }

    @Override
    public DayAdapter.DayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.gridlay, parent, false);
        return new DayAdapter.DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DayAdapter.DayViewHolder holder, int position) {
        if (position >= firstday) {
            position = position - firstday;
            DayModel dayModel = dayModels.get(position);
            boolean selected = dayModel.getDay() == MainActivity.lastdate.getDayOfMonth() && dayModel.getMonth() == MainActivity.lastdate.getMonthOfYear() && dayModel.getYear() == MainActivity.lastdate.getYear() ? true : false;

            if (dayModel.isToday()) {
                holder.textView.setBackgroundResource(R.drawable.circle);
                holder.textView.setTextColor(Color.WHITE);

            } else if (selected) {
                holder.textView.setBackgroundResource(R.drawable.selectedback);
                holder.textView.setTextColor(Color.rgb(91, 128, 231));

            } else {
                holder.textView.setBackgroundColor(Color.TRANSPARENT);
                holder.textView.setTextColor(Color.rgb(80, 80, 80));
            }
            holder.textView.setText(dayModels.get(position).getDay() + "");

            if (dayModel.getEventlist() && !selected) {
                holder.eventview.setVisibility(View.VISIBLE);
            } else {
                holder.eventview.setVisibility(View.GONE);
            }
        } else {
            holder.textView.setBackgroundColor(Color.TRANSPARENT);
            holder.textView.setText("");
            holder.eventview.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return dayModels.size() + firstday;
    }

    class DayViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private View eventview;

        public DayViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView8);
            eventview = itemView.findViewById(R.id.eventview);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getAdapterPosition() >= firstday) {
                        for (DayModel dayModel : dayModels) {
                            dayModel.setSelected(false);
                        }
                        MainActivity.lastdate = new LocalDate(year, month, dayModels.get(getAdapterPosition() - firstday).getDay());
                        EventBus.getDefault().post(new MessageEvent(new LocalDate(year, month, dayModels.get(getAdapterPosition() - firstday).getDay())));
                        notifyDataSetChanged();
                    }

                }
            });
        }
    }
}