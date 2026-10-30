# library-management-jee

Application web Jakarta EE / JSP de **gestion de bibliothèque** : catalogue de livres, emprunts, téléchargements, favoris, administration et statistiques, avec deux rôles (**Client** et **Admin**).

## Objectif
Fournir à une bibliothèque numérique une plateforme complète pour :
- Permettre aux clients de **rechercher**, **emprunter**, **télécharger** et **mettre en favoris** des livres.
- Permettre aux administrateurs de **gérer** le catalogue (livres, auteurs, catégories), **suivre** les utilisateurs actifs et **consulter** les statistiques mensuelles.

## Données
Base **PostgreSQL** `gestion_bib`, schéma `gest_bib`. Connexion directe JDBC, pas d'ORM — requêtes SQL inline dans chaque servlet.

Modèles principaux (JavaBeans) :
- **`Utilisateur`** : id, nom, email, rôle, mot_de_passe (stocké hashé au format `salt:hash`), nombre d'actions
- **`Livre`** : id, titre, résumé, année, format, categorieId, auteurs[], image_url
- **`Auteur`** et **`Categorie`**
- **`LivresEmpruntes`** (dates d'emprunt / retour calculées via `Calendar`)
- **`LivresTelecharges`** (historique de téléchargement)
- **`Favoris`**
- **`StatistiqueMensuelle`** : année, mois, totalEmprunts, totalTelechargements, totalAjouts

## Méthode
Architecture **MVC classique** sans DAO : chaque servlet gère son propre accès JDBC.

- **Hashage des mots de passe** : `HashUtil` (PBKDF2WithHmacSHA1, 10 000 itérations, salt 16 octets, clé 256 bits) — format de stockage `salt:hash` encodé base64.
- **Sessions** : identifiant, email, rôle et nom stockés dans la session HTTP après authentification.
- **Routing** : par annotations `@WebServlet("/<nom>Servlet")` (pas de `web.xml`).

## Stack
- **Jakarta EE Servlet** (package `jakarta.servlet`, Jakarta EE 10)
- **JSP** pour les vues (25 pages)
- **JDBC + PostgreSQL** (`postgresql-42.7.4.jar`)
- **PBKDF2** pour le hashage des mots de passe (javax.crypto)
- Compilation Eclipse — l'IDE recommandé reste Eclipse ou IntelliJ IDEA Ultimate avec plugin Jakarta EE

## Fonctionnalités

**Côté Client**
- Inscription, connexion, modification du profil, déconnexion
- Recherche de livres (par titre, auteur, catégorie)
- Détail d'un livre + livres du même auteur
- Emprunt d'un livre, consultation des emprunts actifs
- Téléchargement d'un livre, historique de téléchargement
- Ajout / retrait des favoris, liste de favoris

**Côté Admin**
- CRUD livres (ajouter, modifier, supprimer)
- CRUD auteurs et catégories
- Liste des utilisateurs actifs
- Statistiques mensuelles (emprunts, téléchargements, ajouts)

**Routes principales** (`@WebServlet`) : `LoginServlet`, `InscriptionServlet`, `DeconnexionServlet`, `ModifProfilServlet`, `ChercherLivresServlet` / `ChercherLivresAdminServlet`, `LivreDetailsServlet` / `LivreDetailsAdminServlet`, `LivresParAuteurServlet`, `AjouterLivreServlet`, `ModifLivreServlet`, `SupprimerLivreServlet`, `GererAuteursServlet`, `AjouterAuteurServlet`, `ModifierAuteurServlet`, `SupprimerAuteurServlet`, `GererCategoriesServlet`, `AjouterCategorieServlet`, `ModifierCategorieServlet`, `SupprimerCategorieServlet`, `EmprunterLivreServlet`, `EmpruntsActifsServlet`, `EmpruntsServlet`, `TelechargerLivreServlet`, `TelechargesServlet`, `ToggleFavorisServlet`, `FavorisServlet`, `UtilisateursActifsServlet`, `StatistiquesServlet`, `DonneesAjoutServlet`.

**Vues JSP** (25 pages sous `src/main/webapp/view/`) :
- Client : `login.jsp`, `inscription.jsp`, `mon-compte.jsp`, `livreListe.jsp`, `livreDetails.jsp`, `livresParAuteur.jsp`, `auteurLivres.jsp`, `favoris.jsp`, `emprunts.jsp`, `empruntsActifs.jsp`, `telecharges.jsp`, `navbar.jsp`
- Admin : `livreListeAdmin.jsp`, `livreDetailsAdmin.jsp`, `ajouterLivre.jsp`, `modifierLivre.jsp`, `ajouterAuteur.jsp`, `modifierAuteur.jsp`, `gererAuteurs.jsp`, `ajouterCategorie.jsp`, `modifierCategorie.jsp`, `gererCategories.jsp`, `utilisateursActifs.jsp`, `statistiques.jsp`, `navbarAdmin.jsp`

## Résultats
Projet pédagogique **fonctionnellement complet** sur toutes les fonctionnalités listées : ~30 servlets + 25 JSP couvrent le périmètre de bout en bout.

### Limites connues
- **Pas de DAO** : les requêtes SQL sont inline dans chaque servlet. Difficilement maintenable à grande échelle.
- **Pas de tests** automatisés.
- **Pas de framework** (ni Spring, ni Hibernate) — Jakarta Servlet brut.
- **Comptes créés avant ce correctif de sécurité** : `LoginServlet` comparait les mots de passe en clair alors que `InscriptionServlet` les hashait déjà. Les comptes créés avant le correctif doivent être réinitialisés — le login refuse désormais tout `storedPassword` qui n'est pas au format `salt:hash`.
- **Credentials PostgreSQL** à fournir via variables d'environnement (voir ci-dessous).

## Comment lancer

### Pré-requis
- JDK 17+ (compat Jakarta EE 10)
- Apache Tomcat 10+ (support Jakarta EE 10)
- PostgreSQL 14+
- Eclipse (recommandé — projet contient `.classpath` et `.project`) ou IntelliJ IDEA Ultimate

### Base de données
Créer la base `gestion_bib` + le schéma `gest_bib` avec les tables : `utilisateurs`, `livres`, `auteurs`, `livres_auteurs`, `categories`, `emprunts`, `telechargements`, `favoris`.

### Variables d'environnement
Avant le lancement, définir :
```bash
export DB_URL="jdbc:postgresql://localhost:5432/gestion_bib"
export DB_USER="postgres"
export DB_PASSWORD="<ton_mot_de_passe>"
```
(ou configurer les variables côté Tomcat / Eclipse Run Configuration).

### Déploiement
1. Importer le projet dans Eclipse (**File → Import → Existing Projects into Workspace**).
2. Associer le serveur Tomcat 10 dans l'onglet Servers.
3. Clic droit sur le projet → **Run As → Run on Server**.
4. L'application est accessible sur `http://localhost:8080/projetJEE/`.

### Compte admin initial
À créer directement en base avec un mot de passe au format `salt:hash` généré par `HashUtil.hashPassword("...")`.
