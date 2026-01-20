package com.amaravathi.tradeidentity.domain.training;

import com.amaravathi.tradeidentity.api.admin.dto.UserResponseDto;
import com.amaravathi.tradeidentity.api.training.dto.SessionDto;
import com.amaravathi.tradeidentity.api.training.dto.TrainingRequestDto;
import com.amaravathi.tradeidentity.api.training.dto.TrainingResponseDto;
import com.amaravathi.tradeidentity.common.ResourceNotFoundException;
import com.amaravathi.tradeidentity.common.TradeIdentityException;
import com.amaravathi.tradeidentity.domain.user.AppUser;
import com.amaravathi.tradeidentity.domain.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final UserTrainingRepository userTrainingRepository;
    private final AppUserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TrainingResponseDto> getAllTrainings() {

        log.info("Fetching all trainings");

        try {
            List<Training> trainings = trainingRepository.findAll();

            int count = trainings.size();
            log.debug("Found {} trainings", count);

            if (trainings.isEmpty()) {
                log.info("No trainings found");
                return List.of();
            }

            return trainings.stream()
                    .map(this::convertToDto)
                    .toList();

        } catch (DataAccessException dae) {
            // Covers DB down, timeout, connection pool issues, etc.
            log.error("Database error while fetching all trainings", dae);
            throw new TradeIdentityException("Database error while fetching trainings", dae);
        }
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

        SessionDto thirdSession = SessionDto.builder()
                .date(r.getThirdSessionDate())
                .startTime(r.getThirdSessionStartTime())
                .endTime(r.getThirdSessionEndTime())
                .build();

        return TrainingResponseDto.builder()
                .trainingId(r.getId())
                .firstSession(firstSession)
                .secondSession(secondSession)
                .thirdSession(thirdSession)
                .build();
    }

    @Transactional
    public TrainingResponseDto createTrainingSession(TrainingRequestDto req) {
        log.info("Request received to create training session");

        if (req == null || req.getFirstSession() == null || req.getSecondSession() == null) {
            log.warn("Invalid training request: req/firstSession/secondSession is null");
            throw new IllegalArgumentException("Invalid training request");
        }

        try {
            Training training = new Training();
            training.setFirstSessionDate(req.getFirstSession().getDate());
            training.setFirstSessionStartTime(req.getFirstSession().getStartTime());
            training.setFirstSessionEndTime(req.getFirstSession().getEndTime());

            training.setSecondSessionDate(req.getSecondSession().getDate());
            training.setSecondSessionStartTime(req.getSecondSession().getStartTime());
            training.setSecondSessionEndTime(req.getSecondSession().getEndTime());

            training.setThirdSessionDate(req.getThirdSession().getDate());
            training.setThirdSessionStartTime(req.getThirdSession().getStartTime());
            training.setThirdSessionEndTime(req.getThirdSession().getEndTime());

            Training savedTraining = trainingRepository.save(training);

            log.info("Training created successfully trainingId={}", savedTraining.getId());
            return convertToDto(savedTraining);

        } catch (DataIntegrityViolationException dive) {
            // constraint errors: null fields, check constraints, etc.
            log.error("Data integrity violation while creating training session", dive);
            throw new TradeIdentityException("Invalid training data. Unable to create training session.", dive);

        } catch (DataAccessException dae) {
            // DB down / timeout / pool issues
            log.error("Database error while creating training session", dae);
            throw new TradeIdentityException("Database error while creating training session.", dae);
        }
    }

    @Transactional(readOnly = true)
    public List<TrainingResponseDto> getTrainingsByUserId(Integer userId) {
        log.info("Fetching trainings for userId={}", userId);

        if (userId == null || userId <= 0) {
            log.warn("Invalid userId received: {}", userId);
            throw new IllegalArgumentException("Invalid userId");
        }

        try {
            // Optional but recommended: if user must exist
            if (!userRepository.existsById(userId)) {
                log.warn("User not found userId={}", userId);
                throw new ResourceNotFoundException("User not found with id: " + userId);
            }

            List<Training> trainings = trainingRepository.findAllTrainingsByUserId(userId);

            int count = (trainings == null) ? 0 : trainings.size();
            log.debug("Found {} trainings for userId={}", count, userId);

            if (trainings == null || trainings.isEmpty()) {
                return List.of();
            }

            return trainings.stream()
                    .map(this::convertToDto)
                    .toList();

        } catch (DataAccessException dae) {
            log.error("Database error while fetching trainings for userId={}", userId, dae);
            throw new TradeIdentityException("Database error while fetching trainings for user.", dae);
        }
    }

    @Transactional
    public void assignTrainingToUser(int userId, int trainingId) {
        log.info("Assigning trainingId={} to userId={}", trainingId, userId);

        if (userId <= 0 || trainingId <= 0) {
            log.warn("Invalid ids userId={} trainingId={}", userId, trainingId);
            throw new IllegalArgumentException("Invalid userId or trainingId");
        }

        try {
            // Validate existence (recommended)
            if (!userRepository.existsById(userId)) {
                log.warn("User not found userId={}", userId);
                throw new ResourceNotFoundException("User not found with id: " + userId);
            }

            if (!trainingRepository.existsById(trainingId)) {
                log.warn("Training not found trainingId={}", trainingId);
                throw new ResourceNotFoundException("Training not found with id: " + trainingId);
            }

            // remove old mapping(s)
            int deleted = userTrainingRepository.deleteAllByUserId(userId);
            log.debug("Deleted {} existing training mappings for userId={}", deleted, userId);

            // create new mapping
            UserTraining userTraining = new UserTraining();
            userTraining.setUserId(userId);
            userTraining.setTrainingId(trainingId);

            userTrainingRepository.save(userTraining);

            log.info("Training assigned successfully trainingId={} userId={}", trainingId, userId);

        } catch (DataIntegrityViolationException dive) {
            log.error("Data integrity violation while assigning trainingId={} to userId={}", trainingId, userId, dive);
            throw new TradeIdentityException("Cannot assign training due to invalid/duplicate mapping.", dive);

        } catch (DataAccessException dae) {
            log.error("Database error while assigning trainingId={} to userId={}", trainingId, userId, dae);
            throw new TradeIdentityException("Database error while assigning training to user.", dae);
        }
    }

    public void deleteTraining(int trainingId) {

        log.info("Request received to delete trainingId={}", trainingId);

        if (trainingId <= 0) {
            log.warn("Invalid trainingId received: {}", trainingId);
            throw new IllegalArgumentException("Invalid trainingId");
        }

        try {
            if (!trainingRepository.existsById(trainingId)) {
                log.warn("Training not found for trainingId={}", trainingId);
                throw new ResourceNotFoundException("Training not found with id: " + trainingId);
            }


            userTrainingRepository.deleteAllByTrainingId(trainingId);

            trainingRepository.deleteById(trainingId);

            log.info("Successfully deleted trainingId={}", trainingId);

        } catch (DataIntegrityViolationException dive) {
            // FK constraint: training is referenced by user_training
            log.error("Cannot delete trainingId={} due to data integrity violation", trainingId, dive);
            throw new TradeIdentityException(
                    "Training cannot be deleted because it is associated with users", dive);

        } catch (DataAccessException dae) {
            // DB unavailable, timeout, etc.
            log.error("Database error while deleting trainingId={}", trainingId, dae);
            throw new TradeIdentityException(
                    "Database error while deleting training", dae);

        }
    }


    public List<UserResponseDto> getAllUsersByTrainingById(int trainingId) {

        log.info("Fetching users enrolled for trainingId={}", trainingId);

        try {
            List<AppUser> users = userRepository.findAllUsersEnrolledForAnyTraining(trainingId);

            int count = (users == null) ? 0 : users.size();
            log.debug("Found {} enrolled users for trainingId={}", count, trainingId);

            if (users == null || users.isEmpty()) {
                log.info("No users enrolled for trainingId={}", trainingId);
                return List.of();
            }

            List<UserResponseDto> dtos = users.stream()
                    .map(user -> UserResponseDto.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .fullName(user.getFullName())
                            .phone(user.getPhone())
                            .status(user.getStatus())
                            .build())
                    .toList();

            log.info("Returning {} users for trainingId={}", dtos.size(), trainingId);
            return dtos;

        } catch (Exception ex) {
            // Any unexpected errors (mapping, NPE, etc.)
            log.error("Unexpected error while fetching enrolled users for trainingId={}", trainingId, ex);
            throw new TradeIdentityException("Unexpected error occurred");
        }

    }
}
