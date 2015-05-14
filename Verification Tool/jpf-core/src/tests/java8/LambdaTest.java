//
// Copyright (C) 2014 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
// 
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
// 
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package java8;

import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 */
public class LambdaTest extends TestJPF{
  
  static class EnforcedException extends RuntimeException {
    // nothing in here
  }
  
  @Test
  public void testFuncObjAssignment() {
    if(verifyUnhandledException(EnforcedException.class.getName())) {
      
      Runnable r = () -> {
        throw new EnforcedException(); // make sure it gets here
        };
      
      assertTrue(r != null);
      
      (new Thread(r)).start();
    }
  }
  
  public interface FI1 {
    void sam();
  }
  
  public interface FI2 extends FI1 {
    public String toString();
  }
  
  @Test
  public void testSyntheticFuncObjClass() {
    if (verifyNoPropertyViolation()) {
      
      FI2 fi = () -> {
        return;
        };
      
      assertTrue(fi != null);
      
      Class cls = fi.getClass();
      
      assertEquals(cls.getInterfaces().length, 1);
      
      assertEquals(cls.getDeclaredMethods().length, 1);
            
      assertSame(cls.getInterfaces()[0], FI2.class);
      
      assertSame(cls.getSuperclass(), Object.class);
    }
  }
  
  public interface FI3 {
    public String ret();
  }
  
  @Test
  public void testSAMReturn() {
    if (verifyNoPropertyViolation()) {
      FI3 rt = () -> {
        return "something"; 
        };
      
      assertEquals(rt.ret(),"something"); 
    }
  }
  
  public class C {
    int x = 1;
  }
  
  public interface IncX {
    public int incX(C o);
  }
  
  @Test
  public void testLambdaArgument() {
    if (verifyNoPropertyViolation()) {
      IncX fo = (arg) -> {
        return ++arg.x;
        };
      
      C o = new C();
      
      assertEquals(fo.incX(o),2);
      assertEquals(fo.incX(o),3);
    }
  }
  
  static Integer io = new Integer(20);
  
  @Test
  public void testClosure() {
    if (verifyNoPropertyViolation()) {
      int i = 10;
      
      FI1 fi = () -> {
        assertSame(i,10);
        assertSame(io.intValue(), 20);
      };
      
      fi.sam();
    }
  }
  
  static void method(FI1 fi) {
    fi.sam();
  }
  
  @Test
  public void testPassingToMethod() {
    if (verifyUnhandledException(EnforcedException.class.getName())) {
      int i = 10;
      
      method(() -> {
        assertSame(i,10);
        assertSame(io.intValue(), 20);
        throw new EnforcedException();
      });
    }
  }
  
  // When invokedynamic executes for the first time, it creates a new function object.
  // Re-executing the same bytecode returns the existing function object.
  @Test
  public void testRepeatInvokedynamic() {
    if (verifyNoPropertyViolation()) {
      int i = 10;
      FI1 f1, f2 = null; 
      
      for(int j=0; j<2; j++) {
        f1 = () -> {
          System.out.println("hello world!");
        };
        
        if(j==1) {
          assertTrue(f1!=null);
          assertSame(f1,f2);
        }
        
        f2 = f1;
      }
    }
  }
}
