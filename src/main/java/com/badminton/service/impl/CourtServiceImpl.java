package com.badminton.service.impl;

import com.badminton.dto.CourtDTO;
import com.badminton.entity.Court;
import com.badminton.entity.User;
import com.badminton.exception.CustomException;
import com.badminton.repository.CourtRepository;
import com.badminton.repository.UserRepository;
import com.badminton.service.CourtService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourtServiceImpl implements CourtService {

    private final CourtRepository courtRepository;
    private final UserRepository userRepository;

    public CourtServiceImpl(CourtRepository courtRepository, UserRepository userRepository) {
        this.courtRepository = courtRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourtDTO> getAllCourts() {
        return courtRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CourtDTO getCourtById(Long id) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new CustomException("Court not found with id: " + id, HttpStatus.NOT_FOUND));
        return convertToDTO(court);
    }

    @Override
    @Transactional
    public CourtDTO createCourt(CourtDTO courtDTO) {
        User manager = userRepository.findById(courtDTO.getManagerId())
                .orElseThrow(() -> new CustomException("Manager not found with id: " + courtDTO.getManagerId(), HttpStatus.NOT_FOUND));

        Court court = Court.builder()
                .name(courtDTO.getName())
                .address(courtDTO.getAddress())
                .pricePerHour(courtDTO.getPricePerHour())
                .manager(manager)
                .build();

        Court savedCourt = courtRepository.save(court);
        return convertToDTO(savedCourt);
    }

    @Override
    @Transactional
    public CourtDTO updateCourt(Long id, CourtDTO courtDTO) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new CustomException("Court not found with id: " + id, HttpStatus.NOT_FOUND));

        if (courtDTO.getName() != null) {
            court.setName(courtDTO.getName());
        }
        if (courtDTO.getAddress() != null) {
            court.setAddress(courtDTO.getAddress());
        }
        if (courtDTO.getPricePerHour() != null) {
            court.setPricePerHour(courtDTO.getPricePerHour());
        }
        if (courtDTO.getManagerId() != null) {
            User manager = userRepository.findById(courtDTO.getManagerId())
                    .orElseThrow(() -> new CustomException("Manager not found with id: " + courtDTO.getManagerId(), HttpStatus.NOT_FOUND));
            court.setManager(manager);
        }

        Court updatedCourt = courtRepository.save(court);
        return convertToDTO(updatedCourt);
    }

    @Override
    @Transactional
    public void deleteCourt(Long id) {
        if (!courtRepository.existsById(id)) {
            throw new CustomException("Court not found with id: " + id, HttpStatus.NOT_FOUND);
        }
        courtRepository.deleteById(id);
    }

    private CourtDTO convertToDTO(Court court) {
        return CourtDTO.builder()
                .id(court.getId())
                .name(court.getName())
                .address(court.getAddress())
                .pricePerHour(court.getPricePerHour())
                .managerId(court.getManager() != null ? court.getManager().getId() : null)
                .createdAt(court.getCreatedAt())
                .build();
    }
}
