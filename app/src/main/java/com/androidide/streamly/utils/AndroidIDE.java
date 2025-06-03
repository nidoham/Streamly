package com.androidide.streamly.utils;

import android.content.Context;
import android.widget.Toast;

public class AndroidIDE {
    
    public static void ShowMessages(final Context context, final String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
}