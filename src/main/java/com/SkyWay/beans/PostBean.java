package com.SkyWay.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.SkyWay.model.CategoriaPost;
import com.SkyWay.model.Post;
import com.SkyWay.service.CategoriaPostService;
import com.SkyWay.service.PostService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;


@Named
@ViewScoped
public class PostBean implements Serializable{

    private static final long serialVersionUID = 5274901437109168969L;
	private String contenido;  // Este será el contenido enriquecido del post
    private String titulo;
    private String descripcion;
    private CategoriaPost categoriaPost;  // Categoría asociada al post
    private Post post;
    private String query;

    private String idPost;
    
    	
    private List<Post>posts;
    
    @Inject
    private PostService postService; // Inyección del servicio PostService
    
    @Inject
    private CategoriaPostService categoriaPostService; // Inyección del servicio CategoriaPostService
    
    
    @PostConstruct
    public void init() {
    	
    	//FacesContext facesContext = FacesContext.getCurrentInstance();
    	//idPost = facesContext.getExternalContext().getRequestParameterMap().get("idPost");

    	
    	 FacesContext context = FacesContext.getCurrentInstance();
         HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
         idPost = request.getParameter("idPost");
    		
         
         if (idPost != null) {

             try {
        
                 // Supongo que el ID del post es un Integer, lo convertimos aquí
                 post = postService.findById(Integer.parseInt(idPost)).orElse(null);
               
             } catch (NumberFormatException e) {
                 // Si el id no es un número válido, podrías manejar el error
                 System.err.println("Error al parsear el parámetro idPost: " + idPost);
             }
         }else {
			System.out.println("Parametro vacio");
		}
         
         // Si necesitas cargar todos los posts, hazlo aquí
         posts = postService.findAll();
    }
    
    
    // Método para guardar el post
    public String guardarPost() {
        // Crear una nueva instancia de Post
        Post nuevoPost = new Post();
        nuevoPost.setTitulo(titulo);
        nuevoPost.setContenido(contenido);
        nuevoPost.setFecha(new Date());
        //nuevoPost.setDescripcion(descripcion);
        CategoriaPost categoria = categoriaPostService.findById(1).get();
        


        // Establecer la categoría del post (puedes ajustar este paso según tu lógica)
        if (categoria != null) {
            nuevoPost.setCategoriaPost(categoria);
        } else {
            // Si no se establece categoría, se puede asignar una predeterminada o mostrar un mensaje de error
            // Asumimos que deberías manejar el caso donde no se selecciona una categoría
            return "error.xhtml?faces-redirect=true"; // O la página de error
        }

        // Guardar el post utilizando el servicio
        postService.save(nuevoPost);

        // Guardar el contenido en la sesión Flash para mostrarlo después
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.getSessionMap().put("contenidoPost", contenido);

        // Imprimir para ver los valores guardados (esto es solo para debugging)
        System.out.println("Post guardado: " + nuevoPost);

        // Redirigir a la página de mostrar el post recién guardado
        return "mostrarPost.xhtml?faces-redirect=true"; 
    }

    // Método para obtener el contenido desde el Flash
    public String getContenidoDesdeFlash() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        String contenidoPost = (String) externalContext.getSessionMap().get("contenidoPost");
        return contenidoPost != null ? contenidoPost : "";
    }
    
    public String verPost(Post post) {
    		
        // Guardar el contenido en la sesión Flash para mostrarlo después
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.getSessionMap().put("contenidoPost", post.getContenido());
        
    	
    	
        return "mostrarPost.xhtml?faces-redirect=true"; 
    }

    
    public void buscarPost() {
    	
    	System.out.println("Posts a buscar :"+query);
    	
    	
    	
    	if(!query.isEmpty() ) {
    		
    	 var postList=posts.stream().filter(pos -> post.getTitulo().contains(query)).toList();
    	 posts=postList;	
    		//posts=postService.findByTitulo(query);
    	}
    	
    	

    }
    
    // Getters y setters
    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public CategoriaPost getCategoriaPost() {
        return categoriaPost;
    }

    public void setCategoriaPost(CategoriaPost categoriaPost) {
        this.categoriaPost = categoriaPost;
    }
    
    public List<Post> getPosts() {
		return posts;
	}
    
    public void setPosts(List<Post> posts) {
		this.posts = posts;
	}
    
    public Post getPost() {
		return post;
	}
    public void setPost(Post post) {
		this.post = post;
	}
    public String getQuery() {
		return query;
	}
    
    public void setQuery(String query) {
		this.query = query;
	}
    
}
