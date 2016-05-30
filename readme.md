Test gc_malloc_native_primitive_arrays
======================================

This app is a test for future Pull Request gc_malloc_atomic_primitive_arrays 
(PR preview at https://github.com/fbertra/scala-native/tree/gc_malloc_atomic_primitive_arrays)

Boehm GC is an conservative garbage collector (see http://stackoverflow.com/questions/7629446/conservative-garbage-collector).  If a 
integer variable holds a value equals to an pointer address, Boehm GC won't collect the address.

As Scala primitve arrays shouldn't contains pointer address, the PR calls GG_malloc_atomic instead of GC_malloc to inform Boehm GC to not inspect their content.

Important note
--------------

Boehm GC is exhibiting random behavior between program execution.

This test is successful if Boehm GC, AT LEAST ONE TIME, free the memory after calling "useMemReturnInt ()" 

For example, in this run, the last three "free bytes": are all equals to 184320.
So we don't know if the penultimate "free bytes" is only bad luck

```
Test Array[Int]
testArrayInt (): before useMemReturnInt
heap size: 65536
free bytes: 53248

useMem (): before gc
ptr address hex: 0x12eb000, int: 19836928
heap size: 10199040
free bytes: 184320

useMem (): after gc
heap size: 10199040
free bytes: 184320

testArrayInt (): after useMemReturnInt and after first gc
ptr address as Int 19836928
heap size: 10199040
free bytes: 184320

testArrayInt (): after forgetting the pointer Int value and after second gc
heap size: 10199040
free bytes: 184320

I'm not sure
```

In the following run, the memory was released "after useMemReturnInt and after first gc"

```
Test Array[Int]
testArrayInt (): before useMemReturnInt
heap size: 65536
free bytes: 53248

useMem (): before gc
ptr address hex: 0x1c6c000, int: 29802496
heap size: 10199040
free bytes: 184320

useMem (): after gc
heap size: 10199040
free bytes: 184320

testArrayInt (): after useMemReturnInt and after first gc
ptr address as Int 29802496
heap size: 10199040
free bytes: 10186752

testArrayInt (): after forgetting the pointer Int value and after second gc
heap size: 10199040
free bytes: 10186752

Test succesfull
```

