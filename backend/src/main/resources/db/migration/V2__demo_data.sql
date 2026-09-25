-- Données de démonstration — cf. contrainte "Démarrage" du cahier des charges (NF5).
-- Chargées au démarrage pour que le correcteur n'ouvre pas une application vide.

INSERT INTO promotion (nom) VALUES ('KFOKAM48 - Batch 2');

INSERT INTO formateur (nom) VALUES ('Admin Formateur');

INSERT INTO etudiant (nom, promotion_id) VALUES
    ('Aïcha Ngo Bella', 1),
    ('Brice Fouda', 1),
    ('Carine Mballa', 1),
    ('David Tchoupo', 1),
    ('Estelle Njoya', 1),
    ('Franck Owona', 1);
