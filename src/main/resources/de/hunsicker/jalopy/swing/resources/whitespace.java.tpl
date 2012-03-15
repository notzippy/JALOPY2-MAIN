public class Whitespace
{
    public void aMethodDef()
    {
        com.foo.someMethodCalls(getValue(), getSecondValue(), thirdValue);

        while (!isDone)
        {
            a = (b + c) * d;
        }

        String[] values = new String[] 
        {
            "One", "Two", "Three", "Four", "Five", "Six", "Seven"
        };
        String[] moreValues = new String[] { "One", "Two" };

        for (i = 0; i < 100; i++)
        {
            sum += value[i];
        }

        String[] names = (String[])data.toArray(new String[0]);

        switch (character)
        {
            case 'A' :
                break;
        }

        doSomething(a, b, c, d);

        int line = ((JavaNode)node).getStartLine();

        if ((LA(1) == '/') && (LA(2) != '*' || (LA(2) == '*')))
        {
            return;
        }

        if (((1L << i) & l) != 0)
        {
            System.out.print("1");
        }
    }
}