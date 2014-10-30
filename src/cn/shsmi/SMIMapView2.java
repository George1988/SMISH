package cn.shsmi;

import com.esri.android.map.Layer;

import cn.shsmi.SMIMapView3.MapResourceType;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class SMIMapView2 extends ViewGroup {
	
	private SMIMapView3 real_mapView;

	public SMIMapView2(Context context) {
		super(context);
		real_mapView = new SMIMapView3(context, this);
		for (int i = 0; i < real_mapView.getChildCount(); i++) {
			this.addView(real_mapView.getChildAt(i));
		}
	}
	
	public SMIMapView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		real_mapView = new SMIMapView3(context, attrs, this);
		for (int i = 0; i < real_mapView.getChildCount(); i++) {
			this.addView(real_mapView.getChildAt(i));
		}
	}

	public SMIMapView2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		real_mapView = new SMIMapView3(context, attrs, defStyle, this);
		for (int i = 0; i < real_mapView.getChildCount(); i++) {
			this.addView(real_mapView.getChildAt(i));
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub

	}
	
	public long LoadBaseMap(String map_resource_path) {
		return real_mapView.LoadBaseMap(map_resource_path);
	}
	
	public long LoadBaseMap(String map_resource_path, int index) {
		return real_mapView.LoadBaseMap(map_resource_path, index);
	}
	
	public long LoadOnlineMap(String map_resource_url, MapResourceType map_resource_type, int index) {
		return real_mapView.LoadOnlineMap(map_resource_url, map_resource_type, index);
	}

	public long LoadOnlineMap(String map_resource_url, MapResourceType map_resource_type) {
		return real_mapView.LoadOnlineMap(map_resource_url, map_resource_type);
	}
	
	public void RemoveMap(long mapID) {
		real_mapView.RemoveMap(mapID);
	}
	
	public int addLayer(Layer layer) {
		return real_mapView.addLayer(layer);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
}
