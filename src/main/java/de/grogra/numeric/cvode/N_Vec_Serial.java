package de.grogra.numeric.cvode;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;

public class N_Vec_Serial {
	public static final boolean LOADED;
	static {
		Native.register("sundials_nvecserial");
		LOADED = true;
	}
	
	public static native N_Vector N_VNew_Serial(NativeLong vec_length);
//	public static native N_Vector N_VNewEmpty_Serial(NativeLong vec_length);
//	public static native N_Vector N_VMake_Serial(NativeLong vec_length, double[] v_data);
	public static native void N_VDestroy_Serial(N_Vector v);
	public static native void N_VPrint_Serial(N_Vector v);
}
