package com.englishlab.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.englishlab.config.MailProperties;
import com.englishlab.model.entity.NotificationStatus;
import com.englishlab.model.entity.NotificationsLog;
import com.englishlab.repository.NotificationsLogRepository;
import com.englishlab.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * El envío real está apagado por defecto: la fila de notifications_log NO puede decir SENT
 * (sería engañoso), debe quedar PENDING.
 */
class NotificationServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void disabledMailPersistsPendingStatus() {
        NotificationsLogRepository logRepository = mock(NotificationsLogRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ObjectProvider<JavaMailSender> senderProvider = mock(ObjectProvider.class);
        NotificationService service = new NotificationService(
                senderProvider, logRepository, userRepository,
                new MailProperties(false, "no-reply@test.local"));

        // Llamada directa (sin proxy @Async): el registro se persiste en el acto.
        service.sendWelcome(1L, "student@test.local");

        ArgumentCaptor<NotificationsLog> captor = ArgumentCaptor.forClass(NotificationsLog.class);
        verify(logRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(captor.getValue().getType()).isEqualTo(NotificationService.TYPE_WELCOME);
        verifyNoInteractions(senderProvider);
    }
}
