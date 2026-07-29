package com.nexora.service;

import com.nexora.dto.ConversationDTO;
import com.nexora.dto.MessageDTO;

import java.util.List;

/** Cas d'usage de la messagerie (conversations client &harr; espace). */
public interface MessageService {

    /** Conversations de l'utilisateur (comme client ou comme propriétaire d'espace). */
    List<ConversationDTO> conversations(Long idUtilisateur);

    /** Ouvre (ou crée) la conversation entre un client et un espace ; renvoie son id. */
    Long ouvrirAvecEspace(Long idClient, Long idEspace);

    /** Messages d'une conversation ; marque comme lus ceux reçus par le lecteur. */
    List<MessageDTO> messages(Long idConversation, Long idLecteur);

    /** Envoie un message dans une conversation. */
    void envoyer(Long idConversation, Long idExpediteur, String contenu);

    /** Nombre de messages non lus reçus par l'utilisateur (toutes conversations). */
    long nbNonLus(Long idUtilisateur);

    /** Marque comme lus les messages d'une conversation reçus par le lecteur. */
    void marquerLu(Long idConversation, Long idLecteur);
}
