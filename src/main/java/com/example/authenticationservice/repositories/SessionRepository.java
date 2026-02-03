package com.example.authenticationservice.repositories;

import com.example.authenticationservice.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Session save(Session session);
}
