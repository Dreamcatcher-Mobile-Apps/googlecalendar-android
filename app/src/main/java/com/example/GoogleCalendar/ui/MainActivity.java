package com.example.GoogleCalendar.ui;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.GoogleCalendar.R;
import com.example.GoogleCalendar.common.MyAppBarBehavior;
import com.example.GoogleCalendar.api.ApiAsyncTask;
import com.example.GoogleCalendar.interfaces.MonthChangeListener;
import com.example.GoogleCalendar.models.AddEvent;
import com.example.GoogleCalendar.models.EventDataModel;
import com.example.GoogleCalendar.models.MessageEvent;
import com.example.GoogleCalendar.models.MonthChange;
import com.example.GoogleCalendar.models.MonthModel;
import com.example.GoogleCalendar.ui.daysVerticalListView.DateAdapter;
import com.example.GoogleCalendar.ui.daysVerticalListView.MyRecyclerView;
import com.example.GoogleCalendar.ui.dropDownCalendarView.GoogleCalenderView;
import com.example.GoogleCalendar.ui.fullScreenMonthCalendarView.MonthFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.material.appbar.AppBarLayout;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements MyRecyclerView.AppBarTracking {

    public static LocalDate lastdate = LocalDate.now();
    public static int topspace = 0;
    public HashMap<LocalDate, Integer> indextrack;
    public int expandedfirst;
    public HashMap<LocalDate, Integer> dupindextrack;

    private long lasttime;
    private MyRecyclerView mNestedView;
    private ViewPager monthviewpager;
    private HashMap<LocalDate, EventDataModel[]> alleventlist;
    private int mAppBarOffset = 0;
    private boolean mAppBarIdle = true;
    private int mAppBarMaxOffset = 0;
    private AppBarLayout mAppBar;
    private boolean mIsExpanded = false;
    private View redlay;
    private ImageView mArrowImageView;
    private TextView monthname;
    private Toolbar toolbar;
    private boolean isappbarclosed = true;
    private int month;
    private GoogleCalenderView calendarView;

    public com.google.api.services.calendar.Calendar mService;

    GoogleAccountCredential credential;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    public static final int YEARS_BACK = 5;
    public static final int YEARS_FORWARD = 5;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_favorite) {
            final LocalDate localDate = LocalDate.now();
            if (monthviewpager.getVisibility() == View.VISIBLE && monthviewpager.getAdapter() != null) {
                monthviewpager.setCurrentItem(calendarView.calculateCurrentMonth(localDate), false);
            }
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mNestedView.getLayoutManager();
            mNestedView.stopScroll();
            if (indextrack.containsKey(new LocalDate(localDate.getYear(), localDate.getMonthOfYear(), localDate.getDayOfMonth()))) {
                final Integer val = indextrack.get(new LocalDate(localDate.getYear(), localDate.getMonthOfYear(), localDate.getDayOfMonth()));
                if (isAppBarExpanded()) {
                    calendarView.setCurrentmonth(new LocalDate());
                }
                expandedfirst = val;
                topspace = 20;
                linearLayoutManager.scrollToPositionWithOffset(val, 20);
                EventBus.getDefault().post(new MonthChange(localDate, 0));
                month = localDate.getDayOfMonth();
                lastdate = localDate;
            }

        } else if (item.getItemId() == R.id.action_refresh) {
           refreshResults();
        }

        return super.onOptionsItemSelected(item);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getnavigationHeight() {
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName(getString(R.string.app_name))
                .build();

        indextrack = new HashMap<>();
        dupindextrack = new HashMap<>();
        mAppBar = findViewById(R.id.app_bar);
        redlay = findViewById(R.id.redlay);
        redlay.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        calendarView = findViewById(R.id.calander);
        calendarView.setPadding(0, getStatusBarHeight(), 0, 0);
        mNestedView = findViewById(R.id.nestedView);
        monthviewpager = findViewById(R.id.monthviewpager);

        monthviewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (monthviewpager.getVisibility() == View.GONE) return;
                if (isAppBarClosed()) {
                    Log.e("selected", i + "");
                    LocalDate localDate = new LocalDate();
                    MonthPageAdapter monthPageAdapter = (MonthPageAdapter) monthviewpager.getAdapter();
                    MonthModel monthModel = monthPageAdapter.getMonthModels().get(i);
                    String year = monthModel.getYear() == localDate.getYear() ? "" : monthModel.getYear() + "";
                    monthname.setText(monthModel.getMonthnamestr() + " " + year);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        mNestedView.setAppBarTracking(this);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mNestedView.setLayoutManager(linearLayoutManager);
        DateAdapter dateAdapter = new DateAdapter(this, linearLayoutManager);
        mNestedView.setAdapter(dateAdapter);
        final StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(dateAdapter);
        mNestedView.addItemDecoration(headersDecor);
        EventBus.getDefault().register(this);

        monthname = findViewById(R.id.monthname);
        calendarView.setMonthChangeListener(new MonthChangeListener() {
            @Override
            public void onMonthChange(MonthModel monthModel) {
                /**
                 * call when Googlecalendarview is open  scroll viewpager available inside GoogleCalendar
                 */
                LocalDate localDate = new LocalDate();
                String year = monthModel.getYear() == localDate.getYear() ? "" : monthModel.getYear() + "";
                monthname.setText(monthModel.getMonthnamestr() + " " + year);
            }
        });
        toolbar = findViewById(R.id.toolbar);
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        mArrowImageView = findViewById(R.id.arrowImageView);
        if (monthviewpager.getVisibility() == View.VISIBLE) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams();
            ((MyAppBarBehavior) layoutParams.getBehavior()).setScrollBehavior(false);
            mAppBar.setElevation(0);
            mArrowImageView.setVisibility(View.INVISIBLE);
        } else {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams();
            ((MyAppBarBehavior) layoutParams.getBehavior()).setScrollBehavior(true);

            mAppBar.setElevation(20);
            mArrowImageView.setVisibility(View.VISIBLE);
        }

        mNestedView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            LinearLayoutManager llm = (LinearLayoutManager) mNestedView.getLayoutManager();
            DateAdapter dateAdapter = (DateAdapter) mNestedView.getAdapter();

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (mAppBarOffset != 0 && isappbarclosed && newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    calendarView.setCurrentmonth(dateAdapter.geteventallList().get(expandedfirst).getLocalDate());
                    calendarView.adjustheight();
                    mIsExpanded = false;
                    mAppBar.setExpanded(false, false);
                    Log.e("callme", "statechange");
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (isappbarclosed) {
                    int pos = llm.findFirstVisibleItemPosition();
                    View view = llm.findViewByPosition(pos);
                    int currentmonth = dateAdapter.geteventallList().get(pos).getLocalDate().getMonthOfYear();

                    if (dateAdapter.geteventallList().get(pos).getType() == 1) {

                        if (dy > 0 && Math.abs(view.getTop()) > 100) {
                            if (month != currentmonth)
                                EventBus.getDefault().post(new MonthChange(dateAdapter.geteventallList().get(pos).getLocalDate(), dy));
                            month = currentmonth;
                            lastdate = dateAdapter.geteventallList().get(pos).getLocalDate();
                            expandedfirst = pos;
                        } else if (dy < 0 && Math.abs(view.getTop()) < 100 && pos - 1 > 0) {

                            pos--;
                            currentmonth = dateAdapter.geteventallList().get(pos).getLocalDate().getMonthOfYear();

                            if (month != currentmonth)
                                EventBus.getDefault().post(new MonthChange(dateAdapter.geteventallList().get(pos).getLocalDate(), dy));
                            month = currentmonth;
                            lastdate = dateAdapter.geteventallList().get(pos).getLocalDate().dayOfMonth().withMaximumValue();
                            expandedfirst = pos;
                        }
                    } else {
                        lastdate = dateAdapter.geteventallList().get(pos).getLocalDate();
                        expandedfirst = pos;
                    }
                }

                super.onScrolled(recyclerView, dx, dy);
            }
        });

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        }

        mAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (mAppBarOffset != verticalOffset) {
                    mAppBarOffset = verticalOffset;
                    mAppBarMaxOffset = -mAppBar.getTotalScrollRange();
                    int totalScrollRange = appBarLayout.getTotalScrollRange();
                    float progress = (float) (-verticalOffset) / (float) totalScrollRange;
                    Log.e("progress",progress+"");
                    if (monthviewpager.getVisibility()==View.GONE)mAppBar.setElevation(20+(20*Math.abs(1-progress)));

                    mArrowImageView.setRotation(progress * 180);
                    mIsExpanded = verticalOffset == 0;
                    mAppBarIdle = mAppBarOffset >= 0 || mAppBarOffset <= mAppBarMaxOffset;

                    if (mAppBarOffset == -appBarLayout.getTotalScrollRange()) {
                        isappbarclosed = true;
                        setExpandAndCollapseEnabled(false);
                    } else {
                        setExpandAndCollapseEnabled(true);
                    }

                    if (mAppBarOffset == 0) {
                        expandedfirst = linearLayoutManager.findFirstVisibleItemPosition();
                        if (mNestedView.getVisibility() == View.VISIBLE) {
                            topspace = linearLayoutManager.findViewByPosition(linearLayoutManager.findFirstVisibleItemPosition()).getTop();//uncomment jigs 28 feb
                        }
                        if (isappbarclosed) {
                            isappbarclosed = false;
                            mNestedView.stopScroll();
                            calendarView.setCurrentmonth(lastdate);
                        }
                    }
                }
            }
        });

        findViewById(R.id.backsupport).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (monthviewpager.getVisibility() == View.VISIBLE) return;
                        mIsExpanded = !mIsExpanded;
                        mNestedView.stopScroll();
                        mAppBar.setExpanded(mIsExpanded, true);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            LocalDate mintime = new LocalDate().minusYears(YEARS_BACK);
            LocalDate maxtime = new LocalDate().plusYears(YEARS_FORWARD);
//            alleventlist = CalendarDataRepository.readCalendarEventsData(this, mintime, maxtime);
//            calendarView.init(alleventlist, mintime.minusYears(10), maxtime.plusYears(10));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    lastdate = new LocalDate();
                    calendarView.setCurrentmonth(new LocalDate());
                    calendarView.adjustheight();
                    mIsExpanded = false;
                    mAppBar.setExpanded(false, false);
                }
            }, 10);
        }
    }

    private int getDeviceHeight() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        int height1 = size.y;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        Log.e("dd", height1 + "=" + height);
        return height1;
    }

    @Override
    public void onBackPressed() {
        if (mIsExpanded) {
            mIsExpanded = false;
            mNestedView.stopScroll();
            mAppBar.setExpanded(false, true);
        } else if (mNestedView.getVisibility() == View.VISIBLE) {
            monthviewpager.setCurrentItem(calendarView.calculateCurrentMonth(MainActivity.lastdate), false);

            mNestedView.setVisibility(View.GONE);
            monthviewpager.setVisibility(View.VISIBLE);
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams();
            ((MyAppBarBehavior) layoutParams.getBehavior()).setScrollBehavior(false);
            mAppBar.setElevation(0);
            mArrowImageView.setVisibility(View.INVISIBLE);
        } else {
            EventBus.getDefault().unregister(this);
            super.onBackPressed();
            finish();
        }
    }

    private void setExpandAndCollapseEnabled(boolean enabled) {
        if (mNestedView.isNestedScrollingEnabled() != enabled) {
            ViewCompat.setNestedScrollingEnabled(mNestedView, enabled);
        }
    }

    @Override
    public boolean isAppBarClosed() {
        return isappbarclosed;
    }

    @Override
    public int appbaroffset() {
        return expandedfirst;
    }

    public void selectdateFromMonthPager(int year, int month, int day) {
        MainActivity.lastdate = new LocalDate(year, month, day);
        LocalDate localDate = new LocalDate();

        String yearstr = MainActivity.lastdate.getYear() == localDate.getYear() ? "" : MainActivity.lastdate.getYear() + "";

        monthname.setText(MainActivity.lastdate.toString("MMMM") + " " + yearstr);
        calendarView.setCurrentmonth(MainActivity.lastdate);
        calendarView.adjustheight();
        mIsExpanded = false;
        mAppBar.setExpanded(false, false);
        EventBus.getDefault().post(new MessageEvent(new LocalDate(year, month, day)));
        monthviewpager.setVisibility(View.GONE);
        mNestedView.setVisibility(View.VISIBLE);
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams();
        ((MyAppBarBehavior) layoutParams.getBehavior()).setScrollBehavior(true);
        mAppBar.setElevation(20);
        mArrowImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean isAppBarExpanded() {
        return mAppBarOffset == 0;
    }

    @Override
    public boolean isAppBarIdle() {
        return mAppBarIdle;
    }

    /**
     * call when Googlecalendarview is open and tap on any date or scroll viewpager available inside GoogleCalendar
     */
    @Subscribe
    public void onEvent(MessageEvent event) {
        Log.e("call", "onEvent(MessageEvent event)");
        ((DateAdapter)mNestedView.getAdapter()).onMessageEventTrigerred(event);
    }

    /**
     * this call when user is scrolling on mNestedView(recyclerview) and month will change
     * or when toolbar top side current date button selected
     */
    @Subscribe
    public void onEvent(MonthChange event) {
        Log.e("call", "onEvent(MonthChange event)");

        if (!isAppBarExpanded()) {
            LocalDate localDate = new LocalDate();
            String year = event.getMessage().getYear() == localDate.getYear() ? "" : event.getMessage().getYear() + "";
            monthname.setText(event.getMessage().toString("MMMM") + " " + year);

            long diff = System.currentTimeMillis() - lasttime;
            boolean check = diff > 600;
            if (check && event.mdy > 0) {
                monthname.setTranslationY(35);
                mArrowImageView.setTranslationY(35);
                lasttime = System.currentTimeMillis();
                monthname.animate().translationY(0).setDuration(200).start();
                mArrowImageView.animate().translationY(0).setDuration(200).start();
            } else if (check && event.mdy < 0) {
                monthname.setTranslationY(-35);
                mArrowImageView.setTranslationY(-35);
                lasttime = System.currentTimeMillis();
                monthname.animate().translationY(0).setDuration(200).start();
                mArrowImageView.animate().translationY(0).setDuration(200).start();
            }
        }
    }

    /**
     * call only after googlecalendarview init() method is done
     */
    @Subscribe
    public void onEvent(final AddEvent event) {
        Log.e("call", "onEvent(final AddEvent event)");

        ((DateAdapter)mNestedView.getAdapter()).setDateAdapterItems(event.getArrayList());

        final TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {

            int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            int monthheight = getDeviceHeight() - actionBarHeight - getnavigationHeight() - getStatusBarHeight();
            Log.e("monthheight", monthheight + "");
            int recyheight = monthheight - getResources().getDimensionPixelSize(R.dimen.monthtopspace);
            int singleitem = (recyheight - 18) / 6;
            if (monthviewpager.getVisibility() == View.VISIBLE) {
                monthviewpager.setAdapter(new MonthPageAdapter(getSupportFragmentManager(), event.getMonthModels(), singleitem));
                monthviewpager.setCurrentItem(calendarView.calculateCurrentMonth(LocalDate.now()), false);
            }
        }

        indextrack = event.getIndextracker();
        for (Map.Entry<LocalDate, Integer> entry : indextrack.entrySet()) {
            dupindextrack.put(entry.getKey(), entry.getValue());
        }

        if (mNestedView.isAttachedToWindow()) {
            mNestedView.getAdapter().notifyDataSetChanged();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LocalDate localDate = new LocalDate();
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mNestedView.getLayoutManager();
                if (indextrack.containsKey(LocalDate.now())) {
                    Integer val = indextrack.get(LocalDate.now());
                    expandedfirst = val;
                    topspace = 20;
                    linearLayoutManager.scrollToPositionWithOffset(expandedfirst, 20);
                    EventBus.getDefault().post(new MonthChange(localDate, 0));
                    month = localDate.getDayOfMonth();
                    lastdate = localDate;
                }
            }
        }, 100);
    }

    class MonthPageAdapter extends FragmentStatePagerAdapter {
        private ArrayList<MonthModel> monthModels;
        private int singleitemheight;

        public MonthPageAdapter(FragmentManager fragmentManager, ArrayList<MonthModel> monthModels, int singleitemheight) {
            super(fragmentManager);
            this.monthModels = monthModels;
            this.singleitemheight = singleitemheight;
        }

        public ArrayList<MonthModel> getMonthModels() {
            return monthModels;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return monthModels.size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            return MonthFragment.newInstance(monthModels.get(position).getMonth(), monthModels.get(position).getYear(), monthModels.get(position).getFirstday(), monthModels.get(position).getDayModelArrayList(), alleventlist, singleitemheight);
        }
    }


    /**
     * Called whenever this activity is pushed to the foreground, such as after
     * a call to onCreate().
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        } else {
            Toast.makeText(this,"Google Play Services required: " +
                    "after installing, close and relaunch this app.",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == RESULT_OK) {
                    refreshResults();
                } else {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        refreshResults();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this,"Account unspecified.",Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    refreshResults();
                } else {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Attempt to get a set of data from the Google Calendar API to display. If the
     * email address isn't known yet, then call chooseAccount() method so the
     * user can pick an account.
     */
    private void refreshResults() {
        if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new ApiAsyncTask(this).execute();
            } else {
                Toast.makeText(this,"No network connection available.",Toast.LENGTH_LONG).show();
            }
        }
    }


    /**
     * Fill the data TextView with the given List of Strings; called from
     * background threads and async tasks that need to update the UI (in the
     * UI thread).
     * @param dataStrings a List of Strings to populate the main TextView with.
     */
    public void updateResultsOnUi(final HashMap<LocalDate, EventDataModel[]> dataStrings) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alleventlist=dataStrings;
                LocalDate mintime = new LocalDate().minusYears(YEARS_BACK);
                LocalDate maxtime = new LocalDate().plusYears(YEARS_FORWARD);
                calendarView.init(alleventlist, mintime, maxtime);

                calendarView.setCurrentmonth(new LocalDate());
                calendarView.adjustheight();
                mIsExpanded = false;
                mAppBar.setExpanded(false, false);
            }
        });
    }

    /**
     * Show a status message; called from background
     * threads and async tasks that need to update the UI (in the UI thread).
     * @param message a String to display in the UI header TextView.
     */
    public void updateStatus(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,message,Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    private void chooseAccount() {
        startActivityForResult(
                credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    public void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode,
                        MainActivity.this,
                        REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

}