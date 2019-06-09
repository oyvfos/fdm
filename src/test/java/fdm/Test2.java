package fdm;

import static de.grogra.numeric.cvode.N_Vec_Serial.N_VNew_Serial;

import java.util.Arrays;

import com.sun.jna.NativeLong;

import de.grogra.numeric.cvode.N_Vector;

public class Test2 {
	
	public static void main(String[] args) {
		int N=100;
		//N_Vector y = null;
		double array[] = new double[100];
		Arrays.fill(array, 0, 3, -50);
		N_Vector y = N_VNew_Serial(new NativeLong(N));
		
	
	      y.set(array);
	 

	}

}
