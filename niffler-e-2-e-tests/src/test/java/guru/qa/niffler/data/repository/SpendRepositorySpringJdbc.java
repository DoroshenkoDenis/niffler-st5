package guru.qa.niffler.data.repository;

import guru.qa.niffler.data.DataBase;
import guru.qa.niffler.data.entity.CategoryEntity;
import guru.qa.niffler.data.entity.SpendEntity;
import guru.qa.niffler.data.jdbc.DataSourceProvider;
import guru.qa.niffler.data.sjdbc.SpendEntityRowMapper;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class SpendRepositorySpringJdbc implements SpendRepository {

    private final static JdbcTemplate jdbcTemplate = new JdbcTemplate(
            DataSourceProvider.dataSource(DataBase.SPEND));

    @Override
    public CategoryEntity createCategory(CategoryEntity category) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO category (category, username) VALUES (?, ?)",
                            PreparedStatement.RETURN_GENERATED_KEYS
                    );
                    ps.setString(1, category.getCategory());
                    ps.setString(2, category.getUsername());
                    return ps;
                }, kh
        );
        category.setId((UUID) kh.getKeys().get("id"));
        return category;
    }

    @Override
    public CategoryEntity editCategory(CategoryEntity category) {
        jdbcTemplate.update(
                "UPDATE category SET category = ?, username = ? WHERE id = ?",
                category.getCategory(),
                category.getUsername(),
                category.getId()
        );
        return category;
    }

    @Override
    public void removeCategory(CategoryEntity category) {
        jdbcTemplate.update(
                "DELETE FROM category WHERE id = ?",
                category.getId()
        );
    }

    @Override
    public SpendEntity createSpend(SpendEntity spend) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO spend (username, currency, spend_date, amount, description, category_id)" +
                                    " VALUES (?, ?, ?, ?, ?, ?)",
                            PreparedStatement.RETURN_GENERATED_KEYS
                    );
                    ps.setString(1, spend.getUsername());
                    ps.setString(2, spend.getCurrency().name());
                    ps.setDate(3, new Date(spend.getSpendDate().getTime()));
                    ps.setDouble(4, spend.getAmount());
                    ps.setString(5, spend.getDescription());
                    ps.setObject(6, categoryId(spend.getCategory()));
                    return ps;
                }, kh
        );
        spend.setId((UUID) Objects.requireNonNull(kh.getKeys()).get("id"));
        return spend;
    }

    @Override
    public SpendEntity editSpend(SpendEntity spend) {
        jdbcTemplate.update(
                "UPDATE spend SET username = ?, currency = ?," +
                        " spend_date = ?, amount = ?, description = ?, category_id = ? WHERE id = ?",
                spend.getUsername(),
                spend.getCurrency().name(),
                new Date(spend.getSpendDate().getTime()),
                spend.getAmount(),
                spend.getDescription(),
                spend.getCategory(),
                spend.getId()
        );
        return spend;
    }

    @Override
    public void removeSpend(SpendEntity spend) {
        jdbcTemplate.update(
                "DELETE FROM spend WHERE id = ?",
                spend.getId()
        );
    }

    public UUID categoryId(String categoryName) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM category WHERE category = ?",
                    UUID.class,
                    categoryName);
        } catch (EmptyResultDataAccessException e) {
            // Обрабатываем ситуацию, когда категории не найдены
            return null; // или возвращаем ошибку, если это необходимо
        }
    }


    //В этом примере Optional используется для безопасного обхода ситуации, когда запрос к базе данных может не вернуть результат.
    //Если запрос успешно выполнен, то возвращается Optional с результатом. Если возникла ошибка, то возвращается пустой Optional.
    @Override
    public Optional<List<SpendEntity>> findAllSpendsByUsername(String username) {
        try {
            List<SpendEntity> spendEntities = jdbcTemplate.query(
                    """
                            SELECT * FROM "spend" WHERE username = ?
                            """,
                    SpendEntityRowMapper.instance,
                    username
            );

            return Optional.of(spendEntities);
        } catch (DataRetrievalFailureException e) {
            // В случае ошибки при извлечении данных возвращаем Optional.empty()
            return Optional.empty();
        }
    }
}
