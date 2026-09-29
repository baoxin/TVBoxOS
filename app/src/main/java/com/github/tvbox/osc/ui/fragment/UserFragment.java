package com.github.tvbox.osc.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseLazyFragment;
import com.github.tvbox.osc.bean.Movie;
import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.event.ServerEvent;
import com.github.tvbox.osc.ui.activity.DetailActivity;
import com.github.tvbox.osc.ui.activity.FastSearchActivity;
import com.github.tvbox.osc.ui.activity.HistoryActivity;
import com.github.tvbox.osc.ui.activity.PushActivity;
import com.github.tvbox.osc.ui.activity.SearchActivity;
import com.github.tvbox.osc.ui.adapter.HomeHistoryAdapter;
import com.github.tvbox.osc.ui.adapter.HomeHotVodAdapter;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.ImgUtil;
import com.github.tvbox.osc.util.UA;
import com.github.tvbox.osc.viewmodel.SourceViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author pj567
 * @date :2021/3/9
 * @description:
 */
public class UserFragment extends BaseLazyFragment {
    public static HomeHotVodAdapter homeHotVodAdapter;
    public static TvRecyclerView tvHotList;
    private HomeHistoryAdapter historyAdapter;
    private TvRecyclerView tvHistoryList;
    private TvRecyclerView tvDoubanList;
    private HomeHotVodAdapter doubanAdapter;
    private View rowHistory;
    private View rowSite;
    private List<Movie.Video> homeSourceRec;
    private SourceViewModel sourceViewModel;

    public static UserFragment newInstance() {
        return new UserFragment();
    }

    public static UserFragment newInstance(List<Movie.Video> recVod) {
        return new UserFragment().setArguments(recVod);
    }

    public UserFragment setArguments(List<Movie.Video> recVod) {
        this.homeSourceRec = recVod;
        return this;
    }

    @Override
    protected void onFragmentResume() {
        super.onFragmentResume();
        tvHotList.setVisibility(View.VISIBLE);
        tvHotList.setHasFixedSize(true);
        tvHotList.setLayoutManager(new V7LinearLayoutManager(this.mContext, V7LinearLayoutManager.HORIZONTAL, false));
        int paddingLeft = -tvHotList.mHorizontalSpacingWithMargins / 2 + getResources().getDimensionPixelSize(R.dimen.vs_6);
        int paddingTop = getResources().getDimensionPixelSize(R.dimen.vs_6);
        int paddingRight = -tvHotList.mHorizontalSpacingWithMargins / 2 + getResources().getDimensionPixelSize(R.dimen.vs_6);
        int paddingBottom = getResources().getDimensionPixelSize(R.dimen.vs_6);
        tvHotList.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        if (Hawk.get(HawkConfig.HOME_REC, HawkConfig.DEFAULT_HOME_REC) == 2) {
            homeHotVodAdapter.setNewData(new ArrayList<Movie.Video>());
        }
        loadHistory();
    }

    private void loadHistory() {
        if (historyAdapter == null) return;
        List<VodInfo> records = RoomDataManger.getAllVodRecord(20);
        historyAdapter.setNewData(records);
        if (rowHistory != null) {
            rowHistory.setVisibility(records.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_user;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TvRecyclerView hotList = view.findViewById(R.id.tvHotList);
        if (hotList != null && hotList.getLayoutManager() == null) {
            hotList.setLayoutManager(new V7LinearLayoutManager(mContext, V7LinearLayoutManager.HORIZONTAL, false));
        }
    }

    private void jumpSearch(Movie.Video vod) {
        Intent newIntent;
        if (Hawk.get(HawkConfig.FAST_SEARCH_MODE, true)) {
            newIntent = new Intent(mContext, FastSearchActivity.class);
        } else {
            newIntent = new Intent(mContext, SearchActivity.class);
        }
        newIntent.putExtra("title", vod.name);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mActivity.startActivity(newIntent);
    }

    private void openVod(Movie.Video vod) {
        if (vod == null) return;
        if (homeSourceRec != null && vod.action != null) {
            sourceViewModel.action(vod.sourceKey, vod.action);
            return;
        }
        if (vod.id != null && vod.id.startsWith("msearch:")) {
            jumpSearch(vod);
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("id", vod.id);
        bundle.putString("sourceKey", vod.sourceKey);
        bundle.putString("title", vod.name);
        bundle.putString("picture", vod.pic);
        jumpActivity(DetailActivity.class, bundle);
    }

    private final TvRecyclerView.OnItemListener rowItemListener = new TvRecyclerView.OnItemListener() {
        @Override
        public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
        }

        @Override
        public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
            itemView.animate().scaleX(1.05f).scaleY(1.05f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
        }

        @Override
        public void onItemClick(TvRecyclerView parent, View itemView, int position) {
        }
    };

    private ImgUtil.Style style;
    @Override
    protected void init() {
        EventBus.getDefault().register(this);
        sourceViewModel = new ViewModelProvider(this).get(SourceViewModel.class);
        tvHotList = findViewById(R.id.tvHotList);
        tvHistoryList = findViewById(R.id.tvHistoryList);
        tvDoubanList = findViewById(R.id.tvDoubanList);
        rowHistory = findViewById(R.id.rowHistory);
        rowSite = findViewById(R.id.rowSite);
        if (homeSourceRec != null) {
            style=ImgUtil.initStyle();
        }
        homeHotVodAdapter = new HomeHotVodAdapter(style,"站点推荐");
        doubanAdapter = new HomeHotVodAdapter(null,"豆瓣热播");
        homeHotVodAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (ApiConfig.get().getSourceBeanList().isEmpty())
                    return;
                Movie.Video vod = ((Movie.Video) adapter.getItem(position));

                if (homeSourceRec != null && vod.action != null) {
                    sourceViewModel.action(vod.sourceKey, vod.action);
                    return;
                }

                if (vod.id != null && vod.id.startsWith("msearch:")) {
                    jumpSearch(vod);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("id", vod.id);
                    bundle.putString("sourceKey", vod.sourceKey);
                    bundle.putString("title", vod.name);
                    bundle.putString("picture", vod.pic);
                    jumpActivity(DetailActivity.class, bundle);
                }
            }
        });
        
        homeHotVodAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                if (ApiConfig.get().getSourceBeanList().isEmpty()) return false;
                Movie.Video vod = ((Movie.Video) adapter.getItem(position));
                // Additional Check if : Home Rec 0=豆瓣, 1=推荐, 2=历史
                assert vod != null;
                if (vod.id != null && vod.id.startsWith("msearch:")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("id", vod.id);
                    bundle.putString("sourceKey", vod.sourceKey);
                    bundle.putString("title", vod.name);
                    bundle.putString("picture", vod.pic);
                    jumpActivity(DetailActivity.class, bundle);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("title", vod.name);
                    jumpActivity(FastSearchActivity.class, bundle);
                }
                return true;
            }    
        });

        tvHotList.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
                itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                itemView.animate().scaleX(1.05f).scaleY(1.05f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {

            }
        });
        tvHotList.setAdapter(homeHotVodAdapter);
        initHomeHotVod(homeHotVodAdapter);
        if (homeSourceRec == null && rowSite != null) {
            rowSite.setVisibility(View.GONE);
        }

        doubanAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (ApiConfig.get().getSourceBeanList().isEmpty())
                    return;
                openVod((Movie.Video) adapter.getItem(position));
            }
        });
        doubanAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                Movie.Video vod = ((Movie.Video) adapter.getItem(position));
                if (vod == null) return false;
                Bundle bundle = new Bundle();
                bundle.putString("title", vod.name);
                jumpActivity(FastSearchActivity.class, bundle);
                return true;
            }
        });
        tvDoubanList.setLayoutManager(new V7LinearLayoutManager(this.mContext, V7LinearLayoutManager.HORIZONTAL, false));
        tvDoubanList.setAdapter(doubanAdapter);
        tvDoubanList.setOnItemListener(rowItemListener);
        setDouBanData(doubanAdapter);

        historyAdapter = new HomeHistoryAdapter();
        historyAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                VodInfo vodInfo = (VodInfo) adapter.getItem(position);
                if (vodInfo == null) return;
                Bundle bundle = new Bundle();
                bundle.putString("id", vodInfo.id);
                bundle.putString("sourceKey", vodInfo.sourceKey);
                bundle.putString("title", vodInfo.name);
                bundle.putString("picture", vodInfo.pic);
                jumpActivity(DetailActivity.class, bundle);
            }
        });
        historyAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                jumpActivity(HistoryActivity.class);
                return true;
            }
        });
        tvHistoryList.setLayoutManager(new V7LinearLayoutManager(this.mContext, V7LinearLayoutManager.HORIZONTAL, false));
        tvHistoryList.setAdapter(historyAdapter);
        tvHistoryList.setOnItemListener(rowItemListener);
        loadHistory();
        sourceViewModel.actionResult.observe(this, new Observer<JSONObject>() {
            @Override
            public void onChanged(JSONObject jsonObject) {
                if (jsonObject == null) return;
                String msg = jsonObject.optString("msg");
                if (!msg.isEmpty()) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initHomeHotVod(HomeHotVodAdapter adapter) {
        if (homeSourceRec != null) {
            adapter.setNewData(homeSourceRec);
        }
    }

    private void setDouBanData(HomeHotVodAdapter adapter) {
        try {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DATE);
            String today = String.format("%d%d%d", year, month, day);
            String requestDay = Hawk.get("home_hot_day", "");
            if (requestDay.equals(today)) {
                String json = Hawk.get("home_hot", "");
                if (!json.isEmpty()) {
                    ArrayList<Movie.Video> hotMovies = loadHots(json);
                    if (hotMovies != null && hotMovies.size() > 0) {
                        adapter.setNewData(hotMovies);
                        return;
                    }
                }
            }
            String doubanUrl = "https://movie.douban.com/j/new_search_subjects?sort=U&range=0,10&tags=&playable=1&start=0&year_range=" + year + "," + year;
            OkGo.<String>get(doubanUrl)
                    .headers("User-Agent", UA.randomOne())
                    .execute(new AbsCallback<String>() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            String netJson = response.body();
                            Hawk.put("home_hot_day", today);
                            Hawk.put("home_hot", netJson);
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.setNewData(loadHots(netJson));
                                }
                            });
                        }

                        @Override
                        public String convertResponse(okhttp3.Response response) throws Throwable {
                            return response.body().string();
                        }
                    });
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private ArrayList<Movie.Video> loadHots(String json) {
        ArrayList<Movie.Video> result = new ArrayList<>();
        try {
            JsonObject infoJson = new Gson().fromJson(json, JsonObject.class);
            JsonArray array = infoJson.getAsJsonArray("data");
            int limit = Math.min(array.size(), 25);
            for (int i = 0; i < limit; i++) {  // 改用索引循环
                JsonElement ele = array.get(i);
                JsonObject obj = ele.getAsJsonObject();
                Movie.Video vod = new Movie.Video();
                vod.name = obj.get("title").getAsString();
                vod.note = obj.get("rate").getAsString();
                if (!vod.note.isEmpty()) vod.note += " 分";
                vod.pic = obj.get("cover").getAsString()
                        + "@User-Agent=" + UA.randomOne()
                        + "@Referer=https://www.douban.com/";

                result.add(vod);
            }
        } catch (Throwable th) {

        }
        return result;
    }

    private View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus)
                v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
            else
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new BounceInterpolator()).start();
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void server(ServerEvent event) {
        if (event.type == ServerEvent.SERVER_CONNECTION) {
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(RefreshEvent event) {
        if (event.type == RefreshEvent.TYPE_HISTORY_REFRESH) {
            loadHistory();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
