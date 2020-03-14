/** 佛祖保佑，永无bug🙏🙏🙏 */
package com.zzz.wifiview;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadFile {
    
    int mode = 0;
    
    ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
    
    public ReadFile(String path, int i) {
        mode = i;
        
        Process process = null;
        String s = "";
        DataOutputStream os = null;
        BufferedReader br = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("cat " + path + "\n");
            os.writeBytes("exit\n");
            os.flush();
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                s += line.trim() + "\n";
            }
        } catch (IOException e) {
            exceptionCatch("ReadFile: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (br != null) {
                    br.close();
                }
                process.destroy();
            } catch (Exception e) {
                exceptionCatch("ReadFile, finally: " + e.getMessage());
                e.printStackTrace();
            }
        }
        scan(s);
    }
    
    
    public void exceptionCatch(String strcontent) {
        // writeTxtToFile(formatter.format(utilDate) + " : " + "58");
        String path = "/storage/emulated/0/Android/data/com.zzz.wifiview/debug/debug.txt";
        // System.currentTimeMillis();
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(path);
            if (!file.exists()) {
                Log.e("TestFile", "Create the file:" + path);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile rf = new RandomAccessFile(file,"rwd");
            rf.seek(file.length());
            rf.write(strContent.getBytes());
            rf.close();
        } catch (Exception e){
            Log.e("TestFile", "Error on write File:"+e);
        }
    }
    
    
    private void scan(String s) {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            Pattern pattern = Pattern.compile("<Network>\\n([\\s\\S]+?)\\n\\</Network>");
            Matcher matcher = pattern.matcher(s);
            while (matcher.find()) {
                addOreo(matcher.group());
            }
        } else {
            Pattern pattern1 = Pattern.compile("network=\\{\\n([\\s\\S]+?)\\n\\}");
            Matcher matcher1 = pattern1.matcher(s);
            while (matcher1.find()) {
                add(matcher1.group());
            }
        }
    }
    
    
    private void add(String s) {
        exceptionCatch(s);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("view", s); // 源信息
        
        String[] list = s.split("\\n");
        
        String ssid = "";
        String psk = "";
        String key_mgmt = "";
        
        // Get value
        // 找出 ssid 和 key_mgmt
        for (String info : list) {
            if (info.contains("=")) {
                int index = info.indexOf("=");
                if (info.substring(0, index).contains("ssid")) {
                    if (!info.substring(0, index).contains("scan_ssid")) {
                        ssid = info.substring(index+1, info.length());
                        if (ssid.contains("\"")) {
                            ssid = ssid.substring(1, ssid.length()-1);
                        } else {
                            ssid = toUTF8(ssid);
                        }
                    }
                } else if (info.substring(0, index).contains("key_mgmt")) {
                    key_mgmt = info.substring(index+1, info.length());
                }
            }
        }
        
        if (mode == 1) { // WPA-PSK 模式
            if (key_mgmt.contains("WPA-PSK")) {
                // 找出 psk
                for (String info : list) {
                    if (info.contains("=")) {
                        int index = info.indexOf("=");
                        if (info.substring(0, index).contains("psk")) {
                            psk = info.substring(index+2, info.length()-1);
                        }
                    }
                }
                
                map.put("ssid", ssid);
                map.put("psk", psk);
                
            }
        } else if (mode == 2) { // 非 WPA-PSK 模式
            if (!key_mgmt.contains("WPA-PSK")) {
                map.put("ssid", ssid);
                map.put("psk", key_mgmt);
            }
        }
        
        this.list.add(map);
    }
    
    
    private void addOreo(String s) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("view", s);
        
        String[] list = s.split("\\n");
        
        String ssid = "";
        String psk = "";
        String key_mgmt = "";
        
        // Get value
        
        for (String info : list) {
            if (info.contains("name=\"SSID\">")) {
                ssid = info.substring(info.indexOf("&quot;")+6, info.lastIndexOf("&quot;"));
            } else if (info.contains("name=\"ConfigKey\">")) {
                key_mgmt = info.substring(info.indexOf("&quot;")+6, info.length());
                key_mgmt = key_mgmt.substring(key_mgmt.indexOf("&quot;")+6, key_mgmt.lastIndexOf("</string>"));
                key_mgmt = key_mgmt.replace("-", "");
            }
        }
        
        if (mode == 1) {
            if (key_mgmt.contains("WPA_PSK")) {
                for (String info : list) {
                    if (info.contains("name=\"PreSharedKey\">")) {
                        psk = info.substring(info.indexOf("&quot;")+6, info.lastIndexOf("&quot;"));
                    }
                }
                
                map.put("ssid", ssid);
                map.put("psk", psk);
                
            }
        } else if (mode == 2) {
            if (!key_mgmt.contains("WPA_PSK")) {
                map.put("ssid", ssid);
                map.put("psk", key_mgmt);
            }
        }
        
        this.list.add(map);
    }
    
    
    public ArrayList<Map<String, String>> getList(Context context) {
        ArrayList<Map<String, String>> m = new ArrayList<Map<String, String>>();
        for (Map<String, String> map : this.list) {
            if (map.containsKey("ssid") && map.containsKey("psk")) m.add(map);
        }
        return this.sorting(m, context);
    }
    
    
    private ArrayList<Map<String, String>> sorting(ArrayList<Map<String, String>> lv, Context context) {
        if (lv.size() < 1) return lv;
        Collections.sort(lv, new sort(getSSID(context)));
        return lv;
    }
    
    
    private String getSSID(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) return ""; // 关闭 WiFi 时
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            
            // 判断有无引号
            if (ssid.contains("\"")) {
                ssid = ssid.substring(1, ssid.length()-1);
            } else {
                ssid = toUTF8(ssid);
            }
            return ssid;
        } catch (Exception e) {
            exceptionCatch("getCurrentWiFi: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }
    
    
    private static String toUTF8(String s) {
        if (s == null || s.equals("")) return null;
        
        try {
            s = s.toUpperCase();
            int total = s.length() / 2;
            int pos = 0;
            byte[] buffer = new byte[total];
            for (int i = 0; i < total; i++) {
                int start = i * 2;
                buffer[i] = (byte) Integer.parseInt(s.substring(start, start + 2), 16);
                pos++;
            }
            return new String(buffer, 0, pos, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
}


class sort implements Comparator<Map<String, String>> {
    
    private String current;
    
    
    public sort(String current) {
        this.current = current;
    }
    
    
    @Override
    public int compare(Map<String, String> t1, Map<String, String> t2) {
        String s1 = t1.get("ssid");
        String s2 = t2.get("ssid");
        if (s1.equals(s2)) return 0;
        if (s1.equals(current)) return -1;
        if (s2.equals(current)) return 1;
        return s1.compareToIgnoreCase(s2);
    }
    
    
}
