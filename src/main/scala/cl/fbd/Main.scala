package cl.fbd

import scalanative.native._
import scalanative.runtime.GC
import scalanative.libc.stdlib._

/*
 *
 */

object Main {
  def main (args : Array [String]) = {
    fprintf(stdout, c"Test Boehm GC\n")
    
    // this array will "remember" a C pointer cast as a Int value 
    val arr = new Array [Int] (2)
    
    // should be small values
    val heapSizeBefore = GC2.GC_get_heap_size ()
    val freeBytesBefore = GC2.GC_get_free_bytes ()
          
    fprintf(stdout, c"main (): before useMem\n")
    
    fprintf(stdout, c"heap size: %d\n", heapSizeBefore)
    fprintf(stdout, c"free bytes: %d\n", freeBytesBefore)
    fprintf(stdout, c"\n")
    
    // the array allocated inside useMem should be candidate to garbage collection
    // but arr (0) retains the pointer value as a integer
    arr (0) = useMem ()

    // The conservative GC treats the Int value as a potential pointer and
    //   don't reclaim the memory    
    GC2.GC_gcollect ()
    
    val heapSizeAfter1 = GC2.GC_get_heap_size ()
    val freeBytesAfter1 = GC2.GC_get_free_bytes()
          
    fprintf(stdout, c"main (): after useMem and after first gc\n")
    
    fprintf(stdout, c"ptr address as Int %d\n", arr (0))
    
    fprintf(stdout, c"heap size: %d\n", heapSizeAfter1)
    fprintf(stdout, c"free bytes: %d\n", freeBytesAfter1)
    fprintf(stdout, c"\n")

    // forget the pointer    
    arr (0) = 0

    // as arr don't hold the pointer value anymore, Boehm GC can claims the memory 
    GC2.GC_gcollect ()
    
    fprintf(stdout, c"main (): after forgetting the pointer Int value and after second gc\n")
    
    val heapSizeAfter2 = GC2.GC_get_heap_size ()
    val freeBytesAfter2 = GC2.GC_get_free_bytes()
          
    fprintf(stdout, c"heap size: %d\n", heapSizeAfter2)
    fprintf(stdout, c"free bytes: %d\n", freeBytesAfter2)    
    fprintf(stdout, c"\n")
    
    ()
  }

  /*
   * allocates a big array
   * return the pointer casted as a Int
   */
   
  def useMem () : Int = {
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
    
    // cast the pointer as integer and return the value 
    ptr2Int (ptr)
    
    // the array should be candidate to garbage collection
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
