package guru.qa.niffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.ElementNotFound;
import com.codeborne.selenide.selector.ByText;
import guru.qa.niffler.page.component.ReactCalendar;
import guru.qa.niffler.page.component.SpendingsTable;
import io.qameta.allure.Step;
import lombok.Getter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainPage extends BasePage<MainPage> {

    public static final String URL = CFG.frontUrl() + "main";

    private final ElementsCollection spendings = $(".spendings-table tbody").$$("tr");
    private final SelenideElement progressBar = $("div[role='progressbar']");
    private final SelenideElement deleteAllSelectedBtn = $(".spendings__bulk-actions button");
    private final SelenideElement categoryDropdown =
            $x("//div[contains(text(), 'Choose spending category')]/parent::div/following-sibling::div");

    private final SelenideElement addSpendingSection = $(".main-content__section-add-spending");
    private final SelenideElement spendingHistorySection = $(".main-content__section-history");

    @Getter
    private final ReactCalendar reactCalendar = new ReactCalendar(addSpendingSection.$(".react-datepicker"));

    @Getter
    protected final SpendingsTable spendingsTable = new SpendingsTable($(".spendings-table tbody"));

    @Step("Ожидание загрузки страницы")
    @Override
    public MainPage waitForPageLoaded() {
        spendingHistorySection.should(visible);
        return this;
    }

    @Step("Выбрать строку расходов по описанию")
    public MainPage selectSpendingByDescription(String description) {
        SelenideElement rowWithSpending = spendings.find(text(description));
        rowWithSpending.$("td").scrollIntoView(true).click();
        return this;
    }

    @Step("Удалить выбранные расходы")
    public MainPage deleteSpending() {
        deleteAllSelectedBtn.click();
        progressBar.should(not(visible), Duration.ofSeconds(10));
        return this;
    }

    @Step("Проверка видимости строки в таблице расходов по description расхода")
    public boolean checkRow(String description) {
        return spendings.find(text(description)).exists();
    }

    @Step("Проверка наличия категории")
    public boolean checkCategory(String value) {
        try {
            categoryDropdown.click();
            $(new ByText(value)).click();
            return true;
        } catch (ElementNotFound exception) {
            throw new RuntimeException("Category not found");
        }
    }

    @Step("Проверка отображаемого списка трат")
    public void checkSpendingsList(List<String> spends) {

        List<String> spendDescriptions = new ArrayList<>();

        for (String entry : spendingsTable.getAllSpendings()) {
            String[] parts = entry.split("\n");
            if (parts.length >= 5) {
                spendDescriptions.add(parts[4]);
            }
        }

        assertTrue(spendDescriptions.containsAll(spends),
                "Список трат в таблице должен совпадать с добавленными тратами");
    }

}