package es.ucm.fdi.iw.g06.printopolis.control;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.catalina.security.SecurityConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import antlr.debug.Event;
import ch.qos.logback.core.net.LoginAuthenticator;
import es.ucm.fdi.iw.g06.printopolis.LocalData;
import es.ucm.fdi.iw.g06.printopolis.LoginSuccessHandler;
import es.ucm.fdi.iw.g06.printopolis.model.Design;
import es.ucm.fdi.iw.g06.printopolis.model.Evento;
import es.ucm.fdi.iw.g06.printopolis.model.Printer;
import es.ucm.fdi.iw.g06.printopolis.model.User;
import es.ucm.fdi.iw.g06.printopolis.model.SalesLine;
import es.ucm.fdi.iw.g06.printopolis.model.Sales;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Controller()
@RequestMapping("sale")
public class SalesController {
	private static final Logger log = LogManager.getLogger(SalesController.class);

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private LocalData localData;


	@GetMapping("/{id}")
	public String getSale(@PathVariable long id, Model model) throws IOException {
	   List<SalesLine> l = entityManager.createNamedQuery("SalesLine.salesProducts", SalesLine.class).setParameter("id", id).getResultList();
       model.addAttribute("products", l);
	   
       return "cart";
	}

    @GetMapping("/{id}/payments")
	public String getPayment(@PathVariable long id, Model model) throws IOException {
	   Sales l = entityManager.createNamedQuery("Sales.sale", Sales.class).setParameter("id", id).getSingleResult();
       model.addAttribute("sales", l);
       return "payment";
	}

	@GetMapping("/")
	public String openCart(Model model, HttpSession session) throws IOException {
	   User u = entityManager.find(User.class, ((User)session.getAttribute("u")).getId());
	   List<SalesLine> l;
	   if(u.getSaleId() != null)
	   l = entityManager.createNamedQuery("SalesLine.salesProducts", SalesLine.class).setParameter("id", u.getSaleId().getId()).getResultList();
	   else
	   l = new ArrayList<SalesLine>();

	   List<Printer> p = entityManager.createNamedQuery("Printer.allPrinters", Printer.class).getResultList();

       model.addAttribute("products", l);
	   model.addAttribute("printers", p);
	//    model.addAttribute("idDesign", l.) NECESITO EL ID DEL DISEÑOOOO
       return "cart";
	}

	@Transactional
	@PostMapping("/{id}/processPayment")
	public String pay(@PathVariable long id, Model model, HttpSession session) throws IOException {
		Sales compra = entityManager.find(Sales.class, id);
		compra.setPaid(true);
		entityManager.refresh(compra);
		User us = entityManager.find(User.class, ((User)session.getAttribute("u")).getId());
		us.setSaleId(null);
		return "redirect:/user/" + us.getId();
	 }

	@Transactional
	@ResponseBody
	@GetMapping("/numberDesign")
	public String pay(Model model, HttpSession session) throws IOException {
		try{
		User u = entityManager.find(User.class, ((User)session.getAttribute("u")).getId());
		Long cont = entityManager.createNamedQuery("SalesLine.numProducts", Long.class).setParameter("id", u.getId()).getSingleResult();
		return "{\"num\": \"" + cont + "\"}";
		}catch(Exception e){
			return "{\"num\": \"" + 0 + "\"}";
		}
	 }

	 @Transactional
	 @GetMapping("/printerChoice/{id}")
	public String printerChoice(@PathVariable long id, Model model, HttpSession session) throws IOException {
		List<Evento> kes = entityManager.createNamedQuery("Evento.getPrinterEvents", Evento.class).setParameter("id", id).getResultList();
		model.addAttribute("eventos", kes);
		return "printerTurn";
	 }
	 @PostMapping("/addEvent")
	 @Transactional
	 @ResponseBody
	 public String addToCart(@RequestBody JsonNode o, Model model, HttpSession session) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Date date = mapper.convertValue(o.get("evento").get("date"), Date.class);
		log.info("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHLAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARGO {}", date);
		Evento e = new Evento();
		e.setFechaPedido(date);
		//e.setImpresora(impresora);
		//e.setSale(sale);
		//e.setUser();
		entityManager.persist(e);
		entityManager.flush();
		return "{\"name\": \"" + e.getId() + "\"}";
	 }
}
