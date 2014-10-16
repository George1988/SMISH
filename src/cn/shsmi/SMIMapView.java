package cn.shsmi;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;

import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.runtime.ArcGISRuntime;

public class SMIMapView extends MapView {
	
	public SMIMapView(Context context) {
		super(context);
		//ע��ArcGIS
        ArcGISRuntime.setClientId("NyRku1gZ5K5voV0I");
	}

	public SMIMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//ע��ArcGIS
        ArcGISRuntime.setClientId("NyRku1gZ5K5voV0I");
	}

	public SMIMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//ע��ArcGIS
        ArcGISRuntime.setClientId("NyRku1gZ5K5voV0I");
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
	 * @return ����ֵΪ��ͼͼ�������ֵ���������ʾ����ʧ��
	 */
	public long LoadBaseMap(String map_resource_path) {
		try {
			CZTiledLayer baseLayer = new CZTiledLayer(map_resource_path);
			this.addLayer(baseLayer);
			return baseLayer.getID();
		} catch (Exception e) {
			return -1;
		}
	}
	
	/**
	 * ����AcrGIS���ߵ�ͼ����
	 * @param map_resource_url AcrGIS��ͼ������ַ
	 * @param map_resource_type ArcGIS��̬��ͼ����ȡֵΪ��DYNAMIC������Ƭ��ͼ����ȡֵΪ��TILED��
	 * @return ����ֵΪ��ͼ���������ֵ���������ʾ����ʧ��
	 */
	public long LoadOnlineMap(String map_resource_url, MapResourceType map_resource_type) {
		try {
			Layer mapLayer = null;
			switch (map_resource_type) {
			case DYNAMIC:
				mapLayer = new ArcGISDynamicMapServiceLayer(map_resource_url);
				this.addLayer(mapLayer);
				return mapLayer.getID();
			case TILED:
				mapLayer = new ArcGISTiledMapServiceLayer(map_resource_url);
				this.addLayer(mapLayer);
				return mapLayer.getID();
			default:
				return -1;
			}
		} catch (Exception e) {
			return -1;
		}
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
	
	

}