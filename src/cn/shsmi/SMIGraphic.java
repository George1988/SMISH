package cn.shsmi;

import java.util.Map;

import com.esri.core.geometry.Geometry;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.Symbol;

public class SMIGraphic {
	
	protected Graphic real_meGraphic;
	
	public SMIGraphic (Geometry geometry, Symbol symbol, Map<String, Object> attributes) {
		real_meGraphic = new Graphic(geometry, symbol, attributes);
	}
	
	public SMIGraphic(Geometry geometry, Symbol symbol) {
		real_meGraphic = new Graphic(geometry, symbol);
	}

}
