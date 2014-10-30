package cn.shsmi;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.runtime.ArcGISRuntime;

public class SMIMapView3 extends MapView {
	
	WeakReference<? extends ViewGroup> mmmvvvReference;
	
	public SMIMapView3(Context context, SMIMapView2 mapView2) {
		super(context);
        mmmvvvReference = new WeakReference<SMIMapView2>(mapView2);
	}

	public SMIMapView3(Context context, AttributeSet attrs, SMIMapView2 mapView2) {
		super(context, attrs);
		mmmvvvReference = new WeakReference<SMIMapView2>(mapView2);
	}

	public SMIMapView3(Context context, AttributeSet attrs, int defStyle, SMIMapView2 mapView2) {
		super(context, attrs, defStyle);
        mmmvvvReference = new WeakReference<SMIMapView2>(mapView2);
	}
	
	/**
	 * AcrGIS在线地图服务类型
	 * @author George
	 *
	 */
	public enum MapResourceType {
		DYNAMIC, //ArcGIS动态地图服务
		TILED //切片地图服务
	}
	
	/**
	 * 载入政务地图
	 * @param map_resource_path 底图数据路径
	 * @return 返回值为底图图层的索引值，负数则表示添加失败
	 */
	public long LoadBaseMap(String map_resource_path) {
		return LoadBaseMap(map_resource_path, this.getLayers().length);
	}
	
	/**
	 * 载入政务地图
	 * @param map_resource_path 底图数据路径
	 * @param index 添加图层到指定的索引值
	 * @return 返回值为底图图层的索引值，负数则表示添加失败
	 */
	public long LoadBaseMap(String map_resource_path, int index) {
		try {
			CZTiledLayer baseLayer = new CZTiledLayer(map_resource_path);
			this.addLayer(baseLayer, index);
			return baseLayer.getID();
		} catch (Exception e) {
			return -1;
		}
	}
	
	/**
	 * 载入AcrGIS在线地图服务
	 * @param map_resource_url AcrGIS地图服务网址
	 * @param map_resource_type ArcGIS动态地图服务取值为（DYNAMIC）；切片地图服务取值为（TILED）
	 * @param index 添加图层到指定的索引值
	 * @return 返回值为地图服务的索引值，负数则表示添加失败
	 */
	public long LoadOnlineMap(String map_resource_url, MapResourceType map_resource_type, int index) {
		try {
			Layer mapLayer = null;
			switch (map_resource_type) {
			case DYNAMIC:
				mapLayer = new ArcGISDynamicMapServiceLayer(map_resource_url);
				this.addLayer(mapLayer, index);
				return mapLayer.getID();
			case TILED:
				mapLayer = new ArcGISTiledMapServiceLayer(map_resource_url);
				this.addLayer(mapLayer, index);
				return mapLayer.getID();
			default:
				return -1;
			}
		} catch (Exception e) {
			return -1;
		}
	}
	
	/**
	 * 载入AcrGIS在线地图服务
	 * @param map_resource_url AcrGIS地图服务网址
	 * @param map_resource_type ArcGIS动态地图服务取值为（DYNAMIC）；切片地图服务取值为（TILED）
	 * @return 返回值为地图服务的索引值，负数则表示添加失败
	 */
	public long LoadOnlineMap(String map_resource_url, MapResourceType map_resource_type) {
		return LoadOnlineMap(map_resource_url, map_resource_type, this.getLayers().length);
	}
	
	/**
	 * 删除地图服务
	 * @param mapID 要删除的地图服务的索引值
	 */
	public void RemoveMap(long mapID) {
		try {
			this.removeLayer(this.getLayerByID(mapID));
		} catch (Exception e) {
			
		}
	}
	
	@Override
	public void onChildViewAdded(View parent, View child) {
		super.onChildViewAdded(parent, child);
		mmmvvvReference.get().addView(child);
	}

}
