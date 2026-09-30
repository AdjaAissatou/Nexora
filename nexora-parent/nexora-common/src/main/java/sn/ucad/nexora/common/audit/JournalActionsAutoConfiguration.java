package sn.ucad.nexora.common.audit;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** Met {@link JournalActions} à disposition de tous les services qui dépendent de nexora-common. */
@AutoConfiguration
public class JournalActionsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JournalActions journalActions() {
        return new JournalActions();
    }
}
