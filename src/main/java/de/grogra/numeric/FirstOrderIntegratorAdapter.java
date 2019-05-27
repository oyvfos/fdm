package de.grogra.numeric;

import static java.lang.Math.abs;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.FirstOrderIntegrator;
import org.apache.commons.math.ode.events.EventException;
import org.apache.commons.math.ode.events.EventHandler;

/**
 * This class is a wrapper to Apache Commons Math.
 * 
 * There is a subtle bug in Apache Commons Math (up to version 2.2 till now),
 * namely if there are multiple event handlers that trigger simultaneously and
 * one of them causes the integration to stop, then subsequent handlers will not
 * be executed.
 * 
 * An intrusive solution would be to modify the responsible code in Apache
 * Commons Math, which seems to be in method acceptStep in AbstractIntegrator.
 * 
 * A workaround is to simply always stop integration when an event triggers and
 * handle events afterwards. This is what is implemented for now.
 * 
 * @author Reinhard Hemmerling
 * 
 */
public class FirstOrderIntegratorAdapter implements Solver {

	public static final double MAX_CHECK_INTERVAL = 1;	// in time units
	public static final double CONVERGENCE = 1e-4;		// in time units
	public static final int MAX_ITERATION_COUNT = 100;

	FirstOrderIntegrator integrator;
	
	int n;
	Monitor monitor;
	
	// data holder to support implementation of setMonitor()
	private static class EventData {
		double t;
		double[] y;
		double[] gout;
	}
	
	final EventData data = new EventData();

	public FirstOrderIntegratorAdapter(FirstOrderIntegrator integrator) {
		this.integrator = integrator;
	}

	@Override
	public void integrate(ODE ode, double t0, double[] y0, double t1, double[] y)
			throws NumericException {

		assert y0 != null;
		assert y != null;
		assert y0.length == y.length;
		
		final int N = y.length;

		FirstOrderDifferentialEquations equations = new FirstOrderDifferentialEquationsAdapter(
				ode, N);
		try {
			// loop until target time reached
			double t = t0;
			System.arraycopy(y0, 0, y, 0, N);
			while (abs(t - t1) > CONVERGENCE) {
				// integrate until target time reached or event triggered
				t = integrator.integrate(equations, t, y, t1, y);
				
//				System.out.println("t = " + t);
//				for (double d : y) System.out.print(d + "  ");
//				System.out.println();
				
				// check if any monitor was set, and if so check if any one triggered
				if (n > 0) {
					// invalidate cache
					data.t = Double.NaN;
					// evaluate monitor function
					monitor.g(data.gout, t, y);
					// check if any event triggered
					boolean stop = false;
					for (int i = 0; i < n; i++) {
						if (abs(data.gout[i]) < 2 * CONVERGENCE) {
							stop |= monitor.handleEvent(i, t, y);
						}
					}
					// if any event requested to stop, stop integration now
					if (stop) {
						break;
					}
				}
			}
		} catch (Exception ex) {
			throw new NumericException(ex);
		}
	}
	
	public void setMonitor(int n, final Monitor monitor)
	{
		assert n == 0 || monitor != null;
		
		// remember parameters for later use in integrate
		this.n = n;
		this.monitor = monitor;
		// remove previously set handlers
		integrator.clearEventHandlers();
		// set new handlers
		data.gout = new double[n];
		for (int i = 0; i < n; i++) {
			final int INDEX = i;
			EventHandler eventHandler = new EventHandler() {
				final int i = INDEX;
				@Override
				public int eventOccurred(double t, double[] y, boolean increasing)
						throws EventException {
					// always stop, as workaround
					return STOP;
				}
				@Override
				public double g(double t, double[] y)
						throws EventException {
					assert y != null;
					// check if (t,y) differs from cached result of g(t,y)
					if (Double.compare(data.t, t) != 0 || !Arrays.equals(data.y, y)) {
						// then update cache
						if (data.y == null || data.y.length != y.length) {
							data.y = y.clone();
						} else {
							System.arraycopy(y, 0, data.y, 0, y.length);
						}
						data.t = t;
						monitor.g(data.gout, t, y);
					}
					return data.gout[i];
				}
				@Override
				public void resetState(double t, double[] y)
						throws EventException {
					throw new EventException("NOT IMPLEMENTED");
				}
			};
			integrator.addEventHandler(eventHandler, MAX_CHECK_INTERVAL, CONVERGENCE, MAX_ITERATION_COUNT);
		}
	}

	@Override
	public void setOptions(Map options) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTolerances(double[] absTol, double[] relTol) {
		// Apache Commons Math does not allow to adjust tolerances later on
		// instead, these must be passed as arguments to the constructor
		// however, the parameter list for the constructor is different for different integrators
		// for now, setting tolerances here is silently ignored
		// instead, the user has to set a new solver to set tolerances
	}

}
