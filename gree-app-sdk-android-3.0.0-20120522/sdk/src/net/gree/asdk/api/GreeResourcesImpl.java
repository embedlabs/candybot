/*
 * Copyright 2012 GREE, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.gree.asdk.api;

/**
 * This is an interface to implement other method for loading resource identifier.
 * @author GREE, Inc.
 * @since 3.0.0
 */
public interface GreeResourcesImpl {

  /**
   * Return a resource identifier for the given resource name.
   * @return The associated resource identifier
   */
	public int getIdentifier(String name, String type);

}
