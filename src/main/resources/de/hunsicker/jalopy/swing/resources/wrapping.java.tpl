public class Wrapping
    extends AnotherFoo
    implements Fooable,
               Forgetable
{
/**
 * Annotation type to indicate a task still needs to be
 *   completed.
 */
    @Target( { ANNOTATION_TYPE, METHOD } )
    @Retention( RUNTIME )
 public @interface TODO {
  String value() default "TODO";
}

/**
 * The card enumeration with built in constructor
 */
enum CardValue {
    ACE(1, 1, 11), DEUCE(2, 2), THREE(3, 3), FOUR(4, 4), 
    FIVE(5, 5), SIX(6, 6), SEVEN(7, 7), EIGHT(8, 8), 
    NINE(9, 9), TEN(10, 10), JACK(11, 10), QUEEN(12, 10), KING(13, 10);
    
    //extra private information
    private int order; // A simple assignment !
    private int value;
    private int otherValue;
    
    //constructor
    CardValue(int order, int value){
        this.order = order;
        this.value = value;
        this.otherValue = -1;
    }}

    public enum NavigateTo
    {
        currentPage,
        previousPage,
        previousAction,
        page
    }
@TODO(value="Figure out the amount of interest per month")
public void calculateInterest(float amount, float rate) {
  // Need to finish this method later
}    
    public void foo(int              param1,
                    String           param2,
                    com.foo.MyObject param3,
                    Object           param4)
        throws FooException,
               SillyException
    {
        message.format(ERROR_SOURCE_ADDRESS).param(m_session.getAimName())
               .send();
        _userDatabase.addUser("Name", encryptPassword("password", _secretKey),
                              "123 fake address");

        if ((condition1 && condition2) ||
            (condition3 && condition4) ||
            !(condition5 && condition6))
        {
            if (!foobar(maybe,wrap) || ((foo&1)==true)&& (bar+1<2) ||(false) ) {
              a++;
            }
            a--;
            foobar(dont,wrap);
        
            ;
        }

		this.customerNumber =
			new CustomerNumber(
				ServiceManager.createService()
				.createNewKey(
				    VerySpecialKeyCreationService.IMPORTANT_SERVICE_KEY
				)
			);

		ServiceManager.createService()
		.createNewKey(
		    VerySpecialKeyCreationService.IMPORTANT_SERVICE_KEY
		);

		do {
			;
		} while (
			method.isStatic()
			&& method.isPublic()
			&& method.isFinal()
			&& method.isSynchronized()
		);
    }
}