package com.danya.trainer;

import com.danya.SecurityTestConfig;
import com.danya.auth.JwtService;
import com.danya.auth.filter.ErrorResponseWriter;
import com.danya.trainer.api.TrainerController;
import com.danya.trainer.dto.CreateTrainerDto;
import com.danya.trainer.dto.TrainerProfileTraineeDto;
import com.danya.trainer.dto.TrainerWithTraineesDto;
import com.danya.trainer.dto.UpdateTrainerDto;
import com.danya.training.TrainingService;
import com.danya.trainingType.TrainingType;
import com.danya.trainingType.TrainingTypeName;
import com.danya.user.dto.CredentialsDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerController.class)
@Import(SecurityTestConfig.class)
class TrainerControllerTest {

    @MockBean
    private TrainerService trainerService;

    @MockBean
    private TrainingService trainingService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private ErrorResponseWriter errorResponseWriter;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void shouldReturn200AndTrainerWithTraineesWhenGetProfile() throws Exception {
        String username = "john.doe";
        TrainingType specialization = new TrainingType();
        specialization.setTrainingTypeName(TrainingTypeName.YOGA);

        TrainerProfileTraineeDto t0 = new TrainerProfileTraineeDto(
                "alice.brown",
                "Alice",
                "Brown"
        );
        TrainerWithTraineesDto dto = new TrainerWithTraineesDto(
                "John",
                "Doe",
                specialization,
                true,
                List.of(t0)
        );

        when(trainerService.getProfileByUsername(username)).thenReturn(dto);

        mockMvc.perform(get("/trainers/{username}", username)
                        .with(authentication(getAdmin()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.specialization.trainingTypeName").value("YOGA"))
                .andExpect(jsonPath("$.trainees[0].username").value("alice.brown"));

        verify(trainerService).getProfileByUsername(username);
    }

    @Test
    void shouldReturn200AndTrainerWithTraineesWhenGetProfileMe() throws Exception {
        String username = "john.doe";
        TrainingType specialization = new TrainingType();
        specialization.setTrainingTypeName(TrainingTypeName.YOGA);

        TrainerProfileTraineeDto t0 = new TrainerProfileTraineeDto(
                "alice.brown",
                "Alice",
                "Brown"
        );
        TrainerWithTraineesDto dto = new TrainerWithTraineesDto(
                "John",
                "Doe",
                specialization,
                true,
                List.of(t0)
        );

        when(trainerService.getProfileByUsername(username)).thenReturn(dto);

        mockMvc.perform(get("/trainers/me")
                        .with(authentication(getTrainerUser(username)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.specialization.trainingTypeName").value("YOGA"))
                .andExpect(jsonPath("$.trainees[0].username").value("alice.brown"));

        verify(trainerService).getProfileByUsername(username);
    }

//    @Test
//    void shouldReturn201AndCredentialsWhenCreateProfile() throws Exception {
//        CreateTrainerDto payload = new CreateTrainerDto(
//                "firstName",
//                "lastName",
//                TrainingTypeName.YOGA
//        );
//        String username = "firstName.lastName";
//        String password = "1234567890";
//        CredentialsDto credentials = new CredentialsDto(username, password);
//
//        when(trainerService.createProfile(any(CreateTrainerDto.class)))
//                .thenReturn(credentials);
//
//        mockMvc.perform(post("/trainers")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(payload))
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.username").value(username))
//                .andExpect(jsonPath("$.password").value(password));
//    }

    @ParameterizedTest
    @MethodSource("invalidCreatePayloads")
    void shouldReturn400WhenCreateProfileForInvalidPayloads(CreateTrainerDto bad) throws Exception {
        mockMvc.perform(post("/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    private static Stream<Arguments> invalidCreatePayloads() {
        return Stream.of(
                Arguments.of(new CreateTrainerDto("q", "ValidLast", TrainingTypeName.YOGA)),
                Arguments.of(new CreateTrainerDto("       ", "ValidLast", TrainingTypeName.YOGA)),
                Arguments.of(new CreateTrainerDto("X".repeat(56), "ValidLast", TrainingTypeName.YOGA)),
                Arguments.of(new CreateTrainerDto(null, "ValidLast", TrainingTypeName.YOGA)),
                Arguments.of(new CreateTrainerDto("ValidFirst", "q", TrainingTypeName.YOGA)),
                Arguments.of(new CreateTrainerDto("ValidFirst", "       ", TrainingTypeName.YOGA)),
                Arguments.of(new CreateTrainerDto("ValidFirst", "X".repeat(56), TrainingTypeName.YOGA)),
                Arguments.of(new CreateTrainerDto("ValidFirst", null, TrainingTypeName.YOGA)),
                Arguments.of(new CreateTrainerDto("ValidFirst", "ValidLast", null))
        );
    }

    @Test
    void shouldReturn200AndUpdatedProfileWhenUpdateProfile() throws Exception {
        String username = "john.doe";
        UpdateTrainerDto payload = new UpdateTrainerDto("JohnNew", "DoeNew", true);

        TrainingType specialization = new TrainingType();
        specialization.setTrainingTypeName(TrainingTypeName.YOGA);

        TrainerWithTraineesDto updated = new TrainerWithTraineesDto(
                "JohnNew", "DoeNew", specialization, true, List.of()
        );

        when(trainerService.updateProfile(eq(username), any(UpdateTrainerDto.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/trainers/{username}", username)
                        .with(authentication(getAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("JohnNew"))
                .andExpect(jsonPath("$.lastName").value("DoeNew"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.specialization.trainingTypeName").value("YOGA"))
                .andExpect(jsonPath("$.trainees").isEmpty());

        verify(trainerService).updateProfile(eq(username), any(UpdateTrainerDto.class));
    }

    @Test
    void shouldReturn200AndUpdatedProfileWhenUpdateProfileMe() throws Exception {
        String username = "john.doe";
        UpdateTrainerDto payload = new UpdateTrainerDto("JohnNew", "DoeNew", true);

        TrainingType specialization = new TrainingType();
        specialization.setTrainingTypeName(TrainingTypeName.YOGA);

        TrainerWithTraineesDto updated = new TrainerWithTraineesDto(
                "JohnNew", "DoeNew", specialization, true, List.of()
        );

        when(trainerService.updateProfile(eq(username), any(UpdateTrainerDto.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/trainers/me")
                        .with(authentication(getTrainerUser(username)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("JohnNew"))
                .andExpect(jsonPath("$.lastName").value("DoeNew"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.specialization.trainingTypeName").value("YOGA"))
                .andExpect(jsonPath("$.trainees").isEmpty());

        verify(trainerService).updateProfile(eq(username), any(UpdateTrainerDto.class));
    }

    @ParameterizedTest
    @MethodSource("invalidUpdatePayloads")
    void shouldReturn400WhenUpdateProfileForInvalidPayloads(UpdateTrainerDto bad) throws Exception {
        String username = "john.doe";

        mockMvc.perform(put("/trainers/{username}", username)
                        .with(authentication(getAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    @ParameterizedTest
    @MethodSource("invalidUpdatePayloads")
    void shouldReturn400WhenUpdateProfileMeForInvalidPayloads(UpdateTrainerDto bad) throws Exception {
        mockMvc.perform(put("/trainers/me")
                        .with(authentication(getTrainerUser("john.doe")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    private static Stream<Arguments> invalidUpdatePayloads() {
        return Stream.of(
                Arguments.of(new UpdateTrainerDto("q", "ValidLast", true)),
                Arguments.of(new UpdateTrainerDto("       ", "ValidLast", true)),
                Arguments.of(new UpdateTrainerDto("X".repeat(56), "ValidLast", true)),
                Arguments.of(new UpdateTrainerDto(null, "ValidLast", true)),
                Arguments.of(new UpdateTrainerDto("ValidFirst", "q", true)),
                Arguments.of(new UpdateTrainerDto("ValidFirst", "       ", true)),
                Arguments.of(new UpdateTrainerDto("ValidFirst", "X".repeat(56), true)),
                Arguments.of(new UpdateTrainerDto("ValidFirst", null, true))
        );
    }

    private static Authentication getTrainerUser(String username) {
        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_TRAINER"))
        );
    }

    private static Authentication getAdmin() {
        return new UsernamePasswordAuthenticationToken(
                "admin.admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}
