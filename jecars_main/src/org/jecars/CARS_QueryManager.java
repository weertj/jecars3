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
package org.jecars;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

/**
 * CARS_QueryManager
 *
 * @version $Id: CARS_QueryManager.java,v 1.1 2007/09/26 14:20:16 weertj Exp $
 */
public class CARS_QueryManager {

  static private Object mRunningQuery = new Object();
   
  /** Execute query one at the time (performance issue)
   */
  static QueryResult executeQuery( Query pQuery ) throws RepositoryException {
    QueryResult qr = null;
    System.out.println( "Want to execute query: " + pQuery.getStatement() );
    synchronized( mRunningQuery ) {
      long time = System.currentTimeMillis();
      System.out.println( "Executing query: " + pQuery.getStatement() );
      qr = pQuery.execute();
      System.out.println( "Query ready in " + ((System.currentTimeMillis()-time)/1000) + " seconds" );
    }
    return qr;
  }
  
}
