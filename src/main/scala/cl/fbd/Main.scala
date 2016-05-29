package cl.fbd

import scalanative.native._
import scalanative.runtime.GC
import scalanative.libc.stdlib._

/*
 *
 */

object Main {
  def main (args : Array [String]) = {
    testArrayInt ()
    
    ()
  }
  
  /*
   * 
   */
   
  def testArrayInt () : Unit = {
    fprintf(stdout, c"Test Array[Int]\n")
    
    // this array will "remember" a C pointer cast as a Int value 
    val arr = new Array [Int] (2)
    
    // should be small values
    val heapSizeBefore = GC2.GC_get_heap_size ()
    val freeBytesBefore = GC2.GC_get_free_bytes ()
          
    fprintf(stdout, c"testArrayInt (): before useMemReturnInt\n")
    
    fprintf(stdout, c"heap size: %d\n", heapSizeBefore)
    fprintf(stdout, c"free bytes: %d\n", freeBytesBefore)
    fprintf(stdout, c"\n")
    
    arr (0) = useMemReturnInt ()

    // even if arr (0) is equals to an pointer address, GC Boehm should collect
    // the memory because we now allocate Array[Int] with GC_malloc_atomic    
    GC2.GC_gcollect ()
    
    val heapSizeAfter1 = GC2.GC_get_heap_size ()
    val freeBytesAfter1 = GC2.GC_get_free_bytes()
          
    fprintf(stdout, c"testArrayInt (): after useMemReturnInt and after first gc\n")
    
    fprintf(stdout, c"ptr address as Int %d\n", arr (0))
    
    fprintf(stdout, c"heap size: %d\n", heapSizeAfter1)
    fprintf(stdout, c"free bytes: %d\n", freeBytesAfter1)
    fprintf(stdout, c"\n")

    // forget the pointer    
    arr (0) = 0

    // we shouldn't see any change
    GC2.GC_gcollect ()
    
    fprintf(stdout, c"testArrayInt (): after forgetting the pointer Int value and after second gc\n")
    
    val heapSizeAfter2 = GC2.GC_get_heap_size ()
    val freeBytesAfter2 = GC2.GC_get_free_bytes()
          
    fprintf(stdout, c"heap size: %d\n", heapSizeAfter2)
    fprintf(stdout, c"free bytes: %d\n", freeBytesAfter2)    
    fprintf(stdout, c"\n")
    
    ()
  }

  /*
   * Allocates memory. 
   * 
   * The memory is unreachable after this function return
   * 
   * return an Int equals to the address of the allocated memory
   */
   
  def useMemReturnInt () : Int = {
    import scalanative.runtime
    
    val ptr = GC.malloc (10000000).cast[Ptr[Byte]]
    
    fprintf(stdout, c"useMem (): before gc\n")
    
    fprintf(stdout, c"ptr address hex: %p, int: %d\n", ptr, ptr)
    
    val heapSizeBefore = GC2.GC_get_heap_size ()
    val freeBytesBefore = GC2.GC_get_free_bytes()
          
    fprintf(stdout, c"heap size: %d\n", heapSizeBefore)
    fprintf(stdout, c"free bytes: %d\n", freeBytesBefore)
    fprintf(stdout, c"\n")
    
    
    // the array is in used inside "useMem", so Boehm GC cannot free the memory    
    GC2.GC_gcollect ()
    
    val heapSizeAfter = GC2.GC_get_heap_size ()
    val freeBytesAfter = GC2.GC_get_free_bytes()
          
    fprintf(stdout, c"useMem (): after gc\n")
    
    fprintf(stdout, c"heap size: %d\n", heapSizeAfter)
    fprintf(stdout, c"free bytes: %d\n", freeBytesAfter)
    fprintf(stdout, c"\n")
    
    // do something with the memory (force the use of the array)
    !ptr = 'H'
    
    // cast the pointer as integer 
    ptr2Int (ptr)
  }
  
  /*
   * converting pointers to int, is a very bad idea
   *
   * but it is for education and you have to break things in order to learn
   */
   
  def ptr2Int (ptr : Ptr[_]) : Int = {
    // I didn't find a way to do this with scala.native
    // but, you can always trust the C library to do nasty things
    
    val addrAsCString = GC.malloc (100).cast[Ptr[Byte]]
    
    // C obviously know how to treat a pointer as a integer
    stdio.sprintf (addrAsCString, c"%d", ptr)
    
    // fprintf(stdout, c"addr as C string %s\n", addrAsCString)
    
    stdlib2.atoi (addrAsCString)
  }    
}

// some external C library used in this test

// gc.h
@link("gc")
@extern 
object GC2 {
  @name("GC_gcollect")
  def GC_gcollect () : Unit = extern
  
  @name("GC_get_heap_size")
  def GC_get_heap_size(): CSize = extern
  
  @name("GC_get_free_bytes")
  def GC_get_free_bytes(): CSize = extern
}

// stdio.h
@extern
object stdio {
  def sprintf(s: CString, format: CString, args: Vararg*): CInt = extern
}

// stdlib.h
@extern
object stdlib2 {
  def atoi (s: CString): CInt = extern
}
