package org.example
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.math.max
//-------------------------------------------------------------------
open class Pelicula (
    val titulo: String,
    var precioBase: Double,
    val duracionMinutos: Int
) {
    init {
        require(titulo.isNotBlank()) { "El titulo no puede estar vacío" }
        require(precioBase >= 0.0) { "El precio base no puede ser 0 o negativo." }
        require(duracionMinutos > 0) { "La duración de la película debe ser mayor a 0 minutos." }
    }

    open fun calcularPrecioEntrada(): Double {
        return precioBase
    }
}
 //-------------------------------------------------------------------
 class PeliculaEstreno (
     titulo: String,
     precioBase: Double,
     duracionMinutos: Int
 ) : Pelicula (titulo, precioBase, duracionMinutos) {

     override fun calcularPrecioEntrada():Double {
         return precioBase*1.3
     }
 }
//-------------------------------------------------------------------
class PeliculaClasica (
    titulo: String,
    precioBase: Double,
    duracionMinutos: Int
) : Pelicula (titulo, precioBase, duracionMinutos) {

    override fun calcularPrecioEntrada(): Double {
        val precioConDescuento = precioBase - 1500.0
        return max(precioConDescuento, 1000.0)
    }
}
//-------------------------------------------------------------------
sealed class EstadoReserva {
    object Procesando : EstadoReserva()
    data class Exito(val mensaje: String) : EstadoReserva()
    data class Error(val motivo: String) : EstadoReserva()
}
//-------------------------------------------------------------------
suspend fun procesarPago(monto: Double): EstadoReserva {
    println("\n[Banco] Conectando con el servidor bancario...")
    delay(2000)

    return if (monto > 20000.0) {
        EstadoReserva.Error("Pago rechazado: El monto supera el limite diario permitido de $20000")
    } else {
        EstadoReserva.Exito("Pago aprobado con éxito por un total de $$monto.")
    }
}
//-------------------------------------------------------------------
fun main() = runBlocking {
    println("\n========================= KotlinPlex =========================")
    val cartelera: MutableList<Pelicula> = try {
        mutableListOf(
        PeliculaEstreno("PeliculaEstreno1", 5000.0, 120),
        PeliculaEstreno("PeliculaEstreno2", 6000.0, 130),
        PeliculaEstreno("PeliculaEstreno3", 4000.0, 110),
        PeliculaClasica("PeliculaClasica1", 5000.0, 120),
        PeliculaClasica("PeliculaClasica2", 6000.0, 90),
        )
    } catch (e: Exception) {
        println("\nError al agregar elementos al la cartelera: ${e.message}")
        return@runBlocking
    }

    try {
        cartelera.add(PeliculaEstreno("PeliculaEstrenoNueva", 6000.0, 120))
        cartelera.add(PeliculaClasica("PeliculaClasicaNueva", 4000.0, 160))
        println("\nTotal en cartelera: ${cartelera.size}")
    } catch (e: Exception) {
        println("\nError al agregar elementos a la cartelera: ${e.message}")
    }

    val peliculasCortas = cartelera.filter { it.duracionMinutos < 120 }
    println("\nPeliculas cortas (< 120 min):")
    peliculasCortas.forEach { println(" - ${it.titulo} (${it.duracionMinutos} min)") }

    val nombresMayusculas = cartelera.map { it.titulo.uppercase() }
    println("\nTitulos disponibles (Mayusculas):")
    println(nombresMayusculas)

    val costoTotalCartelera = cartelera.sumOf { it.calcularPrecioEntrada() }
    println("\nCosto total por comprar una entrada de cada pelicula: $$costoTotalCartelera")

    println("\n--- Cartelera completa ---")
    cartelera.forEach { pelicula ->
        println("\nPelicula: ${pelicula.titulo} | Duracion: ${pelicula.duracionMinutos} min | Precio Entrada: $${pelicula.calcularPrecioEntrada()}")
    }

    println("\nIniciando compra de combo familiar...")
    val estadoInicial = EstadoReserva.Procesando
    println("\nEstado actual: $estadoInicial")

    val resultadoPago = procesarPago(costoTotalCartelera)
    when (resultadoPago) {
        is EstadoReserva.Exito -> println("\n${resultadoPago.mensaje}")
        is EstadoReserva.Error -> println("\n${resultadoPago.motivo}")
        EstadoReserva.Procesando -> throw java.lang.IllegalStateException("\nNo debería quedar en procesando")
    }
}