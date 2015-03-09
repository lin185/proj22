package heap;

import java.util.Arrays;

public class Tuple {
	byte[] data;
	
	public Tuple(byte[] byteArray, int i, int recLength) {
		// TODO Auto-generated constructor stub
	}

	public Tuple() {
		// TODO Auto-generated constructor stub
	}
	public Tuple(byte[] data) {
		//this.data = new byte[data.length];
		this.data = Arrays.copyOf(data, data.length);
	}

	public int getLength(){
		return data.length;
	}

	public byte[] getTupleByteArray() {
		// TODO Auto-generated method stub
		return data;
	}


}
