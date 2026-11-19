package com.englishlab.repository;

import com.englishlab.model.entity.NotificationsLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationsLogRepository extends JpaRepository<NotificationsLog, Long> {
}