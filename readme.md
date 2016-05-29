Test gc_malloc_native_primitive_arrays
======================================

This app is a test for future Pull Request gc_malloc_atomic_primitive_arrays 
(PR preview at https://github.com/fbertra/scala-native/tree/gc_malloc_atomic_primitive_arrays)

Boehm GC is an conservative garbage collector (see http://stackoverflow.com/questions/7629446/conservative-garbage-collector).  If a 
integer variable holds a value equals to an pointer address, Boehm GC won't collect the address.

As Scala primitve arrays shouldn't contains pointer address, the PR calls GG_malloc_atomic instead of GC_malloc to inform Boehm GC to not inspect their content.

Result with version based on GC_malloc (pre PR)
-----------------------------------------------

> Test Boehm GC
> main (): before useMem
> heap size: 65536
> free bytes: 57344
> 
> 
> useMem (): before gc
> ptr address hex: 0x227e000, int: 36167680
> heap size: 10199040
> free bytes: 188416
> 
> 
> useMem (): after gc
> heap size: 10199040
> free bytes: 188416
> 
> 
> main (): after useMem and after first gc
> ptr address as Int 36167680
> heap size: 10199040
> free bytes: 188416
> 
> 
> main (): after forgetting the pointer Int value and after second gc
> heap size: 10199040
> free bytes: 10190848


Free bytes inside useMem and after useMem are equals: 188416.  
Even if the array "useMem ()::ptr" is unreachable from main(), Boehm GC doesn't free the memory because main()::arr(0) contains
a integer equals to the pointer address.

The memory is freed after setting main()::arr(0) to 0


Results with version based on GC_malloc_atomic (post PR)
--------------------------------------------------------

> Test Boehm GC
> main (): before useMem
> heap size: 65536
> free bytes: 53248
> 
> 
> useMem (): before gc
> ptr address hex: 0x234e000, int: 37019648
> heap size: 10199040
> free bytes: 184320
> 
> 
> useMem (): after gc
> heap size: 10199040
> free bytes: 184320
> 
> 
> main (): after useMem and after first gc
> ptr address as Int 37019648
> heap size: 10199040
> free bytes: 10186752
> 
> 
> main (): after forgetting the pointer Int value and after second gc
> heap size: 10199040
> free bytes: 10186752



After useMem() completes, the free bytes don't change between gc: 10186752 

Boehm ignores the content of main()::arr and free the memory inmediately.


Important note
--------------

GC Boehm doesn't always free the memory.  Consecutive executions of the
program behave diferently.  This is very inconvenient, because we need a
automatic test that always behave the same.



TBD
---


Check that non primitives arrays aren't affected by the PR.


