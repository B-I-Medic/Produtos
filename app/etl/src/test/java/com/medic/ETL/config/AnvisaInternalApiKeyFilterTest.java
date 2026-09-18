package com.medic.ETL.config;

import com.medic.ETL.config.property.AnvisaProperties;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AnvisaInternalApiKeyFilterTest {

    @Test
    void shouldNotFilterRequestsOutsideTheInternalAnvisaEndpoint() throws Exception {

        AnvisaProperties properties = new AnvisaProperties();
        AnvisaInternalApiKeyFilter filter = new AnvisaInternalApiKeyFilter(properties);
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = request("/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldReturnServiceUnavailableWhenTheApiKeyIsNotConfigured() throws Exception {

        AnvisaProperties properties = new AnvisaProperties();
        AnvisaInternalApiKeyFilter filter = new AnvisaInternalApiKeyFilter(properties);

        MockHttpServletResponse response = invoke(filter, "/internal/anvisa/atualizacoes", null);

        assertEquals(503, response.getStatus());
        assertEquals("API interna da Anvisa nao configurada", response.getErrorMessage());
    }

    @Test
    void shouldReturnUnauthorizedForMissingOrInvalidApiKey() throws Exception {

        AnvisaProperties properties = new AnvisaProperties();
        properties.setInternalApiKey("expected");
        AnvisaInternalApiKeyFilter filter = new AnvisaInternalApiKeyFilter(properties);

        assertEquals(401, invoke(filter, "/internal/anvisa/atualizacoes", null).getStatus());
        assertEquals(401, invoke(filter, "/internal/anvisa/atualizacoes", "wrong").getStatus());
    }

    @Test
    void shouldContinueWhenTheApiKeyMatches() throws Exception {

        AnvisaProperties properties = new AnvisaProperties();
        properties.setInternalApiKey("expected");
        AnvisaInternalApiKeyFilter filter = new AnvisaInternalApiKeyFilter(properties);
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest request = request("/internal/anvisa/atualizacoes");
        request.addHeader("X-Internal-Api-Key", "expected");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    private MockHttpServletResponse invoke(AnvisaInternalApiKeyFilter filter,
                                           String uri,
                                           String apiKey) throws Exception {

        MockHttpServletRequest request = request(uri);
        if (apiKey != null) {
            request.addHeader("X-Internal-Api-Key", apiKey);
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, mock(FilterChain.class));
        return response;
    }

    private MockHttpServletRequest request(String uri) {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        return request;
    }
}
