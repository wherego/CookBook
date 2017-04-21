package com.simoncherry.cookbook.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.simoncherry.cookbook.R;
import com.simoncherry.cookbook.biz.CategoryBiz;
import com.simoncherry.cookbook.contract.CategoryContract;
import com.simoncherry.cookbook.di.component.DaggerCategoryComponent;
import com.simoncherry.cookbook.di.module.CategoryModule;
import com.simoncherry.cookbook.model.MobCategoryChild1;
import com.simoncherry.cookbook.model.MobCategoryChild2;
import com.simoncherry.cookbook.model.MobCategoryResult;
import com.simoncherry.cookbook.model.RealmCategory;
import com.simoncherry.cookbook.presenter.CategoryPresenter;
import com.simoncherry.cookbook.realm.RealmHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;

public class SplashActivity extends BaseActivity implements CategoryContract.View{

    @Inject
    CategoryPresenter categoryPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initComponent() {
        DaggerCategoryComponent.builder()
                .categoryModule(new CategoryModule(new CategoryBiz(), this))
                .build()
                .inject(this);
    }

    @Override
    public void setPresenter(CategoryContract.Presenter presenter) {
        categoryPresenter = (CategoryPresenter) presenter;
    }

    @Override
    public void onQueryCategorySuccess(MobCategoryResult result) {
        handleCategoryResult(result);
    }

    @Override
    public void onQueryFailed() {
    }

    @Override
    public void onQueryError(String msg) {
    }

    private void init() {
        categoryPresenter.queryCategory();
    }

    private void handleCategoryResult(MobCategoryResult result) {
        if (result != null) {
            List<RealmCategory> categoryList = new ArrayList<>();
            // 第1层子类
            ArrayList<MobCategoryChild1> child1 = result.getChilds();
            if (child1 != null && child1.size() > 0) {
                for (MobCategoryChild1 categoryChild1 : child1) {
                    categoryList.add(RealmHelper.convertMobCategoryToRealmCategory(categoryChild1.getCategoryInfo(), false));
                    // 第2层子类
                    ArrayList<MobCategoryChild2> child2 = categoryChild1.getChilds();
                    if (child2 != null && child2.size() > 0) {
                        for (MobCategoryChild2 categoryChild2 : child2) {
                            categoryList.add(RealmHelper.convertMobCategoryToRealmCategory(categoryChild2.getCategoryInfo(), true));
                        }
                    }
                }
            }
            saveCategoryToRealm(categoryList);
        }
    }

    private void saveCategoryToRealm(final List<RealmCategory> categoryList) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmHelper.saveCategoryToRealm(realm, categoryList);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                realm.close();
                startMainActivity();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                realm.close();
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                startMainActivity();
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
        finish();
    }
}
