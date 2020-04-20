//
// typica - A client library for Amazon Web Services
// Copyright (C) 2007 Xerox Corporation
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.xerox.amazonws.ec2;

import java.util.ArrayList;
import java.util.List;

/**
 * This enumeration represents different instance types that can be launched.
 */
public class InstanceType {
	private static List<InstanceType> defTypes = new ArrayList<InstanceType>();
	
	static{
		defTypes.add(new InstanceType("m1.small"));
		defTypes.add(new InstanceType("m1.large"));
		defTypes.add(new InstanceType("m1.xlarge"));
		defTypes.add(new InstanceType("c1.medium"));
		defTypes.add(new InstanceType("c1.xlarge"));
		defTypes.add(new InstanceType("m2.xlarge"));
		defTypes.add(new InstanceType("m2.2xlarge"));
		defTypes.add(new InstanceType("m2.4xlarge"));
		defTypes.add(new InstanceType("cc1.4xlarge"));
	}
	
	private final String typeId;

	InstanceType(String typeId) {
		this.typeId = typeId;
	}

	public String getTypeId() {
		return typeId;
	}
	
	public static InstanceType getDefault(){
		return defTypes.get(0);
	}

	public static InstanceType getTypeFromString(String val) {
		for (InstanceType t : defTypes) {
			if (t.getTypeId().equals(val)) {
				return t;
			}
		}
		// special handling for OpenStack etc..
		return new InstanceType(val);
	}
}
