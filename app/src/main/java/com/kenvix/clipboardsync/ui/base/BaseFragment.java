// Rcon Manager for Android
// Copyright (c) 2019. Kenvix <i@kenvix.com>
//
// Licensed under GNU Affero General Public License v3.0

package com.kenvix.clipboardsync.ui.base;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kenvix.utils.log.Logging;
import com.kenvix.utils.android.Invoker;

public abstract class BaseFragment extends Fragment implements Logging, BaseFragmentSupport {
    private String logTag;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(getActivity() == null)
            throw new IllegalArgumentException("Activity cant be null");

        if(!(getActivity() instanceof BaseActivity))
            throw new IllegalArgumentException("All Activity must extends BaseActivity");

        return inflater.inflate(getFragmentContentLayout(), container, false);
    }

    /**
     * 当 fragment 被创建时的事件，禁止覆盖。使用 onInitialize()
     * @param view
     * @param savedInstanceState
     */
    @Override
    public final void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Invoker.invokeViewAutoLoader(this, view);
        onInitialize(view);
    }

    @Override
    public String getLogTag() {
        return logTag == null ? (logTag = getBaseActivity().getClass().getSimpleName() + "_" + this.getClass().getSimpleName()) : logTag;
    }

    /**
     * 获取当前 fragment 所属 activity 的对象
     * @return BaseActivity Object
     */
    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    /**
     * 当 fragment 被创建时的事件
     * @param view
     */
    protected abstract void onInitialize(@NonNull View view);
}
