/**
* Copyright (c) Microsoft Corporation
* 
* All rights reserved. 
* 
* MIT License
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
* (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
* publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
* subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
* ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
* THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.microsoft.azure.shortcuts.resources.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.azure.shortcuts.common.implementation.EntitiesImpl;
import com.microsoft.azure.shortcuts.common.implementation.NamedRefreshableImpl;
import com.microsoft.azure.shortcuts.resources.creation.GroupDefinitionBlank;
import com.microsoft.azure.shortcuts.resources.creation.GroupDefinitionProvisionable;
import com.microsoft.azure.shortcuts.resources.listing.Groups;
import com.microsoft.azure.shortcuts.resources.reading.Group;
import com.microsoft.azure.shortcuts.resources.updating.GroupUpdatable;
import com.microsoft.azure.shortcuts.resources.updating.GroupUpdatableBlank;


public class GroupsImpl 
	extends EntitiesImpl<Azure>
	implements Groups {
	
	List<Group> groups = null;
	
	GroupsImpl(Azure azure) {
		super(azure);
	}
	
	
	@Override
	public Map<String, Group> list() throws Exception {
		HashMap<String, Group> wrappers = new HashMap<>();
		for(ResourceGroupExtended nativeItem : getGroups(azure)) {
			GroupImpl wrapper = new GroupImpl(nativeItem);
			wrappers.put(nativeItem.getName(), wrapper);
		}
		
		return Collections.unmodifiableMap(wrappers);
	}

		
	@Override
	// Gets a specific resource group
	public Group get(String name) throws Exception {
		ResourceGroupExtended azureGroup = azure.resourceManagementClient().getResourceGroupsOperations().get(name).getResourceGroup();
		return new GroupImpl(azureGroup);
	}
	
	
	@Override
	public void delete(String name) throws Exception {
		azure.resourceManagementClient().getResourceGroupsOperations().delete(name);
		//TODO: Apparently the effect of the deletion is not immediate - Azure SDK misleadingly returns from this synch call even though listing resource groups will still include this
	}
	

	@Override
	public GroupUpdatableBlank update(String name) {
		return new GroupImpl(createAzureGroup(name));
	}


	@Override
	public GroupDefinitionBlank define(String name) {
		return new GroupImpl(createAzureGroup(name));
	}

	
	// Creates new Azure group object
	private static ResourceGroupExtended createAzureGroup(String name) {
		ResourceGroupExtended azureGroup = new ResourceGroupExtended();
		azureGroup.setName(name);
		return azureGroup;
	}

	
	// Helper to get the resource groups from Azure
	private static ArrayList<ResourceGroupExtended> getGroups(Azure azure) throws Exception {
		return azure.resourceManagementClient().getResourceGroupsOperations().list(null).getResourceGroups();		
	}
	
	
	/***************************************************************
	 * Implements logic for individual resource group
	 ***************************************************************/
	private class GroupImpl 
		extends 
			NamedRefreshableImpl<Group>
		implements
			GroupUpdatable,
			GroupDefinitionProvisionable,
			GroupDefinitionBlank,
			Group {
		
		private ResourceGroupExtended azureGroup;
		
		private GroupImpl(ResourceGroupExtended azureGroup) {
			super(azureGroup.getName(), true);
			this.azureGroup = azureGroup;
		}


		/***********************************************************
		 * Getters
		 ***********************************************************/
		
		@Override
		public String region() throws Exception {
			return this.azureGroup.getLocation();
		}

		@Override
		public String id() throws Exception {
			return this.azureGroup.getId();
		}

		@Override
		public Map<String, String> tags() throws Exception {
			return Collections.unmodifiableMap(this.azureGroup.getTags());
		}

		@Override
		public String provisioningState() throws Exception {
			return this.azureGroup.getProvisioningState();
		}

		
		/**************************************************************
		 * Setters (fluent interface)
		 **************************************************************/
		
		@Override
		public GroupImpl withTags(HashMap<String, String> tags) {
			this.azureGroup.setTags(tags);
			return this;
		}

		@Override
		public GroupImpl withTag(String key, String value) {
			if(this.azureGroup.getTags() == null) {
				this.azureGroup.setTags(new HashMap<String, String>());
			}
			this.azureGroup.getTags().put(key, value);
			return this;
		}

		@Override
		public GroupImpl withoutTag(String key) {
			this.azureGroup.getTags().remove(key);
			return this;
		}

		@Override
		public GroupImpl withRegion(String region) {
			this.azureGroup.setLocation(region);
			return this;
		}


		/************************************************************
		 * Verbs
		 ************************************************************/
		
		@Override
		public GroupImpl apply() throws Exception {
			ResourceGroup params = new ResourceGroup();
			Group group;
			
			params.setTags(this.azureGroup.getTags());
			
			// Figure out the region, since the SDK requires on the params explicitly even though it cannot be changed
			if(this.azureGroup.getLocation() != null) {
				params.setLocation(this.azureGroup.getLocation());
			} else if(null == (group = azure.groups().get(this.name))) {
				throw new Exception("Resource group not found");
			} else {
				params.setLocation(group.region());
			}

			azure.resourceManagementClient().getResourceGroupsOperations().createOrUpdate(this.name, params);
			return this;
		}

		
		@Override
		public void delete() throws Exception {
			azure.groups().delete(this.name);
		}

		
		@Override
		public GroupImpl provision() throws Exception {
			ResourceGroup params = new ResourceGroup();
			params.setLocation(this.azureGroup.getLocation());
			params.setTags(this.azureGroup.getTags());
			azure.resourceManagementClient().getResourceGroupsOperations().createOrUpdate(this.name, params);
			return this;
		}

		
		@Override
		public GroupImpl refresh() throws Exception {
			this.azureGroup =  azure.resourceManagementClient().getResourceGroupsOperations().get(this.name).getResourceGroup();
			this.initialized = true;
			return this;
		}
	}
}
