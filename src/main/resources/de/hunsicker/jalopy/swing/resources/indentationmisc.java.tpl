public class IndentationMisc
{
    public void foo(int              param1, 
                    String           param2, 
                    com.foo.MyObject param3)
    {
        switch (prio)
        {
            case Priority.ERROR_INT :
            case Priority.FATAL_INT :
                color = Color.red;

                break;

            default :
                color = Color.black;

                break;
        }

        String text = "text";
        int a = -1;
        History.Entry entry = new History.Entry(text);

        // the manager
        HistoryManager manager = new HistoryManager();

        if ((condition1 && condition2) || (condition3 && condition4) || 
            !(condition5 && condition6))
        {
            doSomethingAboutIt();
        }

LOOP:   
        for (AST child = tree.getFirstChild();
             child != null;
             child = child.getNextSibling())
        {
            doSomething();
        }

        String comma = spaceAfterComma ? COMMA_SPACE
                                       : COMMA;

/*
        if (node == null)
        {
            return new NullPrinter();
        }
*/

        String comma = (spaceAfterComma && (shouldIndent || forceIndent))
                           ? COMMA_SPACE
                           : COMMA;
    }
}