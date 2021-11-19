/** 佛祖保佑，永无bug🙏🙏🙏 */
package com.zzz.wifiview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import java.io.DataOutputStream;

public class MainActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_DeviceDefault); // 暗色主题
        
        // 检测是否获取root，有则启动ViewActivity
        if (isRoot()) {
            startActivity(new Intent().setClassName("com.zzz.wifiview","com.zzz.wifiview.ViewActivity"));
			finish();
        } else {
            showNoROOTDialog();
        }
    }
    
    
    private void showNoROOTDialog() {
        AlertDialog.Builder noROOTDialog = new AlertDialog.Builder(this);
        noROOTDialog.setTitle("无法获取 ROOT 权限");
        noROOTDialog.setCancelable(false);
        noROOTDialog.setMessage("需要 ROOT 权限以访问 WiFi 密码数据。");
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
