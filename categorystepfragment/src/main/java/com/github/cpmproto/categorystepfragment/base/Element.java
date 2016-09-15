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

package com.github.cpmproto.categorystepfragment.base;

import android.app.Fragment;

public class Element extends Fragment {

    protected static final boolean DEBUG = false;

    protected static final String TAG_LEAN_BACK_ACTIONS_FRAGMENT = "leanBackGuidedStepListFragment";
    protected static final String EXTRA_ACTION_SELECTED_INDEX = "selectedIndex";
    protected static final String EXTRA_ACTION_PREFIX = "action_";
    protected static final String EXTRA_BUTTON_ACTION_PREFIX = "buttonaction_";
    protected static final String TAG = "GuidedStepListFragment";
    protected static final String ENTRY_NAME_REPLACE = "GuidedStepDefault";
    protected static final boolean IS_FRAMEWORK_FRAGMENT = true;
}
