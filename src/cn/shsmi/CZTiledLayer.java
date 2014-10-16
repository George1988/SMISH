package cn.shsmi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import cnsh.Encrypt;
import com.esri.android.map.TiledServiceLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;

public class CZTiledLayer extends  TiledServiceLayer{
	
	private TileInfo tileInfo;
	private String frameWorkPath;
	private final String CONFIGFILENAME = "con.smix";
	private int ratio;

	public CZTiledLayer(String frameString) {
		super(true);
		frameWorkPath = frameString;
		if (!frameString.endsWith("/")) {
			frameWorkPath = frameString + '/';
		}
		this.buildTileInfo();
        super.initLayer();
        return;
	}
	
	
    
	@Override
	protected byte[] getTile(int level, int col, int row) throws Exception {
		String imagePath = frameWorkPath;
		imagePath += String.format("L%02d", level);
		imagePath += String.format("/%08x", row / ratio);
		imagePath += String.format("/%08x", col / ratio);
		FileInputStream fin = new FileInputStream(imagePath);
		FileInputStream findex = new FileInputStream(imagePath + "x");
		findex.skip(((row % ratio) * ratio + (col % ratio)) * 8);
		int offset = readInt32FromFileStream(findex);
		int length = readInt32FromFileStream(findex);
        byte[] imageData = readByteFromFileStreams(fin, offset, length);
        fin.close();
        byte[] result;
        result = Encrypt.decrypt(imageData, imageData.length);
		return result;
	}
	
	@Override
    public TileInfo getTileInfo(){
        return this.tileInfo;
    }
	
	private byte[] readByteFromFileStreams(InputStream fileStream, int offset, int len) throws IOException {
		byte[] result = new byte[len];
		fileStream.skip(offset);
		fileStream.read(result);
		return result;
	}
	
	private int readInt32FromFileStream(InputStream fileStream) throws IOException {
		int result = 0;
		result = fileStream.read();
		result += (fileStream.read() << 8);
		result += (fileStream.read() << 16);
		result += (fileStream.read() << 24);
		return result;
	}
	
	private double readDouble64FromFileStream(InputStream fileStream) throws IOException {
		byte[] buffer = new byte[8];
		fileStream.read(buffer, 0, 8);
		buffer[0] = (byte) (buffer[0] ^ buffer[7]);
		buffer[7] = (byte) (buffer[0] ^ buffer[7]);
		buffer[0] = (byte) (buffer[0] ^ buffer[7]);
		buffer[1] = (byte) (buffer[1] ^ buffer[6]);
		buffer[6] = (byte) (buffer[1] ^ buffer[6]);
		buffer[1] = (byte) (buffer[1] ^ buffer[6]);	
		buffer[2] = (byte) (buffer[2] ^ buffer[5]);
		buffer[5] = (byte) (buffer[2] ^ buffer[5]);
		buffer[2] = (byte) (buffer[2] ^ buffer[5]);	
		buffer[3] = (byte) (buffer[3] ^ buffer[4]);
		buffer[4] = (byte) (buffer[3] ^ buffer[4]);
		buffer[3] = (byte) (buffer[3] ^ buffer[4]);
		return ByteBuffer.wrap(buffer).getDouble();
	}
	
	private void buildTileInfo()
    {
		//闁跨喐鏋婚幏宄板絿闁跨喐鏋婚幏鐑芥晸閺傘倖瀚�
		File indexFile = new File(frameWorkPath + CONFIGFILENAME);
        InputStream indexFileStream = null;
        		
        try {
        	indexFileStream = new FileInputStream(indexFile);
        	
        	int levels = ((int)indexFile.length() - 100) / 16;
        	//闁跨喓笑绾攱瀚归幈浼存晸閿燂拷
            int wkid = readInt32FromFileStream(indexFileStream);
            this.setDefaultSpatialReference(SpatialReference.create(wkid));
            //FullExtent
            double xmin, ymin, xmax, ymax;
            xmin = readDouble64FromFileStream(indexFileStream);
            ymin = readDouble64FromFileStream(indexFileStream);
            xmax = readDouble64FromFileStream(indexFileStream);
            ymax = readDouble64FromFileStream(indexFileStream);
            this.setFullExtent(new Envelope(xmin, ymin, xmax, ymax));
            //InitialExtent
            xmin = readDouble64FromFileStream(indexFileStream);
            ymin = readDouble64FromFileStream(indexFileStream);
            xmax = readDouble64FromFileStream(indexFileStream);
            ymax = readDouble64FromFileStream(indexFileStream);
            this.setInitialExtent(new Envelope(xmin, ymin, xmax, ymax));
            //originalPoint
            xmin = readDouble64FromFileStream(indexFileStream);
            ymin = readDouble64FromFileStream(indexFileStream);
            Point originalPoint=new Point(xmin, ymin);
            //DPI
            int dpi = readInt32FromFileStream(indexFileStream);
            //tileWidth
            int tileWidth = readInt32FromFileStream(indexFileStream);
            //tileHeight
            int tileHeight = readInt32FromFileStream(indexFileStream);
            //ratio
            ratio = readInt32FromFileStream(indexFileStream);
            //resolutions
            double[] res = new double[levels];
            for (int level = 0; level < levels; level++) {
				res[level] = readDouble64FromFileStream(indexFileStream);
			}
            //scales
            double[] scale = new double[levels];
            for (int level = 0; level < levels; level++) {
            	scale[level] = readDouble64FromFileStream(indexFileStream);
			}
            //闁跨喐鍩呴幉瀣闁跨喍鑼庣涵閿嬪闁跨喐鏋婚幏锟�
            indexFileStream.close();
            //闁跨喐鏋婚幏鐑芥晸閻偉顕滈幏鐑芥晸閺傘倖瀚�
            this.tileInfo=new com.esri.android.map.TiledServiceLayer.TileInfo(originalPoint, scale, res, levels, dpi, tileWidth,tileHeight);
            this.setTileInfo(this.tileInfo);
		} catch (Exception e) {
			
		}
        
    }

}
