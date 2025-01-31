package es.ucm.fdi.iw.g06.printopolis.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Entity
@NamedQueries({
        @NamedQuery(name = "Printer.allUserPrinters", query = "SELECT p FROM Printer p "
                + "WHERE p.impresor.id = :userId"),
        @NamedQuery(name = "Printer.dePrinter", query = "DELETE FROM Printer p WHERE p.id = :id"),
       // @NamedQuery(name= "Printer.numImpresoras", query= "SELECT u FROM User u JOIN Printer p ON u.id = p.impresor.id GROUP BY p.impresor.id"),
   })
              

@Data
public class Printer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    public enum Status {
        NO_INK, WORKING, AVAILABLE
    }

    @NotNull
    private String name;
    private float material_level;
    private int punctuation;
    private String status;

    @ManyToOne
    private User impresor;

    @Override
    public String toString() {
        return "Printer #" + id;
    }

}
