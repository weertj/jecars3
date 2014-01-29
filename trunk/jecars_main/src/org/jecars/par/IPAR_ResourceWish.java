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
public interface IPAR_ResourceWish {

  String            wishID();
  IPAR_ResourceWish resourceID( final String pResourceID );
  String            resourceID();  
  IPAR_ResourceWish maxNumberOfRunsPerSystem( int pRuns );
  int               maxNumberOfRunsPerSystem();
  String            runOnSystem();
  IPAR_ResourceWish runOnSystem( String pS );
  String            runOnCPU();
  IPAR_ResourceWish runOnCPU( String pS );
  String            runOnCore();
  IPAR_ResourceWish runOnCore( String pS );
  int               numberOfCores();
  IPAR_ResourceWish numberOfCores( int pNumberOfCores );
  IPAR_ResourceWish system( final String pSystem );
  String            system();
  IPAR_ResourceWish systemType( EPAR_SystemType pST );
  EPAR_SystemType   systemType();
  IPAR_ResourceWish expectedLoad( double pL );
  double            expectedLoad();
  
}
