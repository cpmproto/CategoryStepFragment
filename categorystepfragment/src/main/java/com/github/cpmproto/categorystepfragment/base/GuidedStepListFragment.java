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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.transition.TransitionHelper;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v17.leanback.widget.GuidedActionAdapter;
import android.support.v17.leanback.widget.GuidedActionAdapterGroup;
import android.support.v17.leanback.widget.GuidedActionsStylist;
import android.support.v17.leanback.widget.ViewHolderTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.cpmproto.categorystepfragment.R;
import com.github.cpmproto.categorystepfragment.action.GuidedActionList;

import java.util.ArrayList;
import java.util.List;

public class GuidedStepListFragment extends Element implements GuidedActionAdapter.FocusListener, OnKeyPress {
    private static final int FIRT_ELEMENT = 0;
    /**
     * Fragment argument name for UI style.  The argument value is persisted in fragment state and
     * used to select fragment transition. The value is initially {@link #UI_STYLE_ENTRANCE} and
     * might be changed in one of the three helper functions:
     * <ul>
     * <li>{@link #addAsRoot(Activity, GuidedStepListFragment, int)} sets to
     * {@link #UI_STYLE_ACTIVITY_ROOT}</li>
     * <li>{@link #add(FragmentManager, GuidedStepListFragment)} or {@link #add(FragmentManager,
     * GuidedStepListFragment, int)} sets it to {@link #UI_STYLE_REPLACE} if there is already a
     * GuidedStepListFragment on stack.</li>
     * <li>{@link #finishGuidedStepListFragments()} changes current GuidedStepListFragment to
     * {@link #UI_STYLE_ENTRANCE} for the non activity case.  This is a special case that changes
     * the transition settings after fragment has been created,  in order to force current
     * GuidedStepListFragment run a return transition of {@link #UI_STYLE_ENTRANCE}</li>
     * </ul>
     * <p/>
     * Argument value can be either:
     * <ul>
     * <li>{@link #UI_STYLE_REPLACE}</li>
     * <li>{@link #UI_STYLE_ENTRANCE}</li>
     * <li>{@link #UI_STYLE_ACTIVITY_ROOT}</li>
     * </ul>
     */
    public static final String EXTRA_UI_STYLE = "uiStyle";

    /**
     * This is the case that we use GuidedStepListFragment to replace another existing
     * GuidedStepListFragment when moving forward to next step. Default behavior of this style is:
     * <ul>
     * <li>Enter transition slides in from END(right), exit transition same as
     * {@link #UI_STYLE_ENTRANCE}.
     * </li>
     * </ul>
     */
    public static final int UI_STYLE_REPLACE = 0;

    /**
     * Default value for argument {@link #EXTRA_UI_STYLE}. The default value is assigned in
     * GuidedStepListFragment constructor. This is the case that we show GuidedStepListFragment on top of
     * other content. The default behavior of this style:
     * <ul>
     * <li>Enter transition slides in from two sides, exit transition slide out to START(left).
     * Background will be faded in. Note: Changing exit transition by UI style is not working
     * because fragment transition asks for exit transition before UI style is restored in Fragment
     * .onCreate().</li>
     * </ul>
     * When popping multiple GuidedStepListFragment, {@link #finishGuidedStepListFragments()} also changes
     * the top GuidedStepListFragment to UI_STYLE_ENTRANCE in order to run the return transition
     * (reverse of enter transition) of UI_STYLE_ENTRANCE.
     */
    public static final int UI_STYLE_ENTRANCE = 1;

    /**
     * Animation to slide the contents from the side (left/right).
     *
     * @hide
     */
    public static final int SLIDE_FROM_SIDE = 0;

    /**
     * Find GuidedAction by Id.
     *
     * @param id Id of the action to search.
     * @return GuidedAction object or null if not found.
     */
    public GuidedAction findActionById(long id, String key) {
        int index = findActionPositionById(id, key);
        return index >= 0 ? getActionsByKey(key).get(index) : null;
    }

    /**
     * Notify an action has changed and update its UI.
     *
     * @param position Position of the GuidedAction in array.
     */
    public void notifyActionChanged(int position) {
        if (mAdapter != null) {
            mAdapter.notifyItemChanged(position);
        }
    }

    /**
     * Find GuidedAction position in array by Id.
     *
     * @param id Id of the action to search.
     * @return position of GuidedAction object in array or -1 if not found.
     */
    public int findActionPositionById(long id, String key) {
        List<GuidedAction> guidedActionLists = getActionsByKey(key);

        if (guidedActionLists != null) {
            for (int i = 0; i < guidedActionLists.size(); i++) {
                GuidedAction action = guidedActionLists.get(i);
                if (action.getId() == id) {
                    return i;
                }
            }
        }
        return -1;
    }


    public static final int UI_STYLE_ACTIVITY_ROOT = 2;

    private ContextThemeWrapper mThemeWrapper;
    private GuidanceStylist mGuidanceStylist;
    private GuidedActionsStylist mActionsStylist;
    private GuidedActionsStylist mButtonActionsStylist;
    private int entranceTransitionType = SLIDE_FROM_SIDE;
    private GuidedActionAdapter mAdapter;
    private List<GuidedActionList> mActions = new ArrayList<GuidedActionList>();
    private GuidedActionAdapter mSubAdapter;
    private List<GuidedAction> mButtonActions = new ArrayList<GuidedAction>();
    private GuidedActionAdapter mButtonAdapter;
    private GuidedActionAdapterGroup mAdapterGroup;
    private int mSelectedIndex = -1;
    private GuidedStepRootLayout rootLayout;

    public GuidedStepListFragment() {
        mGuidanceStylist = onCreateGuidanceStylist();
        mActionsStylist = onCreateActionsStylist();
        mButtonActionsStylist = onCreateButtonActionsStylist();
        onProvideFragmentTransitions();
    }

    public GuidedActionsStylist onCreateActionsStylist() {
        return new GuidedActionsStylist();
    }

    public GuidedActionsStylist onCreateButtonActionsStylist() {
        GuidedActionsStylist stylist = new GuidedActionsStylist();
        stylist.setAsButtonActions();
        return stylist;
    }

    /**
     * Creates the presenter used to style the guidance panel. The default implementation returns
     * a basic GuidanceStylist.
     *
     * @return The GuidanceStylist used in this fragment.
     */
    public GuidanceStylist onCreateGuidanceStylist() {
        return new GuidanceStylist();
    }


    /**
     * Read UI style from fragment arguments.  Default value is {@link #UI_STYLE_ENTRANCE} when
     * fragment is first initialized.  UI style is used to choose different fragment transition
     * animations and determine if this is the first GuidedStepListFragment on backstack.
     *
     * @return {@link #UI_STYLE_ACTIVITY_ROOT} {@link #UI_STYLE_REPLACE} or
     * {@link #UI_STYLE_ENTRANCE}.
     * @see #onProvideFragmentTransitions()
     */
    public int getUiStyle() {
        Bundle b = getArguments();
        if (b == null) return UI_STYLE_ENTRANCE;
        return b.getInt(EXTRA_UI_STYLE, UI_STYLE_ENTRANCE);
    }

    protected String firstElementKeys="";

    /**
     * Called by Constructor to provide fragment transitions.  The default implementation assigns
     * transitions based on {@link #getUiStyle()}:
     * <ul>
     * <li> {@link #UI_STYLE_REPLACE} Slide from/to end(right) for enter transition, slide from/to
     * start(left) for exit transition, shared element enter transition is set to ChangeBounds.
     * <li> {@link #UI_STYLE_ENTRANCE} Enter transition is set to slide from both sides, exit
     * transition is same as {@link #UI_STYLE_REPLACE}, no shared element enter transition.
     * <li> {@link #UI_STYLE_ACTIVITY_ROOT} Enter transition is set to null and app should rely on
     * activity transition, exit transition is same as {@link #UI_STYLE_REPLACE}, no shared element
     * enter transition.
     * </ul>
     * <p/>
     * The default implementation heavily relies on {@link GuidedActionsStylist} and
     * {@link GuidanceStylist} layout, app may override this method when modifying the default
     * layout of {@link GuidedActionsStylist} or {@link GuidanceStylist}.
     * <p/>
     * TIP: because the fragment view is removed during fragment transition, in general app cannot
     * use two Visibility transition together. Workaround is to create your own Visibility
     * transition that controls multiple animators (e.g. slide and fade animation in one Transition
     * class).
     */
    protected void onProvideFragmentTransitions() {
        if (Build.VERSION.SDK_INT >= 21) {
            final int uiStyle = getUiStyle();
            if (uiStyle == UI_STYLE_REPLACE) {
                Object enterTransition = TransitionHelper.createFadeAndShortSlide(Gravity.END);
                TransitionHelper.exclude(enterTransition, android.support.v17.leanback.R.id.guidedstep_background, true);
                TransitionHelper.exclude(enterTransition, android.support.v17.leanback.R.id.guidedactions_sub_list_background,
                        true);
                TransitionHelper.setEnterTransition(this, enterTransition);

                Object fade = TransitionHelper.createFadeTransition(TransitionHelper.FADE_IN |
                        TransitionHelper.FADE_OUT);
                TransitionHelper.include(fade, android.support.v17.leanback.R.id.guidedactions_sub_list_background);
                Object changeBounds = TransitionHelper.createChangeBounds(false);
                Object sharedElementTransition = TransitionHelper.createTransitionSet(false);
                TransitionHelper.addTransition(sharedElementTransition, fade);
                TransitionHelper.addTransition(sharedElementTransition, changeBounds);
                TransitionHelper.setSharedElementEnterTransition(this, sharedElementTransition);
            } else if (uiStyle == UI_STYLE_ENTRANCE) {
                if (entranceTransitionType == SLIDE_FROM_SIDE) {
                    Object fade = TransitionHelper.createFadeTransition(TransitionHelper.FADE_IN |
                            TransitionHelper.FADE_OUT);
                    TransitionHelper.include(fade, android.support.v17.leanback.R.id.guidedstep_background);
                    Object slideFromSide = TransitionHelper.createFadeAndShortSlide(Gravity.END |
                            Gravity.START);
                    //TransitionHelper.include(slideFromSide, android.support.v17.leanback.R.id.content_fragment);
                    TransitionHelper.include(slideFromSide, android.support.v17.leanback.R.id.action_fragment_root);
                    Object enterTransition = TransitionHelper.createTransitionSet(false);
                    TransitionHelper.addTransition(enterTransition, fade);
                    TransitionHelper.addTransition(enterTransition, slideFromSide);
                    TransitionHelper.setEnterTransition(this, enterTransition);
                } else {
                    Object slideFromBottom = TransitionHelper.createFadeAndShortSlide(
                            Gravity.BOTTOM);
                    TransitionHelper.include(slideFromBottom, android.support.v17.leanback.R.id.guidedstep_background_view_root);
                    Object enterTransition = TransitionHelper.createTransitionSet(false);
                    TransitionHelper.addTransition(enterTransition, slideFromBottom);
                    TransitionHelper.setEnterTransition(this, enterTransition);
                }
                // No shared element transition
                TransitionHelper.setSharedElementEnterTransition(this, null);
            } else if (uiStyle == UI_STYLE_ACTIVITY_ROOT) {
                // for Activity root, we don't need enter transition, use activity transition
                TransitionHelper.setEnterTransition(this, null);
                // No shared element transition
                TransitionHelper.setSharedElementEnterTransition(this, null);
            }
            // exitTransition is same for all style
            Object exitTransition = TransitionHelper.createFadeAndShortSlide(Gravity.START);
            TransitionHelper.exclude(exitTransition, android.support.v17.leanback.R.id.guidedstep_background, true);
            TransitionHelper.exclude(exitTransition, android.support.v17.leanback.R.id.guidedactions_sub_list_background,
                    true);
            TransitionHelper.setExitTransition(this, exitTransition);
        }
    }

    /**
     * Adds the specified GuidedStepListFragment as content of Activity; no backstack entry is added so
     * the activity will be dismissed when BACK key is pressed.  The method is typically called in
     * Activity.onCreate() when savedInstanceState is null.  When savedInstanceState is not null,
     * the Activity is being restored,  do not call addAsRoot() to duplicate the Fragment restored
     * by FragmentManager.
     * {@link #UI_STYLE_ACTIVITY_ROOT} is assigned.
     * <p/>
     * Note: currently fragments added using this method must be created programmatically rather
     * than via XML.
     *
     * @param activity The Activity to be used to insert GuidedStepListFragment.
     * @param fragment The GuidedStepListFragment to be inserted into the fragment stack.
     * @param id       The id of container to add GuidedStepListFragment, can be android.R.id.content.
     * @return The ID returned by the call FragmentTransaction.commit, or -1 there is already
     * GuidedStepListFragment.
     */
    public static int addAsRoot(Activity activity, GuidedStepListFragment fragment, int id) {
        // Workaround b/23764120: call getDecorView() to force requestFeature of ActivityTransition.
        activity.getWindow().getDecorView();
        FragmentManager fragmentManager = activity.getFragmentManager();
        if (fragmentManager.findFragmentByTag(TAG_LEAN_BACK_ACTIONS_FRAGMENT) != null) {
            Log.w(TAG, "Fragment is already exists, likely calling " +
                    "addAsRoot() when savedInstanceState is not null in Activity.onCreate().");
            return -1;
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        fragment.setUiStyle(UI_STYLE_ACTIVITY_ROOT);
        return ft.replace(id, fragment, TAG_LEAN_BACK_ACTIONS_FRAGMENT).commit();
    }


    /**
     * Convenient method to close GuidedStepListFragments on top of other content or finish Activity if
     * GuidedStepListFragments were started in a separate activity.  Pops all stack entries including
     * {@link #UI_STYLE_ENTRANCE}; if {@link #UI_STYLE_ENTRANCE} is not found, finish the activity.
     * Note that this method must be paired with {@link #add(FragmentManager, GuidedStepListFragment,
     * int)} which sets up the stack entry name for finding which fragment we need to pop back to.
     */
    public void finishGuidedStepListFragments() {
        final FragmentManager fragmentManager = getFragmentManager();
        final int entryCount = fragmentManager.getBackStackEntryCount();
        if (entryCount > 0) {
            for (int i = entryCount - 1; i >= 0; i--) {
                FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(i);
                if (isStackEntryUiStyleEntrance(entry.getName())) {
                    GuidedStepListFragment top = getCurrentGuidedStepListFragment(fragmentManager);
                    if (top != null) {
                        top.setUiStyle(UI_STYLE_ENTRANCE);
                    }
                    fragmentManager.popBackStack(entry.getId(),
                            FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    return;
                }
            }
        }
        ActivityCompat.finishAfterTransition(getActivity());
    }

    private static final String ENTRY_NAME_ENTRANCE = "GuidedStepEntrance";

    /**
     * Returns true if the backstack entry represents GuidedStepListFragment with
     * {@link #UI_STYLE_ENTRANCE}, i.e. this is the first GuidedStepListFragment pushed to stack; false
     * otherwise.
     *
     * @param backStackEntryName Name of BackStackEntry.
     * @return True if the backstack represents GuidedStepListFragment with {@link #UI_STYLE_ENTRANCE};
     * false otherwise.
     * @see #generateStackEntryName(int, Class)
     */
    static boolean isStackEntryUiStyleEntrance(String backStackEntryName) {
        return backStackEntryName != null && backStackEntryName.startsWith(ENTRY_NAME_ENTRANCE);
    }


    /**
     * Returns BackStackEntry name for the GuidedStepListFragment or empty String if no entry is
     * associated.  Note {@link #UI_STYLE_ACTIVITY_ROOT} will return empty String.  The method
     * returns undefined value if the fragment is not in FragmentManager.
     *
     * @return BackStackEntry name for the GuidedStepListFragment or empty String if no entry is
     * associated.
     */
    String generateStackEntryName() {
        return generateStackEntryName(getUiStyle(), getClass());
    }

    /**
     * Generates BackStackEntry name for GuidedStepListFragment class or empty String if no entry is
     * associated.  Note {@link #UI_STYLE_ACTIVITY_ROOT} is not allowed and returns empty String.
     *
     * @param uiStyle {@link #UI_STYLE_REPLACE} or {@link #UI_STYLE_ENTRANCE}
     * @return BackStackEntry name for the GuidedStepListFragment or empty String if no entry is
     * associated.
     */
    static String generateStackEntryName(int uiStyle, Class GuidedStepListFragmentClass) {
        if (!GuidedStepListFragment.class.isAssignableFrom(GuidedStepListFragmentClass)) {
            return "";
        }
        switch (uiStyle) {
            case UI_STYLE_REPLACE:
                return ENTRY_NAME_REPLACE + GuidedStepListFragmentClass.getName();
            case UI_STYLE_ENTRANCE:
                return ENTRY_NAME_ENTRANCE + GuidedStepListFragmentClass.getName();
            case UI_STYLE_ACTIVITY_ROOT:
            default:
                return "";
        }
    }

    /**
     * Adds the specified GuidedStepListFragment to the fragment stack, replacing any existing
     * GuidedStepListFragments in the stack, and configuring the fragment-to-fragment custom
     * transitions.  A backstack entry is added, so the fragment will be dismissed when BACK key
     * is pressed.
     * <li>If current fragment on stack is GuidedStepListFragment: assign {@link #UI_STYLE_REPLACE}
     * <li>If current fragment on stack is not GuidedStepListFragment: assign {@link #UI_STYLE_ENTRANCE}
     * <p/>
     * Note: currently fragments added using this method must be created programmatically rather
     * than via XML.
     *
     * @param fragmentManager The FragmentManager to be used in the transaction.
     * @param fragment        The GuidedStepListFragment to be inserted into the fragment stack.
     * @return The ID returned by the call FragmentTransaction.commit.
     */
    public static int add(FragmentManager fragmentManager, GuidedStepListFragment fragment) {
        return add(fragmentManager, fragment, android.R.id.content);
    }

    /**
     * Adds the specified GuidedStepListFragment to the fragment stack, replacing any existing
     * GuidedStepListFragments in the stack, and configuring the fragment-to-fragment custom
     * transitions.  A backstack entry is added, so the fragment will be dismissed when BACK key
     * is pressed.
     * <li>If current fragment on stack is GuidedStepListFragment: assign {@link #UI_STYLE_REPLACE} and
     * {@link #onAddSharedElementTransition(FragmentTransaction, GuidedStepListFragment)} will be called
     * to perform shared element transition between GuidedStepListFragments.
     * <li>If current fragment on stack is not GuidedStepListFragment: assign {@link #UI_STYLE_ENTRANCE}
     * <p/>
     * Note: currently fragments added using this method must be created programmatically rather
     * than via XML.
     *
     * @param fragmentManager The FragmentManager to be used in the transaction.
     * @param fragment        The GuidedStepListFragment to be inserted into the fragment stack.
     * @param id              The id of container to add GuidedStepListFragment, can be android.R.id.content.
     * @return The ID returned by the call FragmentTransaction.commit.
     */
    public static int add(FragmentManager fragmentManager, GuidedStepListFragment fragment, int id) {
        GuidedStepListFragment current = getCurrentGuidedStepListFragment(fragmentManager);
        boolean inGuidedStep = current != null;
        if (IS_FRAMEWORK_FRAGMENT && Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT < 23
                && !inGuidedStep) {
            // workaround b/22631964 for framework fragment
            fragmentManager.beginTransaction()
                    .replace(id, new DummyFragment(), TAG_LEAN_BACK_ACTIONS_FRAGMENT)
                    .commit();
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();

        fragment.setUiStyle(inGuidedStep ? UI_STYLE_REPLACE : UI_STYLE_ENTRANCE);
        ft.addToBackStack(fragment.generateStackEntryName());
        if (current != null) {
            fragment.onAddSharedElementTransition(ft, current);
        }
        return ft.replace(id, fragment, TAG_LEAN_BACK_ACTIONS_FRAGMENT).commit();
    }

    @Override
    public void onKeyPressLeft() {
        try {
            final View conteintView = getView();

            conteintView.animate()
                    .translationX(-conteintView.getWidth())
                    .setDuration(800)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            conteintView.setVisibility(View.GONE);
                        }
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onKeyPressBack(boolean isSubcategoryFocused) {
        mActionsStylist.setExpandedViewHolder(null);
        rootLayout.setOnSubcategoryfocus(false);
    }

    /**
     * @hide
     */
    public static class DummyFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View v = new View(inflater.getContext());
            v.setVisibility(View.GONE);
            return v;
        }
    }

    /**
     * Called when this fragment is added to FragmentTransaction with {@link #UI_STYLE_REPLACE} (aka
     * when the GuidedStepListFragment replacing an existing GuidedStepListFragment). Default implementation
     * establishes connections between action background views to morph action background bounds
     * change from disappearing GuidedStepListFragment into this GuidedStepListFragment. The default
     * implementation heavily relies on {@link GuidedActionsStylist}'s layout, app may override this
     * method when modifying the default layout of {@link GuidedActionsStylist}.
     *
     * @param ft           The FragmentTransaction to add shared element.
     * @param disappearing The disappearing fragment.
     * @see GuidedActionsStylist
     * @see #onProvideFragmentTransitions()
     */
    protected void onAddSharedElementTransition(FragmentTransaction ft, GuidedStepListFragment
            disappearing) {
        View fragmentView = disappearing.getView();
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(
                android.support.v17.leanback.R.id.action_fragment_root), "action_fragment_root");
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(
                android.support.v17.leanback.R.id.action_fragment_background), "action_fragment_background");
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(
                android.support.v17.leanback.R.id.action_fragment), "action_fragment");
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(
                android.support.v17.leanback.R.id.guidedactions_root), "guidedactions_root");
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(
                android.support.v17.leanback.R.id.guidedactions_content), "guidedactions_content");
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(
                android.support.v17.leanback.R.id.guidedactions_list_background), "guidedactions_list_background");
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(
                android.support.v17.leanback.R.id.guidedactions_root2), "guidedactions_root2");
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(
                android.support.v17.leanback.R.id.guidedactions_content2), "guidedactions_content2");
        addNonNullSharedElementTransition(ft, fragmentView.findViewById(
                android.support.v17.leanback.R.id.guidedactions_list_background2), "guidedactions_list_background2");
    }

    private static void addNonNullSharedElementTransition(FragmentTransaction ft, View subView,
                                                          String transitionName) {
        if (subView != null)
            TransitionHelper.addSharedElement(ft, subView, transitionName);
    }

    /**
     * Set UI style to fragment arguments. Default value is {@link #UI_STYLE_ENTRANCE} when fragment
     * is first initialized. UI style is used to choose different fragment transition animations and
     * determine if this is the first GuidedStepListFragment on backstack. In most cases app does not
     * directly call this method, app calls helper function
     * {@link #add(FragmentManager, GuidedStepListFragment, int)}. However if the app creates Fragment
     * transaction and controls backstack by itself, it would need call setUiStyle() to select the
     * fragment transition to use.
     *
     * @param style {@link #UI_STYLE_ACTIVITY_ROOT} {@link #UI_STYLE_REPLACE} or
     *              {@link #UI_STYLE_ENTRANCE}.
     */
    public void setUiStyle(int style) {
        int oldStyle = getUiStyle();
        Bundle arguments = getArguments();
        boolean isNew = false;
        if (arguments == null) {
            arguments = new Bundle();
            isNew = true;
        }
        arguments.putInt(EXTRA_UI_STYLE, style);
        // call setArgument() will validate if the fragment is already added.
        if (isNew) {
            setArguments(arguments);
        }
        if (style != oldStyle) {
            onProvideFragmentTransitions();
        }
    }


    /**
     * Returns the theme used for styling the fragment. The default returns -1, indicating that the
     * host Activity's theme should be used.
     *
     * @return The theme resource ID of the theme to use in this fragment, or -1 to use the
     * host Activity's theme.
     */
    public int onProvideTheme() {
        return -1;
    }

    private void resolveTheme() {
        // Look up the guidedStepTheme in the currently specified theme.  If it exists,
        // replace the theme with its value.
        Activity activity = getActivity();
        int theme = onProvideTheme();
        if (theme == -1 && !isGuidedStepTheme(activity)) {
            // Look up the guidedStepTheme in the activity's currently specified theme.  If it
            // exists, replace the theme with its value.
            int resId = android.support.v17.leanback.R.attr.guidedStepTheme;
            TypedValue typedValue = new TypedValue();
            boolean found = activity.getTheme().resolveAttribute(resId, typedValue, true);
            if (DEBUG) Log.v(TAG, "Found guided step theme reference? " + found);
            if (found) {
                ContextThemeWrapper themeWrapper =
                        new ContextThemeWrapper(activity, typedValue.resourceId);
                if (isGuidedStepTheme(themeWrapper)) {
                    mThemeWrapper = themeWrapper;
                } else {
                    found = false;
                    mThemeWrapper = null;
                }
            }
            if (!found) {
                Log.e(TAG, "GuidedStepListFragment does not have an appropriate theme set.");
            }
        } else if (theme != -1) {
            mThemeWrapper = new ContextThemeWrapper(activity, theme);
        }
    }

    private static boolean isGuidedStepTheme(Context context) {
        int resId = android.support.v17.leanback.R.attr.guidedStepThemeFlag;
        TypedValue typedValue = new TypedValue();
        boolean found = context.getTheme().resolveAttribute(resId, typedValue, true);
        if (DEBUG) Log.v(TAG, "Found guided step theme flag? " + found);
        return found && typedValue.type == TypedValue.TYPE_INT_BOOLEAN && typedValue.data != 0;
    }

    private LayoutInflater getThemeInflater(LayoutInflater inflater) {
        if (mThemeWrapper == null) {
            return inflater;
        } else {
            return inflater.cloneInContext(mThemeWrapper);
        }
    }

    /**
     * Returns true if allows focus out of start edge of GuidedStepListFragment, false otherwise.
     * Default value is false, the reason is to disable FocusFinder to find focusable views
     * beneath content of GuidedStepListFragment.  Subclass may override.
     *
     * @return True if allows focus out of start edge of GuidedStepListFragment.
     */
    public boolean isFocusOutStartAllowed() {
        return false;
    }

    /**
     * Returns true if allows focus out of end edge of GuidedStepListFragment, false otherwise.
     * Default value is false, the reason is to disable FocusFinder to find focusable views
     * beneath content of GuidedStepListFragment.  Subclass may override.
     *
     * @return True if allows focus out of end edge of GuidedStepListFragment.
     */
    public boolean isFocusOutEndAllowed() {
        return false;
    }

    /**
     * @return True if the sub actions list is expanded, false otherwise.
     */
    public boolean isSubActionsExpanded() {
        return mActionsStylist.isSubActionsExpanded();
    }

    /**
     * Collapse sub actions list.
     *
     * @see GuidedAction#getSubActions()
     */
    public void collapseSubActions() {
        mActionsStylist.setExpandedViewHolder(null);
    }


    /**
     * Expand a given action's sub actions list.
     *
     * @param action GuidedAction to expand.
     * @see GuidedAction#getSubActions()
     */
    public void expandSubActions(GuidedAction action) {
        //final int actionPosition = mActions.indexOf(action);
        final int actionPosition = mActions.get(FIRT_ELEMENT).getActions().indexOf(action);
        if (actionPosition < FIRT_ELEMENT) {
            return;
        }
        mActionsStylist.getActionsGridView().setSelectedPositionSmooth(actionPosition,
                new ViewHolderTask() {
                    @Override
                    public void run(RecyclerView.ViewHolder vh) {
                        rootLayout.setOnSubcategoryfocus(true);
                        GuidedActionsStylist.ViewHolder avh = (GuidedActionsStylist.ViewHolder) vh;
                        mActionsStylist.setExpandedViewHolder(avh);
                    }
                });
    }


    /**
     * Callback invoked when an action in sub actions is taken by the user. Subclasses should
     * override in order to act on the user's decisions.  Default return value is true to close
     * the sub actions list.
     *
     * @param action The chosen action.
     * @return true to collapse the sub actions list, false to keep it expanded.
     */
    public boolean onSubGuidedActionClicked(GuidedAction action) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (DEBUG) Log.v(TAG, "onCreateView");

        resolveTheme();
        inflater = getThemeInflater(inflater);

        rootLayout = (GuidedStepRootLayout) inflater.inflate(R.layout.lb_guidedstep_fragment, container, false);

        rootLayout.setFocusOutStart(isFocusOutStartAllowed());
        rootLayout.setFocusOutEnd(isFocusOutEndAllowed());
        rootLayout.setBackgroundColor(Color.TRANSPARENT);
        rootLayout.setOnKeyPress(this);

        ViewGroup guidanceContainer = (ViewGroup) rootLayout.findViewById(R.id.content_fragment);
        guidanceContainer.setVisibility(View.INVISIBLE);

        ViewGroup actionContainer = (ViewGroup) rootLayout.findViewById(R.id.action_fragment);

        View actionsView = mActionsStylist.onCreateView(inflater, actionContainer);
        actionContainer.addView(actionsView);

        View buttonActionsView = mButtonActionsStylist.onCreateView(inflater, actionContainer);
        actionContainer.addView(buttonActionsView);

        GuidedActionAdapter.EditListener editListener = new GuidedActionAdapter.EditListener() {

            @Override
            public void onImeOpen() {
                runImeAnimations(true);
            }

            @Override
            public void onImeClose() {
                runImeAnimations(false);
            }

            @Override
            public long onGuidedActionEditedAndProceed(GuidedAction action) {
                return GuidedStepListFragment.this.onGuidedActionEditedAndProceed(action);
            }

            @Override
            public void onGuidedActionEditCanceled(GuidedAction action) {
                GuidedStepListFragment.this.onGuidedActionEditCanceled(action);
            }
        };

        mAdapter = new GuidedActionAdapter(getActionsByKey(), new GuidedActionAdapter.ClickListener() {
            @Override
            public void onGuidedActionClicked(GuidedAction action) {
                GuidedStepListFragment.this.onGuidedActionClicked(action);
                if (isSubActionsExpanded()) {
                    collapseSubActions();
                } else if (action.hasSubActions()) {
                    expandSubActions(action);
                }
            }
        }, this, mActionsStylist, false);

        mButtonAdapter = new GuidedActionAdapter(mButtonActions, new GuidedActionAdapter.ClickListener() {
            @Override
            public void onGuidedActionClicked(GuidedAction action) {
                GuidedStepListFragment.this.onGuidedActionClicked(action);
            }
        }, this, mButtonActionsStylist, false);

        mSubAdapter = new GuidedActionAdapter(null, new GuidedActionAdapter.ClickListener() {
            @Override
            public void onGuidedActionClicked(GuidedAction action) {
                if (mActionsStylist.isInExpandTransition()) {
                    return;
                }
                if (GuidedStepListFragment.this.onSubGuidedActionClicked(action)) {
                    collapseSubActions();
                }
                rootLayout.setOnSubcategoryfocus(false);
                setActions(mActions, action.toString());
            }
        }, this, mActionsStylist, true);

        mAdapterGroup = new GuidedActionAdapterGroup();
        mAdapterGroup.addAdpter(mAdapter, mButtonAdapter);
        mAdapterGroup.addAdpter(mSubAdapter, null);
        mAdapterGroup.setEditListener(editListener);
        mActionsStylist.setEditListener(editListener);

        mActionsStylist.getActionsGridView().setAdapter(mAdapter);
        if (mActionsStylist.getSubActionsGridView() != null) {
            mActionsStylist.getSubActionsGridView().setAdapter(mSubAdapter);
        }
        mButtonActionsStylist.getActionsGridView().setAdapter(mButtonAdapter);
        if (mButtonActions.size() == 0) {
            // when there is no button actions, we don't need show the second panel, but keep
            // the width zero to run ChangeBounds transition.
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)
                    buttonActionsView.getLayoutParams();
            lp.weight = 0;
            buttonActionsView.setLayoutParams(lp);
        } else {
            // when there are two actions panel, we need adjust the weight of action to
            // guidedActionContentWidthWeightTwoPanels.
            Context ctx = mThemeWrapper != null ? mThemeWrapper : getActivity();
            TypedValue typedValue = new TypedValue();
            if (ctx.getTheme().resolveAttribute(R.attr.guidedActionContentWidthWeightTwoPanels,
                    typedValue, true)) {
                View actionsRoot = rootLayout.findViewById(R.id.action_fragment_root);
                float weight = typedValue.getFloat();
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) actionsRoot
                        .getLayoutParams();
                lp.weight = weight;
                actionsRoot.setLayoutParams(lp);
            }
        }

        int pos = (mSelectedIndex >= 0 && mSelectedIndex < mActions.size()) ? mSelectedIndex : getFirstCheckedAction();
        setSelectedActionPosition(pos);
        setSelectedButtonActionPosition(0);

        return rootLayout;
    }

    private int getFirstCheckedAction() {
        for (GuidedActionList guidedActionList : mActions) {
            if (guidedActionList.getKey().equals(firstElementKeys)) {
                List<GuidedAction> guidedAction = getActionsByKey(guidedActionList.getKey());

                for (int i = 0, size = guidedAction.size(); i < size; i++) {
                    if (guidedAction.get(i).isChecked())
                        return i;
                }
            }
        }
        return 0;
    }

    /**
     * Returns the current GuidedStepListFragment on the fragment transaction stack.
     *
     * @return The current GuidedStepListFragment, if any, on the fragment transaction stack.
     */
    public static GuidedStepListFragment getCurrentGuidedStepListFragment(FragmentManager fm) {
        Fragment f = fm.findFragmentByTag(TAG_LEAN_BACK_ACTIONS_FRAGMENT);
        if (f instanceof GuidedStepListFragment) {
            return (GuidedStepListFragment) f;
        }
        return null;
    }

    /**
     * Called by onCreateView to inflate background view.  Default implementation loads view
     * from {@link android.support.v17.leanback.R.layout#lb_guidedstep_background} which holds a reference to
     * guidedStepBackground.
     *
     * @param inflater           LayoutInflater to load background view.
     * @param container          Parent view of background view.
     * @param savedInstanceState
     * @return Created background view or null if no background.
     */
    public View onCreateBackgroundView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        return inflater.inflate(android.support.v17.leanback.R.layout.lb_guidedstep_background, container, false);
    }

    /**
     * Scrolls the action list to the position indicated, selecting that button action's view.
     *
     * @param position The integer position of the button action of interest.
     */
    public void setSelectedButtonActionPosition(int position) {
        mButtonActionsStylist.getActionsGridView().setSelectedPosition(position);
    }

    /**
     * Scrolls the action list to the position indicated, selecting that action's view.
     *
     * @param position The integer position of the action of interest.
     */
    public void setSelectedActionPosition(int position) {
        mActionsStylist.getActionsGridView().setSelectedPosition(position);
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().findViewById(R.id.action_fragment).requestFocus();
    }

    @Override
    public void onGuidedActionFocused(GuidedAction action) {

    }

    /**
     * Returns the information required to provide guidance to the user. This hook is called during
     * {@link #onCreateView}.  May be overridden to return a custom subclass of {@link
     * GuidanceStylist.Guidance} for use in a subclass of {@link GuidanceStylist}. The default
     * returns a Guidance object with empty fields; subclasses should override.
     *
     * @param savedInstanceState The saved instance state from onCreateView.
     * @return The Guidance object representing the information used to guide the user.
     */
    public
    @NonNull
    GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance("", "", "", null);
    }

    private void runImeAnimations(boolean entering) {
        ArrayList<Animator> animators = new ArrayList<Animator>();
        if (entering) {
            mGuidanceStylist.onImeAppearing(animators);
            mActionsStylist.onImeAppearing(animators);
            mButtonActionsStylist.onImeAppearing(animators);
        } else {
            mGuidanceStylist.onImeDisappearing(animators);
            mActionsStylist.onImeDisappearing(animators);
            mButtonActionsStylist.onImeDisappearing(animators);
        }
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animators);
        set.start();
    }

    /**
     * Callback invoked when an action has been edited, for example when user clicks confirm button
     * in IME window.  Default implementation calls deprecated method
     * {@link #onGuidedActionEdited(GuidedAction)} and returns {@link GuidedAction#ACTION_ID_NEXT}.
     *
     * @param action The action that has been edited.
     * @return ID of the action will be focused or {@link GuidedAction#ACTION_ID_NEXT},
     * {@link GuidedAction#ACTION_ID_CURRENT}.
     */
    public long onGuidedActionEditedAndProceed(GuidedAction action) {
        onGuidedActionEdited(action);
        return GuidedAction.ACTION_ID_NEXT;
    }

    /**
     * Callback invoked when an action's title or description has been edited, this happens either
     * when user clicks confirm button in IME or user closes IME window by BACK key.
     *
     * @deprecated Override {@link #onGuidedActionEditedAndProceed(GuidedAction)} and/or
     * {@link #onGuidedActionEditCanceled(GuidedAction)}.
     */
    @Deprecated
    public void onGuidedActionEdited(GuidedAction action) {
    }


    /**
     * Callback invoked when an action has been canceled editing, for example when user closes
     * IME window by BACK key.  Default implementation calls deprecated method
     * {@link #onGuidedActionEdited(GuidedAction)}.
     *
     * @param action The action which has been canceled editing.
     */
    public void onGuidedActionEditCanceled(GuidedAction action) {
        onGuidedActionEdited(action);
    }

    /**
     * Callback invoked when an action is taken by the user. Subclasses should override in
     * order to act on the user's decisions.
     *
     * @param action The chosen action.
     */
    public void onGuidedActionClicked(GuidedAction action) {
    }


    /**
     * Fills out the set of actions available to the user. This hook is called during {@link
     * #onCreate}. The default leaves the list of actions empty; subclasses should override.
     *
     * @param actions            A non-null, empty list ready to be populated.
     * @param savedInstanceState The saved instance state from onCreate.
     */
    public void onCreateActions(@NonNull List<GuidedActionList> actions, Bundle savedInstanceState) {
    }

    final static boolean isSaveEnabled(GuidedAction action) {
        return action.isAutoSaveRestoreEnabled() && action.getId() != GuidedAction.NO_ID;
    }

    /**
     * Get the key will be used to save GuidedAction with Fragment.
     *
     * @param action GuidedAction to get key.
     * @return Key to save the GuidedAction.
     */
    final String getAutoRestoreKey(GuidedAction action) {
        return EXTRA_ACTION_PREFIX + action.getId();
    }

    /**
     * Get the key will be used to save GuidedAction with Fragment.
     *
     * @param action GuidedAction to get key.
     * @return Key to save the GuidedAction.
     */
    final String getButtonAutoRestoreKey(GuidedAction action) {
        return EXTRA_BUTTON_ACTION_PREFIX + action.getId();
    }

    final void onRestoreActions(List<GuidedActionList> actions, Bundle savedInstanceState) {
        //TODO:
        for (int i = 0, size = actions.size(); i < size; i++) {
            GuidedAction action = getActionsByKey("", actions).get(i);
            if (isSaveEnabled(action)) {
                action.onRestoreInstanceState(savedInstanceState, getAutoRestoreKey(action));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.v(TAG, "onCreate");
        // Set correct transition from saved arguments.
        onProvideFragmentTransitions();
        Bundle state = (savedInstanceState != null) ? savedInstanceState : getArguments();
        if (state != null) {
            if (mSelectedIndex == -1) {
                mSelectedIndex = state.getInt(EXTRA_ACTION_SELECTED_INDEX, -1);
            }
        }
        ArrayList<GuidedActionList> actions = new ArrayList<>();
        onCreateActions(actions, savedInstanceState);
        if (savedInstanceState != null) {
            onRestoreActions(actions, savedInstanceState);
        }
        setActions(actions, "");
        ArrayList<GuidedAction> buttonActions = new ArrayList<GuidedAction>();
        onCreateButtonActions(buttonActions, savedInstanceState);
        if (savedInstanceState != null) {
            onRestoreButtonActions(buttonActions, savedInstanceState);
        }
        setButtonActions(buttonActions);
    }

    /**
     * Sets the list of button GuidedActions that the user may take in this fragment.
     *
     * @param actions The list of button GuidedActions for this fragment.
     */
    public void setButtonActions(List<GuidedAction> actions) {
        mButtonActions = actions;
        if (mButtonAdapter != null) {
            mButtonAdapter.setActions(mButtonActions);
        }
    }

    final void onRestoreButtonActions(List<GuidedAction> actions, Bundle savedInstanceState) {
        for (int i = 0, size = actions.size(); i < size; i++) {
            GuidedAction action = actions.get(i);
            if (isSaveEnabled(action)) {
                action.onRestoreInstanceState(savedInstanceState, getButtonAutoRestoreKey(action));
            }
        }
    }

    /**
     * Sets the list of GuidedActions that the user may take in this fragment.
     *
     * @param actions The list of GuidedActions for this fragment.
     */
    public void setActions(List<GuidedActionList> actions, @Nullable String key) {
        mActions = actions;
        if (mAdapter != null) {
            mAdapter.setActions(getActionsByKey(key));
        }
    }

    /**
     * Fills out the set of actions shown at right available to the user. This hook is called during
     * {@link #onCreate}. The default leaves the list of actions empty; subclasses may override.
     *
     * @param actions            A non-null, empty list ready to be populated.
     * @param savedInstanceState The saved instance state from onCreate.
     */
    public void onCreateButtonActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
    }

    public List<GuidedAction> getActionsByKey() {
        if (mActions == null)
            return new ArrayList<>();

        if (mActions.size() <= 0)
            return new ArrayList<>();

        return mActions.get(FIRT_ELEMENT).getActions();
    }

    public List<GuidedAction> getActionsByKey(String keyFind) {
        if (mActions == null)
            return new ArrayList<>();

        if (mActions.size() <= 0)
            return new ArrayList<>();

        if (keyFind == null)
            return mActions.get(FIRT_ELEMENT).getActions();

        if (keyFind.isEmpty())
            return mActions.get(FIRT_ELEMENT).getActions();

        List<GuidedAction> guidedActions = new ArrayList<>();

        for (GuidedActionList guidedActionList : mActions) {
            if (guidedActionList.getKey().equals(keyFind)) {

                if (!guidedActionList.getKey().equals(firstElementKeys)) {
                    GuidedAction guidedAction = mActions.get(FIRT_ELEMENT).getActions().get(FIRT_ELEMENT);

                    if (guidedAction != null)
                        guidedAction.setDescription(keyFind);

                    guidedActions.add(guidedAction);
                }

                guidedActions.addAll(guidedActionList.getActions());
                break;
            }
        }
        return guidedActions;
    }

    public List<GuidedAction> getActionsByKey(String keyFind, List<GuidedActionList> guidedActionLists) {
        if (guidedActionLists == null)
            return new ArrayList<>();

        if (guidedActionLists.size() <= 0)
            return new ArrayList<>();

        if (keyFind == null)
            return guidedActionLists.get(FIRT_ELEMENT).getActions();

        List<GuidedAction> guidedActions = new ArrayList<>();

        for (GuidedActionList guidedActionList : guidedActionLists) {
            if (guidedActionList.getKey().equals(keyFind)) {
                guidedActions = guidedActionList.getActions();
                break;
            }
        }
        return guidedActions;
    }
}
