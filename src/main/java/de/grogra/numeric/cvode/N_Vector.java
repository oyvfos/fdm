package de.grogra.numeric.cvode;

import java.util.Arrays;
import java.util.List;

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
public class N_Vector extends Structure {
	
	public static class ByValue extends N_Vector implements Structure.ByValue {
	}

	public static class ByReference extends N_Vector implements
			Structure.ByReference {
	}

	public Pointer content	;
	public Pointer ops;

	@Override
	protected List getFieldOrder() {
		return Arrays.asList(new String[] { "content", "ops" });
	}

	public static class _N_VectorContent_Serial extends Structure {
		public NativeLong length;
		public int own_data;
		public Pointer data;
		
		public _N_VectorContent_Serial(Pointer p) {
			super(p);
		}
		@Override
		protected List getFieldOrder() {
			return Arrays.asList(new String[] { "length", "own_data" , "data" });
		}
	}

	/**
	 * Return the number of elements in this vector.
	 * @return
	 */
	public int getLength()
	{
		_N_VectorContent_Serial content = new _N_VectorContent_Serial(this.content);
		content.read();
		return content.length.intValue();
	}

	/**
	 * Return the element designated by index from the vector.
	 * @param index
	 * @return
	 */
	public double get(int index)
	{
		_N_VectorContent_Serial content = new _N_VectorContent_Serial(this.content);
		content.read();
		return content.data.getDouble(Native.getNativeSize(Double.TYPE)*index);
	}
	
	/**
	 * Set the element designated by index to value.
	 * @param index
	 * @param value
	 */
	public void set(int index, double value)
	{
		_N_VectorContent_Serial content = new _N_VectorContent_Serial(this.content);
		content.read();
		content.data.setDouble(Native.getNativeSize(Double.TYPE)*index, value);
	}
	
	/**
	 * Copy complete vector into the memory provided by data.
	 * @param data
	 */
	public void get(double[] data)
	{
		_N_VectorContent_Serial content = new _N_VectorContent_Serial(this.content);
		content.read();
		final int N = (int)content.length.longValue();
		assert N == data.length;
		content.data.read(0, data, 0, N);
	}
	
	/**
	 * Set the complete vector to the memory provided by data.
	 * @param data
	 */
	public void set(double[] data)
	{
		_N_VectorContent_Serial content = new _N_VectorContent_Serial(this.content);
		content.read();
		final int N = (int)content.length.longValue();
		assert data.length == N;
		content.data.write(0, data, 0, 1);
	}
}
