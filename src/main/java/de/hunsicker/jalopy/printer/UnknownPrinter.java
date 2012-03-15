package de.hunsicker.jalopy.printer;

import java.io.IOException;

import de.hunsicker.jalopy.language.antlr.JavaNode;

import antlr.CommonHiddenStreamToken;
import antlr.collections.AST;


/**
 * TODO 
 */
public class UnknownPrinter extends AbstractPrinter {
    /** Singleton. */
    private static final Printer INSTANCE = new UnknownPrinter();

    /**
     * 
     */
    public UnknownPrinter() {
        super();
        // TODO Auto-generated constructor stub
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
     * TODO 
     *
     * @param node
     * @param out
     * @throws IOException
     */
    public void print(AST node, NodeWriter out) throws IOException {
        printUnknownNode((JavaNode)node,out);

    }

    /**
     * @param node
     * @param out
     */
    private void printUnknownNode(JavaNode node, NodeWriter out) throws IOException {
        CommonHiddenStreamToken firstComment = node.getHiddenBefore();
        for (
                CommonHiddenStreamToken comment = firstComment; comment != null;
                comment = comment.getHiddenAfter())
            {
            out.print(comment.getText(),comment.getType());
            }
        out.print(node.getText(),node.getType());
        
        printChildren(node,out);
        
        CommonHiddenStreamToken lastComment = node.getHiddenAfter();
        for (
                CommonHiddenStreamToken comment = lastComment; comment != null;
                comment = comment.getHiddenAfter())
            {
            out.print(comment.getText(),comment.getType());
            }
        
    }

}
