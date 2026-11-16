package com.uade.tpo.foodmarketplace.service.resena;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Detecta lenguaje ofensivo en comentarios. Normaliza el texto (minúsculas, sin tildes, sin ofuscación
 * típica) y compara palabra entera contra una lista externa editable, para no bloquear palabras legítimas
 * que contienen una mala adentro.
 */
@Component
public class FiltroLenguaOfensivo {

    static final String RECURSO_DEFAULT = "palabras-prohibidas-es.txt";

    private static final Pattern DIACRITICOS = Pattern.compile("\\p{M}+");
    private static final Pattern NO_LETRAS = Pattern.compile("[^a-z]+");
    private static final int MIN_LETRAS_SEPARADAS = 3;

    private final Set<String> palabras;

    public FiltroLenguaOfensivo(
            @Value("${app.moderacion.palabras-prohibidas:" + RECURSO_DEFAULT + "}") String recurso) {
        this.palabras = cargar(recurso);
    }

    /** Constructor para tests con una lista propia (palabras ya sin tildes ni mayúsculas). */
    FiltroLenguaOfensivo(Set<String> palabras) {
        this.palabras = new HashSet<>();
        palabras.forEach(p -> this.palabras.add(colapsarRepetidas(p)));
    }

    public boolean contieneLenguajeOfensivo(String texto) {
        if (texto == null || texto.isBlank()) {
            return false;
        }
        String normalizado = normalizar(texto);

        List<String> tokens = new ArrayList<>();
        for (String token : NO_LETRAS.split(normalizado)) {
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }
        for (String token : tokens) {
            if (esProhibida(token)) {
                return true;
            }
        }
        // p.e.l.o.t.u.d.o / p e l o t u d o: se unen corridas de letras sueltas y se comparan juntas.
        StringBuilder corrida = new StringBuilder();
        int largo = 0;
        for (String token : tokens) {
            if (token.length() == 1) {
                corrida.append(token);
                largo++;
                continue;
            }
            if (largo >= MIN_LETRAS_SEPARADAS && esProhibida(corrida.toString())) {
                return true;
            }
            corrida.setLength(0);
            largo = 0;
        }
        return largo >= MIN_LETRAS_SEPARADAS && esProhibida(corrida.toString());
    }

    private boolean esProhibida(String palabra) {
        String colapsada = colapsarRepetidas(palabra);
        if (palabras.contains(colapsada)) {
            return true;
        }
        // Plurales simples: "pelotudos", "boludes".
        if (colapsada.length() > 3 && colapsada.endsWith("s")) {
            String singular = colapsada.substring(0, colapsada.length() - 1);
            return palabras.contains(singular);
        }
        return false;
    }

    static String normalizar(String texto) {
        String s = Normalizer.normalize(texto.toLowerCase(java.util.Locale.ROOT), Normalizer.Form.NFD);
        s = DIACRITICOS.matcher(s).replaceAll("");
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            sb.append(switch (c) {
                case '0' -> 'o';
                case '1' -> 'i';
                case '3' -> 'e';
                case '4', '@' -> 'a';
                case '5', '$' -> 's';
                default -> c;
            });
        }
        return sb.toString();
    }

    /** "pelotuuudo" y "pelotudo" quedan iguales; la lista se colapsa igual al cargarse. */
    static String colapsarRepetidas(String palabra) {
        StringBuilder sb = new StringBuilder(palabra.length());
        char anterior = 0;
        for (char c : palabra.toCharArray()) {
            if (c != anterior) {
                sb.append(c);
                anterior = c;
            }
        }
        return sb.toString();
    }

    private static Set<String> cargar(String recurso) {
        Set<String> resultado = new HashSet<>();
        try (InputStream in = new ClassPathResource(recurso).getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String limpia = linea.strip();
                if (limpia.isEmpty() || limpia.startsWith("#")) {
                    continue;
                }
                String palabra = NO_LETRAS.matcher(normalizar(limpia)).replaceAll("");
                if (!palabra.isEmpty()) {
                    resultado.add(colapsarRepetidas(palabra));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la lista de palabras prohibidas: " + recurso, e);
        }
        return resultado;
    }
}
