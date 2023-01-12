package integrationtest;

import com.syberry.bakery.BakeryApplication;
import com.syberry.bakery.config.MailConfig;
import com.syberry.bakery.service.EmailService;
import integrationtest.config.H2Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {BakeryApplication.class, H2Config.class})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class UserIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MailConfig mailConfig;
    @MockBean
    private EmailService emailService;

    @Test
    @WithMockUser(username = "admin@mail.com", roles = {"ADMIN", "HR"})
    void should_GetAllUsers() throws Exception {
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1));
    }

    @Test
    @WithMockUser(username = "admin@mail.com", roles = {"ADMIN", "HR"})
    void should_GetUserById() throws Exception {
        final File jsonFile = new ClassPathResource("json/create-user.json").getFile();
        final String userToCreate = Files.readString(jsonFile.toPath());
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userToCreate))
                .andDo(print())
                .andExpect(status().isCreated());
        mockMvc.perform(get("/users/1").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "admin@mail.com", roles = {"ADMIN"})
    void should_CreateUser() throws Exception {
        final File jsonFile = new ClassPathResource("json/create-user.json").getFile();
        final String userToCreate = Files.readString(jsonFile.toPath());
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userToCreate))
                .andDo(print()).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(username = "admin@mail.com", roles = {"ADMIN"})
    void should_UpdateUser() throws Exception {
        File jsonFile = new ClassPathResource("json/create-user.json").getFile();
        String userToUpdate = Files.readString(jsonFile.toPath());
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userToUpdate))
                .andDo(print()).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
        jsonFile = new ClassPathResource("json/update-user.json").getFile();
        userToUpdate = Files.readString(jsonFile.toPath());
        mockMvc.perform(put("/users/1").contentType(MediaType.APPLICATION_JSON).content(userToUpdate))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("updated"));
    }

    @Test
    @WithMockUser(username = "admin@mail.com", roles = {"ADMIN"})
    void should_DisableUser() throws Exception {
        File jsonFile = new ClassPathResource("json/create-user.json").getFile();
        String userToUpdate = Files.readString(jsonFile.toPath());
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userToUpdate))
                .andDo(print()).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
        mockMvc.perform(delete("/users/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/users/1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
