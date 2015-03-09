package heap;

import java.util.Arrays;

public class Tuple {
	byte[] data;
	
	public Tuple(byte[] byteArray, int i, int recLength) {
		int size = recLength - i;
		data = new byte[size];
		
		if(size > byteArray.length)
			size = byteArray.length;
		
		for(int j = 0; j < size; j++){
			data[j] = byteArray[i++];
		}
	}

	public Tuple() {
		data = null;
	}
	
	public Tuple(byte[] data) {
		this.data = new byte[data.length];
		this.data = Arrays.copyOf(data, data.length);
	}

	public int getLength(){
		if(data == null)
			return 0;
		
		return data.length;
	}

	public byte[] getTupleByteArray() {
		return data;
	}


}
