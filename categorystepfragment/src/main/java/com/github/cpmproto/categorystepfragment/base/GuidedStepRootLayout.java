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

import android.content.Context;
import android.support.v17.leanback.widget.Util;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

class GuidedStepRootLayout extends LinearLayout {

    private boolean mFocusOutStart = false;
    private boolean mFocusOutEnd = false;
    private boolean isSubCategory = false;
    private OnKeyPress onKeyPress;

    public GuidedStepRootLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GuidedStepRootLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setFocusOutStart(boolean focusOutStart) {
        mFocusOutStart = focusOutStart;
    }

    public void setFocusOutEnd(boolean focusOutEnd) {
        mFocusOutEnd = focusOutEnd;
    }

    public void setOnKeyPress(OnKeyPress onKeyPress) {
        this.onKeyPress = onKeyPress;
    }

    public void setOnSubcategoryfocus(boolean isOnSubCategory) {
        this.isSubCategory = isOnSubCategory;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() >= 1)
            return super.dispatchKeyEvent(event);

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                if (!isSubCategory)
                    onKeyPress.onKeyPressLeft();
                else
                    onKeyPress.onKeyPressBack(isFocused());

                event.startTracking();
                return true;
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    public View focusSearch(View focused, int direction) {

        if (direction == FOCUS_LEFT) {
            onKeyPress.onKeyPressLeft();
        }

        View newFocus = super.focusSearch(focused, direction);

        if (direction == FOCUS_LEFT || direction == FOCUS_RIGHT) {
            if (Util.isDescendant(this, newFocus)) {
                return newFocus;
            }
            if (getLayoutDirection() == ViewGroup.LAYOUT_DIRECTION_LTR ?
                    direction == FOCUS_LEFT : direction == FOCUS_RIGHT) {
                if (!mFocusOutStart) {
                    return focused;
                }
            } else {
                if (!mFocusOutEnd) {
                    return focused;
                }
            }
        }
        return newFocus;
    }
}
