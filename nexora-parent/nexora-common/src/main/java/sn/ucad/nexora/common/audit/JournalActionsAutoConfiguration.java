package sn.ucad.nexora.common.audit;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import sn.ucad.nexora.common.notification.Notifications;

/**
 * Met {@link JournalActions} et {@link Notifications} à disposition de tous les services qui dépendent
 * de nexora-common.
 */
@AutoConfiguration
public class JournalActionsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JournalActions journalActions() {
        return new JournalActions();
    }

    @Bean
    @ConditionalOnMissingBean
    public Notifications notifications() {
        return new Notifications();
    }
}
