**Index**


# Result Verification Patterns #

<p>In a unit test there are two result verification patterns that can be used. When we make a standard JUnit test case using assertion methods, we are following a State Verification Strategy but when your class has no state to verify we need to follow a Behavior Verification Strategy.</p>
<p>The class tested is called SUT (System Under Test), each dependency is called DOC (Depended-On Component).</p>
<p>There are four basic steps to build a test regardless the strategy we need to follow. These steps are usually done in the same order.<br>
<ol><li>Setup<br>
</li><li>Exercise<br>
</li><li>Verify<br>
</li><li>Teardown</p>
<p>In the first phase (Setup) we setup everything is required by the SUT to be tested. In the second phase (Exercise) we define the interaction that will be tested. In the third phase (Verify) we do whatever is necessary to determine whether the expected outcome has been obtained. In the fourth phase (Teardown) we put the world back into the state in which we found it before the Setup phase.</p></li></ol>

## State Verification ##

In a State Verification we inspect the state of the System Under test after it has been exercised and compare it to the expected state.

<p align='center'>
<img src='http://econference4.googlecode.com/svn/wiki/img/mock/state-based-testing.jpg' /> </p>
<p align='center'><b>Figure 1. State based Testing</b></p>

To do this we usually interact with the SUT after the Exercise step to get his state back and use JUnit assertion methods.

## Behavior Verification ##

In a Behavior Verification we capture the indirect outputs of the SUT as they occur and compare them to the expected behavior.

<p align='center'>
<img src='http://econference4.googlecode.com/svn/wiki/img/mock/behavior-based-testing.jpg' /> </p>
<p align='center'><b>Figure 2. Behavior based Testing</b></p>

Verifying the indirect outputs means that we check the interactions between the SUT and his dependencies. This allows us to define his behavior without checking his state.
Indirect outputs are checked interacting directly with the DOCs. To do this we use Test Doubles with the mocking framework called Mockito.

# Test Doubles #

<p>Test Doubles are objects that are used to replace a component on which the SUT depends. We can do this for several reasons but the most important are: invocation tracking for behavior verification and indirect inputs control.</p>
<p>Every Test Double needs to be installed, we need a way to replace a real DOC and make the SUT interact with it. To make this possible there are Design-for-Testability Patterns that will be explained below.</p>

## Test Stub ##

A Test Stub is a type of Test Double used to replace a real component on which the SUT depends so that the test has a control point for the indirect inputs of the SUT. Its inclusion allows the test to force the SUT down paths it might not otherwise execute. For this we can use this type of Test Double even in a state verification test to avoid untested code.

<p align='center'>
<img src='http://econference4.googlecode.com/svn/wiki/img/mock/test-stub-testing.jpg' /> </p>
<p align='center'><b>Figure 3. Testing with Test Stubs</b></p>

When you create the Test Stub you must specify all the return values it should return during the Exercise phase.

## Mock Object ##

A Mock Object is a Test Double used to replace a real component on which the SUT depends on with a test-specific object that verifies it is being used correctly by the SUT.

<p align='center'>
<img src='http://econference4.googlecode.com/svn/wiki/img/mock/mock-object-testing.jpg' /> </p>
<p align='center'><b>Figure 4. Testing with Mock Objects</b></p>

This Test Double emulates the real DOC tracking all the invocations. It allows to verify these interactions by defining all the expected ones. Note that it won't execute any real DOC code, allowing the test to put the SUT in an isolated state that will facilitate the addressing of future issues.

## Design-for-Testability Patterns ##

<p>When we use a Test Double we need to find a way to make the SUT use it instead of a real DOC. To do this there are many ways we can take but the SUT must be written to make this possible. Keep in mind this patterns when you write your code or its behavior may be hard to test.</p>
<p>There are two design patterns that are specifically intended to design the SUT so that we can replace his dependencies at runtime: Dependency Injection and Dependency Lookup.<br>
Dependency Injection is pretty simple and it’s based on three basic options:</p>

  * Parameter Injection: We pass the dependency directly to the SUT method as we exercise it.
  * Constructor Injection: We tell the SUT which DOC to use when we construct it.
  * Setter Injection: We tell the SUT about the DOC sometime between when we construct it and when we exercise it using a setter method.

Dependency Lookup is based on a "Component Broker" that builds the dependencies of the SUT for him. Basically the SUT asks this object to return the DOCs when he needs them.

## Test-Specific Subclass ##

If Dependency Injection and Dependency Lookup cannot be used there is another solution to force Test Doubles installation. You can extend the SUT defining a specific subclass for testing purpose. This is done to add methods that modify the behavior of the SUT just enough to make it testable by implementing control points (Test Stubs installation) and observation points (Mock Objects installation).

<p align='center'>
<img src='http://econference4.googlecode.com/svn/wiki/img/mock/test-specific-subclass.jpg' /> </p>
<p align='center'><b>Figure 5. Testing with a Test-Specific Subclass</b></p>

This effort typically involves exposing instance variables using setter and getters or overriding specific methods used to create new DOC instances. Since this subclass would be packaged together with the tests that use it, its use does not change how the SUT is seen by the rest of the application. Note that you must be careful to ensure that you do not replace any of the behavior you are actually trying to test. If you can’t install Test Double even with a test-specific subclass, think about refactoring your code.

# Mockito Framework #

Mockito is a Mocking Framework written by Szczepan Faber that allows testing with Test Double and must be used to build new behavior test cases. Note that the word "mock" is used to define both Test Stubs and Mock Objects but they are two different types of Test Doubles.
Mockito framework (ver. 1.8.5) is loaded in the eConference project as a new plugin and uses JUnit runners as in standard JUnit test cases.

## JUnit Integration ##

To use Mockito you just need to import his library in your test class:

```
import static org.mockito.Mockito.*;
```

It will use the same output panel of JUnit for test results:

<p align='center'>
<img src='http://econference4.googlecode.com/svn/wiki/img/mock/junit-output-panel-for-mockito.jpg' /></p>
<p align='center'><b>Figure 6. JUnit output panel for Mockito</b></p>

If you are making a new test plugin, remember to add Mockito on the dependencies list:

<p align='center'>
<img src='http://econference4.googlecode.com/svn/wiki/img/mock/plugin-dependencies.jpg' /></p>
<p align='center'><b>Figure 7. Test Plugin dependencies</b></p>

You need also to export the `allpackagetests` package (Figure 8) to let JUnit run plugin tests from the project level suite and make sure that the general test plugin `it.uniba.di.cdg.econference-tests` has your test plugin in the dependencies list (Figure 9).

<p align='center'>
<img src='http://econference4.googlecode.com/svn/wiki/img/mock/package-exported.jpg' /></p>
<p align='center'><b>Figure 8. Test Plugin exported package</b></p>

<p align='center'>
<img src='http://econference4.googlecode.com/svn/wiki/img/mock/package-imported.jpg' /></p>
<p align='center'><b>Figure 9. General Test Plugin imported packages</b></p>

## Mockito API ##

These examples mock the List class because it’s easy to understand her methods. In a real situation you will probably never mock this class.
When you make a new Test Double you have to specify the concrete class or the interface that will be mocked.

```
LinkedList mockedList = mock(LinkedList.class);
```

Once created, this object will remember all the invocations done on his methods.
The "stubbing" operation is done specifying the methods response of the simulated object but can also be used to throw exceptions:

```
when(mockedList.get(0)).thenReturn("first element");
when(mockedList.get(1)).thenThrow(new Exception);
```

In this case, the following invocation:

```
System.out.println(mockedList.get(0));
```

will print out `"first element"`. This one instead:

```
System.out.println(mockedList.get(1));
```

will throw an exception of type `Exception`.
By default, Mockito makes a default stubbing for all methods that return value. The mock will returns null, an empty collection or appropriate primitive/primitive wrapper value (e.g.: 0 for int/Integer, false for boolean/Boolean and so on). For this, the following invocation for our example:

```
System.out.println(mockedList.get(2));
```

will print `"null"`.
When we need to verify some behavior we need to use the `verify` method:

```
verify(mockedList).add("something");
```

this will check if the `add` method with the `"something"` argument has been called in the Exercise test phase. In fact, if we have the following invocation:

```
mockedList.add("something");
```

the test will success. This verification can fail if there is no call for the `add` method or there is an argument mismatch. The following invocation:

```
mockedList.add("something else");
```

will make the test fail even if we had an invocation on the right method.
Mockito verifies argument values in natural java style using the `equals()` method. When extra flexibility is required then you might use argument matchers:

```
when(mockedList.get(anyInt()).thenReturn("same element");
verify(mockedList).get(anyInt());
```

<p>Note that it usually makes no sense verifying a stubbed method because it means you are using the Test Double as a Test Stub and as a Mock Object too. The example above, however, let you understand easily what an argument matcher is.</p>
<p>There are many matchers you can use: <code>anyString()</code>, <code>anyList()</code>, <code>anyLong()</code>, <code>anyMap()</code>, <code>anyShort()</code>, <code>anySet()</code>, <code>anyObject()</code> and so on.</p>
<p>When you use argument matchers, all arguments have to be provided by matchers. This means that the following verification can’t be done:</p>

```
verify(mock).method(anyInt(), anyString(), "argument 3");
```

In these cases you need to use the `eq()` argument matcher:

```
verify(mock).method(anyInt(), anyString(),eq("argument 3"));
```

When you need to verify the exact number of invocations, you can use the `times(number)` argument in the `verify` method:

```
mockedList.add("one time");

mockedList.add("two times");
mockedList.add("two times");

mockedList.add("three times");
mockedList.add("three times");
mockedList.add("three times");

verify(mockedList, times(1)).add("one time");
// it’s the same of the standard verify so times(1) can be omitted
verify(mockedList).add("one time");

verify(mockedList, times(2)).add("two times");

verify(mockedList, times(3)).add("three times ");
```

If you need more flexibility on the number of invocation check you can use `atLeastOnce()`, `atLeast(number)`, `atMost(number)` as follows:

```
verifyZeroInteractions(mockedList);
// same of:
verifyNoMoreInteractions(mockedList);
```

You can check if some invocations are done in a specific order using the `inOrder` object:

```
mockedList1.add("first");
mockedList2.add("second");

InOrder inOrder = inOrder(mockedList1, mockedList2);

inOrder.verify(mockedList1).add("first");
inOrder.verify(mockedList2).add("second");
```

Stubbing voids requires different approach from `when(mock.method()).then` because method specification are actually real invocations and the compiler does not like void methods inside brackets.
Since you will never stub a void method for specific outputs, this situation applies on the stubbing of exceptions. In these cases you need to follow this syntax:

```
doThrow(new Exception).when(mock.voidMethod("arg0"));
```

For further information on the Mockito API go [here](http://mockito.googlecode.com/svn/tags/1.8.0/javadoc/org/mockito/Mockito.html).

# How to write a Behavior Test #

<p>Since Mockito library is used inside the JUnit framework, you can still use all the Annotations needed to define the steps before and after the real tests (<code>@Before</code>, <code>@After</code>, <code>@Test</code>).</p>
<p>We decided to keep behavior tests separated from the state tests because if you need to mix them there are probably design smells on your class to test.<br>
This separation includes test suites that are called <code>AllBehaviorTests.java</code>. They are organized in the same way of the others JUnit state test suites. For further details go to the <a href='http://code.google.com/p/econference4/wiki/HowToWriteJunitTestCases'>JUnit Test Cases wiki page</a>.</p>

## Behavior Test Structure ##

Usually you will need to initialize your System Under Test class in the `setUp()` method. The `@Before` annotation will make you sure that it will be executed before the tests.
In every test just follow the four steps described below:

  * Setup: create your Test Doubles, define your stubbed methods and install them into the SUT.
  * Exercise: run the SUT method that will start the behavior.
  * Verify: check method invocations.
  * Teardown: teardown is done automatically by the garbage collector.

```
import org.junit.*;
import static org.mockito.Mockito.*;
     
public class ClassBehaviorTest {
  private ClassToTest sut;
     
  @Before
  public void setUp(){
    sut = new ClassToTest();
  }
   
  @Test
  public void testBehavior1(){
    // Setup - Creation
    DependencyClass1 dep1 = mock(DependencyClass1.class);
    DependencyClass2 dep2 = mock(DependencyClass2.class);
    // Setup - Stubbing
    when(dep1.someMethod("arg")).thenReturn("some value");
    // Setup - Installation
    sut.setDependency1(dep1);
    sut.setDependency2(dep2);
    // Exercise
    sut.someMethod();
    // Verify
    verify(dep2).someMethod("some argument"); 
  }
     
  @Test
  public void testBehavior2(){
    ...
  }
}
```

## How to launch a Behavior Test ##

<p>Usually tests are executed clicking on "Run as" -> "JUnit Test" run configuration. In Behavior tests you may need to use a different run configuration because your tested code will probably access directly to the other plugins. To do this click on "Run as" -> "JUnit Plugin Test". This makes JUnit start all the plugins when needed during the Exercise step of your test.</p>
<p>For example, if your code uses the <code>PlatformUI</code> class directly to get back the <code>Workbench</code> component, JUnit will initialize a dummy workbench to make your code run correctly:</p>

<p align='center'>
<img src='http://econference4.googlecode.com/svn/wiki/img/mock/junit-plugin-test-execution.jpg' /></p>
<p align='center'><b>Figure 10. JUnit Plugin Test execution</b></p>

When test execution ends, all the plugin started will be disposed automatically.

# References #

<p><b>Result Verification Patterns, Test Double Patterns, Design-for-Testability Patterns:</b>
Gerard Meszaros, "xUnit Test Patterns Refactoring Test Code", The Addison-Wesley, 1<sup>st</sup> ed., Massachusetts, USA, 2007.</p>

<p><b>Eclipse RCP:</b>
Jeff McAffer, Jean-Michel Lemieux, Chris Aniszczyk, "Eclipse Rich Client Platform", The Addison Wesley, 2<sup>nd</sup> ed., Indiana, USA, 2010.</p>

<p><b>Mockito framework:</b>
Mockito - simpler & better mocking, home page, last access: 14/04/2010, <a href='http://code.google.com/p/mockito/'>http://code.google.com/p/mockito/</a>.</p>

<p><b>JUnit framework:</b> JUnit.org Resources for Test Driver Development, home page, last access: 14/04/2010, <a href='http://www.junit.org/'>http://www.junit.org/</a>.<br>
</p>