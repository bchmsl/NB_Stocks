package com.nbstocks.nbstocks.data.remote.model

data class CurrentStockDto(
    val quoteSummary: QuoteSummary?
) {
    data class QuoteSummary(
        val error: Any?,
        val result: List<Result?>?
    ) {
        data class Result(
            val financialData: FinancialData?
        ) {
            data class FinancialData(
                val currentPrice: DataShort?,
                val earningsGrowth: DataShort?,
                val financialCurrency: String?,
                val freeCashflow: DataLong?,
                val grossMargins: DataShort?,
                val grossProfits: DataLong?,
                val maxAge: Int?,
                val operatingCashflow: DataLong?,
                val operatingMargins: DataShort?,
                val profitMargins: DataShort?,
                val recommendationKey: String?,
                val recommendationMean: DataShort?,
                val revenueGrowth: DataShort?,
                val revenuePerShare: DataShort?,
                val targetHighPrice: DataShort?,
                val targetLowPrice: DataShort?,
                val targetMeanPrice: DataShort?,
                val targetMedianPrice: DataShort?,
                val totalCash: DataLong?,
                val totalDebt: DataLong?,
                val totalRevenue: DataLong?
            ) {
                data class DataShort(
                    val raw: Double?,
                    val fmt: String?
                )

                data class DataLong(
                    val fmt: String?,
                    val longFmt: String?,
                    val raw: Long?
                )
            }
        }
    }
}