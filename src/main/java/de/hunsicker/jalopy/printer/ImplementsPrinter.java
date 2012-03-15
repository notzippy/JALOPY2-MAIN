/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.printer;

import java.io.IOException;

import antlr.collections.AST;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;


/**
 * Printer for implements clauses (<code>IMPLEMENTS_CLAUSE</code>).
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.5 $
 */
final class ImplementsPrinter
    extends AbstractPrinter
{
    //~ Static variables/initializers ----------------------------------------------------

    /** Singleton. */
    private static final Printer INSTANCE = new ImplementsPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new ImplementsPrinter object.
     */
    protected ImplementsPrinter()
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
        AST first = node.getFirstChild();

        if (first != null)
        {
            boolean wrapLines =
                AbstractPrinter.settings.getBoolean(
                    ConventionKeys.LINE_WRAP, ConventionDefaults.LINE_WRAP)
                && (out.mode == NodeWriter.MODE_DEFAULT);
            int lineLength =
                AbstractPrinter.settings.getInt(
                    ConventionKeys.LINE_LENGTH, ConventionDefaults.LINE_LENGTH);
            boolean newlineBefore =
                AbstractPrinter.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_BEFORE_IMPLEMENTS,
                    ConventionDefaults.LINE_WRAP_BEFORE_IMPLEMENTS);
            int indentSize =
                AbstractPrinter.settings.getInt(
                    ConventionKeys.INDENT_SIZE_IMPLEMENTS,
                    ConventionDefaults.INDENT_SIZE_IMPLEMENTS);
            boolean indentCustom = indentSize > -1;
            boolean wrappedBefore = false;

            if (
                out.newline || newlineBefore
                || (wrapLines
                && ((out.column + 11 + first.getText().length()) > lineLength)))
            {
                wrappedBefore = true;

                if (!out.newline)
                {
                    out.printNewline();
                }

                if (indentCustom)
                {
                    out.print(out.getString(indentSize), JavaTokenTypes.WS);
                    out.print(IMPLEMENTS_SPACE, JavaTokenTypes.LITERAL_extends);
                }
                else
                {
                    out.indent();
                    out.print(IMPLEMENTS_SPACE, JavaTokenTypes.LITERAL_extends);
                }
            }
            else
            {
                out.print(SPACE_IMPLEMENTS_SPACE, JavaTokenTypes.LITERAL_extends);
            }

            TestNodeWriter tester = null;

            boolean wrapAll =
                AbstractPrinter.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_TYPES_IMPLEMENTS_EXCEED,
                    ConventionDefaults.LINE_WRAP_AFTER_TYPES_IMPLEMENTS_EXCEED)
                && (out.mode == NodeWriter.MODE_DEFAULT);

            if (wrapLines || wrapAll)
            {
                tester = out.testers.get();
            }

            boolean forceWrapping =
                AbstractPrinter.settings.getBoolean(
                    ConventionKeys.LINE_WRAP_AFTER_TYPES_IMPLEMENTS,
                    ConventionDefaults.LINE_WRAP_AFTER_TYPES_IMPLEMENTS);

            if (!forceWrapping && wrapAll)
            {
                if (wrappedBefore)
                {
                    forceWrapping = true;
                }
                else
                {
                    PrinterFactory.create(node, out).print(node, tester);

                    if ((tester.length - 11 + out.column) > lineLength)
                    {
                        forceWrapping = true;
                    }

                    tester.reset();
                }
            }

            boolean indentDeep =
                AbstractPrinter.settings.getBoolean(
                    ConventionKeys.INDENT_DEEP, ConventionDefaults.INDENT_DEEP);
            String comma =
                AbstractPrinter.settings.getBoolean(
                    ConventionKeys.SPACE_AFTER_COMMA, ConventionDefaults.SPACE_AFTER_COMMA)
                ? COMMA_SPACE
                : COMMA;

            boolean wrappedAfter = false;

            Marker marker = out.state.markers.add();

            for (AST child = first; child != null; child = child.getNextSibling())
            {
                PrinterFactory.create(child, out).print(child, out);

                AST next = child.getNextSibling();

                if (next != null)
                {
                    if (forceWrapping)
                    {
                        out.print(COMMA, JavaTokenTypes.COMMA);
                        out.printNewline();

                        if (!wrappedAfter)
                        {
                            wrappedAfter = true;

                            if (!indentDeep)
                            {
                                out.indent();
                            }
                        }

                        if (
                            indentCustom
                            && (wrappedBefore || out.state.extendsWrappedBefore))
                        {
                            printIndentation(indentSize, out);
                        }
                        else
                        {
                            printIndentation(out);
                        }
                    }
                    else if (wrapLines)
                    {
                        PrinterFactory.create(next, out).print(next, tester);

                        if ((tester.length + out.column + comma.length()) > lineLength)
                        {
                            out.print(COMMA, JavaTokenTypes.COMMA);
                            out.printNewline();

                            if (!wrappedAfter)
                            {
                                wrappedAfter = true;

                                if (!indentDeep)
                                {
                                    out.indent();
                                }

                                if (!wrappedBefore && out.state.extendsWrappedBefore)
                                {
                                    out.indent();
                                }
                            }

                            if (
                                indentCustom
                                && (wrappedBefore || out.state.extendsWrappedBefore))
                            {
                                printIndentation(indentSize, out);
                            }
                            else
                            {
                                printIndentation(out);
                            }
                        }
                        else
                        {
                            out.print(comma, JavaTokenTypes.COMMA);
                        }

                        tester.reset();
                    }
                    else
                    {
                        out.print(comma, JavaTokenTypes.COMMA);
                    }
                }
            }

            if (wrapLines)
            {
                out.testers.release(tester);
            }

            out.state.markers.remove(marker);

            if (wrappedBefore && !indentCustom)
            {
                out.unindent();
            }

            if (wrappedAfter)
            {
                if (!indentDeep)
                {
                    out.unindent();
                }

                if (!wrappedBefore && out.state.extendsWrappedBefore)
                {
                    out.unindent();
                }
            }

            if (
                !out.state.newlineBeforeLeftBrace
                && AbstractPrinter.settings.getBoolean(
                    ConventionKeys.BRACE_TREAT_DIFFERENT_IF_WRAPPED,
                    ConventionDefaults.BRACE_TREAT_DIFFERENT_IF_WRAPPED)
                && (wrappedBefore || wrappedAfter))
            {
                out.state.newlineBeforeLeftBrace = true;
            }
        }
    }
}
