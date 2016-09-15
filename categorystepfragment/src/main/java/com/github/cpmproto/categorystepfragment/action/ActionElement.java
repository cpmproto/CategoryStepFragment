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

public class ActionElement {
    private int id;
    private String description;
    private String subDescription;
    private String imageUrl;
    private Object object;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSubDescription() {
        return subDescription;
    }

    public void setSubDescription(String subDescription) {
        this.subDescription = subDescription;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public ActionElement() {
        this.id = 0;
        this.description = "";
        this.subDescription = "";
        this.imageUrl = "";
    }

    public ActionElement(int id, String description, String subDescription, String imageUrl, Object object) {
        this.id = id;
        this.description = description;
        this.subDescription = subDescription;
        this.imageUrl = imageUrl;
        this.object = object;
    }

    public ActionElement(int id, String description, String subDescription, String imageUrl) {
        this.id = id;
        this.description = description;
        this.subDescription = subDescription;
        this.imageUrl = imageUrl;
    }
}
