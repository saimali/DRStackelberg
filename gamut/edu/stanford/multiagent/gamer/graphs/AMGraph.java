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

import java.util.*;

/**
 * An Adjacency matrix implementation of the Graph interface
 *
 * Not fully implemented.
 */

public abstract class AMGraph extends Graph
{
    private Vector nodes;

    /**
     * Constructor
     */
    public AMGraph()
	throws Exception
    {
      throw new UnsupportedOperationException("AMGraph is not yet functional. Implement it, or use ALGraph instead.");
      //super();
      //nodes=new Vector();
      
    }

    /**
     * Constructor, initializes the number of nodes
     */
    public AMGraph(int nNodes)
	throws Exception
    {
      throw new UnsupportedOperationException("AMGraph is not yet functional. Implement it, or use ALGraph instead.");
      /*
      super();
      nodes = new Vector(nNodes);
      this.nNodes=nNodes;
      
      for(int i=0; i<nNodes; i++)
      nodes.add(new BitSet(nNodes));
      */
    }

    /**
     * Adds a node.
     */
    public void addNode()
    {
	nNodes++;
	nodes.add(new BitSet(nNodes));
    }

    /**
     * Adds an edge.
     */
    public void addEdge(int s, int t)
    {
	BitSet b = (BitSet)nodes.get(s);
	b.set(t);
	nEdges++;
    }


    /**
     * Add an edge with data
     *
     * Currently not implemented, does the same thing as the other
     * version of addEdge.
     */
    public void addEdge(int s, int t, Object data)
    {
	BitSet b = (BitSet) nodes.get(s);
	b.set(t);
	nEdges--;

	// --- TODO: set the edge data
    }
    

    /**
     * Sets the data item for the edge between s and t
     * if this edge already exists
     */
    public void setEdgeData(int s, int t, Object data)
    {
	// --- TODO: fill in this function
    }



    /**
     * Removes an edge
     */
    public void removeEdge(int s, int t)
    {
	BitSet b = (BitSet)nodes.get(s);
	b.clear(t);
	nEdges--;
    }

    public void removeEdge(Edge e)
    {
	removeEdge(e.getSource(), e.getDest());
    }

    /**
     * Gets an Edge
     */
    public Edge getEdge(int s, int t)
    {
	throw new UnsupportedOperationException("Edges not supported in matrix graph");
    }


    /**
     * Checks if two nodes are neighbours.  This check is directional.
     */
    public boolean areNeighbours(int from, int to)
    {
	BitSet b = (BitSet)nodes.get(from);
	return b.get(to);
    }

    /**
     * Returns an iterator over the node's neighbours
     */
    public Iterator getNeighbours(int from)
    {
	final BitSet b = (BitSet)nodes.get(from);

	return new Iterator() {
		
		private int currentBit=b.nextSetBit(0);

		public void remove() { 
		    throw new UnsupportedOperationException("Graph Iterator");
		}
		
		public Object next() {
		    if(currentBit==-1)
			throw new NoSuchElementException("Graph Iterator");
		    int oldBit=currentBit;
		    currentBit = b.nextSetBit(currentBit+1);
		    return new Integer(oldBit);
		}

		public boolean hasNext() {
		    return (currentBit!=-1);
		}
	    };
    }

    /**
     * Return an iterator over the outgoing edges
     */
    public Iterator getEdges(int from)
    {
	throw new UnsupportedOperationException("Edges not supported in matrix graph");
    }

}


