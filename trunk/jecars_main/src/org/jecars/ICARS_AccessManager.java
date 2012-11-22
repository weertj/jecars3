/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars;

import java.util.HashSet;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryManager;

/**
 *
 * @author weert
 */
public interface ICARS_AccessManager {
  void gClearCache();
  void clearPathCache();
  long getCacheSize();
  void fillPrincipalsForGroupMembers( final QueryManager pQM, final List<Node> pAL, final String pUUID ) throws RepositoryException;
  HashSet<String> getReadPathCache();
  HashSet<String> getWritePathCache();
  HashSet<String> getRemovePathCache();
  HashSet<String> getSetPropPathCache();
  HashSet<String> getDenyReadPathCache();
  Object getExclusiveControlObject();
  
}
