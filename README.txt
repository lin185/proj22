ChiaHao Chen
Binhao Lin

For Storing HFPages we decided to use a Binary Max Heap Tree data structure.

We are using "NextPage" and "PrevPage" pageId pointers in HFPage class as
ouright and left child to simulate our Binary Max Heap Tree without changing the structure of the HFPage class. 

we are also using an ArrayList as our Directory to keep track of the order of
HFPages, and we store this Directory into the disk in case we need to
reconstruct the order.

COMPLEXITY
==========

InsertRecord - O(log(n))

since we are using a Max Heap Tree we know that our root will be the HFPage that
has the maximum number of free space. 

If the root is big enough for the new record, we simply add the record into root
HFpage, then we have to update the position of the root since it might not have
the maximum number of free space after insertion. Following the heap tree data
structure re-positioning downward only takes O(log(n))

If the root is not big enough for the new record, we will have to create a new
HFPage we first add this new HFPage to the next spot in the HeapTree( which
takes O(1) ) they we update the position of this new HFPage since it might have
more free space than other pages. Following the heap tree data structure
re-positioning upward only takes O(log (n))

In both cases, our algorithm takes O(log (n)) which makes InsertRecord O(log
(n))



UpdateRecord - O(1)

Since we are given RID we only have to find the right  HFPage (which only
takes 1 IO ) and update the data in the record.

			
deleteRecord - O(log (n))

Since we are given RID finding the right HFPage takes 1 IO, however, after we
remove the record from the HFPage we will have to re-position the HFPage in our
heap upward , becuase it has more free space than before. Re-position the page
upward only takes O (log (n))

getRecCnt() - O(1)

We have a global variable that keeps track of the count, will re-calculate the
count when insert/delete or starting a new HeapFile.


 
