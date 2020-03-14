package com.zzz.wifiview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;
import java.io.DataOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FileActivity extends Activity {
    
    ArrayList<File> fileList;
    Context context = this;
    PopupMenu popup;
    
    String backupPath; // 添加备份文件目录
    String backupParentPath; // /AppData/files/Backup
    String wifiPath; // /data/misc/wifi/xxx.xml
    
    int num; // 计数菌
    
    /** 设定时间格式 */
    java.util.Date utilDate = new java.util.Date();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        
        /** 设置备份目录 */
        backupParentPath = context.getExternalFilesDir("Backup").getPath();
        
        /** 设置 WiFi 密码文件路径 */
        if (android.os.Build.VERSION.SDK_INT >= 26) wifiPath = "/data/misc/wifi/WifiConfigStore.xml"; else wifiPath = "/data/misc/wifi/wpa_supplicant.conf";
        
        /** 获取备份文件列表 */
        fileList = new ArrayList<File>();
        getAllFiles(new File(backupParentPath));
        List<String> data = new ArrayList<String>();
        for(int i=0; i<fileList.size(); i++) {
            String name = fileList.get(i).toString().substring(fileList.get(i).toString().lastIndexOf("/")+1,fileList.get(i).toString().length());
            data.add(name);
        }
        final String[] strings = new String[data.size()];
        data.toArray(strings);
        num = data.size(); // 备份条数
        
        
        final ListView lv = findViewById(R.id.lv);
        ListAdapter adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strings);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                // Toast.makeText(FileActivity.this, backupParentPath + "/" + strings[position], 0).show();
                popup = new PopupMenu(FileActivity.this, view);
                getMenuInflater().inflate(R.menu.file,popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()) {
                            case R.id.menu_recovery: // 时光穿梭
                                showRestoreDialog(backupParentPath + "/" + strings[position]);
                                break;
                            case R.id.menu_read: // 读取备份
                                Intent intent1 = new Intent();
                                intent1.setClass(FileActivity.this, ViewActivity.class);
                                intent1.putExtra("name", strings[position]);
                                intent1.putExtra("path", backupParentPath + "/" + strings[position]);
                                intent1.putExtra("read", true);
                                startActivity(intent1); // 此时 ViewActivity 的主菜单不再显示而是替换成文字返回
                                break;
                            case R.id.menu_delete1: // 删除备份
                                cmd("rm -f " + backupParentPath + "/" + strings[position]);
                                
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
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
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0,0,0,"添加备份");
        menu.add(0,1,0,"Notice");
        menu.getItem(1).setEnabled(false);
        menu.getItem(1).setTitle("共 " + num + " 条备份");
        return true;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0: // 添加备份
                showBackupDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    
    private void showBackupDialog() {
        final EditText et = new EditText(this);
        AlertDialog.Builder BackupDialog = new AlertDialog.Builder(this);
        BackupDialog
            .setTitle("备份名称")
            .setView(et);
        
        et.setText(formatter.format(utilDate));
        et.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        
        BackupDialog
            .setPositiveButton("开始",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,int which) {
                    backupPath = backupParentPath + "/" + et.getText().toString();
                    
                    cmd("cp -f " + wifiPath + " " + backupPath);
                    
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
            })
            .setNegativeButton("取消", null)
            /*.setNeutralButton("发送",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,int which) {
                    backupPath = backupParentPath + "/" + et.getText().toString();
                    
                    cmd("cp -f " + wifiPath + " " + backupPath);
                    
                    File sendFile = new File(backupPath);
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(sendFile));
                    *///share.setType("*/*");
                    /*startActivity(Intent.createChooser(share, "发送"));
                }
            })*/
        .show();
    }
    
    
    private void showRestoreDialog(final String path) {
        AlertDialog.Builder RestoreDialog = new AlertDialog.Builder(this);
        RestoreDialog
            .setTitle("警告")
            .setMessage("此操作将会替换设备的WiFi密码文件。是否继续？\n\n" + "当前文件：\n" + path)
            .setPositiveButton("继续",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,int which) {
                    cmd("cp -f " + path + " " + wifiPath);
                    cmd("chmod 660 " + wifiPath);
                    finish();
                    startActivity(new Intent().setClassName("com.zzz.wifiview","com.zzz.wifiview.ViewActivity"));
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    
    /** 获取文件列表 */
    private void getAllFiles(File root) {
        File files[] = root.listFiles();
        if(files != null)
        for(File f:files) {
             if(f.isDirectory()) {
                getAllFiles(f);
            } else {
                this.fileList.add(f);
            }
        }
    }
    
    
    /** 执行命令 */
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
            Toast.makeText(this, "FAcmd: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "FAcmdF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
    
    
}
