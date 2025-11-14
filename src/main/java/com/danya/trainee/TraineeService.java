package com.danya.trainee;

import com.danya.exception.EntityNotFoundException;
import com.danya.metric.TraineeMetricsRecorder;
import com.danya.trainee.dto.CreateTraineeDto;
import com.danya.trainee.dto.TraineeWithTrainersDto;
import com.danya.trainee.dto.UpdateTraineeDto;
import com.danya.trainee.mapper.TraineeMapper;
import com.danya.user.UserCreationResult;
import com.danya.user.UserService;
import com.danya.user.dto.CredentialsDto;
import com.danya.user.role.RoleName;
import com.danya.workload.WorkloadService;
import com.danya.workload.api.dto.BatchUpdateWorkloadRequest;
import com.danya.workload.api.dto.UpdateWorkloadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraineeService {
    private final TraineeRepository traineeRepository;
    private final UserService userService;
    private final TraineeMapper traineeMapper;
    private final TraineeMetricsRecorder traineeMetricsRecorder;
    private final WorkloadService workloadService;

    @Transactional
    public CredentialsDto createProfile(CreateTraineeDto payload) {
        log.info("Creating trainee with firstname '{}' and lastname '{}'",
                payload.firstName(), payload.lastName());

        UserCreationResult userCreation = userService.createUser(
                payload.firstName(),
                payload.lastName(),
                RoleName.TRAINEE
        );

        Trainee trainee = new Trainee(userCreation.user(), payload.dateOfBirth(), payload.address());
        Trainee saved = traineeRepository.save(trainee);

        log.info("Successfully created trainee with id {} and username '{}'",
                saved.getId(), saved.getUsername());

        traineeMetricsRecorder.incrementRegistration();

        return new CredentialsDto(saved.getUsername(), userCreation.plainPassword());
    }

    @Transactional(readOnly = true)
    public TraineeWithTrainersDto getProfileByUsername(String username) {
        Trainee trainee = traineeRepository.findWithTrainersByUserUsername(username)
                .orElseThrow(() -> {
                    log.warn("Trainee with {} username does not exist", username);
                    return new EntityNotFoundException("Trainee profile not found");
                });
        return traineeMapper.toTraineeWithTrainersDto(trainee);
    }

    @Transactional
    public TraineeWithTrainersDto updateProfile(String username, UpdateTraineeDto payload) {
        log.info("Attempting to update trainee with username '{}'", username);

        Trainee trainee = traineeRepository.findWithTrainersByUserUsername(username)
                .orElseThrow(() -> {
                    log.warn("Unable to update. Trainee with '{}' username does not exist", username);
                    return new EntityNotFoundException("Unable to update. Trainee does not exist");
                });

        trainee.getUser().setFirstName(payload.firstName());
        trainee.getUser().setLastName(payload.lastName());
        trainee.setDateOfBirth(payload.dateOfBirth());
        trainee.setAddress(payload.address());
        trainee.getUser().setActive(payload.isActive());

        traineeRepository.save(trainee);

        log.info("Successfully updated trainee with username '{}'", username);
        return traineeMapper.toTraineeWithTrainersDto(trainee);
    }

    @Transactional
    public void deleteByUsername(String username) {
        Trainee trainee = traineeRepository.findWithUpcomingTrainingsAndTrainersByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Unable to delete. Trainee with '{}' username does not exist", username);
                    return new EntityNotFoundException("Unable to delete. Trainee does not exist");
                });

        workloadService.subtractDuration(createWorkloadUpdates(trainee));

        traineeRepository.delete(trainee);
        log.info("Successfully removed trainee with username '{}'", username);
        traineeMetricsRecorder.incrementDeletion();
    }

    private BatchUpdateWorkloadRequest createWorkloadUpdates(Trainee trainee) {
        List<UpdateWorkloadRequest> updates = trainee.getTrainings().stream()
                .map(tr -> new UpdateWorkloadRequest(
                        tr.getTrainer().getUsername(),
                        tr.getDate(),
                        tr.getDuration()
                ))
                .toList();

        return new BatchUpdateWorkloadRequest(updates);
    }
}
