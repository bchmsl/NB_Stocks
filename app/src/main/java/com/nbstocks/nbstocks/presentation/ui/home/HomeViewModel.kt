package com.nbstocks.nbstocks.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nbstocks.nbstocks.common.extensions.*
import com.nbstocks.nbstocks.common.handlers.Resource
import com.nbstocks.nbstocks.data.mapper.toUserStockUiModel
import com.nbstocks.nbstocks.data.repositories.db_add_users_stock.DbManageUsersStockRepositoryImpl
import com.nbstocks.nbstocks.presentation.model.ViewState
import com.nbstocks.nbstocks.presentation.ui.stock_details.model.UsersStockUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val usersStockRepositoryImpl: DbManageUsersStockRepositoryImpl
) : ViewModel() {

    private val _usersStockState = MutableStateFlow<ViewState<List<UsersStockUiModel>>>(ViewState())
    val usersStockState: StateFlow<ViewState<List<UsersStockUiModel>>> get() = _usersStockState

    fun getUsersStocks() {
        viewModelScope.launch {
            usersStockRepositoryImpl.getUsersStock()
            _usersStockState.resetViewState()
            usersStockRepositoryImpl.stockState.collect { resource ->

                resource.doOnSuccess {
                    _usersStockState.emitSuccessViewState(this) {
                        it.map { it.toUserStockUiModel() }
                    }
                }.doOnFailure {
                    _usersStockState.emitErrorViewState(this) { it }
                }
            }
        }
    }
}