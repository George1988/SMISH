package cn.shsmi;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.SpatialReference;

public class SMIGraphicsLayer extends GraphicsLayer {
	
	public SMIGraphicsLayer() {
		// TODO Auto-generated constructor stub
		super(SpatialReference.create(4326), new Envelope(120.726194259283, 30.4755396800472, 122.352678668461, 32.0153757070882));
	}
	
	public int addGraphic(SMIGraphic graphic) {
		// TODO Auto-generated method stub
		return this.addGraphic(graphic.real_meGraphic);
	}

}
