package de.grogra.numeric;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;

/**
 * 
 * This class is a wrapper to Apache Commons Math.
 * 
 * @author Reinhard Hemmerling
 *
 */
class FirstOrderDifferentialEquationsAdapter implements FirstOrderDifferentialEquations {

	ODE ode;	// rate function
	int dim;	// length of rate/state vector
	
	public FirstOrderDifferentialEquationsAdapter(ODE ode, int dim) {
		this.ode = ode;
		this.dim = dim;
	}
	
	public void computeDerivatives(double t, double[] y, double[] dot)
			throws DerivativeException {

		ode.getRate(dot, t, y);
	}

	public int getDimension() {
		return dim;
	}
	
}
