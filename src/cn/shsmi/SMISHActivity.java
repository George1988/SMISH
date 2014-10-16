package cn.shsmi;

import java.util.ArrayList;
import cn.shsmi.SMIMapView.MapResourceType;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;


public class SMISHActivity extends Activity {	
	SMIMapView mMapView;//显然
	ArrayList<Long> baseLayers;//存储目前的底图图层

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.mycustomtitle);
		
        //获取MapView
        mMapView = (SMIMapView) findViewById(R.id.map);
        //初始化baseLayers
        baseLayers = new ArrayList<Long>();
		
        //2.在线离线切换的Switch控件，添加事件监听
        Switch offlineSwitch = (Switch)findViewById(R.id.switchOfflineAndOnline);
		offlineSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){          
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                onlineOrOffline(isChecked);
            }
        });
		
		//3.添加事件Spinner事件监听,用于更换底图
		Spinner layerControlSpinner = (Spinner)findViewById(R.id.layerControl);
		layerControlSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {	
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				layerControlChanged(((Switch)findViewById(R.id.switchOfflineAndOnline)).isChecked());
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}	
		});
				
		//4.第一次加载，刷新显示
		onlineOrOffline(offlineSwitch.isChecked());	
		
		//5.编辑与否的Switch控件，添加事件监听
        Switch editSwitch = (Switch)findViewById(R.id.switchEditing);
        editSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){          
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
            	Spinner editTypeSpinner = (Spinner)findViewById(R.id.editType);
            	editTypeSpinner.setEnabled(!isChecked);
            }
        });
        
    }
    
    //在线离线切换，刷新layerControlSpinner的列表
    private void onlineOrOffline(boolean isOnline) {
    	Spinner layerControlSpinner = (Spinner)findViewById(R.id.layerControl);
    	String[] layers;
		if (isOnline) {
			layers = getResources().getStringArray(R.array.onlineLayers);
		} else {
			layers = getResources().getStringArray(R.array.offlineLayers);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, layers);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		layerControlSpinner.setAdapter(adapter);
	}
    
    //刷新MapView显示
    private void layerControlChanged(boolean isOnline) {
    	Spinner spinner = (Spinner)findViewById(R.id.layerControl);
    	String layerName = (String)spinner.getSelectedItem();
    	String[] urls = null;
    	
    	//目前没有好办法，暂时用枚举吧
    	if (layerName.compareTo("底图") == 0) {
			urls = getResources().getStringArray(R.array.baseMapURLs);
		} else if (layerName.compareTo("3D地图") == 0) {
			urls = getResources().getStringArray(R.array.base3DMapURLs);
		} else if (layerName.compareTo("影像") == 0) {
			urls = getResources().getStringArray(R.array.imageMapURLs);
		} else if (layerName.compareTo("1948年影像") == 0) {
			urls = getResources().getStringArray(R.array.image1948MapURLs);
		} else if (layerName.compareTo("1979年影像") == 0) {
			urls = getResources().getStringArray(R.array.image1979MapURLs);
		} else if (layerName.compareTo("大比例尺") == 0) {
			urls = getResources().getStringArray(R.array.offlineScaleMapURLs);
		} else if (layerName.compareTo("离线影像") == 0) {
			urls = getResources().getStringArray(R.array.offlineImageMapURLs);
		}
    	
    	//去掉旧图层
    	for (long baseLayer : baseLayers) {
			mMapView.RemoveMap(baseLayer);
		}
    	baseLayers = new ArrayList<Long>();
    	
    	//加载
    	if (isOnline) {
    		for (String layerURL : urls) {
    			baseLayers.add(mMapView.LoadOnlineMap(layerURL, MapResourceType.TILED));
			}
		} else {
			for (String layerURL : urls) {
				baseLayers.add(mMapView.LoadBaseMap(layerURL));
			}
		}
    	
	}
    
    //触摸事件
    public boolean onTouchEvent(MotionEvent me) {
    	//Point touchPoint = mMapView.toMapPoint(new Point(me.getX(), me.getY()));
    	//Graphic gp = new Graphic(touchPoint, new SimpleMarkerSymbol(Color.RED, 24, com.esri.core.symbol.SimpleMarkerSymbol.STYLE.CROSS));
    	//graphLayer.addGraphic(gp);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
 }
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.pause();
 }
	@Override
	protected void onResume() {
		super.onResume();
		mMapView.unpause();
	}

}