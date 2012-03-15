/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.plugin;

import java.util.List;


/**
 * Represents an editor view used to display and interactively modify the contents of a
 * Java source file.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.6 $
 */
public interface Editor
{
    //~ Methods --------------------------------------------------------------------------

    /**
     * Moves the caret to the given location.
     *
     * @param offset the text offset where the caret should be placed (absolute character
     *        position).
     *
     * @throws IllegalArgumentException if <code><em>offset</em> &lt; 0 ||
     *         <em>offset</em> &gt; {@link #getLength}</code>
     */
    public void setCaretPosition(int offset);


    /**
     * Moves the caret to the given location.
     *
     * @param line line number.
     * @param column column offset in the given line.
     *
     * @throws IllegalArgumentException if <code><em>line</em> &lt; 1 || <em>column</em>
     *         &lt; 1</code>
     */
    public void setCaretPosition(
        int line,
        int column);


    /**
     * Returns the current location of the caret.
     *
     * @return current caret position (absolute character position, <code>&gt;=
     *         0</code>).
     */
    public int getCaretPosition();


    /**
     * Returns the column offset in the current line.
     *
     * @return current column offset (<code>&gt;= 1</code>).
     */
    public int getColumn();


    /**
     * Returns the file of the editor.
     *
     * @return the file that has its contents displayed in the editor.
     */
    public ProjectFile getFile();


    /**
     * Returns the number of characters in the editor document.
     *
     * @return the document length (<code>&gt;= 0</code>).
     *
     * @since 1.0b8
     */
    public int getLength();


    /**
     * Returns the current line number.
     *
     * @return current line (<code>&gt;= 1</code>).
     */
    public int getLine();


    /**
     * Returns the selected text contained in this editor.
     *
     * @return selected text. If no text is selected or the document is empty, returns
     *         <code>null</code>.
     */
    public String getSelectedText();


    /**
     * Selects the specified text.
     *
     * @param startOffset the offset you wish to start selection on (absolute character
     *        position).
     * @param endOffset the offset you wish to end selection on (absolute character
     *        position).
     *
     * @throws IllegalArgumentException if <code><em>startOffset</em> &lt; 0 ||
     *         <em>endOffset</em> &lt; 0</code>
     */
    public void setSelection(
        int startOffset,
        int endOffset);


    /**
     * Returns the selected text's end position.
     *
     * @return the selected text's end position (<code>&gt;=0</code>). Returns
     *         <code>0</code> if the document is empty, or the position of the caret if
     *         there is no selection.
     */
    public int getSelectionEnd();


    /**
     * Returns the selected text's start position.
     *
     * @return the start position (<code>&gt;=0</code>). Returns <code>0</code> for an
     *         empty document, or the position of the caret if there is no selection.
     */
    public int getSelectionStart();


    /**
     * Sets the text of this editor to the specified text. If the text is
     * <code>null</code> or empty, has the effect of simply deleting the old text.
     *
     * @param text the new text to be set.
     */
    public void setText(String text);


    /**
     * Returns the text contained in this editor.
     *
     * @return the text.
     */
    public String getText();


    /**
     * Attaches the given annotations to this view.
     *
     * @param annotations list of annotations (of type {@link
     *        de.hunsicker.jalopy.language.Annotation &lt;Annotation&gt;}) to attach.
     *
     * @see de.hunsicker.jalopy.language.Annotation
     * @since 1.0b9
     */
    public void attachAnnotations(List annotations);


    /**
     * Detaches all existing annotations of this view.
     *
     * @return list with all annotations of this view (of type {@link
     *         de.hunsicker.jalopy.language.Annotation &lt;Annotation&gt;}). Returns an
     *         empty list, if no annotations were attached.
     *
     * @see #attachAnnotations
     * @since 1.0b9
     */
    public List detachAnnotations();


    /**
     * Replaces the currently selected content with new content represented by the given
     * string. If there is no selection this amounts to an insert of the given text. If
     * there is no replacement text this amounts to a removal of the current selection.
     *
     * @param text the string to replace the selection with.
     */
    public void paste(String text);


    /**
     * Tries to set the focus on the receiving component.
     */
    public void requestFocus();


    /**
     * Selects the whole text of the editor.
     */
    public void selectAll();
}
