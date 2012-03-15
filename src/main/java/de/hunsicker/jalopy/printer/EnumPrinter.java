package de.hunsicker.jalopy.printer;

import java.io.IOException;

import de.hunsicker.jalopy.language.antlr.JavaNode;
import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;

import antlr.collections.AST;


/**
 * The enum printer 
 */
public class EnumPrinter extends BasicDeclarationPrinter {

    /** Singleton. */
    private static final Printer INSTANCE = new EnumPrinter();

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new EnumPrinter object.
     */
    protected EnumPrinter()
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
     * Prints the enumeration 
     *
     * @param node
     * @param out
     * @throws IOException
     */
    public void print(AST node, NodeWriter out) throws IOException {
        out.state.innerClass = (out.getIndentLength() > 0);
         

        addCommentIfNeeded((JavaNode)node, out);

        printCommentsBefore(node, out);

        // print the modifiers
        AST modifiers = node.getFirstChild();
        PrinterFactory.create(modifiers, out).print(modifiers, out);
        
        out.print(ENUM_SPACE, JavaTokenTypes.LITERAL_enum);
        for(AST child = modifiers.getNextSibling();child!=null;child = child.getNextSibling()) {
            if (child.getType() == JavaTokenTypes.OBJBLOCK) {
                out.state.extendsWrappedBefore = false;
                out.last = JavaTokenTypes.LITERAL_enum;                
            }
            PrinterFactory.create(child, out).print(child, out);
        }
        // TODO out.state.innerClass = false;
        out.last = JavaTokenTypes.ENUM_DEF;
    }
}
