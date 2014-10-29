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

package gov.nasa.jpf.vm;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;

/**
 * a Scheduler implementation base class that allows filtering of runnable threads and
 * implements SyncPolicy without backtracking or empty transitions, i.e. choice sets that
 * include all runnable threads
 */
public class AllRunnablesSyncPolicy implements SyncPolicy {

  protected VM vm;
  protected boolean breakSingleChoice;
  protected boolean breakLockRelease;
  
  public AllRunnablesSyncPolicy (Config config){
    breakSingleChoice = config.getBoolean("cg.break_single_choice", false);    
    breakLockRelease = config.getBoolean("cg.break_lock_release", true);
  }
  
  
  //--- internal methods

  /**
   * this is the main policy method that can be overridden by subclasses, e.g. by filtering
   * out the highest priority runnables, or by ordering according to priority
   * 
   * Default behavior is to first try to find runnables within the provided ApplicationContext,
   * and fall back to any runnable if there are none in this context
   * 
   * this includes threads that are in operations that can timeout
   */
  protected ThreadInfo[] getTimeoutRunnables (ApplicationContext appCtx){
    ThreadList tlist = vm.getThreadList();
    
    if (tlist.hasProcessTimeoutRunnables(appCtx)){
      return tlist.getProcessTimeoutRunnables(appCtx);
    } else {
      return tlist.getTimeoutRunnables();
    }
  }

    
  protected ChoiceGenerator<ThreadInfo> getRunnableCG (String id, ThreadInfo tiCurrent){
    ThreadInfo[] choices = getTimeoutRunnables(tiCurrent.getApplicationContext());
    
    if (choices.length == 0){
      return null;
    }
    
    if ((choices.length == 1) && (choices[0] == tiCurrent)){ // no context switch
      if (!breakSingleChoice){
        return null;
      }
    }
    
    return new ThreadChoiceFromSet( id, choices, true);
  }
  
  protected boolean setNextChoiceGenerator (ChoiceGenerator<ThreadInfo> cg){
    if (cg != null){
      return vm.getSystemState().setNextChoiceGenerator(cg); // listeners could still remove CGs
    } else {
      return false;
    }
  }
  
  /**
   * set a runnable CG that is optional if we are in a atomic section 
   */
  protected boolean setNonBlockingCG (String id, ThreadInfo tiCurrent){
    if (!tiCurrent.isFirstStepInsn() || tiCurrent.isEmptyTransitionEnabled()) {
      if (vm.getSystemState().isAtomic()) {
        return false;
      } else {
        return setNextChoiceGenerator(getRunnableCG(id, tiCurrent));
      }
      
    } else {
      return false;  // no empty transitions
    }
  }
  
  protected static ChoiceGenerator<ThreadInfo> blockedWithoutChoice = 
          new ThreadChoiceFromSet("BLOCKED_NO_CHOICE", new ThreadInfo[0], true);
  
  /**
   * set a runnable CG that would break a atomic section because it requires
   * a context switch
   */
  protected boolean setBlockingCG (String id, ThreadInfo tiCurrent){
    if (!tiCurrent.isFirstStepInsn() || tiCurrent.isEmptyTransitionEnabled()) {
      if (vm.getSystemState().isAtomic()) {
        vm.getSystemState().setBlockedInAtomicSection();
      }
      
      ChoiceGenerator<ThreadInfo> cg = getRunnableCG(id, tiCurrent);
      if (cg == null){ // make sure we don't mask a deadlock
        if (vm.getThreadList().hasLiveThreads()){
          cg = blockedWithoutChoice;
        }
      }
      
      return setNextChoiceGenerator(cg);
      
    } else {
      return false;  // no empty transitions
    }
  }
    
  /**
   * set a runnable CG that only breaks a atomic section if the blocking thread
   * is the currently executing one
   */
  protected boolean setMaybeBlockingCG (String id, ThreadInfo tiCurrent, ThreadInfo tiBlock){
    if (tiCurrent == tiBlock){
      return setBlockingCG( id, tiCurrent);
    } else {
      return setNonBlockingCG( id, tiCurrent);
    }
  }
  
  
  //--- SyncPolicy interface
  
  //--- initialization
  @Override
  public void initializeSyncPolicy (VM vm, ApplicationContext appCtx){
    this.vm  = vm;
  }
  
  @Override
  public void initializeThreadSync (ThreadInfo tiCurrent, ThreadInfo tiNew){
  }
    
  //--- locks
  @Override
  public boolean setsBlockedThreadCG (ThreadInfo ti, ElementInfo ei){
    return setBlockingCG( BLOCK, ti);
  }
  
  @Override
  public boolean setsLockAcquisitionCG (ThreadInfo ti, ElementInfo ei){
    return setNonBlockingCG( LOCK, ti);
  }
  
  @Override
  public boolean setsLockReleaseCG (ThreadInfo ti, ElementInfo ei){
    if (breakLockRelease){
      // <2do> we could check if there are any waiters
      return setNonBlockingCG( RELEASE, ti);
    } else {
      return false;
    }
  }
  
  //--- thread termination
  @Override
  public boolean setsTerminationCG (ThreadInfo ti){
    return setBlockingCG( TERMINATE, ti);
  }
  
  //--- java.lang.Object APIs
  @Override
  public boolean setsWaitCG (ThreadInfo ti, long timeout){
    return setBlockingCG( WAIT, ti);
  }
  
  @Override
  public boolean setsNotifyCG (ThreadInfo ti){
    return setNonBlockingCG( NOTIFY, ti);
  }
  
  @Override
  public boolean setsNotifyAllCG (ThreadInfo ti){
    return setNonBlockingCG( NOTIFYALL, ti);
  }
  
    
  //--- the java.lang.Thread APIs
  @Override
  public boolean setsStartCG (ThreadInfo tiCurrent, ThreadInfo tiStarted){
    return setNonBlockingCG( START, tiCurrent);
  }
  
  @Override
  public boolean setsYieldCG (ThreadInfo ti){
    return setNonBlockingCG( YIELD, ti);
  }
  
  @Override
  public boolean setsPriorityCG (ThreadInfo ti){
    return setNonBlockingCG( PRIORITY, ti);    
  }
  
  @Override
  public boolean setsSleepCG (ThreadInfo ti, long millis, int nanos){
    return setNonBlockingCG( SLEEP, ti);
  }
  
  @Override
  public boolean setsSuspendCG (ThreadInfo tiCurrent, ThreadInfo tiSuspended){
    return setMaybeBlockingCG( SUSPEND, tiCurrent, tiSuspended);      
  }
  
  @Override
  public boolean setsResumeCG (ThreadInfo tiCurrent, ThreadInfo tiResumed){
    return setNonBlockingCG( RESUME, tiCurrent);
  }
  
  @Override
  public boolean setsJoinCG (ThreadInfo tiCurrent, ThreadInfo tiJoin, long timeout){
    return setBlockingCG( JOIN, tiCurrent);      
  }
  
  @Override
  public boolean setsStopCG (ThreadInfo tiCurrent, ThreadInfo tiStopped){
    return setMaybeBlockingCG( STOP, tiCurrent, tiStopped);
  }
  
  @Override
  public boolean setsInterruptCG (ThreadInfo tiCurrent, ThreadInfo tiInterrupted){
    return setNonBlockingCG( INTERRUPT, tiCurrent);
  }
  
  
  //--- sun.misc.Unsafe
  @Override
  public boolean setsParkCG (ThreadInfo ti, boolean isAbsTime, long timeout){
    return setBlockingCG( PARK, ti);
  }
  
  @Override
  public boolean setsUnparkCG (ThreadInfo tiCurrent, ThreadInfo tiUnparked){
    return setNonBlockingCG( UNPARK, tiCurrent);
  }

  
  //--- system scheduling events
  
  /**
   * this one has to be guaranteed to set a CG
   */
  @Override
  public void setRootCG (){
    ThreadInfo[] runnables = vm.getThreadList().getTimeoutRunnables();
    ChoiceGenerator<ThreadInfo> cg = new ThreadChoiceFromSet( ROOT, runnables, true);
    vm.getSystemState().setMandatoryNextChoiceGenerator( cg, "no ROOT choice generator");
  }
  
  
  //--- gov.nasa.jpf.vm.Verify
  @Override
  public boolean setsBeginAtomicCG (ThreadInfo ti){
    return setNonBlockingCG( BEGIN_ATOMIC, ti);
  }
  
  @Override
  public boolean setsEndAtomicCG (ThreadInfo ti){
    return setNonBlockingCG( END_ATOMIC, ti);
  }
  
  //--- ThreadInfo reschedule request
  @Override
  public boolean setsRescheduleCG (ThreadInfo ti, String reason){
    return setNonBlockingCG( reason, ti);
  }
  
  //--- FinalizerThread
  @Override
  public boolean setsPostFinalizeCG (ThreadInfo tiFinalizer){
    // the finalizer is already waiting at this point, i.e. it's not runnable anymore
    return setBlockingCG( POST_FINALIZE, tiFinalizer);
  }
  

}
