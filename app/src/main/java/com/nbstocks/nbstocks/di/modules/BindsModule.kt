package com.nbstocks.nbstocks.di.modules

import com.nbstocks.nbstocks.common.constants.ModuleParams
import com.nbstocks.nbstocks.csv.CSVParser
import com.nbstocks.nbstocks.csv.CurrentStockParser
import com.nbstocks.nbstocks.csv.StockPricesParser
import com.nbstocks.nbstocks.data.remote.model.CurrentStockDto
import com.nbstocks.nbstocks.data.remote.model.StockPricesDto
import com.nbstocks.nbstocks.data.repositories.company_listings.CompanyListingsRepositoryImpl
import com.nbstocks.nbstocks.data.repositories.current_stock.CurrentStockRepositoryImpl
import com.nbstocks.nbstocks.data.repositories.daily_stock.DailyStockPricesPricesRepositoryImpl
import com.nbstocks.nbstocks.domain.repositories.company_listings.CompanyListingsRepository
import com.nbstocks.nbstocks.domain.repositories.current_stock.CurrentStockRepository
import com.nbstocks.nbstocks.domain.repositories.daily_stock.DailyStockPricesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindsModule {

    @Binds
    @Singleton
    abstract fun bindCompanyListingsRepository(
        companyListingsRepositoryImpl: CompanyListingsRepositoryImpl
    ): CompanyListingsRepository

    @Binds
    @Singleton
    abstract fun bindDailyStockRepository(
        dailyStockPricesRepositoryImpl: DailyStockPricesPricesRepositoryImpl
    ): DailyStockPricesRepository

    @Binds
    @Singleton
    abstract fun bindCurrentStockRepository(
        currentStockRepositoryImpl: CurrentStockRepositoryImpl
    ): CurrentStockRepository

    @Binds
    @Singleton
    @Named(ModuleParams.STOCK_PRICES_PARSER)
    abstract fun bindStockPricesParser(
        stockPricesParser: StockPricesParser
    ): CSVParser<StockPricesDto>

    @Binds
    @Singleton
    @Named(ModuleParams.CURRENT_STOCK_PARSER)
    abstract fun bindCurrentStockParser(
        currentStockParser: CurrentStockParser
    ): CSVParser<CurrentStockDto>
}