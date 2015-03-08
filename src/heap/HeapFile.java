package heap;

import global.GlobalConst;
import global.Minibase;
import global.PageId;
import global.RID;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import chainexception.ChainException;


public class HeapFile {
	//HFPage rootHFPage;
	//PageId rootPageId;
	ArrayList<PageId> heap;
	private int global_count;
	public HeapFile(String name){
		global_count = 0;
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
			System.out.println("Global_count: " + global_count + "\n" + heap.toString());
			//update global count
			return;
		}
	
/*	
		System.out.println("HeapFile Constructor");
		
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
			global_count+= root.getSlotCount();
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
			
			

		}
		
		Minibase.BufferManager.unpinPage(rootPageId, true);
		return rid;
	}
	
	
	private void updateHeapUpward() {
		
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
			updateHFPagePtr(parent_hfp, parent_index);
			
			//update child HFPage pointers (setPrevPage, setNextPage)
			updateHFPagePtr(child_hfp, child_index);
			
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
		}
		
		//unpin pages after previous/next pointers are updated
		Minibase.BufferManager.unpinPage(child_pid, true);
		Minibase.BufferManager.unpinPage(parent_pid, true);
		
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

	
	/*private void printHeap(){
		
	}*/
	
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
		return global_count;
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
	
	//public int linkParent()

}
