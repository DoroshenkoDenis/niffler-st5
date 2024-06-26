package guru.qa.niffler.jupiter.extension;

import guru.qa.niffler.api.SpendApiClient;
import guru.qa.niffler.jupiter.annotation.Spend;
import guru.qa.niffler.model.CategoryJson;
import guru.qa.niffler.model.SpendJson;
import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ApiSpendExtension extends AbstractSpendExtension {

    private final SpendApiClient spendApiClient = new SpendApiClient();

    @Override
    @SneakyThrows
    protected SpendJson createSpend(SpendJson spend) {
        return spendApiClient.createSpend(spend);
    }

    @Override
    @SneakyThrows
    protected SpendJson createSpend(ExtensionContext extensionContext, Spend spend, CategoryJson category) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SneakyThrows
    protected void removeSpend(SpendJson spend) {
        spendApiClient.removeSpends(spend.username(), String.valueOf(spend.id()));
    }

}
