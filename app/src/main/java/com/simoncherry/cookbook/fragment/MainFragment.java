package com.simoncherry.cookbook.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;
import com.simoncherry.cookbook.R;
import com.simoncherry.cookbook.adapter.MainFragmentAdapter;
import com.simoncherry.cookbook.model.RealmCategory;
import com.simoncherry.cookbook.realm.RealmHelper;
import com.zhl.channeltagview.bean.ChannelItem;
import com.zhl.channeltagview.listener.OnChannelItemClicklistener;
import com.zhl.channeltagview.listener.UserActionListener;
import com.zhl.channeltagview.view.ChannelTagView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private final static String TAG = MainFragment.class.getSimpleName();

    @BindView(R.id.layout_tab)
    TabLayout tabLayout;
    @BindView(R.id.iv_expand)
    ImageView ivExpand;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.layout_channel)
    FrameLayout layoutChannel;
    @BindView(R.id.channel_tag_view)
    ChannelTagView channelTagView;

    private Unbinder unbinder;

    private MainFragmentAdapter mainFragmentAdapter;
    private ArrayList<Fragment> fragmentList;
    private ArrayList<String> titleList;

    private ArrayList<ChannelItem> addedChannels = new ArrayList<>();
    private ArrayList<ChannelItem> unAddedChannels = new ArrayList<>();

    private Realm realm;
    private RealmResults<RealmCategory> realmResults;

    private boolean isInit = false;


    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_page, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realmResults.removeAllChangeListeners();
        realm.close();
        unbinder.unbind();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        initView();
        initViewPager();
        initIndicator();
        //initFragment();
        initChannelView();
        initRealm();
    }

    private void initView() {
        ivExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutChannel.getVisibility() == View.GONE) {
                    layoutChannel.setVisibility(View.VISIBLE);
                } else {
                    layoutChannel.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initViewPager() {
        titleList = new ArrayList<>();
        fragmentList = new ArrayList<>();
        mainFragmentAdapter = new MainFragmentAdapter(getChildFragmentManager(), fragmentList, titleList);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(mainFragmentAdapter);
    }

    private void initIndicator() {
        tabLayout.setupWithViewPager(viewPager);
    }

    private void initChannelView() {
        channelTagView.showPahtAnim(false);
        channelTagView.setOnChannelItemClicklistener(new OnChannelItemClicklistener() {
            @Override
            public void onAddedChannelItemClick(View itemView, int position) {
            }

            @Override
            public void onUnAddedChannelItemClick(View itemView, ArrayList<ChannelItem> checkedChannels) {
                RealmHelper.changeCategorySelectedByCtgId(realm, checkedChannels.get(checkedChannels.size()-1).value, true);
            }
        });

        channelTagView.setUserActionListener(new UserActionListener() {
            @Override
            public void onMoved(int fromPos, int toPos, ArrayList<ChannelItem> checkedChannels) {
                addedChannels.clear();
                addedChannels.addAll(checkedChannels);
            }

            @Override
            public void onSwiped(int position, View itemView, ArrayList<ChannelItem> checkedChannels, ArrayList<ChannelItem> uncheckedChannels) {
                RealmHelper.changeCategorySelectedByCtgId(realm, uncheckedChannels.get(uncheckedChannels.size()-1).value, false);
            }
        });
    }

    private void initRealm() {
        realm = Realm.getDefaultInstance();

        if (realm.where(RealmCategory.class)
                .equalTo("isChild", true)
                .equalTo("isSelected", true)
                .findAll().size() == 0) {
            Logger.t(TAG).e("set first category selected");
            RealmHelper.setFirstChildCategorySelected(realm);
        }

        realmResults = realm.where(RealmCategory.class)
                .equalTo("isChild", true)
                .findAllAsync();
        realmResults.addChangeListener(new RealmChangeListener<RealmResults<RealmCategory>>() {
            @Override
            public void onChange(RealmResults<RealmCategory> element) {
                Logger.t(TAG).e("RealmCategory: " + element.toString());
                if (element.size() > 0) {
                    handleRealmResult(element);
                }
            }
        });
    }

    private void handleRealmResult(RealmResults<RealmCategory> element) {
        titleList.clear();
        fragmentList.clear();
        addedChannels.clear();
        unAddedChannels.clear();

        for (int i=0; i<element.size(); i++) {
            RealmCategory realmCategory = element.get(i);
            if (realmCategory.isSelected()) {
                titleList.add(realmCategory.getName());
                RecipeFragment recipeFragment = RecipeFragment.newInstance(realmCategory.getCtgId());
                fragmentList.add(recipeFragment);

                addedChannels.add(convertCategoryToChannel(i, realmCategory));
            } else {
                unAddedChannels.add(convertCategoryToChannel(i, realmCategory));
            }
        }
        Logger.t(TAG).e("addedChannels: " + addedChannels.toString());
        Logger.t(TAG).e("unAddedChannels: " + unAddedChannels.toString());
        if (!isInit) {
            isInit = true;
            channelTagView.initChannels(addedChannels, unAddedChannels, null);
        }
        mainFragmentAdapter.notifyDataSetChanged();
    }

    private ChannelItem convertCategoryToChannel(int index, RealmCategory category) {
        ChannelItem channelItem = new ChannelItem();
        channelItem.id = index;
        channelItem.title = category.getName();
        channelItem.value = category.getCtgId();
        return channelItem;
    }
}