package ble.aigo.thl.com.cn.aigothl.MyApp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by teacher on 2016/3/20.
 */
public class UiUtil {

    private static Context context;

    static Handler mainUiHandler = new Handler(Looper.getMainLooper());


    public static Handler getMainUiHandler() {
        return mainUiHandler;
    }

    public static void init(Context context) {
        UiUtil.context = context;
    }

    public static void runInUiThread(Runnable runnable) {
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) {
            mainUiHandler.post(runnable);
        } else {
            runnable.run();
        }
    }

    static Random random = new Random();


    public static int dp2Px(int dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics()) + 0.5);
    }

    public static int getRandomColor() {
        return Color.argb(0xFF, random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }

    public static void setParentAndChildrenEnable(View parent, boolean enable) {
        parent.setEnabled(enable);
        if (parent instanceof ViewGroup) {
            int childCount = ((ViewGroup) parent).getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = ((ViewGroup) parent).getChildAt(i);
                setParentAndChildrenEnable(child, enable);
            }
        }
    }

//    public static  View inflate(int layoutRes){
//        return View.inflate(getContext(),layoutRes,null);
//    }

    public static Context getContext() {
        if (context == null) {
            throw new IllegalStateException("请先调用init方法");
        }
        return context;
    }

    public static float saturation( int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;


        int V = Math.max(b, Math.max(r, g));
        int temp = Math.min(b, Math.min(r, g));

        float S;

        if (V == temp) {
            S = 0;
        } else {
            S = (V - temp) / (float) V;
        }

        return S;
    }

    public static TextView createTextView( String title,Context context) {

        TextView result = new TextView(context);
        result.setText(title);


        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius(dp2Px(5));
        int randomColor = getRandomColor();
        gradientDrawable.setColor(randomColor);


        result.setTextColor(saturation(randomColor) < 0.5f ? Color.BLACK : Color.WHITE);

        result.setBackgroundDrawable(gradientDrawable);
        result.setPadding(dp2Px(2), dp2Px(2), dp2Px(2), dp2Px(2));
        return result;
    }
}
