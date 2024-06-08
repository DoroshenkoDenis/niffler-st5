package guru.qa.niffler.data.repository;

import guru.qa.niffler.data.DataBase;
import guru.qa.niffler.data.entity.CategoryEntity;
import guru.qa.niffler.data.entity.SpendEntity;
import guru.qa.niffler.data.jdbc.DataSourceProvider;
import guru.qa.niffler.model.CurrencyValues;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class SpendRepositoryJdbc implements SpendRepository {

    private static final DataSource dateSource = DataSourceProvider.dataSource(DataBase.SPEND);

    //Метод создает новую категорию в базе данных. Он принимает объект CategoryEntity и возвращает созданную категорию.
    @Override
    public CategoryEntity createCategory(CategoryEntity category) {
        try (Connection conn = dateSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO \"category\" (category, username) VALUES (?, ?)",
                     RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getCategory());
            ps.setString(2, category.getUsername());
            ps.executeUpdate();

            UUID generatedId;
            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next()) {
                    generatedId = UUID.fromString(resultSet.getString("id"));
                } else {
                    throw new IllegalStateException("Can`t access to id");
                }
            }
            category.setId(generatedId);
            return category;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Обновляет существующую категорию в базе данных
    @Override
    public CategoryEntity editCategory(CategoryEntity category) {
        try (Connection conn = dateSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE \"category\" SET category = ?, username = ?" +
                     " WHERE id = ?")) {
            ps.setString(1, category.getCategory());
            ps.setString(2, category.getUsername());
            ps.setObject(3, category.getId());
            ps.executeUpdate();
            return category;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeCategory(CategoryEntity category) {
        try (Connection conn = dateSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM \"category\" WHERE id = ?")) {
            ps.setObject(1, category.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SpendEntity createSpend(SpendEntity spend) {
        String request =
                "INSERT INTO \"spend\" (username, currency, spend_date, amount, description, category_id)" +
                        " VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dateSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(request, RETURN_GENERATED_KEYS)) {
            ps.setString(1, spend.getUsername());
            ps.setString(2, String.valueOf(spend.getCurrency()));

            // Преобразуем java.util.Date в java.sql.Date
            Date spendDate = new Date(spend.getSpendDate().getTime());
            ps.setDate(3, spendDate);

            ps.setDouble(4, spend.getAmount());
            ps.setString(5, spend.getDescription());
            ps.setObject(6, getCategoryId(spend.getCategory()));

            ps.executeUpdate();

            UUID generatedId;
            try (ResultSet resultSet = ps.getGeneratedKeys()) {
                if (resultSet.next()) {
                    generatedId = UUID.fromString(resultSet.getString("id"));
                } else {
                    throw new IllegalStateException("Can`t access to id");
                }
            }
            spend.setId(generatedId);
            return spend;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SpendEntity editSpend(SpendEntity spend) {
        try (Connection conn = dateSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE \"spend\" SET username = ?, currency = ?," +
                     " spend_date = ?, amount = ?, description = ?, category_id = ? WHERE id = ?")) {
            ps.setString(1, spend.getUsername());
            ps.setString(2, String.valueOf(spend.getCurrency()));

            // Преобразуем java.util.Date в java.sql.Date
            Date spendDate = new Date(spend.getSpendDate().getTime());
            ps.setDate(3, spendDate);

            ps.setDouble(4, spend.getAmount());
            ps.setString(5, spend.getDescription());
            ps.setObject(6, getCategoryId(spend.getCategory()));
            ps.setObject(7, getSpendIdByCategoryId(getCategoryId(spend.getCategory())));
            ps.executeUpdate();
            return spend;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeSpend(SpendEntity spend) {
        try (Connection conn = dateSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM \"spend\" WHERE id = ?")) {
            ps.setObject(1, spend.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public UUID getCategoryId(String categoryName) {
        try (Connection conn = dateSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM category WHERE category = ?")) {
            ps.setString(1, categoryName);
            ps.execute();

            try (ResultSet resultSet = ps.getResultSet()) {
                if (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("id"));
                } else {
                    throw new IllegalStateException("Can`t access to id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public UUID getSpendIdByCategoryId(UUID catId) {
        try (Connection conn = dateSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM spend WHERE category_id = ?")) {
            ps.setObject(1, catId);
            ps.execute();

            try (ResultSet resultSet = ps.getResultSet()) {
                if (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("id"));
                } else {
                    throw new IllegalStateException("Can`t access to id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Возвращает список расходов для указанного пользователя.
    @Override
    public Optional<List<SpendEntity>> findAllSpendsByUsername(String username) {
        try (Connection conn = dateSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     """
                             SELECT * FROM "spend" where username = ?
                             """)) {
            ps.setString(1, username);
            ps.executeQuery();

            List<SpendEntity> spendList = new ArrayList<>();

            try (ResultSet resultSet = ps.getResultSet()) {
                while (resultSet.next()) {
                    SpendEntity spend = new SpendEntity();

                    spend.setId(resultSet.getObject("id", UUID.class));
                    spend.setUsername(resultSet.getString("username"));
                    spend.setSpendDate(resultSet.getDate("spend_date"));
                    spend.setCurrency(CurrencyValues.valueOf(resultSet.getString("currency")));
                    spend.setAmount(resultSet.getDouble("amount"));
                    spend.setDescription(resultSet.getString("description"));
                    spend.setCategory(resultSet.getString("category_id"));

                    spendList.add(spend);
                }
            }

            return Optional.of(spendList);
        } catch (SQLException e) {
            return Optional.empty();
        }
    }
}
