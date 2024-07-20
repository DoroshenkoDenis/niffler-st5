package guru.qa.niffler.service;

import guru.qa.niffler.data.Authority;
import guru.qa.niffler.data.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * В этом тесте мы создаем экземпляр UserRepository.FakeUserRepository в методе setup() и передаем его в конструктор NifflerUserDetailsService.
 * Fake реализация возвращает предопределенный UserEntity для пользователя с именем "correct" и выбрасывает UsernameNotFoundException для всех остальных имен пользователей.
 * Тесты loadUserByUsername() и loadUserByUsernameNegative() остались практически без изменений, за исключением того, что мы больше не используем Mockito для настройки поведения репозитория.
 * Преимущества использования Fake реализации:
 * Тесты становятся более независимыми от реальной реализации репозитория, что делает их более устойчивыми к изменениям.
 * Fake реализация может быть легко расширена для покрытия различных сценариев тестирования без необходимости использования Mockito.
 * Fake реализация может быть повторно использована в других тестах, где требуется имитация поведения репозитория.
 * Таким образом, использование Fake реализации вместо Mockito делает тесты более читаемыми, поддерживаемыми и гибкими
 */
@ExtendWith(MockitoExtension.class)
class NifflerUserDetailsServiceTest {

    //****************************** Fake-реализация ********************************************//
    private NifflerUserDetailsService nifflerUserDetailsService;
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository = new UserRepository.FakeUserRepository();
        nifflerUserDetailsService = new NifflerUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsernameFake() {
        final UserDetails correct = nifflerUserDetailsService.loadUserByUsername("correct");

        final List<SimpleGrantedAuthority> expectedAuthorities = List.of(
                new SimpleGrantedAuthority(Authority.read.name()),
                new SimpleGrantedAuthority(Authority.write.name())
        );

        assertEquals(
                "correct",
                correct.getUsername()
        );
        assertEquals(
                "test-pass",
                correct.getPassword()
        );
        assertEquals(
                expectedAuthorities,
                correct.getAuthorities()
        );

        assertTrue(correct.isAccountNonExpired());
        assertTrue(correct.isAccountNonLocked());
        assertTrue(correct.isCredentialsNonExpired());
        assertTrue(correct.isEnabled());
    }

    @Test
    void loadUserByUsernameNegativeFake() {
        final UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> nifflerUserDetailsService.loadUserByUsername("incorrect")
        );

        assertEquals(
                "Username: `incorrect` not found",
                exception.getMessage()
        );
    }

    //****************************** Mock-реализация ********************************************//
//    private UserEntity testUserEntity;
//    private List<AuthorityEntity> authorityEntities;
//
//    @BeforeEach
//    void initMockRepository(@Mock UserRepository userRepository) {
//        AuthorityEntity read = new AuthorityEntity();
//        read.setUser(testUserEntity);
//        read.setAuthority(Authority.read);
//        AuthorityEntity write = new AuthorityEntity();
//        write.setUser(testUserEntity);
//        write.setAuthority(Authority.write);
//        authorityEntities = List.of(read, write);
//
//        testUserEntity = new UserEntity();
//        testUserEntity.setUsername("correct");
//        testUserEntity.setAuthorities(authorityEntities);
//        testUserEntity.setEnabled(true);
//        testUserEntity.setPassword("test-pass");
//        testUserEntity.setAccountNonExpired(true);
//        testUserEntity.setAccountNonLocked(true);
//        testUserEntity.setCredentialsNonExpired(true);
//        testUserEntity.setId(UUID.randomUUID());
//
//        lenient().when(userRepository.findByUsername("correct"))
//                .thenReturn(Optional.of(testUserEntity));
//
//        lenient().when(userRepository.findByUsername(not(eq("correct"))))
//                .thenReturn(Optional.empty());
//
//        nifflerUserDetailsService = new NifflerUserDetailsService(userRepository);
//    }
//
//    @Test
//    void loadUserByUsername() {
//        final UserDetails correct = nifflerUserDetailsService.loadUserByUsername("correct");
//
//        final List<SimpleGrantedAuthority> expectedAuthorities = authorityEntities.stream()
//                .map(a -> new SimpleGrantedAuthority(a.getAuthority().name()))
//                .toList();
//
//        assertEquals(
//                "correct",
//                correct.getUsername()
//        );
//        assertEquals(
//                "test-pass",
//                correct.getPassword()
//        );
//        assertEquals(
//                expectedAuthorities,
//                correct.getAuthorities()
//        );
//
//        assertTrue(correct.isAccountNonExpired());
//        assertTrue(correct.isAccountNonLocked());
//        assertTrue(correct.isCredentialsNonExpired());
//        assertTrue(correct.isEnabled());
//    }
//
//    @Test
//    void loadUserByUsernameNegative() {
//        final UsernameNotFoundException exception = assertThrows(
//                UsernameNotFoundException.class,
//                () -> nifflerUserDetailsService.loadUserByUsername("incorrect")
//        );
//
//        assertEquals(
//                "Username: `incorrect` not found",
//                exception.getMessage()
//        );
//    }

}