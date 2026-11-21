package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        BudgetEntity::class,
        SubBudgetEntity::class,
        ExpenseEntity::class,
        DebtEntity::class,
        MonthlyBillEntity::class,
        AppSettingsEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE budgets ADD COLUMN name TEXT NOT NULL DEFAULT 'Daily Budget'")
                db.execSQL("ALTER TABLE budgets ADD COLUMN isMonthly INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE budgets ADD COLUMN isSelectedOnHome INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE app_settings ADD COLUMN themeMode TEXT NOT NULL DEFAULT 'MIDNIGHT'")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS debts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        personName TEXT NOT NULL,
                        amount REAL NOT NULL,
                        type TEXT NOT NULL,
                        note TEXT NOT NULL,
                        isSettled INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS monthly_bills (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        amount REAL NOT NULL,
                        category TEXT NOT NULL,
                        dueDayOfMonth INTEGER NOT NULL,
                        isPaidThisMonth INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE budgets ADD COLUMN showMainOnHome INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE expenses ADD COLUMN subBudgetId INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE debts ADD COLUMN epochDay INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE monthly_bills ADD COLUMN operationType TEXT NOT NULL DEFAULT 'SUBTRACT'")
                db.execSQL("ALTER TABLE monthly_bills ADD COLUMN targetSubBudgetId INTEGER DEFAULT NULL")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sub_budgets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        parentBudgetId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        allocatedAmount REAL NOT NULL,
                        isShownOnHome INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(parentBudgetId) REFERENCES budgets(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_sub_budgets_parentBudgetId ON sub_budgets(parentBudgetId)"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE debts ADD COLUMN budgetId INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE debts ADD COLUMN countInMainBudget INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE monthly_bills ADD COLUMN budgetId INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE monthly_bills ADD COLUMN countInMainBudget INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "buckwheat_offline.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
