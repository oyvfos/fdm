package de.grogra.numeric;

import java.util.Map;

/**
 * A solver is an implementation of a numerical process to solve an
 * ordinary differential equation.
 * 
 * @author Reinhard Hemmerling
 *
 */
public interface Solver {
	
	/**
	 * Integrate ode from t0 to t1. Initial state is passed in y0.
	 * Final state is stored in memory provided by y1 (can be the same
	 * as y0).
	 * @param ode equations
	 * @param t0 initial time
	 * @param y0 initial state
	 * @param t1 final time
	 * @param y1 memory to return state at t1, may refer to same object as y0
	 * @throws NumericException
	 */
	void integrate(ODE ode, double t0, double[] y0, double t1, double[] y1) throws NumericException;

	/**
	 * Set monitor functions. A previously set monitor will be replaced
	 * by this one. A monitor can be disabled by passing zero for parameter n.
	 * The parameter monitor may be null in this case.
	 * @param n
	 * @param monitor
	 * @throws NumericException
	 */
	void setMonitor(int n, Monitor monitor) throws NumericException;
	
	/**
	 * Set additional options for the integration process.
	 * Options are provided as mapping from keys to values.
	 * Note that supported keys and the data format of associated values
	 * depends on the implementation of the solver.
	 * @param options
	 */
	void setOptions(Map options);
	// TODO use enum-inheritance work-around below
	// http://www.vlad-yatsenko.eu/2011/01/java-magic-extending-enumerations/
	
	/**
	 * Set element-specific absolute and relative tolerance values.
	 * A value of null for either array indicates that element-specific
	 * tolerances should be disabled for this tolerance type.
	 * An value of zero in any of the arrays indicates that the integrator
	 * should provide a default tolerance value instead.
	 * Note that if any of the specified are really used is up to the
	 * implementation of the integrator.
	 * @param absTol element-specific absolute tolerance values, or null
	 * @param relTol element-specific relative tolerance values, or null
	 */
	void setTolerances(double[] absTol, double[] relTol);
}
