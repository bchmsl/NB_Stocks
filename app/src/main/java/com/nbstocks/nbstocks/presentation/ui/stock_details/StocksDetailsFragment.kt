package com.nbstocks.nbstocks.presentation.ui.stock_details

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.nbstocks.nbstocks.R
import com.nbstocks.nbstocks.common.extensions.*
import com.nbstocks.nbstocks.databinding.FragmentStockDetailsBinding
import com.nbstocks.nbstocks.presentation.ui.base.BaseFragment
import com.nbstocks.nbstocks.presentation.ui.home.model.TradeHistoryUiModel
import com.nbstocks.nbstocks.presentation.ui.stock_details.model.CurrentStockUiModel
import com.nbstocks.nbstocks.presentation.ui.stock_details.model.IntervalStockPricesUiModel
import com.nbstocks.nbstocks.presentation.ui.stock_details.model.UsersStockUiModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class StocksDetailsFragment :
    BaseFragment<FragmentStockDetailsBinding>(FragmentStockDetailsBinding::inflate) {

    private val viewModel: StocksDetailsViewModel by viewModels()
    private val args: StocksDetailsFragmentArgs by navArgs()
    private val stockPricesChart by lazy { StockPricesChart() }
    private var amount = DEFAULT_AMOUNT_STOCK
    private var balance = DEFAULT_BALANCE

    override fun start() {
        setupChart()
        observe()
        listeners()
    }

    private fun setupChart() {
        val viewColor = binding.chart.background as ColorDrawable
        val hexColor = String.format("#%06X", 0xFFFFFF and viewColor.color)
        stockPricesChart.apply {
            backgroundColor = hexColor
            initChart()
        }
        binding.chart.apply {
            setBackgroundColor(hexColor)
            setChart(stockPricesChart.waterfall)
            setProgressBar(binding.pbChartLoader)
        }
        binding.tlSwitchStocks.doSelectedTask { range, interval ->
            viewModel.getStocksDetails(args.stockSymbol, range, interval)
        }
    }

    private fun observe() {
        asynchronously {
            viewModel.loaderState.collect {
                binding.progressBar.isVisible = it
            }
        }

        asynchronously {
            viewModel.intervalStockPricesViewState.collectViewState(binding) {
                handleIntervalSuccess(it)
            }
        }
        asynchronously {
            viewModel.getCurrentStock(args.stockSymbol)
            viewModel.currentStockViewState.collectViewState(binding) {
                handleCurrentStockSuccess(it)
            }
        }

        asynchronously {
            viewModel.getStockAmount(args.stockSymbol)
        }

        asynchronously {
            viewModel.amountOfStock.collectViewState(binding) {
                amount = it.toDouble()
            }
        }

        asynchronously {
            viewModel.getBalance()
            viewModel.usersBalanceState.collectViewState(binding) {
                balance = it.toDouble()
            }
        }
        asynchronously {
            viewModel.getWatchlistItems()
            viewModel.watchlistItems.collectViewState(binding) {
                binding.tbFavorite.isChecked = it.contains(args.stockSymbol)
            }
        }
    }

    private fun handleCurrentStockSuccess(currentStockUiModel: CurrentStockUiModel) {
        binding.apply {
            with(currentStockUiModel) {
                tvCurrentPrice.text = currentPrice?.raw.toCurrencyString()
                tvPrice.text = currentPrice?.raw.toCurrencyString()
                tvSymbol.text = symbol
                tvLowPrice.text = targetLowPrice?.raw.toCurrencyString()
                tvHighPrice.text = targetHighPrice?.raw.toCurrencyString()
                tvPercentage.text =
                    if (args.stockChangePercent == 0.0f){
                        revenueGrowth?.raw.toPercentStringTimes100()
                    }else{
                        args.stockChangePercent.toDouble().toPercentString()
                    }
                tvTitleName.text = symbol
                tvPercentage.setBackgroundResource(
                    if (args.stockChangePercent < 0) {
                        R.drawable.shape_rectangle_decrease
                    } else {
                        R.drawable.shape_rectangle_increase
                    }
                )
            }
        }
    }

    private fun handleIntervalSuccess(stockModel: IntervalStockPricesUiModel) {
        stockPricesChart.submitData(stockModel.data)
    }

    private fun addWatchlistStock(symbol: String) {
        viewModel.addStockInWatchlist(symbol)
    }

    private fun removeWatchlistStock(symbol: String) {
        viewModel.removeStockInWatchlist(symbol)
    }

    private fun listeners() {
        binding.tbFavorite.setOnClickListener {
            if (binding.tbFavorite.isChecked) {
                addWatchlistStock(binding.tvSymbol.text.toString())
            } else {
                removeWatchlistStock(binding.tvSymbol.text.toString())
            }
        }
        binding.tlSwitchStocks.onTabSelected {
            binding.tlSwitchStocks.doSelectedTask { range, interval ->
                viewModel.getStocksDetails(args.stockSymbol, range, interval)
            }
        }

        binding.btnBuy.setOnClickListener {
            showConfirmation(binding.tvPrice.text.toString().toCurrencyDouble(), true, amount)
        }
        binding.btnSell.setOnClickListener {
            showConfirmation(binding.tvPrice.text.toString().toCurrencyDouble(), false, amount)
        }

        binding.ibtnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }


    private fun showConfirmation(price: Double, isBuying: Boolean, stocksOwned: Double) {
        val dialog = BuySellDialog(requireContext(), price, isBuying, stocksOwned)
        dialog.show()
        dialog.confirmCallback = { stockAmount ->
            confirm(stockAmount, isBuying) { isTaskSuccessful, message ->
                binding.root.makeSnackbar(message, !isTaskSuccessful)
            }
        }
    }

    private fun confirm(
        amountOfStock: Double?,
        isBuying: Boolean,
        doAfterTask: (isTaskSuccessful: Boolean, message: String) -> Unit
    ) {
        if (isBuying) {
            tradeStock(amountOfStock) { isTaskSuccessful, message ->
                doAfterTask(isTaskSuccessful, message)
            }
        } else {
            sellStock(amountOfStock) { isTaskSuccessful, message ->
                doAfterTask(isTaskSuccessful, message)
            }
        }
    }


    private fun tradeStock(
        amountOfStock: Double?,
        doAfterTask: (isTaskSuccessful: Boolean, message: String) -> Unit
    ) {

        if (balance < (amountOfStock!! * binding.tvCurrentPrice.text.toString()
                .toCurrencyDouble())
        ) {
            doAfterTask(false, "Not enough Balance")
        } else {
            if (amount == 0.0) {
                viewModel.tradeStockToOwner(
                    UsersStockUiModel(
                        symbol = binding.tvSymbol.text.toString(),
                        price = binding.tvCurrentPrice.text.toString(),
                        amountInStocks = amountOfStock
                    )
                )
                viewModel.changeBalance(
                    (balance - (amountOfStock * binding.tvCurrentPrice.text.toString()
                        .toCurrencyDouble()))
                )
                addTradeHistory(
                    TradeHistoryUiModel(
                        isBuy = true,
                        symbol = binding.tvSymbol.text.toString(),
                        money = (amountOfStock * binding.tvPrice.text.toString()
                            .toCurrencyDouble()).toString(),
                        tradeDate = getCurrentDate()
                    )
                )
                doAfterTask(true, "$amountOfStock stocks bought successfully!")
            } else {
                viewModel.tradeStockToOwner(
                    UsersStockUiModel(
                        symbol = binding.tvSymbol.text.toString(),
                        price = binding.tvCurrentPrice.text.toString(),
                        amountInStocks = amountOfStock.plus(amount)
                    )
                )
                addTradeHistory(
                    TradeHistoryUiModel(
                        isBuy = true,
                        symbol = binding.tvSymbol.text.toString(),
                        money = (amountOfStock * binding.tvPrice.text.toString()
                            .toCurrencyDouble()).toString(),
                        tradeDate = getCurrentDate()
                    )
                )
                viewModel.changeBalance(
                    (balance - (amountOfStock * binding.tvCurrentPrice.text.toString()
                        .toCurrencyDouble()))
                )
                doAfterTask(true, "$amountOfStock stocks bought successfully!")
            }
        }


    }

    private fun sellStock(
        amountOfStock: Double?,
        doAfterTask: (isTaskSuccessful: Boolean, message: String) -> Unit
    ) {

        if (amount == 0.0 || amount < amountOfStock!!) {
            doAfterTask(false, "Not enough Amount of Stocks")
        } else {
            viewModel.tradeStockToOwner(
                UsersStockUiModel(
                    symbol = binding.tvSymbol.text.toString(),
                    price = binding.tvCurrentPrice.text.toString(),
                    amountInStocks = amount.minus(amountOfStock)
                )
            )
            addTradeHistory(
                TradeHistoryUiModel(
                    isBuy = false,
                    symbol = binding.tvSymbol.text.toString(),
                    money = (amountOfStock * binding.tvPrice.text.toString()
                        .toCurrencyDouble()).toString(),
                    tradeDate = getCurrentDate()
                )
            )
            viewModel.changeBalance(
                (balance + (amountOfStock * binding.tvCurrentPrice.text.toString()
                    .toCurrencyDouble()))
            )
            doAfterTask(true, "$amountOfStock stocks sold successfully")

        }

        removeStockFromDB()
    }

    private fun removeStockFromDB() {
        asynchronously {
            viewModel.getStockAmount(binding.tvSymbol.text.toString())
            viewModel.amountOfStock.collect {
                if (it.data?.toDouble() == 0.0) {
                    viewModel.removeUsersStock(symbol = binding.tvSymbol.text.toString())
                }
            }
        }
    }

    private fun addTradeHistory(tradeHistoryUiModel: TradeHistoryUiModel) {
        viewModel.addTradeHistory(tradeHistoryUiModel)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDate(): String {
        val dateFormat: DateFormat = SimpleDateFormat("dd-MMM HH:mm:ss")
        val cal = Calendar.getInstance()

        return dateFormat.format(cal.time)
    }

    companion object {
        private const val DEFAULT_BALANCE = 0.0
        private const val DEFAULT_AMOUNT_STOCK = 0.0
    }

}