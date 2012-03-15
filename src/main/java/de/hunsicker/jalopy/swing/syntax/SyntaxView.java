/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */

// Copyright (c) 2000 BlueJ Group, Monash University
//
// This software is made available under the terms of the "MIT License"
// A copy of this license is included with this source distribution
// in "license.txt" and is also available at:
// http://www.opensource.org/licenses/mit-license.html
// Any queries should be directed to Michael Kolling: mik@mip.sdu.dk
package de.hunsicker.jalopy.swing.syntax;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

/**
 * SyntaxView.java - adapted from SyntaxView.java - jEdit's own Swing view implementation
 * to add Syntax highlighting to the BlueJ programming environment.
 */
import javax.swing.text.PlainView;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;


/**
 * A Swing view implementation that colorizes lines of a SyntaxDocument using a
 * TokenMarker. This class should not be used directly; a SyntaxEditorKit should be used
 * instead.
 *
 * @author Slava Pestov
 * @author Bruce Quig
 * @author Michael Kolling
 * @version $Id: SyntaxView.java,v 1.4 2006/01/13 20:27:25 notzippy Exp $
 */
public final class SyntaxView
    extends PlainView
{
    //~ Static variables/initializers ----------------------------------------------------

    static final short TAG_WIDTH = 10;
    static final int BREAKPOINT_OFFSET = TAG_WIDTH + 2;

    //~ Instance variables ---------------------------------------------------------------

    FontMetrics lineNumberMetrics;
    private Font defaultFont;

    // protected FontMetrics metrics;  is inherited from PlainView
    private Font lineNumberFont;
    private Font smallLineNumberFont;

    // private members
    private Segment line;
    private boolean initialised = false;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new <code>SyntaxView</code> for painting the specified element.
     *
     * @param elem The element
     */
    public SyntaxView(Element elem)
    {
        super(elem);
        line = new Segment();
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Paints the specified line. This method performs the following: - Gets the token
     * marker and color table from the current document, typecast to a SyntaxDocument. -
     * Tokenizes the required line by calling the markTokens() method of the token
     * marker. - Paints each token, obtaining the color by looking up the the Token.id
     * value in the color table. If either the document doesn't implement
     * SyntaxDocument, or if the returned token marker is null, the line will be painted
     * with no colorization. Currently, we assume that the whole document uses the same
     * font. To support font changes, some of the code from "initilise" needs to be here
     * to be done repeatedly for each line.
     *
     * @param lineIndex The line number
     * @param g The graphics context
     * @param x The x co-ordinate where the line should be painted
     * @param y The y co-ordinate where the line should be painted
     */
    public void drawLine(
        int      lineIndex,
        Graphics g,
        int      x,
        int      y)
    {
        if (!initialised)
        {
            initialise(g);
        }

        SyntaxDocument document = (SyntaxDocument) getDocument();
        TokenMarker tokenMarker = document.getTokenMarker();

        Color def = getDefaultColor();

        try
        {
            Element lineElement = getElement().getElement(lineIndex);
            int start = lineElement.getStartOffset();
            int end = lineElement.getEndOffset();

            document.getText(start, end - (start + 1), line);

            g.setColor(def);

            drawLineNumber(g, lineIndex + 1, x, y);
            
            // if no tokenMarker just paint as plain text
            if (tokenMarker == null)
            {
                Utilities.drawTabbedText(line, x + BREAKPOINT_OFFSET, y, g, this, 0);
            }
            else
            {
                paintSyntaxLine(
                    line, lineIndex, x + BREAKPOINT_OFFSET, y, g, document, tokenMarker,
                    def);
            }
        }
        catch (BadLocationException bl)
        {
            // shouldn't happen
            bl.printStackTrace();
        }
    }


    /**
     * redefined from PlainView private method to allow for redefinition of modelToView
     * method
     *
     * @param a DOCUMENT ME!
     * @param aline DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Rectangle lineToRect(
        Shape a,
        int   aline)
    {
        Rectangle r = null;

        if (metrics != null)
        {
            Rectangle alloc = a.getBounds();
            r = new Rectangle(
                    alloc.x, alloc.y + (aline * metrics.getHeight()), alloc.width,
                    metrics.getHeight());
        }

        return r;
    }


    /**
     * Provides a mapping from the document model coordinate space to the coordinate
     * space of the view mapped to it.  This is a redefined method from PlainView that
     * adds an offset for the view to allow for a breakpoint area in the associated
     * editor.
     *
     * @param pos the position to convert >= 0
     * @param a the allocated region to render into
     * @param b DOCUMENT ME!
     *
     * @return the bounding box of the given position
     *
     * @exception BadLocationException if the given position does not represent a valid
     *            location in the associated document
     *
     * @see View#modelToView
     */
    public Shape modelToView(
        int           pos,
        Shape         a,
        Position.Bias b)
      throws BadLocationException
    {
        // line coordinates
        Document doc = getDocument();
        Element map = getElement();
        int lineIndex = map.getElementIndex(pos);
        Rectangle lineArea = lineToRect(a, lineIndex);

        // determine span from the start of the line
        int tabBase = lineArea.x + TAG_WIDTH + 2;

        Element eLine = map.getElement(lineIndex);
        int p0 = eLine.getStartOffset();
        Segment buffer = getLineBuffer();
        doc.getText(p0, pos - p0, buffer);

        int xOffs = Utilities.getTabbedTextWidth(buffer, metrics, tabBase, this, p0);

        // fill in the results and return, include breakpoint area offset
        lineArea.x += (xOffs + (TAG_WIDTH + 2));
        lineArea.width = 1;
        lineArea.height = metrics.getHeight();

        return lineArea;
    }


    // --- TabExpander interface methods -----------------------------------

    /**
     * Returns the next tab stop position after a given reference position. This
     * implementation does not support things like centering so it ignores the tabOffset
     * argument.
     *
     * @param x the current position >= 0
     * @param tabOffset the position within the text stream that the tab occurred at >=
     *        0.
     *
     * @return the tab stop, measured in points >= 0
     */
    public float nextTabStop(
        float x,
        int   tabOffset)
    {
        // calculate tabsize using fontwidth and tab spaces
        int tabSize = getTabSize() * metrics.charWidth('m');

        if (tabSize == 0)
        {
            return x;
        }

        int tabStopNumber = (int) ((x - BREAKPOINT_OFFSET) / tabSize) + 1;

        return (tabStopNumber * tabSize) + BREAKPOINT_OFFSET + 2;
    }


    /*
    * redefined paint method to paint breakpoint area
    *
    * @param g DOCUMENT ME!
    * @param allocation DOCUMENT ME!
    *
    public void paint(
    Graphics g,
    Shape    allocation)
    {
    // paint the lines
    super.paint(g, allocation);

    // paint the tag separator line
    g.setColor(Color.gray);

    Rectangle bounds = allocation.getBounds();

    g.drawLine(
        bounds.x + TAG_WIDTH, 0, bounds.x + TAG_WIDTH,
        bounds.y + bounds.height + 1);
    }*/

    /**
     * Provides a mapping from the view coordinate space to the logical coordinate space
     * of the model.
     *
     * @param fx the X coordinate >= 0
     * @param fy the Y coordinate >= 0
     * @param a the allocated region to render into
     * @param bias DOCUMENT ME!
     *
     * @return the location within the model that best represents the given point in the
     *         view >= 0
     *
     * @see View#viewToModel
     */
    public int viewToModel(
        float           fx,
        float           fy,
        Shape           a,
        Position.Bias[] bias)
    {
        // PENDING(prinz) properly calculate bias
        bias[0] = Position.Bias.Forward;

        Rectangle alloc = a.getBounds();
        Document doc = getDocument();
        int x = (int) fx;
        int y = (int) fy;

        if (y < alloc.y)
        {
            // above the area covered by this icon, so the the position
            // is assumed to be the start of the coverage for this view.
            return getStartOffset();
        }
        else if (y > (alloc.y + alloc.height))
        {
            // below the area covered by this icon, so the the position
            // is assumed to be the end of the coverage for this view.
            return getEndOffset() - 1;
        }
        else
        {
            // positioned within the coverage of this view vertically,
            // so we figure out which line the point corresponds to.
            // if the line is greater than the number of lines contained, then
            // simply use the last line as it represents the last possible place
            // we can position to.
            Element map = doc.getDefaultRootElement();
            int lineIndex = Math.abs((y - alloc.y) / metrics.getHeight());

            if (lineIndex >= map.getElementCount())
            {
                return getEndOffset() - 1;
            }

            Element eLine = map.getElement(lineIndex);

            if (x < alloc.x)
            {
                // point is to the left of the line
                return eLine.getStartOffset();
            }
            else if (x > (alloc.x + alloc.width))
            {
                // point is to the right of the line
                return eLine.getEndOffset() - 1;
            }
            else
            {
                // Determine the offset into the text
                try
                {
                    Segment buffer = getLineBuffer();
                    int p0 = eLine.getStartOffset();
                    int p1 = eLine.getEndOffset() - 1;
                    doc.getText(p0, p1 - p0, buffer);

                    // add Moe breakpoint offset area width
                    int tabBase = alloc.x + TAG_WIDTH + 2;
                    int offs =
                        p0
                        + Utilities.getTabbedTextOffset(
                            buffer, metrics, tabBase, x, this, p0);

                    return offs;
                }
                catch (BadLocationException e)
                {
                    // should not happen
                    return -1;
                }
            }
        }
    }


    /**
     * Return default foreground colour
     *
     * @return DOCUMENT ME!
     */
    protected Color getDefaultColor()
    {
        return getContainer().getForeground();
    }


    /**
     * Draw the line number in front of the line
     *
     * @param g The graphics object
     * @param lineNumber the line number
     * @param x The x coordinate
     * @param y The Y coordinate
     */
    private void drawLineNumber(
        Graphics g,
        int      lineNumber,
        int      x,
        int      y)
    {
        String number = Integer.toString(lineNumber);
        int stringWidth = lineNumberMetrics.stringWidth(number);
        int xoffset = BREAKPOINT_OFFSET - stringWidth - 4;

        if (xoffset < -2) // if it doesn't fit, shift one pixel over.
        {
            xoffset++;
        }

        if (xoffset < -2)
        { // if it still doesn't fit...
            g.setFont(smallLineNumberFont);
            g.drawString(number, x - 3, y);
        }
        else
        {
            g.setFont(lineNumberFont);
            g.drawString(number, x + xoffset, y);
        }

        g.setFont(defaultFont);
    }


    /**
     * Initialise some fields after we get a graphics context for the first time
     *
     * @param g DOCUMENT ME!
     */
    private void initialise(Graphics g)
    {
        defaultFont = g.getFont();
        lineNumberFont = defaultFont.deriveFont(9.0f);
        smallLineNumberFont = defaultFont.deriveFont(7.0f);

        Component c = getContainer();
        lineNumberMetrics = c.getFontMetrics(lineNumberFont);
        initialised = true;
    }


    /**
     * paints a line with syntax highlighting, redefined from DefaultSyntaxDocument.
     *
     * @param line DOCUMENT ME!
     * @param lineIndex DOCUMENT ME!
     * @param x DOCUMENT ME!
     * @param y DOCUMENT ME!
     * @param g DOCUMENT ME!
     * @param document DOCUMENT ME!
     * @param tokenMarker DOCUMENT ME!
     * @param def DOCUMENT ME!
     */
    private void paintSyntaxLine(
        Segment        line,
        int            lineIndex,
        int            x,
        int            y,
        Graphics       g,
        SyntaxDocument document,
        TokenMarker    tokenMarker,
        Color          def)
    {
        Color[] colors = document.getColors();
        Token tokens = tokenMarker.markTokens(line, lineIndex);
        int offset = 0;

        for (;;)
        {
            byte id = tokens.id;

            if (id == Token.END)
            {
                break;
            }

            int length = tokens.length;
            Color color;

            if (id == Token.NULL)
            {
                color = def;
            }
            else
            {
                color = colors[id];
            }

            g.setColor((color == null) ? def
                                       : color);

            line.count = length;
            x = Utilities.drawTabbedText(line, x, y, g, this, offset);
            line.offset += length;
            offset += length;

            tokens = tokens.next;
        }
    }
}
