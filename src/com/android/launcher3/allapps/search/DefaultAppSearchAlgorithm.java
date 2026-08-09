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
package com.android.launcher3.allapps.search;

import static com.android.launcher3.allapps.BaseAllAppsAdapter.VIEW_TYPE_EMPTY_SEARCH;
import static com.android.launcher3.model.data.ItemInfoWithIcon.FLAG_DISABLED_QUIET_USER;

import android.content.Context;
import android.icu.text.Transliterator;
import android.os.Handler;
import android.util.LruCache;

import androidx.annotation.AnyThread;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.allapps.BaseAllAppsAdapter.AdapterItem;
import com.android.launcher3.model.data.AppInfo;
import com.android.launcher3.search.SearchAlgorithm;
import com.android.launcher3.search.SearchCallback;
import com.android.launcher3.search.StringMatcherUtility;
import com.android.launcher3.util.LooperExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * The default search implementation.
 */
public class DefaultAppSearchAlgorithm implements SearchAlgorithm<AdapterItem> {

    private static final int MAX_RESULTS_COUNT = 5;
    // LauncherEX: use Android's built-in ICU data for Chinese initials instead of bundling a
    // separate pinyin library, and cache labels because this search runs after every keystroke.
    private static final Transliterator PINYIN_TRANSLITERATOR = Transliterator.getInstance(
            "Han-Latin; Latin-ASCII; Lower()");
    private static final LruCache<String, String> PINYIN_INITIALS_CACHE = new LruCache<>(256);

    private final LauncherAppState mAppState;
    private final Handler mResultHandler;
    private final boolean mAddNoResultsMessage;

    public DefaultAppSearchAlgorithm(Context context, LooperExecutor uiExecutor) {
        this(context, uiExecutor, false);
    }

    public DefaultAppSearchAlgorithm(
            Context context, LooperExecutor uiExecutor, boolean addNoResultsMessage) {
        mAppState = LauncherAppState.getInstance(context);
        mResultHandler = new Handler(uiExecutor.getLooper());
        mAddNoResultsMessage = addNoResultsMessage;
    }

    @Override
    public void cancel(boolean interruptActiveRequests) {
        if (interruptActiveRequests) {
            mResultHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void doSearch(String query, SearchCallback<AdapterItem> callback) {
        mAppState.getModel().enqueueModelUpdateTask((taskController, dataModel, apps) ->  {
            ArrayList<AdapterItem> result = getTitleMatchResult(apps.data, query);
            if (mAddNoResultsMessage && result.isEmpty()) {
                result.add(getEmptyMessageAdapterItem(query));
            }
            mResultHandler.post(() -> callback.onSearchResult(query, result));
        });
    }

    private static AdapterItem getEmptyMessageAdapterItem(String query) {
        AdapterItem item = new AdapterItem(VIEW_TYPE_EMPTY_SEARCH);
        // Add a place holder info to propagate the query
        AppInfo placeHolder = new AppInfo();
        placeHolder.title = query;
        item.itemInfo = placeHolder;
        return item;
    }

    /**
     * Filters {@link AppInfo}s matching specified query
     */
    @AnyThread
    public static ArrayList<AdapterItem> getTitleMatchResult(List<AppInfo> apps, String query) {
        // Do an intersection of the words in the query and each title, and filter out all the
        // apps that don't match all of the words in the query.
        final String queryTextLower = query.toLowerCase();
        final ArrayList<AdapterItem> result = new ArrayList<>();
        StringMatcherUtility.StringMatcher matcher =
                StringMatcherUtility.StringMatcher.getInstance();

        int resultCount = 0;
        int total = apps.size();
        for (int i = 0; i < total && resultCount < MAX_RESULTS_COUNT; i++) {
            AppInfo info = apps.get(i);
            // LauncherEX: the model retains apps from locked Private Space and paused profiles;
            // never expose those quiet-profile entries through either search matching path.
            if ((info.runtimeStatusFlags & FLAG_DISABLED_QUIET_USER) != 0) {
                continue;
            }
            String title = info.title.toString();
            // LauncherEX: keep Launcher3's normal title matching and add pinyin initials only as
            // an alternative, so existing searches retain their behavior and ordering.
            if (StringMatcherUtility.matches(queryTextLower, title, matcher)
                    || matchesPinyinInitials(queryTextLower, title)) {
                result.add(AdapterItem.asApp(info));
                resultCount++;
            }
        }
        return result;
    }

    private static boolean matchesPinyinInitials(String query, String title) {
        if (!containsOnlyAsciiLetters(query) || !containsHan(title)) {
            return false;
        }

        String initials = PINYIN_INITIALS_CACHE.get(title);
        if (initials == null) {
            initials = extractInitials(PINYIN_TRANSLITERATOR.transliterate(title));
            PINYIN_INITIALS_CACHE.put(title, initials);
        }
        return initials.startsWith(query);
    }

    private static boolean containsOnlyAsciiLetters(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character < 'a' || character > 'z') {
                return false;
            }
        }
        return true;
    }

    private static boolean containsHan(String value) {
        for (int i = 0; i < value.length();) {
            int codePoint = value.codePointAt(i);
            if (Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HAN) {
                return true;
            }
            i += Character.charCount(codePoint);
        }
        return false;
    }

    private static String extractInitials(String transliteratedTitle) {
        StringBuilder initials = new StringBuilder();
        boolean atWordStart = true;
        for (int i = 0; i < transliteratedTitle.length(); i++) {
            char character = transliteratedTitle.charAt(i);
            if (character >= 'a' && character <= 'z') {
                if (atWordStart) {
                    initials.append(character);
                }
                atWordStart = false;
            } else {
                atWordStart = true;
            }
        }
        return initials.toString();
    }
}
