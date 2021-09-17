This Junit5 based on project wants to help you with garbage in logs while running tests.

If your test expects some exception (negative path testing) you probably will want not to see them 
in logs in order to have them clear

All you should do is just add @ExtendWith annotations to your test class and one of 
@HideByExceptionClass or @HideByExceptionMessage or @HideByExceptionClassAndMessage 
to the tested field, which exceptions with it should be hidden

~~~
@ExtendWith(LoggingExtension.class)
public class SecondsToMinutesUtilsExceptionTest {

    @HideByExceptionClass({NumberFormatException.class, IllegalArgumentException.class})
    private static SecondsToMinutesUtils secsToMins1;
    
    @HideByExceptionMessage({"unexpected error", "not desired to be logged"})
    private static SecondsToMinutesUtils secsToMins2;
    
    @HideByExceptionClassAndMessage({
            @ClassAndMessage(clazz = NumberFormatException.class, message = "For input string:"),
            @ClassAndMessage(clazz = IllegalArgumentException.class, message = "cannot be 0 or negative"),
            @ClassAndMessage(clazz = NullPointerException.class, message = "Argument cannot be null")
    })
    private static SecondsToMinutesUtils secsToMins3;
    ...
~~~

There is also another way to set up rules for suppressing the logs outside a target class
~~~
@ExtendWith(LoggingExtension.class)
@ClassesToWrapLoggers({HoursHelper.class})
@HideByExceptionClassAndMessage({
    @ClassAndMessage(clazz = IllegalArgumentException.class, message = "Error: obvious"),
})
public class ReplaceOutsideDemoTest {
    private static HoursHelper<Double, Double> hoursHelper;
...
~~~
in that case you may define as many class loggers as you need, not necessary for target class in test.
In example above we can see the target class for testing does not have anny annotation, as desired exceptions 
appear in other class (actually in the example it is the HoursHelper class) and we need to suppress exceptions 
from it.

@ExtendWith(LoggingExtension.class) enables that plugin and one of:
- @HideByExceptionClass(Class[]) allows you to filter out exception classes you're expecting to be thrown
for the tested class.
- @HideByExceptionMessage(String[]) allows you to filter out exceptions by their messages.
- @HideByExceptionClassAndMessage allows to specify both class and message.


How on Earth it works?
It uses extension mechanism for JUnit5 and provides a replacement for all Log4J fields in a target 
(annotated with @HideBy...) class.
That replacement (LoggerAdaptor) class is wrapping real SLF4J logger and every time the target class throws 
an exception it checks if that exception is needed to be hidden. If so - logger skips logging it, 
but exception message is still there.

Note: it likely will not work with final fields, so you perhaps will need also remove 'final' modifier inside the class.
Note: default class for hiding is ReflectiveOperationException.
Note: after test class being processed all extra settings revoked and loggers become unwrapped.