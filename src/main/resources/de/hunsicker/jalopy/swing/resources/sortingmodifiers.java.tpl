public final class SortModifiers
{
    protected static final transient volatile Object lock = new Object();

    public static final synchronized native void foo()
    {
    }

    protected abstract static class Delegatee
    {
    }


    private static final class InnerFoo
    {
    }
}