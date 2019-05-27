package de.grogra.numeric.cvode;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.PointerByReference;

import de.grogra.numeric.CVodeAdapterOriginal;

/**
 * This class provides native access to the CVODE library using JNA.
 * Note that not all functions have been made accessible, but only
 * those necessary to implement {@linkplain CVodeAdapterOriginal}.
 * 
 * For reference check CVODE documentation at:
 * https://computation.llnl.gov/casc/sundials/documentation/cv_guide/cv_guide.html
 * 
 * A native library "cvode" must be created that contains CVODE.
 * To create this library, download SUNDIALS and execute:
 * <pre>
 *   ./configure CFLAGS="-fPIC"
 *   make
 * </pre>
 * Then copy all produced objects files (*.o) into a directory and link them to a 
 * shared library with (for Linux):
 * <pre>
 *   gcc -shared -Wl,-soname=libcvode.so -o libcvode.so -static-libgcc *.o
 * </pre>
 * 
 * To tell JNA where to find the shared library during development, add an option
 * <pre>-Djna.library.path=/path/to/library</pre>
 * to the VM arguments.
 * 
 * To enable crash protection, also add
 * <pre>-Djna.protected</pre>
 * to the VM arguments.
 *
 *requires a single library ::> cvode.dll
 *
 * @author Reinhard Hemmerling
 *
 */
public class CVODEOriginal {
	
	static {
		Native.register("cvode");
	}

	// lmm
	public static final int CV_ADAMS 				= 1;
	public static final int CV_BDF 					= 2;
	
	// iter
	public static final int CV_FUNCTIONAL			= 1;
	public static final int CV_NEWTON 				= 2;

	// itask
	public static final int CV_NORMAL				= 1;
	public static final int CV_ONE_STEP				= 2;

	// cvode return flags
	public static final int CV_SUCCESS				= 0;
	public static final int CV_TSTOP_RETURN			= 1;
	public static final int CV_ROOT_RETURN			= 2;
	public static final int CV_WARNING				= 99;
	public static final int CV_TOO_MUCH_WORK		= -1;
	public static final int CV_TOO_MUCH_ACC			= -2;
	public static final int CV_ERR_FAILURE			= -3;
	public static final int CV_CONV_FAILURE			= -4;
	public static final int CV_LINIT_FAIL			= -5;
	public static final int CV_LSETUP_FAIL			= -6;
	public static final int CV_LSOLVE_FAIL			= -7;
	public static final int CV_RHSFUNC_FAIL			= -8;
	public static final int CV_FIRST_RHSFUNC_ERR	= -9;
	public static final int CV_REPTD_RHSFUNC_ERR	= -10;
	public static final int CV_UNREC_RHSFUNC_ERR	= -11;
	public static final int CV_RTFUNC_FAIL			= -12;
	public static final int CV_MEM_FAIL				= -20;
	public static final int CV_MEM_NULL				= -21;
	public static final int CV_ILL_INPUT			= -22;
	public static final int CV_NO_MALLOC			= -23;
	public static final int CV_BAD_K				= -24;
	public static final int CV_BAD_T				= -25;
	public static final int CV_BAD_DKY				= -26;
	public static final int CV_TOO_CLOSE			= -27;
	

	/**
	 * The function CVodeCreate instantiates a CVODE solver object and specifies
	 * the solution method. The recommended choices for (lmm, iter) are
	 * (CV_ADAMS, CV_FUNCTIONAL) for nonstiff problems and (CV_BDF, CV_NEWTON)
	 * for stiff problems.
	 * 
	 * @param lmm
	 *            specifies the linear multistep method and may be one of two
	 *            possible values: CV_ADAMS or CV_BDF
	 * @param iter
	 *            specifies the type of nonlinear solver iteration and may be
	 *            either CV_NEWTON or CV_FUNCTIONAL
	 * @return If successful, CVodeCreate returns a pointer to the newly created
	 *         CVODE memory block (of type void *). Otherwise, it returns NULL.
	 */
	public static native Pointer CVodeCreate(int lmm, int iter);

	public static native int CVodeInit(Pointer cvode_mem, CVRhsFn f, double t0, N_Vector y0);
	
	public static native void CVodeFree(PointerByReference cvode_mem);
	
	public static native int CVodeSStolerances(Pointer cvode_mem, double reltol, double abstol);
	public static native int CVodeSVtolerances(Pointer cvode_mem, double reltol, N_Vector abstol);
//	public static native int CVodeWFtolerances(Pointer cvode_mem, ...);
	
	public static native int CVDense(Pointer cvode_mem, int N);
//	public static native int CVLapackDense(Pointer cvode_mem, int N);
//	public static native int CVBand(Pointer cvode_mem, int N, int mupper, int mlower);
//	public static native int CVLapackBand(Pointer cvode_mem, int N, int mupper, int mlower);
//	public static native int CVDiag(Pointer cvode_mem);
	// TODO ...
	
	public static native int CVodeRootInit(Pointer cvode_mem, int nrtfn, CVRootFn g);
	
	public static native int CVode(Pointer cvode_mem, double tout,
			N_Vector yout, DoubleByReference tret, int itask);	
	
	// 0 - no root, +1 - increasing root, -1 - decreasing root
	public static native int CVodeGetRootInfo(Pointer cvode_mem, int[] rootsfound);
	
	public static native String CVodeGetReturnFlagName(int flag);
	
	public static native N_Vector N_VNew_Serial(NativeLong vec_length);
//	public static native N_Vector N_VNewEmpty_Serial(NativeLong vec_length);
//	public static native N_Vector N_VMake_Serial(NativeLong vec_length, double[] v_data);
	public static native void N_VDestroy_Serial(N_Vector v);
	public static native void N_VPrint_Serial(N_Vector v);
}
