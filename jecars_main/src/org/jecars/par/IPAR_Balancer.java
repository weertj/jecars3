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

import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 *
 * @author weert
 */
public interface IPAR_Balancer {
 
  List<IPAR_System> systems() throws RepositoryException;
  IPAR_System        system( final Node pNode ) throws RepositoryException;
  IPAR_System        system( final String pName ) throws RepositoryException;
 
  List<IPAR_Core>     coresByWish(        final IPAR_ResourceWish pWish ) throws RepositoryException;
  IPAR_Balancer       resourceWishReady(  final IPAR_ResourceWish pWish );

  List<IPAR_ResourceWish> currentResources();
  boolean allocateWishToCore( final IPAR_Core pCore, final IPAR_ResourceWish pWish );

      
}
