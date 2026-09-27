CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE TYPE sexe AS ENUM (
    'HOMME',
    'FEMME',
    'AUTRE'
);

CREATE TYPE langue AS ENUM (
    'FRANCAIS',
    'ANGLAIS',
    'WOLOF',
    'ARABE'
);

CREATE TYPE statut_compte AS ENUM (
    'EN_ATTENTE',
    'ACTIF',
    'SUSPENDU',
    'BLOQUE',
    'SUPPRIME'
);
CREATE TYPE statut_espace AS ENUM (

'BROUILLON',

'EN_ATTENTE',

'EN_VERIFICATION',

'ACTIF',

'SUSPENDU',

'REFUSE',

'FERME'

);
CREATE TYPE type_champ AS ENUM (
    'TEXTE',
    'NOMBRE',
    'DATE',
    'BOOLEAN',
    'LISTE',
    'MULTI_LISTE'
); 
CREATE TYPE type_reduction AS ENUM (
    'POURCENTAGE',
    'MONTANT_FIXE'
);
CREATE TYPE statut_signalement AS ENUM (

'EN_ATTENTE',
'EN_COURS',
'TRAITE',
'REJETE'

);
CREATE TYPE type_media AS ENUM (

'IMAGE',
'VIDEO',
'DOCUMENT',
'AUDIO',
'LOGO',
'COUVERTURE'

);
CREATE TYPE type_notification AS ENUM (

'INFO',
'SUCCES',
'AVERTISSEMENT',
'ERREUR',
'MESSAGE',
'COMMANDE',
'RESERVATION',
'PROMOTION',
'CERTIFICATION'

);
CREATE TYPE type_contact AS ENUM (

'TELEPHONE',
'MOBILE',
'WHATSAPP',
'EMAIL',
'SITE_WEB',
'FACEBOOK',
'INSTAGRAM',
'LINKEDIN',
'TIKTOK',
'YOUTUBE',
'X',
'TELEGRAM'

);
CREATE TYPE statut_commande AS ENUM (

'EN_ATTENTE',
'CONFIRMEE',
'EN_PREPARATION',
'PRETE',
'EXPEDIEE',
'LIVREE',
'ANNULEE',
'REMBOURSEE'

);
CREATE TYPE statut_reservation AS ENUM (

'EN_ATTENTE',
'CONFIRMEE',
'REFUSEE',
'ANNULEE',
'TERMINEE'

);
CREATE TYPE statut_livraison AS ENUM (

'EN_PREPARATION',
'EN_TRANSIT',
'LIVREE',
'ECHEC',
'RETOUR'

);
CREATE TYPE mode_paiement AS ENUM (

'ESPECES',
'ORANGE_MONEY',
'WAVE',
'CARTE_BANCAIRE',
'PAYPAL'

);

CREATE TYPE statut_paiement AS ENUM (

'EN_ATTENTE',
'VALIDE',
'REFUSE',
'REMBOURSE'

);
CREATE TYPE statut_facture AS ENUM (
    'BROUILLON',
    'EMISE',
    'PAYEE',
    'ANNULEE'
);

CREATE TYPE statut_litige AS ENUM (
    'OUVERT',
    'EN_COURS',
    'RESOLU',
    'FERME',
    'REJETE'
);

CREATE TYPE statut_escrow AS ENUM (
    'EN_ATTENTE',
    'BLOQUE',
    'LIBERE',
    'REMBOURSE',
    'ANNULE'
);
CREATE TYPE type_offre_principale AS ENUM (

'PRODUIT',

'SERVICE'

);
CREATE TYPE type_notification_canal AS ENUM (

'APPLICATION',

'EMAIL',

'SMS',

'WHATSAPP'

);
CREATE TYPE devise AS ENUM (

'XOF',

'EUR',

'USD'

);
CREATE TYPE statut_offre AS ENUM (

'BROUILLON',

'PUBLIE',

'SUSPENDU',

'EXPIRE',

'SUPPRIME'

);
CREATE TYPE statut_certification AS ENUM (
'NON_VERIFIE',
'EN_ATTENTE',
'VERIFIE',
'REJETE'
);

CREATE TYPE type_certification AS ENUM (
'IDENTITE',
'NINEA',
'REGISTRE_COMMERCE',
'DIPLOME',
'LICENCE',
'AUTRE'
);
