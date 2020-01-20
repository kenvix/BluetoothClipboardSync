// Rcon Manager for Android
// Copyright (c) 2019. Kenvix <i@kenvix.com>
//
// Licensed under GNU Affero General Public License v3.0

package com.kenvix.android.ui.base.view.base;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import com.kenvix.android.ui.base.BaseActivity;
import com.kenvix.android.utils.Invoker;

public abstract class BaseHolder<T> extends RecyclerView.ViewHolder {

    public BaseHolder(@NonNull View itemView) {
        super(itemView);
        Invoker.invokeViewAutoLoader(this, itemView);
    }

    protected BaseActivity getActivityByView(View v) {
        return (BaseActivity) v.getContext();
    }

    public abstract void bindView(T data);
}
