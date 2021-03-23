package es.ucm.fdi.iw.g06.printopolis.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Entity
@Data
public class Sales {
    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

    private long user;
    private String address;
    private BigDecimal total_price; 

    @Override
	public String toString() {
		return "Sale #" + id;
	}	

}
