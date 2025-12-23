package exrc_miniprojet.model;

import java.time.LocalDate;

public class DossierAdministratif {
    private int id;
    private String numeroInscription;
    private LocalDate dateCreation;
    private int eleveId;

    public DossierAdministratif() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroInscription() { return numeroInscription; }
    public void setNumeroInscription(String numeroInscription) { this.numeroInscription = numeroInscription; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public int getEleveId() { return eleveId; }
    public void setEleveId(int eleveId) { this.eleveId = eleveId; }
}
