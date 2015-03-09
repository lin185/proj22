package heap;

import global.Minibase;
import global.PageId;
import global.RID;

import java.util.ArrayList;

import chainexception.ChainException;

public class HeapScan {
	ArrayList<PageId> insertOrder;
	HFPage cur_hfp;
	int cur_slotno;
	int cur_PageCount;
	public HeapScan(ArrayList<PageId> insertOrder) {
		this.insertOrder = insertOrder;
		cur_hfp = new HFPage();
		Minibase.BufferManager.pinPage(insertOrder.get(0), cur_hfp, false);
		//Minibase.BufferManager.unpinPage(heap.get(0), false);
		cur_slotno = 0;
		cur_PageCount = 0;
		System.out.printf("Construct new HeapScan  root_hfp_id: %d\n", cur_hfp.getCurPage().pid);
	}

	public Tuple getNext(RID rid) {
		
		while(true) {
			if(cur_slotno == cur_hfp.getSlotCount()){
				cur_PageCount++;
				if(cur_PageCount == insertOrder.size()){
					for(int i = 0; i < insertOrder.size(); i++)
						Minibase.BufferManager.unpinPage(insertOrder.get(i), false);
					return null;
				}
				Minibase.BufferManager.pinPage(insertOrder.get(cur_PageCount), cur_hfp, false);
				cur_slotno = 0;
			}
			
			if(cur_hfp.getSlotOffset(cur_slotno) != 0) {
				RID tuple_rid = new RID(cur_hfp.getCurPage(), cur_slotno);
				//System.out.printf("HeapScan getNext(rid) --- [%d, %d]\n", tuple_rid.pageno.pid, tuple_rid.slotno);
				byte[] data = cur_hfp.selectRecord(tuple_rid);
				//System.out.println(Arrays.toString(data));
				Tuple t = new Tuple(data);
				rid.copyRID(tuple_rid);
				cur_slotno++;
				return t;
			} else {
				cur_slotno++;
			}
		}
	}
	
	public void close() throws ChainException{
	}
}
