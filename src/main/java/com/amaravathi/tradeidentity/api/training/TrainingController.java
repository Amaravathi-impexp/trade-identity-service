package com.amaravathi.tradeidentity.api.training;

import com.amaravathi.tradeidentity.api.admin.dto.UserResponseDto;
import com.amaravathi.tradeidentity.api.training.dto.TrainingRequestDto;
import com.amaravathi.tradeidentity.api.training.dto.TrainingResponseDto;
import com.amaravathi.tradeidentity.domain.training.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trade-identity/v1/training")
@Slf4j
@Validated
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @GetMapping
    public ResponseEntity<List<TrainingResponseDto>> getAllTrainings() {
        log.info("GET /training");
        return ResponseEntity.status(HttpStatus.OK).body(trainingService.getAllTrainings());
    }

    @PostMapping
    public ResponseEntity<TrainingResponseDto> create(@Valid @RequestBody TrainingRequestDto req) {
        log.info("POST /training (create)");
        var trainingResponseDto = trainingService.createTrainingSession(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingResponseDto);
    }

    @DeleteMapping("/{trainingId}")
    public ResponseEntity<String> deleteTraining(@PathVariable int trainingId) {
        log.info("DELETE /training/{}", trainingId);
        trainingService.deleteTraining(trainingId);
        return ResponseEntity.status(HttpStatus.OK).body("Training deleted successfully");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TrainingResponseDto>> getTrainingsByUserId(@PathVariable int userId) {
        log.info("GET /training/user/{}", userId);
        return ResponseEntity.status(HttpStatus.OK).body(trainingService.getTrainingsByUserId(userId));
    }
    @PostMapping("/user/{userId}/training/{trainingId}")
    public ResponseEntity<String> assignTrainingToUser(@PathVariable int userId, @PathVariable int trainingId) {
        log.info("POST /training/user/{}/training/{} (assign)", userId, trainingId);
        trainingService.assignTrainingToUser(userId, trainingId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Training assigned to user successfully");
    }

    @GetMapping("/{trainingId}")
    public ResponseEntity<List<UserResponseDto>> getAllUsersByTrainingById(@PathVariable int trainingId) {
        log.info("GET /training/{} (users)", trainingId);
        return ResponseEntity.status(HttpStatus.OK).body(trainingService.getAllUsersByTrainingById(trainingId));
    }

}
