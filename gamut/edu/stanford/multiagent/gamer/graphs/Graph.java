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
 * An abstract base class for graph implementations.  Note that 
 * currently nodes are assumed to be consecutive, starting at 0,
 * and that this class must be updated, if node removal is desired.
 */

public abstract class Graph extends ParameterizedObject
{
    /*
     *Private fields
     */
    protected int nEdges;
    protected int nNodes;
    protected HashMap nodeData;


    // -----------------------------------------------------------


    /**
     * Constructor.  Currently only one version is available and it
     * does not  take parameters.
     */
    protected Graph()
	throws Exception
    {
	super();

	nEdges=nNodes=0;
	nodeData = new HashMap();
    }


    /**
     * Initializes the graph structure using preset parameter values.
     */
    public void initialize()
	throws Exception
    {
	super.initialize();
    }


    /**
     * Returns the help string with information about the graph class
     * and the parameters taken by the class.
     */
    public String getHelp()
    {
	StringBuffer buff=new StringBuffer();
	buff.append(Global.wrap(getGraphHelp(), 70));

	buff.append("\n\nGraph Parameters:\n");
	buff.append(Global.wrap(parameters.getPrintableInfo(), 70));


	return buff.toString();
    }


    /**
     * Returns a help information string about the particular
     * graph, does not include parameter information.
     */
    protected  abstract String getGraphHelp();


    /**
     * Returns the number of edges in the graph.
     */
    public int getNEdges()
    {
	return nEdges;
    }


    /**
     * Returns the number of vertices in the graph.
     */
    public int getNNodes()
    {
	return nNodes;
    }


    /**
     * Returns any data that is being stored at a node.
     *
     * @param n the index of the node
     */
    public Object getNodeData(int n)
    {
	return nodeData.get(new Integer(n));
    }

    
    /**
     * Sets node data.
     *
     * @param n the index of the node
     * @param data the data to be stored at the node
     */
    public void setNodeData(int n, Object data)
    {
	nodeData.put(new Integer(n), data);
    }


    /**
     * Adds a new node to the graph.
     */
    public abstract void addNode();


    /**
     * Adds a new edge to the graph between nodes s and t.
     *
     * @param s index of first node
     * @param t index of second node
     */
    public abstract void addEdge(int s, int t);


    /**
     * Adds a new edge to the graph between nodes s and t and
     * sets the edge data for this edge.
     *
     * @param s index of first node
     * @param t index of second node
     * @param data data to be stored on this edge
     */
    public abstract void addEdge(int s, int t, Object data);


    /**
     * Sets the edge data for an already existing edge.
     *
     * @param s index of the first node on the edge
     * @param t index of the second node on the egde
     * @param data data to be stored on the edge
     */
    public abstract void setEdgeData(int s, int t, Object data);


    /**
     * Removes an edge from the graph.
     *
     * @param s index of the first node on the edge
     * @param t index of the second node on the edge
     */
    public abstract void removeEdge(int s, int t);


    /**
     * Removes an edge from the graph.
     *
     * @param e the edge to be removed
     */
    public abstract void removeEdge(Edge e);


    /**
     * Gets an Edge.
     *
     * @param s index of the first node on the edge
     * @param t index of the second node on the edge
     */
    public abstract Edge getEdge(int s, int t);


    /**
     * Checks if two nodes are neighbours, i.e. if there exists an
     * edge from the first node to the second.  Note that the
     * order of nodes specified does matter for directed graphs.
     *
     * @param from index of the node at which the edge begins
     * @param to index of the node at which the egde ends
     */
    public abstract boolean areNeighbours(int from, int to);


    /**
     *Returns an iterator over a node's neighbours.
     *
     * @param from index of the node
     */
    public abstract Iterator getNeighbours(int from);


    /**
     *Returns an iterator over the outgoing edges from a node.
     *
     * @param from index of the node
     */
    public abstract Iterator getEdges(int from);


    /**
     * Returns true if the graph is symmetric, i.e. if it must be 
     * the case that for every edge from node a to node b there 
     * also exists an edge from node b to node a.
     */
    public abstract boolean hasSymEdges();


    /**
     * Returns true if reflexive edges are allowed in the graph.
     */
    public abstract boolean reflexEdgesOk();



    //-------------------------------------------------------
    // -- Static stuff: Parameters which are used in many
    // subclasses of graph
    
    protected static Parameters.ParamInfo pSymEdges;
    protected static Parameters.ParamInfo pReflexEdges;
    protected static Parameters.ParamInfo pNumNodes;

    static {

	pSymEdges = new Parameters.ParamInfo("sym_edges", Parameters.ParamInfo.BOOLEAN_PARAM, new Boolean(false), new Boolean(true), "Set this to true if it should be the case that whenever there is an edge from node a to node b, there is also an edge from node b to node a.", false, Boolean.TRUE);

	pReflexEdges = new Parameters.ParamInfo("reflex_ok", Parameters.ParamInfo.BOOLEAN_PARAM, new Boolean(false), new Boolean(true), "Set this to true if reflexive edges are allowed.", false, Boolean.FALSE);

	pNumNodes = new Parameters.ParamInfo("nodes", Parameters.ParamInfo.LONG_PARAM, new Long(2), new Long(100000), "Number of nodes in the random graph.  May be set very large by hand, but when randomized will not be set to anything over 20 since very large graphs do not work well in some games.  Occasionally this parameter must be set to something even smaller by hand.");

    }


}
