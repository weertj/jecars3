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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.jcr.RepositoryException;

/**
 *
 * @author weert
 * @param <E>
 */
public interface IPAR_Core<E> extends IPAR_DefaultNode {
  
  EPAR_CoreType coreType();
  E             execute( final IPAR_Execute<E> pExec, final IPAR_ResourceWish pWish ) throws InterruptedException, RepositoryException, ExecutionException;

  boolean       allocate( IPAR_ResourceWish pWish, boolean pAcceptOverload );
  void          release(  IPAR_ResourceWish pWish );
  
  long          currentRunning();
  long          readyRunning();
  
  double        maxLoad();
  double        currentLoad();
  double        expectedLoad();
  
  List<IPAR_Execute<E>>       queuedExecs();
  List<IPAR_Execute<E>>       runningExecs();
  LinkedList<IPAR_Execute<E>> finishedExecs();
  
  void          removeQueuedExecs(    final List<IPAR_Execute<E>> pExecs );
  void          removeRunningExecs(   final List<IPAR_Execute<E>> pExecs );
  void          removeFinishedExecs(  final List<IPAR_Execute<E>> pExecs );
  
}
  