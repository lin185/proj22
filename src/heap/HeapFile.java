package heap;

import global.GlobalConst;
import global.Minibase;
import global.PageId;
import global.RID;

import java.util.ArrayList;
import java.util.LinkedList;

import chainexception.ChainException;


public class HeapFile {
	//HFPage rootHFPage;
	//PageId rootPageId;
	ArrayList<PageId> heap;
	
	int records_count;
	
	
	public HeapFile(String name){
		records_count = 0;
		heap = new ArrayList<PageId>();
		PageId rootPageId = Minibase.DiskManager.get_file_entry(name);
		if( rootPageId == null )	{ 
			//Root Heap File does not exist
			HFPage rootHFPage = new HFPage();
			rootPageId = Minibase.BufferManager.newPage(rootHFPage, 1);
			rootHFPage.setCurPage(rootPageId);
					
			PageId leftChild = new PageId(GlobalConst.INVALID_PAGEID);
			PageId rightChild = new PageId(GlobalConst.INVALID_PAGEID);
			rootHFPage.setNextPage(rightChild);
			rootHFPage.setPrevPage(leftChild);
			
			heap.add(rootPageId);
			//heap.add(leftChild);
			//heap.add(rightChild);
			
			Minibase.DiskManager.add_file_entry(name, rootPageId);
			Minibase.BufferManager.unpinPage(rootPageId, true);
			

		} else {
			//Root Heap File exists
			HFPage rootHFPage = new HFPage();
			Minibase.BufferManager.pinPage(rootPageId, rootHFPage, false);
			heap.add(rootPageId);
			/**********************
			might need to unpind root in buildHeapArray
			**********************/

			buildHeapArray(rootHFPage);
			//update heap
			System.out.println("Global_count: " + records_count + "\n" + heap.toString());
			//update global count
			return;
		}
	
		printHeap(0, 0);

		/*System.out.println("HeapFile Constructor");

		
		//open hf1 file, if not exsit create it
		PageId pid = Minibase.DiskManager.get_file_entry("hf1");
		HFPage hfpage = new HFPage();
		if(pid == null) {
			pid = Minibase.BufferManager.newPage(hfpage, 1);
			Minibase.DiskManager.add_file_entry("hf1", pid);
		}
		
		hfpage.setCurPage(pid);

		//HFPage hfpage = new HFPage();
		//hfpage.setCurPage(pid);
		PageId nextpageid = new PageId(GlobalConst.INVALID_PAGEID);
		PageId prevpageid = new PageId(GlobalConst.INVALID_PAGEID);
		hfpage.setNextPage(nextpageid);
		hfpage.setPrevPage(prevpageid);
		
		RID rid = hfpage.insertRecord("hello".getBytes());

		hfpage.print();
	
		//page.setData(hfpage.getData());
		//Minibase.BufferManager.unpinPage(pid, true);

		PageId newpid = Minibase.DiskManager.get_file_entry("hf1");
		if(newpid != null) {
			System.out.println("hf1 exists.");
			HFPage newhfpage = new HFPage();
			Minibase.BufferManager.pinPage(hfpage.getCurPage(), newhfpage, false);
			newhfpage.print();
		
		}
		Minibase.BufferManager.unpinPage(newpid, true);
		HeapFile h = new HeapFile("hf1");
*/

	}
	private void buildHeapArray(HFPage rootHFPage){
		LinkedList<HFPage> q = new LinkedList<HFPage>();
		q.add(rootHFPage);
		HFPage root;
		while(!q.isEmpty()){
			root = q.removeFirst();
			records_count+= root.getSlotCount();
			//pushing all the child node in the heapArray and queue
			PageId leftChild = rootHFPage.getPrevPage();
			PageId rightChild = rootHFPage.getNextPage();
			HFPage leftChildHFPage = new HFPage();
			HFPage rightChildHFPage = new HFPage();
			if(leftChild.pid != GlobalConst.INVALID_PAGEID)
				Minibase.BufferManager.pinPage(leftChild, leftChildHFPage, false);
			if(rightChild.pid != GlobalConst.INVALID_PAGEID)
				Minibase.BufferManager.pinPage(rightChild, rightChildHFPage, false);
			//Minibase.BufferManager.unpinPage(root.getCurPage(), true);
			if(leftChild.pid != GlobalConst.INVALID_PAGEID){
				heap.add(leftChild);
				q.add(leftChildHFPage);
			}
			if(rightChild.pid != GlobalConst.INVALID_PAGEID){
				heap.add(leftChild);
				q.add(rightChildHFPage);
			}
		}
	}
	public RID insertRecord(byte[] record) throws ChainException{
		RID rid = new RID();
		
		//PageId cpid = new PageId(rootPageId.pid);
		//PageId ppid = new PageId(GlobalConst.INVALID_PAGEID);
		
		
		//HFPage curHFPage = rootHFPage;
		PageId rootPageId = heap.get(0);
		HFPage rootHFPage = new HFPage();
		Minibase.BufferManager.pinPage(rootPageId, rootHFPage, false);
		
		
		if(rootHFPage.getFreeSpace() <= record.length) {	//why =?
			//Need to create a new page
			System.out.println("NO ENOUGH SPACE!");
			
			//Create a new heap file page
			PageId newPageId = new PageId();
			HFPage newHFPage = new HFPage();
			newPageId = Minibase.BufferManager.newPage(newHFPage, 1);
			newHFPage.setCurPage(newPageId);

			//set left, right children
			PageId leftChild = new PageId(GlobalConst.INVALID_PAGEID);
			PageId rightChild = new PageId(GlobalConst.INVALID_PAGEID);
			newHFPage.setNextPage(rightChild);
			newHFPage.setPrevPage(leftChild);
			
			//insert record
			rid = newHFPage.insertRecord(record);
			
			//insert new page id into heap
			heap.add(newPageId);
			
			//update heap, swap upward
			updateHeapUpward();
			
			Minibase.BufferManager.unpinPage(newPageId, true);

		} else {
			//We have enough space, insert the records. 
			rid = rootHFPage.insertRecord(record);
			if(rid != null)
				System.out.printf("Insert Record! RID<%d, %d>\n", rid.pageno.pid, rid.slotno);
			else 
				System.out.printf("error rid == null\n");
			
			//update heap, swap downward
			updateHeapDownward();
		}
		
		Minibase.BufferManager.unpinPage(rootPageId, true);
		records_count++;
		return rid;
	}
	
	
	private void updateHeapUpward() {
		boolean UPDATED = false;
		
		int child_index = heap.size() - 1;
		int parent_index = findParent(child_index);
		
		PageId child_pid = heap.get(child_index);
		PageId parent_pid = heap.get(parent_index);
		
		HFPage child_hfp = new HFPage();
		HFPage parent_hfp = new HFPage();
		
		Minibase.BufferManager.pinPage(child_pid, child_hfp, false);
		Minibase.BufferManager.pinPage(parent_pid, parent_hfp, false);
		
		while(child_hfp.getFreeSpace() > parent_hfp.getFreeSpace()) {
			//swap parent and child
			
			//swap elements in the heap
			PageId temp_pid = parent_pid;
			heap.set(parent_index, child_pid);
			heap.set(child_index, temp_pid);
			
			
			//update parent HFPage pointers (setPrevPage, setNextPage)
			PageId new_parent_pid = heap.get(parent_index);
			HFPage new_parent_hfp = new HFPage();
			Minibase.BufferManager.pinPage(new_parent_pid, new_parent_hfp, false);
			updateHFPagePtr(new_parent_hfp, parent_index);
			Minibase.BufferManager.unpinPage(new_parent_pid, true);
			
			//update child HFPage pointers (setPrevPage, setNextPage)
			PageId new_child_pid = heap.get(child_index);
			HFPage new_child_hfp = new HFPage();
			Minibase.BufferManager.pinPage(new_child_pid, new_child_hfp, false);
			updateHFPagePtr(new_child_hfp, child_index);
			Minibase.BufferManager.unpinPage(new_child_pid, true);
			
			//unpin pages after previous/next pointers are updated
			Minibase.BufferManager.unpinPage(child_pid, true);
			Minibase.BufferManager.unpinPage(parent_pid, true);
			
			//update parent and child index, and pageid, and hfpage object
			child_index = parent_index;
			parent_index = findParent(child_index);
			
			child_pid = heap.get(child_index);
			parent_pid = heap.get(parent_index);
			
			Minibase.BufferManager.pinPage(child_pid, child_hfp, false);
			Minibase.BufferManager.pinPage(parent_pid, parent_hfp, false);
			
			UPDATED = true;
		}
		
		//unpin pages before return
		Minibase.BufferManager.unpinPage(child_pid, true);
		Minibase.BufferManager.unpinPage(parent_pid, true);
		
		
		if(UPDATED) 
			printHeap(0, 0);

	}
	
	private void updateHeapDownward() {
		boolean UPDATED = false;
		
		int parent_index = 0;
		int left_index = 1;
		int right_index = 2;
		int target_index = 0;
		
		PageId parent_pid = null;
		PageId left_pid = null;
		PageId right_pid = null;
		PageId target_pid = null;
		
		HFPage parent_hfp = null;
		HFPage left_hfp = null;
		HFPage right_hfp = null;
		HFPage target_hfp = null;
		
		short parent_freespace = 0;
		short left_freespace = 0;
		short right_freespace = 0;
		
		while(true) {
			//root hfpage object
			parent_pid = heap.get(parent_index);
			parent_hfp = new HFPage();	
			Minibase.BufferManager.pinPage(parent_pid, parent_hfp, false);
			parent_freespace = parent_hfp.getFreeSpace();
			//
			
			//left child hfpage object
			if(left_index < heap.size()) {
				left_pid = heap.get(left_index);
				left_hfp = new HFPage();
				Minibase.BufferManager.pinPage(left_pid, left_hfp, false);
				left_freespace = left_hfp.getFreeSpace();
				Minibase.BufferManager.unpinPage(left_pid, false);
			}
			
			//right child hfpage object
			if(right_index < heap.size()) {
				right_pid = heap.get(right_index);
				right_hfp = new HFPage();
				Minibase.BufferManager.pinPage(right_pid, right_hfp, false);
				right_freespace = right_hfp.getFreeSpace();
				Minibase.BufferManager.unpinPage(right_pid, false);
			}
			
			//Swapping
			if(left_freespace > right_freespace && left_freespace > parent_freespace) {
					//swap left child with parent
					/*updateHFPagePtr(parent_hfp, left_index);
					updateHFPagePtr(left_hfp, parent_index);
					parent_index = left_index;
					Minibase.BufferManager.unpinPage(left_pid, true);*/
					target_index = left_index;
			} else if(right_freespace > left_freespace && right_freespace > parent_freespace) {
					//swap right child with parent
					/*updateHFPagePtr(parent_hfp, right_index);
					updateHFPagePtr(right_hfp, parent_index);
					parent_index = right_index;
					Minibase.BufferManager.unpinPage(right_pid, true);*/
					target_index = right_index;
			} else {
				Minibase.BufferManager.unpinPage(parent_pid, false);
				break;
			}
	
				//swap			
				target_pid = heap.get(target_index);
				target_hfp = new HFPage();	
				Minibase.BufferManager.pinPage(target_pid, target_hfp, false);
				updateHFPagePtr(parent_hfp, target_index);
				updateHFPagePtr(target_hfp, parent_index);
				Minibase.BufferManager.unpinPage(target_pid, false);
				Minibase.BufferManager.unpinPage(parent_pid, false);
				
				//swap elements in the heap
				PageId temp_pid = parent_pid;
				heap.set(parent_index, target_pid);
				heap.set(target_index, temp_pid);
				
				left_index = findLeftChild(parent_index);
				right_index = findRightChild(parent_index);
				left_freespace = 0;
				right_freespace = 0;
				//Minibase.BufferManager.unpinPage(parent_pid, true);
				
				UPDATED = true;
		
		}
		
		if(UPDATED)
			printHeap(0, 0);

	}
	
	private void updateHFPagePtr(HFPage hfp, int index) {
		int l = findLeftChild(index);
		int r = findRightChild(index);
		if(l < heap.size()) {
			hfp.setPrevPage(heap.get(l));
		} else {
			hfp.setPrevPage(new PageId(GlobalConst.INVALID_PAGEID));
		}
		if(r < heap.size()) {
			hfp.setNextPage(heap.get(r));
		} else {
			hfp.setNextPage(new PageId(GlobalConst.INVALID_PAGEID));
		}
	}

	
	private void printHeap(int parent_index, int tap){
		if(parent_index >= heap.size())
			return;
		
		int left = findLeftChild(parent_index);
		int right = findRightChild(parent_index);
		if(tap == 0)
			System.out.println("------------HEAP------------");
		
		
		for(int i = 0; i<tap; i++)
			System.out.print("\t");
		
		PageId pid = heap.get(parent_index);
		HFPage hfp = new HFPage();
		Minibase.BufferManager.pinPage(pid, hfp, false);
		System.out.printf("Pid[%d]: ", pid.pid);
		System.out.printf("free(%d), ", hfp.getFreeSpace());
		System.out.printf("rcnt(%d), ", hfp.getSlotCount());
		System.out.printf("left(%d), ", hfp.getPrevPage().pid);
		System.out.printf("right(%d)", hfp.getNextPage().pid);
		Minibase.BufferManager.unpinPage(pid, false);
		System.out.printf("\n");
		
		printHeap(left, tap+1); 
		printHeap(right, tap+1);
		if(tap == 0)
			System.out.println("----------------------------");

		
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
		return records_count;
	}
	
	public HeapScan openScan() {
		HeapScan hs = null;
		return hs;
	}
	
	
	public int findParent(int i) {
		return (i-1)/2;
	}
	
	public int findLeftChild(int parent_index) {
		return parent_index * 2 + 1;
	}
	
	public int findRightChild(int parent_index) {
		return parent_index * 2 + 2;
	}
}
