package com.example.downloadservice;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Utils {
    public static boolean isApkDebugable(Context context) {
        try {
            ApplicationInfo info= context.getApplicationInfo();
            return (info.flags&ApplicationInfo.FLAG_DEBUGGABLE)!=0;
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 只有在调试模式时打印日志
     * @param context
     * @param msg
     */
    public static void log(Context context,String msg){
        if(isApkDebugable(context)){
            Log.d("ddebug",msg);
        }
    }
    public static void log(String msg){
        Log.d("ddebug",msg);
    }

    /**
     * 获取进程名
     * @param context
     * @return
     */
    public static String getCurProcessName(Context context) {
        int pid = android.os.Process. myPid ();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context. ACTIVITY_SERVICE );
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    /**
     * 获取应用版本号
     * @param context
     * @return
     * @throws Exception
     */
    public static String getVersionName(Context context) throws Exception {
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
        String version = packInfo.versionName;
        int versionCode = packInfo.versionCode;
        return version.trim();
    }
    /**
     * 获取当前手机系统版本号
     *
     * @return  系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    public static Dialog showLoading(Activity context){
        if (context == null || context.isFinishing() || context.isDestroyed()) {
            return null;
        }
        //Dialog dialog = new Dialog(context,R.style.dialog_style);
        LayoutInflater inflater = LayoutInflater.from(context);
        //加载loading_dialog.xml
        View v = inflater.inflate(R.layout.layout_dialog_loading, null);// 得到加载view

        // loading_dialog.xml中的LinearLayout
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局

        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        // 加载动画load_animation.xml
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.load_animation);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(anim);

        // 创建自定义样式loading_dialog
        Dialog loadingDialog = new Dialog(context, R.style.dialog_style);
        loadingDialog.setCancelable(false);// 不可以用“返回键”取消
        // 设置布局
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT));
        loadingDialog.show();
        return loadingDialog;
    }
    /**
     * 弹出单按钮的弹窗
     * @return
     */
    public static Dialog showSingleButtonDialog(Context context,String msg, View.OnClickListener confirmCallback){

        Dialog dialog = new Dialog(context,R.style.dialog_style);
        dialog.setContentView(R.layout.layout_dialog_single_button);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        TextView msgTv =dialog.findViewById(R.id.tv_msg);
        msgTv.setText(msg);
        View confirm = dialog.findViewById(R.id.btn_confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(confirmCallback != null){
                    confirmCallback.onClick(v);
                }
            }
        });
        dialog.show();
        return dialog;
    }
}
