package guru.qa.niffler.data.repository;

import guru.qa.niffler.data.entity.UserAuthEntity;
import guru.qa.niffler.data.entity.UserEntity;

import java.util.Optional;
import java.util.UUID;

// Интерфейс UserRepository определяет методы для работы с пользователями в базе данных
public interface UserRepository {
    // Статический метод getInstance() возвращает экземпляр UserRepository
    // в зависимости от значения системного свойства "repo"
    static UserRepository getInstance() {
        // Если свойство "repo" равно "sjdbс", возвращается экземпляр UserRepositorySpringJdbc
        // Если свойство "repo" равно "hibernate", возвращается экземпляр UserRepositoryHibernate
        // Если ни одно из предыдущих условий не выполнено, возвращается экземпляр UserRepositoryJdbc
        return switch (System.getProperty("repo")) {
            case "sjdbс" -> new UserRepositorySpringJdbc();
            case "hibernate" -> new UserRepositoryHibernate();
            default -> new UserRepositoryJdbc();
        };
    }

    // Метод createUserInAuth создает нового пользователя в таблице аутентификации
    // и принимает в качестве аргумента объект UserAuthEntity
    UserAuthEntity createUserInAuth(UserAuthEntity user);

    // Метод createUserInUserData создает нового пользователя в таблице данных пользователя
    // и принимает в качестве аргумента объект UserEntity
    UserEntity createUserInUserData(UserEntity user);

    // Метод findUserInAuth ищет пользователя в таблице аутентификации по его имени
    Optional<UserAuthEntity> findUserInAuth(String userName);

    // Метод findUserInUserDataById ищет пользователя в таблице данных пользователя по его идентификатору
    Optional<UserEntity> findUserInUserDataById(UUID id);

    // Метод findUserInUserData ищет пользователя в таблице данных пользователя по его имени
    Optional<Object> findUserInUserData(String userName);

    // Метод updateUserInAuth обновляет информацию о пользователе в таблице аутентификации
    UserAuthEntity updateUserInAuth(UserAuthEntity user);

    // Метод updateUserInUserdata обновляет информацию о пользователе в таблице данных пользователя
    UserEntity updateUserInUserdata(UserEntity user);
}