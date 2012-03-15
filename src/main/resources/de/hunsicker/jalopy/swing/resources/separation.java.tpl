package com.foo.mypackage;

import com.foo.*;
import com.foo.mypackage.stuff.*;

public class Separation
{

    public static final Status STATUS_DISPOSED = new Status("disposed");
    private static String version;
    protected Foo instance;

    static
    {
        loadVersion();
    }

    {
        performChecking(this);
    }

    public void Foo()
    {
    }

    public void Foo(Foo anotherFoo)
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