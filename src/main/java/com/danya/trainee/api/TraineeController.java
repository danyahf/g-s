package com.danya.trainee.api;

import com.danya.trainee.TraineeService;
import com.danya.trainee.dto.CreateTraineeDto;
import com.danya.trainee.dto.TraineeProfileTrainerDto;
import com.danya.trainee.dto.TraineeWithTrainersDto;
import com.danya.trainee.dto.UpdateTraineeDto;
import com.danya.trainer.TrainerService;
import com.danya.training.TrainingService;
import com.danya.training.dto.TraineeTrainingDto;
import com.danya.training.dto.TraineeTrainingFilterDto;
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
@RequestMapping("/trainees")
public class TraineeController implements TraineeApi {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @PostMapping
    public ResponseEntity<CredentialsDto> createProfile(@RequestBody @Valid CreateTraineeDto payload) {
        CredentialsDto credentials = traineeService.createProfile(payload);
        return ResponseEntity.status(HttpStatus.CREATED).
                body(credentials);
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TraineeWithTrainersDto> getProfileByUsername(
            @PathVariable String username
    ) {
        TraineeWithTrainersDto traineeProfile = traineeService.getProfileByUsername(username);
        return ResponseEntity.status(HttpStatus.OK)
                .body(traineeProfile);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('TRAINEE')")
    public ResponseEntity<TraineeWithTrainersDto> getProfile(
            @AuthenticationPrincipal String username
    ) {
        TraineeWithTrainersDto traineeProfile = traineeService.getProfileByUsername(username);
        return ResponseEntity.status(HttpStatus.OK)
                .body(traineeProfile);
    }

    @PutMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TraineeWithTrainersDto> updateProfileByUsername(
            @PathVariable String username,
            @RequestBody @Valid UpdateTraineeDto payload
    ) {
        TraineeWithTrainersDto traineeProfile = traineeService.updateProfile(username, payload);
        return ResponseEntity.status(HttpStatus.OK)
                .body(traineeProfile);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('TRAINEE')")
    public ResponseEntity<TraineeWithTrainersDto> updateProfile(
            @RequestBody @Valid UpdateTraineeDto payload,
            @AuthenticationPrincipal String username
    ) {
        TraineeWithTrainersDto traineeProfile = traineeService.updateProfile(username, payload);
        return ResponseEntity.status(HttpStatus.OK)
                .body(traineeProfile);
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProfileByUsername(@PathVariable String username) {
        traineeService.deleteByUsername(username);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('TRAINEE')")
    public ResponseEntity<Void> deleteProfile(@AuthenticationPrincipal String username) {
        traineeService.deleteByUsername(username);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/{username}/available-trainers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TraineeProfileTrainerDto>> getAvailableTrainers(@PathVariable String username) {
        List<TraineeProfileTrainerDto> trainers = trainerService.findUnassignedForTrainee(username);
        return ResponseEntity.status(HttpStatus.OK)
                .body(trainers);
    }

    @GetMapping("/{username}/trainings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TraineeTrainingDto>> getAllTraineeTrainingsByUsername(
            @PathVariable String username,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String trainerUsername,
            @RequestParam(required = false) Integer trainingTypeId
    ) {
        TraineeTrainingFilterDto payload = TraineeTrainingFilterDto.builder()
                .traineeUsername(username)
                .fromDate(fromDate)
                .toDate(toDate)
                .trainerUsername(trainerUsername)
                .trainingTypeId(trainingTypeId)
                .build();
        List<TraineeTrainingDto> trainings = trainingService.getTraineeTrainings(payload);
        return ResponseEntity.status(HttpStatus.OK)
                .body(trainings);
    }

    @GetMapping("/me/trainings")
    @PreAuthorize("hasRole('TRAINEE')")
    public ResponseEntity<List<TraineeTrainingDto>> getAllTraineeTrainings(
            @AuthenticationPrincipal String username,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String trainerUsername,
            @RequestParam(required = false) Integer trainingTypeId
    ) {
        TraineeTrainingFilterDto payload = TraineeTrainingFilterDto.builder()
                .traineeUsername(username)
                .fromDate(fromDate)
                .toDate(toDate)
                .trainerUsername(trainerUsername)
                .trainingTypeId(trainingTypeId)
                .build();
        List<TraineeTrainingDto> trainings = trainingService.getTraineeTrainings(payload);
        return ResponseEntity.status(HttpStatus.OK)
                .body(trainings);
    }
}
