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

/**
 *
 * @author weert
 */
public class PAR_ResourceWish implements IPAR_ResourceWish {
  
  private EPAR_SystemType mSystemType = EPAR_SystemType.LOCAL;
  private String          mSystem = "localhost";
  private int             mNumberOfCores = 1;
  private double          mExpectedLoad = 1;

  
  @Override
  public int numberOfCores() {
    return mNumberOfCores;
  }

  @Override
  public IPAR_ResourceWish numberOfCores( int pNumberOfCores ) {
    mNumberOfCores = pNumberOfCores;
    return this;
  }
  
  
  @Override
  public IPAR_ResourceWish system( final String pSystem ) {
    mSystem = pSystem;
    return this;
  }

  @Override
  public String system() {
    return mSystem;
  }

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

  
  
  
}
