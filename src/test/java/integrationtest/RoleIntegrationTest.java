package integrationtest;

import com.syberry.bakery.BakeryApplication;
import com.syberry.bakery.dto.RoleName;
import integrationtest.config.H2Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {BakeryApplication.class, H2Config.class})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class RoleIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void should_GetAllRoles() throws Exception {
        mockMvc.perform(get("/roles")).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(4));
    }

    @Test
    void should_AddRoleToUser() throws Exception {
        final File jsonFile = new ClassPathResource("json/create-user.json").getFile();
        final String userToCreate = Files.readString(jsonFile.toPath());
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userToCreate)).andDo(print()).andExpect(status().isCreated());
        mockMvc.perform(post("/users/1/roles/" + 1)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.roles", hasItem(RoleName.ROLE_ADMIN.name())));
    }

    @Test
    void should_RemoveRoleFromUser() throws Exception {
        final File jsonFile = new ClassPathResource("json/create-user-with-two-roles.json").getFile();
        final String userToCreate = Files.readString(jsonFile.toPath());
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userToCreate)).andDo(print()).andExpect(status().isCreated());
        mockMvc.perform(delete("/users/1/roles/" + 2)).andDo(print()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.roles").value(not(contains(RoleName.ROLE_HR.name()))));
    }
}
