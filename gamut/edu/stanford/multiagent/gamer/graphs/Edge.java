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

package edu.stanford.multiagent.gamer.graphs;

/**
 * Implements an Edge data structure for graphs.  Any
 * Object can be stored in the edge
 */
 
public class Edge
{
    private int to;
    private int from;
    private Object data;
    
    // ---------------------------------------

    
    public Edge(int from, int to)
    {
	this.from=from;
	this.to=to;
	data=null;
    }

    public int getSource() { 
	return from;
    }

    public int getDest() { 
	return to; 
    }

    public Object getData() { 
	return data; 
    }
    
    public void setData(Object d) { 
	data=d; 
    }
}

