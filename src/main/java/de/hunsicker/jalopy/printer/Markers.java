/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.util.LinkedList;


/**
 * Manages a set of markers.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
final class Markers
{
    //~ Instance variables ---------------------------------------------------------------

    /** The current number of markers. */
    int count;

    /** Holds the markers. */
    private LinkedList _marks; // LinkedList of <Marker>

    /** The writer for which all markers are set. */
    private NodeWriter _writer;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new Markers object.
     *
     * @param writer writer for which all markers will be set.
     */
    public Markers(NodeWriter writer)
    {
        _writer = writer;
        _marks = new LinkedList();
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the last marker. Equal to {@link #getMark(int) getMark(count() - 1)}.
     *
     * @return the last marker set.
     */
    public Marker getLast()
    {
        return get(_marks.size() - 1);
    }


    /**
     * Indicates whether any markers are set.
     *
     * @return <code>true</code> if there is at least one marker.
     */
    public boolean isMarked()
    {
        return this.count > 0;
    }


    /**
     * Adds a new marker.
     *
     * @return the added marker.
     */
    public Marker add()
    {
        return add(_writer.line, _writer.column - 1);
    }


    /**
     * Adds a marker for the given position.
     *
     * @param line line of the marker
     * @param column column of the marker.
     *
     * @return the created marker.
     */
    public Marker add(
        int line,
        int column)
    {
     return add(line,column,false,null);
    }
    
    /**
     * Adds a marker for the given position.
     *
     * @param line line of the marker
     * @param column column of the marker.
     *
     * @return the created marker.
     */
    public Marker add(
        int line,
        int column,boolean hasIndent,NodeWriter out)
    {
        Marker m = new Marker(line, column,hasIndent);
        _marks.add(m);
        this.count++;
        if (hasIndent)
            out.indent();
        return m;
    }


    /**
     * Returns the number of markers set.
     *
     * @return number of markers set.
     */
    public int count()
    {
        return this.count;
    }


    /**
     * Returns the marker with the given index.
     *
     * @param index the index of the mark (&gt;=0).
     *
     * @return the marker with the given index.
     */
    public Marker get(int index)
    {
        return (Marker) _marks.get(index);
    }


    /**
     * Removes the given mark. Removes all marks that were set after the given mark, too.
     *
     * @param mark mark to remove.
     */
    public void remove(Marker mark)
    {
        int index = _marks.indexOf(mark);

        // remove the given marker and all markers that were set after this
        // marker
        for (int i = index, size = _marks.size(); i < size; i++)
        {
            _marks.removeLast();
            this.count--;
        }
    }
    
    /**
     * Removes the given mark. Removes all marks that were set after the given mark, too.
     *
     * @param mark mark to remove.
     */
    public void remove(Marker mark,NodeWriter out)
    {
        int index = _marks.indexOf(mark);
        Marker lastMark = null;

        // remove the given marker and all markers that were set after this
        // marker
        for (int i = index, size = _marks.size(); i < size; i++)
        {
            lastMark = (Marker)_marks.removeLast();
            if (lastMark.hasIndent){
                out.unindent();
            }
            this.count--;
        }
    }


    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object.
     */
    public String toString()
    {
        return _marks.toString();
    }
    public void reset() {
        if (this._marks!=null) {
            this._marks.clear();
        }
        this.count = 0;
    }
}
