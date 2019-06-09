package de.grogra.numeric.cvode;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * This structure represents an N-dimensional vector of double.
 * 
 * @author Reinhard Hemmerling
 *
 */
public class Example2 {
public interface CLibrary extends Library {
	
	public static class N_VNew_Serial extends Structure {
		public static class ByReference extends N_VNew_Serial implements Structure.ByReference {}

		public int numVals;
		public Pointer vals; // double*
	}
	
	public static native N_Vector N_VNew_Serial(NativeLong vec_length);
	
	public static void main(String[] args) {
		final CLibrary clib = (CLibrary)Native.loadLibrary("sundials_nvecserial", CLibrary.class);
		final CLibrary.N_VNew_Serial.ByReference ex10val = new CLibrary.N_VNew_Serial.ByReference();
		ex10val.numVals = 100;
		// allocate memory for the 100 double values
		ex10val.vals = new Memory(100 * Native.getNativeSize(Double.TYPE));
		// note: Memory instance (and its associated memory allocation) will be freed when ex10val goes out of scope
		// fill in 100 double values into the block of allocated memory
		for (int dloop=0; dloop<100; dloop++) {
			// fill in junk values just for the sake of this example
			ex10val.vals.setDouble(dloop * Native.getNativeSize(Double.TYPE), ((double)dloop + 100) / 100);
		}
		// call the C function
		double ex10total = clib.example10_sendStruct(ex10val);
		System.out.println("example 10: " + ex10total);

	}
}
}