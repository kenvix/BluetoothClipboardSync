package com.kenvix.clipboardsync.ui.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;
import com.kenvix.clipboardsync.R;
import com.kenvix.utils.log.LogUtilsKt;

import java.util.function.Consumer;

public class BaseActivityUI {
    private BaseActivity context;

    BaseActivityUI(BaseActivity activity) {
        context = activity;
    }

    public void showToast(String text, int toastLength) {
        Toast.makeText(context, text, toastLength).show();
    }

    public void showToast(String text) {
        showToast(text, Toast.LENGTH_LONG);
    }

    public void showSnackbar(View container, String text, int snackLength) {
        Snackbar.make(container, text, snackLength).show();
    }

    public void showSnackbar(View container, String text) {
        showSnackbar(container, text, Snackbar.LENGTH_SHORT);
    }

    public void showSnackbar(String text, int snackLength) {
        Snackbar.make(context.findViewById(context.getBaseContainer()), text, snackLength).show();
    }

    public void showSnackbar(String text) {
        showSnackbar(text, Snackbar.LENGTH_SHORT);
    }

    public void showExceptionToastPrompt(Throwable throwable) {
        showToast(context.getString(R.string.error_operation_failed) + throwable.getLocalizedMessage());
        LogUtilsKt.severe(context.getLogger(), throwable, "Global Exception Prompt: Operation FAILED");
    }

    public void showExceptionSnackbarPrompt(Throwable throwable) {
        showSnackbar(context.getString(R.string.error_operation_failed) + throwable.getLocalizedMessage(), Snackbar.LENGTH_LONG);
        LogUtilsKt.severe(context.getLogger(), throwable, "Global Exception Prompt: Operation FAILED");
    }

    public AlertDialog showConfirmDialog(String text, @Nullable String title, @Nullable Consumer<Boolean> callback) {
        return getConfirmBuilder(text, title, callback).show();
    }

    public AlertDialog showConfirmDialog(String text, @Nullable Consumer<Boolean> callback) {
        return getConfirmBuilder(text, null, callback).show();
    }

    public AlertDialog showAlertDialog(String text, @Nullable String title, @Nullable Consumer<Boolean> callback) {
        return getAlertBuilder(text, title, callback).show();
    }

    public AlertDialog showAlertDialog(String text, @Nullable Consumer<Boolean> callback) {
        return getAlertBuilder(text, null, callback).show();
    }

    public AlertDialog showAlertDialog(String text) {
        return getAlertBuilder(text, null, null).show();
    }

    public android.app.AlertDialog showProgressDialog(String text, boolean isCancelable, @Nullable String title, @Nullable Consumer<Boolean> callback) {
        return getProgressDialogBuilder(text, isCancelable, title, callback).show();
    }

    public android.app.AlertDialog showProgressDialog(String text) {
        return getProgressDialogBuilder(text, false, null, null).show();
    }

    public AlertDialog.Builder getAlertBuilder(String text, @Nullable String title, @Nullable Consumer<Boolean> callback) {
        return getAlertBuilder(context, text, title, callback);
    }

    public static AlertDialog.Builder getAlertBuilder(Context context, String text, @Nullable String title, @Nullable Consumer<Boolean> callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setMessage(text)
                .setOnCancelListener(dialog -> {
                    if(callback != null)
                        callback.accept(false);
                })
                .setPositiveButton(context.getString(R.string.action_ok), (dialog, which) -> {
                    if (callback != null)
                        callback.accept(true);
                });

        if (title == null)
            return builder;
        else
            return builder.setTitle(title);
    }

    public AlertDialog.Builder getConfirmBuilder(String text, @Nullable String title, @Nullable Consumer<Boolean> callback) {
        return getAlertBuilder(text, title, callback)
                .setNegativeButton(context.getString(R.string.action_cancel),(dialog, which) -> {
                    if(callback != null)
                        callback.accept(false);
                });
    }

    public ProgressDialog.Builder getProgressDialogBuilder(String text, boolean isCancelable, @Nullable String title, @Nullable Consumer<Boolean> callback) {
        ProgressDialog.Builder builder = new ProgressDialog.Builder(context).setMessage(text);
        builder.setCancelable(isCancelable);

        if (isCancelable) {
            builder.setNegativeButton(context.getString(R.string.action_cancel),(dialog, which) -> {
                if(callback != null)
                    callback.accept(false);
            });
        }

        if (callback != null) {
            builder.setOnCancelListener(dialog -> callback.accept(false));
            builder.setOnDismissListener(dialog -> callback.accept(true));
        }

        if (title != null)
            builder.setTitle(title);
        return builder;
    }
}
