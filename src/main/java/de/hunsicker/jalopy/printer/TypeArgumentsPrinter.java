package de.hunsicker.jalopy.printer;

import java.io.IOException;

import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;

import antlr.collections.AST;


/**
 * TODO 
 */
public class TypeArgumentsPrinter extends AbstractPrinter {
    /** Singleton. */
    private static final Printer INSTANCE = new TypeArgumentsPrinter();

    /**
     * 
     */
    public TypeArgumentsPrinter() {
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
        out.print("<", node.getType());
        boolean first = true;
        // TODO validate this is ok for all 
        for(AST child = node.getFirstChild();child!=null;child = child.getNextSibling()) {
            processTypeArguement(child,out,first);
            first=false;
        }
        out.print(">", node.getType());

    }

    /**
     * @param child
     * @param out
     * @throws IOException
     */
    private void processTypeArguement(AST node, NodeWriter out,boolean first) throws IOException {
        // TODO Clean up commas
    	if (node.getType() == JavaTokenTypes.COMMA) {
        	    PrinterFactory.create(node, out).print(node,out);
    	}
    	else
        for(AST child = node.getFirstChild();child!=null;child = child.getNextSibling()) {
            switch(child.getType()) {
                case JavaTokenTypes.WILDCARD_TYPE:
                    out.print(QUESTION,child.getType());
                	processTypeArguement(child,out,true);
                	break;
                case JavaTokenTypes.TYPE_UPPER_BOUNDS:
                    out.print(EXTENDS_SPACE,child.getType());
	            	processTypeArguement(child,out,true);
            	break;
                case JavaTokenTypes.TYPE_LOWER_BOUNDS:
                    out.print(SUPER_SPACE,child.getType());
                	processTypeArguement(child,out,true);
            	break;
            	
            	default :
            	    PrinterFactory.create(child, out).print(child,out);
            }
        }
    }
}
