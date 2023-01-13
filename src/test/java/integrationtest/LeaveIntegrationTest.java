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
public class LeaveIntegrationTest {

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
    void getAllLeavesWhenEverythingIsOk() throws Exception {
        mockMvc.perform(get("/leaves")).andDo(print())
                .andExpect(jsonPath("$.content.size()").value(0));
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void getLeaveByIdWhenEverythingIsOk() throws Exception {
        prepareData();
        mockMvc.perform(get("/leaves/1").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void  getOwnedContractsWhenEverythingIsOk() throws Exception{
        prepareData();
        mockMvc.perform(get("/leaves/employees").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void getEmployeeLeavesWhenEverythingIsOk() throws Exception {
        prepareData();
        final File jsonFileLeave = new ClassPathResource("json/create-leave2.json").getFile();
        final String leaveToCreate = Files.readString(jsonFileLeave.toPath());
        mockMvc.perform(post("/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leaveToCreate))
                .andDo(print());
        mockMvc.perform(get("/leaves/employees/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void addLeaveWhenEverythingIsOk() throws Exception {
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
        final File jsonFileLeave = new ClassPathResource("json/create-leave.json").getFile();
        final String leaveToCreate = Files.readString(jsonFileLeave.toPath());
        mockMvc.perform(post("/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leaveToCreate))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void updateLeaveWhenEverythingIsOk() throws Exception {
        prepareData();
        final File jsonFileUpdate = new ClassPathResource("json/update-leave.json").getFile();
        final String leaveToUpdate = Files.readString(jsonFileUpdate.toPath());
        mockMvc.perform(put("/leaves/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leaveToUpdate))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaveEndDate").value("2023-02-28"))
                .andExpect(jsonPath("$.leaveReason").value("rest"));
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void deleteLeaveWhenEverythingIsOk() throws Exception {
        prepareData();
        mockMvc.perform(delete("/leaves/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/leaves/1"))
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
        final File jsonFileLeave = new ClassPathResource("json/create-leave.json").getFile();
        final String leaveToCreate = Files.readString(jsonFileLeave.toPath());
        mockMvc.perform(post("/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leaveToCreate))
                .andDo(print());
    }
}
