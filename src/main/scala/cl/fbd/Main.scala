package cl.fbd

import scalanative.native._
import scalanative.runtime.GC
import scalanative.libc.stdlib._

@link("gc")
@extern 
object GC2 {
  @name("GC_get_heap_size")
  def GC_get_heap_size(): CSize = extern
  
  @name("GC_gcollect")
  def GC_gcollect () : Unit = extern
  
  @name("GC_get_free_bytes")
  def GC_get_free_bytes(): CSize = extern
}

@extern
object stdio {
  def sprintf(s: CString, format: CString, args: Vararg*): CInt = extern
}

@extern
object stdlib2 {
  def atoi (s: CString): CInt = extern
}

object Main {
  def main (args : Array [String]) = {
    fprintf(stdout, c"Test GC\n")
    
    // 
    val arr = new Array [Int] (2)
    
    //
    val heapSizeBefore = GC2.GC_get_heap_size ()
    val freeBytesBefore = GC2.GC_get_free_bytes()
          
    fprintf(stdout, c"Outside before useMem\n")
    
    fprintf(stdout, c"heap size: %d\n", heapSizeBefore)
    fprintf(stdout, c"free bytes: %d\n", freeBytesBefore)
    
    arr (0) = useMem ()

    // stop the world    
    GC2.GC_gcollect ()
    
    val heapSizeAfter1 = GC2.GC_get_heap_size ()
    val freeBytesAfter1 = GC2.GC_get_free_bytes()
          
    fprintf(stdout, c"Outside after useMem and after first gc\n")
    
    fprintf(stdout, c"addr as Int %d\n", arr (0))
    
    fprintf(stdout, c"heap size: %d\n", heapSizeAfter1)
    fprintf(stdout, c"free bytes: %d\n", freeBytesAfter1)
    
    //
    arr (0) = 0

    // stop the world    
    GC2.GC_gcollect ()
    
    fprintf(stdout, c"Outside after forgetting the pointer Int value and after second gc\n")
    
    val heapSizeAfter2 = GC2.GC_get_heap_size ()
    val freeBytesAfter2 = GC2.GC_get_free_bytes()
          
    fprintf(stdout, c"heap size: %d\n", heapSizeAfter2)
    fprintf(stdout, c"free bytes: %d\n", freeBytesAfter2)
    
    ()
  }

  /*
   * allocates a big array
   * return the pointer casted as a Int
   */
   
  def useMem () : Int = {
    import scalanative.runtime
    
    fprintf(stdout, c"useMem\n")
    
    val ptr = GC.malloc (10000000).cast[Ptr[Byte]]
    
    fprintf(stdout, c"ptr address hex: %p, int: %d\n", ptr, ptr)
    
    val heapSizeBefore = GC2.GC_get_heap_size ()
    val freeBytesBefore = GC2.GC_get_free_bytes()
          
    fprintf(stdout, c"Inside before GC\n")
    
    fprintf(stdout, c"heap size: %d\n", heapSizeBefore)
    fprintf(stdout, c"free bytes: %d\n", freeBytesBefore)
    
    // stop the world    
    GC2.GC_gcollect ()
    
    val heapSizeAfter = GC2.GC_get_heap_size ()
    val freeBytesAfter = GC2.GC_get_free_bytes()
          
    fprintf(stdout, c"Inside after gc\n")
    
    fprintf(stdout, c"heap size: %d\n", heapSizeAfter)
    fprintf(stdout, c"free bytes: %d\n", freeBytesAfter)
    
    // do something with the memory
    !ptr = 'H'
    
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
    
    fprintf(stdout, c"addr as C string %s\n", addrAsCString)
    
    stdlib2.atoi (addrAsCString)
  }    
}
