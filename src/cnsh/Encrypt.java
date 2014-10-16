package cnsh;

import java.lang.System;

public class Encrypt {
	public static native byte[] decrypt(byte[] nbyte , int size ) ; 
	
	static {
		System.loadLibrary("SMIMap");
	}
	
}
