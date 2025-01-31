package es.ucm.fdi.iw.g06.printopolis.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.bytebuddy.agent.builder.AgentBuilder.PoolStrategy.Eager;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * A user; can be an Admin, a User, or a Moderator
 *
 * Users can log in and send each other messages.
 *
 * @author mfreire
 */
/**
 * An authorized user of the system.
 */
@Entity
@NoArgsConstructor
@NamedQueries({
		@NamedQuery(name = "User.byUsername", query = "SELECT u FROM User u "
				+ "WHERE u.username = :username AND u.enabled = 1"),
		@NamedQuery(name = "User.hasUsername", query = "SELECT COUNT(u) " + "FROM User u "
				+ "WHERE u.username = :username"),
		@NamedQuery(name = "User.allImpresores", query = "SELECT DISTINCT u FROM User u JOIN Printer p on u.id = p.impresor.id"),
		@NamedQuery(name = "User.allUser", query = "SELECT u FROM User u WHERE u.id != :id"),
		@NamedQuery(name = "User.randomImpresores", query= "SELECT u FROM User u JOIN Printer p on u.id = p.impresor.id ORDER BY RAND()"),
		@NamedQuery(name = "User.byId", query = "SELECT u FROM User u WHERE u.id = :senderId"),
		@NamedQuery(name = "User.delUser", query = "DELETE FROM User p WHERE p.id = :id"),
		@NamedQuery(name = "User.delUserDesigns", query = "DELETE FROM Design p WHERE p.designer.id = :id"),
		@NamedQuery(name = "User.delUserPrinters", query = "DELETE FROM Printer p WHERE p.impresor.id = :id"),
		@NamedQuery(name = "User.getPunctuation", query = "SELECT SUM(punctuation) AS punt, COUNT(punctuation) AS numDes FROM Design p WHERE p.designer.id = :id"),
		@NamedQuery(name= "User.numDesigns", query = " SELECT p FROM Printer p JOIN Design d on p.impresor.id = d.designer.id ORDER BY COUNT(d.id) "),
		@NamedQuery(name= "User.ordenAsc", query="SELECT DISTINCT u FROM User u JOIN Printer p on u.id = p.impresor.id ORDER BY u.firstName ASC"),
        @NamedQuery(name= "User.ordenDesc", query="SELECT DISTINCT u FROM User u JOIN Printer p on u.id = p.impresor.id ORDER BY u.firstName DESC"),
   
	})
@Data
public class User implements Transferable<User.Transfer> {

	private static Logger log = LogManager.getLogger(User.class);

	public enum Role {
		USER, // used for logged-in, non-priviledged users
		ADMIN // used for maximum priviledged users
	}

	// do not change these fields

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	/** username for login purposes; must be unique */
	@Column(nullable = false, unique = true)
	private String username;
	/**
	 * encoded password; use setPassword(SecurityConfig.encode(plaintextPassword))
	 * to encode it
	 */
	@Column(nullable = false)
	private String password;
	@Column(nullable = false)
	private String roles; // split by ',' to separate roles
	private byte enabled;
	private String phone;
	private String address;
	private String aboutMe;
	@OneToOne
	@JoinColumn(name = "user")
	private Sales saleId;

	// desginer specific fields, related with designs
	@OneToMany
	@JoinColumn(name = "designer_id")
	private List<Design> designs = new ArrayList<>(); // User designs

	// printer(user) specific fields, related with printers(object)
	@OneToMany(fetch=FetchType.EAGER)
	@JoinColumn(name = "impresor_id")
	private List<Printer> printer = new ArrayList<>(); // list to append all user printers available

	// application-specific fields
	private String firstName;
	private String lastName;

	@OneToMany
	@JoinColumn(name = "sender_id")
	private List<Message> sent = new ArrayList<>();
	@OneToMany
	@JoinColumn(name = "recipient_id")
	private List<Message> received = new ArrayList<>();


	@ManyToMany
    @JoinTable(
            name = "userLikes",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "design_id")}
    )
    private List<Design> likedDesigns = new ArrayList<>();
	public void addDesignLike(Design d){
		this.likedDesigns.add(d);
	}
	// utility methods

	/**
	 * Checks whether this user has a given role.
	 * 
	 * @param role to check
	 * @return true iff this user has that role.
	 */
	public boolean hasRole(Role role) {
		String roleName = role.name();
		return Arrays.stream(roles.split(",")).anyMatch(r -> r.equals(roleName));
	}

	public void addPrinter(Printer p){
		this.printer.add(p);
	}

	@Getter
	@AllArgsConstructor
	public static class Transfer {
		private long id;
		private String username;
		private int totalReceived;
		private int totalSent;
	}

	@Override
	public Transfer toTransfer() {
		return new Transfer(id, username, received.size(), sent.size());
	}

	@Override
	public String toString() {
		return username + " (id: " + id + " role: " + roles + " venta: " +  saleId + ")";
	}
}