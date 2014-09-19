/*
 * Copyright 2014 NLR - National Aerospace Laboratory
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jecars.par;

import java.util.concurrent.atomic.AtomicLong;
import org.jecars.tools.CARS_ToolInterface;

/**
 *
 * @author weert
 */
public class PAR_ResourceWish implements IPAR_ResourceWish {
  
  static private final AtomicLong COUNTER = new AtomicLong(1);

  private       boolean             mMustFollowWish = false;
  private final String              mWishID;
  private       EPAR_SystemType     mSystemType = EPAR_SystemType.LOCAL;
//  private       String          mSystem = "localhost";
  private       int                 mNumberOfCores = 1;
  private       double              mExpectedLoad = 1;
  private       int                 mMaxNumberOfRunsPerSystem = -1;
  private       String              mRunOnSystem  = ".*";
  private       String              mRunOnCPU     = ".*";
  private       String              mRunOnCore    = ".*";
  private       String              mResourceID = null;
  private       CARS_ToolInterface  mToolInterface = null;

  /** PAR_ResourceWish
   * 
   */
  public PAR_ResourceWish() {
    mWishID = "ResourceWish_" + COUNTER.getAndIncrement();
    return;
  }

  @Override
  public String wishID() {
    return mWishID;
  }

  @Override
  public CARS_ToolInterface toolInterface() {
    return mToolInterface;
  }

  @Override
  public IPAR_ResourceWish toolInterface( final CARS_ToolInterface pTI ) {
    mToolInterface = pTI;
    return this;
  }

  @Override
  public boolean mustFollowWish() {
    return mMustFollowWish;
  }

  @Override
  public IPAR_ResourceWish mustFollowWish( final boolean pW ) {
    mMustFollowWish = pW;
    return this;
  }

  @Override
  public String resourceID() {
    return mResourceID;
  }


  @Override
  public IPAR_ResourceWish resourceID( final String pResourceID ) {
    mResourceID = pResourceID;
    return this;
  }

  @Override
  public String runOnCPU() {
    return mRunOnCPU;
  }

  @Override
  public IPAR_ResourceWish runOnCPU(String pS) {
    mRunOnCPU = pS;
    return this;
  }

  @Override
  public String runOnCore() {
    return mRunOnCore;
  }

  @Override
  public IPAR_ResourceWish runOnCore(String pS) {
    mRunOnCore = pS;
    return this;
  }

  @Override
  public String runOnSystem() {
    return mRunOnSystem;
  }

  @Override
  public IPAR_ResourceWish runOnSystem(String pS) {
    mRunOnSystem = pS;
    return this;
  }

  
  @Override
  public int maxNumberOfRunsPerSystem() {
    return mMaxNumberOfRunsPerSystem;
  }

  @Override
  public IPAR_ResourceWish maxNumberOfRunsPerSystem( final int pRuns ) {
    mMaxNumberOfRunsPerSystem = pRuns;
    return this;
  }
  
  @Override
  public int numberOfCores() {
    return mNumberOfCores;
  }

  
  @Override
  public IPAR_ResourceWish numberOfCores( final int pNumberOfCores ) {
    mNumberOfCores = pNumberOfCores;
    return this;
  }
  
  
//  @Override
//  public IPAR_ResourceWish system( final String pSystem ) {
//    mSystem = pSystem;
//    return this;
//  }
//
//  @Override
//  public String system() {
//    return mSystem;
//  }
//
  @Override
  public EPAR_SystemType systemType() {
    return mSystemType;
  }

  @Override
  public IPAR_ResourceWish systemType( final EPAR_SystemType pST ) {
    mSystemType = pST;
    return this;
  }

  @Override
  public double expectedLoad() {
    return mExpectedLoad;
  }
  
  @Override
  public IPAR_ResourceWish expectedLoad( double pL ) {
    mExpectedLoad = pL;
    return this;
  }

  
  /** toString
   * 
   * @return 
   */
  @Override
  public String toString() {
    String s = mWishID + "\n";
    s += "System = " + mRunOnSystem + " type=" + mSystemType + "\n";
    s += "CPU = " + mRunOnCPU + "\n";
    s += "Core = " + mRunOnCore + "\n";
    s += "Expected load = " + mExpectedLoad + "\n";    
    s += "Max runs per system = " + mMaxNumberOfRunsPerSystem + "\n";    
    s += "Number of cores = " + mNumberOfCores + "\n";    
    s += "Must follow wish = " + mMustFollowWish + "\n";    
    return s;
  }

  
}
