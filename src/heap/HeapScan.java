package heap;

import java.util.ArrayList;
import java.util.Arrays;

import global.Minibase;
import global.PageId;
import global.RID;
import chainexception.ChainException;

public class HeapScan {
	ArrayList<PageId> heap;
	HFPage cur_hfp;
	int cur_slotno;
	int cur_PageCount;
	public HeapScan(ArrayList<PageId> heap) {
		/*this.root_pid = root_pid;
		//pin the root page
		root_hfp = new HFPage();
		Minibase.BufferManager.pinPage(root_pid, root_hfp, false);
		System.out.printf("Construct new HeapScan  root_hfp_id: %d\n", root_hfp.getCurPage().pid);
		*/
		this.heap = heap;
		cur_hfp = new HFPage();
		Minibase.BufferManager.pinPage(heap.get(0), cur_hfp, false);
		//Minibase.BufferManager.unpinPage(heap.get(0), false);
		cur_slotno = 0;
		cur_PageCount = 0;
		System.out.printf("Construct new HeapScan  root_hfp_id: %d\n", cur_hfp.getCurPage().pid);
	}

	public Tuple getNext(RID rid) {
		
		if(cur_slotno == cur_hfp.getSlotCount()){
			cur_PageCount++;
			if(cur_PageCount == heap.size()){
				for(int i = 0; i < heap.size(); i++)
					Minibase.BufferManager.unpinPage(heap.get(i), false);
				return null;
			}
			Minibase.BufferManager.pinPage(heap.get(cur_PageCount), cur_hfp, false);
			cur_slotno = 0;
		}
		RID tuple_rid = new RID(cur_hfp.getCurPage(), cur_slotno);
		System.out.printf("HeapScan getNext(rid) --- [%d, %d]\n", tuple_rid.pageno.pid, tuple_rid.slotno);
		byte[] data = cur_hfp.selectRecord(tuple_rid);
		System.out.println(Arrays.toString(data));
		Tuple t = new Tuple(data);
		rid.copyRID(tuple_rid);
		cur_slotno++;
		return t;
	}
	
	public void close() throws ChainException{}
}
