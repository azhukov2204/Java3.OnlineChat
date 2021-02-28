import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import methods2test.Homework6Main;

import java.util.stream.Stream;


public class TestMethods {
    private Homework6Main homework6Main;

    @BeforeAll
    static void greeting() {
        System.out.println("Домашняя работа №6. JUnit. Начинаем тесты методов");
    }

    @AfterAll
    static void endingTests() {
        System.out.println("Тестирование методов завершено");
    }

    //Перед каждым тестом создаем экземпляр класса:
    @BeforeEach
    void init() {
        homework6Main = new Homework6Main();
    }

    //------------------------------------------------------------------------------------------
    //Проверки метода checkArrayOn1And4:

    @ParameterizedTest
    @MethodSource("dataForCheckArrayOn1And4_ExpectedFalse")
    void testCheckArrayOn1And4_ExpectedFalse(int[] arr) {
        Assertions.assertFalse(homework6Main.checkArrayOn1And4(arr));
    }

    @ParameterizedTest
    @MethodSource("dataForCheckArrayOn1And4_ExpectedTrue")
    void testCheckArrayOn1And4_ExpectedTrue(int[] arr) {
        Assertions.assertTrue(homework6Main.checkArrayOn1And4(arr));

    }

    static Stream<Arguments> dataForCheckArrayOn1And4_ExpectedFalse() {
        return Stream.of(
                Arguments.arguments(new int[]{0, 1, 3, 1}),
                Arguments.arguments(new int[]{1, 1}),
                Arguments.arguments(new int[]{4, 4, 0, 0}),
                Arguments.arguments(new int[]{0, 2, 3}),
                Arguments.arguments(new int[]{4, 4, 4}),
                Arguments.arguments(new int[]{1}),
                Arguments.arguments(new int[]{4}),
                Arguments.arguments(new int[]{})
        );
    }

    static Stream<Arguments> dataForCheckArrayOn1And4_ExpectedTrue() {
        return Stream.of(
                Arguments.arguments(new int[]{1, 1, 4, 4}),
                Arguments.arguments(new int[]{1, 4}),
                Arguments.arguments(new int[]{4, 1}),
                Arguments.arguments(new int[]{4, 4, 1, 1}),
                Arguments.arguments(new int[]{4, 1, 5, 6}),
                Arguments.arguments(new int[]{0, 1, 2, 3, 4, 1, 4}));
    }


    //------------------------------------------------------------------------------------------
    //Проверки метода getElementsAfterLast4:

    @ParameterizedTest
    @MethodSource("dataForGetElementsAfterLast4_ExpectedException")
    void testGetElementsAfterLast4_ExpectedException(int[] arr) {
        Assertions.assertThrows(RuntimeException.class, () -> homework6Main.getElementsAfterLast4(arr));
    }

    @ParameterizedTest
    @MethodSource("dataForGetElementsAfterLast4_ExpectedCorrectResult")
    void GetElementsAfterLast4_ExpectedCorrectResult(int[] sourceArray, int[] expectedArray) {
        Assertions.assertArrayEquals(expectedArray, homework6Main.getElementsAfterLast4(sourceArray));
    }


    static Stream<Arguments> dataForGetElementsAfterLast4_ExpectedException() {
        return Stream.of(
                Arguments.arguments(new int[]{0, 1, 2, 3, 5}),
                Arguments.arguments(new int[]{}),
                Arguments.arguments(new int[]{3}));
    }

    static Stream<Arguments> dataForGetElementsAfterLast4_ExpectedCorrectResult() {
        return Stream.of(
                Arguments.arguments(new int[]{0, 1, 2, 3, 4}, new int[]{}),
                Arguments.arguments(new int[]{1, 2, 4, 4, 2, 3, 4, 1, 7,}, new int[]{1, 7}),
                Arguments.arguments(new int[]{3, 4, 0}, new int[]{0}));
    }

}
