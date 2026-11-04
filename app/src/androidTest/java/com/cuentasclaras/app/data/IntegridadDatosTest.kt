package com.cuentasclaras.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cuentasclaras.app.data.local.AppDatabase
import com.cuentasclaras.app.data.local.ConsorcioEntity
import com.cuentasclaras.app.data.local.PropietarioEntity
import com.cuentasclaras.app.data.local.VotoEntity
import com.cuentasclaras.app.data.repository.GastoRepositoryImpl
import com.cuentasclaras.app.data.repository.VotacionRepositoryImpl
import com.cuentasclaras.app.domain.model.*
import com.cuentasclaras.app.domain.usecase.RegistrarVotoUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Pruebas de integridad de datos sobre una base Room en memoria.
 * Usan los repositorios y el caso de uso reales, igual que la app.
 */
@RunWith(AndroidJUnit4::class)
class IntegridadDatosTest {

    private lateinit var db: AppDatabase
    private lateinit var gastoRepository: GastoRepositoryImpl
    private lateinit var votacionRepository: VotacionRepositoryImpl
    private var consorcioId = 0L
    private lateinit var propietario: Propietario

    @Before
    fun crearBase() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        gastoRepository = GastoRepositoryImpl(db.gastoDao())
        votacionRepository = VotacionRepositoryImpl(db.votacionDao())

        val infoDao = db.infoConsorcioDao()
        consorcioId = infoDao.insertarConsorcio(
            ConsorcioEntity(nombre = "Consorcio de prueba", direccion = "Calle 123", unidadesTotales = 10)
        )
        val propietarioId = infoDao.insertarPropietario(
            PropietarioEntity(consorcioId = consorcioId, nombre = "Ana", unidad = "1A", piso = "1", coeficiente = 2.45, esMiembroConsejo = true)
        )
        propietario = infoDao.observarPropietario(propietarioId).first()!!.let {
            Propietario(it.id, it.consorcioId, it.nombre, it.unidad, it.piso, it.coeficiente, it.esMiembroConsejo)
        }
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    // 1. Editar un gasto no debe borrar sus presupuestos ni sus firmas.
    @Test
    fun editarGasto_conservaPresupuestosYFirmas() = runBlocking {
        val gastoId = gastoRepository.guardarGasto(gastoDePrueba(titulo = "Caño troncal"))
        val presupuestoId = gastoRepository.guardarPresupuesto(
            Presupuesto(gastoId = gastoId, proveedor = "Plomería SA", montoCentavos = 100_000_00, plazoObra = "5 días",
                garantiaMeses = 6, estadoArt = EstadoArt.AL_DIA, calificacion = 4.5, consorciosAuditados = 2, notas = "")
        )
        gastoRepository.guardarFirma(
            FirmaConsejo(gastoId = gastoId, presupuestoId = presupuestoId, miembroNombre = "Ana", miembroUnidad = "1A",
                propietarioId = propietario.id, firmado = true, fechaFirma = Instant.now())
        )

        val gastoGuardado = gastoRepository.observarGasto(gastoId).first()!!
        val idDevuelto = gastoRepository.guardarGasto(gastoGuardado.copy(titulo = "Caño troncal (editado)"))

        assertEquals(gastoId, idDevuelto)
        assertEquals("Caño troncal (editado)", gastoRepository.observarGasto(gastoId).first()!!.titulo)
        assertEquals(1, gastoRepository.observarPresupuestos(gastoId).first().size)
        assertEquals(1, gastoRepository.observarFirmas(gastoId).first().size)
    }

    // 2. Una votación con votos no se puede editar: no cambian condiciones, opciones ni votos.
    @Test
    fun editarVotacionConVotos_esRechazadoYNoModificaNada() = runBlocking {
        val votacionId = crearVotacionActiva()
        val opcionesOriginales = votacionRepository.observarOpciones(votacionId).first()
        val resultado = RegistrarVotoUseCase(votacionRepository)(votacionId, opcionesOriginales[0].id, propietario)
        assertTrue(resultado is RegistrarVotoUseCase.Result.Exito)

        val votacionOriginal = votacionRepository.observarVotacion(votacionId).first()!!
        val edicion = votacionOriginal.copy(
            titulo = "Título cambiado",
            quorumRequerido = 10.0,
            cierre = votacionOriginal.cierre.plus(30, ChronoUnit.DAYS)
        )
        val nuevasOpciones = listOf(
            OpcionVoto(votacionId = votacionId, titulo = "Otra opción", descripcion = "", tipo = TipoOpcion.A_FAVOR, recomendada = false, orden = 0)
        )

        val idGuardado = votacionRepository.guardarVotacionConOpciones(edicion, nuevasOpciones)

        assertNull(idGuardado)
        assertEquals(votacionOriginal, votacionRepository.observarVotacion(votacionId).first())
        assertEquals(opcionesOriginales, votacionRepository.observarOpciones(votacionId).first())
        val votos = votacionRepository.observarVotos(votacionId).first()
        assertEquals(1, votos.size)
        assertEquals(opcionesOriginales[0].id, votos[0].opcionId)
    }

    // 2b. Una votación sin votos sí se puede editar, y sus opciones quedan guardadas.
    @Test
    fun editarVotacionSinVotos_guardaLosCambios() = runBlocking {
        val votacionId = crearVotacionActiva()
        val votacion = votacionRepository.observarVotacion(votacionId).first()!!
        val nuevasOpciones = listOf(
            OpcionVoto(votacionId = votacionId, titulo = "Sí", descripcion = "", tipo = TipoOpcion.A_FAVOR, recomendada = true, orden = 0),
            OpcionVoto(votacionId = votacionId, titulo = "No", descripcion = "", tipo = TipoOpcion.EN_CONTRA, recomendada = false, orden = 1)
        )

        val idGuardado = votacionRepository.guardarVotacionConOpciones(votacion.copy(titulo = "Título corregido"), nuevasOpciones)

        assertEquals(votacionId, idGuardado)
        assertEquals("Título corregido", votacionRepository.observarVotacion(votacionId).first()!!.titulo)
        assertEquals(listOf("Sí", "No"), votacionRepository.observarOpciones(votacionId).first().map { it.titulo })
    }

    // 3. Un segundo voto, aunque sea por otra opción, nunca reemplaza al original.
    @Test
    fun votoDuplicado_conservaElVotoOriginal() = runBlocking {
        val votacionId = crearVotacionActiva()
        val opciones = votacionRepository.observarOpciones(votacionId).first()
        val registrarVoto = RegistrarVotoUseCase(votacionRepository)

        val primero = registrarVoto(votacionId, opciones[0].id, propietario)
        val segundo = registrarVoto(votacionId, opciones[1].id, propietario)

        assertTrue(primero is RegistrarVotoUseCase.Result.Exito)
        assertEquals(RegistrarVotoUseCase.Result.Error.YaVoto, segundo)

        // Inserción directa en la base, salteando la verificación previa del caso de uso
        // (simula dos toques simultáneos): la restricción única la rechaza.
        val idDuplicado = db.votacionDao().insertarVoto(
            VotoEntity(votacionId = votacionId, opcionId = opciones[1].id, propietarioId = propietario.id,
                coeficiente = propietario.coeficiente, fechaHora = Instant.now(), comprobante = "DUPLICADO")
        )
        assertEquals(-1L, idDuplicado)

        val votos = votacionRepository.observarVotos(votacionId).first()
        assertEquals(1, votos.size)
        assertEquals(opciones[0].id, votos[0].opcionId)
        assertEquals((primero as RegistrarVotoUseCase.Result.Exito).comprobante, votos[0].comprobante)
    }

    private fun gastoDePrueba(titulo: String) = Gasto(
        consorcioId = consorcioId,
        titulo = titulo,
        descripcion = "",
        tipo = TipoGasto.EXTRAORDINARIA,
        rubro = RubroGasto.MANTENIMIENTO,
        montoCentavos = 100_000_00,
        fecha = LocalDate.now(),
        proveedor = "A definir",
        tipoComprobante = TipoComprobante.FACTURA_A,
        numeroComprobante = "0001-0001"
    )

    private suspend fun crearVotacionActiva(): Long {
        val ahora = Instant.now()
        val votacion = Votacion(
            consorcioId = consorcioId,
            numero = 1,
            titulo = "Arreglo de terraza",
            descripcion = "",
            tipoAsamblea = TipoAsamblea.ORDINARIA,
            apertura = ahora.minus(1, ChronoUnit.DAYS),
            cierre = ahora.plus(1, ChronoUnit.DAYS)
        )
        val opciones = listOf(
            OpcionVoto(votacionId = 0, titulo = "Aprobar", descripcion = "", tipo = TipoOpcion.A_FAVOR, recomendada = true, orden = 0),
            OpcionVoto(votacionId = 0, titulo = "Rechazar", descripcion = "", tipo = TipoOpcion.EN_CONTRA, recomendada = false, orden = 1)
        )
        return votacionRepository.guardarVotacionConOpciones(votacion, opciones)!!
    }
}
