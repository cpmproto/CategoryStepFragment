/*
 * Copyright (C) 2016 Dewin J. Martínez (@cpmproto) <dewin.martinez@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.cpmproto.categorystepfragment.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.widget.GuidedAction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.cpmproto.categorystepfragment.R;
import com.github.cpmproto.categorystepfragment.action.ActionCategory;
import com.github.cpmproto.categorystepfragment.action.ActionElement;
import com.github.cpmproto.categorystepfragment.action.GuidedActionList;
import com.github.cpmproto.categorystepfragment.base.Category;
import com.github.cpmproto.categorystepfragment.base.GuidedStepListFragment;

import java.util.ArrayList;
import java.util.List;

public class CategoryStepFragment extends GuidedStepListFragment {
    private static int CATEGORY_BUTTON_ID = 0;
    private List<ActionCategory> actionCategories;
    private String categoryTitle = "";
    private String categoryDescription = "";
    private String categoryKey = "";
    private StepClickListener callback;

    private static int sSelectedCard = -1;

    public static GuidedStepListFragment build(List<ActionCategory> actionCategories,
                                               StepClickListener stepClickListener,
                                               Category category) {
        CategoryStepFragment fragment = new CategoryStepFragment();

        fragment.setActionCategories(actionCategories);
        fragment.setStepClickListener(stepClickListener);
        fragment.setCategoryKey(category.getKey());
        fragment.setCategoryTitle(category.getTitle());
        fragment.setCategoryDescription(category.getDescription());
        return fragment;
    }

    public void setActionCategories(List<ActionCategory> actionCategories) {
        this.actionCategories = actionCategories;
    }

    public void setCategoryTitle(String title) {
        this.categoryTitle = title;
    }

    public void setCategoryDescription(String description) {
        this.categoryDescription = description;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public void setCategoryKey(String categoryKey) {
        this.categoryKey = categoryKey;
        this.firstElementKeys = categoryKey;
    }

    public void setStepClickListener(StepClickListener stepClickListener) {
        this.callback = stepClickListener;
    }

    @Override
    public int onProvideTheme() {
        return R.style.Theme_Example_LeanbackWizard;
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedActionList> actions, Bundle savedInstanceState) {
        List<GuidedAction> subActions = new ArrayList();
        List<GuidedAction> guidedActions = new ArrayList();

        GuidedAction action = new GuidedAction.Builder(getActivity())
                .id(CATEGORY_BUTTON_ID)
                .title(categoryTitle)
                .description(categoryDescription)
                .subActions(subActions)
                .build();

        guidedActions.add(action);

        if (actionCategories == null)
            return;

        for (final ActionCategory actionCategory : actionCategories) {
            for (final ActionElement actionElement : actionCategory.getActionElements()) {
                action = new GuidedAction.Builder(getActivity())
                        .id(actionElement.getId())
                        .title(actionElement.getDescription())
                        .description(actionElement.getSubDescription())
                        .build();

                final GuidedAction finalAction = action;

                Glide.with(getActivity())
                        .load(actionElement.getImageUrl())
                        .asBitmap()
                        .centerCrop()
                        .error(null)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                if (resource != null) {
                                    Drawable drawable = new BitmapDrawable(resource);
                                    finalAction.setIcon(drawable);
                                    notifyActionChanged(actionElement.getId());
                                }
                            }
                        });
                guidedActions.add(finalAction);
            }

            actions.add(new GuidedActionList(actionCategory.getCategory(), guidedActions));
            guidedActions = new ArrayList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        GuidedAction payment = findActionById(0, getCategoryKey());

        if (payment == null)
            return;

        List<GuidedAction> paymentSubActions = payment.getSubActions();

        if (paymentSubActions != null)
            paymentSubActions.clear();

        for (int i = 0; i < actionCategories.size(); i++) {
            paymentSubActions.add(new GuidedAction.Builder(getActivity())
                    .title(actionCategories.get(i).getCategory())
                    .description("")
                    .checkSetId(GuidedAction.DEFAULT_CHECK_SET_ID)
                    .checked(i == 0)
                    .build()
            );
        }

        if (sSelectedCard >= 0 && sSelectedCard < actionCategories.size())
            payment.setDescription(actionCategories.get(sSelectedCard).getCategory());
    }

    @Override
    public boolean onSubGuidedActionClicked(GuidedAction action) {
        if (action.isChecked()) {
            findActionById(CATEGORY_BUTTON_ID, getCategoryKey()).setDescription(getCategoryKey());
            notifyActionChanged(findActionPositionById(CATEGORY_BUTTON_ID, getCategoryKey()));

            if (callback != null)
                callback.onSubGuidedActionClicked(action);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (callback != null)
            callback.onGuidedActionClicked(action);
    }
}
