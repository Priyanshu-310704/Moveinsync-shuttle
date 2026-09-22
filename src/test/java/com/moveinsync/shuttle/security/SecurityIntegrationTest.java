package com.moveinsync.shuttle.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectedEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(
                        get("/api/trips/1/availability")
                                .param("fromOrder", "0")
                                .param("toOrder", "2")
                )
                .andExpect(status().isUnauthorized());
    }

//    @Test
//    void validCredentialsAllowAccessToProtectedEndpoint() throws Exception {
//        mockMvc.perform(
//                        post("/api/auth/register")
//                                .contentType("application/json")
//                                .content("""
//                            {
//                              "email": "valid-auth@example.com",
//                              "password": "password123"
//                            }
//                            """)
//                )
//                .andExpect(status().isOk());
//
//        mockMvc.perform(
//                        get("/api/trips/1/availability")
//                                .param("fromOrder", "0")
//                                .param("toOrder", "2")
//                                .with(httpBasic("valid-auth@example.com", "password123"))
//                )
//                .andExpect(status().isOk());
//    }

    @Test
    void invalidCredentialsAreRejected() throws Exception {
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType("application/json")
                                .content("""
                            {
                              "email": "invalid-auth@example.com",
                              "password": "password123"
                            }
                            """)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/trips/1/availability")
                                .param("fromOrder", "0")
                                .param("toOrder", "2")
                                .with(httpBasic("invalid-auth@example.com", "wrongpassword"))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationCreatesUser() throws Exception {
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType("application/json")
                                .content("""
                            {
                              "email": "registration-test@example.com",
                              "password": "password123"
                            }
                            """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("registration-test@example.com"))
                .andExpect(jsonPath("$.status").value("REGISTERED"));
    }


    @Test
    void actuatorEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}