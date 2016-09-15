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

package com.github.cpmproto.categorystepfragment.example;

import android.app.Activity;
import android.os.Bundle;
import android.support.v17.leanback.widget.GuidedAction;
import android.widget.Toast;

import com.github.cpmproto.categorystepfragment.action.ActionCategory;
import com.github.cpmproto.categorystepfragment.action.ActionElement;
import com.github.cpmproto.categorystepfragment.base.Category;
import com.github.cpmproto.categorystepfragment.base.GuidedStepListFragment;
import com.github.cpmproto.categorystepfragment.R;
import com.github.cpmproto.categorystepfragment.fragment.CategoryStepFragment;
import com.github.cpmproto.categorystepfragment.fragment.StepClickListener;

import java.util.ArrayList;
import java.util.List;

public class ExampleActivity extends Activity implements StepClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.drawable.background_blackned);

        GuidedStepListFragment fragment = CategoryStepFragment.build(getData(), this,
                new Category("Category", "Select one", "All"));

        if (fragment != null) {
            GuidedStepListFragment.addAsRoot(this, fragment, android.R.id.content);
        }
    }

    @Override
    public void onBackPressed() {
        if (GuidedStepListFragment.getCurrentGuidedStepListFragment(getFragmentManager())
                instanceof CategoryStepFragment) {
            // The user 'bought' the product. When he presses 'Back' the Wizard will be closed and
            // he will not be send back to 'Processing Payment...'-Screen.
            finish();
        } else super.onBackPressed();
    }

    private List<ActionCategory> getData() {
        List<ActionCategory> actionCategories = new ArrayList<>();
        List<ActionElement> infantiles = new ArrayList<>();

        infantiles.add(new ActionElement(1, "JavaScript", "",
                "https://www.iconfinder.com/icons/652581/download/png/128",
                "http://localhost/example.html"));

        infantiles.add(new ActionElement(2, "Php", "",
                "https://www.iconfinder.com/icons/652580/download/png/128",
                "http://localhost/example.html"));

        infantiles.add(new ActionElement(3, "Html5", "",
                "https://www.iconfinder.com/icons/652583/download/png/128",
                "http://localhost/example.html"));

        infantiles.add(new ActionElement(4, "Android", "",
                "https://www.iconfinder.com/icons/1269841/download/png/128"));


        List<ActionElement> deportes = new ArrayList<>();
        deportes.add(new ActionElement(5, "Twitter", "",
                "https://www.iconfinder.com/icons/771365/download/png/128"));

        deportes.add(new ActionElement(7, "Youtube", "",
                "https://www.iconfinder.com/icons/771367/download/png/128"));

        deportes.add(new ActionElement(6, "Facebook", "",
                "https://www.iconfinder.com/icons/771367/download/png/128"));

        deportes.add(new ActionElement(8, "Google+", "",
                "https://www.iconfinder.com/icons/682223/download/png/128"));


        List<ActionElement> all = new ArrayList<>();
        all.addAll(infantiles);
        all.addAll(deportes);
        actionCategories.add(new ActionCategory("All", all));
        actionCategories.add(new ActionCategory("Skill", infantiles));
        actionCategories.add(new ActionCategory("Social Networks", deportes));

        return actionCategories;
    }


    @Override
    public boolean onSubGuidedActionClicked(GuidedAction action) {
        return false;
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        Toast.makeText(this, action.getTitle(), Toast.LENGTH_LONG).show();
    }
}
