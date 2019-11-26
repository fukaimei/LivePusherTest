package net.fkm.livepushertest.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import net.fkm.livepushertest.application.LivePusherApplication;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;

/**
 * 基础工具类
 */
public class BaseTools {

    //获取图片所在文件夹名称
    public static String getDir(String path) {
        String subString = path.substring(0, path.lastIndexOf('/'));
        return subString.substring(subString.lastIndexOf('/') + 1, subString.length());
    }

    public static int getWindowWidth(Context context) {
        // 获取屏幕分辨率
        WindowManager wm = (WindowManager) (context
                .getSystemService(Context.WINDOW_SERVICE));
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int mScreenWidth = dm.widthPixels;
        return mScreenWidth;
    }

    public static int getWindowHeigh(Context context) {
        // 获取屏幕分辨率
        WindowManager wm = (WindowManager) (context
                .getSystemService(Context.WINDOW_SERVICE));
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int mScreenHeigh = dm.heightPixels;
        return mScreenHeigh;
    }

    //获得状态栏/通知栏的高度
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * 使用默认方式显示货币：
     * 例如:￥12,345.46 默认保留2位小数，四舍五入
     *
     * @param d double
     * @return String
     */
    public static String formatCurrency(double d) {
        String s = "";
        try {
            DecimalFormat nf = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.CHINA);
            s = nf.format(d);
        } catch (Exception e) {
            e.printStackTrace();
            return "" + d;
        }
        return s;
    }

    /**
     * 去掉无效小数点 ".00"
     */
    public static String formatMoney(double d) {
        String tmp = formatCurrency(d);

        if (tmp.endsWith(".00")) {
            return tmp.substring(0, tmp.length() - 3);
        } else {
            return tmp;
        }
    }

    /**
     * 处于栈顶的Activity名
     */
    public String getTopActivityName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List var2 = am.getRunningTasks(1);
        return ((ActivityManager.RunningTaskInfo) var2.get(0)).topActivity.getClassName();
    }

    public static void setText(String text, TextView textView) {
        if (textView != null) {
            if (TextUtils.isEmpty(text)) {
                textView.setText("");
            } else {
                textView.setText(text);
            }
        }
    }

    /**
     * 获取当前应用的版本Name
     */
    public static String getVersionName() {
        // 获取packagemanager的实例
        PackageManager packageManager = LivePusherApplication.getInstance().getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(LivePusherApplication.getInstance().getPackageName(), 0);
            return packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "unknown";
        }
    }

    /**
     * @return 当前应用的版本Code
     */
    public static int getVersionCode() {
        try {
            PackageManager packageManager = LivePusherApplication.getInstance().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    LivePusherApplication.getInstance().getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 实现文本复制功能
     *
     * @param content 复制的文本
     */
    public static void copy(String content) {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) LivePusherApplication.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }

    /**
     * 使用浏览器打开链接
     */
    public static void openLink(Context context, String content) {
        Uri issuesUrl = Uri.parse(content);
        Intent intent = new Intent(Intent.ACTION_VIEW, issuesUrl);
        context.startActivity(intent);
    }

    /**
     * 判断手机是否安装某个应用
     *
     * @param context
     * @param appPackageName 应用包名
     * @return true：安装，false：未安装
     */
    public static boolean isApplicationAvilible(Context context, String appPackageName) {
        try {
            // 获取packagemanager
            PackageManager packageManager = context.getPackageManager();
            // 获取所有已安装程序的包信息
            List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
            if (pinfo != null) {
                for (int i = 0; i < pinfo.size(); i++) {
                    String pn = pinfo.get(i).packageName;
                    if (appPackageName.equals(pn)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * 打开该应用所在应用市场上线的地址
     *
     * @param context
     * @param appPkg    该应用包名
     * @param marketPkg 应用市场App包名
     */
    public static void launchAppDetail(Context context, String appPkg, String marketPkg) {
        try {
            if (TextUtils.isEmpty(appPkg)) {
                return;
            }
            Uri uri = Uri.parse("market://details?id=" + appPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            // 如果设置了market包名 打开指定app市场
            if (!TextUtils.isEmpty(marketPkg)) {
                intent.setPackage(marketPkg);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getNowDateTime() {
        String format = "yyyyMMddHHmmss";
        SimpleDateFormat s_format = new SimpleDateFormat(format);
        Date d_date = new Date();
        String s_date = "";
        s_date = s_format.format(d_date);
        return s_date;
    }

    public static String getNowDateTimeFull() {
        String format = "yyyyMMddHHmmssSSS";
        SimpleDateFormat s_format = new SimpleDateFormat(format);
        Date d_date = new Date();
        String s_date = "";
        s_date = s_format.format(d_date);
        return s_date;
    }

    public static String getNowDateTimeFormat() {
        String format = "yyyy/MM/dd HH:mm:ss";
        SimpleDateFormat s_format = new SimpleDateFormat(format);
        Date d_date = new Date();
        String s_date = "";
        s_date = s_format.format(d_date);
        return s_date;
    }

    public static String getNowDateTimeFormat1() {
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat s_format = new SimpleDateFormat(format);
        Date d_date = new Date();
        String s_date = "";
        s_date = s_format.format(d_date);
        return s_date;
    }

    public static String getNowDate() {
        String format = "yyyyMMdd";
        SimpleDateFormat s_format = new SimpleDateFormat(format);
        Date d_date = new Date();
        String s_date = "";
        s_date = s_format.format(d_date);
        return s_date;
    }

    public static String getNowTime() {
        SimpleDateFormat s_format = new SimpleDateFormat("HH:mm:ss");
        return s_format.format(new Date());
    }

    /**
     * 将长时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
     *
     * @param strDate
     * @return
     */
    public static String strToDateLong(String strDate) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            Date date = formatter.parse(strDate);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss");
            String sDate = sdf.format(date);
            return sDate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将长时间格式时间转换为字符串 yyyy-MM-dd HH:mm:ss
     *
     * @param dateDate
     * @return
     */
    public static String dateToStrLong(Date dateDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(dateDate);
        return dateString;
    }

    /**
     * 将短时间格式时间转换为字符串 yyyy-MM-dd
     *
     * @param dateDate
     * @param k
     * @return
     */
    public static String dateToStr(Date dateDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(dateDate);
        return dateString;
    }

    /**
     * 将短时间格式字符串转换为时间 yyyy-MM-dd
     *
     * @param strDate
     * @return
     */
    public static Date strToDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }

    /**
     * 获取前n天日期、后n天日期
     *
     * @param distanceDay 前几天 如获取前7天日期则传-7即可；如果后7天则传7
     * @return
     */
    public static String getDate(int distanceDay) {

        SimpleDateFormat dft = new SimpleDateFormat("yyyyMMdd");
        Date beginDate = new Date();
        Calendar date = Calendar.getInstance();
        date.setTime(beginDate);
        date.set(Calendar.DATE, date.get(Calendar.DATE) + distanceDay);
        Date endDate = null;
        try {
            endDate = dft.parse(dft.format(date.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }
//        L.i("后10天==" + dft.format(endDate));
        return dft.format(endDate);
    }

    /**
     * 获取Assets目录的json文件
     *
     * @return json data
     */
    public static String getAssetsJson(Context context, String fileName) {
        InputStream input = null;
        try {
            input = context.getAssets().open(fileName);
            String json = convertStreamToString(input);
            return json;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * input 流转换为字符串
     *
     * @param is
     * @return
     */
    private static String convertStreamToString(InputStream is) {
        String s = null;
        try {
            Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
            if (scanner.hasNext()) {
                s = scanner.next();
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    /**
     * 判断当前设备是手机还是平板
     *
     * @param context
     * @return 平板返回 True，手机返回 False
     */
    public static boolean isPad(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * 返回当前屏幕是否为横屏
     *
     * @param context
     * @return 当且仅当当前屏幕为横屏时返回true, 否则返回false。
     */
    public static boolean isScreenOriatationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    public static void hideBottomNav(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(0);
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /**
     * 重新显示导航栏和状态栏
     */
    public static void showBottomNav(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(0);
    }

    /**
     * 沉浸式全屏状态下，显示导航栏和状态栏
     */
    public static void showBottomNav1(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(0);
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    /**
     * 获得一个UUID
     *
     * @return String UUID
     */
    public static String getUUID() {
        String uuid = UUID.randomUUID().toString();
        //去掉“-”符号
        return uuid.replaceAll("-", "");
    }

    public static String replaceAllStr(String str) {
        try {
            return str.replaceAll(",", " ");
        } catch (Exception e) {
            return "";
        }
    }

    public static String getCPUFramework() {

        String cpuFramework = "";
        try {
            String[] abis = new String[]{};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                abis = Build.SUPPORTED_ABIS;
            } else {
                abis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
            }
            StringBuilder abiStr = new StringBuilder();
            for (String abi : abis) {
                abiStr.append(abi);
                abiStr.append(',');
            }
            cpuFramework = abiStr.toString();
        } catch (Exception e) {
            cpuFramework = "";
            e.printStackTrace();
        }
        return cpuFramework;
    }

    /**
     * 阿里云直播需要的时间戳
     *
     * @return
     * @throws ParseException
     */
    public static String dateToStamp() {
        Long time = System.currentTimeMillis();
        time += 30 * 1000 * 60;
        String res = "";
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = simpleDateFormat.parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time));
            long ts = date.getTime();
            ts = ts / 1000;
            res = String.valueOf(ts);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

}
