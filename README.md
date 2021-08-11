# RestServlet
Libreria para javaEE que permite la creación de Rest apis por medio de servlets especiales para eso
Permite la creación de apis y envio de java beans en formato json como respusta de una petición. Potenciada por org.json.

## Uso
Se basa en la implementación de un objeto <b>RestServlet</b>, el cual asemeja un HttpServlet común de JavaEE con métodos:
<ul>
<li> proccessRequest </li><li> processGet </li><li> processPost </li><li> processPut </li><li> processDelete </li>
</ul>
El primero para procesar cualquier tipo de petición y el resto para procesar peticiones de los metodos especificos.
Estas funciones deben devolver un objeto de cualquiera de los siguientes tipos:
<ul>
<li> java bean </li><li> String en formato json </li><li> JSONObject </li><li> JSONArray </li><li> Arreglo de java beans </li>
</ul>
El cual será codificado a formato json y enviado como respuesta de la request.

Pendiente de crear wiki y publicar javadocs
