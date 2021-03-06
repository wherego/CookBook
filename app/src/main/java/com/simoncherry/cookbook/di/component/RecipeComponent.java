package com.simoncherry.cookbook.di.component;

import com.simoncherry.cookbook.ui.fragment.RecipeFragment;
import com.simoncherry.cookbook.di.module.RecipeModule;

import dagger.Component;

/**
 * Created by Simon on 2017/3/29.
 */
@Component(modules = RecipeModule.class)
public interface RecipeComponent {
    void inject(RecipeFragment recipeFragment);
}
