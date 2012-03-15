/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;

import de.hunsicker.jalopy.language.CompositeFactory;
import de.hunsicker.jalopy.language.antlr.JavaNodeFactory;


/**
 * NodeWriter can be used to &quot;test&quot; the output result for nodes. The class'
 * sole purpose is to determine the length of an AST tree (or portions thereof) if
 * printed.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
final class TestNodeWriter
    extends NodeWriter
{
    //~ Instance variables ---------------------------------------------------------------

    /** The length it would take to print the tree/node. */
    int length;
    int maxColumn;
    boolean hasIndent = false;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new TestNodeWriter object.
     */
    TestNodeWriter(WriterCache writer,CompositeFactory factory, NodeWriter source)
    {
        super(factory);
        this.mode = MODE_TEST;
        this.testers = writer;
        this.filename = source.filename;
        
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the length of the testet AST.
     *
     * @return the length it would take to output.
     */
    public int getLength()
    {
        return this.length;
    }


    /**
     * {@inheritDoc}
     */
    public void close()
      throws IOException
    {
        super.close();
        reset();
        if (this.state!=null)
            this.state.dispose();
            
        this.state = null;
        
    }


    /**
     * {@inheritDoc}
     */
    public void flush()
      throws IOException
    {
        super.flush();
        reset();
    }


    /**
     * {@inheritDoc}
     */
    public int print(
        String string,
        int    type)
      throws IOException
    {
        if (this.newline)
        {
            int l = this.indentLevel * this.indentSize;
            addColumn(l);
            this.length += l;
            this.newline = false;
        }
        int l = string.length();
        this.length += l;
        addColumn(l);
        this.last = type;

        return 1;
    }


    /**
     * {@inheritDoc}
     */
    public void printNewline()
      throws IOException
    {
        this.newline = true;
        this.column = 1;
        this.line++;
        
    }
    
    /**
     * Resets the stream. Call this method prior reusing the stream.
     */
    public void reset()
    {
        this.length = 0;
        this.line = 1;
        this.column = 1;
        this.maxColumn = 1;
        this.indentLevel=0;
        //this.indentSize=0;
        //this.indent();
        if (this.state!=null)
            this.state.reset();
        else 
            this.state=new PrinterState(this);
        hasIndent = false;
//        data=new StringBuffer();        
        
    }
    
	/**
	 * Resets the stream. Call this method prior reusing the stream.
	 * 
	 * @param out The output writer to synchronize with
	 * @param newline1 True if a new line should be emulated
	 */
    public void reset(NodeWriter out,boolean newline1)
    {
        this.reset();
        this.indentLevel = out.indentLevel;
        this.column=this.maxColumn=this.length=out.column;
        if (out.state.markers.isMarked()) {
            Marker m=out.state.markers.getLast();
            this.state.markers.add(m.line,m.column);
        }
        if (newline1){
            try {
                this.printNewline();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void addColumn(int amount) {
        this.column +=amount;
        if (this.column>this.maxColumn)
            this.maxColumn = this.column;
    }
    public void indent() {
        hasIndent=true;
        super.indent();
    }

    public void unindent() {
    }

}
