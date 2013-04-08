/*
 * Copyright 2010 NLR - National Aerospace Laboratory
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

package org.jecars.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_DefaultNode;
import org.jecars.client.JC_Defs;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Filter;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.JC_Params;
import org.jecars.client.JC_RESTComm;
import org.jecars.client.JC_Rights;
import org.jecars.client.nt.JC_GroupNode;
import org.jecars.client.nt.JC_GroupsNode;
import org.jecars.client.nt.JC_PermissionNode;
import org.jecars.client.nt.JC_UserNode;
import org.jecars.client.nt.JC_UsersNode;

/**
 *
 * @author weert
 */
public class UserManagerPanel extends javax.swing.JPanel {

    static enum ITEMCHANGED { USERLIST, GROUPLIST, GROUPNAMEID, NAMEID };

    private boolean mIsRefreshing = false;

    private JC_Clientable mClient;
    private JC_UsersNode  mUsers;
    private JC_GroupsNode mGroups;


    /** UsersCellRenderer
     *
     */
    private class UsersCellRenderer extends DefaultListCellRenderer {

      /** getListCellRendererComponent
       *
       * @param list
       * @param value
       * @param index
       * @param isSelected
       * @param cellHasFocus
       * @return
       */
      @Override
      public Component getListCellRendererComponent(JList list, Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
        if (value instanceof JC_UserNode) {
          try {
            JC_UserNode user = (JC_UserNode)value;
            JLabel comp = new JLabel( user.getName() );
            if (isSelected) {
              comp.setBackground( Color.LIGHT_GRAY );
              comp.setOpaque( true );
            }
            return comp;
          } catch( Exception e) {
            e.printStackTrace();
          }
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus );
      }

    }

    /** GroupsCellRenderer
     *
     */
    private class GroupsCellRenderer extends DefaultListCellRenderer {

      /** getListCellRendererComponent
       *
       * @param list
       * @param value
       * @param index
       * @param isSelected
       * @param cellHasFocus
       * @return
       */
      @Override
      public Component getListCellRendererComponent(JList list, Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
        if (value instanceof JC_GroupNode) {
          try {
            JC_GroupNode group = (JC_GroupNode)value;
            JLabel comp = new JLabel( group.getName() );
            if (isSelected) {
              comp.setBackground( Color.LIGHT_GRAY );
              comp.setOpaque( true );
            }
            final JC_UserNode userN = (JC_UserNode)mUsersList.getSelectedValue();
            
            if (group.isRole()) {
              comp.setFont( comp.getFont().deriveFont( Font.ITALIC ));              
            }
            
            if ((userN!=null) && (group.isMember( userN ))) {
              comp.setFont( comp.getFont().deriveFont( Font.BOLD ));
              comp.setForeground( Color.BLUE );
            }

            return comp;
          } catch( Exception e) {
          }
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus );
      }

    }


    /** GroupsCellRenderer
     *
     */
    private class GroupMemberCellRenderer extends DefaultListCellRenderer {

      /** getListCellRendererComponent
       *
       * @param list
       * @param value
       * @param index
       * @param isSelected
       * @param cellHasFocus
       * @return
       */
      @Override
      public Component getListCellRendererComponent(JList list, Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
        JLabel comp = null;
        if (value instanceof JC_GroupNode) {
          try {
            JC_GroupNode group = (JC_GroupNode)value;
            comp = new JLabel( group.getName() );
          } catch( Exception e) {
          }
        } else if (value instanceof JC_UserNode) {
          try {
            JC_UserNode user = (JC_UserNode)value;
            comp = new JLabel( user.getName() );
          } catch( Exception e) {
          }
        } else if (value instanceof JC_Nodeable) {
          JC_Nodeable node = (JC_Nodeable)value;
          try {
            node.getProperties();
            if (node.isInErrorState()) throw new JC_Exception( "ERROR" );
            comp = new JLabel( node.getName() );
            comp.setForeground( Color.RED );
          } catch( JC_Exception je ) {
            comp = new JLabel( node.getName() );
            comp.setForeground( Color.RED );
          }
        }
        if (comp!=null) {
          if (isSelected) {
            comp.setBackground( Color.LIGHT_GRAY );
            comp.setOpaque( true );
          }
          return comp;
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus );
      }

    }


    /** Creates new form UserManagerPanel
     */
    public UserManagerPanel() {
      initComponents();
      mUsersList.setCellRenderer(  new UsersCellRenderer() );
      mGroupsList.setCellRenderer( new GroupsCellRenderer() );
      mGroupMemberList.setCellRenderer( new GroupMemberCellRenderer() );
      return;
    }

    /** setClient
     * 
     * @param pClient
     */
    public void setClient( final JC_Clientable pClient ) throws JC_Exception {
      mClient = pClient;
      JC_Params params = mClient.createParams( JC_RESTComm.GET );
//      params.setFilterEventTypes( "READ,HEAD" );
      mClient.setDefaultParams( JC_RESTComm.GET, params );
      refresh();
      return;
    }

    /** refreshSelectedUser
     * 
     * @throws org.jecars.client.JC_Exception
     */
    public void refreshSelectedUser() throws JC_Exception {
      final JC_UserNode userN = (JC_UserNode)mUsersList.getSelectedValue();
      if (userN==null) {
        mNameID.setText( "" );
        mNameID.setEditable( true );
        mNameID.setEnabled( true );
      } else {
        mNameID.setText( userN.getName() );
        mNameID.setEditable( false );
        mNameID.setEnabled( false );
      }
      mNewPassword.setText( "" );
      mSetPasswordMessage.setText( "" );
      if (userN==null) {
        mFirstName.setText( "" );
        mLastName.setText(  "" );
        mEmail.setText(  "" );
      } else {
        mFirstName.setText( userN.getFirstName() );
        mLastName.setText( userN.getLastName() );
        mEmail.setText( userN.getEmail() );
        mModifyUser.setEnabled( true );
      }
      refreshGroup();
      return;
    }

    public void refreshSelectedGroup() throws JC_Exception {
      final JC_GroupNode groupN = (JC_GroupNode)mGroupsList.getSelectedValue();
      if (groupN==null) {
        mGroupNameID.setText( "" );
        mGroupNameID.setEditable( true );
        mGroupNameID.setEnabled( true );
        mGroupTitle.setText( "" );
        mGroupTitle.setEditable( true );
        mGroupTitle.setEnabled( true );
        mIsRole.setSelected( false );
        mIsRole.setEnabled( true );
      } else {
        mGroupNameID.setText( groupN.getName() );
        mGroupNameID.setEditable( false );
        mGroupNameID.setEnabled( false );
        if (groupN.hasProperty( "jecars:Title" )) {
          mGroupTitle.setText( groupN.getProperty( "jecars:Title" ).getValueString() );
        } else {
          mGroupTitle.setText( "" );
        }
        mGroupTitle.setEditable( false );
        mGroupTitle.setEnabled( false );
        mIsRole.setSelected( groupN.isRole() );
        mIsRole.setEnabled( true );

        final JC_UserNode userN = (JC_UserNode)mUsersList.getSelectedValue();
        if (userN==null) {
          mRemoveMembership.setEnabled( false );
          mAddMemberShip.setEnabled(    false );
          mAddMemberShipAsRole.setEnabled(    false );
        } else {
          boolean isMember = groupN.isMember( userN );
          mRemoveMembership.setEnabled( isMember );
          mAddMemberShip.setEnabled( !isMember );
          mAddMemberShipAsRole.setEnabled( !isMember );
        }
      }
      refreshMembersOfGroup();
      return;
    }

    /** refreshMembersOfGroup
     *
     * @throws JC_Exception
     */
    public void refreshMembersOfGroup() throws JC_Exception {
      mGroupMemberList.clearSelection();
      mRemoveMember.setEnabled( false );
      final JC_GroupNode groupN = (JC_GroupNode)mGroupsList.getSelectedValue();
      mGroupMemberList.setModel( new DefaultListModel() );
      final DefaultListModel dlm = (DefaultListModel)mGroupMemberList.getModel();
      dlm.clear();
      if (groupN!=null) {
        final Collection<JC_UserNode>users = groupN.getUsers();
        for (JC_UserNode user : users) {
          dlm.addElement( user );
        }
        final Collection<JC_GroupNode>groups = groupN.getGroups();
        for (JC_GroupNode group : groups) {
          dlm.addElement( group );
        }
        groupN.refresh();
        final Collection<JC_Nodeable>errorMembers = groupN.getErrorMembers();
        for (JC_Nodeable em : errorMembers) {
          dlm.addElement( em );
        }
      }
      return;
    }

    /** refreshGroup
     *
     * @throws JC_Exception
     */
    public void refreshGroup() throws JC_Exception {
      mRemoveMembership.setEnabled( false );
      mGroupsList.clearSelection();
      mGroupsList.repaint();

      mGroupNameID.setText( "" );
      mGroupNameID.setEditable( true );
      mGroupNameID.setEnabled( true );

      refreshMembersOfGroup();

//      JC_UserNode user = (JC_UserNode)mUsersList.getSelectedValue();
//      Collection<JC_GroupNode> groups = mGroups.getGroups();
//      int ix = 0;
//      for (JC_GroupNode group : groups) {
//        if (group.isMember( user )) {
//          mGroupsList.addSelectionInterval( ix, ix );
//          mRemoveMembership.setEnabled( true );
//        }
//        ix++;
//      }
//      user.g
//      mGroupsList.setSelectedIndex( -1 );
      return;
    }

    public void refresh() throws JC_Exception {
      refreshUsers();
      refreshGroups();
      return;
    }

    static class compareJCNode implements Comparator<JC_Nodeable> {

      @Override
      public int compare(JC_Nodeable o1, JC_Nodeable o2) {
        return o1.getName().compareTo(o2.getName());
      }
      
    }
    
    /** refreshUsers
     *
     * @throws org.jecars.client.JC_Exception
     */
    public void refreshUsers() throws JC_Exception {
      if (mUsers==null) mUsers = mClient.getUsersNode();
      mUsersList.setModel( new DefaultListModel() );
      DefaultListModel dlm = (DefaultListModel)mUsersList.getModel();
      dlm.clear();
      JC_Filter filter = JC_Filter.createFilter();
      filter.addCategory( "jecars:User" );
      Collection<JC_Nodeable> usersN = mUsers.getNodes( null, filter, null );
      List<JC_Nodeable> listUsersN = new ArrayList<JC_Nodeable>( usersN );
      Collections.sort( listUsersN, new compareJCNode());
      for ( JC_Nodeable userN : listUsersN) {
        dlm.addElement( userN );
      }
      return;
    }

    /** refreshGroups
     *
     * @throws JC_Exception
     */
    public void refreshGroups() throws JC_Exception {
      if (mGroups==null) mGroups = mClient.getGroupsNode();
      mGroupsList.setModel( new DefaultListModel() );
      DefaultListModel dlm = (DefaultListModel)mGroupsList.getModel();
      dlm.clear();
      JC_Filter filter = JC_Filter.createFilter();
      filter.addCategory( "jecars:Group" );
      Collection<JC_Nodeable> groupsN = mGroups.getNodes( null, filter, null );
      List<JC_Nodeable> listGroupsN = new ArrayList<JC_Nodeable>( groupsN );
      Collections.sort( listGroupsN, new compareJCNode());
      for ( JC_Nodeable groupN : listGroupsN) {
        dlm.addElement( groupN );
      }
      refreshMembersOfGroup();
      return;
    }

    /** refreshData
     *
     * @param pIC
     */
    private void refreshData( final ITEMCHANGED pIC ) throws JC_Exception {
      if (mIsRefreshing) return;
      mIsRefreshing = true;
      try {
        mCreateUser.setEnabled( false );
        mRemoveUser.setEnabled( false );
//        mModifyUser.setEnabled( false );
        final String nameID = mNameID.getText();
        final String groupID = mGroupNameID.getText();
        if (pIC.equals( ITEMCHANGED.NAMEID )) {
          if ((!"".equals(nameID)) && (mUsers.hasNode( nameID ))) {
            mUsersList.setSelectedValue( mUsers.getNode( nameID ), true );
          } else {
            mUsersList.clearSelection();
          }
        }
        if (pIC.equals( ITEMCHANGED.USERLIST )) {
          refreshSelectedUser();
        }
        if (pIC.equals( ITEMCHANGED.GROUPNAMEID )) {
          if ((!"".equals(groupID)) && (mGroups.hasNode( groupID ))) {
            mGroupsList.setSelectedValue( mGroups.getNode( nameID ), true );
          } else {
            mGroupsList.clearSelection();
          }
        }
        if (pIC.equals( ITEMCHANGED.GROUPLIST )) {
          refreshSelectedGroup();
        }

        if (mUsers.hasNode(nameID)) {
          mRemoveUser.setEnabled( true );
        } else if ((mNewPassword.getPassword().length>0) &&
                   (mFirstName.getText().length()>0) &&
                   (mLastName.getText().length()>0)) {
          mCreateUser.setEnabled( true );
        }
        if (mGroups.hasNode(groupID)) {
          mRemoveGroup.setEnabled( true );
        } else if (1==1) {
          mCreateGroup.setEnabled( true );
        } else {
          mRemoveGroup.setEnabled( true );
        }
      } finally {
        mIsRefreshing = false;
      }
      return;
    }

    private void reportError( Throwable e ) {
      e.printStackTrace();
      return;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        mUsersScroll = new javax.swing.JScrollPane();
        mUsersList = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        mNameID = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        mNewPassword = new javax.swing.JPasswordField();
        mSetNewPassword = new javax.swing.JButton();
        mSetPasswordMessage = new javax.swing.JLabel();
        mRemoveUser = new javax.swing.JButton();
        mCreateUser = new javax.swing.JButton();
        mModifyUser = new javax.swing.JButton();
        mUserMayAccess = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        mFirstName = new javax.swing.JTextField();
        mLastName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        mEmail = new javax.swing.JTextField();
        mClearData = new javax.swing.JButton();
        mDefaultMembership = new javax.swing.JCheckBox();
        mSealUser = new javax.swing.JButton();
        mUnsealUser = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        mGroupsList = new javax.swing.JList();
        jPanel6 = new javax.swing.JPanel();
        mAddMemberShip = new javax.swing.JButton();
        mRemoveMembership = new javax.swing.JButton();
        mAddMemberShipAsRole = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        mGroupMemberList = new javax.swing.JList();
        jLabel6 = new javax.swing.JLabel();
        mRemoveMember = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        mAddGroupMembership = new javax.swing.JButton();
        mAddGroupMemberName = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        mRemoveGroup = new javax.swing.JPanel();
        mGroupNameID = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        mGroupTitle = new javax.swing.JTextField();
        mIsRole = new javax.swing.JCheckBox();
        mClearGroupData = new javax.swing.JButton();
        mCreateGroup = new javax.swing.JButton();
        mRemoveUser1 = new javax.swing.JButton();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Users"));

        mUsersList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        mUsersList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        mUsersList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                mUsersListValueChanged(evt);
            }
        });
        mUsersScroll.setViewportView(mUsersList);

        jLabel1.setText("Name (ID)");

        mNameID.setBackground(new java.awt.Color(255, 255, 204));
        mNameID.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                mNameIDKeyReleased(evt);
            }
        });

        jLabel2.setText("New password");

        mNewPassword.setBackground(new java.awt.Color(255, 255, 204));
        mNewPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                mNewPasswordKeyReleased(evt);
            }
        });

        mSetNewPassword.setText("Set");
        mSetNewPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSetNewPasswordActionPerformed(evt);
            }
        });

        mSetPasswordMessage.setText(" ");

        mRemoveUser.setForeground(new java.awt.Color(255, 0, 0));
        mRemoveUser.setText("Remove User");
        mRemoveUser.setEnabled(false);
        mRemoveUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mRemoveUserActionPerformed(evt);
            }
        });

        mCreateUser.setText("Create User");
        mCreateUser.setEnabled(false);
        mCreateUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mCreateUserActionPerformed(evt);
            }
        });

        mModifyUser.setText("Modify User");
        mModifyUser.setEnabled(false);
        mModifyUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mModifyUserActionPerformed(evt);
            }
        });

        mUserMayAccess.setSelected(true);
        mUserMayAccess.setText("User may access user data");

        jPanel4.setBackground(new java.awt.Color(232, 232, 232));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "User data", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(102, 102, 102)));

        jPanel5.setOpaque(false);

        jLabel3.setText("First name");

        mFirstName.setBackground(new java.awt.Color(255, 255, 204));
        mFirstName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                mFirstNameKeyReleased(evt);
            }
        });

        mLastName.setBackground(new java.awt.Color(255, 255, 204));
        mLastName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                mLastNameKeyReleased(evt);
            }
        });

        jLabel4.setText("Last name");

        jLabel9.setText("EMail");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(mLastName)
                    .addComponent(mFirstName, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel9)
                    .addComponent(mEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(mLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(38, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(78, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        mClearData.setText("Clear Data");
        mClearData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mClearDataActionPerformed(evt);
            }
        });

        mDefaultMembership.setSelected(true);
        mDefaultMembership.setText("Default Membership");

        mSealUser.setText("Seal User");
        mSealUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSealUserActionPerformed(evt);
            }
        });

        mUnsealUser.setText("Unseal User");
        mUnsealUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mUnsealUserActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(mNameID)
                                    .addComponent(mNewPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(mSetNewPassword)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(mSealUser)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(mUnsealUser)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(mSetPasswordMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(mUserMayAccess)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(mDefaultMembership))))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(mCreateUser)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mModifyUser, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mClearData)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mRemoveUser))))
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(mNameID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mUserMayAccess)
                    .addComponent(mDefaultMembership))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(mNewPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mSetPasswordMessage)
                    .addComponent(mSetNewPassword)
                    .addComponent(mSealUser)
                    .addComponent(mUnsealUser))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mCreateUser)
                    .addComponent(mModifyUser)
                    .addComponent(mClearData)
                    .addComponent(mRemoveUser))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(mUsersScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(89, 89, 89))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(mUsersScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Groups"));

        mGroupsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        mGroupsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        mGroupsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                mGroupsListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(mGroupsList);

        jPanel6.setBackground(new java.awt.Color(230, 230, 230));

        mAddMemberShip.setText("Add User Membership");
        mAddMemberShip.setActionCommand("Add Membership");
        mAddMemberShip.setEnabled(false);
        mAddMemberShip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mAddMemberShipActionPerformed(evt);
            }
        });

        mRemoveMembership.setText("Remove User Membership");
        mRemoveMembership.setEnabled(false);
        mRemoveMembership.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mRemoveMembershipActionPerformed(evt);
            }
        });

        mAddMemberShipAsRole.setText("Add User Membership (As Role)");
        mAddMemberShipAsRole.setActionCommand("Add Membership");
        mAddMemberShipAsRole.setEnabled(false);
        mAddMemberShipAsRole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mAddMemberShipAsRoleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mRemoveMembership, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mAddMemberShip, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mAddMemberShipAsRole)
                .addGap(71, 71, 71))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mRemoveMembership)
                    .addComponent(mAddMemberShip)
                    .addComponent(mAddMemberShipAsRole))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        mGroupMemberList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        mGroupMemberList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        mGroupMemberList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                mGroupMemberListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(mGroupMemberList);

        jLabel6.setBackground(new java.awt.Color(204, 204, 204));
        jLabel6.setText("GroupMembers");
        jLabel6.setOpaque(true);

        mRemoveMember.setText("Remove Member");
        mRemoveMember.setEnabled(false);
        mRemoveMember.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mRemoveMemberActionPerformed(evt);
            }
        });

        jPanel7.setBackground(new java.awt.Color(230, 230, 230));

        mAddGroupMembership.setText("Add Group Membership");
        mAddGroupMembership.setEnabled(false);
        mAddGroupMembership.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mAddGroupMembershipActionPerformed(evt);
            }
        });

        mAddGroupMemberName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                mAddGroupMemberNameKeyReleased(evt);
            }
        });

        jLabel7.setText("Group Name");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mAddGroupMemberName, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                .addGap(8, 8, 8)
                .addComponent(mAddGroupMembership)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mAddGroupMembership)
                    .addComponent(mAddGroupMemberName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        mRemoveGroup.setBackground(new java.awt.Color(239, 255, 255));
        mRemoveGroup.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Group data", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(102, 102, 102)));

        mGroupNameID.setBackground(new java.awt.Color(255, 255, 204));
        mGroupNameID.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                mGroupNameIDKeyReleased(evt);
            }
        });

        jLabel5.setText("Groupname (ID)");

        jLabel8.setText("Group title");

        mIsRole.setText("Is Role");
        mIsRole.setContentAreaFilled(false);
        mIsRole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mIsRoleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mRemoveGroupLayout = new javax.swing.GroupLayout(mRemoveGroup);
        mRemoveGroup.setLayout(mRemoveGroupLayout);
        mRemoveGroupLayout.setHorizontalGroup(
            mRemoveGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mRemoveGroupLayout.createSequentialGroup()
                .addGroup(mRemoveGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mRemoveGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(mIsRole, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                    .addGroup(mRemoveGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(mGroupTitle)
                        .addComponent(mGroupNameID, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)))
                .addGap(379, 379, 379))
        );
        mRemoveGroupLayout.setVerticalGroup(
            mRemoveGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mRemoveGroupLayout.createSequentialGroup()
                .addGroup(mRemoveGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(mGroupNameID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mRemoveGroupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(mGroupTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mIsRole)
                .addContainerGap(58, Short.MAX_VALUE))
        );

        mClearGroupData.setText("Clear Group Data");
        mClearGroupData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mClearGroupDataActionPerformed(evt);
            }
        });

        mCreateGroup.setText("Create Group");
        mCreateGroup.setEnabled(false);
        mCreateGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mCreateGroupActionPerformed(evt);
            }
        });

        mRemoveUser1.setForeground(new java.awt.Color(255, 0, 0));
        mRemoveUser1.setText("Remove Group");
        mRemoveUser1.setEnabled(false);
        mRemoveUser1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mRemoveUser1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(mRemoveMember)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mRemoveGroup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(mCreateGroup)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mClearGroupData)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mRemoveUser1)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(3, 3, 3)
                                .addComponent(mRemoveMember))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mRemoveGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(mCreateGroup)
                                    .addComponent(mClearGroupData)
                                    .addComponent(mRemoveUser1))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 983, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void mUsersListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_mUsersListValueChanged
      try {
        refreshData( ITEMCHANGED.USERLIST );
      } catch( JC_Exception e ) {
        reportError( e );
      }
      return;
    }//GEN-LAST:event_mUsersListValueChanged

    private void mSetNewPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mSetNewPasswordActionPerformed
      try {
        Object[] users = mUsersList.getSelectedValues();
        for ( Object userN : users) {
          JC_UserNode user = (JC_UserNode)userN;
          user.setPassword( mNewPassword.getPassword() );
          user.save();
          mSetPasswordMessage.setText( "password set" );
        }
      } catch( Exception e ) {
        reportError( e );
      }
      return;
    }//GEN-LAST:event_mSetNewPasswordActionPerformed

    private void mRemoveUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRemoveUserActionPerformed
      if (JOptionPane.showConfirmDialog( mRemoveUser, "Are you sure you want to remove the selected user(s)?" )==JOptionPane.OK_OPTION) {
        try {
          Object[] users = mUsersList.getSelectedValues();
          for ( Object userN : users) {
            mUsers.removeUser( (JC_UserNode)userN );
          }
        } catch( Exception e ) {
          reportError(e);
        } finally {
          try {
            refreshUsers();
            refreshGroups();
          } catch( JC_Exception je ) {
            reportError( je );
          }
        }
      }
      return;
    }//GEN-LAST:event_mRemoveUserActionPerformed

    private void mNameIDKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mNameIDKeyReleased
      try {
        refreshData( ITEMCHANGED.NAMEID );
      } catch( JC_Exception je ) {
        reportError( je );
      }
      return;
    }//GEN-LAST:event_mNameIDKeyReleased

    private void mCreateUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mCreateUserActionPerformed
      try {
        final String fullname = mFirstName.getText() + " " + mLastName.getText();
        final JC_UserNode user;
        if (mUserMayAccess.isSelected()) {
          user = mUsers.addUser( mNameID.getText(), fullname, mNewPassword.getPassword(), JC_PermissionNode.RS_ALLRIGHTS );
        } else {
          user = mUsers.addUser( mNameID.getText(), fullname, mNewPassword.getPassword(), null );
        }
        user.setFirstName( mFirstName.getText() );
        user.setLastName(  mLastName.getText() );
        user.setEmail(     mEmail.getText() );
        user.save();
        if (mDefaultMembership.isSelected()) {
          JC_GroupNode group = mGroups.getGroup( "DefaultReadGroup" );
          group.addUser( user );
          group.save();
        }
        refreshUsers();
        refreshGroups();
        refreshData( ITEMCHANGED.NAMEID );
      } catch( JC_Exception je ) {
        reportError( je );
      }
      return;
    }//GEN-LAST:event_mCreateUserActionPerformed

    private void mNewPasswordKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mNewPasswordKeyReleased
      try {
        refreshData( ITEMCHANGED.NAMEID );
      } catch( JC_Exception je ) {
        reportError( je );
      }
      return;
    }//GEN-LAST:event_mNewPasswordKeyReleased

    private void mFirstNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mFirstNameKeyReleased
      try {
        refreshData( ITEMCHANGED.NAMEID );
      } catch( JC_Exception je ) {
        reportError( je );
      }
      return;
    }//GEN-LAST:event_mFirstNameKeyReleased

    private void mLastNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mLastNameKeyReleased
      try {
        refreshData( ITEMCHANGED.NAMEID );
      } catch( JC_Exception je ) {
        reportError( je );
      }
      return;
    }//GEN-LAST:event_mLastNameKeyReleased

    private void mRemoveMembershipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRemoveMembershipActionPerformed
      try {
        final JC_UserNode   userN = (JC_UserNode)mUsersList.getSelectedValue();
        final JC_GroupNode groupN = (JC_GroupNode)mGroupsList.getSelectedValue();
        if (userN!=null) {
          groupN.removeUser( userN );
          groupN.save();
          // **** Check for the permission object
          if (groupN.hasNode( "P_" + userN.getName() )) {
            groupN.getNode( "P_" + userN.getName() ).removeNode();
            groupN.save();
          }
          groupN.refresh();
        }
        refreshData( ITEMCHANGED.USERLIST );
      } catch( JC_Exception e ) {
        reportError( e );
      }
      return;
    }//GEN-LAST:event_mRemoveMembershipActionPerformed

    private void mGroupsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_mGroupsListValueChanged
      try {
        refreshData( ITEMCHANGED.GROUPLIST );
      } catch( JC_Exception e ) {
        reportError( e );
      }
      return;
    }//GEN-LAST:event_mGroupsListValueChanged

    private void mGroupNameIDKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mGroupNameIDKeyReleased
      try {
        refreshData( ITEMCHANGED.GROUPNAMEID );
      } catch( JC_Exception je ) {
        reportError( je );
      }
      return;
    }//GEN-LAST:event_mGroupNameIDKeyReleased

    private void mClearDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mClearDataActionPerformed
      try {
        mUsersList.clearSelection();
        refreshData( ITEMCHANGED.USERLIST );
      } catch( JC_Exception e ) {
        reportError( e );
      }
    }//GEN-LAST:event_mClearDataActionPerformed

    private void mAddMemberShipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mAddMemberShipActionPerformed
      try {
        final JC_UserNode   userN = (JC_UserNode)mUsersList.getSelectedValue();
        final JC_GroupNode groupN = (JC_GroupNode)mGroupsList.getSelectedValue();
        if (userN!=null) {
          groupN.addUser( userN );
          groupN.save();
          groupN.refresh();
        }
        refreshData( ITEMCHANGED.USERLIST );
      } catch( JC_Exception e ) {
        reportError( e );
      }
    }//GEN-LAST:event_mAddMemberShipActionPerformed

    private void mGroupMemberListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_mGroupMemberListValueChanged
      mRemoveMember.setEnabled( mGroupMemberList.getSelectedValue()!=null );
      return;
    }//GEN-LAST:event_mGroupMemberListValueChanged

    private void mRemoveMemberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRemoveMemberActionPerformed
      try {
        final JC_GroupNode groupN = (JC_GroupNode)mGroupsList.getSelectedValue();
        if (groupN!=null) {
          final JC_Nodeable memberN = (JC_Nodeable)mGroupMemberList.getSelectedValue();
          if (memberN instanceof JC_UserNode) {
            groupN.removeUser((JC_UserNode)memberN);
          } else if (memberN instanceof JC_GroupNode) {
            groupN.removeGroup((JC_GroupNode)memberN);
          } else {
            groupN.removeObject( memberN );
          }
          groupN.save();
          groupN.refresh();
          refreshMembersOfGroup();
          mRemoveMember.setEnabled( false );
        }
      } catch( JC_Exception e ) {
        reportError( e );
      }
    }//GEN-LAST:event_mRemoveMemberActionPerformed

    private void mAddGroupMemberNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mAddGroupMemberNameKeyReleased
      try {
        final JC_GroupNode groupN = (JC_GroupNode)mGroupsList.getSelectedValue();
        if (groupN!=null) {
          String agmn = mAddGroupMemberName.getText();
          if (mGroups.hasGroup( agmn )) {
            if (groupN.getGroupname().equals( agmn )) {
              mAddGroupMembership.setEnabled( false );
              mAddGroupMembership.setToolTipText( "Group " + agmn + " cannot be member of itself" );
            } else {
              mAddGroupMembership.setEnabled( true );
              mAddGroupMembership.setToolTipText( "Group " + agmn + " will be member of " + groupN.getGroupname() );
            }
          } else {
            mAddGroupMembership.setEnabled( false );
            mAddGroupMembership.setToolTipText( "Group " + agmn + " not found" );
          }
        } else {
          mAddGroupMembership.setEnabled( false );
          mAddGroupMembership.setToolTipText( "Select a group" );
        }
      } catch( JC_Exception e ) {
        reportError( e );
      }
      return;
    }//GEN-LAST:event_mAddGroupMemberNameKeyReleased

    private void mAddGroupMembershipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mAddGroupMembershipActionPerformed
      try {
        final JC_GroupNode groupN = (JC_GroupNode)mGroupsList.getSelectedValue();
        if (groupN!=null) {
          final JC_GroupNode g = mGroups.getGroup( mAddGroupMemberName.getText() );
          groupN.addGroup( g );
          groupN.save();
          refreshUsers();
          refreshGroups();
          refreshMembersOfGroup();
        }
      } catch( JC_Exception je ) {
        reportError( je );
      }
      return;
    }//GEN-LAST:event_mAddGroupMembershipActionPerformed

    private void mClearGroupDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mClearGroupDataActionPerformed
      try {
        mGroupsList.clearSelection();
        refreshData( ITEMCHANGED.GROUPLIST );
      } catch( JC_Exception e ) {
        reportError( e );
      }
    }//GEN-LAST:event_mClearGroupDataActionPerformed

    private void mRemoveUser1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mRemoveUser1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_mRemoveUser1ActionPerformed

    private void mCreateGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mCreateGroupActionPerformed
      try {
        final JC_GroupNode group = mGroups.addGroup( mGroupNameID.getText(), mGroupTitle.getText() );
        group.save();
        refreshUsers();
        refreshGroups();
        refreshData( ITEMCHANGED.NAMEID );
      } catch( JC_Exception je ) {
        reportError( je );
      }
      return;
    }//GEN-LAST:event_mCreateGroupActionPerformed

    private void mModifyUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mModifyUserActionPerformed
      try {
        final JC_UserNode user = mUsers.getUser( mNameID.getText() );
        if (user==null) {
          throw new JC_Exception( "User " + mNameID.getText() + " doesn't exists" );
        } else {
          user.setFirstName( mFirstName.getText() );
          user.setLastName(  mLastName.getText() );
          user.setEmail(     mEmail.getText() );
          user.save();
        }
      } catch( JC_Exception je ) {
        reportError( je );
      }
      return;
    }//GEN-LAST:event_mModifyUserActionPerformed

    private void mSealUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mSealUserActionPerformed
      try {
        final JC_UserNode user = mUsers.getUser( mNameID.getText() );
        if (user==null) {
          throw new JC_Exception( "User " + mNameID.getText() + " doesn't exists" );
        } else {
          final Collection<String> rights = new ArrayList<String>();
          rights.add( JC_Rights.R_READ );
          rights.add( JC_Rights.R_GETPROPERTY );
          rights.add( JC_Rights.R_DELEGATE );
          final JC_DefaultNode permN = (JC_DefaultNode)user.getNode( "jecars:P_UserPermission" );
          final JC_PermissionNode perm = (JC_PermissionNode)permN.morphToNodeType();
          perm.setProperty( "jecars:Principal", "+" + user.getPath() );
          perm.setRights( rights );
          perm.save();
        }
      } catch( JC_Exception je ) {
        reportError( je );
      }
      return;
    }//GEN-LAST:event_mSealUserActionPerformed

    private void mUnsealUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mUnsealUserActionPerformed
      try {
        final JC_UserNode user = mUsers.getUser( mNameID.getText() );
        if (user==null) {
          throw new JC_Exception( "User " + mNameID.getText() + " doesn't exists" );
        } else {
          final Collection<String> rights = new ArrayList<String>();
          rights.add( JC_Rights.R_ADDNODE );
          rights.add( JC_Rights.R_GETPROPERTY );
          rights.add( JC_Rights.R_READ );
          rights.add( JC_Rights.R_REMOVE );
          rights.add( JC_Rights.R_SETPROPERTY );
          rights.add( JC_Rights.R_DELEGATE );
          final JC_DefaultNode permN = (JC_DefaultNode)user.getNode( "jecars:P_UserPermission" );
          final JC_PermissionNode perm = (JC_PermissionNode)permN.morphToNodeType();
          perm.setProperty( "jecars:Principal", "+" + user.getPath() );
          if (perm.hasProperty( "jecars:P_UserPermission" )) {
            perm.setRights( rights );
          } else {
            perm.addRights( user, rights );
          }
          perm.save();
        }
      } catch( JC_Exception je ) {
        reportError( je );
      }
      return;
    }//GEN-LAST:event_mUnsealUserActionPerformed

    private void mAddMemberShipAsRoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mAddMemberShipAsRoleActionPerformed
      try {
        final JC_UserNode   userN = (JC_UserNode)mUsersList.getSelectedValue();
        final JC_GroupNode groupN = (JC_GroupNode)mGroupsList.getSelectedValue();
        if (userN!=null) {
          groupN.addUser( userN );
          groupN.save();
          groupN.addPermissionNode( "P_" + userN.getName(), userN, JC_PermissionNode.RS_ALLREADACCESS );
          groupN.save();
          groupN.refresh();          
        }
        refreshData( ITEMCHANGED.USERLIST );
      } catch( JC_Exception e ) {
        reportError( e );
      }
    }//GEN-LAST:event_mAddMemberShipAsRoleActionPerformed

    private void mIsRoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mIsRoleActionPerformed
      final JC_GroupNode groupN = (JC_GroupNode)mGroupsList.getSelectedValue();
      try {
        groupN.setProperty( "jecars:IsRole", mIsRole.isSelected() );
        groupN.save();
      } catch( JC_Exception e ) {
        reportError( e );
      }
      return;
    }//GEN-LAST:event_mIsRoleActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField mAddGroupMemberName;
    private javax.swing.JButton mAddGroupMembership;
    private javax.swing.JButton mAddMemberShip;
    private javax.swing.JButton mAddMemberShipAsRole;
    private javax.swing.JButton mClearData;
    private javax.swing.JButton mClearGroupData;
    private javax.swing.JButton mCreateGroup;
    private javax.swing.JButton mCreateUser;
    private javax.swing.JCheckBox mDefaultMembership;
    private javax.swing.JTextField mEmail;
    private javax.swing.JTextField mFirstName;
    private javax.swing.JList mGroupMemberList;
    private javax.swing.JTextField mGroupNameID;
    private javax.swing.JTextField mGroupTitle;
    private javax.swing.JList mGroupsList;
    private javax.swing.JCheckBox mIsRole;
    private javax.swing.JTextField mLastName;
    private javax.swing.JButton mModifyUser;
    private javax.swing.JTextField mNameID;
    private javax.swing.JPasswordField mNewPassword;
    private javax.swing.JPanel mRemoveGroup;
    private javax.swing.JButton mRemoveMember;
    private javax.swing.JButton mRemoveMembership;
    private javax.swing.JButton mRemoveUser;
    private javax.swing.JButton mRemoveUser1;
    private javax.swing.JButton mSealUser;
    private javax.swing.JButton mSetNewPassword;
    private javax.swing.JLabel mSetPasswordMessage;
    private javax.swing.JButton mUnsealUser;
    private javax.swing.JCheckBox mUserMayAccess;
    private javax.swing.JList mUsersList;
    private javax.swing.JScrollPane mUsersScroll;
    // End of variables declaration//GEN-END:variables

}
