package de.grogra.numeric;

/**
 * This interface is used to describe an initial value problem.
 * The problem is given by an initial state y0 at an initial
 * time t0, so that y0 = y(t0), and a rate function y'(t) = f(t, y).
 *  
 * @author Reinhard Hemmerling
 */
public interface ODE {
	
	/**
	 * Calculate the rate (derivative of state) of the system
	 * for a given time and state.
	 * @param out provides memory for storing the rate
	 * @param t current time
	 * @param state current state
	 */
	void getRate(double[] out, double t, double[] state);
}
