package integrationtest;

import com.google.common.cache.LoadingCache;
import com.syberry.bakery.BakeryApplication;
import com.syberry.bakery.config.MailConfig;
import com.syberry.bakery.dto.EmailVerificationDto;
import com.syberry.bakery.dto.ResetPasswordDto;
import com.syberry.bakery.dto.UpdatePasswordDto;
import com.syberry.bakery.entity.User;
import com.syberry.bakery.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {BakeryApplication.class, H2Config.class})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class AuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LoadingCache<String, String> oneTimePasswordCache;
    @Autowired
    private PasswordEncoder encoder;
    @MockBean
    private EmailService emailService;
    @MockBean
    private MailConfig mailConfig;

    @Test
    void should_Login_Without2fa() throws Exception {
        final File jsonFile = new ClassPathResource("json/login-admin.json").getFile();
        final String login_admin = Files.readString(jsonFile.toPath());
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(login_admin))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("admin@mail.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    void should_Login_With_2fa() throws Exception {
        User user = userRepository.findByIdAndIsBlockedFalse(1L).get();
        user.set2faEnabled(true);
        userRepository.save(user);
        when(emailService.getTemplate(any(),any())).thenReturn(null);
        final File jsonFile = new ClassPathResource("json/login-admin.json").getFile();
        final String login_admin = Files.readString(jsonFile.toPath());
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(login_admin))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(result -> result.toString().equals("Verification code was sent to email"))
                .andExpect(cookie().doesNotExist("accessToken"))
                .andExpect(cookie().doesNotExist("refreshToken"));
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void should_ClearCookieToken() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andDo(print())
                .andExpect(cookie().value("accessToken", ""))
                .andExpect(cookie().value("refreshToken", ""));
    }

    @Test
    void should_ResetPassword() throws Exception {
        final File jsonFile = new ClassPathResource("json/reset-password-admin.json").getFile();
        final String reset_password_admin = Files.readString(jsonFile.toPath());
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("admin@mail.com","token","1234");
        oneTimePasswordCache.put(resetPasswordDto.getEmail(), resetPasswordDto.getToken());
        mockMvc.perform(post("/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content(reset_password_admin))
                .andDo(print())
                .andExpect(status().isNoContent());
        User admin = userRepository.findByEmailAndIsBlockedFalse(resetPasswordDto.getEmail()).get();
        assertThat(encoder.matches("1234", admin.getPassword())).isTrue();
    }

    @Test
    void should_SendResetPasswordCodeToEmail() throws Exception {
        when(emailService.getTemplate(any(),any())).thenReturn(null);
        mockMvc.perform(post("/auth/forgot-password?email=admin@mail.com"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void should_Disable2fa() throws Exception {
        mockMvc.perform(put("/auth/2fa/disabled"))
                .andDo(print())
                .andExpect(status().isNoContent());
        User admin = userRepository.findByEmailAndIsBlockedFalse("admin@mail.com").get();
        assertThat(admin.is2faEnabled()).isFalse();
    }

    @Test
    @WithUserDetails("admin@mail.com")
    void should_UpdatePasswordOfLoggedInUser() throws Exception{
        final File jsonFile = new ClassPathResource("json/update-password-admin.json").getFile();
        final String update_password_admin = Files.readString(jsonFile.toPath());
        UpdatePasswordDto updatePasswordDto = new UpdatePasswordDto("admin","new");
        mockMvc.perform(put("/auth/update-password").contentType(MediaType.APPLICATION_JSON)
                        .content(update_password_admin))
                .andDo(print())
                .andExpect(status().isNoContent());
        User admin = userRepository.findByEmailAndIsBlockedFalse("admin@mail.com").get();
        assertThat(encoder.matches(updatePasswordDto.getNewPassword(),admin.getPassword())).isTrue();
    }

}
