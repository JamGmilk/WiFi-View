package com.zzz.wifiview;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map;

public class ViewActivity extends Activity {
    ArrayList<Map<String, String>> mainList;
    PopupMenu popup;
    Context context = this;
    String backupPath; // 备份目录 (/AppData/files/Backup/AutoBackup)
    String sPath; // 源文件
    boolean isRead = false; // 读取备份模式
    boolean isMore = false; // 显示更多 WiFi
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        
        // 缓存目录(/AppData/cache/bk)
        //bkPath = this.getExternalCacheDir().getPath() + "/bk";
        
        // 备份目录 (/AppData/files/Backup/AutoBackup)
        backupPath = this.getExternalFilesDir("Backup").getPath() + "/AutoBackup";
        
        /* 获取启动来源 */
        try {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            isRead = bundle.getBoolean("read");
            isMore = bundle.getBoolean("more");
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(this, "VAoC: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        
        start();
    }
    
    
    private void start() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        
        /* 判断启动来源 */
        if (isRead) { // 读取备份模式
            setTitle(bundle.getString("name"));
            sPath = bundle.getString("path");
            mainList = get(sPath, 1);
        } else if (isMore) { // 显示更多 WiFi
            setTitle("更多 WiFi");
            sPath = bundle.getString("path");
            mainList = get(sPath, 2);
        } else { // 读取系统文件
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                if (android.os.Build.VERSION.SDK_INT >= 30) {
                sPath = "/data/misc/apexdata/com.android.wifi/WifiConfigStore.xml";
                } else {
                    sPath = "/data/misc/wifi/WifiConfigStore.xml";
                }
            } else {
                sPath = "/data/misc/wifi/wpa_supplicant.conf";
            }
            mainList = get(sPath, 1);
        }
        
        
        if (mainList == null) {
            Toast.makeText(ViewActivity.this, "获取列表失败", Toast.LENGTH_LONG).show();
        } else {
            if (mainList.size() == 0) {
                Toast.makeText(ViewActivity.this, "列表为空", Toast.LENGTH_LONG).show();
            } else {
                final ListView lv = findViewById(R.id.lv);
                lv.setAdapter(new WiFiAdapter(this, mainList));
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        popup = new PopupMenu(ViewActivity.this, view);
                        
                        if (isMore) {
                            getMenuInflater().inflate(R.menu.more, popup.getMenu());
                        } else {
                            getMenuInflater().inflate(R.menu.copy, popup.getMenu());
                        }
                        
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                final ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                switch(item.getItemId()) {
                                    case R.id.menu_ssid:
                                        cm.setPrimaryClip(ClipData.newPlainText(null, mainList.get(position).get("ssid")));
                                        Toast.makeText(ViewActivity.this, "SSID已复制", Toast.LENGTH_SHORT).show();
                                        break;
                                    case R.id.menu_password:
                                        cm.setPrimaryClip(ClipData.newPlainText(null, mainList.get(position).get("psk")));
                                        Toast.makeText(ViewActivity.this, "密码已复制", Toast.LENGTH_SHORT).show();
                                        break;
                                    case R.id.menu_all:
                                        Map<String, String> s = mainList.get(position);
                                        cm.setPrimaryClip(ClipData.newPlainText(null, "SSID: " + s.get("ssid") + "\n" + "密码: " + s.get("psk")));
                                        Toast.makeText(ViewActivity.this, "SSID和密码都已复制", Toast.LENGTH_SHORT).show();
                                        break;
                                    case R.id.menu_view:
                                        AlertDialog.Builder TextDialog = new AlertDialog.Builder(ViewActivity.this);
                                        TextDialog.setTitle("源信息浏览").setMessage(mainList.get(position).get("view")).setPositiveButton("关闭",null).setNeutralButton("复制", new DialogInterface.OnClickListener(){@Override public void onClick(DialogInterface dialog, int which) {
                                            cm.setPrimaryClip(ClipData.newPlainText(null, mainList.get(position).get("view")));
                                            }}).show();
                                        break;
                                    case R.id.menu_delete:
                                        delete(mainList.get(position).get("view"));
                                        cmd("cp -f " + backupPath + " " + sPath);
                                        if (!isRead) cmd("chmod 660 " + sPath);
                                        Toast.makeText(ViewActivity.this, "已删除", Toast.LENGTH_SHORT).show();
                                        
                                        mainList.clear();
                                        start();
                                        
                                        break;
                                    default:
                                        return false;
                                }
                                return true;
                            }
                        });
                        popup.show();
                    }
                });
            }
        }
    
    }
    
    
    public ArrayList<Map<String, String>> get(String path, int i) {
        try {
            ReadFile file = new ReadFile(path, i);
            return file.getList(this.context);
        } catch (Exception e) {
            Toast.makeText(this, "VAget: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }
    
    
    @Override
    protected void onStart() {
        super.onStart();
        if (isMore) {
            ActionBar actionBar = this.getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0,0,0,"刷新列表");
        menu.add(0,1,0,"显示更多 WiFi");
        menu.add(0,2,0,"打开 WiFi 设置");
        menu.add(0,3,0,"备份与恢复");
        menu.add(0,4,0,"关于");
        menu.add(0,5,0,"获取列表出错");
        menu.getItem(5).setEnabled(false);
        if (mainList != null) menu.getItem(5).setTitle("共 " + mainList.size() + " 条WiFi");
        if (isRead || isMore) {
            menu.getItem(2).setVisible(false);
            menu.getItem(3).setVisible(false);
            menu.getItem(4).setVisible(false);
        }
        if (isRead) {
            menu.getItem(0).setVisible(false);
            menu.add(0,6,0,"返回");
        }
        if (isMore) menu.getItem(1).setVisible(false);
        return true;
    }
    
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mainList != null) menu.getItem(5).setTitle("共 " + mainList.size() + " 条WiFi");
        return super.onPrepareOptionsMenu(menu);
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0: // 刷新列表
                mainList.clear();
                start();
                Toast.makeText(this, "刷新成功", Toast.LENGTH_SHORT).show();
                return true;
            case 1: // 显示更多 WiFi
                Intent intent = new Intent();
                intent.setClass(ViewActivity.this, ViewActivity.class);
                intent.putExtra("path", sPath);
                intent.putExtra("more", true);
                startActivity(intent);
                return true;
            case 2: // 打开 WiFi 设置
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                return true;
            case 3: // 备份与恢复
                startActivity(new Intent().setClassName("com.zzz.wifiview", "com.zzz.wifiview.FileActivity"));
                return true;
            case 4: // 关于
                showAboutDialog();
                return true;
            case 6: // 返回
                finish();
                return true;
            case android.R.id.home: // 左上角返回
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    
    private void delete(String ss) {
        String s = "";
        Process process = null;
        DataOutputStream os = null;
        BufferedReader br = null;
        OutputStreamWriter pw = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("cat " + sPath + "\n");
            os.writeBytes("exit\n");
            os.flush();
            
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                s += line.trim() + "\n";
            }
            pw = new OutputStreamWriter(new FileOutputStream(backupPath), "UTF-8");
            s = s.replace(ss,"");
            pw.write(s);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(ViewActivity.this, "VAdelete: " + e, Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (br != null) {
                    br.close();
                }
                if (pw != null) {
                    pw.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "VAdeleteF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
    
    
    private void showAboutDialog() {
        final String[] items = {"版本: 12", "应用信息", "置顶当前连接的WiFi", "开放源代码", "Copyright © 2016 - 2021 JamGmilk."};
        AlertDialog.Builder AboutDialog = new AlertDialog.Builder(this)
            .setTitle("WiFi View")
            .setIcon(com.zzz.wifiview.R.drawable.ic)
            .setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            Toast.makeText(ViewActivity.this, "版本代号: 40", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            Intent intent = new Intent();
                            intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:com.zzz.wifiview"));
                            startActivity(intent);
                            break;
                        case 2:
                            showPermissionsDialog();
                            break;
                        case 3:
                            Uri uri2 = Uri.parse("https://github.com/JamGmilk/WiFi-View");
                            Intent intent2 = new Intent(Intent.ACTION_VIEW, uri2);
                            startActivity(intent2);
                            break;
                    }
                }
            });
        AboutDialog.create().show();
    }

    
    private void showPermissionsDialog() {
        AlertDialog.Builder permissionsDialog = new AlertDialog.Builder(this);
        permissionsDialog.setTitle("置顶当前连接WiFi");
        permissionsDialog.setMessage("在 Android 8.0 以后，软件扫描 WLAN 需要满足以下所有条件：\n① 授予软件精确位置权限；\n② 设备已启用位置服务。\n\n仅用于置顶当前连接的WiFi。");
        permissionsDialog.setPositiveButton("前往授权",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:com.zzz.wifiview"));
                startActivity(new Intent().setClassName("com.zzz.wifiview", "com.zzz.wifiview.ViewActivity"));
                startActivity(intent);
                finish();
            }
        });
        permissionsDialog.setNegativeButton("关闭", null);
        permissionsDialog.show();
    }
    
    public void cmd(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "VAcmd: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "VAcmdF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
    
    
}
