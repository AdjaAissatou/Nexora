package sn.ucad.nexora.auth.application.port.outbound;

import java.time.LocalDateTime;

public interface ClockPort {

    LocalDateTime now();

}