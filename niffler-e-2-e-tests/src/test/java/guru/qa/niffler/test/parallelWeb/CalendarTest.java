package guru.qa.niffler.test.parallelWeb;

import guru.qa.niffler.test.BaseWebTest;
import io.qameta.allure.Epic;
import org.junit.jupiter.api.*;

import java.time.LocalDate;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic("Календарь")
@Tag("ParallelWebTests")
public class CalendarTest extends BaseWebTest {

    @Test
    @DisplayName("Работа календаря при выборе даты расхода")
    void dateSelectionTest() {
        open(CFG.frontUrl());
        welcomePage.goToLogin();
        loginPage.login("dima", "12345");

        // Получаем текущую дату
        LocalDate currentDate = LocalDate.now();
        // Добавляем 1 год, 5 месяца и 16 дней к текущей дате
        LocalDate futureDate = currentDate.plusYears(1).plusMonths(5).plusDays(16);
        // Убавляем 1 год, 2 месяца и 16 дней от текущей даты
        LocalDate pastDate = currentDate.minusYears(1).minusMonths(2).minusDays(2);

        mainPage.getReactCalendar().selectDate(futureDate);
        assertEquals(
                futureDate.toString(),
                mainPage.getReactCalendar().getSelectedDate());

        mainPage.getReactCalendar().selectDate(pastDate);
        assertEquals(
                pastDate.toString(),
                mainPage.getReactCalendar().getSelectedDate());

        mainPage.getReactCalendar().selectDate(currentDate);
        assertEquals(
                currentDate.toString(),
                mainPage.getReactCalendar().getSelectedDate());
    }
}
