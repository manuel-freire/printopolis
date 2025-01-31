$(document).ready(function () {

  //Modal para añadir diseño
  var modal = document.getElementById("addDesignModal");

  // Get the button that opens the modal
  var btn = document.getElementById("boton_design");

  // Get the <span> element that closes the modal
  var span = document.getElementsByClassName("close")[0];

  // When the user clicks the button, open the modal
  if (btn != null)
    btn.onclick = function () {
      modal.style.display = "block";
    }

  // When the user clicks on <span> (x), close the modal
  if (span != null)
    span.onclick = function () {
      modal.style.display = "none";
    }

  // When the user clicks anywhere outside of the modal, close it
  window.onclick = function (event) {
    if (event.target == modal) {
      modal.style.display = "none";
    }
  }

  //Modal para añadir impresora
  var modalP = document.getElementById("addPrinterModal");

  // Get the button that opens the modal
  var btn1 = document.getElementById("boton_printer");

  // Get the <span> element that closes the modal
  var span = document.getElementsByClassName("close1")[0];

  // When the user clicks the button, open the modal
  if (btn1 != null)
    btn1.onclick = function () {
      modalP.style.display = "block";
    }

  // When the user clicks on <span> (x), close the modal
  if (span != null)
    span.onclick = function () {
      modalP.style.display = "none";
    }

  // When the user clicks anywhere outside of the modal, close it
  // window.onclick = function (event) {
  //   if (event.target == modalP) {
  //     modalP.style.display = "none";
  //   }
  // }

  jQuery('.btn[href^="#' + '"]').click(function (e) {
    e.preventDefault();
    var href = jQuery(this).attr('href');
    jQuery(href).modal('toggle');
  });

  var uploadField = document.getElementById("modelo3D");

  if (uploadField != null)
    uploadField.onchange = function () {
      if (this.files[0].size > 1048576) {
        alert("El archivo es demasiado grande");
        this.value = "";
      };
    };
});


document.addEventListener("DOMContentLoaded", () => {
  // selector para elegir sobre qué elementos validar
  let u = document.querySelectorAll('#email')[0]
  let v = document.querySelectorAll('#username')[0]
  // cada vez que cambien, los revalidamos

  if (u != null) {
    u.oninput = u.onchange =
      async () => u.setCustomValidity( // si "", válido; si no, inválido
        await validaUsername(u.value))

  }

  if (v != null) {
    v.oninput = v.onchange =
      async () => v.setCustomValidity( // si "", válido; si no, inválido
        await existeUsuarioLogin(v.value))

  }

  //Carga de manera dinámica los elementos que hay en el carrito
  let cart = document.getElementById('lblCartCount');
  if(cart != null)
    designCarrito();

  //   //Borrar producto del carrito
  //   $(".del").on('click', function (e) {
  //     //let idOfTarget = document.getElementById("disenio").value;

  //      let url = this.parentElement.action
      
  //     // let id;
  //     // let name = this.name
  //     // let form = document.getElementById(name)
  //     // let url = form.action

  //     e.preventDefault(); // <-- evita que se envíe de la forma normal
  //     go(url, // <-- hace el `fetch`, y recibe resultados //config.rootUrl + "sale/delProduct/" + id, 'POST'
  //         {})
  //         .then(d => { console.log("report done", d); })
  //         .catch(e => console.log("bad luck, evil is better", e))
  // });
})

async function go(url, method, data = {}) {
  let params = {
    method: method, // POST, GET, POST, PUT, DELETE, etc.
    headers: {
      "Content-Type": "application/json; charset=utf-8",
    },
    body: JSON.stringify(data)
  };
  if (method === "GET") {
    // GET requests cannot have body
    delete params.body;
  }
  else {
    params.headers["X-CSRF-TOKEN"] = config.csrf.value;
  }
  console.log("sending", url, params)
  return fetch(url, params).then((response) => {
    if (response.ok) {
      return data = response.json();
    } else {
      response.text().then(t => { throw new Error(t + ", at " + url) });
    }
  })

}

async function validaUsername(username) {
  let params = { username: username };
  // Spring Security lo añade en formularios html, pero no en Ajax
  params[config.csrf.name] = config.csrf.value;
  // petición en sí
  return go(config.rootUrl + "user/username?id=" + username, 'GET', params)
    .then(response => {
      if (response.name == "")
        return "";
      else
        return "Nombre de usuario inválido o duplicado";
    });
}

async function existeUsuarioLogin(username) {
  let params = { username: username };
  // Spring Security lo añade en formularios html, pero no en Ajax
  params[config.csrf.name] = config.csrf.value;
  // petición en sí
  return go(config.rootUrl + "user/username?id=" + username, 'GET', params)
    .then(response => {
      if (response.name != "")
        return "";
      else
        return "Usuario no registrado en nuestra plataforma";
    });
}

//Incremento num elementos del carrito
function designCarrito() {
  let params = {};
  // Spring Security lo añade en formularios html, pero no en Ajax
  // params[config.csrf.name] = config.csrf.value;
  // petición en sí
  return go(config.rootUrl + "sale/numberDesign", 'GET', params)
    .then((response) => {
      var num = response.num;
      document.getElementById('lblCartCount').innerHTML = num;
    })
}

//Incremento de likes
async function designLiked(id) {
  let params = {};
  // Spring Security lo añade en formularios html, pero no en Ajax
  // params[config.csrf.name] = config.csrf.value;
  // petición en sí
  return go(config.rootUrl + "design/numLikes?id=" + id, 'GET', params)
    .then((response) => {
      var num = response.num;
     
      document.getElementById(id).innerHTML = num;
    })
}






