/*
 * Copyright 2008 NLR - National Aerospace Laboratory
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
package org.jecars.client.nt;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_DefaultNode;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Filter;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.JC_Params;
import org.jecars.client.JC_Query;
import org.jecars.client.JC_RESTComm;

/**
 * JC_UserNode
 *
 * @version $Id: JC_UserNode.java,v 1.7 2009/05/27 14:17:59 weertj Exp $
 */
public class JC_UserNode extends JC_DefaultNode {

    /** getUsername
     * 
     * @return
     * @throws org.jecars.client.JC_Exception
     */
    public String getUsername() throws JC_Exception {
        String name = getName();
        // Map %40 to @
        name = name.replaceAll("%40", "@");
        // TODO: Map other escaped HTML characters, but don't expect these
        return name;
    }

    /** getFullname
     * 
     * @return
     * @throws org.jecars.client.JC_Exception
     */
    public String getFullname() throws JC_Exception {
      return (String) getProperty("jecars:Fullname").getValue();
    }

    public void setFullname(final String pFullname) throws JC_Exception {
      String value = pFullname == null ? "" : new String(pFullname);
      setProperty("jecars:Fullname", value);
      return;
    }

    /** setPassword
     * Changes the password of the user. After changing, the password will
     * no longer be expired, if it was.
     * 
     * @param pPassword
     * @return
     * @throws org.jecars.client.JC_Exception
     */
    public void setPassword(final char[] pPassword) throws JC_Exception {
        setProperty("jecars:Password_crypt", new String(pPassword));
        return;
    }

    /** changePassword
     *
     * @param pOldPassword
     * @param pNewPassword
     * @throws JC_Exception
     */
    public void changePassword( final char[] pOldPassword, final char[] pNewPassword ) throws JC_Exception {
      final JC_Clientable client = getClient();
      final char[] pwd = client.getPassword();
      try {
        client.setCredentials( client.getUsername(), pOldPassword );
        setProperty("jecars:Password_crypt", new String(pNewPassword) );
        save();
      } finally {
        client.setProxyCredentials( client.getUsername(), pwd );
      }
      return;
    }

    /** expirePassword
     * Marks the password of the user as expired. This means that the user is
     * forced to change the password on next logon. Until the password is changed,
     * the user cannot work with JeCARS.
     * 
     * @param
     * @return
     * @throws org.jecars.client.JC_Exception
     */
    public void expirePassword() throws JC_Exception {
      setProperty( "jecars:PasswordMustChange", "true" );
      return;
    }

    /** setSuspended
     * Suspends the user (or disable the suspension). While the user is suspended disabled, logon is not
     * possible.
     * 
     * @param value
     * @return
     * @throws org.jecars.client.JC_Exception
     */
    public void setSuspended( final boolean pValue ) throws JC_Exception {
      setProperty( "jecars:Suspended", String.valueOf(pValue) );
      return;
    }

    /** getSuspended
     * 
     * @return
     * @throws org.jecars.client.JC_Exception
     */
    public boolean getSuspended() throws JC_Exception {
      if (hasProperty( "jecars:Suspended" )) {
        return (Boolean)(getProperty( "jecars:Suspended" ).getValueAs( Boolean.class ));
      }
      return false;
    }

    /** getPasswordChangedAt
     * 
     * @return
     * @throws org.jecars.client.JC_Exception
     */
    public Calendar getPasswordChangedAt() throws JC_Exception {
      try {
        return (Calendar)getProperty( "jecars:PasswordChangedAt" ).getValueAs( Calendar.class );
      } catch( JC_Exception je ) {
        if (je.getErrorCode()==JC_Exception.ERROR_PROPERTYNOTFOUND) {
          refresh();
          return (Calendar)getProperty( "jcr:created" ).getValueAs( Calendar.class );        
        }
        throw je;
      }
    }

    public void setFirstName( final String pFirstName ) throws JC_Exception {
      final String value = pFirstName == null ? "" : pFirstName;
      if (pFirstName!=null) {
        setProperty("jecars:FirstName", value);
      }
    }

    public String getFirstName() throws JC_Exception {
      try {
        return getProperty("jecars:FirstName").getValueString();
      } catch (JC_Exception je) {
        if (je.getErrorCode() == JC_Exception.ERROR_PROPERTYNOTFOUND) {
          return "";
        }
        throw je;
      }
    }

    public void setPrefix(final String pPrefix) throws JC_Exception {
      final String value = pPrefix == null ? "" : pPrefix;
      setProperty("jecars:Prefix", value);
      return;
    }

    public String getPrefix() throws JC_Exception {
      try {
        return (String) getProperty("jecars:Prefix").getValue();
      } catch (JC_Exception je) {
        if (je.getErrorCode() == JC_Exception.ERROR_PROPERTYNOTFOUND) {
          return "";
        }
        throw je;
      }
    }

    public void setLastName(final String pLastName) throws JC_Exception {
      final String value = pLastName == null ? "" : pLastName;
      setProperty("jecars:LastName", value);
      return;
    }

    public String getLastName() throws JC_Exception {
      try {
        return (String) getProperty("jecars:LastName").getValueString();
      } catch (JC_Exception je) {
        if (je.getErrorCode() == JC_Exception.ERROR_PROPERTYNOTFOUND) {
          return "";
        }
        throw je;
      }
    }

    public void setUserTitle(final String pUserTitle) throws JC_Exception {
      final String value = pUserTitle == null ? "" : pUserTitle;
      setProperty("jecars:UserTitle", value);
      return;
    }

    public String getUserTitle() throws JC_Exception {
      try {
        return (String) getProperty("jecars:UserTitle").getValue();
      } catch (JC_Exception je) {
        if (je.getErrorCode() == JC_Exception.ERROR_PROPERTYNOTFOUND) {
          return "";
        }
        throw je;
      }
    }

    public void setEmail(final String pEmail) throws JC_Exception {
      final String value = pEmail == null ? "" : pEmail;
      setProperty("jecars:Email", value);
    }

    public String getEmail() throws JC_Exception {
      try {
        return (String) getProperty("jecars:Email").getValue();
      } catch (JC_Exception je) {
        if (je.getErrorCode() == JC_Exception.ERROR_PROPERTYNOTFOUND) {
          return "";
        }
        throw je;
      }
    }

    public void setInitials(final String pInitials) throws JC_Exception {
      final String value = pInitials == null ? "" : pInitials;
      setProperty("jecars:Initials", value);
      return;
    }

    public String getInitials() throws JC_Exception {
      try {
        return (String) getProperty("jecars:Initials").getValue();
      } catch (JC_Exception je) {
        if (je.getErrorCode() == JC_Exception.ERROR_PROPERTYNOTFOUND) {
          return "";
        }
        throw je;
      }
    }

    public void setCompany(final String pCompany) throws JC_Exception {
      final String value = pCompany == null ? "" : pCompany;
      setProperty("jecars:Company", value);
      return;
    }

    public String getCompany() throws JC_Exception {
      try {
        return (String) getProperty("jecars:Company").getValue();
      } catch (JC_Exception je) {
        if (je.getErrorCode() == JC_Exception.ERROR_PROPERTYNOTFOUND) {
          return "";
        }
        throw je;
      }
    }

    public void setDepartment(final String pDepartment) throws JC_Exception {
      final String value = pDepartment == null ? "" : pDepartment;
      setProperty("jecars:Department", value);
    }

    public String getDepartment() throws JC_Exception {
      try {
        return (String) getProperty("jecars:Department").getValue();
      } catch (JC_Exception je) {
        if (je.getErrorCode() == JC_Exception.ERROR_PROPERTYNOTFOUND) {
          return "";
        }
        throw je;
      }
    }

    public void setStreet(final String pStreet) throws JC_Exception {
      final String value = pStreet == null ? "" : pStreet;
      setProperty("jecars:Street", value);
      return;
    }

    public String getStreet() throws JC_Exception {
      try {
        return (String) getProperty("jecars:Street").getValue();
      } catch (JC_Exception je) {
        if (je.getErrorCode() == JC_Exception.ERROR_PROPERTYNOTFOUND) {
          return "";
        }
        throw je;
      }
    }

    public void setPostalCode(final String pPostalCode) throws JC_Exception {
      String value = pPostalCode == null ? "" : new String(pPostalCode);
      setProperty("jecars:PostalCode", value);
    }

    public String getPostalCode() throws JC_Exception {
      try {
        return (String) getProperty("jecars:PostalCode").getValue();
      } catch (JC_Exception je) {
        if (je.getErrorCode() == JC_Exception.ERROR_PROPERTYNOTFOUND) {
          return "";
        }
        throw je;
      }
    }

    public void setCity(final String pCity) throws JC_Exception {
      final String value = pCity == null ? "" : pCity;
      setProperty("jecars:City", value);
      return;
    }

    public String getCity() throws JC_Exception {
      try {
        return (String) getProperty("jecars:City").getValue();
      } catch (JC_Exception je) {
        if (je.getErrorCode() == JC_Exception.ERROR_PROPERTYNOTFOUND) {
          return "";
        }
        throw je;
      }
    }

    public void setCountry(final String pCountry) throws JC_Exception {
      final String value = pCountry == null ? "" : pCountry;
      setProperty("jecars:Country", value);
      return;
    }

    public String getCountry() throws JC_Exception {
      try {
        return (String) getProperty("jecars:Country").getValue();
      } catch (JC_Exception je) {
        if (je.getErrorCode() == JC_Exception.ERROR_PROPERTYNOTFOUND) {
          return "";
        }
        throw je;
      }
    }

    public void setPhone(final String pPhone) throws JC_Exception {
      final String value = pPhone == null ? "" : pPhone;
      setProperty("jecars:Phone", value);
      return;
    }

    public String getPhone() throws JC_Exception {
      try {
        return (String) getProperty("jecars:Phone").getValue();
      } catch (JC_Exception je) {
        if (je.getErrorCode() == JC_Exception.ERROR_PROPERTYNOTFOUND) {
          return "";
        }
        throw je;
      }
    }

    public void setOtherPhone(final String pOtherPhone) throws JC_Exception {
      final String value = pOtherPhone == null ? "" : pOtherPhone;
      setProperty( "jecars:OtherPhone", value );
      return;
    }

    public String getOtherPhone() throws JC_Exception {
      try {
        return (String) getProperty("jecars:OtherPhone").getValue();
      } catch (JC_Exception je) {
        if (je.getErrorCode() == JC_Exception.ERROR_PROPERTYNOTFOUND) {
          return "";
        }
        throw je;
      }
    }

    public void setFax(final String pFax) throws JC_Exception {
      String value = pFax == null ? "" : pFax;
      setProperty("jecars:Fax", value);
    }

    public String getFax() throws JC_Exception {
      try {
        return (String) getProperty("jecars:Fax").getValue();
      } catch (JC_Exception je) {
        if (je.getErrorCode() == JC_Exception.ERROR_PROPERTYNOTFOUND) {
          return "";
        }
        throw je;
      }
    }

    public void setMobile(final String pMobile) throws JC_Exception {
      String value = pMobile == null ? "" : pMobile;
      setProperty("jecars:Mobile", value);
    }

    public String getMobile() throws JC_Exception {
      try {
        return (String) getProperty("jecars:Mobile").getValue();
      } catch (JC_Exception je) {
        if (je.getErrorCode() == JC_Exception.ERROR_PROPERTYNOTFOUND) {
          return "";
        }
        throw je;
      }
    }

    public void setWebsite(final String pWebsite) throws JC_Exception {
      String value = pWebsite == null ? "" : new String(pWebsite);
      setProperty("jecars:Website", value);
    }

    public String getWebsite() throws JC_Exception {
      try {
        return (String) getProperty("jecars:Website").getValue();
      } catch (JC_Exception je) {
        if (je.getErrorCode() == JC_Exception.ERROR_PROPERTYNOTFOUND) {
          return "";
        }
        throw je;
      }
    }

    /** getPrefsNode
     *
     * @return
     * @throws org.jecars.client.JC_Exception
     */
    public JC_PrefsNode getPrefsNode() throws JC_Exception {
      final JC_DefaultNode n = (JC_DefaultNode)getNode( "jecars:Prefs" );
      return (JC_PrefsNode)n.morphToNodeType();
    }

    /** getGroups
     * 
     * @return
     * @throws JC_Exception 
     */
    public List<JC_GroupNode> getGroups() throws JC_Exception {
      final List<JC_GroupNode> groupList = new ArrayList<JC_GroupNode>();
      final JC_Clientable c = getClient();
      final JC_Params params = c.createParams( JC_RESTComm.GET ).cloneParams();
      final JC_Filter filter = JC_Filter.createFilter();
      filter.addCategory( "jecars:Group" );
      final JC_Query  query  = JC_Query.createQuery();
      query.setWhereString( JC_GroupNode.GROUPMEMBERS + " LIKE '%" + getName() + "'" );
      final JC_Nodeable groups = c.getSingleNode( "/JeCARS/default/Groups" );      
      for( final JC_Nodeable group : groups.getNodes( params, filter, query )) {
        groupList.add( (JC_GroupNode)group.morphToNodeType() );
      }
      groups.refresh();
      return groupList;
    }
    
    public List<JC_Nodeable> getRoleFeatures() throws JC_Exception {
      final List<JC_Nodeable> fList = new ArrayList<JC_Nodeable>();
      final JC_Clientable c = getClient();
      final JC_Params params = c.createParams( JC_RESTComm.GET ).cloneParams();
      params.setDeep( true );
      final JC_Filter filter = JC_Filter.createFilter();
      filter.addCategory( "jecars:Feature" );
      final JC_Nodeable groups = c.getSingleNode( "/JeCARS/default/Groups" );      
      for( final JC_Nodeable feature : groups.getNodes( params, filter, null )) {
        fList.add( feature );
      }
      return fList;      
    }
    
    
}
