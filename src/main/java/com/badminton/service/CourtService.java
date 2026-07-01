package com.badminton.service;

import com.badminton.dto.CourtDTO;

import java.util.List;

public interface CourtService {
    List<CourtDTO> getAllCourts();
    CourtDTO getCourtById(Long id);
    CourtDTO createCourt(CourtDTO courtDTO);
    CourtDTO updateCourt(Long id, CourtDTO courtDTO);
    void deleteCourt(Long id);
}
