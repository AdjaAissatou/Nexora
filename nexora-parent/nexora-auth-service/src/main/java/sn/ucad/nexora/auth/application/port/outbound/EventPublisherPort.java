package sn.ucad.nexora.auth.application.port.outbound;

public interface EventPublisherPort {

    void publish(Object event);

}