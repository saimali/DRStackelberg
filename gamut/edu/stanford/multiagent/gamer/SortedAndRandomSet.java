/*
 * Copyright (C) 2004 Jennifer Wortman, Eugene Nudelman, Kevin Leyton-Brown, Yoav Shoham.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.stanford.multiagent.gamer;

import java.util.*;


/**
 * Utility class holding a list of objects that can be returned 
 * either by size (largest or smallest) or randomly from the 
 * remaining elements in the list
 */


public class SortedAndRandomSet extends TreeSet {

    public SortedAndRandomSet() {
	super();
    }

    /**
     * Removes the smallest element in the list
     */
    public Object removeSmallest() {
	Object smallest = first();
	remove(smallest);
	return (smallest);
    }

    /**
     * Removes the largest element in the list.
     */
    public Object removeLargest() {
	Object largest = last();
	remove(largest);
	return(largest);
    }

    /**
     * Removes a random element from the list.
     */
    public Object removeRandom() {
	Object randomObject = first();
	int position = Global.rand.nextInt(size());
	Iterator iter = iterator();

	for (int i = 0; i <= position; i++)
	    randomObject = iter.next();

	iter.remove();

	return (randomObject);
    }

}


