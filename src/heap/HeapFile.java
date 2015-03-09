package heap;

import global.GlobalConst;
import global.Minibase;
import global.PageId;
import global.RID;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;

import chainexception.ChainException;


public class HeapFile {

	ArrayList<PageId> heap;
	ArrayList<PageId> insertOrder;
	int records_count;
	String filename;	
	
	HFPage rootHFPage;
	PageId rootPageId;
	
	PageId root_directory_pid;
	HFPage root_directory_hfp;
	PageId curr_directory_pid;
	HFPage curr_directory_hfp;
	
	public HeapFile(String name){
		records_count = 0;
		insertOrder = new ArrayList<PageId>(); 
		filename = name;
		heap = new ArrayList<PageId>();
		rootPageId = Minibase.DiskManager.get_file_entry(name);
		if( rootPageId == null )	{
			//Root Heap File does not exist
			rootHFPage = new HFPage();
			rootPageId = Minibase.BufferManager.newPage(rootHFPage, 1);
			rootHFPage.setCurPage(rootPageId);
					
			PageId leftChild = new PageId(GlobalConst.INVALID_PAGEID);
			PageId rightChild = new PageId(GlobalConst.INVALID_PAGEID);
			rootHFPage.setNextPage(rightChild);
			rootHFPage.setPrevPage(leftChild);
			
			heap.add(rootPageId);
			insertOrder.add(rootPageId);
			//heap.add(leftChild);
			//heap.add(rightChild);
			
			Minibase.DiskManager.add_file_entry(name, rootPageId);
			Minibase.BufferManager.unpinPage(rootPageId, true);
			
			//Create a directory
			root_directory_hfp = new HFPage();
			root_directory_pid = Minibase.BufferManager.newPage(root_directory_hfp, 1);
			root_directory_hfp.setCurPage(root_directory_pid);
			root_directory_hfp.insertRecord(ByteBuffer.allocate(4).putInt(rootPageId.pid).array());
			Minibase.DiskManager.add_file_entry(name+"_directory", root_directory_pid);
			Minibase.BufferManager.unpinPage(root_directory_pid, true);
			
		} else {
			//Root Heap File exists
			
			//reconstruct heap array
			buildHeapArray();
			
			//reconstruct root directory
			buildOrderArray();
			
			//System.out.println()
			
			//System.out.println("Global_count: " + records_count + "\n" + heap.toString() );
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
	private void buildOrderArray() {
		root_directory_pid = Minibase.DiskManager.get_file_entry(filename+"_directory");
		root_directory_hfp = new HFPage();
		Minibase.BufferManager.pinPage(root_directory_pid, root_directory_hfp, false);
	
		int slotcount = root_directory_hfp.getSlotCount();
		for(int i=0; i<slotcount; i++) {
			if(root_directory_hfp.getSlotOffset(i) != 0) {
				RID rid = new RID(root_directory_pid, i);
				byte[] record = root_directory_hfp.selectRecord(rid);
				int order = new BigInteger(record).intValue();
				PageId pid = new PageId(order);
				insertOrder.add(pid);
			}
		}
		Minibase.BufferManager.unpinPage(root_directory_pid, true);
		
	}
	
	
	
	private void buildHeapArray(){
		rootHFPage = new HFPage();
		Minibase.BufferManager.pinPage(rootPageId, rootHFPage, false);
		heap.add(rootPageId);
		
		
		LinkedList<HFPage> q = new LinkedList<HFPage>();
		q.add(rootHFPage);
		//System.out.printf("RootId: %d RootPre %d RootNext %d\n",  rootHFPage.getCurPage().pid, rootHFPage.getPrevPage().pid, rootHFPage.getNextPage().pid);
		HFPage root;
		while(!q.isEmpty()){
			root = q.remove(0);

		/*System.out.println("==heapId====");
			for(int i = 0; i < q.size(); i++){
				System.out.println(q.get(i).getCurPage().pid);
			}
			System.out.println("==========");
*/
			
			int slotcount = root.getSlotCount();
			for(int i=0; i<slotcount; i++) {
				int offset = root.getSlotOffset(i);
				if(offset != 0)
					records_count++;
			}
			
			
			//pushing all the child node in the heapArray and queue
//		System.out.printf("RootId: %d RootPre %d RootNext %d\n",  root.getCurPage().pid, root.getPrevPage().pid, root.getNextPage().pid);
			PageId leftChild = root.getPrevPage();
			PageId rightChild = root.getNextPage();
			HFPage leftChildHFPage = new HFPage();
			HFPage rightChildHFPage = new HFPage();
			if(leftChild.pid != GlobalConst.INVALID_PAGEID)
				Minibase.BufferManager.pinPage(leftChild, leftChildHFPage, false);
			if(rightChild.pid != GlobalConst.INVALID_PAGEID)
				Minibase.BufferManager.pinPage(rightChild, rightChildHFPage, false);
			//System.out.printf("left %d, right %d", leftChild.pid, rightChild.pid);
			if(leftChild.pid != GlobalConst.INVALID_PAGEID){
				heap.add(leftChild);
				q.addLast(leftChildHFPage);
				Minibase.BufferManager.unpinPage(leftChild, false);
			}
			if(rightChild.pid != GlobalConst.INVALID_PAGEID){
				heap.add(rightChild);
				q.addLast(rightChildHFPage);
				Minibase.BufferManager.unpinPage(rightChild, false);
			}	
		}
		
		Minibase.BufferManager.unpinPage(rootPageId, true);
	}
	
	
	public RID insertRecord(byte[] record) throws ChainException{
		if(record.length > GlobalConst.MAX_TUPSIZE)
			throw new SpaceNotAvailableException(null, "SpaceNotAvailableException");
		
		RID rid = new RID();
		
		//PageId cpid = new PageId(rootPageId.pid);
		//PageId ppid = new PageId(GlobalConst.INVALID_PAGEID);
		//ootNexte curHFPage = rootHFPage;
		PageId rootPageId = heap.get(0);
		HFPage rootHFPage = new HFPage();
		Minibase.BufferManager.pinPage(rootPageId, rootHFPage, false);
		
		
		if(rootHFPage.getFreeSpace() <= record.length) {	//why =?
			//Need to create a new page
			//System.out.println("NO ENOUGH SPACE!");
			
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
			
			//insert new page id into heap, and order array
			heap.add(newPageId);
			insertOrder.add(newPageId);
			root_directory_hfp.insertRecord(ByteBuffer.allocate(4).putInt(newPageId.pid).array());
			
		
			//update heap, swap upward
			int start_index = heap.size() - 1;
			updateHeapUpward(start_index);
			
			Minibase.BufferManager.unpinPage(newPageId, true);

		} else {
			//We have enough space, insert the records. 
			rid = rootHFPage.insertRecord(record);
			/*if(rid != null)
				System.out.printf("Insert Record! RID<%d, %d>\n", rid.pageno.pid, rid.slotno);
			else 
				System.out.printf("error rid == null\n");*/
			
			//update heap, swap downward
			updateHeapDownward();
		}
		PageId newRootId = heap.get(0);
		HFPage newRootHFP = new HFPage();
		Minibase.BufferManager.pinPage(newRootId, newRootHFP, false);
		Minibase.DiskManager.delete_file_entry(filename);
		Minibase.DiskManager.add_file_entry(filename, newRootId);
		Minibase.BufferManager.unpinPage(newRootId, true);
		Minibase.BufferManager.unpinPage(rootPageId, true);
		records_count++;
		return rid;
	}
	
	
	private void updateHeapUpward(int start_index) {
		boolean UPDATED = false;
		
		int child_index = start_index;
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
		
		//update parent-child relation
		if(child_index != 0) { //not the root
			if(child_index % 2 == 1) { //odd index, it is left child
				parent_hfp.setPrevPage(child_pid);
			} else { //even index, it is right child
				parent_hfp.setNextPage(child_pid);
			}
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

	
	public void printHeap(int parent_index, int tap){
		/*if(parent_index >= heap.size())
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
*/
		
	}
	
	public Tuple getRecord(RID rid) {
		Tuple t = null;
		
		HFPage hfp = new HFPage();
		Minibase.BufferManager.pinPage(rid.pageno, hfp, false);
		 byte[] data = hfp.selectRecord(rid);
		t = new Tuple(data);
		Minibase.BufferManager.unpinPage(rid.pageno, false);
		return t;
	}
	
	public boolean updateRecord(RID rid, Tuple newRecord) throws ChainException {
	
		Tuple t = getRecord(rid);
		//System.out.println("oldRecordSize: " + t.getLength() + "newRecord: " + newRecord.getLength());
		if(t.getLength() != newRecord.getLength())
			throw new InvalidUpdateException(null, "InvalidUpdate");;
		
		HFPage hfp = new HFPage();
		Minibase.BufferManager.pinPage(rid.pageno, hfp, false);
		hfp.updateRecord(rid, newRecord);
		Minibase.BufferManager.unpinPage(rid.pageno, false);
		return true;
	}

	public boolean deleteRecord(RID rid){
		if(rid == null)
			return false;
		
		//System.out.printf("Delete Record! RID<%d, %d>\n", rid.pageno.pid, rid.slotno);
		
		PageId pid = rid.pageno;
		HFPage hfp = new HFPage();
		int index = 0;
		for(int i=0; i<heap.size(); i++) {
			if(pid.pid == heap.get(i).pid) {
				index = i;
				//System.out.printf("find index: %d\n", index);
				break;
			}
		}
		
		Minibase.BufferManager.pinPage(pid, hfp, false);
		hfp.deleteRecord(rid);
		Minibase.BufferManager.unpinPage(pid, true);
		updateHeapUpward(index);
		
		PageId newRootId = heap.get(0);
		HFPage newRootHFP = new HFPage();
		Minibase.BufferManager.pinPage(newRootId, newRootHFP, false);
		Minibase.DiskManager.delete_file_entry(filename);
		Minibase.DiskManager.add_file_entry(filename, newRootId);
		Minibase.BufferManager.unpinPage(newRootId, true);
		records_count--;
		
		
		return true;
		
	}

	public int getRecCnt(){
		return records_count;
	}
	
	public HeapScan openScan() {
		HeapScan hs = new HeapScan(insertOrder);		
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
