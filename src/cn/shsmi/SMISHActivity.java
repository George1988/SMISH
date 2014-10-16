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
	SMIMapView mMapView;//��Ȼ
	ArrayList<Long> baseLayers;//�洢Ŀǰ�ĵ�ͼͼ��

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.mycustomtitle);
		
        //��ȡMapView
        mMapView = (SMIMapView) findViewById(R.id.map);
        //��ʼ��baseLayers
        baseLayers = new ArrayList<Long>();
		
        //2.���������л���Switch�ؼ�������¼�����
        Switch offlineSwitch = (Switch)findViewById(R.id.switchOfflineAndOnline);
		offlineSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){          
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                onlineOrOffline(isChecked);
            }
        });
		
		//3.����¼�Spinner�¼�����,���ڸ�����ͼ
		Spinner layerControlSpinner = (Spinner)findViewById(R.id.layerControl);
		layerControlSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {	
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				layerControlChanged(((Switch)findViewById(R.id.switchOfflineAndOnline)).isChecked());
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}	
		});
				
		//4.��һ�μ��أ�ˢ����ʾ
		onlineOrOffline(offlineSwitch.isChecked());	
		
		//5.�༭����Switch�ؼ�������¼�����
        Switch editSwitch = (Switch)findViewById(R.id.switchEditing);
        editSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){          
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
            	Spinner editTypeSpinner = (Spinner)findViewById(R.id.editType);
            	editTypeSpinner.setEnabled(!isChecked);
            }
        });
        
    }
    
    //���������л���ˢ��layerControlSpinner���б�
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
    
    //ˢ��MapView��ʾ
    private void layerControlChanged(boolean isOnline) {
    	Spinner spinner = (Spinner)findViewById(R.id.layerControl);
    	String layerName = (String)spinner.getSelectedItem();
    	String[] urls = null;
    	
    	//Ŀǰû�кð취����ʱ��ö�ٰ�
    	if (layerName.compareTo("��ͼ") == 0) {
			urls = getResources().getStringArray(R.array.baseMapURLs);
		} else if (layerName.compareTo("3D��ͼ") == 0) {
			urls = getResources().getStringArray(R.array.base3DMapURLs);
		} else if (layerName.compareTo("Ӱ��") == 0) {
			urls = getResources().getStringArray(R.array.imageMapURLs);
		} else if (layerName.compareTo("1948��Ӱ��") == 0) {
			urls = getResources().getStringArray(R.array.image1948MapURLs);
		} else if (layerName.compareTo("1979��Ӱ��") == 0) {
			urls = getResources().getStringArray(R.array.image1979MapURLs);
		} else if (layerName.compareTo("�������") == 0) {
			urls = getResources().getStringArray(R.array.offlineScaleMapURLs);
		} else if (layerName.compareTo("����Ӱ��") == 0) {
			urls = getResources().getStringArray(R.array.offlineImageMapURLs);
		}
    	
    	//ȥ����ͼ��
    	for (long baseLayer : baseLayers) {
			mMapView.RemoveMap(baseLayer);
		}
    	baseLayers = new ArrayList<Long>();
    	
    	//����
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
    
    //�����¼�
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