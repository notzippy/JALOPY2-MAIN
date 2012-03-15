package de.hunsicker.jalopy.language;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import de.hunsicker.jalopy.language.antlr.ExtendedToken;
import de.hunsicker.jalopy.language.antlr.JavaNodeFactory;
import de.hunsicker.jalopy.language.antlr.Node;

/**
 * This class creates instances of all the Factories used to generate JavaNodes, Nodes, &
 * Extended Tokens. It is also responsible for maintaining and clearing the cache for these
 * factories
 */
public class CompositeFactory {
    /**TODO DOCUMENT ME!*/
    private ExtendedTokenFactory extendedTokenFactory = null;

    /**TODO DOCUMENT ME!*/
    private JavaNodeFactory javaNodeFactory = null;

    /**TODO DOCUMENT ME!*/
    private final Map cacheMap;

    /**TODO DOCUMENT ME!*/
    private final Map reuseMap;

    /**TODO DOCUMENT ME!*/
    private NodeFactory nodeFactory = null;

    /**TODO DOCUMENT ME!*/
    private Recognizer recognizer;

    /**
     * TODO DOCUMENT ME!
     * @author Steve Heyns Mar, 2007
     * @version 1.0
      */
    public class ExtendedTokenFactory {
        /**TODO DOCUMENT ME!*/
        private final CompositeFactory compositeFactory;

        /**
         * TODO DOCUMENT ME!
         * @author Steve Heyns Mar, 2007
         * @version 1.0
          */
        private class ExtendedTokenImpl extends ExtendedToken {
            // Empty implementation
        } // end ExtendedTokenImpl

        /**
         * TODO Creates a new ExtendedTokenFactory object.
         *
         * @param compositeFactory DOCUMENT ME!
         */
        private ExtendedTokenFactory(CompositeFactory compositeFactory) {
            this.compositeFactory = compositeFactory;
        } // end ExtendedTokenFactory()

        /**
         * TODO DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public ExtendedToken create() {
            ExtendedToken cached = (ExtendedToken)compositeFactory.getCached(this.getClass());

            if (cached == null) {
                cached = new ExtendedTokenImpl();
                compositeFactory.addCached(ExtendedTokenFactory.class, cached);
            } // end if

            return cached;
        } // end create()

        /**
         * TODO DOCUMENT ME!
         *
         * @param type The type
         * @param text The text
         *
         * @return Creates a new extended token
         */
        public ExtendedToken create(int    type,
                                    String text) {
            ExtendedToken token = create();

            token.setType(type);
            token.setText(text);
            return token;
        } // end create()
    } // end ExtendedTokenFactory

    /**
     * A constructor to provide single instance attributes
     */
    public CompositeFactory() {
        this.cacheMap = new HashMap();
        this.reuseMap = new HashMap();
        this.extendedTokenFactory = new ExtendedTokenFactory(this);
        this.javaNodeFactory = new JavaNodeFactory(this);
        this.nodeFactory = new NodeFactory(this);
    } // end CompositeFactory()

    /**
     * Added the object to the cache
     *
     * @param class1 The type of object
     * @param cached The object
     */
    public void addCached(Class  class1,
                          Object cached) {
        synchronized (cacheMap) {
            List cache = (List)cacheMap.get(class1);

            if (cache == null) {
                cache = new Vector();
                cacheMap.put(class1, cache);
                reuseMap.put(class1, new Vector());
            } // end if
            cache.add(cached);
        } // end synchronized
    } // end addCached()

    /**
     * Clears all objects that were created in the factories
     */
    public void clear() {
        List cache = null;

        //        List reuseList = null;
        Object item = null;

        // For each of the types clear the contents
        synchronized (cacheMap) {
            for (Iterator i = cacheMap.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Entry)i.next();

                //            reuseList = (List) reuseMap.get(entry.getKey());
                cache = (List)entry.getValue();

                for (Iterator l = cache.iterator(); l.hasNext();) {
                    item = l.next();
                    if (item instanceof Node) {
                        ((Node)item).clear();
                    } // end if
                    else if (item instanceof ExtendedToken) {
                        ((ExtendedToken)item).clear();
                    } // end else if
                    //                reuseList.add(item);
                    l.remove();
                } // end for
            } // end for
        } // end synchronized
    } // end clear()

    /**
     * A factory calls this method to return an object that was cached
     *
     * @param class1 The class used to index
     *
     * @return
     */
    public Object getCached(Class class1) {
        return null;
        // Commented out, just allow the objects to be created by the factories and let
        // the GC take care of removing them
        //        List cache = (List) reuseMap.get(class1);
        //        Object result= null;
        //        if (cache!=null && !cache.isEmpty()) {
        //            result = cache.remove(0);
        //            addCached(class1, result);
        //        }
        //        
        //        return result;
    } // end getCached()

    /**
     * Returns the local copy of the token factory
     *
     * @return The local copy of the token factory
     */
    public ExtendedTokenFactory getExtendedTokenFactory() {
        return extendedTokenFactory;
    } // end getExtendedTokenFactory()

    /**
     * Returns the local copy of the java node factory 
     *
     * @return The local copy of the java node factory
     */
    public JavaNodeFactory getJavaNodeFactory() {
        return javaNodeFactory;
    } // end getJavaNodeFactory()

    /**
     * Returns the local node factory
     *
     * @return The node factory
     */
    public NodeFactory getNodeFactory() {
        return nodeFactory;
    } // end getNodeFactory()

    /**
     * Returns the Recognizer
     *
     * @return The Recognizer
     */
    public Recognizer getRecognizer() {
        return recognizer;
    } // end getRecognizer()

    /**
     * Sets the java Recognizer
     *
     * @param recognizer The java Recognizer
     */
    public void setJavadocRecognizer(Recognizer recognizer) {
        this.recognizer = recognizer;
    } // end setJavadocRecognizer()
} // end CompositeFactory
