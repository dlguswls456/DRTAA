package com.drtaa.core_data.repositoryimpl

import com.drtaa.core_data.datasource.RentDataSource
import com.drtaa.core_data.repository.RentRepository
import com.drtaa.core_data.util.ResultWrapper
import com.drtaa.core_data.util.safeApiCall
import com.drtaa.core_model.network.ResponseRentCar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class RentRepositoryImpl @Inject constructor(
    private val rentDataSource: RentDataSource
) : RentRepository {
    override suspend fun getUnassignedCar(): Flow<Result<ResponseRentCar>> = flow {
        when (
            val response = safeApiCall { rentDataSource.getUnassignedCar() }
        ) {
            is ResultWrapper.Success -> {
                emit(Result.success(response.data))
                Timber.d("성공")
            }

            is ResultWrapper.GenericError -> {
                emit(Result.failure(Exception(response.message)))
                Timber.d("실패")
            }

            is ResultWrapper.NetworkError -> {
                emit(Result.failure(Exception("네트워크 에러")))
                Timber.d("네트워크 에러")
            }
        }
    }
}