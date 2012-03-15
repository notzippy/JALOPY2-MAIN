public class SeparationComments
{
    public static final Statuz STATUS_DISPOSED = new Status("disposed");
    private static String version;

    static
    {
        loadVersion();
    }

    protected Foo instance;

    {
        performChecking(this);
    }

    public Foo()
    {
    }

    public Foo(Foo anotherFoo)
    {
    }

    private final void loadVersion()
    {
    }


    private void performChecking(Foo instance)
    {
    }

    public static final class Status
    {
        String name;

        private Status(String name)
        {
            this.name = name;
        }
    }
}