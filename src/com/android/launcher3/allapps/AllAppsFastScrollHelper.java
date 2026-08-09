/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.allapps;

import static android.view.HapticFeedbackConstants.CLOCK_TICK;
import static com.android.launcher3.allapps.BaseAllAppsAdapter.VIEW_TYPE_MASK_ICON;

import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.LinearSmoothScroller;

import com.android.launcher3.allapps.AlphabeticalAppsList.FastScrollSectionInfo;
import com.android.launcher3.allapps.BaseAllAppsAdapter.AdapterItem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllAppsFastScrollHelper {

    private static final int NO_POSITION = -1;
    private static final float DIMMED_ICON_ALPHA = 0.3f;
    private static final long ICON_ALPHA_DURATION_MS = 150;

    private int mTargetFastScrollPosition = NO_POSITION;
    private CharSequence mTargetSectionName;
    private final Set<View> mTrackedIconViews = new HashSet<>();

    private AllAppsRecyclerView mRv;

    public AllAppsFastScrollHelper(AllAppsRecyclerView rv) {
        mRv = rv;
    }

    /**
     * Smooth scrolls the recycler view to the given section.
     */
    public void smoothScrollToSection(FastScrollSectionInfo info) {
        if (mTargetFastScrollPosition == info.position) {
            return;
        }
        mTargetFastScrollPosition = info.position;
        mTargetSectionName = info.sectionName;
        updateIconAlphas(mTargetSectionName);
        mRv.getLayoutManager().startSmoothScroll(new MyScroller(mTargetFastScrollPosition));
    }

    public void onFastScrollCompleted() {
        mTargetFastScrollPosition = NO_POSITION;
        mTargetSectionName = null;
        updateIconAlphas(null);
    }

    private void updateIconAlphas(CharSequence selectedSectionName) {
        // LauncherEX: keep the selected letter's apps fully visible and de-emphasize the rest.
        if (selectedSectionName == null) {
            for (View icon : mTrackedIconViews) {
                icon.animate().alpha(1f).setDuration(ICON_ALPHA_DURATION_MS).start();
            }
            mTrackedIconViews.clear();
            return;
        }
        List<AdapterItem> items = mRv.getApps().getAdapterItems();
        for (int i = 0; i < mRv.getChildCount(); i++) {
            View child = mRv.getChildAt(i);
            int position = mRv.getChildAdapterPosition(child);
            if (position == NO_POSITION || position >= items.size()) {
                continue;
            }
            AdapterItem item = items.get(position);
            if ((item.viewType & VIEW_TYPE_MASK_ICON) == 0 || item.itemInfo == null) {
                continue;
            }
            mTrackedIconViews.add(child);
            float targetAlpha = TextUtils.equals(item.itemInfo.sectionName, selectedSectionName)
                    ? 1f : DIMMED_ICON_ALPHA;
            child.animate().alpha(targetAlpha).setDuration(ICON_ALPHA_DURATION_MS).start();
        }
    }

    private class MyScroller extends LinearSmoothScroller {

        private final int mTargetPosition;

        public MyScroller(int targetPosition) {
            super(mRv.getContext());

            mTargetPosition = targetPosition;
            setTargetPosition(targetPosition);
        }

        @Override
        protected int getVerticalSnapPreference() {
            mRv.performHapticFeedback(CLOCK_TICK);
            return SNAP_TO_ANY;
        }

        @Override
        protected void onStop() {
            super.onStop();
            if (mTargetPosition != mTargetFastScrollPosition) {
                // Target changed, before the last scroll can finish
                return;
            }

            updateIconAlphas(mTargetSectionName);
        }
    }
}
