package de.hunsicker.jalopy.printer;

import java.io.IOException;

import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
import de.hunsicker.jalopy.storage.ConventionDefaults;
import de.hunsicker.jalopy.storage.ConventionKeys;

import antlr.collections.AST;


/**
 * The EnumConstant printer 
 */
public class EnumConstantPrinter extends BasicDeclarationPrinter {
    /** Singleton. */
    private static final Printer INSTANCE = new EnumConstantPrinter();

    /**
     * 
     */
    public EnumConstantPrinter() {
        super();
    }
    
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
     * Prints the children. If child will exceed line length a new line is printed
     *
     * @param node The node
     * @param out The node writer
     * @throws IOException If an error occurs
     */
    public void print(AST node, NodeWriter out) throws IOException {
        TestNodeWriter tester = out.testers.get();
        boolean spaceAfterComma = AbstractPrinter.settings.getBoolean(
                    ConventionKeys.SPACE_AFTER_COMMA, ConventionDefaults.SPACE_AFTER_COMMA);
        
        tester.reset(out,false);
        printChildren(node,tester);
        int lineLength =
            AbstractPrinter.settings.getInt(
                ConventionKeys.LINE_LENGTH, ConventionDefaults.LINE_LENGTH);

        if (tester.line>1 || tester.column> lineLength) {
            out.printNewline();
        }
        out.testers.release(tester);
        
        addCommentIfNeeded((JavaNode)node, out);
        printCommentsBefore(node,false,out);
        printChildren(node,out);
        AST next = node.getNextSibling();
        if (next!=null) {
            if (next.getType()==JavaTokenTypes.ENUM_CONSTANT_DEF) {
                if (spaceAfterComma && !out.nextNewline) {
                    out.print(COMMA_SPACE,out.last);
                }
                else {
                    out.print(COMMA,out.last);
                }
            }
            else {
                    out.print(SEMI,out.last);
            }
        }
        else {
                out.print(SEMI,out.last);
        }
        printCommentsAfter(
                next, NodeWriter.NEWLINE_NO, NodeWriter.NEWLINE_YES, out);
        
    }
}
