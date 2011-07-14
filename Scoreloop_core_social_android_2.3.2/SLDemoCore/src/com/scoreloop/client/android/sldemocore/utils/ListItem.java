/*
 * In derogation of the Scoreloop SDK - License Agreement concluded between
 * Licensor and Licensee, as defined therein, the following conditions shall
 * apply for the source code contained below, whereas apart from that the
 * Scoreloop SDK - License Agreement shall remain unaffected.
 * 
 * Copyright: Scoreloop AG, Germany (Licensor)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.scoreloop.client.android.sldemocore.utils;

import android.app.Activity;

import com.scoreloop.client.android.core.model.User;

public class ListItem { // dumb wrapper

	private Class<? extends Activity> clazz;
	private String label;
	private User user;

	public ListItem(final String label) {
		this.label = label;
	}

	public ListItem(final String label, final Class<? extends Activity> clazz) {
		this.label = label;
		this.clazz = clazz;
	}

	public ListItem(final User user) {
		this.user = user;
	}

	public Class<? extends Activity> getClazz() {
		return clazz;
	}

	public User getUser() {
		return user;
	}

	public boolean isSpecialItem() {
		return (user == null);
	}

	@Override
	public String toString() {
		if (user != null) {
			return user.getLogin();
		}
		return label;
	}
}
