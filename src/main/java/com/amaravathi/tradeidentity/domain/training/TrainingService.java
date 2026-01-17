package com.amaravathi.tradeidentity.domain.training;

import com.amaravathi.tradeidentity.api.admin.dto.RoleResponseResponseDto;
import com.amaravathi.tradeidentity.api.admin.dto.UserResponseDto;
import com.amaravathi.tradeidentity.api.training.dto.SessionDto;
import com.amaravathi.tradeidentity.api.training.dto.TrainingRequestDto;
import com.amaravathi.tradeidentity.api.training.dto.TrainingResponseDto;
import com.amaravathi.tradeidentity.domain.user.AppUser;
import com.amaravathi.tradeidentity.domain.user.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final UserTrainingRepository userTrainingRepository;

    private final AppUserRepository userRepository;

    public TrainingService(TrainingRepository trainingRepository,
                           UserTrainingRepository userTrainingRepository,
                           AppUserRepository userRepository) {
        this.trainingRepository = trainingRepository;
        this.userTrainingRepository = userTrainingRepository;
        this.userRepository = userRepository;
    }

    public List<TrainingResponseDto> getAllTrainings() {
        return trainingRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    private TrainingResponseDto convertToDto(Training r) {
        SessionDto firstSession = SessionDto.builder()
                .date(r.getFirstSessionDate())
                .startTime(r.getFirstSessionStartTime())
                .endTime(r.getFirstSessionEndTime())
                .build();

        SessionDto secondSession = SessionDto.builder()
                .date(r.getSecondSessionDate())
                .startTime(r.getSecondSessionStartTime())
                .endTime(r.getSecondSessionEndTime())
                .build();
        return TrainingResponseDto.builder()
                .trainingId(r.getId())
                .firstSession(firstSession)
                .secondSession(secondSession)
                .build();
    }

    public TrainingResponseDto createTrainingSession(TrainingRequestDto req) {
        Training training = new Training();
        training.setFirstSessionDate(req.getFirstSession().getDate());
        training.setFirstSessionStartTime(req.getFirstSession().getStartTime());
        training.setFirstSessionEndTime(req.getFirstSession().getEndTime());
        training.setSecondSessionDate(req.getSecondSession().getDate());
        training.setSecondSessionStartTime(req.getSecondSession().getStartTime());
        training.setSecondSessionEndTime(req.getSecondSession().getEndTime());
        Training savedTraining = trainingRepository.save(training);
        return convertToDto(savedTraining);
    }

    public List<TrainingResponseDto> getTrainingsByUserId(Integer userId) {
        return trainingRepository.findAllTrainingsByUserId(userId).stream()
                .map(this::convertToDto)
                .toList();
    }

    public void assignTrainingToUser(int userId, int trainingId) {

        userTrainingRepository.deleteAllByUserId(userId);

        UserTraining userTraining = new UserTraining();
        userTraining.setUserId(userId);
        userTraining.setTrainingId(trainingId);

        userTrainingRepository.save(userTraining);
    }

    public void deleteTraining(int trainingId) {
        trainingRepository.deleteById(trainingId);
    }

    public List<UserResponseDto> getAllUsersByTrainingById(int trainingId) {

        return userRepository.findAllUsersEnrolledForAnyTraining(trainingId).stream()
                .map(user -> UserResponseDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .phone(user.getPhone())
                        .status(user.getStatus())
                        .build()).toList();

    }
}
