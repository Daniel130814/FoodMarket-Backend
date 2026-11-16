package com.uade.tpo.foodmarketplace.service.resena;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Usa la lista real de recursos para validar la normalización y la comparación por palabra entera. */
class FiltroLenguaOfensivoTest {

    private final FiltroLenguaOfensivo filtro = new FiltroLenguaOfensivo(FiltroLenguaOfensivo.RECURSO_DEFAULT);

    @Test
    void detectaUnInsultoSimple() {
        assertTrue(filtro.contieneLenguajeOfensivo("sos un pelotudo"));
    }

    @Test
    void ignoraMayusculas() {
        assertTrue(filtro.contieneLenguajeOfensivo("QUE PELOTUDO"));
    }

    @Test
    void ignoraTildes() {
        assertTrue(filtro.contieneLenguajeOfensivo("qué imbécil"));
        assertTrue(filtro.contieneLenguajeOfensivo("qué IMBÉCIL"));
    }

    @Test
    void detectaOfuscacionConSimbolosYNumeros() {
        assertTrue(filtro.contieneLenguajeOfensivo("p3l0tud0"));
        assertTrue(filtro.contieneLenguajeOfensivo("qué b0lud0"));
        assertTrue(filtro.contieneLenguajeOfensivo("que $orete"));
        assertTrue(filtro.contieneLenguajeOfensivo("m13rd@"));
    }

    @Test
    void detectaLetrasRepetidas() {
        assertTrue(filtro.contieneLenguajeOfensivo("pelotuuuudo"));
        assertTrue(filtro.contieneLenguajeOfensivo("boooludo"));
    }

    @Test
    void detectaSeparadoresEntreLetras() {
        assertTrue(filtro.contieneLenguajeOfensivo("p.e.l.o.t.u.d.o"));
        assertTrue(filtro.contieneLenguajeOfensivo("b o l u d o"));
        assertTrue(filtro.contieneLenguajeOfensivo("i-d-i-o-t-a"));
    }

    @Test
    void detectaPluralesSimples() {
        assertTrue(filtro.contieneLenguajeOfensivo("son unos pelotudos"));
    }

    @Test
    void noBloqueaPalabrasLegitimasQueContienenUnaMalaAdentro() {
        // "pija" dentro de "pijama", "culo" dentro de "particulo", "puta" dentro de "computadora".
        assertFalse(filtro.contieneLenguajeOfensivo("Vine en pijama a probar el plato"));
        assertFalse(filtro.contieneLenguajeOfensivo("Pedí desde la computadora y llegó caliente"));
        assertFalse(filtro.contieneLenguajeOfensivo("El espectáculo gastronómico fue un acierto"));
    }

    @Test
    void noBloqueaFrasesNormalesConNumerosYPuntuacion() {
        assertFalse(filtro.contieneLenguajeOfensivo("Pedí 3 porciones, llegaron en 45 minutos. Muy rico!"));
        assertFalse(filtro.contieneLenguajeOfensivo("A B C"));
    }

    @Test
    void comentarioNuloOVacioNoSeValida() {
        assertFalse(filtro.contieneLenguajeOfensivo(null));
        assertFalse(filtro.contieneLenguajeOfensivo(""));
        assertFalse(filtro.contieneLenguajeOfensivo("   "));
    }

    @Test
    void comentarioValidoLargoPasa() {
        String largo = ("La pasta estaba en su punto justo, la salsa tenía un sabor casero increíble y el "
                + "envío llegó antes de lo prometido. Volvería a pedir sin dudarlo, además el chef incluyó "
                + "una nota muy amable. ").repeat(10);
        assertFalse(filtro.contieneLenguajeOfensivo(largo));
    }
}
