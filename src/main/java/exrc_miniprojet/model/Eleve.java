package exrc_miniprojet.model;

public class Eleve {
    private int id;
    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private StatutEleve statut = StatutEleve.ACTIF;

    private int filiereId;
    private String filiereCode;
    private String filiereNom;

    public Eleve() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public StatutEleve getStatut() { return statut; }
    public void setStatut(StatutEleve statut) { this.statut = statut; }

    public int getFiliereId() { return filiereId; }
    public void setFiliereId(int filiereId) { this.filiereId = filiereId; }

    public String getFiliereCode() { return filiereCode; }
    public void setFiliereCode(String filiereCode) { this.filiereCode = filiereCode; }

    public String getFiliereNom() { return filiereNom; }
    public void setFiliereNom(String filiereNom) { this.filiereNom = filiereNom; }

    public String getNomComplet() { return prenom + " " + nom; }
}
