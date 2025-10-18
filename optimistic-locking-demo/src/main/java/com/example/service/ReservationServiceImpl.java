package com.example.service;

import com.example.model.Reservation;

import javax.persistence.*;
import java.util.Optional;

public class ReservationServiceImpl implements ReservationService {

    private final EntityManagerFactory emf;

    public ReservationServiceImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            Reservation r = em.find(Reservation.class, id);
            // detach before returning to simulate detached entity behavior (optional)
            if (r != null) em.detach(r);
            return Optional.ofNullable(r);
        } finally {
            em.close();
        }
    }

    @Override
    public Long create(Reservation reservation) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(reservation);
            tx.commit();
            return reservation.getId();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Reservation update(Reservation reservation) throws OptimisticLockException {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // merge returns managed instance; if version mismatch, JPA will throw OptimisticLockException on commit
            Reservation merged = em.merge(reservation);
            tx.commit();
            // detach to return a detached instance (optional)
            em.detach(merged);
            return merged;
        } catch (OptimisticLockException ole) {
            if (tx.isActive()) tx.rollback();
            throw ole; // laisser caller gérer le conflit
        } catch (PersistenceException pe) {
            if (tx.isActive()) tx.rollback();
            // si la cause est verrou optimiste encapsulée, la re-lancer proprement
            Throwable cause = pe.getCause();
            if (cause instanceof OptimisticLockException) throw (OptimisticLockException) cause;
            throw pe;
        } finally {
            em.close();
        }
    }
}
