package ble.aigo.thl.com.cn.aigothl.MyApp;

import android.util.Log;

/**
 * Created by Q on 2016/4/8.
 */
public class Logger {
    public static final int debugLevel = 0;

    public static void v(String tag, String msg) {
        if (debugLevel < 1) {
            return;
        }
        Log.v(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (debugLevel < 2) {
            return;
        }
        Log.d(tag, msg);
    }

    public static void i(String tag, String msg) {
        if (debugLevel < 3) {
            return;
        }
        Log.i(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (debugLevel < 4) {
            return;
        }
        Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (debugLevel < 5) {
            return;
        }
        Log.e(tag, msg);
    }

    public static void v(String msg) {
        v("Logger", msg);
    }

    public static void d(String msg) {
        d("Logger", msg);
    }

    public static void i(String msg) {
        i("Logger", msg);
    }

    public static void w(String msg) {
        w("Logger", msg);
    }

    public static void e(String msg) {
        e("Logger", msg);
    }


}
