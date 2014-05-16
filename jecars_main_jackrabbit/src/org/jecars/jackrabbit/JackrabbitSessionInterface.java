/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars.jackrabbit;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.data.GarbageCollector;
import org.jecars.ICARS_Session;

/**
 *
 * @author weert
 */
public class JackrabbitSessionInterface implements ICARS_Session {

  @Override
  public int runGarbageCollector( final Session pSession ) throws RepositoryException {
    GarbageCollector gc = ((SessionImpl)pSession).createDataStoreGarbageCollector();
    try {
      System.out.println(" GC : MARK Started");
      gc.mark();
      System.out.println(" GC : SWEEP Started");
      final int du = gc.sweep();
      return du;
    } finally {
      gc.close();
    }
  }

  @Override
  public Session cloneSession( final Session pSession ) throws RepositoryException {
    return (SessionImpl)((SessionImpl)pSession).createSession( pSession.getWorkspace().getName() );
  }
    
}
