package ble.aigo.thl.com.cn.aigothl.MyApp;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Stack;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by Administrator on 2016/12/15.
 */

public class AppManager extends Application {
    private Application app;
    Stack<WeakReference<Activity>> activityStack = new Stack<WeakReference<Activity>>();


    private AppManager() {
    }

    public void init(Application app) {
        if (app == null) {
            throw new IllegalArgumentException("app can't be null");
        }
        if (Looper.myLooper() == null || Looper.myLooper().getThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalStateException("call this method at MAIN Thread!");
        }

        Thread.currentThread().setUncaughtExceptionHandler(crashHandler);
        this.app = app;
//        app.registerActivityLifecycleCallbacks(actLifeCallback);
    }

    private static AppManager instance = new AppManager();

    public static AppManager getInstance() {
        return instance;
    }

    public void addActivity(Activity act) {
        activityStack.push(new WeakReference<Activity>(act));
    }

    public void removeTopActivity() {
        activityStack.pop();
    }

    public Activity getTopActivity() {
        if (activityStack.empty()) {
            return null;
        }
        return activityStack.peek().get();
    }


    public boolean removeActivity(Activity act) {
        int size = activityStack.size();
        for (int i = size - 1; i >= 0; i--) {
            WeakReference<Activity> actRef = activityStack.get(i);
            if (actRef.get() == act) {
                activityStack.remove(i);
                return true;
            }
        }
        return false;
    }

    public void finishAll() {
        while (!activityStack.empty()) {
            Activity topAct = activityStack.pop().get();
            if (topAct != null) {
                topAct.finish();
            }
        }

    }

    private CyclicBarrier cyclicBarrier;
    private Thread.UncaughtExceptionHandler crashHandler = new Thread.UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Thread thread, final Throwable ex) {
            Log.d("uncaughtException", "Thread " + thread.getName());
            ex.printStackTrace();

            cyclicBarrier = new CyclicBarrier(2, new Runnable() {
                @Override
                public void run() {
                    Log.d("all finish", "shot");
                    finishApp();
                }
            });

            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    showNotify(ex);
                    Looper.loop();
                }
            }.start();
            final File report = saveReport(ex);
            new Thread() {
                @Override
                public void run() {
                    uploadReport(report);
                    try {
                        Log.d("report", "finish");
                        cyclicBarrier.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
//            AppManager.getInstance().finishAll();
//            defaultHandler.uncaughtException(thread, ex);

        }

        private void showNotify(Throwable ex) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AppManager.getInstance().getTopActivity());
            builder.setTitle("出错了");
            builder.setMessage("非常抱歉，程序出现错误，已经将错误报告提交给开发人员，程序即将退出。");
            builder.setPositiveButton("退出程序", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finishAll();
                    try {
                        Log.d("dialog", "finish");
                        cyclicBarrier.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            builder.show();
        }


        private File saveReport(Throwable ex) {
            FileWriter writer ;
            PrintWriter printWriter = null;
            try {
                File file = new File(app.getFilesDir(), "" + System.currentTimeMillis());
                writer = new FileWriter(file);
                printWriter = new PrintWriter(writer);
                writer.append("========Build==========\n");
                writer.append(String.format("BOARD\t%s\n", Build.BOARD));
                writer.append(String.format("BOOTLOADER\t%s\n", Build.BOOTLOADER));
                writer.append(String.format("BRAND\t%s\n", Build.BRAND));
                writer.append(String.format("CPU_ABI\t%s\n", Build.CPU_ABI));
                writer.append(String.format("CPU_ABI2\t%s\n", Build.CPU_ABI2));
                writer.append(String.format("DEVICE\t%s\n", Build.DEVICE));
                writer.append(String.format("DISPLAY\t%s\n", Build.DISPLAY));
                writer.append(String.format("FINGERPRINT\t%s\n", Build.FINGERPRINT));
                writer.append(String.format("HARDWARE\t%s\n", Build.HARDWARE));
                writer.append(String.format("HOST\t%s\n", Build.HOST));
                writer.append(String.format("ID\t%s\n", Build.ID));
                writer.append(String.format("MANUFACTURER\t%s\n", Build.MANUFACTURER));
                writer.append(String.format("MODEL\t%s\n", Build.MODEL));
                writer.append(String.format("SERIAL\t%s\n", Build.SERIAL));
                writer.append(String.format("PRODUCT\t%s\n", Build.PRODUCT));

                writer.append("========APP==========\n");
                try {
                    PackageInfo packageInfo = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
                    int versionCode = packageInfo.versionCode;
                    String versionName = packageInfo.versionName;
                    writer.append(String.format("versionCode\t%s\n", versionCode));
                    writer.append(String.format("versionName\t%s\n", versionName));

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                writer.append("========Exception==========\n");
                ex.printStackTrace(printWriter);
                return file;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (printWriter != null) {
                    printWriter.close();
                }
            }


        }


    };

    private void uploadReport(File report) {
        OutputStream os = null;
        FileInputStream fis = null;
        try {
            URL url = new URL("http://10.0.2.2:8080/p2p/ErrorReportServlet");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            os = urlConnection.getOutputStream();
            fis = new FileInputStream(report);
            byte[] buf = new byte[1024 * 8];
            int len = 0;
            while ((len = fis.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
            int responseCode = urlConnection.getResponseCode();
            Log.d("uploadReport","" + responseCode);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(os);
            close(fis);
        }



    }
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void finishApp() {
        System.exit(0);
    }

}
