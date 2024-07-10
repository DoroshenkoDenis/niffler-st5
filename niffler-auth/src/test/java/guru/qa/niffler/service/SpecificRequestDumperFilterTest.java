package guru.qa.niffler.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.GenericFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpecificRequestDumperFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;
    @Mock
    private GenericFilter decorate;

    private SpecificRequestDumperFilter filter;

    @BeforeEach
    void setup() {
        // добавляем шаблоны в декоратор
        filter = new SpecificRequestDumperFilter(decorate, "/login", "/oauth2/.*");
    }

    /**
     * Проверяет, что фильтр вызывает `decorate.doFilter()`, когда запрос соответствует
     * одному из указанных URL-шаблонов.
     *
     * @throws IOException
     * @throws ServletException
     */
    @Test
    void doFilterMatchingUrl() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/login");

        filter.doFilter(request, response, chain);

        verify(decorate).doFilter(request, response, chain); // equivalent to verify(mock, times(1)).someMethod("some arg");
    }

    /**
     * Проверяет, что фильтр передает запрос дальше по цепочке фильтров без применения декорированного фильтра decorate,
     * когда URL запроса не соответствует ни одному из шаблонов
     *
     * @throws IOException
     * @throws ServletException
     */
    @Test
    void doFilterNonMatchingUrl() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/oauth3/.*");

        filter.doFilter(request, response, chain);

        verifyNoInteractions(decorate); // Verifies that no interactions happened on given mocks.
        verify(chain).doFilter(request, response);
    }

    /**
     * Проверяет, что фильтр передает запрос дальше по цепочке фильтров  без применения декорированного фильтра decorate,
     * когда запрос не является экземпляром `HttpServletRequest`
     *
     * @throws IOException
     * @throws ServletException
     */
    @Test
    void doFilterNonHttpServletRequest() throws IOException, ServletException {
        ServletRequest nonInstanceofHttpServletRequest = new MockHttpServletRequest();

        filter.doFilter(nonInstanceofHttpServletRequest, response, chain);

        verifyNoInteractions(decorate);
        verify(chain).doFilter(nonInstanceofHttpServletRequest, response);
    }

    /**
     * Проверяет, что фильтр вызывает `decorate.destroy()` при уничтожении
     */
    @Test
    void destroy() {
        filter.destroy();
        verify(decorate).destroy();
    }
}
