package com.danya.trainer.api;

import com.danya.trainer.TrainerService;
import com.danya.trainer.dto.CreateTrainerDto;
import com.danya.trainer.dto.TrainerWithTraineesDto;
import com.danya.trainer.dto.UpdateTrainerDto;
import com.danya.training.TrainingService;
import com.danya.training.dto.TrainerTrainingDto;
import com.danya.training.dto.TrainerTrainingFilterDto;
import com.danya.user.dto.CredentialsDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/trainers")
public class TrainerController implements TrainerApi {
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @PostMapping
    public ResponseEntity<CredentialsDto> createProfile(@RequestBody @Valid CreateTrainerDto payload) {
        CredentialsDto credentials = trainerService.createProfile(payload);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(credentials);
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainerWithTraineesDto> getProfileByUsername(@PathVariable String username) {
        TrainerWithTraineesDto traineeProfile = trainerService.getProfileByUsername(username);
        return ResponseEntity.status(HttpStatus.OK)
                .body(traineeProfile);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<TrainerWithTraineesDto> getProfile(@AuthenticationPrincipal String username) {
        TrainerWithTraineesDto traineeProfile = trainerService.getProfileByUsername(username);
        return ResponseEntity.status(HttpStatus.OK)
                .body(traineeProfile);
    }

    @PutMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainerWithTraineesDto> updateProfileByUsername(
            @PathVariable String username,
            @RequestBody @Valid UpdateTrainerDto payload
    ) {
        TrainerWithTraineesDto traineeProfile = trainerService.updateProfile(username, payload);
        return ResponseEntity.status(HttpStatus.OK)
                .body(traineeProfile);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<TrainerWithTraineesDto> updateProfile(
            @AuthenticationPrincipal String username,
            @RequestBody @Valid UpdateTrainerDto payload
    ) {
        TrainerWithTraineesDto traineeProfile = trainerService.updateProfile(username, payload);
        return ResponseEntity.status(HttpStatus.OK)
                .body(traineeProfile);
    }

    @GetMapping("/{username}/trainings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TrainerTrainingDto>> getAllTrainerTrainingsByUsername(
            @PathVariable String username,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String traineeUsername
    ) {
        TrainerTrainingFilterDto payload = TrainerTrainingFilterDto.builder()
                .trainerUsername(username)
                .fromDate(fromDate)
                .toDate(toDate)
                .traineeUsername(traineeUsername)
                .build();
        List<TrainerTrainingDto> trainings = trainingService.getTrainerTrainings(payload);
        return ResponseEntity.status(HttpStatus.OK)
                .body(trainings);
    }

    @GetMapping("/me/trainings")
    @PreAuthorize("hasRole('TRAINER')")
    public ResponseEntity<List<TrainerTrainingDto>> getAllTrainerTrainings(
            @AuthenticationPrincipal String username,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String traineeUsername
    ) {
        TrainerTrainingFilterDto payload = TrainerTrainingFilterDto.builder()
                .trainerUsername(username)
                .fromDate(fromDate)
                .toDate(toDate)
                .traineeUsername(traineeUsername)
                .build();
        List<TrainerTrainingDto> trainings = trainingService.getTrainerTrainings(payload);
        return ResponseEntity.status(HttpStatus.OK)
                .body(trainings);
    }
}
