package sn.ucad.nexora.catalogue.application.dto.response;

import java.util.List;

/**
 * Réponse paginée de la recherche d'offres.
 */
public class OffrePageResponse {

    private List<OffreSummaryResponse> contenu;
    private int page;
    private int taille;
    private long total;
    private int totalPages;
    private boolean dernierePage;

    public OffrePageResponse() {}

    public OffrePageResponse(List<OffreSummaryResponse> contenu, int page, int taille, long total) {
        this.contenu = contenu;
        this.page = page;
        this.taille = taille;
        this.total = total;
        this.totalPages = taille > 0 ? (int) Math.ceil((double) total / taille) : 0;
        this.dernierePage = page >= totalPages - 1;
    }

    public List<OffreSummaryResponse> getContenu() { return contenu; }
    public void setContenu(List<OffreSummaryResponse> contenu) { this.contenu = contenu; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getTaille() { return taille; }
    public void setTaille(int taille) { this.taille = taille; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public boolean isDernierePage() { return dernierePage; }
    public void setDernierePage(boolean dernierePage) { this.dernierePage = dernierePage; }
}
