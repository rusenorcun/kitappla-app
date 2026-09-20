package com.kitap.app.data.repo

import com.kitap.app.core.net.ApiResult
import com.kitap.app.core.net.safeApiCall
import com.kitap.app.core.net.safeUnitCall
import com.kitap.app.data.api.ProfileApi
import com.kitap.app.data.dto.MeDto
import com.kitap.app.data.dto.PasswordChangeBody
import com.kitap.app.data.dto.ProfileUpdateBody
import com.kitap.app.data.dto.QuotaDto
import com.kitap.app.data.dto.StudentConfirmationBody
import com.kitap.app.data.dto.StudentEmailBody
import com.kitap.app.data.dto.UserDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(private val api: ProfileApi) {
    suspend fun getProfile(): ApiResult<MeDto> = safeApiCall { api.getProfile() }

    suspend fun updateProfile(name: String, address: String?, phone: String?, school: String?): ApiResult<UserDto> =
        safeApiCall { api.updateProfile(ProfileUpdateBody(name, address, phone, school)) }

    suspend fun changePassword(current: String, new: String, confirm: String): ApiResult<Unit> =
        safeUnitCall { api.changePassword(PasswordChangeBody(current, new, confirm)) }

    suspend fun verifyStudent(email: String, level: String? = null, school: String? = null): ApiResult<UserDto> =
        safeApiCall { api.verifyStudent(StudentEmailBody(email, level, school)) }

    suspend fun confirmStudent(token: String): ApiResult<UserDto> =
        safeApiCall { api.confirmStudent(StudentConfirmationBody(token)) }

    suspend fun getQuota(): ApiResult<QuotaDto> = safeApiCall { api.getQuota() }
}
