package com.example.service;

import com.example.model.Reservation;

import javax.persistence.OptimisticLockException;
import java.util.Optional;

public interface ReservationService {
    Optional<Reservation> findById(Long id);
    Reservation update(Reservation reservation) throws OptimisticLockException;
    Long create(Reservation reservation);
}