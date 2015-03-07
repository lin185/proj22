package heap;

import chainexception.ChainException;
import global.RID;
import bufmgr.*;
import global.*;
import diskmgr.*;
public class HeapFile {
	HFPage rootHFPage;
	PageId rootPageId;
	public HeapFile(String name){
		if((rootPageId= Minibase.DiskManager.get_file_entry(name) ) == null)	{ 
		
			Page newPage = new Page();
			rootPageId = Minibase.BufferManager.newPage(newPage, 1);
			rootHFPage = new HFPage(newPage);
			rootHFPage.setPage(newPage);
			rootHFPage.setCurPage(rootPageId);
			PageId leftChild = new PageId(GlobalConst.INVALID_PAGEID);
			PageId rightChild = new PageId(GlobalConst.INVALID_PAGEID);
			rootHFPage.setNextPage(rightChild);
			rootHFPage.setPrevPage(leftChild);
			Minibase.DiskManager.add_file_entry(name, rootPageId);
			Minibase.BufferManager.unpinPage(rootPageId, true);
			
		}
	}
	
	public RID insertRecord(byte[] record) throws ChainException{
		RID rid = new RID();
		PageId curPageId = new PageId(rootPageId.pid);
		HFPage curHFPage = new HFPage();
		Minibase.BufferManager.pinPage(rootPageId, rootHFPage, false);
		if()
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
