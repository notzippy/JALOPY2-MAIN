/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.util.ArrayList;
import java.util.List;

import de.hunsicker.jalopy.language.CompositeFactory;
import de.hunsicker.jalopy.language.antlr.JavaNodeFactory;


/**
 * A simple cache to avoid continually creating and destroying new TestNodeWriter
 * objects.
 *
 * @since 1.0b9
 */
final class WriterCache
{
    //~ Instance variables ---------------------------------------------------------------

    /** The cached writers. */
    private final List _writers = new ArrayList();
    private final String _originalLineSeparator;

    //~ Constructors ---------------------------------------------------------------------
    NodeWriter nodeWriter = null;
    CompositeFactory _factory = null;
    /**
     * Creates a new WriterCache object.
     *
     * @param writer DOCUMENT ME!
     */
    public WriterCache(CompositeFactory factory, NodeWriter writer)
    {
        _factory = factory;
        _originalLineSeparator = writer.originalLineSeparator;
        nodeWriter = writer;
        TestNodeWriter tester = new TestNodeWriter(this,factory,nodeWriter);
        tester.originalLineSeparator = _originalLineSeparator;
        _writers.add(tester);
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns a TestNodeWriter object. If the cache is not empty, an element from the
     * cache will be returned; otherwise a new object will be created.
     *
     * @return a TestNodeWriter object.
     */
    public TestNodeWriter get()
    {
        synchronized (this)
        {
            if (_writers.size() > 0)
            {
                return (TestNodeWriter) _writers.remove(0);
            }
        }

        TestNodeWriter tester = new TestNodeWriter(this,_factory,nodeWriter);
        tester.originalLineSeparator = _originalLineSeparator;

        return tester;
    }


    /**
     * Releases the given writer and adds it to the cache.
     *
     * @param writer the writer object that should be cached.
     */
    public synchronized void release(TestNodeWriter writer)
    {
        writer.reset();
        _writers.add(writer);
    }
}
