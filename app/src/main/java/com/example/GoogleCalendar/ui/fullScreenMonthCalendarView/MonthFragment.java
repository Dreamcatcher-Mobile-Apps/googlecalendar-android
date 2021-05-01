package com.example.GoogleCalendar.ui.fullScreenMonthCalendarView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.GoogleCalendar.R;
import com.example.GoogleCalendar.models.DayModel;
import com.example.GoogleCalendar.ui.fullScreenMonthCalendarView.adapters.MyAdapter;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MonthFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonthFragment extends Fragment {
    private ArrayList<DayModel> dayModels;
    private int singleitemheight;
    private int index;

    public MonthFragment() {
        // Required empty public constructor
    }

    public static MonthFragment newInstance(int month, int year, int page, ArrayList<DayModel> dayModels, HashMap<LocalDate, String[]> alleventlist, int singleitemheight) {
        MonthFragment fragmentFirst = new MonthFragment();
        Bundle args = new Bundle();
        args.putInt("singleitemheight", singleitemheight);
        args.putInt("firstday", page);
        args.putInt("month", month);
        args.putInt("year", year);
        LocalDate prevmonth = new LocalDate(year, month, 1);
        LocalDate todaydate = new LocalDate();
        ArrayList<DayModel> adapterdata = new ArrayList<>(43);
        for (int i = 0; i < 42; i++) {
            if (i < page) {
                LocalDate localDate = prevmonth.minusDays(page - i);

                DayModel dayModel = new DayModel();
                if (localDate.isEqual(todaydate)) {
                    dayModel.setToday(true);
                }
                dayModel.setDay(localDate.getDayOfMonth());
                dayModel.setMonth(localDate.getMonthOfYear());
                dayModel.setYear(localDate.getYear());
                if (alleventlist.containsKey(localDate)) {
                    dayModel.setEvents(alleventlist.get(localDate));
                }

                dayModel.setIsenable(false);
                adapterdata.add(dayModel);

            } else if (i >= dayModels.size() + page) {

                LocalDate localDate = prevmonth.plusDays(i - (page));
                DayModel dayModel = new DayModel();
                if (localDate.isEqual(todaydate)) {
                    dayModel.setToday(true);
                }
                dayModel.setDay(localDate.getDayOfMonth());
                dayModel.setMonth(localDate.getMonthOfYear());
                dayModel.setYear(localDate.getYear());
                dayModel.setIsenable(false);
                if (alleventlist.containsKey(localDate)) {
                    dayModel.setEvents(alleventlist.get(localDate));
                }
                adapterdata.add(dayModel);
            } else {
                DayModel dayModel = dayModels.get(i - page);
                dayModel.setIsenable(true);
                if (dayModel.isToday()) {
                    Log.e("index", i % 7 + "");
                    args.putInt("index", i % 7);
                }
                LocalDate mydate = new LocalDate(year, month, dayModel.getDay());
                if (alleventlist.containsKey(mydate)) {
                    dayModel.setEvents(alleventlist.get(mydate));
                }
                adapterdata.add(dayModels.get(i - page));

            }
        }
        fragmentFirst.dayModels = adapterdata;
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        index = getArguments().getInt("index", -1);
        singleitemheight = getArguments().getInt("singleitemheight");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_month, container, false);
        RecyclerView gridView = view.findViewById(R.id.recyclerview);
        ConstraintLayout constraintLayout = view.findViewById(R.id.dd);
        for (int i = 0; i < constraintLayout.getChildCount(); i++) {
            TextView textView = (TextView) constraintLayout.getChildAt(i);
            if (i == index) {
                textView.setTextColor(getResources().getColor(R.color.selectday));
            } else {
                textView.setTextColor(getResources().getColor(R.color.unselectday));
            }
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 7) {};
        gridView.setLayoutManager(gridLayoutManager);
        MiddleDividerItemDecoration vertecoration = new MiddleDividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        MiddleDividerItemDecoration hortdecoration = new MiddleDividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL);
        gridView.addItemDecoration(vertecoration);
        gridView.addItemDecoration(hortdecoration);
        gridView.setAdapter(new MyAdapter(getActivity(), dayModels, singleitemheight));
        return view;
    }
}