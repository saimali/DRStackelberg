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
import edu.stanford.multiagent.gamer.*;

/**
 * An Adjacency list implementation of the Graph interface.
 *
 */


public abstract class ALGraph extends Graph
{
    protected Vector nodes;

    /**
     * Constructor
     */
    public ALGraph()
	throws Exception
    {
	super();
	nodes=new Vector();
    }


    /**
     * Constructor which initializes the number of nodes.
     *
     * @param nNodes the number of nodes in the graph
     */
    public ALGraph(int nNodes)
	throws Exception
    {
	super();
	nodes = new Vector(nNodes);
	this.nNodes=nNodes;

	for(int i=0; i<nNodes; i++)
	    nodes.add(new LinkedList());
    }


    /**
     * Adds a new node to the graph.
     */
    public void addNode()
    {
	nNodes++;
	nodes.add(new LinkedList());
    }


    /**
     * Add a new edge to the graph.
     *
     * @param s the index of the node where the edge should start
     * @param t the index of the node where the edge should end
     */
    public void addEdge(int s, int t)
    {
      addEdge(s,t,null);
    }


    /**
     * Add an edge with data to the graph.
     *
     * @param s the index of the node where the edge should start
     * @param t the index of the node where the graph should end
     * @param data the data which is to be stored at the edge
     */
    public void addEdge(int s, int t, Object data) 
    {
      if(areNeighbours(s,t))
	return;

	LinkedList list = (LinkedList)nodes.get(s);
	Edge e=new Edge(s, t);
	e.setData(data);
	list.add(e);
	nEdges++;
    }


    /**
     * Sets the data item for a preexisting edge.
     *
     * @param s the index of the node where the edge begins
     * @param t the index of the node where the edge ends
     * @param data the data which is to be stored at the edge
     */
    public void setEdgeData(int s, int t, Object data)
    {
	LinkedList list = (LinkedList)nodes.get(s);
	Iterator it = list.iterator(); 
	while(it.hasNext())
	    {
		Edge e = (Edge)it.next();
		if(e.getDest() == t)
		    {
			it.remove();
			e.setData(data);
			list.add(e);
			return;
		    }
	    }
    }


    /**
     * Removes an edge from the graph.
     * 
     * @param s the index of the node where the edge begins
     * @param t the index of the node where the edge ends
     */
    public void removeEdge(int s, int t)
    {
	LinkedList list = (LinkedList)nodes.get(s);
	Iterator it=list.iterator(); 
	while(it.hasNext())
	    {
		Edge e = (Edge)it.next();
		if(e.getDest()==t)
		    {
			it.remove();
			nEdges--;
			return;
		    }
	    }
    }

    /** 
     * Removes an edge from the graph.
     *
     * @param e the edge to be removed
     */
    public void removeEdge(Edge e)
    {
	LinkedList list = (LinkedList)nodes.get(e.getSource());
	list.remove(e);
	nEdges--;
    }


    /**
     * Returns an Edge object from the graph.
     * 
     * @param s the index of the node where the edge begins
     * @param t the index of the node where the edge ends
     */
    public Edge getEdge(int s, int t)
    {
	LinkedList list = (LinkedList)nodes.get(s);
	Iterator it=list.iterator(); 
	while(it.hasNext())
	    {
		Edge e = (Edge)it.next();
		if(e.getDest()==t)
		    return e;
	    }

	return null;
    }


    /**
     * Checks if two nodes are neighbours in the graph.  Note that
     * direction matters in this check.
     * 
     * @param from the index of the node where the edge begins
     * @param to the index of the node where the edge ends
     */
    public boolean areNeighbours(int from, int to)
    {
	LinkedList list = (LinkedList)nodes.get(from);
	Iterator it=list.iterator(); 
	while(it.hasNext())
	    {
		Edge e = (Edge)it.next();
		if(e.getDest()==to)
		    return true;
	    }

	return false;
    }


    /**
     * Returns an iterator over the node's neighbours.
     *
     * @param from the index of the node whose neighbours should
     * be returned
     */
    public Iterator getNeighbours(int from)
    {
	LinkedList list = (LinkedList)nodes.get(from);
	final Iterator it = list.iterator();

	return new Iterator() {
		
		public void remove() { 
		    throw new UnsupportedOperationException("Graph Iterator");
		}
		
		public Object next() {
		    return new Integer(((Edge)it.next()).getDest());
		}

		public boolean hasNext() {
		    return it.hasNext();
		}
	    };
    }


    /**
     * Returns an iterator over the outgoing edges from a node.
     *
     * @param from the index of the node whose neighbours should
     * be returned
     */
    public Iterator getEdges(int from)
    {
	return ((LinkedList)nodes.get(from)).iterator();
    }


    /**
     * Return number of neighbours extending from a node.
     *
     * @param from the index of the node
     */
    public int getNumNeighbours(int from)
    {
	return ((LinkedList)nodes.get(from)).size();
    }


    /**
     * May be implemented by subclasses to check parameters 
     * if any exist.
     */
    protected void checkParameters() throws Exception
    {
	return;
    } 

}


