# WiFi View
 
快速查看与管理在 Android 设备上连过的 WiFi 的密码


### 应用特性
- 支持 Android 4.0 +
- 支持显示中文名称的 WiFi
- 支持一键复制 SSID 与密码
- 支持备份与恢复
- 支持查看 WiFi 的源信息


## 应用截图
<figure class="pic">
    <a href="/Screenshots/1.jpg"><img src="/Screenshots/1.jpg" height="25%" width="25%" /></a>
    <a href="/Screenshots/2.jpg"><img src="/Screenshots/2.jpg" height="25%" width="25%" /></a>
    <a href="/Screenshots/3.jpg"><img src="/Screenshots/3.jpg" height="25%" width="25%" /></a>
    <a href="/Screenshots/4.jpg"><img src="/Screenshots/4.jpg" height="25%" width="25%" /></a>
    <a href="/Screenshots/5.jpg"><img src="/Screenshots/5.jpg" height="25%" width="25%" /></a>
    <a href="/Screenshots/6.jpg"><img src="/Screenshots/6.jpg" height="25%" width="25%" /></a>
    <a href="/Screenshots/7.jpg"><img src="/Screenshots/7.jpg" height="25%" width="25%" /></a>
    <a href="/Screenshots/8.jpg"><img src="/Screenshots/8.jpg" height="25%" width="25%" /></a>
    <a href="/Screenshots/9.jpg"><img src="/Screenshots/9.jpg" height="25%" width="25%" /></a>
    <a href="/Screenshots/10.jpg"><img src="/Screenshots/10.jpg" height="25%" width="25%" /></a>

## 历代更新日志
[CHANGELOG.md](CHANGELOG.md)


## 下载
- [GitHub](https://github.com/JamGmilk/WiFi-View/releases)
- [酷安](https://www.coolapk.com/apk/com.zzz.wifiview)


## 隐私权限说明
### ACCESS_SUPERUSER:
使用 ROOT 权限读写 WiFi 密码配置文件。  
 
### ACCESS_WIFI_STATE:
扫描 WLAN ，用于置顶当前连接的 WiFi 。  
 
### ACCESS_FINE_LOCATION:
此项权限是 Android 官方要求的[[1]]。在 Android 8.0 以后，需要授予此权限才能使用 ACCESS_WIFI_STATE 权限。  
 
>软件扫描 WLAN 需要满足以下所有条件：  
> ① 授予软件精确位置权限  
> ② 设备已启用位置服务  
 
[[1]] https://developer.android.google.cn/guide/topics/connectivity/wifi-scan#wifi-scan-restrictions

[1]: <https://developer.android.google.cn/guide/topics/connectivity/wifi-scan#wifi-scan-restrictions>


## 注意

1. 手动删除的 WiFi 无法获取；

2. 部分系统可能会加密，导致软件不能正常工作；

3. 软件需要读取 /data 下的系统文件，必须授予 ROOT 权限。
 > Android 8.0 以下读取 /data/misc/wifi/wpa_supplicant.conf<br>
 > Android 8~11 读取 /data/misc/wifi/WifiConfigStore.xml<br>
 > Android 11 以上读取 /data/misc/apexdata/com.android.wifi/WifiConfigStore.xml

## License
```
Copyright (c) 2021 JamGmilk

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

*Copyright &#169; 2016-2021 JamGmilk. All rights reserved.*
