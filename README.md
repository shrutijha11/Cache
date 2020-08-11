# Cache

This program in java first implements cache mapping at level-1 (L1) of cache by using the following three different cache mapping techniques: 1. Direct mapping 2. Fully Associative Mapping 3. K-way Set Associative Mapping

This program in java then implements the 2-level cache searching using the following
method :
● If the CPU requests for a memory address MAR to complete a task, it is first
searched in cache level-1 (called L1).
● If it is found in L1, then it is called a cache L1 HIT. If it is not found, then it is
called a cache L1 MISS.
● In case of L1 MISS, the address is searched in cache level-2 (called L2).
● If the address is found in L2, it is called a cache L2 HIT, otherwise it is called a
cache L2 MISS.
● In case of cache L2 miss, the address is finally searched in the main memory.
The level-2 of cache (L2) ,is usually of larger size than level-1 of cache (L1), storing
more number of blocks of main memory than cache L1. Although, most of the time the
memory address is found in L1 only, it is possible that the need to search for a block in
L2 arises.

