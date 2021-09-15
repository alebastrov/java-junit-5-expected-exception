This Junit5 based on project wants to help you with garbage in logs while running tests.

If your test expects some exception (negative path testing) you probably will want not to see them in logs in order to have them clear

All you should do is just add 2 annotations to your test class

~~~
@ExtendWith(LoggingExtension.class)
public class SecondsToMinutesUtilsExceptionTest {

    @Mitigate({NumberFormatException.class, IllegalArgumentException.class})
    private static SecondsToMinutesUtils secsToMins;
    ...
~~~

@ExtendWith(LoggingExtension.class) enables that plugin and
@Mitigate(Class[]) allows you to set up which Exceptions you're expecting to be thrown
for the tested class.
