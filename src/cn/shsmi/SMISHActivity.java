package cn.shsmi;

import com.esri.android.map.Callout;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Geometry.Type;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.shsmi.CZGraLayer.EditingMode;
import cn.shsmi.SMIMapView.MapResourceType;
import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import org.ksoap2.SoapEnvelope;  
import org.ksoap2.serialization.SoapObject;  
import org.ksoap2.serialization.SoapSerializationEnvelope;  
import org.ksoap2.transport.HttpTransportSE;


public class SMISHActivity extends Activity {	
	SMIMapView mMapView;//显然
	ArrayList<Long> baseLayers;//存储目前的底图图层
	CZGraLayer graphLayer;
	ArcGISDynamicMapServiceLayer fireHydrantsLayer;
	
	//编辑相关
	//点线面资源图片的id数组
    int[] drawableIds = {R.drawable.point, R.drawable.line, R.drawable.polygon, R.drawable.line};
    //点线面字符串的id数组
    int[] msgIds = {R.string.point, R.string.line, R.string.polygon, R.string.curve};
    //编辑状态文本
    int[] modeIds = {R.string.add, R.string.delete, R.string.update, R.string.select};

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
        //编辑layer
        graphLayer = new CZGraLayer();
        mMapView.addLayer(graphLayer);
        mMapView.setAllowRotationByPinch(true);
        //消防栓
        fireHydrantsLayer = new ArcGISDynamicMapServiceLayer("http://202.136.213.6:8399/arcgis/rest/services/Fireplug/MapServer");
        mMapView.addLayer(fireHydrantsLayer);
        
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
            	setEditingMode();
            }
        });
        
        //6.初始化编辑类型的Spinner，并添加事件监听,用于更换编辑类型 
        Spinner editTypeSpinner = (Spinner)findViewById(R.id.editType);
        editTypeSpinner.setEnabled(!editSwitch.isChecked());
        //设置
        BaseAdapter editTypeBaseAdapter = new BaseAdapter() {
			public View getView(int position, View convertView, ViewGroup parent) {
				/*
				 * *动态生成每个下拉项对应的View，每个下拉项View由LinearLayout
				 * *中包含一个ImageView及一个TextView构成
				 * */
				//a.初始化LinearLayout
				LinearLayout result = new LinearLayout(SMISHActivity.this);
				result.setOrientation(LinearLayout.HORIZONTAL);
				//b.初始化ImageView
				ImageView image = new ImageView(SMISHActivity.this);
				image.setImageDrawable(getResources().getDrawable(drawableIds[position]));//设置图片
				result.addView(image);//添加到LinearLayout中
				//c.初始化TextView
				TextView tv = new TextView(SMISHActivity.this);
				tv.setText("" + getResources().getText(msgIds[position]));//设置内容
				tv.setTextSize(18);
				result.addView(tv);//添加到LinearLayout中
				return result;
			}
			
			public long getItemId(int position) {
				return 0;
			}
			
			public Object getItem(int position) {
				return null;
			}
			
			public int getCount() {
				return drawableIds.length;
			}
		};
		editTypeSpinner.setAdapter(editTypeBaseAdapter);
        //添加事件监听
        editTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {	
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				setEditingType();
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}	
		});
        
        //7.初始化EditMode的Spinner，并添加事件监听,用于更换EditMode 
        Spinner editModeSpinner = (Spinner)findViewById(R.id.editMode);
        editModeSpinner.setEnabled(!editSwitch.isChecked());
        //设置
        BaseAdapter editModeBaseAdapter = new BaseAdapter() {	
			public View getView(int position, View convertView, ViewGroup parent) {
				//a.初始化LinearLayout
				LinearLayout result = new LinearLayout(SMISHActivity.this);
				result.setOrientation(LinearLayout.HORIZONTAL);
				//b.初始化TextView
				TextView tv = new TextView(SMISHActivity.this);
				tv.setText("" + getResources().getText(modeIds[position]));//设置内容
				tv.setTextSize(18);
				result.addView(tv);//添加到LinearLayout中
				return result;
			}
			
			public long getItemId(int position) {
				return 0;
			}
			
			public Object getItem(int position) {
				return null;
			}
			
			public int getCount() {
				return modeIds.length;
			}
		};
		editModeSpinner.setAdapter(editModeBaseAdapter);
        //添加事件监听
        editModeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {	
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				setEditingMode();
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}	
		});
        
		//8.地图事件
		MapOnTouchListener mapOnTouchListener = new MapOnTouchListener(this, mMapView) {			
			@Override
			public boolean onSingleTap(MotionEvent point) {
				if (graphLayer.SingleTap((Point)mMapView.toMapPoint(new Point(point.getX(), point.getY())))) {
					return true;
				}
				graphLayer.clearSelection();
				int[] graphicIDs = graphLayer.getGraphicIDs(point.getX(), point.getY(), 25);
				graphLayer.setSelectedGraphics(graphicIDs, true);
				if (graphicIDs != null && graphicIDs.length > 0) {
					
					Graphic gr = graphLayer.getGraphic(graphicIDs[0]);
					Map<String, Object> attss = gr.getAttributes();
					Callout callout = mMapView.getCallout();
					callout.show(new Point(point.getX(), point.getY()));
				} else {
					//PopupForEditOffline popup=new PopupForEditOffline(SMISHActivity.this, mMapView);
				    //popup.showDia(point.getX(), point.getY(), 25);
					//ArcGISAttributeView vhjArcGISAttributeView = new ArcGISAttributeView(mMapView.getContext(), new Popup(mMapView, null));
					//vhjArcGISAttributeView.showContextMenu();
					
					Envelope tttEnvelope = new Envelope((Point)mMapView.toMapPoint(new Point(point.getX(), point.getY())), 
							mMapView.getResolution() * 50, 
							mMapView.getResolution() * 50);
					new RunQueryLocalFeatureLayerTask2()
					.execute(tttEnvelope);
				}
				return super.onSingleTap(point);
			}
			
			@Override
			public boolean onDoubleTap(MotionEvent point) {
				if (graphLayer.DoubleTap((Point)mMapView.toMapPoint(new Point(point.getX(), point.getY())))) {
					return true;
				}
				return super.onDoubleTap(point);
			}
			
			@Override
			public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
				if (!graphLayer.DragStart((Point)mMapView.toMapPoint(new Point(from.getX(), from.getY())))) {
					return super.onDragPointerMove(from, to);
				}
				graphLayer.SingleTap((Point)mMapView.toMapPoint(new Point(to.getX(), to.getY())));
				return true;
			}
			
			@Override
			public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {
				if (graphLayer.DragStart()) {
					return graphLayer.DoubleTap((Point)mMapView.toMapPoint(new Point(to.getX(), to.getY())));
				}
				return super.onDragPointerUp(from, to);
			}
		};
		mMapView.setOnTouchListener(mapOnTouchListener);
		
		//9.搜索
		final EditText editText = (EditText)findViewById(R.id.searchText);
		editText.setAlpha((float)0.6);
		editText.clearFocus();
		
		Button searchButton = (Button)findViewById(R.id.searchButton);
		android.view.View.OnClickListener aaaClickListener =  new android.view.View.OnClickListener() {	
			public void onClick(View v) {
				new RunQueryLocalFeatureLayerTask3().execute(editText);
			}
		};
		searchButton.setOnClickListener(aaaClickListener);
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
    	mMapView.removeLayer(fireHydrantsLayer);
    	for (long baseLayer : baseLayers) {
			mMapView.RemoveMap(baseLayer);
		}
    	baseLayers = new ArrayList<Long>();
    	
    	//加载
    	int urlCount = 0;
    	if (isOnline) {
    		for (String layerURL : urls) {
    			baseLayers.add(mMapView.LoadOnlineMap(layerURL, MapResourceType.TILED, urlCount++));
			}
    		mMapView.addLayer(fireHydrantsLayer);
		} else {
			for (String layerURL : urls) {
				baseLayers.add(mMapView.LoadBaseMap(layerURL, urlCount++));
			}
			mMapView.addLayer(fireHydrantsLayer);
		}    	
	}
    
    public void setEditingMode() {
		Switch editSwitch = (Switch)findViewById(R.id.switchEditing);
		Spinner editModeSpinner = (Spinner)findViewById(R.id.editMode);
		Spinner editTypeSpinner = (Spinner)findViewById(R.id.editType);
		if (editSwitch.isChecked()) {
			graphLayer.set_editingMode(EditingMode.None);
			editModeSpinner.setEnabled(false);
			editTypeSpinner.setEnabled(false);
		} else {
			editModeSpinner.setEnabled(true);
			editTypeSpinner.setEnabled(true);
			switch (editModeSpinner.getSelectedItemPosition()) {
			case 0:
				graphLayer.set_editingMode(EditingMode.Add);
				break;
			case 1:
				graphLayer.set_editingMode(EditingMode.Delete);
				break;
			case 2:
				graphLayer.set_editingMode(EditingMode.Update);
				break;
			case 3:
				graphLayer.set_editingMode(EditingMode.Select);
				break;
			default:
				break;
			}
		}
		this.setEditingType();
	}
    
    public void setEditingType() {
    	Spinner editTypeSpinner = (Spinner)findViewById(R.id.editType);
    	switch (editTypeSpinner.getSelectedItemPosition()) {
		case 0:
			graphLayer.set_editingType(Type.POINT);
			break;
		case 1:
			graphLayer.set_editingType(Type.POLYLINE);
			break;
		case 2:
			graphLayer.set_editingType(Type.POLYGON);
			break;
		case 3:
			graphLayer.set_editingType(Type.UNKNOWN);
			break;
		default:
			break;
		}
	}
    
    @SuppressWarnings("unused")
	private void PropertyChangeEve() {

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
	
	class RunQueryLocalFeatureLayerTask extends AsyncTask<EditText, Void,List<Geometry>>{
		
		class czRunnable implements Runnable {

			private Graphic gggGraphic;
			public czRunnable(Graphic gra) {
				gggGraphic = gra;
			}
			public void run() {
				createPopupView(gggGraphic);
			}
			
		}
	
	@Override
	protected List<Geometry> doInBackground(EditText... params) {
	   
	   QueryTask queryTask = new QueryTask("http://202.136.213.6:8399/arcgis/rest/services/Fireplug/MapServer/0");
	   Query queryParameters = new Query();
	   queryParameters.setInSpatialReference(mMapView.getSpatialReference());
	   queryParameters.setOutSpatialReference(mMapView.getSpatialReference());
	   //queryParameters.setReturnGeometry(true);
	   queryParameters.setWhere("消防编号='" + params[0].getText().toString() + "'");
	   //queryParameters.setGeometry(new Envelope(120.726194259283, 30.4755396800472, 122.352678668461, 32.0153757070882));
	   String[] outFields = {"状态", "规格", "径口直径", "地址", "中队"};
	   queryParameters.setOutFields(outFields);
	   
	   //Execute query task  
	   try {  
		   FeatureSet queryResult = queryTask.execute(queryParameters); 
		   Graphic[] resultGraphic = queryResult.getGraphics();
		   if (resultGraphic.length > 0) {
			   runOnUiThread(new czRunnable(resultGraphic[0]));
		   } else {
			   runOnUiThread(new czRunnable(null));
		   }
		   
	  
	   } catch (Exception e) {    
	    e.printStackTrace();  
	   } 
	   
	   return null;
	  }
	}
	
	private void createPopupView(Graphic showGraphic) {
		TextView resultTextView = new TextView(mMapView.getContext());
		if (showGraphic == null) {
			resultTextView.setTextSize(30);
			resultTextView.setText("未找到");
		} else {
			resultTextView.setText("");
			resultTextView.setTextSize(10);
			Map<String, Object> atts = showGraphic.getAttributes();
			for (java.util.Map.Entry<String, Object> entry : atts.entrySet()) {
				resultTextView.setText(resultTextView.getText() + entry.getKey() + " : " + entry.getValue() + "\n");
			}
		}
		((EditText)findViewById(R.id.searchText)).clearFocus();
		InputMethodManager imm = (InputMethodManager)getSystemService(SMISHActivity.INPUT_METHOD_SERVICE); 
        imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.searchText)).getWindowToken(),0);
		

        Callout callout = mMapView.getCallout();
        if (showGraphic == null) {
			//callout.show(mMapView.toMapPoint(new Point(300.0, 300.0)), resultTextView);
		} else {
			callout.show((Point)showGraphic.getGeometry(), resultTextView);
			mMapView.centerAt((Point)showGraphic.getGeometry(), true);
		}
	}
	
class RunQueryLocalFeatureLayerTask2 extends AsyncTask<Envelope, Void, List<Geometry>>{
		
		class czRunnable implements Runnable {

			private Graphic gggGraphic;
			public czRunnable(Graphic gra) {
				gggGraphic = gra;
			}
			public void run() {
				createPopupView(gggGraphic);
			}
			
		}
	
	@Override
	protected List<Geometry> doInBackground(Envelope... params) {
	   
	   QueryTask queryTask = new QueryTask("http://202.136.213.6:8399/arcgis/rest/services/Fireplug/MapServer/0");
	   Query queryParameters = new Query();
	   queryParameters.setInSpatialReference(mMapView.getSpatialReference());
	   queryParameters.setOutSpatialReference(mMapView.getSpatialReference());
	   //queryParameters.setReturnGeometry(true);
	   queryParameters.setWhere("1=1");
	   queryParameters.setGeometry(params[0]);
	   String[] outFields = {"状态", "规格", "径口直径", "地址", "中队"};
	   queryParameters.setOutFields(outFields);
	   
	   //Execute query task
	   try {  
		   FeatureSet queryResult = queryTask.execute(queryParameters); 
		   Graphic[] resultGraphic = queryResult.getGraphics();
		   if (resultGraphic.length > 0) {
			   runOnUiThread(new czRunnable(resultGraphic[0]));
		   } else {
			   runOnUiThread(new czRunnable(null));
		   }
		   
	  
	   } catch (Exception e) {    
		   e.printStackTrace();  
	   } 
	   
	   return null;
	  }
	}



/** 
 * 手机号段归属地查询 
 *  
 * @param phoneSec 手机号段 
 */  
public void getRemoteInfo(EditText editText) {  
    // 命名空间  
    String nameSpace = "http://WebXml.com.cn/";  
    // 调用的方法名称  
    String methodName = "getMobileCodeInfo";  
    // EndPoint  
    String endPoint = "http://webservice.webxml.com.cn/WebServices/MobileCodeWS.asmx";  
    // SOAP Action  
    String soapAction = "http://WebXml.com.cn/getMobileCodeInfo";  

    // 指定WebService的命名空间和调用的方法名  
    SoapObject rpc = new SoapObject(nameSpace, methodName);  

    // 设置需调用WebService接口需要传入的两个参数mobileCode、userId  
    rpc.addProperty("mobileCode", "13761786610");  
    rpc.addProperty("userId", "");  

    // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本  
    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);  

    envelope.bodyOut = rpc;  
    // 设置是否调用的是dotNet开发的WebService  
    envelope.dotNet = true;  
    // 等价于envelope.bodyOut = rpc;  
    envelope.setOutputSoapObject(rpc);  

    HttpTransportSE transport = new HttpTransportSE(endPoint);  
    try {  
        // 调用WebService  
        transport.call(null, envelope);  
    } catch (Exception e) {  
        e.printStackTrace();  
        return;
    }  

    // 获取返回的数据  
    SoapObject object = (SoapObject) envelope.bodyIn;  
    // 获取返回的结果  
    String result = object.getProperty(0).toString();  

    // 将WebService返回的结果显示在TextView中  
    editText.setText(result);  
} 

class RunQueryLocalFeatureLayerTask3 extends AsyncTask<EditText, Void, Void>{
	
	class czRunnable implements Runnable {

		private Graphic gggGraphic;
		public czRunnable(Graphic gra) {
			gggGraphic = gra;
		}
		public void run() {
			createPopupView(gggGraphic);
		}
		
	}

@Override
protected Void doInBackground(EditText... params) {

   try {  
	   	// 命名空间  
	    String nameSpace = "http://tempuri.org/";  
	    // 调用的方法名称  
	    String methodName = "AddressSearch";  
	    // EndPoint  
	    String endPoint = "http://202.136.213.6/XF_Tablet/service1.asmx";  
	    // SOAP Action  
	    String soapAction = "http://tempuri.org/AddressSearch";  

	    // 指定WebService的命名空间和调用的方法名  
	    SoapObject rpc = new SoapObject(nameSpace, methodName);  

	    // 设置需调用WebService接口需要传入的两个参数mobileCode、userId  
	    //rpc.addProperty("mobileCode", params[0].getText()); 
	    rpc.addProperty("sAddress", params[0].getText().toString());
	    rpc.addProperty("m_len", 1);

	    // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
	    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);  

	    envelope.bodyOut = rpc;
	    // 设置是否调用的是dotNet开发的WebService
	    envelope.dotNet = true;
	    // 等价于envelope.bodyOut = rpc;
	    envelope.setOutputSoapObject(rpc);
	    
	    HttpTransportSE transport = new HttpTransportSE(endPoint);
	    transport.call(soapAction, envelope);    

	    // 获取返回的数据  
	    SoapObject object = (SoapObject) envelope.getResponse();
	    // 获取返回的结果
	    
	    Point geo = new Point(0.0, 0.0);
	    Map<String, Object> attributes = new TreeMap<String, Object>();
	    
	    if (object.hasProperty("addressR")) {
			object = (SoapObject)object.getProperty(0);
			if (object.hasProperty("m_POIName")) {
				attributes.put("m_POIName", object.getProperty("m_POIName"));
			}
			if (object.hasProperty("m_RoadName")) {
				attributes.put("m_RoadName", object.getProperty("m_RoadName"));
			}
			if (object.hasProperty("m_DoorPlate")) {
				attributes.put("m_DoorPlate", object.getProperty("m_DoorPlate"));
			}
			if (object.hasProperty("m_MatchAccuracy")) {
				attributes.put("m_MatchAccuracy", object.getProperty("m_MatchAccuracy"));
			}
			if (object.hasProperty("m_MatchMemo")) {
				attributes.put("m_MatchMemo", object.getProperty("m_MatchMemo"));
			}
			if (object.hasProperty("m_bline")) {
				attributes.put("m_bline", object.getProperty("m_bline"));
			}
			if (object.hasProperty("m_class")) {
				attributes.put("m_class", object.getProperty("m_class"));
			}
			if (object.hasProperty("m_Lon")) {
				geo.setX(Double.valueOf(object.getProperty("m_Lon").toString()));
			}
			if (object.hasProperty("m_Lat")) {
				geo.setY(Double.valueOf(object.getProperty("m_Lat").toString()));
			}			
		}
	    
	    Graphic result = new Graphic(geo, 
	    		new SimpleMarkerSymbol(Color.BLUE, 10, com.esri.core.symbol.SimpleMarkerSymbol.STYLE.CIRCLE), 
	    		attributes);
	    
	    runOnUiThread(new czRunnable(result));

   } catch (Exception e) {    
	   e.printStackTrace();  
   }
   return null; 
   
  }
}
	
}