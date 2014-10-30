package cn.shsmi;

import android.graphics.Color;

import com.esri.android.map.MapGestureDetector.OnGestureListener;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Geometry.Type;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.internal.widget.ViewGallery.OnEventListener;
import com.esri.core.symbol.CompositeSymbol;
import com.esri.core.symbol.LineSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.Symbol;

public class CZGraLayer extends SMIGraphicsLayer {

	//编辑状态
    public enum EditingMode {
    	None, 
    	Add, 
    	Delete, 
    	Update, 
    	Select
    }
    private EditingMode _editingMode;
    
    //编辑类型
    private Type _editingType;
    
    //当前编辑的图形
	private Geometry editingGeometry;
    private int editingGraphicID;
    
    //渲染
    private CompositeSymbol layerSymbol;
    private CompositeSymbol layerSymbol2;
    private CompositeSymbol layerSymbol3;
    
    public CZGraLayer() {
    	super();
    	//渲染器
    	layerSymbol = new CompositeSymbol();
        layerSymbol.add(new SimpleMarkerSymbol(Color.BLUE, 10, com.esri.core.symbol.SimpleMarkerSymbol.STYLE.CIRCLE));//点
        layerSymbol.add(new SimpleLineSymbol(Color.GREEN, 2, com.esri.core.symbol.SimpleLineSymbol.STYLE.SOLID));//线
        layerSymbol.add(new SimpleFillSymbol(Color.GRAY));//面
        
        layerSymbol3 = new CompositeSymbol();
        layerSymbol3.add(new SimpleMarkerSymbol(Color.BLUE, 20, com.esri.core.symbol.SimpleMarkerSymbol.STYLE.CIRCLE));//点
        layerSymbol3.add(new SimpleLineSymbol(Color.GREEN, 2, com.esri.core.symbol.SimpleLineSymbol.STYLE.SOLID));//线
        layerSymbol3.add(new SimpleFillSymbol(Color.GRAY));//面
        
        layerSymbol2 = new CompositeSymbol();
        layerSymbol2.add(new SimpleMarkerSymbol(Color.BLUE, 1, com.esri.core.symbol.SimpleMarkerSymbol.STYLE.CIRCLE));//点
        layerSymbol2.add(new SimpleLineSymbol(Color.GREEN, 2, com.esri.core.symbol.SimpleLineSymbol.STYLE.SOLID));//线
        layerSymbol2.add(new SimpleFillSymbol(Color.GRAY));//面
        this.setSelectionColor(Color.RED);
	}

	public EditingMode editingMode() {
		return _editingMode;
	}

	public void set_editingMode(EditingMode _editingMode) {
		editingGraphicID = -1;
		editingGeometry = null;
		this._editingMode = _editingMode;
		editingGraphicID = -1;
	}

	public Type editingType() {
		return _editingType;
	}

	public void set_editingType(Type _editingType) {
		editingGraphicID = -1;
		editingGeometry = null;
		this._editingType = _editingType;
	}
	
	public boolean SingleTap(Point point) {
		switch (this.editingMode()) {
		case None:
			return false;
		case Add:
			switch (this.editingType()) {
			case POINT:
				editingGeometry = point;
				this.AddCurrentGraphic();
				editingGraphicID = -1;
				break;
			case POLYLINE:
			case UNKNOWN:
				this.removeGraphic(editingGraphicID);				
				if (editingGeometry == null || editingGeometry.getType() != Type.POLYLINE) {
					editingGeometry = new Polyline();
					((Polyline)editingGeometry).startPath(point);
				}
				((Polyline)editingGeometry).lineTo(point);
				this.AddCurrentGraphic();
				break;
			case POLYGON:
				this.removeGraphic(editingGraphicID);
				if (editingGeometry == null || editingGeometry.getType() != Type.POLYGON) {
					editingGeometry = new Polygon();
					((Polygon)editingGeometry).startPath(point);
				}
				((Polygon)editingGeometry).lineTo(point);
				this.AddCurrentGraphic();
				break;
			default:
				break;
			}
			return true;
		case Delete:
			return true;
		case Update:
			return true;
		case Select:
			return true;
		default:
			return false;
		}
	}

	public boolean DoubleTap(Point point) {
		switch (this.editingMode()) {
		case None:
			return false;
		case Add:
			switch (this.editingType()) {
			case POINT:
				editingGeometry = point;
				this.AddCurrentGraphic();
				editingGraphicID = -1;
				break;
			case POLYLINE:
			case UNKNOWN:
				this.removeGraphic(editingGraphicID);				
				if (editingGeometry == null || editingGeometry.getType() != Type.POLYLINE) {
					editingGeometry = new Polyline();
					((Polyline)editingGeometry).startPath(point);
				}
				((Polyline)editingGeometry).lineTo(point);
				this.AddCurrentGraphic();
				editingGraphicID = -1;
				editingGeometry = null;
				break;
			case POLYGON:
				this.removeGraphic(editingGraphicID);
				if (editingGeometry == null || editingGeometry.getType() != Type.POLYGON) {
					editingGeometry = new Polygon();
					((Polygon)editingGeometry).startPath(point);
				}
				((Polygon)editingGeometry).lineTo(point);
				this.AddCurrentGraphic();
				editingGraphicID = -1;
				editingGeometry = null;
				break;
			default:
				break;
			}
			return true;
		case Delete:
			return true;
		case Update:
			return true;
		case Select:
			return true;
		default:
			return false;
		}
	}
	
	public boolean DragStart(Point point) {
		if (editingType() != Type.UNKNOWN || editingMode() != EditingMode.Add) {
			return false;
		}
		SingleTap(point);
		return true;
	}
	
	public boolean DragStart() {
		if (editingType() != Type.UNKNOWN || editingMode() != EditingMode.Add) {
			return false;
		}
		return true;
	}
	
	private void AddCurrentGraphic() {
		if(editingType() == Type.UNKNOWN) {
			SMIGraphic addedGraphic = new SMIGraphic(editingGeometry, layerSymbol2);
			editingGraphicID = this.addGraphic(addedGraphic);
		} else {
			SMIGraphic addedGraphic = new SMIGraphic(editingGeometry, layerSymbol);
			editingGraphicID = this.addGraphic(addedGraphic);
		}
		
		//editingGraphicID = this.addGraphic(addedGraphic);
	}
    
}
