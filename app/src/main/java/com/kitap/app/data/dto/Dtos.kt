package com.kitap.app.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Long,
    val name: String = "",
    val email: String = "",
    val admin: Boolean = false,
    val studentStatus: String? = null,
    val schoolLevel: String? = null,
    val initials: String = "",
    val address: String? = null,
    val phone: String? = null,
    val school: String? = null,
)

@Serializable
data class QuotaDto(
    val tier: String = "",
    val weeklyUsed: Long = 0,
    val weeklyLimit: Int = 0,
    val weeklyRemaining: Long = 0,
    val monthlyUsed: Long = 0,
    val monthlyLimit: Int = 0,
    val monthlyRemaining: Long = 0,
    val canReceive: Boolean = false,
)

@Serializable
data class MeDto(val user: UserDto, val quota: QuotaDto? = null)

@Serializable
data class ApiErrorDto(val error: String? = null)

@Serializable
data class CsrfTokenDto(
    val token: String,
    val headerName: String = "X-CSRF-TOKEN",
    val parameterName: String = "_csrf",
)

@Serializable
data class BookDto(
    val id: Long = 0,
    val title: String = "",
    val author: String? = null,
    val coverUrl: String? = null,
    val purchaseLink: String? = null,
    val description: String? = null,
)

@Serializable
data class PickupPointDto(
    val id: Long = 0,
    val name: String = "",
    val description: String? = null,
    val active: Boolean = true,
)

@Serializable
data class EligibilityDto(
    val allowed: Boolean = false,
    val code: String? = null,
    val reason: String? = null,
)

@Serializable
data class DonationDto(
    val id: Long,
    val book: BookDto = BookDto(),
    val donorName: String? = null,
    val donorInitials: String? = null,
    val description: String? = null,
    val quantity: Int = 1,
    val claimed: Long = 0,
    val remaining: Long = 0,
    val source: String? = null,
    val targetLevel: String? = null,
    val status: String? = null,
    val priorityActive: Boolean = false,
    val priorityLeft: String? = null,
    val point: PickupPointDto? = null,
    val createdAt: String? = null,
    val eligibility: EligibilityDto? = null,
)

@Serializable
data class MeetingDto(
    val point: PickupPointDto? = null,
    val note: String? = null,
    val at: String? = null,
    val arrangedAt: String? = null,
    val remindedAt: String? = null,
)

@Serializable
data class ArrangeMeetingBody(
    val pointId: Long? = null,
    val note: String? = null,
    val at: String,
)

@Serializable
data class ClaimDto(
    val id: Long,
    val status: String = "",
    val requesterName: String? = null,
    val requesterInitials: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val meeting: MeetingDto? = null,
    val conversationId: Long? = null,
    val createdAt: String? = null,
)

@Serializable
data class MyDonationDto(
    val id: Long,
    val book: BookDto = BookDto(),
    val quantity: Int = 1,
    val claimed: Long = 0,
    val remaining: Long = 0,
    val source: String? = null,
    val targetLevel: String? = null,
    val status: String = "",
    val claims: List<ClaimDto> = emptyList(),
    val point: PickupPointDto? = null,
    val pointNote: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class MyClaimDto(
    val id: Long,
    val status: String = "",
    val book: BookDto = BookDto(),
    val donorName: String? = null,
    val donorInitials: String? = null,
    val meeting: MeetingDto? = null,
    val conversationId: Long? = null,
    val createdAt: String? = null,
    val arrangedAt: String? = null,
    val shippedAt: String? = null,
    val deliveredAt: String? = null,
)

@Serializable
data class CreateDonationBody(
    val title: String,
    val author: String? = null,
    val purchaseLink: String? = null,
    val quantity: Int = 1,
    val targetLevel: String = "HEPSI",
    val source: String = "OWN",
    val description: String? = null,
    val coverUrl: String? = null,
    val pointId: Long? = null,
    val pointNote: String? = null,
)

@Serializable
data class ThankBody(val message: String? = null)

@Serializable
data class IdStatusDto(val id: Long, val status: String)

@Serializable
data class MessageDto(val message: String)

@Serializable
data class RequestDto(
    val id: Long,
    val book: BookDto = BookDto(),
    val requesterName: String? = null,
    val requesterInitials: String? = null,
    val description: String? = null,
    val source: String? = null,
    val status: String = "",
    val fulfilledByName: String? = null,
    val fulfilledByInitials: String? = null,
    val meeting: MeetingDto? = null,
    val conversationId: Long? = null,
    val createdAt: String? = null,
)

@Serializable
data class CreateRequestBody(
    val title: String,
    val author: String? = null,
    val purchaseLink: String? = null,
    val description: String? = null,
)

@Serializable
data class FulfillBody(val source: String = "OWN")

@Serializable
data class SwapListingDto(
    val id: Long,
    val book: BookDto = BookDto(),
    val note: String? = null,
    val status: String = "",
    val ownerName: String? = null,
    val ownerInitials: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class CreateSwapBookBody(
    val title: String,
    val author: String? = null,
    val note: String? = null,
    val purchaseLink: String? = null,
    val coverUrl: String? = null,
)

@Serializable
data class SwapStatusBody(val status: String)

@Serializable
data class OfferDto(
    val id: Long,
    val direction: String = "",
    val status: String = "",
    val message: String? = null,
    val counterpartName: String? = null,
    val counterpartInitials: String? = null,
    val takeBook: BookDto? = null,
    val giveBook: BookDto? = null,
    val mineHandedOver: Boolean = false,
    val theirsHandedOver: Boolean = false,
    val addressVisible: Boolean = false,
    val meeting: MeetingDto? = null,
    val conversationId: Long? = null,
    val createdAt: String? = null,
)

@Serializable
data class CreateOfferBody(
    val targetBookId: Long,
    val offeredBookId: Long,
    val message: String? = null,
)

@Serializable
data class ConversationDto(
    val id: Long,
    val kind: String = "",
    val refId: Long = 0,
    val title: String? = null,
    val counterpartName: String? = null,
    val counterpartInitials: String? = null,
    val lastMessage: String? = null,
    val lastAt: String? = null,
    val unread: Long = 0,
)

@Serializable
data class ChatMessageDto(
    val id: Long,
    val body: String = "",
    val mine: Boolean = false,
    val senderName: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class SendMessageBody(val body: String)

@Serializable
data class NotificationDto(
    val id: Long,
    val type: String = "",
    val message: String = "",
    val read: Boolean = false,
    val link: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class NotificationsResponse(
    val items: List<NotificationDto> = emptyList(),
    val unread: Long = 0,
)

@Serializable
data class ProfileUpdateBody(
    val name: String,
    val address: String? = null,
    val phone: String? = null,
    val school: String? = null,
)

@Serializable
data class PasswordChangeBody(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String,
)

@Serializable
data class StudentEmailBody(
    val email: String,
    val level: String? = null,
    val school: String? = null,
)

@Serializable
data class StudentConfirmationBody(val token: String)

@Serializable
data class ReportBody(
    val reason: String,
    val note: String? = null,
)

@Serializable
data class MyReportDto(
    val id: Long,
    val kind: String = "",
    val kindLabel: String? = null,
    val refId: Long = 0,
    val reason: String = "",
    val reasonLabel: String? = null,
    val note: String? = null,
    val status: String = "",
    val adminNote: String? = null,
    val conversationId: Long? = null,
    val createdAt: String? = null,
    val reviewedAt: String? = null,
)

@Serializable
data class BookMetadataDto(
    val title: String? = null,
    val author: String? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val found: Boolean = false,
)

@Serializable
data class PreviewBody(val purchaseLink: String)

@Serializable
data class UploadedFileDto(val url: String)

@Serializable
data class AdminStatsDto(
    val totalUsers: Int = 0,
    val pendingDocs: Int = 0,
    val donations: Int = 0,
    val delivered: Int = 0,
)

@Serializable
data class AdminReportDto(
    val id: Long,
    val kind: String = "",
    val kindLabel: String? = null,
    val refId: Long = 0,
    val reason: String = "",
    val reasonLabel: String? = null,
    val note: String? = null,
    val status: String = "",
    val adminNote: String? = null,
    val reporterId: Long? = null,
    val reporterName: String? = null,
    val reportedUserId: Long? = null,
    val reportedUserName: String? = null,
    val createdAt: String? = null,
    val reviewedAt: String? = null,
)

@Serializable
data class ResolveReportBody(
    val actioned: Boolean = false,
    val adminNote: String? = null,
)

@Serializable
data class ForgotPasswordBody(val email: String)

@Serializable
data class ResetPasswordBody(
    val token: String,
    val newPassword: String,
    val confirmPassword: String,
)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val school: String? = null,
    val level: String? = null,
    val phone: String? = null,
    val address: String? = null,
)
