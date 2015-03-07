package heap;

import chainexception.ChainException;
import global.RID;

public class HeapFile {
	public HeapFile(String name){}
	
	public RID insertRecord(byte[] record) throws ChainException{
		RID rid = null;
		
		return rid;
	}
	
	public Tuple getRecord(RID rid) {
		Tuple t = null;
		
		return t;
	}
	
	public boolean updateRecord(RID rid, Tuple newRecord) throws ChainException {
		return false;
	}

	public boolean deleteRecord(RID rid){
		return false;
		
	}

	public int getRecCnt(){
		return 0;
	}
	
	public HeapScan openScan() {
		HeapScan hs = null;
		return hs;
	}

}
