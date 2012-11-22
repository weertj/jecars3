/*
 * Copyright 2007-2009 NLR - National Aerospace Laboratory
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
 * CARS_DefaultVersionManager
 * 
 * @version $Id: CARS_DefaultVersionManager.java,v 1.3 2009/06/23 22:40:31 weertj Exp $
 */
public class CARS_DefaultVersionManager implements CARS_VersionManager {

  @Override
  public CARS_Version checkin(CARS_Main pMain, Node pNode, String pLabel) throws CARS_VersionException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Node checkout(CARS_Main pMain, Node pNode) throws CARS_VersionException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Node restore(CARS_Main pMain, Node pNode, String pLabel) throws CARS_VersionException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<String> history(Node pNode) throws RepositoryException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removeVersionByLabel(String pLabel, Node pNode) throws RepositoryException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
