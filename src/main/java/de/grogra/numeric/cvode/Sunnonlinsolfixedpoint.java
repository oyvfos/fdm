package de.grogra.numeric.cvode;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public class Sunnonlinsolfixedpoint {
	public static final boolean LOADED;
	static {
		Native.register("sundials_sunnonlinsolfixedpoint");
		LOADED = true;
	}
	
	public static native Pointer SUNNonlinSol_FixedPoint(N_Vector y0, int in);
}
