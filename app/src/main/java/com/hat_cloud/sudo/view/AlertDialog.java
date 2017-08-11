package com.hat_cloud.sudo.view;

import android.content.Context;

/**
 * Created by linmh on 2017/8/8.
 */

public class AlertDialog extends android.app.AlertDialog.Builder {
    public AlertDialog(Context context) {
        super(context);
    }

    public AlertDialog(Context context, int theme) {
        super(context, theme);
    }

}
