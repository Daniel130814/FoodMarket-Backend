package com.uade.tpo.foodmarketplace.service.imagen;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import com.uade.tpo.foodmarketplace.exceptions.plato.ImagenNotFoundException;

/** Guarda imágenes de platos en disco local con nombres únicos y las expone por URL absoluta. */
@Service
public class ImagenStorageService {

    public static final long MAX_BYTES = 5L * 1024 * 1024;

    private static final Map<String, String> EXTENSION_POR_TIPO = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");
    private static final Map<String, String> TIPO_POR_EXTENSION = Map.of(
            "jpg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp");
    private static final Pattern NOMBRE_VALIDO = Pattern
            .compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|webp)");

    private final Path directorio;
    private final String baseUrl;

    public ImagenStorageService(@Value("${app.imagenes.dir:uploads/imagenes}") String directorio,
            @Value("${app.public-base-url:http://localhost:4002}") String baseUrl) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    /** Valida y guarda el archivo; devuelve su URL absoluta pública. */
    public String guardar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Debés enviar un archivo de imagen en el campo 'file'");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessRuleException("La imagen supera el tamaño máximo de 5 MB");
        }
        String tipo = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        String extension = EXTENSION_POR_TIPO.get(tipo);
        if (extension == null) {
            throw new BusinessRuleException("Formato no permitido: solo se aceptan image/jpeg, image/png e image/webp");
        }
        try {
            if (!coincideConFirma(file, extension)) {
                throw new BusinessRuleException("El contenido del archivo no coincide con un formato de imagen permitido");
            }
            Files.createDirectories(directorio);
            String nombre = UUID.randomUUID() + "." + extension;
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, directorio.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
            }
            return baseUrl + "/imagenes/" + nombre;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la imagen", e);
        }
    }

    /** Elimina el archivo de una URL generada por este servicio (ignora URLs ajenas). */
    public void eliminarSilencioso(String url) {
        String prefijo = baseUrl + "/imagenes/";
        if (url == null || !url.startsWith(prefijo)) {
            return;
        }
        String nombre = url.substring(prefijo.length());
        if (!NOMBRE_VALIDO.matcher(nombre).matches()) {
            return;
        }
        try {
            Files.deleteIfExists(directorio.resolve(nombre));
        } catch (IOException ignored) {
            // Un archivo huérfano no debe romper la operación principal.
        }
    }

    public Resource cargar(String nombre) {
        if (!NOMBRE_VALIDO.matcher(nombre).matches()) {
            throw new ImagenNotFoundException();
        }
        Path archivo = directorio.resolve(nombre).normalize();
        if (!archivo.startsWith(directorio) || !Files.isRegularFile(archivo)) {
            throw new ImagenNotFoundException();
        }
        return new PathResource(archivo);
    }

    public String tipoContenido(String nombre) {
        return TIPO_POR_EXTENSION.get(nombre.substring(nombre.lastIndexOf('.') + 1));
    }

    private boolean coincideConFirma(MultipartFile file, String extension) throws IOException {
        byte[] h = new byte[12];
        int leidos;
        try (InputStream in = file.getInputStream()) {
            leidos = in.readNBytes(h, 0, h.length);
        }
        return switch (extension) {
            case "jpg" -> leidos >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF;
            case "png" -> leidos >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G'
                    && h[4] == 0x0D && h[5] == 0x0A && h[6] == 0x1A && h[7] == 0x0A;
            case "webp" -> leidos >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                    && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P';
            default -> false;
        };
    }
}
