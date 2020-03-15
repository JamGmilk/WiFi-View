/** 佛祖保佑，永无bug🙏🙏🙏 */
package com.zzz.wifiview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import java.io.DataOutputStream;

public class MainActivity extends Activity {
    
    SharedPreferences sharedPreferences;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_DeviceDefault); // 暗色主题
        
        // 检测是否获取root，如果得到root就启动ViewActivity
        if (isRoot()) {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                sharedPreferences = this.getSharedPreferences("PCGJG", MODE_PRIVATE);
                boolean isRequested = sharedPreferences.getBoolean("isRequested", false);
                if (!isRequested){
                    showPermissionsDialog();
                } else {
                    startActivity(new Intent().setClassName("com.zzz.wifiview","com.zzz.wifiview.ViewActivity"));
                    finish();
                }
            } else {
                startActivity(new Intent().setClassName("com.zzz.wifiview","com.zzz.wifiview.ViewActivity"));
                finish();
            }
        } else {
            showNoROOTDialog();
        }
    }
    
    
    private void showPermissionsDialog() {
        AlertDialog.Builder permissionsDialog = new AlertDialog.Builder(this);
        permissionsDialog.setTitle("关于置顶当前连接 WiFi 的说明");
        permissionsDialog.setCancelable(false);
        permissionsDialog.setMessage("置顶当前连接的 WiFi ，软件需要 WiFi 状态权限，用于扫描 WLAN 。\n\n另外，Android 更新了隐私权。在 Android 8.0 以后，软件扫描 WLAN 需要满足以下所有条件：\n① 授予软件精确位置权限；\n② 设备已启用位置服务。\n\n以上不会影响软件的正常使用，只是为了能够快速找到 WiFi 。");
        permissionsDialog.setPositiveButton("前往授权",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:com.zzz.wifiview"));
                startActivity(new Intent().setClassName("com.zzz.wifiview","com.zzz.wifiview.ViewActivity"));
                startActivity(intent);
                finish();
            }
        });
        //noROOTDialog.setNegativeButton("退出", null);
        permissionsDialog.setNeutralButton("不再提醒",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,int which) {
                Editor editor = sharedPreferences.edit();
                editor.putBoolean("isRequested", true);
                editor.commit();
                startActivity(new Intent().setClassName("com.zzz.wifiview","com.zzz.wifiview.ViewActivity"));
                finish();
            }
        });
        permissionsDialog.show();
    }
    
    
    private void showNoROOTDialog() {
        AlertDialog.Builder noROOTDialog = new AlertDialog.Builder(this);
        noROOTDialog.setTitle("无法获取 ROOT 权限");
        noROOTDialog.setCancelable(false);
        noROOTDialog.setMessage("WiFi View 未能获取 ROOT 权限，你无法使用本软件来查看当前设备中的 WiFi 密码信息！");
        noROOTDialog.setPositiveButton("关闭",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,int which) {
                finish();
            }
        });
        //noROOTDialog.setNegativeButton("退出", null);
        noROOTDialog.setNeutralButton("卸载",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,int which) {
                Uri uri = Uri.parse("package:com.zzz.wifiview");
                Intent intent = new Intent(Intent.ACTION_DELETE, uri);
                startActivity(intent);
                finish();
            }
        });
        noROOTDialog.show();
    }
    
    
    /** 判断应用是否被授予root权限 */
    public static synchronized boolean isRoot() {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            if (exitValue == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
}
