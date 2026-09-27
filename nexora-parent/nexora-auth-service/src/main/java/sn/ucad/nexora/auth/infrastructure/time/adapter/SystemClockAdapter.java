package sn.ucad.nexora.auth.infrastructure.time.adapter;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import sn.ucad.nexora.auth.application.port.outbound.ClockPort;

@Component
public class SystemClockAdapter implements ClockPort {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}