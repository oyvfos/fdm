package fdm;



import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.FirstOrderIntegrator;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;

import de.grogra.numeric.CVodeAdapter;



/** Simple example of JNA interface mapping and usage. */
public class Test {

    static // This is the standard, stable way of mapping, which supports extensive
    // customization and mapping of Java to native types.
	StepHandler stepHandler = new StepHandler() {
	    public void init(double t0, double[] y0, double t) {
	    }
	            
	    public void handleStep(StepInterpolator interpolator, boolean isLast) throws DerivativeException {
	        double   t = interpolator.getCurrentTime();
	        double[] y = interpolator.getInterpolatedState();
	        System.out.println(t + " " + y[0] + " " + y[1]);
	    }

		@Override
		public boolean requiresDenseOutput() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void reset() {
			// TODO Auto-generated method stub
			
		}
	};
	private static class CircleODE implements FirstOrderDifferentialEquations {

	    private double[] c;
	    private double omega;

	    public CircleODE(double[] c, double omega) {
	        this.c     = c;
	        this.omega = omega;
	    }

	    public int getDimension() {
	        return 2;
	    }

	    public void computeDerivatives(double t, double[] y, double[] yDot) {
	        yDot[0] = omega * (c[1] - y[1]);
	        yDot[1] = omega * (y[0] - c[0]);
	    }

	}
  
    public static void main(String[] args) throws DerivativeException, IntegratorException {
    	FirstOrderIntegrator cvode = new CVodeAdapter();
    	
    	//cvode.addStepHandler(stepHandler);
    	
    	double[] y = new double[] { 0.0, 1.0 }; // initial state
    	FirstOrderDifferentialEquations ode = new CircleODE(new double[] { 1.0, 1.0 }, 0.2);
    	//FirstOrderIntegrator dp853 = new DormandPrince853Integrator(1.0e-8, 100.0, 1.0e-10, 1.0e-10);
    	//dp853.addStepHandler(stepHandler);
    	cvode.integrate(ode, 0.0, y, 16.0, y); // now y contains final state at time t=16.0
    	
    	System.out.println(y[1]);
    	//dp853.integrate(ode, 0.0, y, 16.0, y); // now y contains final state at time t=16.0
    	//System.out.println(y[1]);
    	
    	
    }
}