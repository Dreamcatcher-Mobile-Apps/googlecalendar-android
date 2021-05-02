package com.example.GoogleCalendar.ui.dropDownCalendarView;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.GoogleCalendar.common.Converters;
import com.example.GoogleCalendar.models.AddEvent;
import com.example.GoogleCalendar.models.DayModel;
import com.example.GoogleCalendar.models.EventDataModel;
import com.example.GoogleCalendar.models.EventModel;
import com.example.GoogleCalendar.ui.MainActivity;
import com.example.GoogleCalendar.models.MessageEvent;
import com.example.GoogleCalendar.interfaces.MonthChangeListener;
import com.example.GoogleCalendar.models.MonthModel;
import com.example.GoogleCalendar.R;
import com.example.GoogleCalendar.ui.dropDownCalendarView.adapters.MonthPagerAdapter;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Months;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class GoogleCalenderView extends LinearLayout {
    private Context context;
    private ViewPager2 viewPager;
    private MonthChangeListener monthChangeListener;
    private int currentmonth = 0;
    private LocalDate mindate;
    private HashMap<LocalDate, EventDataModel[]> eventuser = new HashMap<>();

    public GoogleCalenderView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.viewpagerlay, this);
        this.context = context;
    }

    public GoogleCalenderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.viewpagerlay, this);
        this.context = context;
    }

    public GoogleCalenderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.viewpagerlay, this);
        this.context = context;
    }

    public void setMonthChangeListener(MonthChangeListener monthChangeListener) {
        this.monthChangeListener = monthChangeListener;
    }

    public int calculateCurrentMonth(LocalDate currentmonthda) {
        if (currentmonthda == null) return 0;

        LocalDate mindateobj = mindate.toDateTimeAtStartOfDay().dayOfMonth().withMinimumValue().toLocalDate();
        LocalDate current = currentmonthda.dayOfMonth().withMaximumValue();
        int months = Months.monthsBetween(mindateobj, current).getMonths();
        return months;
    }

    public void setCurrentmonth(LocalDate currentmonthda) {
        currentmonth = calculateCurrentMonth(currentmonthda);
        if (viewPager.getCurrentItem() != currentmonth) {
            viewPager.setCurrentItem(currentmonth, false);
        }
    }

    public void init(HashMap<LocalDate, EventDataModel[]> eventhashmap, LocalDate mindate, LocalDate maxdate) {
        eventuser = eventhashmap;
        viewPager = findViewById(R.id.viewpager);
        this.mindate = mindate;
        DateTime mindateobj = mindate.toDateTimeAtStartOfDay();
        DateTime maxdateobj = maxdate.toDateTimeAtStartOfDay();
        int months = Months.monthsBetween(mindateobj, maxdateobj).getMonths();

        final ArrayList<MonthModel> arrayList = new ArrayList<>();
        HashMap<LocalDate, EventDataModel[]> eventhash = new HashMap<>();

        for (int i = 0; i <= months; i++) {

            int firstday = mindateobj.dayOfMonth().withMinimumValue().dayOfWeek().get();
            if (firstday == 7) firstday = 0;
            MonthModel month = new MonthModel();
            month.setMonthnamestr(mindateobj.toString("MMMM"));
            month.setMonth(mindateobj.getMonthOfYear());
            month.setNoofday(mindateobj.dayOfMonth().getMaximumValue());
            month.setYear(mindateobj.getYear());
            month.setFirstday(firstday);
            int currentYear = new LocalDate().getYear();
            ArrayList<DayModel> dayModelArrayList = new ArrayList<>();
            DateTime startday = mindateobj.dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            LocalDate minweek = startday.dayOfWeek().withMinimumValue().toLocalDate().minusDays(1);

            while (minweek.compareTo(startday.dayOfMonth().withMaximumValue().toLocalDate()) < 0) {
                if (minweek.getMonthOfYear() == minweek.plusDays(6).getMonthOfYear()) {
                    String lastpattern = minweek.getYear() == currentYear ? "d MMM" : "d MMM YYYY";
                    String s[] = {"tojigs" + minweek.toString("d").toUpperCase() + " - " + minweek.plusDays(6).toString(lastpattern).toUpperCase()};
                    if (!eventhash.containsKey(minweek)){
                        eventhash.put(minweek, Converters.Companion.convertStringArrayToEventDataModelArray(s));
                    }
                    minweek = minweek.plusWeeks(1);
                } else {
                    String lastpattern = minweek.getYear() == currentYear ? "d MMM" : "d MMM YYYY";
                    String s[] = {"tojigs" + minweek.toString("d MMM").toUpperCase() + " - " + minweek.plusDays(6).toString(lastpattern).toUpperCase()};
                    if (!eventhash.containsKey(minweek)) {
                        eventhash.put(minweek, Converters.Companion.convertStringArrayToEventDataModelArray(s));
                    }
                    minweek = minweek.plusWeeks(1);
                }
            }

            for (int j = 1; j <= month.getNoofday(); j++) {

                DayModel dayModel = new DayModel();
                dayModel.setDay(startday.getDayOfMonth());
                dayModel.setMonth(startday.getMonthOfYear());
                dayModel.setYear(startday.getYear());
                if (eventuser.containsKey(startday.toLocalDate())) {
                    if (eventhash.containsKey(startday.toLocalDate())) {
                        List<EventDataModel> list = Arrays.asList(eventhash.get(startday.toLocalDate()));
                        list = new ArrayList<>(list);
                        for (EventDataModel event : eventuser.get(startday.toLocalDate())) {
                            list.add(event);
                        }
                        EventDataModel[] mEventsArray = new EventDataModel[list.size()];
                        EventDataModel[] s = list.toArray(mEventsArray);
                        eventhash.put(startday.toLocalDate(), s);
                    } else {
                        eventhash.put(startday.toLocalDate(), eventuser.get(startday.toLocalDate()));
                    }
                    dayModel.setEventlist(true);

                }

                if (startday.toLocalDate().equals(new LocalDate())) {
                    dayModel.setToday(true);
                    currentmonth = i;
                } else {
                    dayModel.setToday(false);
                }
                dayModelArrayList.add(dayModel);

                if (j == 1) {
                    EventDataModel events[] = {new EventDataModel("start", null, null)};
                    if (eventhash.containsKey(startday.toLocalDate())) {
                        List<EventDataModel> list = Arrays.asList(eventhash.get(startday.toLocalDate()));
                        list = new ArrayList<>(list);
                        list.add(0, new EventDataModel("start", null, null));
                        EventDataModel[] mEventsArray = new EventDataModel[list.size()];
                        events = list.toArray(mEventsArray);
                    }
                    eventhash.put(startday.toLocalDate(), events);
                }
                startday = startday.plusDays(1);
            }
            month.setDayModelArrayList(dayModelArrayList);
            arrayList.add(month);
            mindateobj = mindateobj.plusMonths(1);
        }

        if (viewPager.getAdapter() == null) {
            setupViewPagerAdapter(arrayList);
        }

        LocalDate todaydate = LocalDate.now();
        if (!eventhash.containsKey(todaydate)) {
            eventhash.put(todaydate, new EventDataModel[]{new EventDataModel("todaydate", null, null)});
        } else {
            List<EventDataModel> list = Arrays.asList(eventhash.get(todaydate));
            list = new ArrayList<>(list);

            list.add(new EventDataModel("todaydate", null, null));

            EventDataModel[] mEventsArray = new EventDataModel[list.size()];
            eventhash.put(todaydate, list.toArray(mEventsArray));
        }
        Map<LocalDate, EventDataModel[]> treeMap = new TreeMap<>(eventhash);
        HashMap<LocalDate, Integer> indextrack = new HashMap<>();
        int i = 0;
        ArrayList<EventModel> eventModelslist = new ArrayList<>();
        for (HashMap.Entry<LocalDate, EventDataModel[]> localDateStringEntry : treeMap.entrySet()) {
            for (EventDataModel event : localDateStringEntry.getValue()) {
                if (event == null) continue;
                int type = 0;
                if (event.getEventName().startsWith("todaydate")) type = 2;
                else if (event.getEventName().equals("start")) type = 1;
                else if (event.getEventName().contains("jigs")) type = 3;
                if (type == 2 && eventModelslist.get(eventModelslist.size() - 1).getType() == 0 && eventModelslist.get(eventModelslist.size() - 1).getLocalDate().equals(localDateStringEntry.getKey())) {

                } else {
                    if (type == 0 && eventModelslist.size() > 0 && eventModelslist.get(eventModelslist.size() - 1).getType() == 0 && !eventModelslist.get(eventModelslist.size() - 1).getLocalDate().equals(localDateStringEntry.getKey())) {
                        eventModelslist.add(new EventModel(new EventDataModel("dup", null, null), localDateStringEntry.getKey(), 100));
                        i++;
                    } else if ((type == 3) && eventModelslist.size() > 0 && eventModelslist.get(eventModelslist.size() - 1).getType() == 0) {
                        eventModelslist.add(new EventModel(new EventDataModel("dup", null, null), eventModelslist.get(eventModelslist.size() - 1).getLocalDate(), 100));
                        i++;
                    } else if ((type == 1) && eventModelslist.size() > 0 && eventModelslist.get(eventModelslist.size() - 1).getType() == 0) {
                        eventModelslist.add(new EventModel(new EventDataModel("dup", null, null), eventModelslist.get(eventModelslist.size() - 1).getLocalDate(), 200));
                        i++;
                    } else if (type == 0 && eventModelslist.size() > 0 && (eventModelslist.get(eventModelslist.size() - 1).getType() == 1)) {
                        eventModelslist.add(new EventModel(new EventDataModel("dup", null, null), localDateStringEntry.getKey(), 200));
                        i++;
                    } else if (type == 2 && eventModelslist.size() > 0 && eventModelslist.get(eventModelslist.size() - 1).getType() == 0) {
                        eventModelslist.add(new EventModel(new EventDataModel("dup", null, null), eventModelslist.get(eventModelslist.size() - 1).getLocalDate(), 100));
                        i++;
                    }

                    eventModelslist.add(new EventModel(event, localDateStringEntry.getKey(), type));
                    indextrack.put(localDateStringEntry.getKey(), i);
                    i++;
                }
            }
        }
        EventBus.getDefault().post(new AddEvent(eventModelslist, indextrack, arrayList));
    }

    private void setupViewPagerAdapter(ArrayList<MonthModel> arrayList) {
        final MonthPagerAdapter myPagerAdapter = new MonthPagerAdapter(context, arrayList);

        viewPager.setAdapter(myPagerAdapter);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Log.e("onPageSelected", position + "");
                MainActivity mainActivity = (MainActivity) context;
                currentmonth = position;

                if (!mainActivity.isAppBarClosed()) {
                    Log.e("onPageSelected", "Googlecalendaraview");
                    adjustheight();
                    EventBus.getDefault().post(new MessageEvent(new LocalDate(myPagerAdapter.getMonthModels().get(position).getYear(), myPagerAdapter.getMonthModels().get(position).getMonth(), 1)));

                    updategrid();

                    if (monthChangeListener != null)
                        monthChangeListener.onMonthChange(myPagerAdapter.getMonthModels().get(position));
                }

            }
        });
    }

    public void updategrid() {
        final MonthPagerAdapter myPagerAdapter = (MonthPagerAdapter) viewPager.getAdapter();
        if (myPagerAdapter != null) {
            final int position = viewPager.getCurrentItem();
            RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
            MonthPagerAdapter.MonthViewHolder monthViewHolder = (MonthPagerAdapter.MonthViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
            if (monthViewHolder != null && monthViewHolder.getGridview() != null && monthViewHolder.getGridview().getAdapter() != null) {
                monthViewHolder.getGridview().getAdapter().notifyDataSetChanged();
            }
        }
    }

    public void adjustheight() {
        final MonthPagerAdapter myPagerAdapter = (MonthPagerAdapter) viewPager.getAdapter();
        if (myPagerAdapter != null) {
            final int position = viewPager.getCurrentItem();
            int size = myPagerAdapter.getMonthModels().get(position).getDayModelArrayList().size() + myPagerAdapter.getMonthModels().get(position).getFirstday();
            int numbercolumn = size % 7 == 0 ? size / 7 : (size / 7) + 1;
            ViewGroup.LayoutParams params = getLayoutParams();
            int setheight = 65 + (context.getResources().getDimensionPixelSize(R.dimen.itemheight) * numbercolumn) + context.getResources().getDimensionPixelSize(R.dimen.tendp) + getStatusBarHeight();
            if (params.height == setheight) return;
            params.height = setheight;
            setLayoutParams(params);
            RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
            Log.e("adjust0", recyclerView.getHeight() + "");
            Log.e("adjust1", viewPager.getHeight() + "");
            Log.e("adjust2", params.height + "");
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}