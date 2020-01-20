// Rcon Manager for Android
// Copyright (c) 2019. Kenvix <i@kenvix.com>
//
// Licensed under GNU Affero General Public License v3.0

package com.kenvix.android.ui.base;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.kenvix.android.utils.Invoker;
import com.kenvix.utils.log.Logging;

public abstract class BaseActivity extends AppCompatActivity implements Logging, BaseActivitySupport {
    protected FragmentManager fragmentManager;
    private Fragment _foregroundFragment = null;
    private String logTag;
    private BaseActivityUI ui;

    @Override
    public String getLogTag() {
        return logTag == null ? (logTag = this.getClass().getSimpleName()) : logTag;
    }

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLogger().finest("Start activity");
        setContentView(getBaseLayout());
        fragmentManager = getSupportFragmentManager();
        Invoker.invokeViewAutoLoader(this);
        ui = new BaseActivityUI(this);

        onInitialize(savedInstanceState);
    }

    public final SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    /**
     * 设置当前前台 fragment
     * @param container 容器 ID
     * @param fragment fragment 对象
     * @return 设置结果
     */
    public final boolean setForegroundFragment(int container, Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if(_foregroundFragment != null)
            transaction.hide(_foregroundFragment);

        _foregroundFragment = fragment;

        if(!fragment.isAdded()) {
            transaction.add(container, fragment);
        } else {
            transaction.show(fragment);
        }

        transaction.commit();

        getLogger().finest("Changed fragment to " + fragment.getClass().getSimpleName() + " on container " + container);
        return true;
    }

    /**
     * 设置当前前台 fragment
     * @param fragment fragment 对象
     * @return 设置结果
     */
    public final boolean setForegroundFragment(BaseFragment fragment) {
        return setForegroundFragment(fragment.getBaseActivityContainer(), fragment);
    }

    /**
     * 当所有权限被授权时回调
     * @param code
     */
    public void onAllPermissionsGranted(int code) {

    }

    public BaseActivityUI getUi() {
        return ui;
    }

    /**
     * 当 Activity 被创建时的事件。代替 onCreate()
     * @param savedInstanceState
     */
    protected abstract void onInitialize(@Nullable Bundle savedInstanceState);

    /**
     * 获取当前前台fragment
     * @return Fragment
     */
    public Fragment getForegroundFragment() {
        return _foregroundFragment;
    }
}
