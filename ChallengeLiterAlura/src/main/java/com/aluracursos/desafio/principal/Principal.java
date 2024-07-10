package com.aluracursos.desafio.principal;

import com.aluracursos.desafio.model.Datos;
import com.aluracursos.desafio.model.DatosAutor;
import com.aluracursos.desafio.model.DatosLibros;
import com.aluracursos.desafio.service.ConsumoAPI;
import com.aluracursos.desafio.service.ConvierteDatos;


import java.util.*;


public class Principal {
    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private Scanner teclado = new Scanner(System.in);

    public void muestraElMenu(){
        var json = consumoAPI.obtenerDatos(URL_BASE);
        System.out.println(json);
        var datos = conversor.obtenerDatos(json,Datos.class);
        System.out.println(datos);


        while (true) {
            System.out.println(" ");
            System.out.println("**************************************");
            System.out.println("Sea bienvenido/a al buscador de libros:");
            System.out.println();
            System.out.println("1) Buscar libro por título");
            System.out.println("2) Listar libros registrados");
            System.out.println("3) Listar autores registrados");
            System.out.println("4) Listar autores vivos en un determinado año");
            System.out.println("5) Listar libros por idioma");
            System.out.println("0) Salir");
            System.out.println("Elija una opción válida: ");
            System.out.println("**************************************");

            int option = -1;
            try {
                option = teclado.nextInt();
                teclado.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Entrada no válida. Por favor, ingrese un número entero.");
                teclado.next(); // Clear the invalid input
                continue;
            }

            if (option == 0) {
                System.out.println("Finalizo el buscador de libros.");
                break;
            }

            switch (option) {
                case 1:
                    System.out.println("Ingrese el nombre del libro que desea buscar");
                    var tituloLibro = teclado.nextLine();
                    if (tituloLibro.isEmpty()) {
                        System.out.println("No ingresó un título. Inténtelo de nuevo.");
                        break;
                    }
                    json = consumoAPI.obtenerDatos(URL_BASE+"?search=" + tituloLibro.replace(" ","+"));
                    var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
                    Optional<DatosLibros> libroBuscado = datosBusqueda.resultados().stream()
                            .filter(l -> l.titulo().toUpperCase().contains(tituloLibro.toUpperCase()))
                            .findFirst();

                    if(libroBuscado.isPresent()){
                        System.out.println("Libro Encontrado: ");
                        System.out.println(" ");
                        System.out.println("--------- LIBROS ---------");
                        System.out.println("Título: " + libroBuscado.get().titulo());
                        DatosLibros libro = libroBuscado.get();

                        for (DatosAutor autor : libro.autor()) {
                            System.out.println("Autor: " + autor.nombre());
                            System.out.println("Fecha de nacimiento: "+ autor.fechaDeNacimiento());
                        }

                        System.out.println("Idíoma: " + libroBuscado.get().idiomas());
                        System.out.println("Número de descargas: " + libroBuscado.get().numeroDeDescargas());
                        System.out.println("---------------------------");
                    }else {
                        System.out.println("Libro no encontrado");
                    }
                    break;
                case 2:
                    List<DatosLibros> librosRegistrados;
                    System.out.println("Lista de libros registrados: ");
                    json = consumoAPI.obtenerDatos(URL_BASE + "?search=");
                    datosBusqueda = conversor.obtenerDatos(json, Datos.class);
                    librosRegistrados = datosBusqueda.resultados();

                    for (DatosLibros libro : librosRegistrados) {
                        System.out.println("Título: " + libro.titulo());


                        for (DatosAutor autor : libro.autor()) {
                            System.out.println("Autor: " + autor.nombre());
                            System.out.println("Fecha de nacimiento: " + autor.fechaDeNacimiento());
                        }

                        System.out.println("Idioma: " + libro.idiomas());
                        System.out.println("Número de descargas: " + libro.numeroDeDescargas());
                        System.out.println("---------------------------");
                    }
                    break;
                case 3:
                    System.out.println("Lista de autores registrados: ");
                    json = consumoAPI.obtenerDatos(URL_BASE + "?search=");
                    datosBusqueda = conversor.obtenerDatos(json, Datos.class);
                    librosRegistrados = datosBusqueda.resultados();

                    // Mapa para almacenar autores y sus libros
                    Map<DatosAutor, List<String>> autoresLibrosMap = new HashMap<>();
                    for (DatosLibros libro : librosRegistrados) {
                        for (DatosAutor autor : libro.autor()) {
                            autoresLibrosMap.computeIfAbsent(autor, k -> new ArrayList<>()).add(libro.titulo());
                        }
                    }

                    // Imprimir autores y sus libros
                    for (Map.Entry<DatosAutor, List<String>> entry : autoresLibrosMap.entrySet()) {
                        DatosAutor autor = entry.getKey();
                        List<String> libros = entry.getValue();

                        System.out.println();
                        System.out.println("--------- AUTORES ---------");
                        System.out.println("Nombre: " + autor.nombre());
                        System.out.println("Fecha de nacimiento: " + autor.fechaDeNacimiento());
                        System.out.println("Fecha de fallecimiento: " + autor.fechaDeFallecimiento());
                        System.out.println("Libros: " + String.join(", ", libros));

                    }
                    break;
                case 4:
                    System.out.println("Ingrese el año vivo de autores(es) que desea buscar:");
                    int anioBusqueda = teclado.nextInt();
                    teclado.nextLine(); // Limpia el búfer de entrada después de leer el número

                    json = consumoAPI.obtenerDatos(URL_BASE + "?search=");
                    datosBusqueda = conversor.obtenerDatos(json, Datos.class);
                    librosRegistrados = datosBusqueda.resultados();

                    // Mapa para almacenar autores vivos y sus libros
                    Map<DatosAutor, List<String>> autoresVivosLibrosMap = new HashMap<>();
                    for (DatosLibros libro : librosRegistrados) {
                        for (DatosAutor autor : libro.autor()) {
                            int nacimiento = Integer.parseInt(autor.fechaDeNacimiento());
                            int fallecimiento = autor.fechaDeFallecimiento().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(autor.fechaDeFallecimiento());

                            if (nacimiento <= anioBusqueda && fallecimiento >= anioBusqueda) {
                                autoresVivosLibrosMap.computeIfAbsent(autor, k -> new ArrayList<>()).add(libro.titulo());
                            }
                        }
                    }

                    if (autoresVivosLibrosMap.isEmpty()) {
                        System.out.println("No se encontraron autores vivos en el año " + anioBusqueda + ".");
                    } else {
                        System.out.println("Lista de autores vivos en el año " + anioBusqueda + ":");
                        for (Map.Entry<DatosAutor, List<String>> entry : autoresVivosLibrosMap.entrySet()) {
                            DatosAutor autor = entry.getKey();
                            List<String> libros = entry.getValue();

                            System.out.println();
                            System.out.println("--------- AUTORES ---------");
                            System.out.println("Nombre: " + autor.nombre());
                            System.out.println("Fecha de nacimiento: " + autor.fechaDeNacimiento());
                            System.out.println("Fecha de fallecimiento: " + (autor.fechaDeFallecimiento().isEmpty() ? "N/A" : autor.fechaDeFallecimiento()));
                            System.out.println("Libros: " + String.join(", ", libros));

                        }
                    }
                    break;

                case 5:
                    System.out.println("Ingrese el idioma que deseas buscar: ");
                    System.out.println();
                    System.out.println("1) es - español");
                    System.out.println("2) en - ingles");
                    System.out.println("3) fr - frances");
                    System.out.println("4) pt - portugues");
                    int idiomaOpcion = teclado.nextInt();
                    teclado.nextLine(); // Clear the buffer

                    String codigoIdioma;
                    switch (idiomaOpcion) {
                        case 1:
                            codigoIdioma = "es";
                            break;
                        case 2:
                            codigoIdioma = "en";
                            break;
                        case 3:
                            codigoIdioma = "fr";
                            break;
                        case 4:
                            codigoIdioma = "pt";
                            break;
                        default:
                            System.out.println("Código de idioma no válido. Inténtelo de nuevo.");
                            continue;
                    }

                    json = consumoAPI.obtenerDatos(URL_BASE + "?languages=" + codigoIdioma);
                    datosBusqueda = conversor.obtenerDatos(json, Datos.class);
                    librosRegistrados = datosBusqueda.resultados();

                    System.out.println("Lista de libros en el idioma " + codigoIdioma + ":");
                    for (DatosLibros libro : librosRegistrados) {
                        System.out.println();
                        System.out.println("--------- LIBROS ---------");
                        System.out.println("Título: " + libro.titulo());

                        // Imprimir detalles de los autores
                        for (DatosAutor autor : libro.autor()) {
                            System.out.println("Autor: " + autor.nombre());
                            System.out.println("Fecha de nacimiento: " + autor.fechaDeNacimiento());
                        }

                        System.out.println("Idioma: " + libro.idiomas());
                        System.out.println("Número de descargas: " + libro.numeroDeDescargas());

                    }
                    break;
            }


        }

    }
}
