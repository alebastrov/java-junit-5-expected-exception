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


How on Earth it works?
It uses extension mechanism for JUnit5 and provides a replacement for your Log4J field in target class.
That replacement class is wrapping real SLF4J logger and evry time target class throwing an exception it check if that exception class is expected. If so - it skips logging it.

Note: sometimes it cannot work with final fielda, so you perhaps will need also remove 'final' modificator inside the class.
