/*
 * Copyright 2007 NLR - National Aerospace Laboratory
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
package org.jecars.version;

import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.jecars.CARS_Main;

/**
 * CARS_VersionManager
 * 
 * @version $Id: CARS_VersionManager.java,v 1.3 2009/06/23 22:39:24 weertj Exp $
 */
public interface CARS_VersionManager {

  /** checkin the node
   * @param pNode node which will be checked in
   * @param pLabel optional label for the checked in version
   * @return CARS_Version the result of the checkin
   * @throws CARS_VersionException when an error occurs
   */
  CARS_Version checkin( CARS_Main pMain, Node pNode, String pLabel ) throws CARS_VersionException;

  /** checkout the node
   * @param pMain CARS_Main object
   * @param pNode node which will be checked out
   * @return The checkedout node
   * @throws CARS_VersionException when an error occurs
   */
  Node checkout( CARS_Main pMain, Node pNode ) throws CARS_VersionException;

  /** restore an version of a node
   * @param pMain CARS_Main object
   * @param pNode the node
   * @param pLabel node with this label will be restored
   * @return restored node
   * @throws CARS_VersionException when an error occurs
   */
  Node restore( CARS_Main pMain, Node pNode, String pLabel ) throws CARS_VersionException;

  /** history
   *
   * @param pNode
   * @return
   * @throws javax.jcr.RepositoryException
   */
  List<String> history( final Node pNode ) throws RepositoryException;

  /** removeVersionByLabel
   * 
   * @param pLabel
   * @param pNode
   * @throws javax.jcr.RepositoryException
   */
  void removeVersionByLabel( final String pLabel, final Node pNode ) throws RepositoryException;


}
