package de.grogra.numeric;

//import static de.grogra.numeric.cvode.CVODE.CVDense;
import static de.grogra.numeric.cvode.CVODE.CV_BDF;
import static de.grogra.numeric.cvode.CVODE.CV_NEWTON;
import static de.grogra.numeric.cvode.CVODE.CV_NORMAL;
import static de.grogra.numeric.cvode.CVODE.CV_ROOT_RETURN;
import static de.grogra.numeric.cvode.CVODE.CV_SUCCESS;
import static de.grogra.numeric.cvode.CVODE.CV_TOO_MUCH_WORK;
import static de.grogra.numeric.cvode.CVODE.CVode;
import static de.grogra.numeric.cvode.CVODE.CVodeCreate;
import static de.grogra.numeric.cvode.CVODE.CVodeFree;
import static de.grogra.numeric.cvode.CVODE.CVodeGetReturnFlagName;
import static de.grogra.numeric.cvode.CVODE.CVodeGetRootInfo;
import static de.grogra.numeric.cvode.CVODE.CVodeInit;
import static de.grogra.numeric.cvode.CVODE.CVodeRootInit;
import static de.grogra.numeric.cvode.CVODE.CVodeSStolerances;
import static de.grogra.numeric.cvode.CVODE.CVodeSVtolerances;
import static de.grogra.numeric.cvode.N_Vec_Serial.N_VDestroy_Serial;
import static de.grogra.numeric.cvode.N_Vec_Serial.N_VNew_Serial;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.FirstOrderIntegrator;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.StepHandler;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.PointerByReference;

import de.grogra.numeric.cvode.CVRhsFn;
import de.grogra.numeric.cvode.CVRootFn;
import de.grogra.numeric.cvode.N_Vector;

/**
 * Wrapper to CVODE2 library.
 * It implements the Solver interface and delegates computation
 * to the CVODE2 library, which is part of SUNDIALS.
 * 
 * requires 2 libraries ::> sundials_covde.dll + sundials_nvecserial.dll
 * 
 * based on CVodeAdapterOriginal.java by Reinhard Hemmerling
 * extended by Jonas Coussement 2016
 *
 */
public class CVodeAdapter implements FirstOrderIntegrator {

//	public enum Options {
//		TEST
//	}
	
	// TODO add this to Solver interface
	// set additional options that affect behavior of the solver
//	void setOptions(Map<Options, Object> options) {
//		for (Map.Entry<Options, Object> e : options.entrySet()) {
//			switch (e.getKey()) {
//			case TEST:
//				// TODO
//				break;
//
//			default:
//				break;
//			}
//		}
//	}
	
	double absTolDefault = 1e-10;
	double relTolDefault = 1e-10;
	
	double[] absTol;
//	double[] relTol;
	
	int nrtfn;
	Monitor monitor;
	
	public void setMonitor(int n, Monitor monitor) throws NumericException
	{
		if (n < 0) throw new NumericException("number of monitor functions must be non-negative");
		this.nrtfn = n;
		this.monitor = monitor;
	}
	public double integrate(FirstOrderDifferentialEquations ode, double t0, double[] y0, double t1, double[] y1)
			throws DerivativeException, IntegratorException {
		assert y0.length == y1.length;
		//System.out.println("State received by solver intially (line called in CVODE Adapter): ");
		//System.out.println(Arrays.toString(y0));
		//System.out.println(Arrays.toString(y1));
		int flag;
		N_Vector y = null;
		Pointer cvode_mem = Pointer.NULL;
		
		// get problem dimension
		final int N = y0.length;

		// create callback for rate function
		CVRhsFn f = new CVRhsFn() {
			final double[] state = new double[N];
			final double[] rate = new double[N];
			@Override
			public int callback(double t, N_Vector y, N_Vector ydot,
					Pointer user_data) {
				try {
					assert N == y.getLength();
					assert N == ydot.getLength();
					y.get(state);
					ode.computeDerivatives(t,state,rate);
					ydot.set(rate);
					return 0;
				} catch (Throwable throwable) {
					throwable.printStackTrace();
				}
				// return negative value to indicate unrecoverable error
				return -1;
			}
			
		};

		// create callback for root functions
		CVRootFn g = new CVRootFn() {
			final double[] out = new double[nrtfn];
			final double[] y = new double[N];
			@Override
			public int callback(double t, N_Vector y, Pointer gout,
					Pointer user_data) {
				try {
					assert N == y.getLength();
					y.get(this.y);
					monitor.g(out, t, this.y);
					gout.write(0, out, 0, nrtfn);
					return 0;
				} catch (Throwable throwable) {
					throwable.printStackTrace();
				}
				// return non-zero value to indicate error
				return -1;
			}
		};
		
		/* try { */
			// alloc N_Vector
			y = N_VNew_Serial(new NativeLong(N));

			// create solver
			cvode_mem = CVodeCreate(CV_BDF, CV_NEWTON);
		/*
		 * if (cvode_mem == Pointer.NULL) throw new
		 * NumericException("could not create CVODE solver");
		 */
			// init solver
			y.set(y0);
			
			flag = CVodeInit(cvode_mem, f, t0, y);
		/* checkFlag(flag, "could not init CVODE solver"); */

			// set tolerances
			if (absTol != null) {
				// relative is scalar, absolute is vector 
				assert absTolDefault > 0;
				assert relTolDefault > 0;
				assert absTol.length == N;
				y.set(absTol);
				for (int i = 0; i < N; i++) {
					// replace zero tolerance by default one
					if (absTol[i] == 0)
						y.set(i, absTolDefault);
				}
				flag = CVodeSVtolerances(cvode_mem, relTolDefault, y);
			} else {
				// relative is scalar, absolute is scalar
				flag = CVodeSStolerances(cvode_mem, relTolDefault, absTolDefault);
			}
		/* checkFlag(flag, "could not set tolerances"); */
			
			// set optional inputs
			// TODO

			// set linear solver for newton iteration
			//flag = CVDense(cvode_mem, N);
		/* checkFlag(flag, "could not set linear solver"); */
			
			// set linear solver optional inputs
			// TODO
			
			// specify rootfinding problem
			CVodeRootInit(cvode_mem, nrtfn, g);
			
			// repeat integration until t1 reached
			while (true) {
				// perform actual integration
				DoubleByReference t = new DoubleByReference();
				long startTime = System.currentTimeMillis();
				
				
				flag = CVode(cvode_mem, t1, y, t, CV_NORMAL);
				System.out.println(System.currentTimeMillis() - startTime);
				System.out.println("t = " + t.getValue()  + " t1 " + t1);
				System.out.println("y = " + y.get(1));
				if (flag == CV_SUCCESS) {
					// break loop if target time was reached
					System.out.println("t = " + flag);
					break;
				} else if (flag == CV_ROOT_RETURN) {
					// one of the monitor functions triggered
					// find out which
					int[] rootsfound = new int[nrtfn];
					flag = CVodeGetRootInfo(cvode_mem, rootsfound);
				/* checkFlag(flag, "root was found, but could not determine which"); */
					boolean stop = false;
					for (int i = 0; i < nrtfn; i++) {
						if (rootsfound[i] != 0) {
							// call event handler
							stop |= monitor.handleEvent(i, t.getValue(), y1);
						}
					}
					if (stop) {
						break;
					}
				} else {
				/* checkFlag(flag, "error during integration"); */
				}
			}
			
		/*} catch (NumericException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {*/
			if (cvode_mem != Pointer.NULL) {
				// free solver
				PointerByReference p_cvode_mem = new PointerByReference(cvode_mem);
				CVodeFree(p_cvode_mem);
			}

			if (y != null) {
				// free N_Vector
				N_VDestroy_Serial(y);
			}
		/*}*/
		return 0d;
	}

	// throw an exception if flag is not CV_SUCCESS
	private void checkFlag(int flag, String msg) throws NumericException {
		if (flag == CV_TOO_MUCH_WORK){
			//added to override stopping at maximal number of steps (auth: Jonas Coussement)
		}else{	
			if (flag != CV_SUCCESS)
				throw new NumericException("[" + CVodeGetReturnFlagName(flag)
					+ "] " + msg);
		}
	}

	public void setOptions(Map options) {
		// TODO Auto-generated method stub
		
	}

	public void setTolerances(double[] absTol, double[] relTol) {
		assert absTol == null || relTol == null || absTol.length == relTol.length;
		this.absTol = absTol != null ? absTol.clone() : null;
//		this.relTol = relTol != null ? relTol.clone() : null;
		if (relTol != null) {
			// TODO output that setting relTol is unsupported
		}
	}

	public double getAbsTolDefault() {
		return absTolDefault;
	}

	public double getRelTolDefault() {
		return relTolDefault;
	}

	public void setAbsTolDefault(double value) {
		if(value<0) value = 1e-4;
		absTolDefault = value;
	}

	public void setRelTolDefault(double value) {
		if(value<0) value = 1e-4;
		relTolDefault = value;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addStepHandler(StepHandler handler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<StepHandler> getStepHandlers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearStepHandlers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addEventHandler(EventHandler handler, double maxCheckInterval, double convergence,
			int maxIterationCount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<EventHandler> getEventHandlers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearEventHandlers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getCurrentStepStart() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCurrentSignedStepsize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMaxEvaluations(int maxEvaluations) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getMaxEvaluations() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getEvaluations() {
		// TODO Auto-generated method stub
		return 0;
	}


	
}
