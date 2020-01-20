//--------------------------------------------------
// Interface BaseFragmentSupport
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.android.ui.base

import android.content.ComponentCallbacks
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner

interface BaseFragmentSupport : ComponentCallbacks, View.OnCreateContextMenuListener,
    LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    /**
     * 获取当前的 fragment 的 layout
     * @return 范例： R.layout.fragment_forum
     */
    fun getFragmentContentLayout(): Int

    /**
     * 获取当前的 fragment 所属 activity 的 fragment 容器的 ID
     * @return 范例： R.id.main_fragment_container
     */
    fun getBaseActivityContainer(): Int
}