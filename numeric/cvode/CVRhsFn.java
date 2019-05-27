package de.grogra.numeric.cvode;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

/**
 * This function computes the ODE right-hand side for a given value of the
 * independent variable t and state vector y.
 */
public interface CVRhsFn extends Callback {
	int callback(double t, N_Vector y, N_Vector ydot, Pointer user_data);
}
