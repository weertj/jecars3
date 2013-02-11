/*
 * Copyright (c) 2007 Maverick Software Development, 11/11 Software.
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
package org.jecars.backup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.jcr.PropertyType;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.*;
import javax.jcr.version.OnParentVersionAction;
import nl.msd.jdots.JD_Taglist;

/**
 * JB_ExportNodeTypes
 * 
 * @author Jacco van Weert
 * @version $Id: JB_ExportNodeTypes.java,v 1.3 2009/02/03 10:49:55 weertj Exp $
 */
public class JB_ExportNodeTypes {
  
  private JD_Taglist mNTs = new JD_Taglist();
    
  /** Creates a new instance of JCRE_NodeTypes */
  public JB_ExportNodeTypes() {
  }

  
  
  /** Get the super nodetype
   *
   * @param pThisType
   * @return
   */
  private String getTopNodeType( String pThisType ) {
    if (mNTs.isEmpty()==false) {
      if (pThisType==null) {
        pThisType = (String)mNTs.getIterator().next();
      }
      if ((pThisType.startsWith( "nt:"  )) || (pThisType.startsWith( "mix:" )) ||
          (pThisType.startsWith( "rep:" ))) return null;
      NodeType nt = (NodeType)mNTs.getData( pThisType );
      NodeType[] snt = nt.getSupertypes();
      for( int i=0; i<snt.length; i++ ) {
        if (mNTs.getData( snt[i].getName() )!=null) {
          return getTopNodeType( snt[i].getName() );          
        }
      }
  
            // **** Check for childnode definitions
//   System.out.println( " CHECK NDEFS " + nt.getName() );
      NodeDefinition[] ndefs = nt.getChildNodeDefinitions();
      for( int ndi=0; ndi<ndefs.length; ndi++ ) {
//        System.out.println( " NDEFS " + nt.getName() + " -- " + ndefs[ndi].getName() );
        NodeType[] cnt = ndefs[ndi].getRequiredPrimaryTypes();
        for( int cnti=0; cnti<cnt.length; cnti++ ) {
          if (mNTs.getData( cnt[cnti].getName() )!=null) {
//          System.out.println( " CHODDEE " + cnt[cnti].getName() );
            if (cnt[cnti].getName().equals( nt.getName() )==false) {
              return getTopNodeType( cnt[cnti].getName() );
            }
          }
        }
      }
      
    } else {
      pThisType = null;
    }
    return pThisType;
  }

  /** nodeTypeToCND
   *
   * @param pNodeTypeName
   * @return
   * @throws java.lang.Exception
   */
  public String nodeTypeToCND( String pNodeTypeName ) throws Exception {
    if ((pNodeTypeName.startsWith( "nt:"  )) || (pNodeTypeName.startsWith( "mix:" )) ||
        (pNodeTypeName.startsWith( "rep:" ))) return null;

    StringBuilder cnd = new StringBuilder();
    NodeType nt = (NodeType)mNTs.getData( pNodeTypeName );
    cnd.append( "[" ).append( nt.getName() ).append( "]" );
    NodeType[] snt = nt.getDeclaredSupertypes();
    if (snt.length>0) {
      cnd.append( " > " );
      for( int i=0; i<snt.length; i++ ) {
        if (i>0) cnd.append( " , " );
        cnd.append( snt[i].getName() );
      }
    }
    cnd.append( " " );
    if (nt.hasOrderableChildNodes()==true) cnd.append( "orderable " );
    if (nt.isMixin()==true)                cnd.append( "mixin " );
    cnd.append( "\n" );
    PropertyDefinition pds[] = nt.getDeclaredPropertyDefinitions();
    for( int i=0; i<pds.length; i++ ) {
      cnd.append( "  - " ).append( pds[i].getName() );
      cnd.append( " (" ).append( PropertyType.nameFromValue( pds[i].getRequiredType() )).append( ") " );
      Value[] vals = pds[i].getDefaultValues();
      if ((vals!=null) && (vals.length>0)) {
        cnd.append( "= " );
        for( int vali=0; vali<vals.length; vali++ ) {
          if (vali>0) cnd.append( ", " );
          cnd.append( "'" ).append( vals[vali].getString() ).append( "' " );
        }
      }
      if (pds[i].getName().equals(nt.getPrimaryItemName())) cnd.append( "primary " );
      if (pds[i].isAutoCreated()) cnd.append( "autocreated " );
      if (pds[i].isMandatory()  ) cnd.append( "mandatory " );
      if (pds[i].isMultiple()   ) cnd.append( "multiple " );
      if (pds[i].isProtected()  ) cnd.append( "protected " );
      cnd.append( OnParentVersionAction.nameFromValue( pds[i].getOnParentVersion() )).append( " " );
      String[] cons = pds[i].getValueConstraints();
      if ((cons!=null) && (cons.length>0)) {
        cnd.append( "< " );
        for( int ci=0; ci<cons.length; ci++ ) {
          if (ci>0) cnd.append( ", " );
          cnd.append( "'" ).append( cons[ci] ).append( "' " );
        }
      }
      
      cnd.append( "\n" );
    }
    
    NodeDefinition[] nd = nt.getDeclaredChildNodeDefinitions();
    for( int i=0; i<nd.length; i++ ) {
      cnd.append( "  + " ).append( nd[i].getName() );
      NodeType[] nts = nd[i].getRequiredPrimaryTypes();
      if ((nts!=null) && (nts.length>0)) {
        cnd.append( " (" );
        for( int ci=0; ci<nts.length; ci++ ) {
          if (ci>0) cnd.append( ", " );
          cnd.append( nts[ci].getName() );
        }        
        cnd.append( ")" );
      }
      if (nd[i].getDefaultPrimaryType()!=null) cnd.append( " = " ).append( nd[i].getDefaultPrimaryType().getName() );
      if (nd[i].isMandatory())   cnd.append( " mandatory" );
      if (nd[i].isAutoCreated()) cnd.append( " autocreated" );
      if (nd[i].isProtected())   cnd.append( " protected" );
      if (nd[i].allowsSameNameSiblings()) cnd.append( " multiple" );
      cnd.append( " " ).append( OnParentVersionAction.nameFromValue( nd[i].getOnParentVersion() ));
      
      cnd.append( "\n" );
    }

    cnd.append( "\n" );
    return cnd.toString();
  }
  
  
  /** Start of export CND file
   */
  public String createCND( Session pSession, boolean pInternalTypes ) throws Exception {
//    String cnd = "";
    StringBuilder cnd = new StringBuilder();
    
    // **** Write commented start
    SimpleDateFormat sdf = new SimpleDateFormat();
    cnd.append( "// ***********************************\n" );
    cnd.append( "// **** JeCARS exported nodetypes\n" );
    cnd.append( "// ****-------------------------------\n" );
    cnd.append( "// **** Export Nodetypes in CND format\n" );
    cnd.append( "// **** Created at: " );
    cnd.append( sdf.format( Calendar.getInstance().getTime()) );
    cnd.append( "\n\n" );
//    cnd += "// ***********************************\n";
//    cnd += "// **** JeCARS exported nodetypes\n";
//    cnd += "// ****-------------------------------\n";
//    cnd += "// **** Export Nodetypes in CND format\n";
//    cnd += "// **** Created at: " + sdf.format( Calendar.getInstance().getTime()) + "\n\n";
    
    // **** Write namespaces
    String ns[] = pSession.getNamespacePrefixes();
    for (int i = 0; i<ns.length; i++ ) {
      if (ns[i].equals("")==false) {
        cnd.append( "<'" ).append( ns[i] ).append( "'='" ).append( pSession.getNamespaceURI(ns[i]) ).append( "'>\n" );
//        cnd += "<'" + ns[i] + "'='" + pSession.getNamespaceURI(ns[i]) + "'>\n";
      }
    }
    cnd.append( '\n' );
//    cnd += '\n';
    
    mNTs.clear();
    NodeTypeManager ntm = pSession.getWorkspace().getNodeTypeManager();
    NodeTypeIterator nti = ntm.getAllNodeTypes();
    while( nti.hasNext() ) {
      NodeType nt = nti.nextNodeType();
      mNTs.replaceData( nt.getName(), nt );
    }
    
    String topNode = getTopNodeType( null );
    while( topNode!=null ) {
      boolean include = true;
      if (pInternalTypes==false) {
        if ((topNode.startsWith( "nt:"  )) || (topNode.startsWith( "mix:" )) ||
            (topNode.startsWith( "rep:" ))) {
          include = false;
        }
      }
      if (include==true) {
        String cnt = nodeTypeToCND( topNode );
        if (cnt!=null) cnd.append( cnt );
      }
//      if (include==true) cnd += nodeTypeToCND( topNode );
      mNTs.removeData( topNode );
      topNode = getTopNodeType( null );      
    }
    return cnd.toString();
  }
  
}
