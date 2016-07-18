package com.king.kingsafe.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

	public static void show(Context context, String text) {
		Toast.makeText(context, text, 0).show();
	}
}
