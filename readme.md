This project based on Junit5 (SLF4J or Log4J by your choice) helps you with garbage in logs while running tests.

If your test expects some exception (negative path testing) you probably will want not to see them 
in logs in order to have them clear

All you should do is just add @ExtendWith annotations to your test class and one of the following:
- add annotation @HideByExceptionClass (*)
- add annotation @HideByExceptionMessage (*)
- add annotation @HideByExceptionClassAndMessage (*)
- set up true to LoggingExtension.setSuspendLogging(true);

* May be set to the tested field in test class, which exceptions with it should be hidden or to the whole class.

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

Note: it likely will not work with 'final' fields, so you will need to remove 'final' modifier inside for that Loggers.
Note: default class for hiding is ReflectiveOperationException.
Note: after test class being processed all extra settings revoked and loggers become unwrapped.

@ToDo do the same for particular test,not only for test class