package br.com.nsfatima.gestao.calendario.support;

import jakarta.servlet.http.Cookie;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class SecurityTestSupport {

    private SecurityTestSupport() {
    }

    public static MockHttpSession loginSession(MockMvc mockMvc, String username, String password) throws Exception {
        MvcResult result = performLogin(mockMvc, username, password);
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    public static Cookie login(MockMvc mockMvc, String username, String password) throws Exception {
        MvcResult result = performLogin(mockMvc, username, password);
        Cookie sessionCookie = result.getResponse().getCookie("JSESSIONID");
        if (sessionCookie == null && result.getRequest().getSession(false) != null) {
            sessionCookie = new Cookie("JSESSIONID", result.getRequest().getSession(false).getId());
        }
        assertNotNull(sessionCookie, "Expected JSESSIONID cookie after successful login");
        return sessionCookie;
    }

    private static MvcResult performLogin(MockMvc mockMvc, String username, String password) throws Exception {
        String payload = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        return mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();
    }
}
