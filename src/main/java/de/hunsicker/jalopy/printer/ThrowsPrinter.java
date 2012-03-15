/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;

import antlr.collections.AST;
import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;


/**
 * Printer for the exception specification of methods and constructors
 * [<code>LITERAL_throws</code>].
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.9 $
 */
final class ThrowsPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new ThrowsPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ThrowsPrinter object.
     */
    protected ThrowsPrinter()
    {
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Returns the sole instance of this class.
     *
     * @return the sole instance of this class.
     */
    public static final Printer getInstance()
    {
        return INSTANCE;
    }


    /**
     * {@inheritDoc}
     */
    public void print(
        AST        node,
        NodeWriter out)
      throws IOException
    {
        if (((JavaNode)node).hasCommentsBefore()) {
            printCommentsBefore(node,true,out);
        }
        AST firstType = node.getFirstChild();

        boolean wrappedBefore = false;
        boolean wrappedAfter = false;

        Marker marker = null;

        boolean wrapLines =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.LINE_WRAP, ConventionDefaults.LINE_WRAP)
            && (out.mode == NodeWriter.MODE_DEFAULT);
        int lineLength =
            AbstractPrinter.settings.getInt(
                ConventionKeys.LINE_LENGTH, ConventionDefaults.LINE_LENGTH);
        boolean indentDeep =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.INDENT_DEEP, ConventionDefaults.INDENT_DEEP);
        int indentLength = out.getIndentLength();
        int deepIndent =
            AbstractPrinter.settings.getInt(
                ConventionKeys.INDENT_SIZE_DEEP, ConventionDefaults.INDENT_SIZE_DEEP);
        int indentSize =
            AbstractPrinter.settings.getInt(
                ConventionKeys.INDENT_SIZE_THROWS, ConventionDefaults.INDENT_SIZE_THROWS);
        boolean indentCustom = indentSize > -1;

        if (
            (out.mode == NodeWriter.MODE_DEFAULT)
            && (out.newline
            || AbstractPrinter.settings.getBoolean(
                ConventionKeys.LINE_WRAP_BEFORE_THROWS,
                ConventionDefaults.LINE_WRAP_BEFORE_THROWS)
            || (wrapLines
            && exceedsBarriers(node, firstType, lineLength, deepIndent, out))))
        {
            wrappedBefore = true;

            if (!out.newline)
            {
                out.printNewline();
            }

            if (indentCustom)
            {
                out.print(out.getString(indentSize), JavaTokenTypes.WS);
                out.print(THROWS_SPACE, JavaTokenTypes.LITERAL_throws);
                marker = out.state.markers.add();
            }
            else
            {
                if (indentDeep && canAlign(firstType, lineLength, deepIndent, out))
                {
                    marker = out.state.markers.getLast();

                    // shift the throws to the left so that the
                    // exception type(s) align with the parameter(s)
                    out.print(
                        out.getString((marker.column - indentLength) - 7),
                        JavaTokenTypes.WS);
                    out.print(THROWS_SPACE, JavaTokenTypes.LITERAL_throws);
                }
                else // use standard indentation
                {
                    indentDeep = false;
                    out.indent();
                    out.print(THROWS_SPACE, JavaTokenTypes.LITERAL_throws);
                    marker = out.state.markers.add();
                }
            }
        }
        else // print directly after parameters
        {
            out.print(SPACE_THROWS_SPACE, JavaTokenTypes.LITERAL_throws);
            marker = out.state.markers.add();
        }

        boolean spaceAfterComma =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.SPACE_AFTER_COMMA, ConventionDefaults.SPACE_AFTER_COMMA);
        boolean forceWrapping =
             AbstractPrinter.settings.getBoolean(
                ConventionKeys.LINE_WRAP_AFTER_TYPES_THROWS,
                ConventionDefaults.LINE_WRAP_AFTER_TYPES_THROWS);
        boolean wrapAll =
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.LINE_WRAP_AFTER_TYPES_THROWS_EXCEED,
                ConventionDefaults.LINE_WRAP_AFTER_TYPES_THROWS_EXCEED)
            && (out.mode == NodeWriter.MODE_DEFAULT);

        TestNodeWriter tester = null;

        if (wrapLines || wrapAll)
        {
            tester = out.testers.get();
        }

        if (!forceWrapping && wrapAll)
        {
            PrinterFactory.create(node, out).print(node, tester);

            if ((tester.length - 7 + out.column) > lineLength)
            {
                forceWrapping = true;
            }

            tester.reset();
        }

        boolean newline = false;
        for (AST child = firstType; child != null; child = child.getNextSibling())
        {
            newline = false;
            switch (child.getType())
            {
                case JavaTokenTypes.COMMA :
                    out.print(COMMA, JavaTokenTypes.COMMA);

                    if (forceWrapping)
                    {
                        out.printNewline();
                        newline = true;

                        if (!wrappedAfter)
                        {
                            wrappedAfter = true;

                            if (!indentDeep)
                            {
                                out.indent();
                            }
                        }

                        if (indentCustom && wrappedBefore)
                        {
                            printIndentation(indentSize, out);
                        }
                        else
                        {
                            printIndentation(out);
                        }

                        /*if (newlineAfterThrows)
                        {
                            out.state.newlineBeforeLeftBrace = true;
                        }*/
                    }
                    else if (wrapLines)
                    {
                        AST next = child.getNextSibling();

                        if (next != null)
                        {
                            PrinterFactory.create(next, out).print(next, tester);

                            if ((tester.length + out.column) > lineLength)
                            {
                                out.printNewline();
                                newline = true;

                                if (!wrappedAfter)
                                {
                                    wrappedAfter = true;

                                    if (!indentDeep)
                                    {
                                        out.indent();
                                    }
                                }

                                if (indentCustom && wrappedBefore)
                                {
                                    printIndentation(indentSize, out);
                                }
                                else
                                {
                                    printIndentation(out);
                                }

                                /*if (newlineAfterThrows)
                                {
                                    out.state.newlineBeforeLeftBrace = true;
                                }*/
                            }

                            tester.reset();
                        }
                    }
                    if (spaceAfterComma && !newline)
                    {
                        out.print(" ", JavaTokenTypes.WS);
                    }


                    break;

                default :
                    PrinterFactory.create(child, out).print(child, out);

                    break;
            }
        }

        if (tester != null)
        {
            out.testers.release(tester);
        }

        if (!indentDeep)
        {
            if (!indentCustom && wrappedBefore)
            {
                out.unindent();
            }

            if (wrappedAfter)
            {
                out.unindent();
            }
        }

        if (
            AbstractPrinter.settings.getBoolean(
                ConventionKeys.BRACE_TREAT_DIFFERENT_IF_WRAPPED,
                ConventionDefaults.BRACE_TREAT_DIFFERENT_IF_WRAPPED)
            && (wrappedBefore || wrappedAfter || out.state.parametersWrapped))
        {
            out.state.newlineBeforeLeftBrace = true;
        }
        
    }


    /**
     * Determines whether the given node (denoting the first type name of the throws
     * clause) can be aligned with the parameter list of the method declaration.
     *
     * @param node the first type the throws clause.
     * @param lineLength the maximum line length.
     * @param deepIndent deep indent setting.
     * @param out stream to write to.
     *
     * @return <code>true</code> if the type name can be aligned.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b7
     */
    private boolean canAlign(
        AST        node,
        int        lineLength,
        int        deepIndent,
        NodeWriter out)
      throws IOException
    {
        TestNodeWriter tester = out.testers.get();
        PrinterFactory.create(node, out).print(node, tester);

        Marker marker = out.state.markers.getLast();

        if (
            ((marker.column - 7) < deepIndent)
            && ((marker.column + tester.length) < lineLength)
            && ((marker.column - out.getIndentLength() - 7) > 0))
        {
            out.testers.release(tester);

            return true;
        }

        out.testers.release(tester);

        return false;
    }


    /**
     * Determines whether the given node (denoting the first type name of the throws
     * clause) would exceed one of the wrapping barriers without wrapping.
     *
     * @param node a LITERAL_throws node.
     * @param firstType a LITERAL_String node (the first type of the throws clause).
     * @param lineLength the maximal line length.
     * @param deepIndent the deepIndent size.
     * @param out stream to write to.
     *
     * @return <code>true</code> if the node would exceed either one of the barriers.
     *
     * @throws IOException if an I/O error occured.
     *
     * @since 1.0b7
     */
    private boolean exceedsBarriers(
        AST        node,
        AST        firstType,
        int        lineLength,
        int        deepIndent,
        NodeWriter out)
      throws IOException
    {
        if ((out.column + 1) > deepIndent)
        {
            return true;
        }

        TestNodeWriter tester = out.testers.get();

        try
        {
            PrinterFactory.create(node, out).print(node, tester);

            if ((out.column + tester.length) > lineLength)
            {
                return true;
            }

            tester.reset();

            PrinterFactory.create(firstType, out).print(firstType, tester);

            if ((out.column + 7 + tester.length) > lineLength)
            {
                return true;
            }

            return false;
        }
        finally
        {
            out.testers.release(tester);
        }
    }
}
