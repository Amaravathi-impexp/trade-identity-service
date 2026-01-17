package com.amaravathi.tradeidentity.api.training;

import com.amaravathi.tradeidentity.api.admin.dto.CreateUserRequestDto;
import com.amaravathi.tradeidentity.api.admin.dto.UserResponseDto;
import com.amaravathi.tradeidentity.api.training.dto.TrainingRequestDto;
import com.amaravathi.tradeidentity.api.training.dto.TrainingResponseDto;
import com.amaravathi.tradeidentity.domain.training.TrainingService;
import com.amaravathi.tradeidentity.domain.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trade-identity/v1/training")
public class TrainingController {

    private final TrainingService trainingService;


    public TrainingController( TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping
    public ResponseEntity<List<TrainingResponseDto>> getAllTrainings() {
        return ResponseEntity.status(HttpStatus.OK).body(trainingService.getAllTrainings());
    }

    @PostMapping
    public ResponseEntity<TrainingResponseDto> create(@Valid @RequestBody TrainingRequestDto req) {
        var trainingResponseDto = trainingService.createTrainingSession(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingResponseDto);
    }

    @DeleteMapping("/{trainingId}")
    public ResponseEntity<String> deleteTraining(@PathVariable int trainingId) {
        trainingService.deleteTraining(trainingId);
        return ResponseEntity.status(HttpStatus.OK).body("Training deleted successfully");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TrainingResponseDto>> getTrainingsByUserId(@PathVariable int userId) {
        return ResponseEntity.status(HttpStatus.OK).body(trainingService.getTrainingsByUserId(userId));
    }
    @PostMapping("/user/{userId}/training/{trainingId}")
    public ResponseEntity<String> assignTrainingToUser(@PathVariable int userId, @PathVariable int trainingId) {
        trainingService.assignTrainingToUser(userId, trainingId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Training assigned to user successfully");
    }

    @GetMapping("/{trainingId}")
    public ResponseEntity<List<UserResponseDto>> getAllUsersByTrainingById(@PathVariable int trainingId) {
        return ResponseEntity.status(HttpStatus.OK).body(trainingService.getAllUsersByTrainingById(trainingId));
    }

}
