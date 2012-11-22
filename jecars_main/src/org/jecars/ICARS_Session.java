/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 *
 * @author weert
 */
public interface ICARS_Session {
  
  int       runGarbageCollector( final Session pSession ) throws RepositoryException;
  Session   cloneSession( final Session pSession ) throws RepositoryException;
}
