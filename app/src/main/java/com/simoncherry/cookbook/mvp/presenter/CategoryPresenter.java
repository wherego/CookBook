package com.simoncherry.cookbook.mvp.presenter;

import com.simoncherry.cookbook.mvp.biz.CategoryBiz;
import com.simoncherry.cookbook.api.ApiCallback;
import com.simoncherry.cookbook.mvp.contract.CategoryContract;
import com.simoncherry.cookbook.model.MobCategoryResult;

import javax.inject.Inject;

/**
 * Created by Simon on 2017/3/29.
 */

public class CategoryPresenter extends RxPresenter<CategoryContract.View> implements CategoryContract.Presenter{

    private CategoryBiz mBiz;

    @Inject
    public CategoryPresenter(CategoryBiz mBiz) {
        this.mBiz = mBiz;
    }

    @Override
    public void queryCategory() {
        addSubscribe(mBiz.queryCategory(new ApiCallback.QueryCategoryCallback() {
            @Override
            public void onStart() {
                mView.onShowProgressBar();
            }

            @Override
            public void onEnd() {
                mView.onHideProgressBar();
            }

            @Override
            public void onQueryCategorySuccess(MobCategoryResult value) {
                mView.onQueryCategorySuccess(value);
            }

            @Override
            public void onQueryCategoryEmpty() {
                mView.onQueryEmpty();
            }

            @Override
            public void onQueryError(String msg) {
                mView.onQueryError(msg);
            }
        }));
    }
}
