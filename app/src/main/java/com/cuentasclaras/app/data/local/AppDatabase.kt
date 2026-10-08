package com.cuentasclaras.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ConsorcioEntity::class,
        PropietarioEntity::class,
        ExpensaEntity::class,
        TopeMensualEntity::class,
        GastoEntity::class,
        PresupuestoEntity::class,
        FirmaConsejoEntity::class,
        ConceptoAdministrativoEntity::class,
        VotacionEntity::class,
        OpcionVotoEntity::class,
        VotoEntity::class,
        AlertaEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gastoDao(): GastoDao
    abstract fun conceptoDao(): ConceptoDao
    abstract fun votacionDao(): VotacionDao
    abstract fun alertaDao(): AlertaDao
    abstract fun infoConsorcioDao(): InfoConsorcioDao
}
