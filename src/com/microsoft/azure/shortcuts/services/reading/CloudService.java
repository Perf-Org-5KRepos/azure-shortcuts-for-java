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
package com.microsoft.azure.shortcuts.services.reading;

import java.util.Calendar;

import com.microsoft.azure.shortcuts.common.reading.Named;
import com.microsoft.azure.shortcuts.common.reading.Refreshable;
import com.microsoft.azure.shortcuts.common.reading.Wrapper;
import com.microsoft.azure.shortcuts.services.updating.CloudServiceUpdatableBlank;
import com.microsoft.windowsazure.management.compute.models.HostedServiceListResponse.HostedService;

// Encapsulates the readable properties of a cloud service
public interface CloudService extends 
	Named,
	Refreshable<CloudService>,
	Wrapper<HostedService>,
	CloudServiceUpdatableBlank {
	
	String region() throws Exception;
	String description() throws Exception;
	String label() throws Exception;
	String reverseDnsFqdn() throws Exception;
	Calendar created() throws Exception;
	Calendar modified() throws Exception;
	String affinityGroup() throws Exception;
}
