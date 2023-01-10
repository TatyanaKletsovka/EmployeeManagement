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
public class EmployeeIntegrationTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    MailConfig mailConfig;
    @MockBean
    EmailService emailService;

    @Test
    @WithMockUser(username = "admin@mail.com", roles = {"ADMIN"})
    void should_GetAllEmployees() throws Exception {
        mockMvc.perform(get("/employees"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()")
                        .value(0));
    }

    @Test
    @WithMockUser(username = "admin@mail.com", roles = {"ADMIN"})
    void should_GetEmployeeById() throws Exception {
        final File jsonFile = new ClassPathResource("json/create-user.json").getFile();
        final String userToCreate = Files.readString(jsonFile.toPath());
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userToCreate)).andDo(print());

        final File jsonFileEmp = new ClassPathResource("json/create-employee.json").getFile();
        final String empToCreate = Files.readString(jsonFileEmp.toPath());
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empToCreate))
                .andDo(print())
                .andExpect(status().isCreated());
        mockMvc.perform(get("/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "admin@mail.com", roles = {"ADMIN"})
    void should_CreateEmployee() throws Exception {
        final File jsonFile = new ClassPathResource("json/create-user.json").getFile();
        final String userToCreate = Files.readString(jsonFile.toPath());
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userToCreate)).andDo(print());
        final File jsonFileEmp = new ClassPathResource("json/create-employee.json").getFile();
        final String empToCreate = Files.readString(jsonFileEmp.toPath());
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empToCreate))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithMockUser(username = "admin@mail.com", roles = {"ADMIN"})
    void should_UpdateEmployee() throws Exception {
        final File jsonFile = new ClassPathResource("json/create-user.json").getFile();
        final String userToCreate = Files.readString(jsonFile.toPath());
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userToCreate)).andDo(print());

        File jsonFileEmp = new ClassPathResource("json/create-employee.json").getFile();
        String empToUpdate = Files.readString(jsonFileEmp.toPath());
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(empToUpdate))
                .andDo(print());

        jsonFileEmp = new ClassPathResource("json/update-employee.json").getFile();
        empToUpdate = Files.readString(jsonFileEmp.toPath());
        mockMvc.perform(put("/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(empToUpdate)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("updated"));
    }

    @Test
    @WithMockUser(username = "admin@mail.com", roles = {"ADMIN"})
    void should_DisableEmployee() throws Exception {
        final File jsonFile = new ClassPathResource("json/create-user.json").getFile();
        final String userToCreate = Files.readString(jsonFile.toPath());
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userToCreate)).andDo(print());
        final File jsonFileEmp = new ClassPathResource("json/create-employee.json").getFile();
        final String empToCreate = Files.readString(jsonFileEmp.toPath());
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empToCreate))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
        mockMvc.perform(delete("/employees/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/employees/1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
