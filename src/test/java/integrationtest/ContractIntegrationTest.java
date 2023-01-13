package integrationtest;

import com.syberry.bakery.BakeryApplication;
import com.syberry.bakery.config.MailConfig;
import com.syberry.bakery.repository.ContractRepository;
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
public class ContractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ContractRepository contractRepository;
    @MockBean
    private MailConfig mailConfig;
    @MockBean
    private EmailService emailService;

    @Test
    @WithUserDetails("admin@mail.com")
    void getAllContractsWhenEverythingIsOk() throws Exception {
        mockMvc.perform(get("/contracts")).andDo(print())
                .andExpect(jsonPath("$.content.size()").value(0));
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void getContractByIdWhenEverythingIsOk() throws Exception {
        prepareData();
        mockMvc.perform(get("/contracts/1").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.contractId").value(1));
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void getOwnedContractsWhenEverythingIsOk() throws Exception {
        prepareData();
        mockMvc.perform(get("/contracts/employees").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void getUserContractsWhenEverythingIsOk() throws Exception {
        prepareData();
        final File jsonFileContract1 = new ClassPathResource("json/create-contract2.json").getFile();
        final String contractToCreate1 = Files.readString(jsonFileContract1.toPath());
        mockMvc.perform(post("/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contractToCreate1))
                .andDo(print());
        mockMvc.perform(get("/contracts/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void addContractWhenEverythingIsOk() throws Exception {
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
                .andDo(print());
        final File jsonFileContract = new ClassPathResource("json/create-contract.json").getFile();
        final String contractToCreate = Files.readString(jsonFileContract.toPath());
        mockMvc.perform(post("/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contractToCreate))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contractId").exists());
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void updateContractWhenEverythingIsOk() throws Exception {
        prepareData();
        final File jsonFileUpdate = new ClassPathResource("json/update-contract.json").getFile();
        final String contractToUpdate = Files.readString(jsonFileUpdate.toPath());
        mockMvc.perform(put("/contracts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contractToUpdate))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateOfSignature").value("2024-01-01"))
                .andExpect(jsonPath("$.type").value("PART_TIME"));
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void deleteContractWhenEverythingIsOk() throws Exception {
        prepareData();
        mockMvc.perform(delete("/contracts/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/contracts/1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private void prepareData() throws Exception {
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
                .andDo(print());
        final File jsonFileContract = new ClassPathResource("json/create-contract.json").getFile();
        final String contractToCreate = Files.readString(jsonFileContract.toPath());
        mockMvc.perform(post("/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contractToCreate))
                .andDo(print());
    }
}
