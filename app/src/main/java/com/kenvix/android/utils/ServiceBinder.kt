//--------------------------------------------------
// Class ServiceBinder
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.android.utils

import android.os.Binder

abstract class ServiceBinder<T: BaseService> : Binder() {
    abstract val service: T
    val isServiceSuccessfullyInitialized
        get() = service.initException == null
    val serviceInitException
        get() = service.initException
}