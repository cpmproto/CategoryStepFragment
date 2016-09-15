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

package com.github.cpmproto.categorystepfragment.action;

import android.support.v17.leanback.widget.GuidedAction;

import java.util.ArrayList;
import java.util.List;

public class GuidedActionList {
    private String key;
    private List<GuidedAction> actions;

    public List<GuidedAction> getActions() {
        return actions;
    }

    public void setActions(List<GuidedAction> actions) {
        this.actions = actions;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public GuidedActionList(String key, List<GuidedAction> actions) {
        this.key = key;
        this.actions = actions;
    }

    public GuidedActionList() {
        this.key = "";
        this.actions = new ArrayList<>();
    }
}
