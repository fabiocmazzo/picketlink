/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.test.idm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.SimpleGroup;

/**
 * <p>
 * Test case for {@link Group} basic management operations.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class GroupManagementTestCase extends AbstractIdentityTypeTestCase {

    /**
     * <p>
     * Creates a new {@link Group} instance using the API. This method also checks if the user was properly created by retrieving
     * his information from the store.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group newGroupInstance = new SimpleGroup("someGroup");

        // let's create the new role
        identityManager.createGroup(newGroupInstance);

        // let's retrieve the role information and see if they are properly stored
        Group storedGroupInstance = identityManager.getGroup(newGroupInstance.getName());

        assertNotNull(storedGroupInstance);
        
        assertEquals(newGroupInstance.getKey(), storedGroupInstance.getKey());
        assertEquals(newGroupInstance.getName(), storedGroupInstance.getName());
    }
    
    /**
     * <p>
     * Creates a new {@link Group} instance as a child of another {@link Group} using the API. This method also checks if the user was properly created by retrieving
     * his information from the store.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testCreateWithParentGroup() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group parentGroup = new SimpleGroup("parentGroup");
        
        identityManager.createGroup(parentGroup);
        
        Group childGroup = new SimpleGroup("childGroup", parentGroup);

        // let's create the new role
        identityManager.createGroup(childGroup);

        // let's retrieve the role information and see if they are properly stored
        Group storedChildGroup = identityManager.getGroup(childGroup.getName());

        assertNotNull(storedChildGroup);
        assertNotNull(storedChildGroup.getParentGroup());
        
        assertEquals(childGroup.getKey(), storedChildGroup.getKey());
        assertEquals(childGroup.getName(), storedChildGroup.getName());
        assertEquals(childGroup.getParentGroup().getName(), storedChildGroup.getParentGroup().getName());
    }

    /**
     * <p>Loads from the LDAP tree an already stored role.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testGet() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group storedGroupInstance = identityManager.getGroup("Test Group");

        assertNotNull(storedGroupInstance);
        assertNotNull(storedGroupInstance.getParentGroup());

        assertEquals("GROUP:///Test Parent Group/Test Group", storedGroupInstance.getKey());
        assertEquals("Test Group", storedGroupInstance.getName());
    }
    
    /**
     * <p>Remove from the LDAP tree an already stored role.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testRemove() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group storedGroupInstance = identityManager.getGroup("Test Group");

        assertNotNull(storedGroupInstance);
        
        identityManager.removeGroup(storedGroupInstance);
        
        Group removedGroupInstance = identityManager.getGroup("Test Group");
        
        assertNull(removedGroupInstance);
    }
    
}
