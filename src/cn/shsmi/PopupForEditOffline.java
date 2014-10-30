package cn.shsmi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.esri.android.map.FeatureLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupContainer;
import com.esri.android.map.popup.PopupContainerView;
import com.esri.core.map.Feature;
import com.esri.core.map.Field;
import com.esri.core.map.Graphic;
import com.esri.core.table.TableException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class PopupForEditOffline {
	
	private Activity mianActivity;  
	private MapView mapView;  
	private PopupContainer container;//保存popup的容器
	   
	private PopupDialog popupDialog;//显示的Popup的对话框  
	private ProgressDialog dialog;  
	private LinearLayout editorBarLocal;//编辑工具条的布局  
	private int count;//记录查询图层的个数  
	private int popupCount;//记录popup的个数  
	
	
	public PopupForEditOffline(Activity mianActivity, MapView mapView) {  
		this.mianActivity = mianActivity;  
	    this.mapView = mapView;  
	}  

	
	/** 
	    * 显示popupDialog 
	    * @param x 长按处的x坐标 
	    * @param y 长按处的y坐标 
	    * @param tolerance 查询的容限 
	    */  
	   public void showDia(float x,float y, int tolerance){  
	    if(!mapView.isLoaded()){  
	        return;  
	    }  
	    container=new PopupContainer(mapView);  
	    count=0;  
	    popupCount=0;  
	    popupDialog=null;  
	    if(dialog==null|| !dialog.isShowing()){  
	        dialog=new ProgressDialog(mianActivity);  
	        dialog.setMessage("正在查询。。。。");  
	        dialog.show();  
	    }  
	      
	    //得到所有图层  
	    Layer[] layers= mapView.getLayers();  
	    for(Layer layer :layers){  
	        if (!layer.isInitialized() || !layer.isVisible()){  
	            continue;//如果图层没有初始化，或者不可见，就跳过  
	        }  
	        if(layer instanceof FeatureLayer){  
	            FeatureLayer  fLayer=(FeatureLayer) layer;  
	            //该图层具有popup信息  
	            if(fLayer.getPopupInfos()!=null){  
	                count++;  
	                //进行查询  
	                new RunQueryLocalFeatureLayerTask(x, y, tolerance)  
	                .execute(fLayer);  
	            }  
	        }  
	    }  
	   }  
	   
	   /**
	     * 查询FeatureLayer中 popup信息的异步类
	     */
	    class RunQueryLocalFeatureLayerTask extends AsyncTask<FeatureLayer, Void,List<Feature>>{
	    	private float x;
	    	private float y;
	    	private int tolerance;
	    	private FeatureLayer localFeatureLayer;
			public RunQueryLocalFeatureLayerTask(float x, float y, int tolerance) {
				super();
				this.x = x;
				this.y = y;
				this.tolerance = tolerance;
			}

			@Override
			protected List<Feature> doInBackground(FeatureLayer... params) {
				for (FeatureLayer featureLayer : params) {
					this.localFeatureLayer = featureLayer;
					long[] ids = featureLayer.getFeatureIDs(x, y, tolerance);
					if (ids != null && ids.length > 0) {
						List<Feature> features = new ArrayList<Feature>();
						for (long id : ids) {
							Feature f = featureLayer.getFeature(id);
							if (f == null)
								continue;
							features.add(f);
						}
						return features;
					}
				}
				return null;
				
			}

			@Override
			protected void onPostExecute(List<Feature> result) {
				count--;
				if(result==null || result.size()==0){
					if(count==0&& dialog!=null && dialog.isShowing()){
						dialog.dismiss();
					}
					return ;
				}
				for(Feature feature :result){
					//得到这个Feature对应得Popup
					Popup popup=localFeatureLayer.createPopup(mapView, 0, feature);				
					//将Popup加入到PopupContainer中
					container.addPopup(popup);
					popupCount++;
					//当第一个popup被加入时，就开始构建编辑工具条
					if(popupCount==1){
						editorBarLocal=new LinearLayout(mianActivity);
						//取消按钮
						Button canclebtn= new Button(mianActivity);
						canclebtn.setText("取消");
						canclebtn.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								if(popupDialog!=null&& popupDialog.isShowing()){
									popupDialog.dismiss();
								}	
							}
						});
						editorBarLocal.addView(canclebtn);
						
						//增加Attachment按钮
						final Button addAchbtn=new Button(mianActivity);
						addAchbtn.setText("添加Att");
						addAchbtn.setEnabled(false);
						
						addAchbtn.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
									Intent it=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
									mianActivity.startActivityForResult(it, 0);
							}
						});	
						editorBarLocal.addView(addAchbtn);
						
						
						
						container.getPopupContainerView().addView(editorBarLocal,0);
					}
				}
				if(count==0 &&dialog!=null &&dialog.isShowing()){
					dialog.dismiss();
					popupDialog=new PopupDialog(mianActivity, container);
					popupDialog.show();
				}			
			}	
	    }
	   
}


//显示的Popup的对话框  
class PopupDialog extends Dialog{  
    private PopupContainer container;  
    private Context context;  
    public PopupDialog(Context context,PopupContainer container) {  
        super(context);  
        this.container=container;  
        this.context=context;  
    }  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {      	
        LinearLayout line=new LinearLayout(context);  
        line.addView(container.getPopupContainerView(), new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));    
        setContentView(line);  
        super.onCreate(savedInstanceState);  
    }  
}  