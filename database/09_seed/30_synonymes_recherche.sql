-- ============================================================================
-- Synonymes de recherche de départ : métiers → activités, mots courants → mots du catalogue.
-- Un seul sens (terme → équivalent) ; éviter les mots trop larges (« mobile » trouverait
-- « mobilier », « porte » trouverait « réfrigérateur 2 portes »).
-- Rejouable ; à compléter librement (une ligne par équivalent).
-- ============================================================================

SET client_encoding = 'UTF8';

INSERT INTO synonyme_recherche (terme, equivalent)
SELECT t, e FROM (VALUES
    ('plombier', 'plomberie'), ('plombier', 'canalisation'), ('plombier', 'sanitaire'), ('plombier', 'robinet'),
    ('plombier', 'fuite'), ('plombier', 'chauffe-eau'), ('plombier', 'débouchage'),
    ('électricien', 'électricité'), ('électricien', 'câblage'), ('électricien', 'disjoncteur'),
    ('menuisier', 'menuiserie'), ('menuisier', 'meuble'), ('menuisier', 'mobilier'),
    ('ébéniste', 'menuiserie'), ('ébéniste', 'meuble'),
    ('couturier', 'couture'), ('couturier', 'confection'), ('couturier', 'retouche'), ('couturier', 'sur mesure'),
    ('tailleur', 'couture'), ('tailleur', 'confection'), ('tailleur', 'retouche'),
    ('coiffeur', 'coiffure'), ('coiffeur', 'tresse'), ('coiffeur', 'barbier'), ('coiffeur', 'salon'),
    ('mécanicien', 'mécanique'), ('mécanicien', 'garage'), ('mécanicien', 'vidange'),
    ('maçon', 'maçonnerie'), ('maçon', 'carrelage'),
    ('peintre', 'peinture'),
    ('frigo', 'réfrigérateur'), ('clim', 'climatiseur'), ('clim', 'climatisation'),
    ('téléphone', 'smartphone'), ('téléphone', 'iphone'), ('téléphone', 'samsung'),
    ('portable', 'smartphone'), ('portable', 'ordinateur portable'),
    ('ordinateur', 'pc'), ('ordinateur', 'laptop'), ('ordi', 'ordinateur'),
    ('télé', 'télévision'), ('tv', 'télévision'),
    ('resto', 'restaurant'), ('resto', 'restauration'),
    ('taxi', 'vtc'), ('taxi', 'chauffeur'), ('taxi', 'course'),
    ('livreur', 'livraison'), ('livreur', 'coursier'), ('livreur', 'colis'),
    ('traiteur', 'restauration'), ('traiteur', 'mariage'), ('traiteur', 'repas'),
    ('photographe', 'photo'), ('photographe', 'photographie'),
    ('chaussure', 'derby'), ('chaussure', 'bottine'), ('chaussure', 'basket'), ('chaussure', 'sandale'),
    ('soulier', 'chaussure'),
    ('habit', 'vêtement'), ('habit', 'chemise'), ('habit', 'pantalon'), ('habit', 'robe'),
    ('montre', 'chronographe'), ('bijou', 'montre'), ('bijou', 'collier'),
    ('poisson', 'tilapia'), ('poisson', 'thiof'), ('poisson', 'darne')
) AS s(t, e)
ON CONFLICT (terme, equivalent) DO NOTHING;

-- Retirés après essai : trop larges
DELETE FROM synonyme_recherche WHERE (terme, equivalent) IN (('électricien', 'électrique'), ('téléphone', 'mobile'),
    ('maçon', 'construction'), ('menuisier', 'porte'));
