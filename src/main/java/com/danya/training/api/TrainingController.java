package com.danya.training.api;

import com.danya.training.TrainingService;
import com.danya.training.dto.CreateTrainingDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/trainings")
public class TrainingController implements TrainingApi {
    private final TrainingService trainingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINER')")
    public ResponseEntity<Long> create(@RequestBody @Valid CreateTrainingDto payload) {
        Long id = trainingService.createProfile(payload);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(id);
    }
}
