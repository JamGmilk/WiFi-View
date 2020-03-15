/** 佛祖保佑，永无bug🙏🙏🙏 */
package com.zzz.wifiview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Map;

public class WiFiAdapter extends BaseAdapter {
    private Context context;
    ArrayList<Map<String,String>> list;
    
    public WiFiAdapter(Context context, ArrayList<Map<String,String>> list) {
        super();
        this.context = context;
        this.list = list;
    }
    
    @Override
    public int getCount() {
        return list.size();
    }
    
    @Override
    public Object getItem(int position) {
        return position;
    }
    
    @Override
    public long getItemId(int position) {
        return position;
		}
    
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = View.inflate(context, R.layout.item, null);
            holder = new ViewHolder();
            holder.ssid = (TextView) view.findViewById(R.id.title);
            holder.password = (TextView) view.findViewById(R.id.text);
            view.setTag(holder);
        }
        
        Map<String,String> map = list.get(position);
        holder.ssid.setText(map.get("ssid"));
        holder.password.setText(map.get("psk"));
        
        return view;
    }
    
    
    class ViewHolder {
        public TextView ssid;
        public TextView password;
    }
    
    
}
