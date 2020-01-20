package com.kenvix.android.ui.base;

public interface BaseActivitySupport {
    /**
     * 获取当前的 Activity 的 layout
     * @return 范例： R.layout.activity_forum
     */
    int getBaseLayout();

    /**
     * 获取当前的 Activity 的顶层容器的 ID
     * @return 范例： R.id.login_container
     */
    int getBaseContainer();
}
