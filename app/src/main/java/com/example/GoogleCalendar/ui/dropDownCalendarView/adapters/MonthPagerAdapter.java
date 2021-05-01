package com.example.GoogleCalendar.ui.dropDownCalendarView.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.GoogleCalendar.models.MonthModel;
import com.example.GoogleCalendar.R;

import java.util.ArrayList;

public class MonthPagerAdapter extends RecyclerView.Adapter<MonthPagerAdapter.MonthViewHolder> {
    private ArrayList<MonthModel> monthModels;
    private LayoutInflater mInflater;
    private Context context;

    public MonthPagerAdapter(Context context, ArrayList<MonthModel> data) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.monthModels = data;
    }

    @NonNull
    @Override
    public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.fraglay, parent, false);
        return new MonthViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
        MonthModel monthtemp = monthModels.get(position);
        DayAdapter dayadapter = new DayAdapter(context, monthtemp.getDayModelArrayList(), monthtemp.getFirstday(), monthtemp.getMonth(), monthtemp.getYear());
        holder.gridview.setAdapter(dayadapter);
        dayadapter.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return monthModels.size();
    }

    public ArrayList<MonthModel> getMonthModels() {
        return monthModels;
    }

    public class MonthViewHolder extends RecyclerView.ViewHolder {
        RecyclerView gridview;

        public RecyclerView getGridview() {
            return gridview;
        }

        MonthViewHolder(View itemView) {
            super(itemView);
            gridview = itemView.findViewById(R.id.recyclerview);
        }
    }
}