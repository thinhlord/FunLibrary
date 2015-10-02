package com.teamx.funlibrary;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.view.View;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ruler_000 on 05/04/2015.
 * Project: SoYBa
 */
public class Toolbox {

    public static String getDateTime(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    public static Date getDateTime(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            return dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static String getMonthYear(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String getMonthYear(String date) {
        return getMonthYear(getDateTime(date));
    }

    public static String formatDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            return dateFormat.format(dateFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatDate(Date date) {
        return formatDate(getDateTime(date));
    }

    public static Date addMonth(Date start, int added) {
        Calendar c = Calendar.getInstance();
        c.setTime(start);
        c.add(Calendar.MONTH, added);
        return c.getTime();
    }

    public static String addMonth(String start, int added) {
        return getDateTime(addMonth(getDateTime(start), added));
    }

    public static Date addDay(Date start, int added) {
        Calendar c = Calendar.getInstance();
        c.setTime(start);
        c.add(Calendar.DATE, added);
        return c.getTime();
    }

    public static String addDay(String start, int added) {
        return getDateTime(addDay(getDateTime(start), added));
    }

    public static int getMonth(Date start) {
        Calendar c = Calendar.getInstance();
        c.setTime(start);
        return c.get(Calendar.MONTH) + 1;
    }

    public static int getYear(Date start) {
        Calendar c = Calendar.getInstance();
        c.setTime(start);
        return c.get(Calendar.YEAR);
    }

    public static int getMonth(String start) {
        return getMonth(getDateTime(start));
    }

    public static int getYear(String start) {
        return getYear(getDateTime(start));
    }

    public static Integer differenceInMonths(Date beginningDate, Date endingDate) {
        if (beginningDate == null || endingDate == null) {
            return 0;
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(beginningDate);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(endingDate);
        return differenceInMonths(cal1, cal2);
    }

    private static Integer differenceInMonths(Calendar beginningDate, Calendar endingDate) {
        if (beginningDate == null || endingDate == null) {
            return 0;
        }
        int m1 = beginningDate.get(Calendar.YEAR) * 12 + beginningDate.get(Calendar.MONTH);
        int m2 = endingDate.get(Calendar.YEAR) * 12 + endingDate.get(Calendar.MONTH);
        return m2 - m1;
    }

    public static int differenceInDates(Date start, Date end) {
        return (int) ((end.getTime() - start.getTime()) / 86400000);
    }

    public static long differenceInDates(String start, String end) {
        return differenceInDates(getDateTime(start), getDateTime(end));
    }

    public static Date today() {
        return Calendar.getInstance().getTime();
    }

    public static long nowInSec() {
        return System.currentTimeMillis() / 1000;
    }

    public static boolean sameDay(Date d1, Date d2) {
        return formatDate(d1).equals(formatDate(d2));
    }

    public static boolean isLocationServicesTurnOn(Context context) {
        boolean gps_enabled, network_enabled;

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return gps_enabled || network_enabled;
    }

    public static void startGoToGooglePlayIntent(Context context){
        final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    public static <T> int findInArray(T[] array, T objToFind){
        int index;
        for (index = 0; index < array.length; index++){
            T obj = array[index];
            if (obj.equals(objToFind)){
                return index;
            }
        }
        return -1;
    }

    public static String getText(String string) {
        if (string == null) return "";
        if (!string.equalsIgnoreCase("null")) return string;
        else return "";
    }

    public static String toLowerCaseAndRemoveAccent(String str){
        str = str.toLowerCase();
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = str.replaceAll("[^\\p{ASCII}]", "");
        return str;
    }


    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("bluebee-uet.com"); //You can replace it with your name

            if (ipAddr.equals("")) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            return false;
        }
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static String getStackTrace(Throwable t){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    public static void toggleVisibility(View v) {
        v.setVisibility(v.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }
}
