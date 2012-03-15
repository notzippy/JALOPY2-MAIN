@javax.xml.bind.annotation.XmlSchema(namespace = "http://test...",
elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)
package test;

public class Javadoc
{
    public int aPublicField = 1;
    protected int aProtectedField = 2;
    int aPackageProtectedField = 3;
    private int aPrivateField = 4;

    /** A Javadoc comment for a field.*/
    private int _count;
    
    public enum Test {
        FOO("foo"),
        /** Bar */
        BAR("bar");
        Test(String a) {
            
        }
    }
    /**
     * Commented ENUM
     */
    public enum Test1 {
        FOO("foo"),
        /** Bar */
        BAR("bar");
        Test(String a) {
            
        }
    }

    
    public Javadoc(int param)
    {
      //J-
      String a = "This " +
                 "section should remain " +
                 "unformated";
      //J+    
      String a = "This " +
                 "section should be " +
                 "formated";
    }

    public String aMethodfoo(int              param1, 
                             com.foo.MyObject param2)
        throws FooException, 
               SillyException
    {
        throw new IllegalArgumentException("");
    }

    private class AnInnerClass
    {
        public void aMethod()
        {
        }
    }
}

public interface AnInterface
{
}