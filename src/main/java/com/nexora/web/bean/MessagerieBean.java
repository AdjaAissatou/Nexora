package com.nexora.web.bean;

import com.nexora.dto.ConversationDTO;
import com.nexora.dto.MessageDTO;
import com.nexora.service.MessageService;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

/** Messagerie du côté présentation : liste de conversations + fil + envoi. */
@Named("msgBean")
@ViewScoped
public class MessagerieBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private MessageService messageService;
    @Inject private SessionBean session;

    private Long selectedId;
    private String texte;

    /** Entrée sur la page : ouvre la conversation d'un espace (?espace=) ou la 1re. */
    public void entree(ComponentSystemEvent e) {
        if (!session.isConnecte()) return;
        String p = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("espace");
        if (p != null && !p.isBlank()) {
            try {
                selectedId = messageService.ouvrirAvecEspace(session.getIdUtilisateur(), Long.valueOf(p.trim()));
            } catch (NumberFormatException ignore) { }
        }
        if (selectedId == null) {
            List<ConversationDTO> l = getConversations();
            if (!l.isEmpty()) selectedId = l.get(0).getId();
        }
    }

    public List<ConversationDTO> getConversations() {
        return session.isConnecte() ? messageService.conversations(session.getIdUtilisateur()) : List.of();
    }

    public List<MessageDTO> getMessages() {
        return selectedId != null ? messageService.messages(selectedId, session.getIdUtilisateur()) : List.of();
    }

    public void selectionner(Long id) { this.selectedId = id; }

    public void envoyer() {
        if (selectedId != null && texte != null && !texte.isBlank()) {
            messageService.envoyer(selectedId, session.getIdUtilisateur(), texte);
            texte = null;
        }
    }

    public String getSelectedTitre() {
        if (selectedId == null) return "";
        for (ConversationDTO c : getConversations()) {
            if (c.getId().equals(selectedId)) return c.getTitre();
        }
        return "";
    }

    /** Nombre de messages non lus, pour le badge de la barre. */
    public long getNbNonLus() {
        return session.isConnecte() ? messageService.nbNonLus(session.getIdUtilisateur()) : 0;
    }

    public boolean active(Long id)     { return id != null && id.equals(selectedId); }
    public Long getSelectedId()        { return selectedId; }
    public String getTexte()           { return texte; }
    public void setTexte(String texte) { this.texte = texte; }
}
