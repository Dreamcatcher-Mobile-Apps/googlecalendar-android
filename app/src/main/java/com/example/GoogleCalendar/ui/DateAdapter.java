package com.example.GoogleCalendar.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.GoogleCalendar.R;
import com.example.GoogleCalendar.models.EventDataModel;
import com.example.GoogleCalendar.models.EventModel;
import com.example.GoogleCalendar.models.MessageEvent;
import com.gjiazhe.scrollparallaximageview.ScrollParallaxImageView;
import com.gjiazhe.scrollparallaximageview.parallaxstyle.VerticalMovingStyle;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import org.joda.time.LocalDate;

import java.util.ArrayList;


public class DateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {

    LocalDate today = LocalDate.now();
    private int lastchangeindex = -1;
    private final int[] monthresource = {
            R.drawable.bkg_01_jan,
            R.drawable.bkg_02_feb,
            R.drawable.bkg_03_mar,
            R.drawable.bkg_04_apr,
            R.drawable.bkg_05_may,
            R.drawable.bkg_06_jun,
            R.drawable.bkg_07_jul,
            R.drawable.bkg_08_aug,
            R.drawable.bkg_09_sep,
            R.drawable.bkg_10_oct,
            R.drawable.bkg_11_nov,
            R.drawable.bkg_12_dec
    };
    private final String[] var = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN",};
    private Toast mostRecentToastMessage;
    private MainActivity activity;
    private LinearLayoutManager linearLayoutManager;

    ArrayList<EventModel> items = new ArrayList<>();

    DateAdapter(MainActivity activity, LinearLayoutManager linearLayoutManager) {
        this.activity = activity;
        this.linearLayoutManager = linearLayoutManager;
    }

    public void setDateAdapterItems(ArrayList<EventModel> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public ArrayList<EventModel> geteventallList() {
        return items;
    }

    public void onMessageEventTrigerred(MessageEvent event) {
        int previous = lastchangeindex;
        if (previous != -1) {
            activity.indextrack.clear();
            activity.indextrack.putAll(activity.dupindextrack);
            notifyDataSetChanged();
        }

        if (activity.indextrack.containsKey(event.getMessage())) {
            int index = activity.indextrack.get(event.getMessage());
            int type = items.get(index).getType();
            if (type == 0 || type == 2) {
                MainActivity.lastdate = event.getMessage();
                activity.expandedfirst = index;
                activity.topspace = 20;
                linearLayoutManager.scrollToPositionWithOffset(activity.expandedfirst, 20);
                lastchangeindex = -1;
            } else {
                MainActivity.lastdate = event.getMessage();
                Integer ind = activity.indextrack.get(event.getMessage());
                ind++;
                for (int i = ind; i < items.size(); i++) {
                    if (event.getMessage().isBefore(items.get(i).getLocalDate())) {
                        ind = i;
                        break;
                    }
                }
                lastchangeindex = ind;
                int typeselect = items.get(ind + 1).getType() == 200 ? 200 : 100;
                if (!items.get(ind - 1).getEventData().getEventName().startsWith("dup")) {
                    items.add(ind, new EventModel(new EventDataModel("dupli", null, null), event.getMessage(), typeselect));
                    ind++;
                }
                activity.expandedfirst = ind;
                items.add(ind, new EventModel(new EventDataModel("click", null, null), event.getMessage(), 1000));
                ind++;
                if (!items.get(ind).getEventData().getEventName().startsWith("dup")) {

                    items.add(ind, new EventModel(new EventDataModel("dupli", null, null), event.getMessage(), typeselect));
                }
                notifyDataSetChanged();
                MainActivity.topspace = 20;
                linearLayoutManager.scrollToPositionWithOffset(activity.expandedfirst, 20);
                for (int i = lastchangeindex; i < items.size(); i++) {
                    if (!items.get(i).getEventData().getEventName().startsWith("dup"))
                        activity.indextrack.put(items.get(i).getLocalDate(), i);
                }
            }

        } else {
            Integer ind = activity.indextrack.get(event.getMessage().dayOfWeek().withMinimumValue().minusDays(1));
            ind++;
            for (int i = ind; i < items.size(); i++) {
                if (event.getMessage().isBefore(items.get(i).getLocalDate())) {
                    ind = i;
                    break;
                }
            }
            lastchangeindex = ind;
            int typeselect = items.get(ind + 1).getType() == 200 ? 200 : 100;
            if (!items.get(ind - 1).getEventData().getEventName().startsWith("dup")) {
                items.add(ind, new EventModel(new EventDataModel("dupli", null, null), event.getMessage(), typeselect));
                ind++;
            }

            activity.expandedfirst = ind;
            items.add(ind, new EventModel(new EventDataModel("click", null, null), event.getMessage(), 1000));
            ind++;
            if (!items.get(ind).getEventData().getEventName().startsWith("dup")) {
                items.add(ind, new EventModel(new EventDataModel("dupli", null, null), event.getMessage(), typeselect));
            }

            notifyDataSetChanged();
            activity.topspace = 20;
            linearLayoutManager.scrollToPositionWithOffset(activity.expandedfirst, 20);

            for (int i = lastchangeindex; i < items.size(); i++) {
                if (!items.get(i).getEventData().getEventName().startsWith("dup"))
                    activity.indextrack.put(items.get(i).getLocalDate(), i);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position > 1 && items.get(position).getType() == 0 && getHeaderId(position) == getHeaderId(position - 1))
            return 5;
        if (position > 1 && items.get(position).getType() == 3 && items.get(position - 1).getType() == 1)
            return 7;
        if (position + 1 < items.size() && items.get(position).getType() == 3 && (items.get(position + 1).getType() == 1 || items.get(position + 1).getType() == 0))
            return 6;
        return items.get(position).getType();
    }

    public int getHeaderItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_item, parent, false);
            return new ItemViewHolder(view);
        } else if (viewType == 5) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.viewitemlessspace, parent, false);
            return new ItemViewHolder(view);
        } else if (viewType == 100) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.extraspace, parent, false);
            return new RecyclerView.ViewHolder(view) {
            };
        } else if (viewType == 200) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.liitlespace, parent, false);
            return new RecyclerView.ViewHolder(view) {
            };
        } else if (viewType == 1) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.viewlast, parent, false);
            return new EndViewHolder(view);
        } else if (viewType == 2) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.noplanlay, parent, false);
            return new NoplanViewHolder(view);
        } else if (viewType == 1000) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.noplanlittlespace, parent, false);
            return new NoplanViewHolder(view);
        } else if (viewType == 6) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rangelayextrabottomspace, parent, false);
            return new RangeViewHolder(view);
        } else if (viewType == 7) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rangelayextratopspace, parent, false);
            return new RangeViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rangelay, parent, false);
            return new RangeViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewtype = getItemViewType(position);
        if (viewtype == 0 || viewtype == 5) {
            ItemViewHolder holder = (ItemViewHolder) viewHolder;
            holder.eventtextview.setText(items.get(position).getEventData().getEventName());
            if (position + 1 < items.size() && items.get(position).getLocalDate().equals(today) && (!items.get(position + 1).getLocalDate().equals(today) || items.get(position + 1).getType() == 100 || items.get(position + 1).getType() == 200)) {
                holder.circle.setVisibility(View.VISIBLE);
                holder.line.setVisibility(View.VISIBLE);
            } else {
                holder.circle.setVisibility(View.GONE);
                holder.line.setVisibility(View.GONE);
            }
        } else if (viewtype == 1) {
            EndViewHolder holder = (EndViewHolder) viewHolder;
            holder.eventimageview.setImageResource(monthresource[items.get(position).getLocalDate().getMonthOfYear() - 1]);
            holder.monthname.setText(items.get(position).getLocalDate().toString("MMMM YYYY"));
        } else if (viewtype == 2 || viewtype == 100 || viewtype == 200 || viewtype == 1000) {

        } else {
            RangeViewHolder holder = (RangeViewHolder) viewHolder;
            holder.rangetextview.setText(items.get(position).getEventData().getEventName().replaceAll("tojigs", ""));
        }
    }

    @Override
    public long getHeaderId(int position) {
        if (items.get(position).getType() == 1) return position;
        else if (items.get(position).getType() == 3) return position;
        else if (items.get(position).getType() == 100) return position;
        else if (items.get(position).getType() == 200) return position;
        LocalDate localDate = items.get(position).getLocalDate();
        String uniquestr = "" + localDate.getDayOfMonth() + localDate.getMonthOfYear() + localDate.getYear();
        return Long.parseLong(uniquestr);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int position) {
        int viewtype = getHeaderItemViewType(position);
        if (viewtype == 2) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.todayheader, parent, false);
            return new RecyclerView.ViewHolder(view) {
            };
        } else if (viewtype == 0 && items.get(position).getLocalDate().equals(today)) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.todayheader, parent, false);
            return new RecyclerView.ViewHolder(view) {
            };
        } else if (viewtype == 1 || viewtype == 3 || viewtype == 100 || viewtype == 200) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.empty, parent, false);
            return new RecyclerView.ViewHolder(view) {
            };
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.headerview, parent, false);
            return new RecyclerView.ViewHolder(view) {
            };
        }
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewtype = getHeaderItemViewType(position);
        if (viewtype == 0 || viewtype == 2 || viewtype == 1000) {
            TextView vartextView = holder.itemView.findViewById(R.id.textView9);
            TextView datetextView = holder.itemView.findViewById(R.id.textView10);
            vartextView.setText(var[items.get(position).getLocalDate().getDayOfWeek() - 1]);
            datetextView.setText(items.get(position).getLocalDate().getDayOfMonth() + "");
            holder.itemView.setTag(position);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        String daysList[] = {"", "Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday", "Sunday"};

        TextView eventtextview;
        View circle, line;

        public ItemViewHolder(View itemView) {
            super(itemView);
            eventtextview = itemView.findViewById(R.id.view_item_textview);
            eventtextview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    // Todo: Move to separate method.
                    LocalDate localDate = items.get(getAdapterPosition()).getLocalDate();
                    String title = items.get(getAdapterPosition()).getEventData().getEventName();
                    String date = daysList[localDate.getDayOfWeek()] + ", " + localDate.toString("d MMM");
                    String toastText = title + "\n" + date;
                    if (items.get(getAdapterPosition()).getEventData().getStartDateTime() != null) {
                        String startingTime = items.get(getAdapterPosition()).getEventData().getStartDateTime().toString();
                        toastText += ("\n" + startingTime);
                    }
                    if (items.get(getAdapterPosition()).getEventData().getEndDateTime() != null) {
                        String endingTime = items.get(getAdapterPosition()).getEventData().getEndDateTime().toString();
                        toastText += ("\n" + endingTime);
                    }
                    if (mostRecentToastMessage != null) {
                        mostRecentToastMessage.cancel();
                    }
                    mostRecentToastMessage = Toast.makeText(activity, toastText, Toast.LENGTH_SHORT);
                    mostRecentToastMessage.show();

                }
            });
            circle = itemView.findViewById(R.id.circle);
            line = itemView.findViewById(R.id.line);
        }
    }

    class EndViewHolder extends RecyclerView.ViewHolder {

        ScrollParallaxImageView eventimageview;
        TextView monthname;

        public EndViewHolder(View itemView) {
            super(itemView);
            eventimageview = itemView.findViewById(R.id.imageView);
            eventimageview.setParallaxStyles(new VerticalMovingStyle());
            monthname = itemView.findViewById(R.id.textView11);
        }
    }

    class NoplanViewHolder extends RecyclerView.ViewHolder {

        TextView noplantextview;

        public NoplanViewHolder(View itemView) {
            super(itemView);
            noplantextview = itemView.findViewById(R.id.view_noplan_textview);
        }
    }

    class RangeViewHolder extends RecyclerView.ViewHolder {

        TextView rangetextview;

        public RangeViewHolder(View itemView) {
            super(itemView);
            rangetextview = itemView.findViewById(R.id.view_range_textview);
        }
    }
}