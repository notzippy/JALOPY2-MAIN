public class Foo
{
    public void foo()
    {
        do
        {
            for (int i = 0, n = getMax(); i < n; i++)
            {
                doFor();
            }

            try
            {
                if (x > 0)
                {
                    doIf();
                }
                else if (x < 0)
                {
                    doElseIf();
                }
                else
                {
                    doElse();
                }

                switch (a)
                {
                    case 0 :
                        doCase0();

                        break;

                    default :
                        doDefault();
                }
            }
            catch (Exception e)
            {
                processException(e.getMessage(), x + y, z, a);
            }
            finally
            {
                processFinally();
            }
        }
        while (true);
    }
}