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
	private PopupContainer container;//����popup������
	   
	private PopupDialog popupDialog;//��ʾ��Popup�ĶԻ���  
	private ProgressDialog dialog;  
	private LinearLayout editorBarLocal;//�༭�������Ĳ���  
	private int count;//��¼��ѯͼ��ĸ���  
	private int popupCount;//��¼popup�ĸ���  
	
	
	public PopupForEditOffline(Activity mianActivity, MapView mapView) {  
		this.mianActivity = mianActivity;  
	    this.mapView = mapView;  
	}  

	
	/** 
	    * ��ʾpopupDialog 
	    * @param x ��������x���� 
	    * @param y ��������y���� 
	    * @param tolerance ��ѯ������ 
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
	        dialog.setMessage("���ڲ�ѯ��������");  
	        dialog.show();  
	    }  
	      
	    //�õ�����ͼ��  
	    Layer[] layers= mapView.getLayers();  
	    for(Layer layer :layers){  
	        if (!layer.isInitialized() || !layer.isVisible()){  
	            continue;//���ͼ��û�г�ʼ�������߲��ɼ���������  
	        }  
	        if(layer instanceof FeatureLayer){  
	            FeatureLayer  fLayer=(FeatureLayer) layer;  
	            //��ͼ�����popup��Ϣ  
	            if(fLayer.getPopupInfos()!=null){  
	                count++;  
	                //���в�ѯ  
	                new RunQueryLocalFeatureLayerTask(x, y, tolerance)  
	                .execute(fLayer);  
	            }  
	        }  
	    }  
	   }  
	   
	   /**
	     * ��ѯFeatureLayer�� popup��Ϣ���첽��
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
					//�õ����Feature��Ӧ��Popup
					Popup popup=localFeatureLayer.createPopup(mapView, 0, feature);				
					//��Popup���뵽PopupContainer��
					container.addPopup(popup);
					popupCount++;
					//����һ��popup������ʱ���Ϳ�ʼ�����༭������
					if(popupCount==1){
						editorBarLocal=new LinearLayout(mianActivity);
						//ȡ����ť
						Button canclebtn= new Button(mianActivity);
						canclebtn.setText("ȡ��");
						canclebtn.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								if(popupDialog!=null&& popupDialog.isShowing()){
									popupDialog.dismiss();
								}	
							}
						});
						editorBarLocal.addView(canclebtn);
						
						//����Attachment��ť
						final Button addAchbtn=new Button(mianActivity);
						addAchbtn.setText("���Att");
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


//��ʾ��Popup�ĶԻ���  
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