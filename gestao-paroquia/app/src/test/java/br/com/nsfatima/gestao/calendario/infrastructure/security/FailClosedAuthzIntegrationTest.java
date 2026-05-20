package br.com.nsfatima.gestao.calendario.infrastructure.security;

import java.util.UUID;
import br.com.nsfatima.gestao.iam.infrastructure.security.ExternalMembershipReader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class FailClosedAuthzIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExternalMembershipReader externalMembershipReader;

    @Test
    void shouldReturnServiceUnavailableWhenAuthorizationSourceFails() throws Exception {
        when(externalMembershipReader.findActiveMemberships(any(UUID.class)))
                .thenThrow(new CannotAcquireLockException("db unavailable"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"username\": \"joao.silva\",
                                  \"password\": \"senha123\"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorCode").value("AUTHZ_SOURCE_UNAVAILABLE"));
    }
}
