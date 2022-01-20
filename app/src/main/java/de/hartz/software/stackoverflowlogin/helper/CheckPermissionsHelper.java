package de.hartz.software.stackoverflowlogin.helper;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;

import java.lang.reflect.Method;

/**
 * Use to check granted permission on all devices.
 *
 * https://stackoverflow.com/a/33382532/8524651
 */
public class CheckPermissionsHelper {
    @SuppressLint("NewApi")
    public static boolean canDrawOverlayViews(Context con){
        if(Build.VERSION.SDK_INT< Build.VERSION_CODES.LOLLIPOP)
            return true;
        try {
            return Settings.canDrawOverlays(con);
        }
        catch(NoSuchMethodError e){
            return canDrawOverlaysUsingReflection(con);
        }

    }

    public static boolean canDrawOverlaysUsingReflection(Context context) {
        try {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            Class clazz = AppOpsManager.class;
            Method dispatchMethod = clazz.getMethod("checkOp", new Class[] { int.class, int.class, String.class });
            //AppOpsManager.OP_SYSTEM_ALERT_WINDOW = 24
            int mode = (Integer) dispatchMethod.invoke(manager, new Object[] { 24, Binder.getCallingUid(), context.getApplicationContext().getPackageName() });
            return AppOpsManager.MODE_ALLOWED == mode;
        } catch (Exception e) {
            return false;
        }
    }
}
