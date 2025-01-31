package es.ucm.fdi.iw.g06.printopolis.control;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.validation.constraints.Null;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import es.ucm.fdi.iw.g06.printopolis.LocalData;
import es.ucm.fdi.iw.g06.printopolis.model.Design;
import es.ucm.fdi.iw.g06.printopolis.model.Message;
import es.ucm.fdi.iw.g06.printopolis.model.Printer;
import es.ucm.fdi.iw.g06.printopolis.model.Sales;
import es.ucm.fdi.iw.g06.printopolis.model.Transferable;
import es.ucm.fdi.iw.g06.printopolis.model.User;
import es.ucm.fdi.iw.g06.printopolis.model.Message.Transfer;
import es.ucm.fdi.iw.g06.printopolis.model.User.Role;;

/**
 * User-administration controller
 * 
 * @author mfreire
 */
@Controller()
@RequestMapping("user")
public class UserController {

	private static final Logger log = LogManager.getLogger(UserController.class);

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private LocalData localData;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	/**
	 * Tests a raw (non-encoded) password against the stored one.
	 * 
	 * @param rawPassword     to test against
	 * @param encodedPassword as stored in a user, or returned
	 *                        y @see{encodePassword}
	 * @return true if encoding rawPassword with correct salt (from old password)
	 *         matches old password. That is, true iff the password is correct
	 */
	public boolean passwordMatches(String rawPassword, String encodedPassword) {
		return passwordEncoder.matches(rawPassword, encodedPassword);
	}

	/**
	 * Encodes a password, so that it can be saved for future checking. Notice that
	 * encoding the same password multiple times will yield different encodings,
	 * since encodings contain a randomly-generated salt.
	 * 
	 * @param rawPassword to encode
	 * @return the encoded password (typically a 60-character string) for example, a
	 *         possible encoding of "test" is
	 *         {bcrypt}$2y$12$XCKz0zjXAP6hsFyVc8MucOzx6ER6IsC1qo5zQbclxhddR1t6SfrHm
	 */
	public String encodePassword(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

	@GetMapping("/{id}")
	public String getUser(@PathVariable long id, Model model, HttpSession session) throws JsonProcessingException {
		User u = entityManager.find(User.class, id);
		model.addAttribute("user", u);
		List<Design> l = entityManager.createNamedQuery("Design.allUserDesigns", Design.class)
				.setParameter("userId", u.getId()).getResultList();
		model.addAttribute("userDesigns", l);
		List<Printer> l1 = entityManager.createNamedQuery("Printer.allUserPrinters", Printer.class)
				.setParameter("userId", u.getId()).getResultList();
		Object punt = entityManager.createNamedQuery("User.getPunctuation").setParameter("id", u.getId())
				.getSingleResult();
		
		List<Object> l2 = entityManager.createNamedQuery("Sales.getAllSales", Object.class).setParameter("id", u.getId()).getResultList();
		log.info("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH {}", l2);
		model.addAttribute("sales", l2);

		System.out.println(l1.toString());
		model.addAttribute("userPrinters", l1);
		model.addAttribute("punctuation", punt);
		log.info("Sending a message to {} with contents '{}'", id, l);

		return "profile";
	}

	@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "No eres administrador, y éste no es tu perfil") // 403
	public static class NoEsTuPerfilException extends RuntimeException {
	}

	@PostMapping("/{id}")
	@Transactional
	public String postUser(HttpServletResponse response, @PathVariable long id, @ModelAttribute User edited,
			@RequestParam(required = false) String pass2, Model model, HttpSession session) throws IOException {
		User target = entityManager.find(User.class, id);
		model.addAttribute("user", target);

		User requester = (User) session.getAttribute("u");
		if (requester.getId() != target.getId() && !requester.hasRole(Role.ADMIN)) {
			throw new NoEsTuPerfilException();
		}

		if (edited.getPassword() != null && edited.getPassword().equals(pass2)) {
			// save encoded version of password
			target.setPassword(encodePassword(edited.getPassword()));
		}
		target.setUsername(edited.getUsername());
		target.setFirstName(edited.getFirstName());
		target.setLastName(edited.getLastName());

		// update user session so that changes are persisted in the session, too
		session.setAttribute("u", target);

		return "user";
	}

	@GetMapping(value = "/{id}/photo")
	public StreamingResponseBody getPhoto(@PathVariable long id, Model model) throws IOException {
		File f = localData.getFile("user", "" + id);
		InputStream in;
		if (f.exists()) {
			in = new BufferedInputStream(new FileInputStream(f));
		} else {
			in = new BufferedInputStream(
					getClass().getClassLoader().getResourceAsStream("static/img/unknown-user.jpg"));
		}
		return new StreamingResponseBody() {
			@Override
			public void writeTo(OutputStream os) throws IOException {
				FileCopyUtils.copy(in, os);
			}
		};
	}

	@PostMapping("/{id}/msg")
	@ResponseBody
	@Transactional
	public String postMsg(@PathVariable long id, @RequestBody JsonNode o, Model model, HttpSession session)
			throws JsonProcessingException {

		String text = o.get("message").asText();
		User u = entityManager.find(User.class, id);
		User sender = entityManager.find(User.class, ((User) session.getAttribute("u")).getId());
		model.addAttribute("user", u);

		// construye mensaje, lo guarda en BD
		Message m = new Message();
		m.setRecipient(u);
		m.setSender(sender);
		m.setDateSent(LocalDateTime.now());
		m.setText(text);
		entityManager.persist(m);
		entityManager.flush(); // to get Id before commit

		// construye json
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("from", sender.getUsername());
		rootNode.put("to", u.getUsername());
		rootNode.put("text", text);
		rootNode.put("id", m.getId());
		String json = mapper.writeValueAsString(rootNode);

		log.info("Sending a message to {} with contents '{}'", id, json);

		messagingTemplate.convertAndSend("/user/" + u.getUsername() + "/queue/updates", json);
		return "{\"result\": \"message sent.\"}";
	}

	@PostMapping("/{id}/photo")
	public String postPhoto(HttpServletResponse response, @RequestParam("photo") MultipartFile photo,
			@PathVariable("id") String id, Model model, HttpSession session) throws IOException {
		User target = entityManager.find(User.class, Long.parseLong(id));
		model.addAttribute("user", target);

		// check permissions
		User requester = (User) session.getAttribute("u");
		if (requester.getId() != target.getId() && !requester.hasRole(Role.ADMIN)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "No eres administrador, y éste no es tu perfil");
			return "user";
		}

		log.info("Updating photo for user {}", id);
		File f = localData.getFile("user", id);
		if (photo.isEmpty()) {
			log.info("failed to upload photo: emtpy file?");
		} else {
			try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f))) {
				byte[] bytes = photo.getBytes();
				stream.write(bytes);
			} catch (Exception e) {
				log.warn("Error uploading " + id + " ", e);
			}
			log.info("Successfully uploaded photo for {} into {}!", id, f.getAbsolutePath());
		}
		return "user";
	}

	@Transactional
	@PostMapping("/delUser/{id}")
	public String delUser(@PathVariable long id, Model model, HttpSession session) {
		User u = entityManager.find(User.class, ((User) session.getAttribute("u")).getId());
		if (u.hasRole(User.Role.ADMIN)) {
			entityManager.createNamedQuery("User.delUserDesigns").setParameter("id", id).executeUpdate();
			entityManager.createNamedQuery("User.delUserPrinters").setParameter("id", id).executeUpdate();
			entityManager.createNamedQuery("User.delUser").setParameter("id", id).executeUpdate();
			entityManager.flush();
		}

		return "redirect:/admin/";
	}

	// @GetMapping("/username")
	@RequestMapping(value = "/username", method = RequestMethod.GET)
	@ResponseBody // <-- "lo que devuelvo es la respuesta, tal cual"
	public String getUser(@RequestParam(name = "id", required = false) String uname) {
		try {
			User u = buscaUsuarioOLanzaExcepcion(uname);
			String s = "{\"name\": \"" + u.getFirstName() + "\"}";
			return s;
		} catch (Exception e) {
			return "{\"name\": \"\"}";
		}
	}

	private User buscaUsuarioOLanzaExcepcion(String uname) throws Exception {
		User u = entityManager.createNamedQuery("User.byUsername", User.class).setParameter("username", uname)
				.getSingleResult();
		if (u != null)
			return u;
		else
			throw new Exception("Existe el usuario");
	}

	@PostMapping("/report/{id}")
	@ResponseBody
	@Transactional
	public String reportar(@PathVariable long id, Model model, HttpSession session)
			throws JsonProcessingException {

		String text = "Este mensaje es un aviso por actitud inapropiada. Mantenga las formas.";
		User u = entityManager.find(User.class, id);
		User sender = entityManager.find(User.class, ((User) session.getAttribute("u")).getId());
		model.addAttribute("user", u);

		// construye mensaje, lo guarda en BD
		Message m = new Message();
		m.setRecipient(u);
		m.setSender(sender);
		m.setDateSent(LocalDateTime.now());
		m.setText(text);
		entityManager.persist(m);
		entityManager.flush(); // to get Id before commit

		// construye json
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("from", sender.getUsername());
		rootNode.put("to", u.getUsername());
		rootNode.put("text", text);
		rootNode.put("id", m.getId());
		String json = mapper.writeValueAsString(rootNode);

		log.info("Sending a message to {} with contents '{}'", id, json);

		messagingTemplate.convertAndSend("/user/" + u.getUsername() + "/queue/updates", json);
		return "{\"result\": \"message sent.\"}";
	}
}