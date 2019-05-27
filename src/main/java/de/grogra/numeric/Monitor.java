package de.grogra.numeric;

/**
 * This class represents a set of monitor functions.
 * Each monitor function gi(t,y) calculates a single value
 * for a node (t,y). Multiple monitor functions are calculated
 * by a single function g(t,y) at once. When the return value
 * of one of the gi changes its sign, rootfinding methods are
 * used to determine the exact time t of the first event (when
 * gi(t,y) is 0). Then, for each event i that triggered, the 
 * function handleEvent(i,t,y) is called. The event handler
 * will return true if integration should stop. Multiple
 * events triggered, integration will stop if at least one
 * of the gi returned true.
 * 
 * @author Reinhard Hemmerling
 *
 */
public interface Monitor {
	/**
	 * Evaluate all monitor functions at once.
	 * @param out 
	 * @param t time
	 * @param y state
	 */
	void g(double[] out, double t, double[] y);
	/**
	 * Event handler must return true if integration should stop.
	 * @param i
	 * @param t time
	 * @param y state
	 * @return true if integration should stop
	 */
	boolean handleEvent(int i, double t, double[] y);
}
