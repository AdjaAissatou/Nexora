package sn.ucad.nexora.web.dto.administration;

import java.util.List;

public record PageJournalResponse(List<ActionJournalResponse> actions, int page, boolean pageSuivante,
                                  List<String> modules) {}
