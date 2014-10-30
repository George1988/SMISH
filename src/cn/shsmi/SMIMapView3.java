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
	 * AcrGIS���ߵ�ͼ��������
	 * @author George
	 *
	 */
	public enum MapResourceType {
		DYNAMIC, //ArcGIS��̬��ͼ����
		TILED //��Ƭ��ͼ����
	}
	
	/**
	 * ���������ͼ
	 * @param map_resource_path ��ͼ����·��
	 * @return ����ֵΪ��ͼͼ�������ֵ���������ʾ���ʧ��
	 */
	public long LoadBaseMap(String map_resource_path) {
		return LoadBaseMap(map_resource_path, this.getLayers().length);
	}
	
	/**
	 * ���������ͼ
	 * @param map_resource_path ��ͼ����·��
	 * @param index ���ͼ�㵽ָ��������ֵ
	 * @return ����ֵΪ��ͼͼ�������ֵ���������ʾ���ʧ��
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
	 * ����AcrGIS���ߵ�ͼ����
	 * @param map_resource_url AcrGIS��ͼ������ַ
	 * @param map_resource_type ArcGIS��̬��ͼ����ȡֵΪ��DYNAMIC������Ƭ��ͼ����ȡֵΪ��TILED��
	 * @param index ���ͼ�㵽ָ��������ֵ
	 * @return ����ֵΪ��ͼ���������ֵ���������ʾ���ʧ��
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
	 * ����AcrGIS���ߵ�ͼ����
	 * @param map_resource_url AcrGIS��ͼ������ַ
	 * @param map_resource_type ArcGIS��̬��ͼ����ȡֵΪ��DYNAMIC������Ƭ��ͼ����ȡֵΪ��TILED��
	 * @return ����ֵΪ��ͼ���������ֵ���������ʾ���ʧ��
	 */
	public long LoadOnlineMap(String map_resource_url, MapResourceType map_resource_type) {
		return LoadOnlineMap(map_resource_url, map_resource_type, this.getLayers().length);
	}
	
	/**
	 * ɾ����ͼ����
	 * @param mapID Ҫɾ���ĵ�ͼ���������ֵ
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
