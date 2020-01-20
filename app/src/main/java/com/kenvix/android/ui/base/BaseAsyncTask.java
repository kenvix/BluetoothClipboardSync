package com.kenvix.android.ui.base;

import android.os.AsyncTask;
import androidx.annotation.Nullable;

import com.kenvix.utils.log.LogUtilsKt;
import com.kenvix.utils.log.Logging;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

/**
 * 基 AsyncTask
 * @param <T>
 * @param <P>
 * @param <R>
 */
public abstract class BaseAsyncTask<T, P, R, C> extends AsyncTask<T, P, R> implements Logging {
    private Exception exception = null;
    private String taskName;
    @Nullable
    private Consumer<Exception> onExceptionCallback;
    private WeakReference<C> connection;

    /**
     * Run task default implement
     * @param ts args
     * @return X if success, null if failed
     */
    @Override
    @SafeVarargs
    protected final @Nullable R doInBackground(T... ts) {
        try {
            return doTask(ts);
        } catch (Exception e) {
            setException(e);
            onException(e);
            return null;
        }
    }

    /**
     * 所要执行的任务
     * @param ts 任务的参数列表
     * @return R 任务结果
     * @throws Exception 任务执行过程中抛出的异常
     */
    protected abstract R doTask(T... ts) throws Exception;
    protected void onException(Exception exception) {
        LogUtilsKt.warning(getLogger(), exception, "Task failed");

        if (onExceptionCallback != null)
            onExceptionCallback.accept(exception);
    }

    /**
     * 获取任务执行过程中抛出的异常
     * @return null 为无异常
     */
    public Exception getException() {
        return exception;
    }

    /**
     * 获取任务发生异常时的回调
     * @return
     */
    public Consumer<Exception> getOnExceptionCallback() {
        return onExceptionCallback;
    }

    /**
     * 设置任务发生异常时的回调
     * @param onExceptionCallback
     */
    public void setOnExceptionCallback(@Nullable Consumer<Exception> onExceptionCallback) {
        this.onExceptionCallback = onExceptionCallback;
    }

    /**
     * 标记任务处于异常状态
     * @param exception
     */
    protected void setException(Exception exception) {
        this.exception = exception;
    }

    @Override
    public String getLogTag() {
        return taskName == null ? taskName = this.getClass().getSimpleName() : taskName;
    }

    /**
     * 判断任务执行过程中是否抛出异常
     * @return
     */
    public boolean isExceptionThrown() {
        return exception != null;
    }

    /**
     * 设置基于弱引用的连接对象。一般是 Activity 等任务的创建者
     * @param connection
     */
    public void setConnection(C connection) {
        this.connection = new WeakReference<>(connection);
    }


    /**
     * 获取强引用的的连接对象
     * @return
     */
    @Nullable
    public C getConnection() {
        return connection.get();
    }
}

