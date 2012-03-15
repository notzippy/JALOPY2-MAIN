public class Braces
{
    public void foo() throws IOException, FileNotFoundException, InterruptedIOException, UnsupportedEncodingException
    {
        do
        {
            for (int i = 0, n = getMax(); i < n; i++)
            {
                doFor();
            } // end for

            try
            {
                if (x > 0)
                {
                    doIf();
                } // end if
                else if (x < 0)
                {
                    doElseIf();
                } // end else if
                else
                {
                    doElse();
                } // end else

                switch (a)
                {
                    case 0 : {
                        doCase0();
                        break;
                    } // end case
                    case 1:
                        doCase1();
                        break;

                    default : {
                        doDefault();
                        break;
                    } // end default
                } // end switch
            } // end try
            catch (Exception e)
            {
                processException(e.getMessage(), x + y, z, a);
            } // end catch
            finally
            {
                processFinally();
            } // end finally
        }
        while (true);
    } // end method
} // end class