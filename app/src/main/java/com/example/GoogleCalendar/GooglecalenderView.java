package com.example.GoogleCalendar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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


public class GooglecalenderView extends LinearLayout {
    private Context context;
    private ViewPager2 viewPager;
    private MonthChangeListner monthChangeListner;
    private int currentmonth = 0;
    private LocalDate mindate, maxdate;
    private HashMap<LocalDate, String[]> eventuser = new HashMap<>();

    public GooglecalenderView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.viewpagerlay, this);
        this.context = context;
    }

    public GooglecalenderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.viewpagerlay, this);
        this.context = context;
    }

    public GooglecalenderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.viewpagerlay, this);
        this.context = context;
    }

    public void setMonthChangeListner(MonthChangeListner monthChangeListner) {
        this.monthChangeListner = monthChangeListner;
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

    public void init(HashMap<LocalDate, String[]> eventhashmap, LocalDate mindate, LocalDate maxdate) {
        eventuser = eventhashmap;
        viewPager = findViewById(R.id.viewpager);

        this.mindate = mindate;
        this.maxdate = maxdate;
        DateTime mindateobj = mindate.toDateTimeAtStartOfDay();
        DateTime maxdateobj = maxdate.toDateTimeAtStartOfDay();
        int months = Months.monthsBetween(mindateobj, maxdateobj).getMonths();

        final ArrayList<MonthModel> arrayList = new ArrayList<>();
        HashMap<LocalDate, String[]> eventhash = new HashMap<>();

        for (int i = 0; i <= months; i++) {

            int firstday = mindateobj.dayOfMonth().withMinimumValue().dayOfWeek().get();
            if (firstday == 7) firstday = 0;
            MonthModel month = new MonthModel();
            month.setMonthnamestr(mindateobj.toString("MMMM"));
            month.setMonth(mindateobj.getMonthOfYear());
            month.setNoofday(mindateobj.dayOfMonth().getMaximumValue());
            month.setYear(mindateobj.getYear());
            month.setFirstday(firstday);
            int currentyear = new LocalDate().getYear();
            ArrayList<DayModel> dayModelArrayList = new ArrayList<>();
            DateTime startday = mindateobj.dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
            LocalDate minweek = startday.dayOfWeek().withMinimumValue().toLocalDate().minusDays(1);
            while (minweek.compareTo(startday.dayOfMonth().withMaximumValue().toLocalDate()) < 0) {
                if (minweek.getMonthOfYear() == minweek.plusDays(6).getMonthOfYear()) {
                    String lastpattern = minweek.getYear() == currentyear ? "d MMM" : "d MMM YYYY";

                    String s[] = {"tojigs" + minweek.toString("d").toUpperCase() + " - " + minweek.plusDays(6).toString(lastpattern).toUpperCase()};

                    if (!eventhash.containsKey(minweek)) eventhash.put(minweek, s);

                    minweek = minweek.plusWeeks(1);

                } else {
                    String lastpattern = minweek.getYear() == currentyear ? "d MMM" : "d MMM YYYY";
                    String s[] = {"tojigs" + minweek.toString("d MMM").toUpperCase() + " - " + minweek.plusDays(6).toString(lastpattern).toUpperCase()};
                    if (!eventhash.containsKey(minweek)) eventhash.put(minweek, s);

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
                        List<String> list = Arrays.asList(eventhash.get(startday.toLocalDate()));
                        list = new ArrayList<>(list);
                        for (String s : eventuser.get(startday.toLocalDate())) {
                            list.add(s);
                        }
                        String[] mStringArray = new String[list.size()];
                        String[] s = list.toArray(mStringArray);
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
                    String s[] = {"start"};
                    if (eventhash.containsKey(startday.toLocalDate())) {
                        List<String> list = Arrays.asList(eventhash.get(startday.toLocalDate()));
                        list = new ArrayList<>(list);
                        list.add(0, "start");
                        String[] mStringArray = new String[list.size()];
                        s = list.toArray(mStringArray);
                    }
                    eventhash.put(startday.toLocalDate(), s);
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
            eventhash.put(todaydate, new String[]{"todaydate"});
        } else {
            List<String> list = Arrays.asList(eventhash.get(todaydate));
            list = new ArrayList<>(list);

            list.add("todaydate");

            String[] mStringArray = new String[list.size()];
            eventhash.put(todaydate, list.toArray(mStringArray));
        }
        Map<LocalDate, String[]> treeMap = new TreeMap<LocalDate, String[]>(eventhash);
        HashMap<LocalDate, Integer> indextrack = new HashMap<>();
        int i = 0;
        ArrayList<EventModel> eventModelslist = new ArrayList<>();
        for (HashMap.Entry<LocalDate, String[]> localDateStringEntry : treeMap.entrySet()) {
            for (String s : localDateStringEntry.getValue()) {
                if (s == null) continue;
                int type = 0;
                if (s.startsWith("todaydate")) type = 2;
                else if (s.equals("start")) type = 1;
                else if (s.contains("jigs")) type = 3;
                if (type == 2 && eventModelslist.get(eventModelslist.size() - 1).getType() == 0 && eventModelslist.get(eventModelslist.size() - 1).getLocalDate().equals(localDateStringEntry.getKey())) {

                } else {
                    if (type == 0 && eventModelslist.size() > 0 && eventModelslist.get(eventModelslist.size() - 1).getType() == 0 && !eventModelslist.get(eventModelslist.size() - 1).getLocalDate().equals(localDateStringEntry.getKey())) {
                        eventModelslist.add(new EventModel("dup", localDateStringEntry.getKey(), 100));
                        i++;
                    } else if ((type == 3) && eventModelslist.size() > 0 && eventModelslist.get(eventModelslist.size() - 1).getType() == 0) {
                        eventModelslist.add(new EventModel("dup", eventModelslist.get(eventModelslist.size() - 1).getLocalDate(), 100));
                        i++;
                    } else if ((type == 1) && eventModelslist.size() > 0 && eventModelslist.get(eventModelslist.size() - 1).getType() == 0) {
                        eventModelslist.add(new EventModel("dup", eventModelslist.get(eventModelslist.size() - 1).getLocalDate(), 200));
                        i++;
                    } else if (type == 0 && eventModelslist.size() > 0 && (eventModelslist.get(eventModelslist.size() - 1).getType() == 1)) {
                        eventModelslist.add(new EventModel("dup", localDateStringEntry.getKey(), 200));
                        i++;
                    } else if (type == 2 && eventModelslist.size() > 0 && eventModelslist.get(eventModelslist.size() - 1).getType() == 0) {
                        eventModelslist.add(new EventModel("dup", eventModelslist.get(eventModelslist.size() - 1).getLocalDate(), 100));
                        i++;
                    }

                    eventModelslist.add(new EventModel(s, localDateStringEntry.getKey(), type));
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

                    if (monthChangeListner != null)
                        monthChangeListner.onmonthChange(myPagerAdapter.getMonthModels().get(position));
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
            if (monthViewHolder != null && monthViewHolder.gridview != null && monthViewHolder.gridview.getAdapter() != null) {
                monthViewHolder.gridview.getAdapter().notifyDataSetChanged();
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