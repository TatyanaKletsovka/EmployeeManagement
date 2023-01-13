package integrationtest;

import com.syberry.bakery.BakeryApplication;
import com.syberry.bakery.config.MailConfig;
import com.syberry.bakery.service.EmailService;
import integrationtest.config.H2Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
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
public class CompensationIntegrationTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    MailConfig mailConfig;
    @MockBean
    EmailService emailService;

    @BeforeEach
    void setup() throws Exception {
        //add users
        final File jsonFile = new ClassPathResource("json/create-user.json").getFile();
        final String userToCreate = Files.readString(jsonFile.toPath());
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userToCreate)).andDo(print());
        final File jsonFile2 = new ClassPathResource("json/create-user2.json").getFile();
        final String user2ToCreate = Files.readString(jsonFile2.toPath());
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(user2ToCreate)).andDo(print());
        final File jsonFileEmp = new ClassPathResource("json/create-employee.json").getFile();
        final String empToCreate = Files.readString(jsonFileEmp.toPath());
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(empToCreate))
                .andDo(print());
        final File jsonFileEmp2 = new ClassPathResource("json/create-employee2.json").getFile();
        final String emp2ToCreate = Files.readString(jsonFileEmp2.toPath());
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emp2ToCreate))
                .andDo(print());
        final File jsonFileComp = new ClassPathResource("json/create-compensation.json").getFile();
        final String compToCreate = Files.readString(jsonFileComp.toPath());
        mockMvc.perform(post("/compensations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compToCreate))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void should_GetAllCompensations() throws Exception {
        mockMvc.perform(get("/compensations"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()")
                        .value(1));
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void should_GetCompensationsByEmployeeId() throws Exception {
        mockMvc.perform(get("/compensations/employees/2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()")
                        .value(0));
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void should_GetCompensationById() throws Exception {
        mockMvc.perform(get("/compensations/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void should_CreateCompensation() throws Exception {
        final File jsonFileComp = new ClassPathResource("json/create-compensation2.json").getFile();
        final String compToCreate = Files.readString(jsonFileComp.toPath());
        mockMvc.perform(post("/compensations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compToCreate))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void should_ThrowError_When_CreateCompensationWithExistedDates() throws Exception {
        final File jsonFileComp = new ClassPathResource("json/create-compensation3.json").getFile();
        final String compToCreate = Files.readString(jsonFileComp.toPath());
        mockMvc.perform(post("/compensations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compToCreate))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void should_ThrowError_When_CreateCompensationWithInvalidDates() throws Exception {
        final File jsonFileComp = new ClassPathResource("json/create-compensation4.json").getFile();
        final String compToCreate = Files.readString(jsonFileComp.toPath());
        mockMvc.perform(post("/compensations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compToCreate))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void should_UpdateCompensation() throws Exception {
        final File jsonFileComp = new ClassPathResource("json/create-compensation3.json").getFile();
        final String compToCreate = Files.readString(jsonFileComp.toPath());
        mockMvc.perform(put("/compensations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compToCreate))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validUntil").value("2023-06-01"));
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void should_DisableCompensation() throws Exception {
        mockMvc.perform(delete("/compensations/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/compensations/1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
