public class Indentation
    extends AnotherFoo
    implements Fooable
{
    public void foo(int              param1, 
                    String           param2, 
                    com.foo.MyObject param3)
        throws FooException, 
               SillyException
    {
        if (validateParam1(param2) && validateParam2(param2) && 
            validateParam3(param3))
        {
            doSomething(); // endline comments follow statements
        }

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
    }
}