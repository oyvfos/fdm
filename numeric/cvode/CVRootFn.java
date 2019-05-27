package de.grogra.numeric.cvode;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

/**
 * This function implements a vector-valued function g(t, y) such that the
 * roots of the nrtfn components gi(t, y) are sought.
 */
public interface CVRootFn extends Callback {
	int callback(double t, N_Vector y, Pointer gout, Pointer user_data);
}
