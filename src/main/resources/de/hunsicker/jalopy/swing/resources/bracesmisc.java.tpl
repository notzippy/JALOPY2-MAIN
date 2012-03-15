public class BracesMisc
{
    public void foo()
    {
        if (firstCondition())
        {
            doIf();
        }
        else if (secondCondition())
        {
            doElseIf();
        }
        else
        {
            doElse();
        }

        if (firstCondition()) // endline comment
            doIf();
        else if (secondCondition())
            doElseIf();
        else
            doElse();

        for (int i = 0, n = getMax(); i < n; i++)
        {
            doFor();
        }

        for (int i = 0, n = getMax(); i < n; i++)
            doFor();

        while (condition())
        {
            doWhile();
        }

        while (condition())
            doWhile();

        do
        {
            doDoWhile();
        }
        while (true);

        do
            doDoWhile();
        while (true);

        doSomething();

        {
            int a = 3;
        }

        {
            a = 3;
        }

        try
        {
            trySomething();
        }
        catch (Exception ex)
        {
        }
        finally
        {
            cleanup();
        }
    }
}